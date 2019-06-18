package com.netifi.acmeair.jmeter;

import java.util.Map;

import com.google.protobuf.GeneratedMessageV3;
import com.netifi.broker.BrokerClient;
import io.rsocket.RSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netifi.acmeair.BookingServiceClient;
import com.netifi.acmeair.CancelBookingRequest;
import reactor.core.publisher.Mono;

public class CancelBookingReactiveSamplerClient extends AbstractReactiveJavaSamplerClient<GeneratedMessageV3> {

    private static final Logger log = LoggerFactory.getLogger(
            CancelBookingReactiveSamplerClient.class);

    private static final String USERNAME = "username";
    private static final String BOOKING_CONTEXT_VAR = "booking.context";

    private RSocket              rSocket;
    private BookingServiceClient client;

    @Override
    protected void setupTestClient(BrokerClient brokerClient) {
        rSocket = brokerClient.group("netifi.acmeair.booking");
        client = new BookingServiceClient(rSocket);
    }

    @Override
    protected Mono<Void> prepareTestRun(Map<String, Object> parameters, PublisherInstrumentation<GeneratedMessageV3> instrumentation) {
        return ReactiveVariableHolder
            .variables()
            .flatMap(variables -> {
                if (log.isDebugEnabled()) {
                    log.debug("Retrieved Reactor Context Variables [{}]", variables);
                }

                BookingContext bookingContext = (BookingContext) variables.get(BOOKING_CONTEXT_VAR);
                String username = (String) variables.get(USERNAME);

                CancelBookingRequest body = CancelBookingRequest.newBuilder()
                                                                .setUsername(username)
                                                                .build();

//                client.cancelBooking()

                return Mono.empty();
            });
    }
}
