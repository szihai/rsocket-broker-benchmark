package com.netifi.acmeair.jmeter;

public class FlightsContext {

    private final int     numberOfToFlights;
    private final int     numberOfReturnFlights;
    private final String  toFlightId;
    private final String  toFlightSegmentId;
    private final String  returnFlightId;
    private final String  returnFlightSegmentId;
    private final boolean oneWay;
    private final boolean flightAvailable;

    public FlightsContext(
        int numberOfToFlights,
        int numberOfReturnFlights,
        String toFlightId,
        String toFlightSegmentId,
        String returnFlightId,
        String returnFlightSegmentId,
        boolean oneWay,
        boolean flightAvailable
    ) {
        this.numberOfToFlights = numberOfToFlights;
        this.numberOfReturnFlights = numberOfReturnFlights;
        this.toFlightId = toFlightId;
        this.toFlightSegmentId = toFlightSegmentId;
        this.returnFlightId = returnFlightId;
        this.returnFlightSegmentId = returnFlightSegmentId;
        this.oneWay = oneWay;
        this.flightAvailable = flightAvailable;
    }

    public int getNumberOfToFlights() {
        return numberOfToFlights;
    }

    public int getNumberOfReturnFlights() {
        return numberOfReturnFlights;
    }

    public String getToFlightId() {
        return toFlightId;
    }

    public String getToFlightSegmentId() {
        return toFlightSegmentId;
    }

    public String getReturnFlightId() {
        return returnFlightId;
    }

    public String getReturnFlightSegmentId() {
        return returnFlightSegmentId;
    }

    public boolean isOneWay() {
        return oneWay;
    }

    public boolean isFlightAvailable() {
        return flightAvailable;
    }

    @Override
    public String toString() {
        return "FlightsContext{" + "numberOfToFlights=" + numberOfToFlights + ", numberOfReturnFlights=" + numberOfReturnFlights + ", toFlightId='" + toFlightId + '\'' + ", toFlightSegmentId='" + toFlightSegmentId + '\'' + ", returnFlightId='" + returnFlightId + '\'' + ", returnFlightSegmentId='" + returnFlightSegmentId + '\'' + ", oneWay=" + oneWay + ", flightAvailable=" + flightAvailable + '}';
    }

    public FlightsContext.Builder mutate() {
        return new Builder(this);
    }

    public static FlightsContext.Builder builder() {
        return new Builder();
    }


    public static final class Builder {

        private int     numberOfToFlights;
        private int     numberOfReturnFlights;
        private String  toFlightId;
        private String  toFlightSegmentId;
        private String  returnFlightId;
        private String  returnFlightSegmentId;
        private boolean oneWay;
        private boolean flightAvailable;

        private Builder() {
        }

        private Builder(FlightsContext parent) {
            numberOfToFlights = parent.numberOfToFlights;
            numberOfReturnFlights = parent.numberOfReturnFlights;
            toFlightId = parent.toFlightId;
            toFlightSegmentId = parent.toFlightSegmentId;
            returnFlightId = parent.returnFlightId;
            returnFlightSegmentId = parent.returnFlightSegmentId;
            oneWay = parent.oneWay;
            flightAvailable = parent.flightAvailable;
        }

        public FlightsContext build() {
            FlightsContext flightsContext = new FlightsContext(
                numberOfToFlights,
                numberOfReturnFlights,
                toFlightId,
                toFlightSegmentId,
                returnFlightId,
                returnFlightSegmentId,
                oneWay,
                flightAvailable
            );

            return flightsContext;
        }

        public Builder withFlightAvailable(boolean flightAvailable) {
            this.flightAvailable = flightAvailable;
            return this;
        }

        public Builder withNumberOfToFlights(int numberOfToFlights) {
            this.numberOfToFlights = numberOfToFlights;
            return this;
        }

        public Builder withNumberOfReturnFlights(int numberOfReturnFlights) {
            this.numberOfReturnFlights = numberOfReturnFlights;
            return this;
        }

        public Builder withOneWay(boolean oneWay) {
            this.oneWay = oneWay;
            return this;
        }

        public Builder withReturnFlightId(String returnFlightId) {
            this.returnFlightId = returnFlightId;
            return this;
        }

        public Builder withReturnFlightSegmentId(String returnFlightSegmentId) {
            this.returnFlightSegmentId = returnFlightSegmentId;
            return this;
        }

        public Builder withToFlightSegmentId(String toFlightSegmentId) {
            this.toFlightSegmentId = toFlightSegmentId;
            return this;
        }

        public Builder withToFlightId(String toFlightId) {
            this.toFlightId = toFlightId;
            return this;
        }
    }
}
