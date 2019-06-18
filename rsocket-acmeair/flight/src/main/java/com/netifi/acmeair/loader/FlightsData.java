package com.netifi.acmeair.loader;

import reactor.core.publisher.Flux;

import java.util.Date;
import java.util.Objects;

public class FlightsData {
  private final Flux<FlightSegment> flightSegments;
  private final Flux<Flight> flights;
  private final Flux<Airport> airports;

  public FlightsData(Flux<FlightSegment> flightSegments,
                     Flux<Flight> flights,
                     Flux<Airport> airports) {
    this.flightSegments = flightSegments;
    this.flights = flights;
    this.airports = airports;
  }

  public Flux<FlightSegment> flightSegments() {
    return flightSegments;
  }

  public Flux<Flight> flights() {
    return flights;
  }

  public Flux<Airport> airports() {
    return airports;
  }

  public static class FlightSegment {
    private final String flightId;
    private final String srcAirportCode;
    private final String destAirportCode;
    private final int miles;

    public FlightSegment(String flightId,
                         String srcAirportCode,
                         String destAirportCode,
                         int miles) {
      this.flightId = flightId;
      this.srcAirportCode = srcAirportCode;
      this.destAirportCode = destAirportCode;
      this.miles = miles;
    }

    public String getFlightId() {
      return flightId;
    }

    public String getSrcAirportCode() {
      return srcAirportCode;
    }

    public String getDestAirportCode() {
      return destAirportCode;
    }

    public int getMiles() {
      return miles;
    }

    @Override
    public String toString() {
      return "FlightSegment{" +
          "flightSegmentId='" + flightId + '\'' +
          ", srcAirportCode='" + srcAirportCode + '\'' +
          ", destAirportCode='" + destAirportCode + '\'' +
          ", miles=" + miles +
          '}';
    }
  }

  public static class Flight {
    private final String flightId;
    private final Date departureTime;
    private final Date arrivalTime;
    private final int firstClassBaseCost;
    private final int economyClassBaseCost;
    private final int firstClassSeatsNum;
    private final int economyClassSeatsNum;
    private final String airplaneTypeId;

    public Flight(String flightId,
                  Date departureTime,
                  Date arrivalTime,
                  int firstClassBaseCost,
                  int economyClassBaseCost,
                  int firstClassSeatsNum,
                  int economyClassSeatsNum,
                  String airplaneTypeId) {
      this.flightId = flightId;
      this.departureTime = departureTime;
      this.arrivalTime = arrivalTime;
      this.firstClassBaseCost = firstClassBaseCost;
      this.economyClassBaseCost = economyClassBaseCost;
      this.firstClassSeatsNum = firstClassSeatsNum;
      this.economyClassSeatsNum = economyClassSeatsNum;
      this.airplaneTypeId = airplaneTypeId;
    }

    public String getFlightId() {
      return flightId;
    }

    public Date getDepartureTime() {
      return departureTime;
    }

    public Date getArrivalTime() {
      return arrivalTime;
    }

    public int getFirstClassBaseCost() {
      return firstClassBaseCost;
    }

    public int getEconomyClassBaseCost() {
      return economyClassBaseCost;
    }

    public int getFirstClassSeatsNum() {
      return firstClassSeatsNum;
    }

    public int getEconomyClassSeatsNum() {
      return economyClassSeatsNum;
    }

    public String getAirplaneTypeId() {
      return airplaneTypeId;
    }
  
    @Override
    public String toString() {
      return "Flight{" +
                 "flightId='" + flightId + '\'' +
                 ", departureTime=" + departureTime +
                 ", arrivalTime=" + arrivalTime +
                 ", firstClassBaseCost=" + firstClassBaseCost +
                 ", economyClassBaseCost=" + economyClassBaseCost +
                 ", firstClassSeatsNum=" + firstClassSeatsNum +
                 ", economyClassSeatsNum=" + economyClassSeatsNum +
                 ", airplaneTypeId='" + airplaneTypeId + '\'' +
                 '}';
    }
  }

  public static class Airport {
    private String _id;
    private String airportName;

    public Airport(String airportCode, String airportName) {
      this._id = airportCode;
      this.airportName = airportName;
    }

    public String getCode() {
      return _id;
    }

    public void setCode(String code) {
      this._id = code;
    }

    public String getName() {
      return airportName;
    }

    public void setName(String airportName) {
      this.airportName = airportName;
    }
  
    @Override
    public String toString() {
      return "Airport{" +
                 "_id='" + _id + '\'' +
                 ", airportName='" + airportName + '\'' +
                 '}';
    }
  }
}

