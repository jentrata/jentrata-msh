/**
 * 
 */
package org.jentrata.validation.extension;

import java.util.Properties;

import org.jentrata.validation.ValidationProcessor;
import org.jentrata.validation.Validator;
import org.jentrata.validation.ValidatorComponent;

import hk.hku.cecid.piazza.commons.spa.Extension;
import hk.hku.cecid.piazza.commons.spa.ExtensionPointIteratedHandler;
import hk.hku.cecid.piazza.commons.spa.PluginException;

/**
 * @author aaronwalker
 *
 */
public class PayloadValidationExtensionPointHander extends ExtensionPointIteratedHandler {

    @SuppressWarnings("unchecked")
    @Override
    public void processExtension(Extension extension) throws PluginException {

        String cpaId  = extension.getParameter("cpaId");
        String className  = extension.getParameter("class");
        
        Properties config = new Properties();
        config.putAll(extension.getParameters());
        
        if (className != null && cpaId != null) { 
            Class<? extends Validator> handlerClass = extension.getPlugin().loadClass(className);
            register(cpaId, handlerClass,config);
        } else {
            throw new PluginException("Unable to register payload validator: No class or cpaId defined");                
        }
    }

    private void register(String cpaId, Class<? extends Validator> handlerClass, Properties config) {
        String contentType = config.getProperty("contentType");
        
        ValidatorComponent vc = getComponent();
        if(contentType == null) {
            vc.registerDefault(cpaId, handlerClass,config);
        } else {
            vc.register(cpaId, contentType, handlerClass,config);
        }
    }
    
    protected ValidatorComponent getComponent() {
        return  (ValidatorComponent) ValidationProcessor.core.getComponent(ValidatorComponent.COMP_ID);
    }
}
