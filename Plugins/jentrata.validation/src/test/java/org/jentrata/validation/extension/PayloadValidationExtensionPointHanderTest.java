package org.jentrata.validation.extension;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import hk.hku.cecid.piazza.commons.spa.Extension;
import hk.hku.cecid.piazza.commons.spa.Plugin;
import hk.hku.cecid.piazza.commons.spa.PluginException;

import java.util.Properties;

import javax.xml.soap.AttachmentPart;

import org.jentrata.validation.ValidationException;
import org.jentrata.validation.Validator;
import org.jentrata.validation.ValidatorComponent;
import org.junit.Before;
import org.junit.Test;

public class PayloadValidationExtensionPointHanderTest {
    
    protected PayloadValidationExtensionPointHander ep;
    protected ValidatorComponent mockValidatorComponent;
    
    @Before
    public void setup() {
        ep = spy(new PayloadValidationExtensionPointHander());
        mockValidatorComponent = mock(ValidatorComponent.class);
        doReturn(mockValidatorComponent).when(ep).getComponent();
    }
    
    @Test
    public void testRegisterExtensionPointDefault() throws Exception {
        
        Properties config = new Properties();
        config.setProperty("class", "org.jentrata.validation.extension.PayloadValidationExtensionPointHanderTest.MockValidator");
        config.setProperty("cpaId", "test");
        
        assertRegisterExtensionPoint(config);
        
        verify(mockValidatorComponent).registerDefault(eq("test"), eq(MockValidator.class),isA(Properties.class));
    }
    
    @Test
    public void testRegisterExtensionPointWithContentType() throws Exception {
        
        Properties config = new Properties();
        config.setProperty("class", "org.jentrata.validation.extension.PayloadValidationExtensionPointHanderTest.MockValidator");
        config.setProperty("cpaId", "test");
        config.setProperty("contentType", "text/plain");
        
        assertRegisterExtensionPoint(config);
        
        verify(mockValidatorComponent).register(eq("test"), eq("text/plain"), eq(MockValidator.class),isA(Properties.class));
    }
    
    @Test(expected=PluginException.class)
    public void testRegisterExtensionPointMissingCpaId() throws Exception {
        
        Properties config = new Properties();
        config.setProperty("class", "org.jentrata.validation.extension.PayloadValidationExtensionPointHanderTest.MockValidator");
        config.setProperty("contentType", "text/plain");
        assertRegisterExtensionPoint(config);
    }

    @Test(expected=PluginException.class)
    public void testRegisterExtensionPointMissingClass() throws Exception {
        
        Properties config = new Properties();
        config.setProperty("cpaId", "test");
        config.setProperty("contentType", "text/plain");
        assertRegisterExtensionPoint(config);
    }

    protected void assertRegisterExtensionPoint(Properties config) throws Exception {
        Extension ext = spy(new Extension(null, "org.jentrata.validator", "test", config));
        doReturn(mockPlugin()).when(ext).getPlugin();
        ep.processExtension(ext);        
    }
    
    protected Plugin mockPlugin() throws Exception {
        Plugin mockPlugin = mock(Plugin.class);
        doReturn(MockValidator.class).when(mockPlugin).loadClass("org.jentrata.validation.extension.PayloadValidationExtensionPointHanderTest.MockValidator");
        return mockPlugin;
    }

    public static class MockValidator implements Validator {

        public void init(Properties config) {
            
        }

        public void validate(AttachmentPart payload) throws ValidationException {
        }
        
    }
}
