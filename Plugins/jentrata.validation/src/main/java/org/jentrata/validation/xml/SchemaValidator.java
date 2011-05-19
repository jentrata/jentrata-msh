/**
 * 
 */
package org.jentrata.validation.xml;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.jentrata.validation.ValidationException;
import org.jentrata.validation.ValidationProcessor;
import org.xml.sax.SAXException;

/**
 * @author aaronwalker
 *
 */
public class SchemaValidator implements org.jentrata.validation.Validator {

    private static final String SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";

    private SchemaFactory factory;
    private File schemaFile;
    private Schema xsdScheme;
    private Properties config;
    
    /* (non-Javadoc)
     * @see org.jentrata.validation.Validator#init(java.util.Properties)
     */
    public void init(Properties config) {
        this.config = config;
        factory = SchemaFactory.newInstance(SCHEMA_LANGUAGE);
        schemaFile = new File(getSchemaLocation());
        try {
            xsdScheme = factory.newSchema(schemaFile);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.jentrata.validation.Validator#validate(javax.xml.soap.AttachmentPart, java.lang.String)
     */
    public void validate(AttachmentPart payload) throws ValidationException {

        Validator validator = xsdScheme.newValidator();
        Source source;
        try {
            source = new StreamSource(payload.getRawContent());
            validator.validate(source);
        } catch (SOAPException e) {
            ValidationProcessor.core.log.error("unable to schema validate payload " + e);
            ValidationProcessor.core.log.debug("",e);
        } catch (SAXException e) {
            throw new ValidationException(e.getMessage(),e);
        } catch (IOException e) {
            ValidationProcessor.core.log.error("unable to schema validate payload " + e);
            ValidationProcessor.core.log.debug("",e);
        }
    }

    protected String getSchemaLocation() {
        return (String)config.get("schemaLocation");
    }
}
