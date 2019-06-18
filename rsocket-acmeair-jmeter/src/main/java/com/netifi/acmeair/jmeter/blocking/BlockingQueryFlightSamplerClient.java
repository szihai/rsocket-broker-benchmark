package com.netifi.acmeair.jmeter.blocking;

import com.google.protobuf.GeneratedMessageV3;
import com.netifi.broker.BrokerClient;
import io.rsocket.RSocket;
import com.netifi.acmeair.Flight;
import com.netifi.acmeair.FlightServiceClient;
import com.netifi.acmeair.GetTripFlightsRequest;
import com.netifi.acmeair.GetTripFlightsResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netifi.acmeair.jmeter.FlightsContext;
import reactor.core.Exceptions;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

public class BlockingQueryFlightSamplerClient extends AbstractBlockingJavaSamplerClient {

  private static final Logger log = LoggerFactory.getLogger(BlockingQueryFlightSamplerClient.class);

  private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT =
      ThreadLocal.withInitial(() -> new SimpleDateFormat("EEE MMM dd 00:00:00 z yyyy"));

  private static final String FLIGHT_CONTEXT_VAR = "flight.context";
  private static final String FLIGHT_HAS_VAR = "flight.has";

  private static final String FROM_AIRPORT = "fromAirport";
  private static final String TO_AIRPORT = "toAirport";
  private static final String FROM_DATE = "fromDate";
  private static final String RETURN_DATE = "returnDate";
  private static final String ONE_WAY = "oneWay";

  private static final String FLIGHT_TO_COUNT = "flightToCount";
  private static final String FLIGHT_RET_COUNT = "flightReturnCount";

  private RSocket rSocket;
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
  protected GeneratedMessageV3 doCall(JavaSamplerContext context, Runnable beforeCall) {
    JMeterVariables jMeterVariables = context.getJMeterVariables();

    String fromAirport = context.getParameter(FROM_AIRPORT);
    if (log.isDebugEnabled()) {
      log.debug("Extracted from Airport [{}]", fromAirport);
    }

    String toAirport = context.getParameter(TO_AIRPORT);
    if (log.isDebugEnabled()) {
      log.debug("Extracted to Airport [{}]", toAirport);
    }

    String fromDateString = context.getParameter(FROM_DATE);
    String returnDateString = context.getParameter(RETURN_DATE);

    Date fromDate;
    Date returnDate;

    try {
      fromDate = DATE_FORMAT.get().parse(fromDateString);
      returnDate = DATE_FORMAT.get().parse(returnDateString);
    } catch (Exception e) {
      throw Exceptions.propagate(e);
    }

    boolean oneWay = Boolean.parseBoolean(context.getParameter(ONE_WAY));
    if (log.isDebugEnabled()) {
      log.debug("Extracted one way [{}]", oneWay);
    }
    GetTripFlightsRequest body =
        GetTripFlightsRequest.newBuilder()
            .setOneWay(oneWay)
            .setFromDate(fromDate.getTime())
            .setReturnDate(returnDate.getTime())
            .setToAirport(toAirport)
            .setFromAirport(fromAirport)
            .build();

    if (log.isDebugEnabled()) {
      log.debug("Sending Body [{}]", body.toString());
    }

    beforeCall.run();

    GetTripFlightsResponse response = client.getTripsFlight(body).block(Duration.ofSeconds(5));

    postProcess(jMeterVariables, response);

    return response;
  }

  private void postProcess(JMeterVariables variables, GetTripFlightsResponse tripFlights) {
    FlightsContext flightsContext = (FlightsContext) variables.getObject(FLIGHT_CONTEXT_VAR);

    variables.putObject(FLIGHT_HAS_VAR, false);

    if (flightsContext == null) {
      flightsContext = FlightsContext.builder().withFlightAvailable(false).build();
    }

    if (log.isDebugEnabled()) {
      log.debug("Post process flight query result [{}].", tripFlights);
      log.debug("Current Flight Context is [{}].", flightsContext);
    }

    FlightsContext.Builder builder = flightsContext.mutate();
    int toFlightCount = tripFlights.getToFlightCount();
    int returnFlightCount = tripFlights.getReturnFlightCount();

    if (toFlightCount == 0 && returnFlightCount == 0) {
      variables.putObject(FLIGHT_CONTEXT_VAR, builder.withFlightAvailable(false).build());
    } else {
      boolean hasToFlights = toFlightCount > 0;
      boolean hasReturnFlights = hasToFlights && returnFlightCount > 0;

      builder.withOneWay(hasToFlights && !hasReturnFlights);
      variables.putObject(ONE_WAY, hasToFlights && !hasReturnFlights);

      if (hasToFlights) {
        Flight flight = tripFlights.getToFlight(toFlightCount - 1);

        builder
            .withNumberOfToFlights(toFlightCount)
            .withFlightAvailable(true)
            .withToFlightId(flight.getId())
            .withToFlightSegmentId(flight.getSegment().getFlightSegmentId());

        variables.putObject(FLIGHT_HAS_VAR, true);
        variables.putObject(FLIGHT_TO_COUNT, toFlightCount);
      }

      if (hasReturnFlights) {
        Flight flight = tripFlights.getToFlight(returnFlightCount - 1);

        builder
            .withNumberOfReturnFlights(returnFlightCount)
            .withReturnFlightId(flight.getId())
            .withReturnFlightSegmentId(flight.getSegment().getFlightSegmentId());

        variables.putObject(FLIGHT_RET_COUNT, returnFlightCount);
      }

      variables.putObject(FLIGHT_CONTEXT_VAR, builder.build());
    }
  }
}
