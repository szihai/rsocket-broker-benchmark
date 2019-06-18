package com.netifi.acmeair.jmeter;

import java.io.Serializable;
import java.util.UUID;

import com.netifi.broker.BrokerClient;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

public class NetifiSetup extends AbstractTestElement
        implements TestStateListener, LoopIterationListener, NoThreadClone, Serializable {

    private static final long serialVersionUID = 1L;

    private static final String GROUP = "NetifiSetup.group"; // $NON-NLS-1$

    private static final String DESTINATION = "NetifiSetup.destination"; // $NON-NLS-1$

    private static final String ACCESS_KEY = "NetifiSetup.access_key"; // $NON-NLS-1$

    private static final String ACCESS_TOKEN = "NetifiSetup.access_token"; // $NON-NLS-1$

    private static final String HOST = "NetifiSetup.host"; // $NON-NLS-1$

    private static final String PORT = "NetifiSetup.port"; // $NON-NLS-1$

    private static final String KEEPALIVE = "NetifiSetup.keepalive"; // $NON-NLS-1$

    private static final String SSL_DISABLED = "NetifiSetup.ssl.disabled"; // $NON-NLS-1$

    public static final String VAR_CLIENT = "NetifiSetup.client"; // $NON-NLS-1$

    private transient BrokerClient brokerClient;

    @Override
    public void testStarted() {
        testStarted("local");
    }

    @Override
    public void testStarted(String host) {
        System.out.println("Started");
        brokerClient = BrokerClient.builder()
                         .group(getGroup())
                         .destination(getDestination() + "_" + UUID.randomUUID().toString())
                         .accessKey(getAccessKey())
                         .accessToken(getAccessToken())
                         .host(getHost())
                         .port(getPort())
                         .sslDisabled(isSSLDisabled())
                         .keepalive(isKeepalive())
                         .build();
    }

    @Override
    public void testEnded() {
        testEnded("local");
    }

    @Override
    public void testEnded(String host) {
        brokerClient.dispose();
    }


    @Override
    public void iterationStart(LoopIterationEvent iterEvent) {
        JMeterVariables variables = JMeterContextService.getContext().getVariables();

        variables.putObject(VAR_CLIENT, brokerClient);
    }



    public String getGroup() {
        return getPropertyAsString(GROUP);
    }

    public void setGroup(String group) {
        setProperty(GROUP, group);
    }

    public String getDestination() {
        return getPropertyAsString(DESTINATION);
    }

    public void setDestination(String destination) {
        setProperty(DESTINATION, destination);
    }

    public void setAccessKey(long accessKey) {
        setProperty(ACCESS_KEY, accessKey);
    }

    public long getAccessKey() {
        return getPropertyAsLong(ACCESS_KEY);
    }

    public void setAccessToken(String accessToken) {
        setProperty(ACCESS_TOKEN, accessToken);
    }

    public String getAccessToken() {
        return getPropertyAsString(ACCESS_TOKEN);
    }

    public void setHost(String host) {
        setProperty(HOST, host);
    }

    public String getHost() {
        return getPropertyAsString(HOST);
    }

    public void setPort(int port) {
        setProperty(PORT, port);
    }

    public int getPort() {
        return getPropertyAsInt(PORT);
    }

    public void setKeepalive(boolean isKeepalive) {
        setProperty(KEEPALIVE, isKeepalive);
    }

    public boolean isKeepalive() {
        return getPropertyAsBoolean(KEEPALIVE);
    }

    public void setSLLDisabled(boolean isSSLDisabled) {
        setProperty(SSL_DISABLED, isSSLDisabled);
    }

    public boolean isSSLDisabled() {
        return getPropertyAsBoolean(SSL_DISABLED);
    }
}
