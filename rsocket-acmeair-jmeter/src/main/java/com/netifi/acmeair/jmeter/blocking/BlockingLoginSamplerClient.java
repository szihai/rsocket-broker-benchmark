package com.netifi.acmeair.jmeter.blocking;

import com.google.protobuf.GeneratedMessageV3;
import com.netifi.broker.BrokerClient;
import io.rsocket.RSocket;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netifi.acmeair.BlockingLoginServiceClient;
import com.netifi.acmeair.LoginRequest;
import com.netifi.acmeair.LoginResponse;
import com.netifi.acmeair.LoginServiceClient;

import java.time.Duration;

public class BlockingLoginSamplerClient extends AbstractBlockingJavaSamplerClient {

    private static final Logger log = LoggerFactory.getLogger(BlockingLoginSamplerClient.class);

    private static final String USER_ID = "userId";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String SESSION_ID = "sessionId";

    private RSocket                    rSocket;
    private LoginServiceClient client;

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument(PASSWORD, "");
        return defaultParameters;
    }

    @Override
    protected void setupTestClient(BrokerClient brokerClient) {
        rSocket = brokerClient.group("netifi.acmeair.login");
        client = new LoginServiceClient(rSocket);
    }


    @Override
    protected GeneratedMessageV3 doCall(JavaSamplerContext context, Runnable beforeCall) {
        JMeterVariables jMeterVariables = context.getJMeterVariables();
        String userId = jMeterVariables.get(USER_ID);
        String username = String.format("uid%s@email.com", userId);
        String password = context.getParameter(PASSWORD);

        if (log.isDebugEnabled()) {
            log.debug("Retrieved variables : [userId={}] [username={}] [password={}]", userId, username, password);
        }


        LoginRequest body = LoginRequest.newBuilder()
                                        .setUsername(username)
                                        .setPassword(password)
                                        .build();

        if (log.isDebugEnabled()) {
            log.debug("Sending Body [{}]", body.toString());
        }

        beforeCall.run();

        LoginResponse login = client.login(body).block(Duration.ofSeconds(5));

        jMeterVariables.put(USERNAME, username);
        jMeterVariables.put(SESSION_ID, login.getSessionId());

        return login;
    }
}
