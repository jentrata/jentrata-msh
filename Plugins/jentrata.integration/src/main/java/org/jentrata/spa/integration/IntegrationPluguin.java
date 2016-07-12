package org.jentrata.spa.integration;

import hk.hku.cecid.piazza.commons.Sys;
import hk.hku.cecid.piazza.commons.module.ModuleException;
import hk.hku.cecid.piazza.commons.module.ModuleGroup;
import hk.hku.cecid.piazza.commons.module.SystemModule;
import hk.hku.cecid.piazza.commons.spa.Plugin;
import hk.hku.cecid.piazza.commons.spa.PluginException;
import hk.hku.cecid.piazza.commons.spa.PluginHandler;
import org.jentrata.spa.integration.module.SpringBootstrapComponent;
import org.springframework.context.ApplicationContext;

/**
 * Created by aaronwalker on 11/07/2016.
 */
public class IntegrationPluguin implements PluginHandler {

    public static final String PLUGIN_ID = "org.jentrata.jms";
    public static final String JMS_COMP_ID = "jms";

    private static ModuleGroup moduleGroup;

    public static SystemModule core;

    private static IntegrationPluguin pluginHandler;

    public static IntegrationPluguin getInstance() {
        if (pluginHandler == null) {
            throw new ModuleException("IntegrationPluguin not initialized");
        }
        return pluginHandler;
    }

    @Override
    public void processActivation(Plugin plugin) throws PluginException {
        Sys.main.log.debug("Integration Plugin activation");
        pluginHandler = this;

        String mgDescriptor = plugin.getParameters().getProperty("module-group-descriptor");
        moduleGroup = new ModuleGroup(mgDescriptor, plugin.getClassLoader());
        Sys.getModuleGroup().addChild(moduleGroup);

        core = moduleGroup.getSystemModule();
        moduleGroup.startActiveModules();

        if (core == null) {
            throw new PluginException("Integration system module not found");
        }
    }

    @Override
    public void processDeactivation(Plugin plugin) throws PluginException {
        moduleGroup.stopActiveModules();

        SpringBootstrapComponent camel = (SpringBootstrapComponent) core.getComponent("spring");
        if(camel != null) {
            camel.shutdown();
        }
    }

    public SpringBootstrapComponent getSpringBootstrapComponent() {
        return ((SpringBootstrapComponent) moduleGroup.getModule("spring").getComponent("bootstrap"));
    }

}
