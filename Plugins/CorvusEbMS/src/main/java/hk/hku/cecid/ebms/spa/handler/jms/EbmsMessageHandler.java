/**
 * 
 */
package hk.hku.cecid.ebms.spa.handler.jms;

import hk.hku.cecid.ebms.spa.EbmsProcessor;
import hk.hku.cecid.piazza.commons.message.Message;
import hk.hku.cecid.piazza.commons.message.MessageHandler;


/**
 * @author aaronwalker
 *
 */
public class EbmsMessageHandler implements MessageHandler {

    public void onMessage(Message message) {
    
        EbmsProcessor.core.log.debug("got message:" + message.getSource().toString());
        
    }

}
