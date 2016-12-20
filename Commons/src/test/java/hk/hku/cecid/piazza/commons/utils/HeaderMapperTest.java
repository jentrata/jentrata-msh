package hk.hku.cecid.piazza.commons.utils;

import hk.hku.cecid.piazza.commons.util.HeaderMapper;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by aaronwalker on 20/12/2016.
 */
public class HeaderMapperTest {

    @Test
    public void testHeaderMap() {
        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("ebxml.to", "fromPartyId");
        mappings.put("ebxml.operation", "action");
        Map<String, Object> defaults = new HashMap<String, Object>();
        defaults.put("service", "Manage WorkOrder");

        HeaderMapper mapper = new HeaderMapper(mappings, defaults);
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("ebxml.to", "to");
        headers.put("ebxml.operation", "submitTest");
        headers.put("ebxml.notmapped", "notmapped");

        Map<String, Object> result = mapper.map(headers);
        assertEquals("to", result.get("fromPartyId"));
        assertEquals("submitTest", result.get("action"));
        assertEquals("Manage WorkOrder", result.get("service"));
        assertEquals("notmapped", result.get("ebxml.notmapped"));
        assertEquals(6, result.size());
    }

    @Test
    public void testOverrideDefault() {
        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("ebxml.to", "fromPartyId");
        mappings.put("ebxml.operation", "action");
        Map<String, Object> defaults = new HashMap<String, Object>();
        defaults.put("service", "Manage WorkOrder");
        defaults.put("ebxml.operation", "updateWorkOrder");

        HeaderMapper mapper = new HeaderMapper(mappings, defaults);
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("ebxml.to", "to");
        headers.put("ebxml.operation", "submitTest");
        headers.put("ebxml.notmapped", "notmapped");

        Map<String, Object> result = mapper.map(headers);
        assertEquals("to", result.get("fromPartyId"));
        assertEquals("submitTest", result.get("action"));
        assertEquals("Manage WorkOrder", result.get("service"));
        assertEquals("notmapped", result.get("ebxml.notmapped"));
        assertEquals(6, result.size());
    }
}
