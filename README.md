# RSocket broker benchmark
## Purpose
The purpose of the demo is to benchmark the throughput of RSocket broker. The product we're testing against is from Netifi. There are other products in the area. But so far Netifi's broker is the most feature rich and ready.

## RSocket broker
RSocket broker simplifies the process of building cloud-native applications and microservices. Your applications and services can communicate with each other as peers, without any of them having to keep track of where their peers are to be found, while the broker takes care of networking concerns like routing, session resumption, application flow control, and predictive load balancing. The broker accepts connections from your applications and manages communication between them seamlessly, using zero-copy transmission to form virtual peer-to-peer connections.

## RSocket broker service mesh?
Service mesh is a concept. It's not equal to sidecar. It's not equal to Istio. There are different implementations of the concept. And by using a RSocket, micro services can focus on the logic while the broker takes care of connecting, securing, controlling, and oberserving the services.

[RSocket](http://rsocket.io) provides unique features to simplify these tasks. The broker would be able to handle service registration, service discovery, health checking, traffic shaping, etc, without the control plane. On top of that, RScoket itself is a better protocol than HTTP/2 for micro services communications. 

## Setup
For the benchmark, you'll need:   
* A K8s Cluster, 3 nodes minimum
* Postgres DB server(s), 16 cores minimum
* A Jmeter server, 16 cores minimum

Make sure all the nodes and server are in the same data center so the results is not affected by Internet speed.

## Steps

### Provision the DB
* Login to your DB server. Modify the postgresql.conf (if possible) to extend the `max_connections` to 1000 and `shared_buffers` to a quarter of your memory.
* Start a postgres terminal, copy & paste the content under rsocket-acmeair/sql/schema.sql and run it. This will create the schema of the application database.

### Install the application and broker
* Go to rsocket-acmeair/scripts/k8s/acme, 
* Modify the `-Dnetifi.acmeair.postgres.host`, `-Dnetifi.acmeair.postgres.username` and `Dnetifi.acmeair.postgres.password` fields in booking.yaml, customer.yaml, login.yaml, flight.yaml with the internal DB server IP, user name and password.
* Modify the `WEBSOCKET_BROKER_PUBLIC_ADDRESS` in broker.yaml with one of the K8s node public IP.
* Go to rsocket-acmeair/scripts/
* run `setup_broker.sh`
* run `setup_acme.sh`
And wait for a while until all the pods are up and running.

### Pump the data
* Modify rsocket-acmeair/client/src/main/resources/application-prod.properties with the broker LB IP address,
* Locally, build the rsocket-acmeair project with `./gradlew clean build`.
* Go to rsocket-acmeair/client/
* Run the `ClientMain` class in an IDE or terminal. This will pump the data to DB.

### Run Jmeter
* Go to rsocket-acmeair-jmeter
* build the project with `./gradlew clean build`. This will generate a Jar file.
* Login to the Jmeter server, download Jmeter.
* Copy the Jar file to Jmeter server, under`<Apache-JMeter-Home>/lib/ext`.
* Copy the scipt file under rsocket-acmeair-jmeter/scripts/ to Jmeter server jmeter/bin directory.
* Run Jmeter with command `JVM_ARGS="-Duser.timezone=America/Los_Angeles -Xmx8g -Xms8g -XX:+AlwaysPreTouch -XX:+UseStringDeduplication -XX:+UseG1GC" ./jmeter -n -t test_1000.jmx`
Now we can see the results.

## Summary
You can tear down the benchmark by running `rsocket-acmeair/scripts/k8s/teardown_broker.sh` and `teardown_acme.sh`. If you are interested, you can try the original [acmeair](https://github.com/blueperf/acmeair-mainservice-java) with Istio and compare the results.





