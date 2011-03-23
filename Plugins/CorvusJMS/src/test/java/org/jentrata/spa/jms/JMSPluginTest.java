package org.jentrata.spa.jms;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hk.hku.cecid.piazza.commons.spa.ExtensionPoint;
import hk.hku.cecid.piazza.commons.test.PluginTest;
import hk.hku.cecid.piazza.corvus.core.Kernel;

import org.junit.Test;

public class JMSPluginTest extends PluginTest{
    
    @Test
    public void testJMSModulesStarted() {
        assertFalse(Kernel.getInstance().hasErrors());
        assertFalse(Kernel.getInstance().getPluginRegistry().hasErrors());
        assertEquals(JMSProcessor.PLUGIN_ID, Kernel.getInstance().getPluginRegistry().getPlugin(JMSProcessor.PLUGIN_ID).getId());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void shouldHaveJMSSenderExceptionPoint() {
        Collection<ExtensionPoint> points = Kernel.getInstance().getPluginRegistry().getPlugin(JMSProcessor.PLUGIN_ID).getExtensionPoints();
        assertThat("org.jentrata.jms.sender",isIn(extensionPointsAsString(points)));
    }
    
    private List<String> extensionPointsAsString(Collection<ExtensionPoint> eps) {
        
        List<String> list = new ArrayList<String>();
        for(ExtensionPoint ep:eps) {
            list.add(ep.getId());
        }
        return list;
    }

}
