package org.jentrata.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Properties;

import javax.xml.soap.AttachmentPart;

import hk.hku.cecid.piazza.commons.module.Module;
import hk.hku.cecid.piazza.commons.test.PluginTest;

import org.jentrata.validation.Validator;
import org.jentrata.validation.ValidatorComponent;
import org.junit.Test;

public class ValidatorComponentTest extends PluginTest {

    @Test
    public void testValidatorComponentConfig() {
        Module m = new Module("modules/validator-test-module.xml");
        assertThat(m.getComponent(ValidatorComponent.COMP_ID), is(instanceOf(ValidatorComponent.class)));
        ValidatorComponent vc = (ValidatorComponent) m.getComponent(ValidatorComponent.COMP_ID);
        vc.registerDefault("test", MockValidator.class, buildDefaultConfig());
        Validator v = vc.getDefaultValidator("test");
        assertThat(v,is(instanceOf(MockValidator.class)));
    }
    
    @Test
    public void testValidatorComponentWithContentType() {
        Module m = new Module("modules/validator-test-module.xml");
        assertThat(m.getComponent(ValidatorComponent.COMP_ID), is(instanceOf(ValidatorComponent.class)));
        ValidatorComponent vc = (ValidatorComponent) m.getComponent(ValidatorComponent.COMP_ID);
        Properties props = buildDefaultConfig();
        vc.register("test","text/plain", MockValidator.class, props);
        Validator v = vc.getValidator("test","text/plain");
        assertThat(v,is(instanceOf(MockValidator.class)));
    }
    
    @Test
    public void testValidatorComponentDefault() {
        Module m = new Module("modules/validator-test-module.xml");
        assertThat(m.getComponent(ValidatorComponent.COMP_ID), is(instanceOf(ValidatorComponent.class)));
        ValidatorComponent vc = (ValidatorComponent) m.getComponent(ValidatorComponent.COMP_ID);
        Properties props = buildDefaultConfig();
        vc.registerDefault("test", MockValidator.class, props);
        Validator v = vc.getValidator("test","text/plain");
        assertThat(v,is(instanceOf(MockValidator.class)));
        v = vc.getValidator("test", "text/xml");
        assertThat(v,is(instanceOf(MockValidator.class)));
    }
    
    private Properties buildDefaultConfig() {
        Properties config = new Properties();
        config.put("type", "mock");
        return config;
    }

    public static class MockValidator implements Validator {

        public void init(Properties config) {
            assertThat(config.get("type").toString(),is(equalTo("mock")));
        }

        public void validate(AttachmentPart payload) throws ValidationException {
        }
    }
}
