/**
 * 
 */
package org.jentrata.validation;

import hk.hku.cecid.piazza.commons.module.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author aaronwalker
 *
 */
public class ValidatorComponent extends Component {
    
    public static final String COMP_ID = "payloadValidator";
    
    private Map<String,Validator> validators;
    
    @Override
    protected void init() throws Exception {
        validators = new HashMap<String, Validator>();
    }
    
    public void registerDefault(String cpaId, Class<? extends Validator> c, Properties config) {
        Validator v = instance(c,config);
        validators.put(cpaId, v); //register the default validator for the cpaId
    }
    
    public void register(String cpaId,String contentType, Class<? extends Validator> c, Properties config) {
        Validator v = instance(c,config);
        validators.put(cpaId + ":" + contentType,v);
    }
    
    public Validator getDefaultValidator(String cpaId) {
        return validators.get(cpaId);
    }
    
    public Validator getValidator(String cpaId, String contentType) {
        Validator v = validators.get(cpaId + ":" + contentType);
        if(v == null) {
            return getDefaultValidator(cpaId);
        }
        return v;
    }
    
    protected Validator instance(Class<? extends Validator> c, Properties props) {
        if(c != null) {
            try {
                Validator v  = c.newInstance();
                v.init(props);
                return v;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
