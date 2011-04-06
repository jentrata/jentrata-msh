/**
 * 
 */
package hk.hku.cecid.ebms.spa.handler.jms;

import hk.hku.cecid.ebms.pkg.EbxmlMessage;
import hk.hku.cecid.ebms.pkg.MessageHeader;
import hk.hku.cecid.ebms.spa.EbmsProcessor;
import hk.hku.cecid.ebms.spa.EbmsUtility;
import hk.hku.cecid.ebms.spa.handler.MessageServiceHandler;
import hk.hku.cecid.ebms.spa.listener.EbmsRequest;
import hk.hku.cecid.ebms.spa.util.PartnershipDAOHelper;
import hk.hku.cecid.piazza.commons.activation.ByteArrayDataSource;
import hk.hku.cecid.piazza.commons.dao.DAOException;
import hk.hku.cecid.piazza.commons.message.Message;
import hk.hku.cecid.piazza.commons.message.MessageHandler;
import hk.hku.cecid.piazza.commons.util.Generator;
import hk.hku.cecid.piazza.commons.util.Logger;

import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;


/**
 * @author aaronwalker
 *
 */
public class EbmsMessageHandler implements MessageHandler {

    public void onMessage(Message message) {
    
        log().debug("got message:" + message.getSource().toString());
        try {
            EbmsRequest ebmsRequest = buildEbmsRequest(message);
            getMSH().processOutboundMessage(ebmsRequest, null);
        } catch (Exception e) {
            log().error("Failed to process outbound message: " + e);
            log().debug("",e);
            throw new RuntimeException(e);
        }
    }
    
    protected MessageServiceHandler getMSH() {
        return MessageServiceHandler.getInstance();
    }

    private EbmsRequest buildEbmsRequest(Message message) throws Exception {
        EbmsRequest request = new EbmsRequest();
        
        //extract delivery info from the message
        Map<String,Object> header = message.getHeader();
        String cpaId = asString(header,"cpaId");
        String conversationId = asString(header,"conversationId");
        String serviceType = asString(header, "serviceType");
        String service = asString(header, "service");
        String action = asString(header,"action");
        String refToMessageId = asString(header,"refToMessageId");
        String [] toPartyIds = asStringArray(header,"toPartyId");
        String [] toPartyIdTypes = asStringArray(header, "toPartyType");
        String [] fromPartyIds = asStringArray(header,"fromPartyId");
        String [] fromPartyIdTypes = asStringArray(header, "fromPartyType");

        //check if there is a valid registered channel for this message
        if(!checkValidChannel(cpaId, service, action)) {
            throw new RuntimeException("No registered sender channel");
        }

        //create the ebxml message
        EbxmlMessage ebxml = new EbxmlMessage();
        MessageHeader ebxmlHeader = ebxml.addMessageHeader();

        addFromParty(ebxmlHeader,fromPartyIds,fromPartyIdTypes);
        addToParty(ebxmlHeader,toPartyIds,toPartyIdTypes);
        
        //These methods need to be call in this exact order
        ebxmlHeader.setCpaId(cpaId);
        ebxmlHeader.setConversationId(conversationId);
        ebxmlHeader.setService(service);
        ebxmlHeader.setAction(action);        
        
        if (serviceType != null && !serviceType.equals("")) {
            ebxmlHeader.setServiceType(serviceType);
        }
        
        String messageId = Generator.generateMessageID();
        ebxmlHeader.setMessageId(messageId);
        log().info("Genereating message id: " + messageId);
        
        if(refToMessageId != null) {
            ebxmlHeader.setRefToMessageId(refToMessageId);
        }
        
        ebxmlHeader.setTimestamp(EbmsUtility.getCurrentUTCDateTime());
        
        log().info("Outbound payload received - cpaId: " 
                + cpaId 
                + ", service: "     + service 
                + ", serviceType:"  + serviceType               
                + ", action: "      + action 
                + ", convId: "      + conversationId 
                + ", fromPartyId: " + fromPartyIds
                + ", fromPartyType: " + fromPartyIdTypes 
                + ", toPartyId: "     + toPartyIds
                + ", toPartyType: " + toPartyIdTypes
                + ", refToMessageId: " + refToMessageId);

        attachPayloads(ebxml,message.getPayloads());
        request.setSource(message);
        request.setMessage(ebxml);
        return request;
    }

    protected boolean checkValidChannel(String cpaId, String service, String action) throws DAOException {
        return PartnershipDAOHelper.isChannelRegistered(cpaId, service, action);
    }
    
    protected Logger log() {
        return EbmsProcessor.core.log;
    }
    
    private void attachPayloads(EbxmlMessage ebxml, List<byte[]> payloads) throws Exception {
        int i=0;
        for(byte [] payload:payloads) {
            ebxml.addPayloadContainer(new DataHandler(new String(payload),"text/xml"), "Payload-" + i++, null);
        }
    }
    
    private void addToParty(MessageHeader ebxmlHeader, String[] toPartyIds, String[] toPartyIdTypes) throws Exception {
        for(int i=0;i<toPartyIds.length;i++) {
            ebxmlHeader.addToPartyId(toPartyIds[i], toPartyIdTypes[i]);
        }
    }

    private void addFromParty(MessageHeader ebxmlHeader, String[] fromPartyIds, String[] fromPartyIdTypes) throws Exception {
        for(int i=0;i<fromPartyIds.length;i++) {
            ebxmlHeader.addFromPartyId(fromPartyIds[i], fromPartyIdTypes[i]);
        }        
    }

    private String[] asStringArray(Map<String, Object> map, String key) {
        String value = asString(map, key);
        if(value != null) {
            return value.split(",");
        }
        return null;
    }

    private String asString(Map<String,Object> map, String key) {
        Object o = map.get(key);
        if(o != null) {
            return o.toString();
        }
        return null;
    }

}
