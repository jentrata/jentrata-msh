/**
 * 
 */
package org.jentrata.spa.jms;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Properties;

import junit.framework.Assert;

import hk.hku.cecid.piazza.commons.message.Message;
import hk.hku.cecid.piazza.commons.message.MessageHandler;
import hk.hku.cecid.piazza.commons.module.Module;
import hk.hku.cecid.piazza.commons.test.PluginTest;

import org.jentrata.spa.jms.module.JMSComponent;
import org.junit.Ignore;
import org.junit.Test;


/**
 * @author aaronwalker
 *
 */
public class JMSComponentTest extends PluginTest {
    
    @Test
    public void testJMSComponentMessageHandler() throws Exception {
        Module m = new Module("modules/jms-test-module.xml");
        assertThat(m.getComponent("jmsTest"), is(instanceOf(JMSComponent.class)));
        JMSComponent jms = (JMSComponent) m.getComponent("jmsTest");
        
        MessageHandler handler = mock(MessageHandler.class);
        
        jms.registerHandler("jmsTest:queue:testQueue", handler,new Properties());
        jms.getProducer().sendBody("jmsTest:queue:testQueue", "test".getBytes());
        
        Thread.sleep(100);
        
        verify(handler).onMessage((Message) any());

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
    
    @Test @Ignore
    public void testJMSComponentWith2MessageHandlers() throws Exception {
        Module m = new Module("modules/jms-test-module.xml");
        assertThat(m.getComponent("jmsTest"), is(instanceOf(JMSComponent.class)));
        JMSComponent jms = (JMSComponent) m.getComponent("jmsTest2");
        
        MessageHandler handler1 = mock(MessageHandler.class);
        MessageHandler handler2 = mock(MessageHandler.class);
        
        jms.registerHandler("jmsTest:queue:testQueue", handler1,new Properties());
        jms.registerHandler("jmsTest:queue:testQueue2", handler2,new Properties());
        jms.getProducer().sendBody("jmsTest:queue:testQueue", "test".getBytes());
        jms.getProducer().sendBody("jmsTest:queue:testQueue2", "test2".getBytes());
        
        Thread.sleep(100);
        
        verify(handler1).onMessage((Message) any());
        verify(handler2).onMessage((Message) any());
    }
}
