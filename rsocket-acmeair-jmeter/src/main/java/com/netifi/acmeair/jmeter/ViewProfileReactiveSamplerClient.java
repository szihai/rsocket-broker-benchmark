package com.netifi.acmeair.jmeter;

import java.util.Map;

import com.netifi.broker.BrokerClient;
import io.rsocket.RSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netifi.acmeair.CustomerServiceClient;
import com.netifi.acmeair.GetCustomerRequest;
import com.netifi.acmeair.GetCustomerResponse;
import reactor.core.publisher.Mono;

public class ViewProfileReactiveSamplerClient extends AbstractReactiveJavaSamplerClient<GetCustomerResponse> {

    private static final Logger log = LoggerFactory.getLogger(ViewProfileReactiveSamplerClient.class);

    private static final String USERNAME = "username";
    private static final String SESSION_ID = "sessionId";
    private static final String CUSTOMER = "customer";

    private RSocket               rSocket;
    private CustomerServiceClient client;

    @Override
    protected void setupTestClient(BrokerClient brokerClient) {
        rSocket = brokerClient.group("netifi.acmeair.customer");
        client = new CustomerServiceClient(rSocket);
    }

    @Override
    protected Mono<Void> prepareTestRun(Map<String, Object> parameters, PublisherInstrumentation<GetCustomerResponse> instrumentation) {
        return ReactiveVariableHolder
            .variables()
            .flatMap(variables -> {
                if (log.isDebugEnabled()) {
                    log.debug("Retrieved Reactor Context Variables [{}]", variables);
                }

                String username = (String) variables.get(USERNAME);
                String sessionId = (String) variables.get(SESSION_ID);
                GetCustomerRequest body = GetCustomerRequest.newBuilder()
                                                            .setUsername(username)
                                                            .setToken(sessionId)
                                                            .build();

                if (log.isDebugEnabled()) {
                    log.debug("Sending Body [{}]", body.toString());
                }

                return client
                    .getCustomer(body)
                    .doOnNext(r -> {
                        if (log.isDebugEnabled()) {
                            log.debug("Retrieved profile for variables : [username={}] [sessionId={}]. Profile is [{}]", username, sessionId, r.getCustomer());
                        }

                        variables.put(CUSTOMER, r.getCustomer());
                    })
                    .as(instrumentation);
            });
    }
}
