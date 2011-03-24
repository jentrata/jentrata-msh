/**
 * 
 */
package org.jentrata.spa.jms.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hk.hku.cecid.piazza.commons.message.Message;

/**
 * @author aaronwalker
 *
 */
public class CamelMessage implements Message {

    private org.apache.camel.Message camelMessage;
    
    public CamelMessage(org.apache.camel.Message camelMessage) {
        this.camelMessage = camelMessage;
    }
    
    public Map<String, Object> getHeader() {
        return camelMessage.getHeaders();
    }

    public List<byte[]> getPayloads() {
        return new ArrayList<byte[]>();
    }

    public Object getSource(){
        return camelMessage;
    }

}
