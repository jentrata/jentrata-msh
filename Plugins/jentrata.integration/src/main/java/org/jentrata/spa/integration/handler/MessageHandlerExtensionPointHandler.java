package org.jentrata.spa.integration.handler;

import hk.hku.cecid.piazza.commons.Sys;
import hk.hku.cecid.piazza.commons.message.MessageHandler;
import hk.hku.cecid.piazza.commons.spa.Extension;
import hk.hku.cecid.piazza.commons.spa.ExtensionPointIteratedHandler;
import hk.hku.cecid.piazza.commons.spa.PluginException;
import hk.hku.cecid.piazza.commons.util.Instance;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.jentrata.spa.integration.IntegrationPluguin;
import org.jentrata.spa.integration.message.CamelMessage;

import java.util.Properties;

/**
 * Created by aaronwalker on 11/07/2016.
 */
public class MessageHandlerExtensionPointHandler extends ExtensionPointIteratedHandler {

    @Override
    public void processExtension(Extension extension) throws PluginException {
        String id = extension.getParameter("id");
        String className  = extension.getParameter("class");

        try {
            Properties config = new Properties();
            config.putAll(extension.getParameters());

            if (className != null) {
                Class<?> handlerClass = extension.getPlugin().loadClass(className);
                Instance i = new Instance(handlerClass);
                MessageHandler handler = (MessageHandler)i.getObject();
                registerHandler(id, handler,config);
            } else {
                throw new PluginException("Unable to deployCamelContext handler: No handler class defined");
            }
        } catch (Exception e) {
            throw new PluginException("Unable to deployCamelContext the camel context extension: "+ id, e);
        }
    }

    public void registerHandler(String id, MessageHandler handler, Properties config) throws Exception {
        Sys.main.log.debug("Adding message handler:" + id);
        IntegrationPluguin.getInstance().getSpringBootstrapComponent().registerBean(id,new MessageHandlerProcessor(handler,config));
        Sys.main.log.info("Added message handler:" + id);
    }

    private static class MessageHandlerProcessor implements Processor {
        private MessageHandler messageHandler;
        private Properties config;

        MessageHandlerProcessor(MessageHandler messageHandler, Properties config) {
            this.messageHandler = messageHandler;
            this.config = config;
        }

        @Override
        public void process(Exchange exchange) throws Exception {
            messageHandler.onMessage(new CamelMessage(exchange.getIn(),config));
        }
    }
}