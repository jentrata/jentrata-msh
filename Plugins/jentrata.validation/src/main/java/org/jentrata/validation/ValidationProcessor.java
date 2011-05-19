package org.jentrata.validation;

import hk.hku.cecid.piazza.commons.Sys;
import hk.hku.cecid.piazza.commons.module.ModuleException;
import hk.hku.cecid.piazza.commons.module.ModuleGroup;
import hk.hku.cecid.piazza.commons.module.SystemModule;
import hk.hku.cecid.piazza.commons.spa.Plugin;
import hk.hku.cecid.piazza.commons.spa.PluginException;
import hk.hku.cecid.piazza.commons.spa.PluginHandler;

public class ValidationProcessor implements PluginHandler {

    public static final String PLUGIN_ID = "org.jentrata.validation";
    

    private static ModuleGroup moduleGroup;
    
    public static SystemModule core;
    
    private static ValidationProcessor processor;
 
    public static ValidationProcessor getInstance() {
        if (processor == null)
            throw new ModuleException("ValidationProcessor not initialized");
        
        return processor;
    }
    
    public void processActivation(Plugin plugin) throws PluginException {
        Sys.main.log.debug("ValidationProcessor activation");
        processor = this;
        
        String mgDescriptor = plugin.getParameters().getProperty("module-group-descriptor");
        moduleGroup = new ModuleGroup(mgDescriptor, plugin.getClassLoader());
        Sys.getModuleGroup().addChild(moduleGroup);
        
        core = moduleGroup.getSystemModule();
        moduleGroup.startActiveModules();
        
        if (core == null) {
            throw new PluginException("Validation system module not found");
        }
    }

    public void processDeactivation(Plugin plugin) throws PluginException {
        moduleGroup.stopActiveModules();
    }

}
