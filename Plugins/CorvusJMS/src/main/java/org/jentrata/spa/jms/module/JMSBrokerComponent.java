package org.jentrata.spa.jms.module;

import hk.hku.cecid.piazza.commons.Sys;
import hk.hku.cecid.piazza.commons.module.Component;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.jentrata.spa.jms.JMSProcessor;

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
        brokerService.setUseShutdownHook(false);
        Sys.main.log.info("Started JMS Broker");
    }

    public BrokerService getBrokerService() {
        return brokerService;
    }

    public String getBrokerUri() {
        return getParameters().getProperty("brokerUri");
    }

    public void shutdownBroker() {
        if(brokerService != null && brokerService.isStarted()) {
            try {
                Sys.main.log.info("Stopping JMS Broker:" + getBrokerUri());
                brokerService.stop();
            } catch (Exception e) {
                Sys.main.log.warn("unable to stop JMS Broker:" + e.getMessage());
                Sys.main.log.debug("",e);
            }
        } else {
            Sys.main.log.warn("JMS Broker already stopped");
        }
    }
}
