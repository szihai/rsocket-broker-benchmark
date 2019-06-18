package com.netifi.acmeair.jmeter.blocking;

import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import com.netifi.broker.BrokerClient;
import io.rsocket.RSocket;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netifi.acmeair.BlockingBookingServiceClient;
import com.netifi.acmeair.BookFlightRequest;
import com.netifi.acmeair.BookOnewayFlightRequest;
import com.netifi.acmeair.BookingServiceClient;
import com.netifi.acmeair.jmeter.FlightsContext;

import java.time.Duration;

public class BlockingBookFlightSamplerClient extends AbstractBlockingJavaSamplerClient {

    private static final Logger log = LoggerFactory.getLogger(BlockingBookFlightSamplerClient.class);

    private static final String USERNAME = "username";
    private static final String FLIGHT_CONTEXT_VAR = "flight.context";

    private RSocket                      rSocket;
    private BookingServiceClient client;

    @Override
    protected void setupTestClient(BrokerClient brokerClient) {
        rSocket = brokerClient.group("netifi.acmeair.booking");
        client = new BookingServiceClient(rSocket);
    }

    @Override
    protected GeneratedMessageV3 doCall(JavaSamplerContext context, Runnable beforeCall) {
        JMeterVariables jMeterVariables = context.getJMeterVariables();
        FlightsContext flightsContext = (FlightsContext) jMeterVariables.getObject(FLIGHT_CONTEXT_VAR);
        String username = jMeterVariables.get(USERNAME);
        
        
        if (username == null) {
            return Empty.getDefaultInstance();
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Current flightsContext is [{}]", flightsContext);
        }

        if (flightsContext.isOneWay()) {
            BookOnewayFlightRequest body = BookOnewayFlightRequest.newBuilder()
                                                                  .setToFlightId(flightsContext.getToFlightId())
                                                                  .setUsername(username)
                                                                  .build();

            if (log.isDebugEnabled()) {
                log.debug("Sending Body [{}]", body.toString());
            }

            return client.bookOnewayFlight(body).block(Duration.ofSeconds(5));
        }
        else {
            BookFlightRequest body = BookFlightRequest.newBuilder()
                                                      .setToFlightId(flightsContext.getToFlightId())
                                                      .setReturnFlightId(flightsContext.getReturnFlightId())
                                                      .setUsername(username)
                                                      .build();

            if (log.isDebugEnabled()) {
                log.debug("Sending Body [{}]", body.toString());
            }

            return client.bookFlight(body).block();
        }
    }
}
