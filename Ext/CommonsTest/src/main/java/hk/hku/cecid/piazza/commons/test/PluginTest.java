package hk.hku.cecid.piazza.commons.test;

import static org.junit.Assert.assertFalse;
import hk.hku.cecid.piazza.corvus.core.Kernel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PluginTest {

    @Before
    public void initKernel() {
        System.setProperty("sys.module.group", "corvus/corvus.test.module-group.xml");
        System.setProperty("corvus.home", "target/classes");
        Kernel.getInstance();
    }
    
    @After
    public void shutdown() {
        Kernel.getInstance().shutdown();
    }
    
    @Test
    public void testModulesStarted() {
        assertFalse(Kernel.getInstance().hasErrors());
        assertFalse(Kernel.getInstance().getPluginRegistry().hasErrors());
    }
}
