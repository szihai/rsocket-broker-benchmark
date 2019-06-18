package com.netifi.acmeair.jmeter.blocking;

import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import com.netifi.broker.BrokerClient;
import io.rsocket.RSocket;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netifi.acmeair.BookingServiceClient;
import com.netifi.acmeair.CancelBookingRequest;
import com.netifi.acmeair.jmeter.BookingContext;

import java.time.Duration;

public class BlockingCancelBookingSamplerClient extends AbstractBlockingJavaSamplerClient {

  private static final Logger log =
      LoggerFactory.getLogger(BlockingCancelBookingSamplerClient.class);

  private static final String USERNAME = "username";
  private static final String BOOKING_ID_VAR = "booking.loop.id";
  private static final String BOOKING_CONTEXT_VAR = "booking.context";

  private RSocket rSocket;
  private BookingServiceClient client;

  @Override
  protected void setupTestClient(BrokerClient brokerClient) {
    rSocket = brokerClient.group("netifi.acmeair.booking");
    client = new BookingServiceClient(rSocket);
  }

  protected GeneratedMessageV3 doCall(JavaSamplerContext context, Runnable beforeCall) {
    JMeterVariables jMeterVariables = context.getJMeterVariables();
    BookingContext bookingContext = (BookingContext) jMeterVariables.getObject(BOOKING_CONTEXT_VAR);
    int bookingLoopId = Integer.valueOf(jMeterVariables.get(BOOKING_ID_VAR));

    if (bookingContext.getBookingIds() != null
        && bookingLoopId < (bookingContext.getBookingIds().length)
        && bookingContext.getBookingIds().length > 1) {
      String username = jMeterVariables.get(USERNAME);

      CancelBookingRequest body =
          CancelBookingRequest.newBuilder()
              .setBookingId(bookingContext.getBookingIds()[bookingLoopId])
              .setUsername(username)
              .build();

      return client.cancelBooking(body).block(Duration.ofSeconds(5));
    } else {
      return Empty.getDefaultInstance();
    }
  }
}
