package org.jentrata.validation.xml;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.soap.AttachmentPart;

import org.jentrata.validation.ValidationException;
import org.jentrata.validation.Validator;
import org.junit.Before;
import org.junit.Test;

public class SchemaValidatorTest {
    
    
    protected Validator v;
    
    @Before
    public void setup() {
        v = spy(new SchemaValidator());
        Properties config = new Properties();
        config.put("schemaLocation", "src/test/resources/schema/shiporder.xsd");
        v.init(config);
    }
    
    @Test
    @SuppressWarnings("restriction")
    public void testSchemaValidatorValidPayload() throws Exception {
        try {
            assertValidation("src/test/resources/schema/valid.xml");
        } catch (ValidationException e) {
            fail(e.getMessage());
        }
    }
    
    @Test(expected=ValidationException.class)
    @SuppressWarnings("restriction")
    public void testSchemaValidatorInvalidPayload() throws Exception {
        assertValidation("src/test/resources/schema/invalid.xml");
        fail("validation should have failed");
    }
    
    @Test(expected=ValidationException.class)
    @SuppressWarnings("restriction")
    public void testSchemaValidatorNotWellFormedPayload() throws Exception {
        assertValidation("src/test/resources/schema/notwellformed.xml");
        fail("validation should have failed");
    }
    
    @SuppressWarnings("restriction")
    protected void assertValidation(String filename) throws Exception {
        assertThat(v,notNullValue());
        AttachmentPart payload = mockAttachment(filename);
        v.validate(payload);
    }
    
    @SuppressWarnings("restriction")
    protected AttachmentPart mockAttachment(String filename) throws Exception {
        
        AttachmentPart payload = mock(AttachmentPart.class);
        
        InputStream in = new FileInputStream(new File(filename));
        doReturn(in).when(payload).getRawContent();     
        
        return payload;
    }

}
