package com.netifi.acmeair.jmeter;

import java.util.Arrays;

public class BookingContext {

    private final String[] bookingIds;
    private final int      counter;
    private final int      numberOfBookings;
    private final int      numberToCancel;

    public BookingContext(
            String[] bookingIds,
            int counter,
            int numberOfBookings,
            int numberToCancel) {
        this.bookingIds = bookingIds;
        this.counter = counter;
        this.numberOfBookings = numberOfBookings;
        this.numberToCancel = numberToCancel;
    }

    public String[] getBookingIds() {
        return bookingIds;
    }

    public int getCounter() {
        return counter;
    }

    public int getNumberOfBookings() {
        return numberOfBookings;
    }

    public int getNumberToCancel() {
        return numberToCancel;
    }

    @Override
    public String toString() {
        return "BookingContext{" + "bookingIds=" + Arrays.toString(bookingIds) + ", counter=" + counter + ", numberOfBookings='" + numberOfBookings + '\'' + ", numberToCancel='" + numberToCancel + '\'' + '}';
    }

    public Builder mutate() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String[] bookingIds;
        private int      counter = - 1;
        private int   numberOfBookings;
        private int   numberToCancel;

        private Builder() {
        }

        private Builder(BookingContext bookingContext) {
            this.bookingIds = bookingContext.bookingIds;
            this.counter = bookingContext.counter;
            this.numberOfBookings = bookingContext.numberOfBookings;
            this.numberToCancel = bookingContext.numberToCancel;
        }

        public BookingContext build() {
            return new BookingContext(bookingIds, counter, numberOfBookings, numberToCancel);
        }

        public Builder withBookingIds(String[] bookingIds) {
            this.bookingIds = bookingIds;
            return this;
        }

        public Builder withCounter(int counter) {
            this.counter = counter;
            return this;
        }

        public Builder withNumberOfBookings(int numberOfBookings) {
            this.numberOfBookings = numberOfBookings;
            return this;
        }

        public Builder withNumberToCancel(int numberToCancel) {
            this.numberToCancel = numberToCancel;
            return this;
        }
    }
}
