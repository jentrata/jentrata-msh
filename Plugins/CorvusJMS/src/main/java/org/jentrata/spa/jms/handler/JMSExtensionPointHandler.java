/**
 * 
 */
package org.jentrata.spa.jms.handler;

import hk.hku.cecid.piazza.commons.message.MessageHandler;
import hk.hku.cecid.piazza.commons.spa.Extension;
import hk.hku.cecid.piazza.commons.spa.ExtensionPointIteratedHandler;
import hk.hku.cecid.piazza.commons.spa.PluginException;
import hk.hku.cecid.piazza.commons.util.Instance;

import java.util.Properties;

import org.jentrata.spa.jms.JMSProcessor;
import org.jentrata.spa.jms.module.JMSComponent;

/**
 * @author aaronwalker
 *
 */
public class JMSExtensionPointHandler extends ExtensionPointIteratedHandler {

    @Override
    public void processExtension(Extension extension) throws PluginException {
        
        String queueName = extension.getParameter("queue");
        String className  = extension.getParameter("class");

        try {
            Properties config = new Properties();
            config.putAll(extension.getParameters());

            if (className != null) { 
                Class<?> handlerClass = extension.getPlugin().loadClass(className);
                register(queueName, handlerClass);
            } else {
                throw new PluginException("Unable to register handler: No handler class defined");                
            }
        } catch (Exception e) {
            throw new PluginException("Unable to register the JMS client: "+ queueName, e);
        }        
    }
    
    public void register(String queue, Class<?> handlerClass) throws Exception {
        JMSComponent jms = getJMSComponent(queue);
        
        Instance i = new Instance(handlerClass);
        jms.registerHandler(queue, (MessageHandler) i.getObject());
    }
    
    public JMSComponent getJMSComponent(String queue) {
        return JMSProcessor.getInstance().getJmsComponent(queue);
    }

}
