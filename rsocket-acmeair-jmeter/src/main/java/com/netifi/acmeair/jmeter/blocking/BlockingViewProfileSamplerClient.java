package com.netifi.acmeair.jmeter.blocking;

import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import com.netifi.broker.BrokerClient;
import io.rsocket.RSocket;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netifi.acmeair.CustomerServiceClient;
import com.netifi.acmeair.GetCustomerRequest;
import com.netifi.acmeair.GetCustomerResponse;

import java.time.Duration;

public class BlockingViewProfileSamplerClient extends AbstractBlockingJavaSamplerClient {

    private static final Logger log = LoggerFactory.getLogger(BlockingViewProfileSamplerClient.class);

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
        if (username ==  null || sessionId == null) {
            return Empty.getDefaultInstance();
        }
        
        GetCustomerRequest body = GetCustomerRequest.newBuilder()
                                                    .setUsername(username)
                                                    .setToken(sessionId)
                                                    .build();

        if (log.isDebugEnabled()) {
            log.debug("Sending Body [{}]", body.toString());
        }

        beforeCall.run();

        GetCustomerResponse response = client.getCustomer(body).block(Duration.ofSeconds(5));

        if (log.isDebugEnabled()) {
            log.debug("The response is [{}]", response);
        }

        if (response != null && response.hasCustomer()) {
            jMeterVariables.putObject(CUSTOMER, response.getCustomer());
        }
        else if(response == null) {
            return Empty.getDefaultInstance();
        }

        return response;
    }
}
