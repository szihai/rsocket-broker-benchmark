package com.netifi.acmeair.jmeter.blocking;

import com.google.protobuf.GeneratedMessageV3;
import com.netifi.broker.BrokerClient;
import io.rsocket.RSocket;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netifi.acmeair.BlockingCustomerServiceClient;
import com.netifi.acmeair.Customer;
import com.netifi.acmeair.CustomerServiceClient;
import com.netifi.acmeair.UpdateCustomerRequest;

import java.time.Duration;

public class BlockingUpdateProfileSamplerClient extends AbstractBlockingJavaSamplerClient {

    private static final Logger log = LoggerFactory.getLogger(BlockingUpdateProfileSamplerClient.class);

    private static final String USERNAME = "username";
    private static final String SESSION_ID = "sessionId";
    private static final String CUSTOMER = "customer";

    private RSocket                       rSocket;
    private CustomerServiceClient client;

    @Override
    protected void setupTestClient(BrokerClient brokerClient) {
        rSocket = brokerClient.group("netifi.acmeair.customer");
        client = new CustomerServiceClient(rSocket);
    }

    @Override
    protected GeneratedMessageV3 doCall(JavaSamplerContext context, Runnable beforeCall) {
        JMeterVariables jMeterVariables = context.getJMeterVariables();

        String username = jMeterVariables.get(USERNAME);
        String sessionId = jMeterVariables.get(SESSION_ID);
        Customer customer = (Customer) jMeterVariables.getObject(CUSTOMER);

        UpdateCustomerRequest body = UpdateCustomerRequest.newBuilder()
                                                          .setUsername(username)
                                                          .setToken(sessionId)
                                                          .setCustomer(customer)
                                                          .build();

        if (log.isDebugEnabled()) {
            log.debug("Sending Body [{}]", body.toString());
        }

        beforeCall.run();

        return client.updateCustomer(body).block(Duration.ofSeconds(5));
    }
}
