package com.netifi.acmeair.loader;

import com.netifi.acmeair.FlightServiceDelegate;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.UnicastProcessor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.*;

public class CsvFlightsLoader implements FlightsLoader {

  private static final int MAX_FLIGHTS_PER_SEGMENT = 15;

  private static void complete(Subscriber<?>... subs) {
    for (Subscriber<?> sub : subs) {
      sub.onComplete();
    }
  }

  private static void error(Throwable err, Subscriber<?>... subs) {
    for (Subscriber<?> sub : subs) {
      sub.onError(err);
    }
  }

  private static Date departTime(int daysFromNow) {
    Calendar c = Calendar.getInstance();
    c.setTime(new Date());
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    c.add(Calendar.DATE, daysFromNow);
    return c.getTime();
  }

  static InputStream resourceStream(String resource) {
    return FlightServiceDelegate.class.getResourceAsStream("/" + resource);
  }

  static FlightsData.Airport newAirport(String airportCode, String airportName) {
    return new FlightsData.Airport(airportCode, airportName);
  }

  static FlightsData.Airport newAirport(String airportName) {
    return newAirport(null, airportName);
  }

  private static Date calculateArrivalTime(Date departureTime, int mileage) {
    double averageSpeed = 600.0; // 600 miles/hours
    double hours = (double) mileage / averageSpeed; // miles / miles/hour = hours
    double partsOfHour = hours % 1.0;
    int minutes = (int) (60.0 * partsOfHour);
    //Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    Calendar c = Calendar.getInstance();
    c.setTime(departureTime);
    c.add(Calendar.HOUR, (int) hours);
    c.add(Calendar.MINUTE, minutes);
    return c.getTime();
  }

  private static boolean contains(List<FlightsData.Airport> airports, String code) {
    return airports.stream().anyMatch(a -> code.equals(a.getCode()));
  }

  @Override
  public FlightsData load(String name) {

    Objects.requireNonNull(name, "Flights source name must be non null");

    final UnicastProcessor<FlightsData.Flight> flightsFlux = UnicastProcessor.create();
    final UnicastProcessor<FlightsData.FlightSegment> flightSegmentsFlux = UnicastProcessor.create();
    final UnicastProcessor<FlightsData.Airport> airportsFlux = UnicastProcessor.create();

    try (LineNumberReader lnr =
        new LineNumberReader(new BufferedReader(new InputStreamReader(resourceStream(name))))) {

      // read the first line which are airport names
      String airportNames = lnr.readLine();
      // read the second line which contains matching airport codes for the first line
      String airportCodes = lnr.readLine();

      List<FlightsData.Airport> airports = readAirports(airportNames, airportCodes);

      StringTokenizer st;
      // read the other lines which are of format:
      // airport name, aiport code, distance from this airport to whatever airport is in the column
      // from lines one and two
      int flightNumber = 0;
      String line = lnr.readLine();

      while (line != null && !line.trim().isEmpty()) {
        st = new StringTokenizer(line, ",");
        /*next line*/
        line = lnr.readLine();

        String airportName = st.nextToken();
        String airportCode = st.nextToken();
        if (!contains(airports, airportCode)) {
          FlightsData.Airport acm = newAirport(airportCode, airportName);
          airports.add(acm);
        }
        int indexIntoTopLine = 0;
        while (st.hasMoreTokens()) {
          String milesString = st.nextToken();
          if (milesString.equals("NA")) {
            indexIntoTopLine++;
            continue;
          }
          int miles = Integer.parseInt(milesString);
          String toAirport = airports.get(indexIntoTopLine).getCode();
          String flightId = "AA" + flightNumber;
          flightSegmentsFlux.onNext(new FlightsData.FlightSegment(flightId, airportCode, toAirport, miles));
          for (int daysFromNow = -1; daysFromNow < MAX_FLIGHTS_PER_SEGMENT; daysFromNow++) {
            Date departureTime = departTime(daysFromNow);
            Date arrivalTime = calculateArrivalTime(departureTime, miles);
            flightsFlux.onNext(
                new FlightsData.Flight(flightId, departureTime, arrivalTime, 500, 200, 10, 200, "B747"));
          }
          flightNumber++;
          indexIntoTopLine++;
        }
      }
      airports.forEach(airportsFlux::onNext);

      complete(airportsFlux, flightSegmentsFlux, flightsFlux);
    } catch (Exception e) {
      error(e, airportsFlux, flightSegmentsFlux, flightsFlux);
    }
    return new FlightsData(flightSegmentsFlux, flightsFlux, airportsFlux);
  }

  private List<FlightsData.Airport> readAirports(String airportNames, String airportCodes) {
    StringTokenizer st = new StringTokenizer(airportNames, ",");
    List<FlightsData.Airport> airports = new ArrayList<>();
    while (st.hasMoreTokens()) {
      airports.add(newAirport(st.nextToken()));
    }

    st = new StringTokenizer(airportCodes, ",");
    int i = 0;
    while (st.hasMoreTokens()) {
      String airportCode = st.nextToken();
      airports.get(i).setCode(airportCode);
      i++;
    }
    return airports;
  }
}
