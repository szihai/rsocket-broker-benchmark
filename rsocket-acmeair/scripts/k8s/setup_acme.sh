#!/bin/bash
kubectl create -f acme/booking.yaml
kubectl create -f acme/customer.yaml
kubectl create -f acme/flight.yaml
kubectl create -f acme/login.yaml
