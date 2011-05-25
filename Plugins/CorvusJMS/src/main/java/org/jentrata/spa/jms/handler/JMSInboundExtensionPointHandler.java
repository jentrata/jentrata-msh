/**
 * 
 */
package org.jentrata.spa.jms.handler;

import hk.hku.cecid.piazza.commons.spa.Extension;
import hk.hku.cecid.piazza.commons.spa.ExtensionPointIteratedHandler;
import hk.hku.cecid.piazza.commons.spa.PluginException;

/**
 * @author aaronwalker
 *
 */
public class JMSInboundExtensionPointHandler extends ExtensionPointIteratedHandler {

    @Override
    public void processExtension(Extension extension) throws PluginException {
        String queueName = extension.getParameter("queue");
        String className  = extension.getParameter("class");
        String cpaId = extension.getParameter("cpaId");
    }

}
