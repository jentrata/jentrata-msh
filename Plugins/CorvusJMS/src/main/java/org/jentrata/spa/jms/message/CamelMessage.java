/**
 * 
 */
package org.jentrata.spa.jms.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import hk.hku.cecid.piazza.commons.message.Message;

/**
 * @author aaronwalker
 *
 */
public class CamelMessage implements Message {

    private org.apache.camel.Message camelMessage;
    private Map<String,Object> header;
    private List<byte[]> payloads = new ArrayList<byte[]>();
    
    public CamelMessage(org.apache.camel.Message camelMessage, Properties defaultConfig) {
        if(camelMessage == null) {
            throw new IllegalArgumentException("camelMessage can not be null");
        }
        this.camelMessage = camelMessage;
        header = mergeHeader(camelMessage.getHeaders(),defaultConfig);
        addPayload();
        
    }

    public Map<String, Object> getHeader() {
        return header;
    }

    public List<byte[]> getPayloads() {
        return payloads;
    }

    public Object getSource(){
        return camelMessage;
    }
    
    //merge the default properties defined by the extension point with
    //the one supplied by the message, properties on the message will
    //override the defaults
    private Map<String, Object> mergeHeader(Map<String, Object> headers, Properties defaultConfig) {
        Map<String,Object> merged = new HashMap<String,Object>();
        merged.putAll(headers);
        for(String key:VALID_HEADER_KEYS) {
            if(!headers.containsKey(key)) {
                merged.put(key, defaultConfig.get(key));
            }
        }
        return merged;
    }
    
    //add the body of the camel message to the payloads
    //currently we only support send a single payload from
    //a camel message
    private void addPayload() {
        String body = camelMessage.getBody(String.class);
        if(body != null) {
            payloads.add(body.getBytes());
        }
    }

}
