package com.netifi.acmeair.jmeter;

import java.util.Arrays;
import java.util.function.Function;

import com.google.protobuf.GeneratedMessageV3;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PublisherInstrumentation<T extends GeneratedMessageV3>
        implements Function<Publisher<T>, Mono<Void>> {


    private static final Logger log = LoggerFactory.getLogger(PublisherInstrumentation.class);

    private final ReactiveSampleResult sample;

    public PublisherInstrumentation(ReactiveSampleResult sample) {
        this.sample = sample;
    }

    @Override
    public Mono<Void> apply(Publisher<T> publisher) {
        if (log.isDebugEnabled()) {
            log.debug("Instrumentation is applying for sample result [{}]", sample);
        }

        Mono<byte[]> toInstrument;
        if (publisher instanceof Mono) {
            toInstrument = Mono.from(publisher)
                               .doOnSubscribe(__ -> {
                                   if (log.isDebugEnabled()) {
                                       log.debug("Subscribed. Mark sample result [{}] as connected", sample);
                                   }
                                   sample.connectEnd();
                               })
                               .doOnTerminate(() -> {
                                   if (log.isDebugEnabled()) {
                                       log.debug("Terminated. Latency end for sample result [{}]" , sample);
                                   }

                                   sample.latencyEnd();
                               })
                               .map(GeneratedMessageV3::toByteArray);
        }
        else {
            toInstrument = Flux.from(publisher)
                               .doOnSubscribe(__ -> {
                                   if (log.isDebugEnabled()) {
                                       log.debug("Subscribed. Mark sample result [{}] as connected", sample);
                                   }
                                   sample.connectEnd();
                               })
                               .doOnTerminate(() -> {
                                   if (log.isDebugEnabled()) {
                                       log.debug("Terminated. Latency end for sample result [{}]" , sample);
                                   }

                                   sample.latencyEnd();
                               })
                               .map(GeneratedMessageV3::toByteArray)
                               .reduce((a, b) -> {
                                   byte[] rv = new byte[a.length + b.length];

                                   System.arraycopy(a, 0, rv, 0, a.length);
                                   System.arraycopy(b, 0, rv, a.length, b.length);

                                   return rv;
                               });
        }

        return toInstrument
            .doAfterSuccessOrError((data, t) -> {
                sample.setValid(true);

                if (t != null) {
                    if (log.isErrorEnabled()) {
                        log.error("Sample Result [{}] Finished with error", sample);
                        log.error("Error ", t);
                    }
                    sample.setDataType("text");
                    sample.setResponseData(Arrays.toString(t.getStackTrace())
                                                 .getBytes());
                    sample.setResponseMessage(t.getMessage());
                    sample.setSuccessful(false);
                    sample.sampleEnd();
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Finished successfully. Sample result [{}]", sample);
                    }

                    if (data != null) {
                        sample.setResponseData(data);
                        sample.setBodySize(data.length);
                    }

                    sample.setSuccessful(true);
                    sample.sampleEnd();
                }
            })
            .then();
    }
}
