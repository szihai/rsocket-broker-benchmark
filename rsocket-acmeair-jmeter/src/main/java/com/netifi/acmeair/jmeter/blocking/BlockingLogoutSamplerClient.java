package com.netifi.acmeair.jmeter.blocking;

import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import com.netifi.broker.BrokerClient;
import io.rsocket.RSocket;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netifi.acmeair.LoginServiceClient;
import com.netifi.acmeair.LogoutRequest;
import reactor.core.Exceptions;

import java.time.Duration;

public class BlockingLogoutSamplerClient extends AbstractBlockingJavaSamplerClient {

  private static final Logger log = LoggerFactory.getLogger(BlockingLogoutSamplerClient.class);

  private static final String USERNAME = "username";
  private static final String SESSION_ID = "sessionId";

  private RSocket rSocket;
  private LoginServiceClient client;

  @Override
  public Arguments getDefaultParameters() {
    Arguments defaultParameters = new Arguments();
    return defaultParameters;
  }

  @Override
  protected void setupTestClient(BrokerClient brokerClient) {
    rSocket = brokerClient.group("netifi.acmeair.login");
    client = new LoginServiceClient(rSocket);
  }

  @Override
  protected GeneratedMessageV3 doCall(JavaSamplerContext context, Runnable beforeCall) {
    JMeterVariables jMeterVariables = context.getJMeterVariables();
    String sessionId = jMeterVariables.get(SESSION_ID);
    String username = jMeterVariables.get(USERNAME);

    try {
      if (sessionId != null) {
        if (log.isDebugEnabled()) {
          log.debug("Retrieved variables : [sessionId={}]]", sessionId);
        }

        LogoutRequest body =
            LogoutRequest.newBuilder().setUsername(username).setSessionId(sessionId).build();

        if (log.isDebugEnabled()) {
          log.debug("Sending Body [{}]", body.toString());
        }

        beforeCall.run();

        client.logout(body).block(Duration.ofSeconds(5));
      } else {
        log.info("SESSION IS NULL!!!!!!!");
      }

      return Empty.getDefaultInstance();
    } catch (Throwable t) {
      log.error("error", t);
      throw Exceptions.propagate(t);
    } finally {
      jMeterVariables.remove(USERNAME);
      jMeterVariables.remove(SESSION_ID);
    }
  }
}
