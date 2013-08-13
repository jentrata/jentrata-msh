/**
 * 
 */
package org.jentrata.spa.jms;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.Properties;

import junit.framework.Assert;

import hk.hku.cecid.piazza.commons.message.Message;
import hk.hku.cecid.piazza.commons.message.MessageHandler;
import hk.hku.cecid.piazza.commons.module.Module;
import hk.hku.cecid.piazza.commons.test.PluginTest;

import org.apache.camel.Exchange;
import org.jentrata.spa.jms.module.JMSComponent;
import org.junit.Test;


/**
 * @author aaronwalker
 *
 */
public class JMSComponentTest extends PluginTest {

    @Override
    protected void configure() {
        System.setProperty("jentrata.activemq.broker.uri","broker:(vm://localhost?broker.persistent=false&useJmx=false)?persistent=false&useJmx=false");
    }

    @Test
    public void testJMSComponentMessageHandler() throws Exception {
        Module m = new Module("modules/jms-test-module.xml");
        assertThat(m.getComponent("jmsTest"), is(instanceOf(JMSComponent.class)));
        JMSComponent jms = (JMSComponent) m.getComponent("jmsTest");
        
        MessageHandler handler = mock(MessageHandler.class);
        
        Properties props = new Properties();
        props.setProperty("errorUri", "jmsTest:queue:testDLQ");
        
        jms.registerHandler("jmsTest:queue:testQueue", handler,props);
        jms.getProducer().sendBody("jmsTest:queue:testQueue", "test".getBytes());
        
        Thread.sleep(100);
        
        verify(handler).onMessage((Message) any());

    }
    
    @Test
    public void testJMSComponentMessageHandlerError() throws Exception {
        Module m = new Module("modules/jms-test-module.xml");
        assertThat(m.getComponent("jmsTest"), is(instanceOf(JMSComponent.class)));
        JMSComponent jms = (JMSComponent) m.getComponent("jmsTest");
        
        MessageHandler handler = mock(MessageHandler.class);
        doThrow(new RuntimeException("bad message")).when(handler).onMessage((Message) any());
        
        Properties props = new Properties();
        props.setProperty("errorUri", "jmsTest:queue:testDLQ");
        
        jms.registerHandler("jmsTest:queue:testQueue2", handler,props);
        jms.getProducer().sendBody("jmsTest:queue:testQueue2", "test".getBytes());
        
        Thread.sleep(100);
        
        verify(handler).onMessage((Message) any());
        
        Exchange ex = jms.getConsumer().receive("jmsTest:queue:testDLQ",1000);
        assertThat(ex,notNullValue());
    }
    
    @Test(expected=Exception.class)
    public void testJMSComponentMessageHandlerRegisterFailure() throws Exception {
        Module m = new Module("modules/jms-test-module.xml");
        assertThat(m.getComponent("jmsTest"), is(instanceOf(JMSComponent.class)));
        JMSComponent jms = (JMSComponent) m.getComponent("jmsTest");
        
        MessageHandler handler = mock(MessageHandler.class);
        
        jms.registerHandler("blash:queue:testQueue", handler,new Properties());

        //Shouldn't get here
        Assert.fail();
    }
    
    @Test
    public void testJMSComponentWith2MessageHandlers() throws Exception {
        Module m = new Module("modules/jms-test-module.xml");
        assertThat(m.getComponent("jmsTest"), is(instanceOf(JMSComponent.class)));
        JMSComponent jms = (JMSComponent) m.getComponent("jmsTest");
        
        MessageHandler handler1 = mock(MessageHandler.class);
        MessageHandler handler2 = mock(MessageHandler.class);
        
        jms.registerHandler("jmsTest:queue:testQueue3", handler1,new Properties());
        jms.registerHandler("jmsTest:queue:testQueue4", handler2,new Properties());
        jms.getProducer().sendBody("jmsTest:queue:testQueue3", "test".getBytes());
        jms.getProducer().sendBody("jmsTest:queue:testQueue4", "test2".getBytes());
        
        Thread.sleep(100);
        
        verify(handler1).onMessage((Message) any());
        verify(handler2).onMessage((Message) any());
    }
}
