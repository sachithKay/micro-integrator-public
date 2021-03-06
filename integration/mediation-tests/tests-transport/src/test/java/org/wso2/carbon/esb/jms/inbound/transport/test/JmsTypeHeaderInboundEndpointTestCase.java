/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.carbon.esb.jms.inbound.transport.test;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;
import org.wso2.esb.integration.common.utils.clients.jmsclient.JmsClientHelper;

import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

/**
 * This testcase tests whether JMSType header is ignored when transport.jms.ContentTypeProperty"
 * or "transport.jms.ContentType" is specified.
 */
public class JmsTypeHeaderInboundEndpointTestCase extends ESBIntegrationTest {

    private static final String QUEUE_NAME = "jmsTypeHeaderInboundEndpointTestCase";

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {

        init();
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" },
            description = "Test JMSType header with inbound endpoint")
    public void testJmsTypeHeaderWithInboundEndpoint() throws Exception {

        CarbonLogReader logViewerClient = new CarbonLogReader();
        logViewerClient.start();
        sendMessage();
        boolean assertValue = logViewerClient.checkForLog("** jmsTypeHeaderInboundEPSendInSequence was called **", 60);
        Assert.assertTrue(assertValue, "Message was not received to the inbound EP when JMSType was set.");
        Assert.assertTrue(Utils.isQueueEmpty(QUEUE_NAME), "Queue should be empty if message was properly received");
        logViewerClient.stop();
    }

    private void sendMessage() throws Exception {

        InitialContext initialContext = JmsClientHelper.getActiveMqInitialContext();
        QueueConnectionFactory connectionFactory = (QueueConnectionFactory) initialContext.lookup(
                JmsClientHelper.QUEUE_CONNECTION_FACTORY);
        QueueConnection queueConnection = connectionFactory.createQueueConnection();
        QueueSession queueSession = queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
        QueueSender sender = queueSession.createSender(queueSession.createQueue(QUEUE_NAME));

        String message = "<?xml version='1.0' encoding='UTF-8'?>"
                + "    <ser:getQuote xmlns:ser=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\"> "
                + "      <ser:request>" + "        <xsd:symbol>IBM</xsd:symbol>" + "      </ser:request>"
                + "    </ser:getQuote>";
        try {
            TextMessage jmsMessage = queueSession.createTextMessage(message);
            jmsMessage.setJMSType("incorrecttype");
            sender.send(jmsMessage);
        } finally {
            queueConnection.close();
        }
    }
}
