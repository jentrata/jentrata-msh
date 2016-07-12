package org.jentrata.spa.integration.module;

import hk.hku.cecid.piazza.commons.Sys;
import hk.hku.cecid.piazza.commons.message.MessageHandler;
import hk.hku.cecid.piazza.commons.module.Component;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Created by aaronwalker on 8/07/2016.
 */
public class SpringBootstrapComponent extends Component {

    private ClassPathXmlApplicationContext parent;

    @Override
    protected void init() throws Exception {
        super.init();
        Sys.main.log.debug("Camel Bootstrap");
        parent = new ClassPathXmlApplicationContext();
        parent.refresh();
        Sys.main.log.info("Spring Root parent is active:" + parent.isActive() + " and running " + parent.isRunning());
    }

    public ApplicationContext deployApplicationContext(String contextFile,Map<String,Object> beans) {
        FileSystemXmlApplicationContext newContext = new FileSystemXmlApplicationContext(parent);
        newContext.refresh();
        for(String key: beans.keySet()) {
            newContext.getBeanFactory().registerSingleton(key,beans.get(key));
        }
        newContext.setConfigLocation(contextFile);
        newContext.refresh();
        return newContext;
    }

    public void registerBean(String id, Object bean) {
        parent.getBeanFactory().registerSingleton(id,bean);
    }


    public ApplicationContext getSpringContext() {
        return parent;
    }

    public void shutdown() {
        if(parent != null) {
            parent.stop();
        }
    }


}

