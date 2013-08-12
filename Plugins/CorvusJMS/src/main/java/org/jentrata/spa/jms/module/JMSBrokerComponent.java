package org.jentrata.spa.jms.module;

import hk.hku.cecid.piazza.commons.Sys;
import hk.hku.cecid.piazza.commons.module.Component;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

/**
 * Creates an ActiveMQ Message Broker
 */
public class JMSBrokerComponent extends Component {

    private BrokerService brokerService;

    @Override
    protected void init() throws Exception {
        super.init();
        Sys.main.log.debug("Starting JMS Broker:" + getBrokerUri());
        brokerService = BrokerFactory.createBroker(getBrokerUri(),true);
        Sys.main.log.info("Started JMS Broker");
    }

    public BrokerService getBrokerService() {
        return brokerService;
    }

    public String getBrokerUri() {
        return getParameters().getProperty("brokerUri");
    }
}
