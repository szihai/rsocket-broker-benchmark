package com.netifi.acmeair.jmeter.blocking;

import java.io.Serializable;

import com.google.protobuf.GeneratedMessageV3;
import com.netifi.acmeair.jmeter.ProtobufSampleResult;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.threads.JMeterContext;

public class ProtobufExtractor extends AbstractScopedTestElement implements PostProcessor,
                                                                            Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public void process() {
        JMeterContext context = getThreadContext();
        ProtobufSampleResult previousResult = (ProtobufSampleResult) context.getPreviousResult();

        GeneratedMessageV3 message = previousResult.getMessage();
//        message.getField()
    }
}
