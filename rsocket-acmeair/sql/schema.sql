
DROP INDEX  IF EXISTS username1_idx;
DROP INDEX  IF EXISTS username2_idx;
DROP INDEX  IF EXISTS origin_dest_idx;
DROP INDEX  IF EXISTS flight_segment_id_idx;

DROP TABLE BOOKING;
DROP TABLE CUSTOMER;
DROP TABLE ADDRESS;
DROP TABLE AIRPORT;
DROP TABLE FLIGHT;
DROP TABLE FLIGHT_SEGMENT;
DROP TABLE FLIGHT_TO_FLIGHT_SEGMENT;
DROP TABLE LOGIN;

CREATE TABLE BOOKING(
   bookingId TEXT PRIMARY KEY NOT NULL,
   username TEXT NOT NULL,
   flightId  TEXT  NOT NULL,
   dateOfBooking TIMESTAMP NOT NULL
);

CREATE INDEX username1_idx ON BOOKING (username);

CREATE TABLE CUSTOMER (
    username TEXT PRIMARY KEY NOT NULL,
    password TEXT NOT NULL,
    status TEXT NOT NULL,
    totalMiles INTEGER NOT NULL,
    mileYtd INTEGER NOT NULL,
    phoneNumber TEXT NOT NULL,
    phoneNumberType TEXT NOT NULL
);

CREATE TABLE ADDRESS (
    addressId TEXT PRIMARY KEY NOT NULL,
    username TEXT NOT NULL,
    streetAddress1 TEXT NOT NULL,
    streetAddress2 TEXT NULL,
    city TEXT NOT NULL,
    stateProvince TEXT NOT NULL,
    country TEXT NOT NULL,
    postCode TEXT  NOT NULL
);

CREATE INDEX username2_idx ON ADDRESS (username);

CREATE TABLE AIRPORT (
    airportCode TEXT NOT NULL,
    airportName TEXT NOT NULL
);

CREATE TABLE FLIGHT (
    flightId TEXT NOT NULL,
    firstClassBaseCost INTEGER NOT NULL,
    numFirstClassSeats INTEGER NOT NULL,
    economyClassBaseCost INTEGER NOT NULL,
    numEconomyClassSeats INTEGER NOT NULL,
    airplaneTypeId TEXT NOT NULL,
    scheduledDepartureTime TIMESTAMP NOT NULL,
    scheduledArrivalTime TIMESTAMP NOT NULL,
    PRIMARY KEY(flightId, scheduledDepartureTime)
);

CREATE TABLE FLIGHT_TO_FLIGHT_SEGMENT (
    flightId TEXT NOT NULL,
    flightSegmentId TEXT NOT NULL,
    PRIMARY KEY(flightId, flightSegmentId)
);

CREATE INDEX flight_segment_id_idx ON FLIGHT_TO_FLIGHT_SEGMENT (flightSegmentId);


CREATE TABLE FLIGHT_SEGMENT (
   flightSegmentId TEXT PRIMARY KEY NOT NULL,
   originPort TEXT NOT NULL,
   destPort TEXT NOT NULL,
   miles INTEGER NOT NULL
);

CREATE INDEX origin_dest_idx ON FLIGHT_SEGMENT (originPort, destPort);

ALTER TABLE FLIGHT_SEGMENT CLUSTER ON origin_dest_idx;

CREATE TABLE LOGIN (
    sessionId TEXT PRIMARY KEY NOT NULL,
    userId TEXT NOT NULL,
    lastAccessedTime TIMESTAMP NOT NULL,
    timeoutTime TIMESTAMP NOT NULL,
    logoutTime TIMESTAMP
);

