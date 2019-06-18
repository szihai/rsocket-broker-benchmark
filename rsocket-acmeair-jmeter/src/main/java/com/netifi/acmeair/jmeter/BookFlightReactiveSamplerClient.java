package com.netifi.acmeair.jmeter;

import java.util.Map;

import com.google.protobuf.GeneratedMessageV3;
import com.netifi.broker.BrokerClient;
import io.rsocket.RSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netifi.acmeair.BookFlightRequest;
import com.netifi.acmeair.BookOnewayFlightRequest;
import com.netifi.acmeair.BookingServiceClient;
import reactor.core.publisher.Mono;

public class BookFlightReactiveSamplerClient extends AbstractReactiveJavaSamplerClient<GeneratedMessageV3> {

    private static final Logger log = LoggerFactory.getLogger(BookFlightReactiveSamplerClient.class);

    private static final String USERNAME = "username";
    private static final String FLIGHT_CONTEXT_VAR = "flight.context";

    private RSocket              rSocket;
    private BookingServiceClient client;

    @Override
    protected void setupTestClient(BrokerClient brokerClient) {
        rSocket = brokerClient.group("netifi.acmeair.booking");
        client = new BookingServiceClient(rSocket);
    }

    @Override
    protected Mono<Void> prepareTestRun(Map<String, Object> parameters, PublisherInstrumentation<GeneratedMessageV3> instrumentation) {
        return ReactiveVariableHolder
            .variables()
            .flatMap(variables -> {
                if (log.isDebugEnabled()) {
                    log.debug("Retrieved Reactor Context Variables [{}]", variables);
                }

                FlightsContext flightsContext = (FlightsContext) variables.get(FLIGHT_CONTEXT_VAR);
                String username = (String) variables.get(USERNAME);

                if (log.isDebugEnabled()) {
                    log.debug("Current flightsContext is [{}]", flightsContext);
                }

                if (flightsContext.isFlightAvailable()) {
                    if (flightsContext.isOneWay()) {
                        BookOnewayFlightRequest body = BookOnewayFlightRequest.newBuilder()
                                                                              .setToFlightId(flightsContext.getToFlightId())
                                                                              .setUsername(username)
                                                                              .build();

                        if (log.isDebugEnabled()) {
                            log.debug("Sending Body [{}]", body.toString());
                        }

                        return client.bookOnewayFlight(body)
                                     .cast(GeneratedMessageV3.class)
                                     .as(instrumentation);
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

                        return client.bookFlight(body)
                                     .cast(GeneratedMessageV3.class)
                                     .as(instrumentation);
                    }
                }

                return Mono.empty();
            });
    }
}
