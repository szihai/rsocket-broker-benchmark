FROM adoptopenjdk/openjdk11:x86_64-ubuntu-jdk-11.0.2.9-slim
MAINTAINER Netifi Team
ARG APP_VERSION
ENV APP_VERSION ${APP_VERSION}
VOLUME /tmp
ADD build/distributions/customer-boot-${APP_VERSION}.tar /opt/
ENTRYPOINT ["sh","-c","/opt/customer-boot-${APP_VERSION}/bin/customer", "-fg"]

