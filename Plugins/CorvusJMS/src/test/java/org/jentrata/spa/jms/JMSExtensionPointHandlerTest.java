/**
 * 
 */
package org.jentrata.spa.jms;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;
import hk.hku.cecid.piazza.commons.message.Message;
import hk.hku.cecid.piazza.commons.message.MessageHandler;
import hk.hku.cecid.piazza.commons.spa.Extension;
import hk.hku.cecid.piazza.commons.spa.Plugin;
import hk.hku.cecid.piazza.commons.spa.PluginException;

import java.util.Properties;

import org.jentrata.spa.jms.handler.JMSExtensionPointHandler;
import org.jentrata.spa.jms.module.JMSComponent;
import org.junit.Before;
import org.junit.Test;

/**
 * @author aaronwalker
 *
 */
public class JMSExtensionPointHandlerTest {

    protected JMSExtensionPointHandler ep;
    protected JMSComponent mockJMS;
    
    @Before
    public void setup() {
        ep = spy(new JMSExtensionPointHandler());
        mockJMS = mock(JMSComponent.class);
        doReturn(mockJMS).when(ep).getJMSComponent("jms:queue:testQueue");
    }
    
    @Test
    public void testRegisterExtensionPoint() throws Exception {
        
        Properties config = new Properties();
        config.setProperty("class", "org.jentrata.spa.jms.JMSExtensionPointHandlerTest.MockMsgHandler");
        config.setProperty("queue", "jms:queue:testQueue");
        
        assertRegisterExtensionPoint(config);
        
        verify(mockJMS).registerHandler(eq("jms:queue:testQueue"), isA(MockMsgHandler.class),isA(Properties.class));
    }
    
    @Test(expected=PluginException.class)
    public void testRegisterWithMissingClass() throws Exception {
        Properties config = new Properties();
        config.setProperty("queue", "jms:queue:testQueue");       
        assertRegisterExtensionPoint(config);
    }
    
    @Test(expected=PluginException.class)
    public void testRegisterWithFailure() throws Exception {
        Properties config = new Properties();
        config.setProperty("queue", "jms:queue:testQueue");
        
        doThrow(new Exception()).when(mockJMS).registerHandler(anyString(),(MessageHandler) anyObject(),isA(Properties.class));
        
        assertRegisterExtensionPoint(config);
    }
    
    protected void assertRegisterExtensionPoint(Properties config) throws Exception {

        Extension ext = spy(new Extension(null, "org.jentrata.jms.sender", "test", config));
        doReturn(mockPlugin()).when(ext).getPlugin();
        
        ep.processExtension(ext);

    }
    
    protected Plugin mockPlugin() throws Exception {
        Plugin mockPlugin = mock(Plugin.class);
        doReturn(MockMsgHandler.class).when(mockPlugin).loadClass("org.jentrata.spa.jms.JMSExtensionPointHandlerTest.MockMsgHandler");
        return mockPlugin;
    }
    
    public static class MockMsgHandler implements MessageHandler {

        public void onMessage(Message message) {
            
        }
        
    }
}
