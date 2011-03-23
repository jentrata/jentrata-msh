/**
 * 
 */
package org.jentrata.spa.jms.module;

import hk.hku.cecid.piazza.commons.module.Component;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.jentrata.spa.jms.JMSProcessor;
import org.jentrata.spa.jms.handler.MessageHandler;

/**
 * @author aaronwalker
 *
 */
public class JMSComponent extends Component {

    private ConnectionFactory connectionFactory;
    private CamelContext camel;

    @Override
    protected void init() throws Exception {
        super.init();
        connectionFactory = buildConnectionFactory();
        camel = new DefaultCamelContext();
        camel.addComponent(getId(), JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
        camel.start();
        JMSProcessor.core.log.debug("Camel Context Configured");
    }
    
    public void registerHandler(final String queue, MessageHandler handler) throws Exception {
        RouteBuilder builder = new MessageHandlerRouteBuilder(queue,handler);
        camel.addRoutes(builder);
    }
    
    public ProducerTemplate getProducer() {
        return camel.createProducerTemplate();
    }
    
    protected ConnectionFactory buildConnectionFactory() {
        ConnectionFactory cf = new ActiveMQConnectionFactory(getConnectionFactoryUrl());
        return cf;
    }
    
    protected String getConnectionFactoryUrl() {
        return getParameters().getProperty("connectionFactoryUrl");
    }
    
    private static class MessageHandlerRouteBuilder extends RouteBuilder {
        
        private String queueUri;
        private MessageHandler handler;

        public MessageHandlerRouteBuilder(String queueUri, MessageHandler handler) {
            this.queueUri = queueUri;
            this.handler = handler;
        }
        
        public void configure() {
            from(queueUri)
                .process(new Processor() {
                 public void process(Exchange exchange) throws Exception {
                    handler.onMessage(exchange.getIn());
                }
            });
        }
    }
}
