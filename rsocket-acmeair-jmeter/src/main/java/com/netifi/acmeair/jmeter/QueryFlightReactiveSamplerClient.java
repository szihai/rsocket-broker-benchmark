package com.netifi.acmeair.jmeter;

import java.util.Calendar;
import java.util.Map;
import java.util.Random;

import com.netifi.broker.BrokerClient;
import io.rsocket.RSocket;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netifi.acmeair.Flight;
import com.netifi.acmeair.FlightServiceClient;
import com.netifi.acmeair.GetTripFlightsRequest;
import com.netifi.acmeair.GetTripFlightsResponse;
import reactor.core.publisher.Mono;

public class QueryFlightReactiveSamplerClient extends AbstractReactiveJavaSamplerClient<GetTripFlightsResponse> {

    private static final Logger log = LoggerFactory.getLogger(QueryFlightReactiveSamplerClient.class);

    private static final String USERNAME = "username";
    private static final String SESSION_ID = "sessionId";
    private static final String CUSTOMER = "customer";

    private static final String FLIGHT_CONTEXT_VAR = "flight.context";

    private static final String FROM_AIRPORT = "fromAirport";
    private static final String TO_AIRPORT = "toAirport";
    private static final String FROM_DATE = "fromDate";
    private static final String RETURN_DATE = "returnDate";
    private static final String ONE_WAY = "oneWay";


    private static final String FLIGHT_TO_COUNT = "flightToCount";
    private static final String FLIGHT_RET_COUNT = "flightReturnCount";



    private RSocket             rSocket;
    private FlightServiceClient client;

    @Override
    protected void setupTestClient(BrokerClient brokerClient) {
        rSocket = brokerClient.group("netifi.acmeair.flight");
        client = new FlightServiceClient(rSocket);
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument(FROM_AIRPORT, "");
        defaultParameters.addArgument(TO_AIRPORT, "");
        defaultParameters.addArgument(FROM_DATE, "");
        defaultParameters.addArgument(RETURN_DATE, "");
        defaultParameters.addArgument(ONE_WAY, "false");
        return defaultParameters;
    }

    @Override
    protected Map<String, Object> extractParameters(Map<String, Object> params, JavaSamplerContext context) {


        params.put(FROM_AIRPORT, context.getParameter(FROM_AIRPORT));
        params.put(TO_AIRPORT, context.getParameter(TO_AIRPORT));

        Calendar aDay = Calendar.getInstance();
        aDay.add(Calendar.DATE, new Random().nextInt(6));
        params.put(FROM_DATE, aDay.toInstant().toEpochMilli());
        aDay = Calendar.getInstance();
        aDay.add(Calendar.DATE, new Random().nextInt(7) + 6);
        params.put(RETURN_DATE, aDay.toInstant().toEpochMilli());
        params.put(ONE_WAY, Boolean.parseBoolean(context.getParameter(ONE_WAY)));

        return params;
    }

    @Override
    protected Mono<Void> prepareTestRun(Map<String, Object> parameters, PublisherInstrumentation<GetTripFlightsResponse> instrumentation) {
        return ReactiveVariableHolder
            .variables()
            .flatMap(variables -> {
                if (log.isDebugEnabled()) {
                    log.debug("Retrieved Reactor Context Variables [{}]", variables);
                }

                String fromAirport = (String) parameters.get(FROM_AIRPORT);
                if (log.isDebugEnabled()) {
                    log.debug("Extracted from Airport [{}]", fromAirport);
                }
                String toAirport = (String) parameters.get(TO_AIRPORT);
                if (log.isDebugEnabled()) {
                    log.debug("Extracted to Airport [{}]", toAirport);
                }
                long fromDate = (long) parameters.get(FROM_DATE);
                if (log.isDebugEnabled()) {
                    log.debug("Extracted from date [{}]", fromDate);
                }
                long returnDate = (long) parameters.get(RETURN_DATE);
                if (log.isDebugEnabled()) {
                    log.debug("Extracted return date [{}]", returnDate);
                }
                boolean oneWay = (boolean) parameters.get(ONE_WAY);
                if (log.isDebugEnabled()) {
                    log.debug("Extracted one way [{}]", oneWay);
                }
                GetTripFlightsRequest body = GetTripFlightsRequest.newBuilder()
                                                                  .setOneWay(oneWay)
                                                                  .setReturnDate(returnDate)
                                                                  .setFromDate(fromDate)
                                                                  .setToAirport(toAirport)
                                                                  .setFromAirport(fromAirport)
                                                                  .build();

                if (log.isDebugEnabled()) {
                    log.debug("Sending Body [{}]", body.toString());
                }

                return client.getTripsFlight(body)
                             .doOnNext(t -> postProcess(variables, t))
                             .as(instrumentation);
            });
    }


    private void postProcess(Map<String, Object> variables, GetTripFlightsResponse tripFlights) {
        FlightsContext flightsContext =
                (FlightsContext) variables.getOrDefault(FLIGHT_CONTEXT_VAR, FlightsContext.builder()
                                                                                          .withFlightAvailable(false)
                                                                                          .build());

        if (log.isDebugEnabled()) {
            log.debug("Post process flight query result [{}].", tripFlights);
            log.debug("Current Flight Context is [{}].", flightsContext);
        }

        FlightsContext.Builder builder = flightsContext.mutate();
        int toFlightCount = tripFlights.getToFlightCount();
        int returnFlightCount = tripFlights.getReturnFlightCount();

        if (toFlightCount == 0 && returnFlightCount == 0) {
            variables.put(
                FLIGHT_CONTEXT_VAR,
                builder.withFlightAvailable(false)
                       .build()
            );
        }
        else {
            boolean hasToFlights = toFlightCount > 0;
            boolean hasReturnFlights = hasToFlights && returnFlightCount > 0;

            builder.withOneWay(hasToFlights && !hasReturnFlights);
            variables.put(ONE_WAY, hasToFlights && !hasReturnFlights);

            if (hasToFlights) {
                Flight flight = tripFlights.getToFlight(toFlightCount - 1);

                builder.withNumberOfToFlights(toFlightCount)
                       .withFlightAvailable(true)
                       .withToFlightId(flight.getId())
                       .withToFlightSegmentId(flight.getSegment().getFlightSegmentId());

                variables.put(FLIGHT_TO_COUNT, toFlightCount);
            }

            if (hasReturnFlights) {
                Flight flight = tripFlights.getToFlight(returnFlightCount - 1);

                builder.withNumberOfReturnFlights(returnFlightCount)
                       .withReturnFlightId(flight.getId())
                       .withReturnFlightSegmentId(flight.getSegment().getFlightSegmentId());

                variables.put(FLIGHT_RET_COUNT, returnFlightCount);
            }

            variables.put(
                FLIGHT_CONTEXT_VAR,
                builder.build()
            );
        }
    }
}
