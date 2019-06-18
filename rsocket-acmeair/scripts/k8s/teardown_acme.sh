#!/usr/bin/env bash

kubectl delete -f acme/booking.yaml
kubectl delete -f acme/customer.yaml
kubectl delete -f acme/login.yaml
kubectl delete -f acme/flight.yaml
