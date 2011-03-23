/**
 * 
 */
package org.jentrata.spa.jms.handler;

import org.apache.camel.Message;


/**
 * @author aaronwalker
 *
 */
public interface MessageHandler {
    
    public void onMessage(Message message);

}
