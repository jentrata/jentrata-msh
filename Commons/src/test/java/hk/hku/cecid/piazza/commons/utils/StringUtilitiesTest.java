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
        String property = "${test.property:}${test.property:}";

        assertEquals(null,StringUtilities.propertyValue(property));
        
        System.setProperty("test.property", "somevalue");
        
        assertEquals("somevaluesomevalue",StringUtilities.propertyValue(property));

    }
    
    @Test
    public void testSystemPropertyReplaceWithMultipleProps() {
        String property = "${test.property:default}/${test.property:}";

        assertEquals("default/",StringUtilities.propertyValue(property));
        
        System.setProperty("test.property", "somevalue");
        
        assertEquals("somevalue/somevalue",StringUtilities.propertyValue(property));

    }
    
    @Test
    public void testSystemPropertyReplaceNotDefined() {
        String property = "${jentrata.activemq.connectionFactoryUrl:vm://localhost?broker.persistent=false}";

        assertEquals("vm://localhost?broker.persistent=false",StringUtilities.propertyValue(property));

    }
    
    
    
    @Test
    public void testSplitFirstSimple() {
        
        assertNull(StringUtilities.splitFirst(null, "|"));
        
        String result [] = StringUtilities.splitFirst("first half|second half", "|");
        
        assertEquals(2,result.length);
        assertEquals("first half",result[0]);
        assertEquals("second half",result[1]);
        
    }
    
    @Test
    public void testSplitFirstMultiple() {
        
        String result [] = StringUtilities.splitFirst("first half|second half|third", "|");
        
        assertEquals(2,result.length);
        assertEquals("first half",result[0]);
        assertEquals("second half|third",result[1]);
        
    }
}
