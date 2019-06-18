package com.netifi.acmeair.jmeter;

import java.util.function.Function;

import com.google.protobuf.GeneratedMessageV3;
import com.netifi.broker.BrokerClient;
import io.rsocket.RSocket;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.reactivestreams.Publisher;

public class ReactiveSamplerClient extends AbstractJavaSamplerClient {


    private static final String TYPE         = "type";
    private static final String TYPE_DEFAULT = "group";

    private static final String GROUP = "group.name";

    private static final String DESTINATION = "destination.name";

    private final Function<RSocket, Function<? super GeneratedMessageV3, Publisher<? extends GeneratedMessageV3>>> functorFactory;
    private final DataFeeder<?> dataFeeder;

    private RSocket rSocket;
    private Function<? super GeneratedMessageV3, Publisher<? extends GeneratedMessageV3>> functor;

    public ReactiveSamplerClient(Function<RSocket, Function<? super GeneratedMessageV3, Publisher<? extends GeneratedMessageV3>>> factory,
            DataFeeder<?> feeder) {
        functorFactory = factory;
        dataFeeder = feeder;
    }

    @Override
    public void setupTest(JavaSamplerContext context) {
        BrokerClient brokerClient = (BrokerClient) context.getJMeterVariables().getObject(NetifiSetup.VAR_CLIENT);

        String type = context.getParameter(TYPE, TYPE_DEFAULT);
        String group = context.getParameter(GROUP, "");
        String destination = context.getParameter(DESTINATION, "");

        switch (type) {
            case "broadcast":
                rSocket = brokerClient.broadcast(group);
                break;
            case "group":
                rSocket = brokerClient.group(group);
                break;
            case "destination":
                rSocket = brokerClient.destination(destination, group);
                break;
            default:
                throw new IllegalArgumentException("Incorrect messaging type");
        }
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument(TYPE, TYPE_DEFAULT);
        defaultParameters.addArgument(GROUP, "");
        defaultParameters.addArgument(DESTINATION, "");
        return defaultParameters;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SampleResult runTest(JavaSamplerContext context) {
//        dataFeeder.next()
//        String method = context.getParameter(METHOD, METHOD_DEFAULT);
//        String url = getURL(context);
//        long sampleTimeout = context.getLongParameter(SAMPLE_TIMEOUT,
//                Long.valueOf(SAMPLE_TIMEOUT_DEFAULT));
//        long initialResponseReadDelay = context.getLongParameter(INITIAL_RESPONSE_READ_DELAY,
//                Long.valueOf(INITIAL_RESPONSE_READ_DELAY_DEFAULT));
//        long responseReadDelay = context.getLongParameter(RESPONSE_READ_DELAY,
//                Long.valueOf(RESPONSE_READ_DELAY_DEFAULT));
//        String accept = context.getParameter(ACCEPT, ACCEPT_DEFAULT);
//
//        return this.client.request(method, url, sampleTimeout, initialResponseReadDelay,
//                responseReadDelay, accept);
        return null;
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        super.teardownTest(context);
    }
}
