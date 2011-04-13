/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hk.hku.cecid.ebms.spa.handler;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.soap.AttachmentPart;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author mhanda
 */
public class ValidationComponentTest {

	ValidationComponent instance;
	
	AttachmentPart attachment;
	
	public ValidationComponentTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() {
		instance = spy(new ValidationComponent());
		doReturn("src/test/resources/hk.hku.cecid.ebms.handler.ValidationComponentTest/Schema/shiporder.xsd").when(
				instance).getSchemaLocation();
		attachment = mock(AttachmentPart.class);
		
	}

	@After
	public void tearDown() {
	}

	/**
	 * Test of validate method, of class ValidationComponent.
	 */
	@Test(expected = InvalidAttachmentException.class)
	public void testValidateWithInvalidPayload() throws Exception {
		InputStream in = new FileInputStream(
				new File(
						"src/test/resources/hk.hku.cecid.ebms.handler.ValidationComponentTest/invalid.xml"));
		doReturn(in).when(attachment).getRawContent();

		instance.init();
		instance.validate(attachment);
		in.close();
	}

	@Test
	public void testValidateWithValidPayload() throws Exception {
		InputStream in = new FileInputStream(
				new File(
						"src/test/resources/hk.hku.cecid.ebms.handler.ValidationComponentTest/valid.xml"));
		doReturn(in).when(attachment).getRawContent();

		instance.init();
		try {
			instance.validate(attachment);
		} catch (InvalidAttachmentException e) {
			fail("Schema validation not working for valid payload");
		}
		in.close();

	}
}
