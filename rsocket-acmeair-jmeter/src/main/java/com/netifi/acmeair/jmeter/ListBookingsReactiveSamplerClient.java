package com.netifi.acmeair.jmeter;

import java.util.List;
import java.util.Map;

import com.netifi.broker.BrokerClient;
import io.rsocket.RSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netifi.acmeair.Booking;
import com.netifi.acmeair.BookingServiceClient;
import com.netifi.acmeair.GetBookingsByUserRequest;
import reactor.core.publisher.Mono;

public class ListBookingsReactiveSamplerClient extends AbstractReactiveJavaSamplerClient<Booking> {

    private static final Logger log = LoggerFactory.getLogger(
            ListBookingsReactiveSamplerClient.class);

    private static final String USERNAME = "username";
    private static final String BOOKING_CONTEXT_VAR = "booking.context";

    private RSocket              rSocket;
    private BookingServiceClient client;

    @Override
    protected void setupTestClient(BrokerClient brokerClient) {
        rSocket = brokerClient.group("netifi.acmeair.booking");
        client = new BookingServiceClient(rSocket);
    }

    @Override
    protected Mono<Void> prepareTestRun(Map<String, Object> parameters, PublisherInstrumentation<Booking> instrumentation) {
        return ReactiveVariableHolder
            .variables()
            .flatMap(variables -> {
                if (log.isDebugEnabled()) {
                    log.debug("Retrieved Reactor Context Variables [{}]", variables);
                }

                String username = (String) variables.get(USERNAME);

                GetBookingsByUserRequest body = GetBookingsByUserRequest.newBuilder()
                                                                        .setUsername(username)
                                                                        .build();

                if (log.isDebugEnabled()) {
                    log.debug("Sending Body [{}]", body.toString());
                }

                return client.getBookingsByUser(body)
                             .collectList()
                             .doOnNext(bookings -> {})
                             .flatMapIterable(l -> l)
                             .as(instrumentation);
            });
    }

    private void postProcess(Map<String, Object> variables, List<Booking> bookedFlightsList) {
        BookingContext bookingContext =
                (BookingContext) variables.getOrDefault(BOOKING_CONTEXT_VAR, BookingContext.builder()
                                                                                           .build());

        if (log.isDebugEnabled()) {
            log.debug("Post process booking result [{}].", bookedFlightsList);
            log.debug("Current Booking Context is [{}].", bookingContext);
        }

        int bookingNum = bookedFlightsList.size();
        String[] bookingIds = bookedFlightsList.stream()
                                               .map(Booking::getBookingId)
                                               .toArray(String[]::new);

        bookingContext = bookingContext
            .mutate()
            .withNumberOfBookings(bookingNum)
            .withNumberToCancel(bookingNum > 2 ? bookingNum - 2 : 0)
            .withBookingIds(bookingIds)
            .build();

        variables.put(
            BOOKING_CONTEXT_VAR,
            bookingContext
        );
    }
}
