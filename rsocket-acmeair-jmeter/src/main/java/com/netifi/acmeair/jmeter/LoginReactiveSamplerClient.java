package com.netifi.acmeair.jmeter;

import java.util.Map;

import com.netifi.broker.BrokerClient;
import io.rsocket.RSocket;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netifi.acmeair.LoginRequest;
import com.netifi.acmeair.LoginResponse;
import com.netifi.acmeair.LoginServiceClient;
import reactor.core.publisher.Mono;

public class LoginReactiveSamplerClient extends AbstractReactiveJavaSamplerClient<LoginResponse> {

    private static final Logger log = LoggerFactory.getLogger(LoginReactiveSamplerClient.class);

    private static final String USER_ID = "userId";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String SESSION_ID = "sessionId";

    private RSocket rSocket;
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
    protected Map<String, Object> extractParameters(Map<String, Object> params, JavaSamplerContext context) {
        JMeterVariables variables = context.getJMeterVariables();

        params.put(USER_ID, variables.get(USER_ID));
        params.put(PASSWORD, context.getParameter(PASSWORD));

        return params;
    }

    @Override
    protected Mono<Void> prepareTestRun(Map<String, Object> params, PublisherInstrumentation<LoginResponse> instrumentation) {
        String userId = (String) params.get(USER_ID);
        String username = String.format("uid%s@email.com", userId);
        String password = (String) params.get(PASSWORD);

        if (log.isDebugEnabled()) {
            log.debug("Retrieved variables : [userId={}] [username={}] [password={}]", userId, username, password);
        }

        LoginRequest body = LoginRequest.newBuilder()
                                        .setUsername(username)
                                        .setPassword(password)
                                        .build();

        return ReactiveVariableHolder
            .variables()
            .flatMap(variables -> {
                if (log.isDebugEnabled()) {
                    log.debug("Retrieved Reactor Context Variables [{}]", variables);
                }

                if (log.isDebugEnabled()) {
                    log.debug("Sending Body [{}]", body.toString());
                }

                return client
                    .login(body)
                    .doOnNext(lr -> {
                        if (log.isDebugEnabled()) {
                            log.debug("Logged in for variables : [userId={}] [username={}] [password={}]. Current session ID is [{}]", userId, username, password, lr.getSessionId());
                        }

                        variables.put(USER_ID, userId);
                        variables.put(SESSION_ID, lr.getSessionId());
                        variables.put(USERNAME, username);
                    })
                    .transform(instrumentation);
            });
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Test teardown is started");
        }

//        rSocket.dispose();

        if (log.isDebugEnabled()) {
            log.debug("Test teardown is finished");
        }
    }
}
