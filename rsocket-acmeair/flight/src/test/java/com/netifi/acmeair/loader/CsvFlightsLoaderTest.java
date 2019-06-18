package com.netifi.acmeair.loader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import com.netifi.acmeair.loader.FlightsData.Airport;
import com.netifi.acmeair.loader.FlightsData.Flight;
import com.netifi.acmeair.loader.FlightsData.FlightSegment;

public class CsvFlightsLoaderTest {

  private CsvFlightsLoader flightsLoader;

  @Before
  public void setUp() {
    flightsLoader = new CsvFlightsLoader();
  }

  @Test
  public void sampleDataSmokeTest() {
    FlightsData flightsData = flightsLoader.load("mileage.csv");
    List<Airport> airports = flightsData.airports().collectList().block();
    List<Flight> flights = flightsData.flights().collectList().block();
    List<FlightSegment> flightSegments = flightsData.flightSegments().collectList().block();

    assertCollection(CsvFlightsLoaderTest::notEmpty, airports, flights, flightSegments);
  }

  private static boolean notEmpty(Collection<?> l) {
    return l != null && l.size() > 0;
  }

  private void assertCollection(Predicate<Collection<?>> p, Collection<?>... colls) {
    for (Collection<?> c : colls) {
      Assert.assertTrue(p.test(c));
    }
  }
}
