package com.netifi.acmeair.jmeter;

import com.netifi.broker.BrokerClient;
import io.rsocket.RSocket;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import com.netifi.acmeair.LoginServiceClient;
import com.netifi.acmeair.LogoutRequest;

public class LogoutReactiveSamplerClient extends AbstractJavaSamplerClient {


    private static final String USER_ID = "userId";
    private static final String SESSION_ID = "sessionId";

    private RSocket rSocket;
    private LoginServiceClient client;

    @Override
    public void setupTest(JavaSamplerContext context) {
        BrokerClient brokerClient = (BrokerClient) context.getJMeterVariables().getObject(NetifiSetup.VAR_CLIENT);

        rSocket = brokerClient.group("netifi.acmeair.login");
        client = new LoginServiceClient(rSocket);
    }

    @Override
    @SuppressWarnings("unchecked")
    public SampleResult runTest(JavaSamplerContext context) {
        ReactiveSampleResult result = new ReactiveSampleResult();
        PublisherInstrumentation instrumentation = new PublisherInstrumentation<>(result);

        String userId = context.getJMeterVariables().get(USER_ID);
        String sessionId = context.getJMeterVariables().get(SESSION_ID);
        String username = String.format("uid%s@email.com", userId);

        LogoutRequest body = LogoutRequest.newBuilder()
                                          .setUsername(username)
                                          .setSessionId(sessionId)
                                          .build();

        result.setExecutionResult(
            client.logout(body)
                  .transform(instrumentation)
        );

        return result;
    }
}
