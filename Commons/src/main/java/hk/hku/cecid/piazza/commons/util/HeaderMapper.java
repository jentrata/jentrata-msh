package hk.hku.cecid.piazza.commons.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by aaronwalker on 20/12/2016.
 */
public class HeaderMapper {

    private Map<String, String> mappings = new HashMap<String, String>();
    private Map<String, Object> defaults = new HashMap<String, Object>();

    public HeaderMapper(Map<String, String> mappings, Map<String, Object> defaults) {
        this.mappings = mappings;
        this.defaults = defaults;
    }

    public Map<String, Object> map(Map<String, Object> src) {
        src = merge(src,getDefaults());
        for(String srcKey : getMappings().keySet()) {

            src.put(mappings.get(srcKey),src.get(srcKey));
        }
        return src;
    }

    public Map<String, String> getMappings() {
        return mappings;
    }

    public void setMappings(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    public Map<String, Object> getDefaults() {
        return defaults;
    }

    public void setDefaults(Map<String, Object> defaults) {
        this.defaults = defaults;
    }

    private Map<String, Object> merge(Map<String, Object> src, Map<String, Object> dest) {
        for(String key : dest.keySet()) {
            if(!src.containsKey(key)) {
                src.put(key, dest.get(key));
            }
        }
        return src;
    }
}
