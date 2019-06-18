package com.netifi.acmeair.jmeter.blocking;

import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import com.netifi.broker.BrokerClient;
import io.rsocket.RSocket;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netifi.acmeair.Booking;
import com.netifi.acmeair.BookingServiceClient;
import com.netifi.acmeair.GetBookingsByUserRequest;
import com.netifi.acmeair.jmeter.BookingContext;

import java.time.Duration;
import java.util.List;

public class BlockingListBookingsSamplerClient extends AbstractBlockingJavaSamplerClient {

  private static final Logger log =
      LoggerFactory.getLogger(BlockingListBookingsSamplerClient.class);

  private static final String USERNAME = "username";
  private static final String BOOKING_CONTEXT_VAR = "booking.context";

  private RSocket rSocket;
  private BookingServiceClient client;

  @Override
  protected void setupTestClient(BrokerClient brokerClient) {
    rSocket = brokerClient.group("netifi.acmeair.booking");
    client = new BookingServiceClient(rSocket);
  }

  @Override
  protected GeneratedMessageV3 doCall(JavaSamplerContext context, Runnable beforeCall) {
    JMeterVariables variables = context.getJMeterVariables();
    String username = variables.get(USERNAME);

    if (username == null) {
      return Empty.getDefaultInstance();
    }

    GetBookingsByUserRequest body =
        GetBookingsByUserRequest.newBuilder().setUsername(username).build();

    if (log.isDebugEnabled()) {
      log.debug("Sending Body [{}]", body.toString());
    }

    beforeCall.run();

    List<Booking> list = client.getBookingsByUser(body).collectList().block(Duration.ofSeconds(5));

    postProcess(variables, list);

    return list.stream()
        .findAny()
        .map(b -> (GeneratedMessageV3) b)
        .orElse(Empty.getDefaultInstance());
  }

  private void postProcess(JMeterVariables variables, List<Booking> bookedFlightsList) {
    BookingContext bookingContext = (BookingContext) variables.getObject(BOOKING_CONTEXT_VAR);

    if (bookingContext == null) {
      bookingContext = BookingContext.builder().build();
    }

    if (log.isDebugEnabled()) {
      log.debug("Post process booking result [{}].", bookedFlightsList);
      log.debug("Current Booking Context is [{}].", bookingContext);
    }

    int bookingNum = bookedFlightsList.size();
    String[] bookingIds =
        bookedFlightsList.stream().map(Booking::getBookingId).toArray(String[]::new);

    bookingContext =
        bookingContext
            .mutate()
            .withNumberOfBookings(bookingNum)
            .withNumberToCancel(bookingNum > 2 ? bookingNum - 2 : 0)
            .withBookingIds(bookingIds)
            .build();

    variables.putObject(BOOKING_CONTEXT_VAR, bookingContext);
  }
}
