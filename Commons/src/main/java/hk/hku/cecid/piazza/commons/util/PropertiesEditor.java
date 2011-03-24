/**
 * 
 */
package hk.hku.cecid.piazza.commons.util;

import java.util.Properties;

/**
 * @author aaronwalker
 *
 */
public class PropertiesEditor extends Properties {

    /**
     * 
     */
    private static final long serialVersionUID = 3551672337159812403L;

    public PropertiesEditor(Properties parameters) {
        super(parameters);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        String value =  super.getProperty(key, defaultValue);
        return StringUtilities.propertyValue(value);
    }

    @Override
    public String getProperty(String key) {
        String value = super.getProperty(key);
        return StringUtilities.propertyValue(value);
    }
}
