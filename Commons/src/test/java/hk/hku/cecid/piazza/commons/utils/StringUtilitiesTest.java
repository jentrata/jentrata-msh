package hk.hku.cecid.piazza.commons.utils;


import static org.junit.Assert.*;
import hk.hku.cecid.piazza.commons.util.StringUtilities;

import org.junit.After;
import org.junit.Test;

public class StringUtilitiesTest {

    @After
    public void reset() {
        System.clearProperty("test.property");
    }
    
    @Test
    public void testSystemPropertyReplace() {
       String property = "${test.property}";
       System.setProperty("test.property", "somevalue");
       
       assertEquals("somevalue",StringUtilities.propertyValue(property));
       assertEquals("somevalue/test",StringUtilities.propertyValue(property + "/test"));
       
    }
    
    @Test
    public void testSystemPropertyReplaceWithDefault() {
        String property = "${test.property:defaultvalue}";

        assertEquals("defaultvalue",StringUtilities.propertyValue(property));
        
        System.setProperty("test.property", "somevalue");
        
        assertEquals("somevalue",StringUtilities.propertyValue(property));

    }
    
    @Test
    public void testSystemPropertyReplaceWithDefaultWithColonInTheValues() {
        String property = "${test.property:defaultvalue:value1:value2}";

        assertEquals("defaultvalue:value1:value2",StringUtilities.propertyValue(property));
        
        System.setProperty("test.property", "somevalue");
        
        assertEquals("somevalue",StringUtilities.propertyValue(property));

    }
    
    @Test
    public void testSystemPropertyReplaceWithEmptyDefault() {
        String property = "${test.property:}";

        assertEquals(null,StringUtilities.propertyValue(property));
        
        System.setProperty("test.property", "somevalue");
        
        assertEquals("somevalue",StringUtilities.propertyValue(property));

    }
}
