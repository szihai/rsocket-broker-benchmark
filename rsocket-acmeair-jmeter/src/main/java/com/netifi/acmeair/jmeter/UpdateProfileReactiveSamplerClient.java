package com.netifi.acmeair.jmeter;

import java.util.Map;

import com.google.protobuf.Empty;
import com.netifi.broker.BrokerClient;
import io.rsocket.RSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netifi.acmeair.Customer;
import com.netifi.acmeair.CustomerServiceClient;
import com.netifi.acmeair.UpdateCustomerRequest;
import reactor.core.publisher.Mono;

public class UpdateProfileReactiveSamplerClient extends AbstractReactiveJavaSamplerClient<Empty> {

    private static final Logger log = LoggerFactory.getLogger(UpdateProfileReactiveSamplerClient.class);

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
    protected Mono<Void> prepareTestRun(Map<String, Object> parameters,
            PublisherInstrumentation<Empty> instrumentation) {
        return ReactiveVariableHolder
            .variables()
            .flatMap(variables -> {
                if (log.isDebugEnabled()) {
                    log.debug("Retrieved Reactor Context Variables [{}]", variables);
                }

                String username = (String) variables.get(USERNAME);
                String sessionId = (String) variables.get(SESSION_ID);
                Customer customer = (Customer) variables.get(CUSTOMER);
                UpdateCustomerRequest body = UpdateCustomerRequest.newBuilder()
                                                                  .setUsername(username)
                                                                  .setToken(sessionId)
                                                                  .setCustomer(customer)
                                                                  .build();

                if (log.isDebugEnabled()) {
                    log.debug("Sending Body [{}]", body.toString());
                }

                return client.updateCustomer(body)
                             .as(instrumentation);
            });
    }
}
