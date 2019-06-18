/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.netifi.acmeair.jmeter;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.jmeter.processor.gui.AbstractPreProcessorGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * The GUI for SampleTimeout.
 */
public class NetifiSetupGui extends AbstractPreProcessorGui {

    private static final long serialVersionUID = 240L;

    /**
     * The default value for the timeout.
     */
    private static final String DEFAULT_GROUP       = "jmeter.clients";
    private static final String DEFAULT_DESTINATION = "jmeter.client";
    private static final String DEFAULT_HOST        = "localhost";
    private static final String DEFAULT_PORT        = "8001";

    private JTextField groupField;
    private JTextField destinationField;
    private JTextField accessKeyField;
    private JTextField accessTokenField;
    private JTextField hostField;
    private JTextField portField;
    private JCheckBox  keepaliveBox;
    private JCheckBox  sslDisableBox;

    /**
     * No-arg constructor.
     */
    public NetifiSetupGui() {
        init();
    }

    /**
     * Handle an error.
     *
     * @param e the Exception that was thrown.
     * @param thrower the JComponent that threw the Exception.
     */
    public static void error(Exception e, JComponent thrower) {
        JOptionPane.showMessageDialog(thrower, e, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public String getStaticLabel() {
        return "Netifi Setup";
    }

    @Override
    public String getLabelResource() {
        return "sample_timeout_title"; // $NON-NLS-1$
    }

    /**
     * Create the test element underlying this GUI component.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        NetifiSetup netifiSetup = new NetifiSetup();
        modifyTestElement(netifiSetup);
        return netifiSetup;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement el) {
        super.configureTestElement(el);

        NetifiSetup netifiSetup = (NetifiSetup) el;

        netifiSetup.setGroup(groupField.getText());
        netifiSetup.setDestination(destinationField.getText());
        if (NumberUtils.isParsable(accessKeyField.getText())) {
            netifiSetup.setAccessKey(Long.parseLong(accessKeyField.getText()));
        }
        netifiSetup.setAccessToken(accessTokenField.getText());
        netifiSetup.setHost(hostField.getText());
        if (NumberUtils.isParsable(portField.getText())) {
            netifiSetup.setPort(Integer.parseInt(portField.getText()));
        }
        netifiSetup.setKeepalive(keepaliveBox.isSelected());
        netifiSetup.setSLLDisabled(sslDisableBox.isSelected());
    }

    /**
     * Configure this GUI component from the underlying TestElement.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#configure(TestElement)
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);

        NetifiSetup netifiSetup = (NetifiSetup) el;

        groupField.setText(netifiSetup.getGroup());
        destinationField.setText(netifiSetup.getDestination());
        accessKeyField.setText(String.valueOf(netifiSetup.getAccessKey()));
        accessTokenField.setText(netifiSetup.getAccessToken());
        hostField.setText(netifiSetup.getHost());
        portField.setText(String.valueOf(netifiSetup.getPort()));
        keepaliveBox.setSelected(netifiSetup.isKeepalive());
        sslDisableBox.setSelected(netifiSetup.isSSLDisabled());
    }

    /**
     * Initialize this component.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));

        setBorder(makeBorder());
        add(makeTitlePanel());

        Box groupPanel = Box.createHorizontalBox();
        JLabel groupLabel = new JLabel("Netifi Client Group");//$NON-NLS-1$
        groupPanel.add(groupLabel);
        groupField = new JTextField(6);
        groupField.setText(DEFAULT_GROUP);
        groupPanel.add(groupField);

        Box destinationPanel = Box.createHorizontalBox();
        JLabel destinationLabel = new JLabel("Netifi Client Destination");//$NON-NLS-1$
        destinationPanel.add(destinationLabel);
        destinationField = new JTextField(6);
        destinationField.setText(DEFAULT_DESTINATION);
        destinationPanel.add(destinationField);

        Box accessKeyPanel = Box.createHorizontalBox();
        JLabel accessKeyLabel = new JLabel("Netifi Access Key");//$NON-NLS-1$
        accessKeyPanel.add(accessKeyLabel);
        accessKeyField = new JTextField(6);
        accessKeyPanel.add(accessKeyField);

        Box accessTokenPanel = Box.createHorizontalBox();
        JLabel accessTokenLabel = new JLabel("Netifi Access Token");//$NON-NLS-1$
        accessTokenPanel.add(accessTokenLabel);
        accessTokenField = new JTextField(6);
        accessTokenPanel.add(accessTokenField);

        Box hostAndPortPanel = Box.createHorizontalBox();
        JLabel hostLabel = new JLabel("Netifi Broker Host");//$NON-NLS-1$
        hostAndPortPanel.add(hostLabel);
        hostField = new JTextField(6);
        hostField.setText(DEFAULT_HOST);
        hostAndPortPanel.add(hostField);
        JLabel portLabel = new JLabel("Port");//$NON-NLS-1$
        hostAndPortPanel.add(portLabel);
        portField = new JTextField(6);
        portField.setText(DEFAULT_PORT);
        hostAndPortPanel.add(portField);

        Box keepalivePanel = Box.createHorizontalBox();
        JLabel keepaliveLabel = new JLabel("Keep Alive Connection");//$NON-NLS-1$
        keepalivePanel.add(keepaliveLabel);
        keepaliveBox = new JCheckBox();
        keepalivePanel.add(keepaliveBox);

        Box sslDisabledPanel = Box.createHorizontalBox();
        JLabel sslDisabledLabel = new JLabel("Disable SSL");//$NON-NLS-1$
        sslDisabledPanel.add(sslDisabledLabel);
        sslDisableBox = new JCheckBox();
        sslDisabledPanel.add(sslDisableBox);

        add(groupPanel);
        add(destinationPanel);
        add(accessKeyPanel);
        add(accessTokenPanel);
        add(hostAndPortPanel);
        add(keepalivePanel);
        add(sslDisabledPanel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearGui() {
        groupField.setText(DEFAULT_GROUP);
        destinationField.setText(DEFAULT_DESTINATION);
        accessKeyField.setText("");
        accessTokenField.setText("");
        hostField.setText(DEFAULT_HOST);
        portField.setText(DEFAULT_PORT);
        keepaliveBox.setSelected(false);
        keepaliveBox.setSelected(false);

        super.clearGui();
    }
}
