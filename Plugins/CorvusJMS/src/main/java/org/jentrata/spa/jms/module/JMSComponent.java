/**
 * 
 */
package org.jentrata.spa.jms.module;

import java.util.Properties;

import hk.hku.cecid.piazza.commons.message.MessageHandler;
import hk.hku.cecid.piazza.commons.module.Component;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.jentrata.spa.jms.message.CamelMessage;

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
    }
    
    public void registerHandler(final String queue, MessageHandler handler, Properties config) throws Exception {
        
        String errorUri = config.getProperty("errorUri");
        if(errorUri == null) {
            errorUri = getErrorUri();
        }
        RouteBuilder builder = new MessageHandlerRouteBuilder(queue,errorUri,handler,config);
        camel.addRoutes(builder);
    }
    
    public ProducerTemplate getProducer() {
        return camel.createProducerTemplate();
    }
    
    public ConsumerTemplate getConsumer() {
        return camel.createConsumerTemplate();
    }
    
    protected ConnectionFactory buildConnectionFactory() {
        String connectionFactoryUrl = getConnectionFactoryUrl();
        ConnectionFactory cf = new ActiveMQConnectionFactory(connectionFactoryUrl);
        return cf;
    }
    
    protected String getConnectionFactoryUrl() {
        return getParameters().getProperty("connectionFactoryUrl");
    }
    
    protected String getErrorUri() {
        return getParameters().getProperty("errorUri",getId() + ":queue:DLQ");
    }
    
    private static class MessageHandlerRouteBuilder extends RouteBuilder {
        
        private String queueUri;
        private String errorUri;
        private MessageHandler handler;
        private Properties config;

        public MessageHandlerRouteBuilder(String queueUri, String errorUri, MessageHandler handler, Properties config) {
            this.queueUri = queueUri;
            this.handler = handler;
            this.config = config;
            this.errorUri = errorUri;
        }
        
        public void configure() {
            from(queueUri)
            .doTry()
                .process(new Processor() {
                     public void process(Exchange exchange) throws Exception {
                         try {
                             handler.onMessage(new CamelMessage(exchange.getIn(),config));
                         } catch(Exception ex) {
                             exchange.getIn().setHeader("dlqReason", ex.getMessage());
                             throw ex;
                         }
                    }
                })
            .doCatch(RuntimeException.class)
                .to(errorUri)
            .end();
        }
    }
}
