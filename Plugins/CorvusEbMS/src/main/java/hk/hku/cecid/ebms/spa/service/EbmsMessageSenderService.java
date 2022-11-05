/* 
 * Copyright(c) 2005 Center for E-Commerce Infrastructure Development, The
 * University of Hong Kong (HKU). All Rights Reserved.
 *
 * This software is licensed under the GNU GENERAL PUBLIC LICENSE Version 2.0 [1]
 * 
 * [1] http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt
 */

package hk.hku.cecid.ebms.spa.service;

import hk.hku.cecid.ebms.pkg.EbxmlMessage;
import hk.hku.cecid.ebms.pkg.MessageHeader;
import hk.hku.cecid.ebms.spa.EbmsProcessor;
import hk.hku.cecid.ebms.spa.EbmsUtility;
import hk.hku.cecid.ebms.spa.dao.PartnershipDAO;
import hk.hku.cecid.ebms.spa.dao.PartnershipDVO;
import hk.hku.cecid.ebms.spa.handler.MessageServiceHandler;
import hk.hku.cecid.ebms.spa.handler.MessageServiceHandlerException;
import hk.hku.cecid.ebms.spa.listener.EbmsRequest;
import hk.hku.cecid.piazza.commons.dao.DAOException;
import hk.hku.cecid.piazza.commons.soap.SOAPFaultException;
import hk.hku.cecid.piazza.commons.soap.SOAPRequest;
import hk.hku.cecid.piazza.commons.soap.SOAPRequestException;
import hk.hku.cecid.piazza.commons.soap.WebServicesAdaptor;
import hk.hku.cecid.piazza.commons.soap.WebServicesRequest;
import hk.hku.cecid.piazza.commons.soap.WebServicesResponse;
import hk.hku.cecid.piazza.commons.util.Generator;
import hk.hku.cecid.piazza.commons.util.StringUtilities;

import java.util.Iterator;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.Element;

/**
 * EbmsMessageSenderService
 * 
 * @author Hugo Y. K. Lam
 *  
 */
public class EbmsMessageSenderService extends WebServicesAdaptor {

    public void serviceRequested(WebServicesRequest request,
            WebServicesResponse response) throws SOAPRequestException,
            DAOException {

        Element[] bodies = request.getBodies();
        String cpaId = getText(bodies, "cpaId");
        String service = getText(bodies, "service");
        String serviceType = getText(bodies, "serviceType");
        String action = getText(bodies, "action");        
        String convId = getText(bodies, "convId");
        String fromPartyId = getText(bodies, "fromPartyId");
        String[] fromPartyIds = StringUtilities.tokenize(fromPartyId, ",");
        String fromPartyType = getText(bodies, "fromPartyType");
        String[] fromPartyTypes = StringUtilities.tokenize(fromPartyType, ",");
        String toPartyId = getText(bodies, "toPartyId");
        String[] toPartyIds = StringUtilities.tokenize(toPartyId, ",");
        String toPartyType = getText(bodies, "toPartyType");
        String[] toPartyTypes = StringUtilities.tokenize(toPartyType, ",");
        String refToMessageId = getText(bodies, "refToMessageId");
        String fromPartyRole = getText(bodies, "fromPartyRole");
        String toPartyRole = getText(bodies, "toPartyRole");
	String messageId = getText(bodies, "messageId");

        if (cpaId == null || service == null || action == null
                || convId == null || fromPartyId == null
                || fromPartyType == null || toPartyId == null
                || toPartyType == null) {
        	throwSoapClientFault("Missing delivery information");
        }
        
        if (fromPartyIds.length != fromPartyTypes.length
                || toPartyIds.length != toPartyTypes.length) {
        	throwSoapClientFault("The number of From/To Party and its type is not same.");
        }        
        
		PartnershipDAO partnershipDAO = (PartnershipDAO) EbmsProcessor.core.dao
				.createDAO(PartnershipDAO.class);
		PartnershipDVO partnershipDVO = (PartnershipDVO) partnershipDAO
				.createDVO();
		partnershipDVO.setCpaId(cpaId);
		partnershipDVO.setService(service);
		partnershipDVO.setAction(action);
		if (!partnershipDAO.findPartnershipByCPA(partnershipDVO)) {
			throwSoapClientFault("No registered sender channel");
		}

        EbmsProcessor.core.log.info("Outbound payload received - cpaId: " 
        		+ cpaId 
        		+ ", service: " 	+ service 
        		+ ", serviceType:" 	+ serviceType        		
                + ", action: " 		+ action 
                + ", convId: " 		+ convId 
                + ", fromPartyId: " + fromPartyId
                + ", fromPartyType: " + fromPartyType 
                + ", fromPartyRole: " + fromPartyRole
                + ", toPartyId: "	  + toPartyId
                + ", toPartyType: " + toPartyType
                + ", toPartyRole: " + toPartyRole
                + ", refToMessageId: " + refToMessageId
                + ", messageId: " + messageId);

        // Construct Ebxml message
        EbxmlMessage ebxmlMessage = null;
        try {
            ebxmlMessage = new EbxmlMessage();
            MessageHeader msgHeader = ebxmlMessage.addMessageHeader();
           
            msgHeader.setCpaId(cpaId);
            msgHeader.setConversationId(convId);
            msgHeader.setService(service);
            msgHeader.setAction(action);
            
            if (serviceType != null && !serviceType.equals("")) {
            	msgHeader.setServiceType(serviceType);
            }
            
            if (messageId == null || messageId.equals("")) {
                messageId = Generator.generateMessageID();
                EbmsProcessor.core.log.info("Generated message id: " + messageId);
	    }
            ebxmlMessage.getMessageHeader().setMessageId(messageId);

            msgHeader.setTimestamp(EbmsUtility.getCurrentUTCDateTime());
            
            for (int i = 0; i < fromPartyIds.length; i++) {
                msgHeader.addFromPartyId(fromPartyIds[i], fromPartyTypes[i]);
            }
            if(fromPartyRole != null && !fromPartyRole.isEmpty()) {
                msgHeader.setFromRole(fromPartyRole);
            }

            for (int i = 0; i < toPartyIds.length; i++) {
                msgHeader.addToPartyId(toPartyIds[i], toPartyTypes[i]);
            }
            if(toPartyRole != null && !toPartyRole.isEmpty()) {
                msgHeader.setToRole(toPartyRole);
            }

            if (refToMessageId != null && !refToMessageId.equals("")) {
            	msgHeader.setRefToMessageId(refToMessageId);
            }
            
            setPayloads(request, ebxmlMessage);

        } catch (Exception e) {
            EbmsProcessor.core.log.error(
            		"Error in constructing ebxml message", e);
            throwSoapServerFault("Error in constructing ebxml message", e);
        }

        try {
        	MessageServiceHandler msh = MessageServiceHandler.getInstance();
            EbmsRequest ebmsRequest = new EbmsRequest(request);
            ebmsRequest.setMessage(ebxmlMessage);
            msh.processOutboundMessage(ebmsRequest, null);
        } catch (MessageServiceHandlerException e) {
            EbmsProcessor.core.log.error("Error in passing ebms Request to msh outbound", e);
            throwSoapServerFault("Error in passing ebms Request to msh outbound", e);
        }

        generateReply(response, messageId);

        EbmsProcessor.core.log.info("Outbound payload processed - cpaId: "
                + cpaId + ", service: " + service + ", action: " + action
                + ", convId: " + convId + ", fromPartyId: " + fromPartyId
                + ", fromPartyType: " + fromPartyType + ", toPartyId: "
                + toPartyId + ", toPartyType: " + toPartyType
                + ", refToMessageId: " + refToMessageId);
    }

	private void setPayloads(WebServicesRequest request, EbxmlMessage ebxmlMessage) 
			throws SOAPException {
		SOAPRequest soapRequest = (SOAPRequest) request.getSource();
		SOAPMessage soapRequestMessage = soapRequest.getMessage();

		Iterator<?> i = soapRequestMessage.getAttachments();

		for (int j = 0; i.hasNext(); j++) {
		    AttachmentPart attachmentPart = (AttachmentPart) i.next();

		    ebxmlMessage
		    	.addPayloadContainer(attachmentPart.getDataHandler(),
		            "Payload-" + String.valueOf(j), null);
		    
		    /**
		     * Modifification by Jumbo
		     * The original code will always override the contentId
		     *  with "Payload-n" all the time, 
		     * 
		     * This change is pending until a complete study of content-id handling in H2O.
		     */
		    /*
		    String contentId = attachmentPart.getContentId();
		    if(contentId == null || contentId.equals("")){
		    	contentId =  "Payload-" + String.valueOf(j);
		    }
		    ebxmlMessage
		            .addPayloadContainer(attachmentPart.getDataHandler(),
		            		contentId , null);
		    */
		}
	}

    private void generateReply(WebServicesResponse response, String messageId)
            throws SOAPRequestException {
        try {
            SOAPElement responseElement = createText("message_id", messageId,
                    "http://service.ebms.edi.cecid.hku.hk/");
            response.setBodies(new SOAPElement[] { responseElement });
        } catch (Exception e) {
        	throwSoapServerFault("Unable to generate reply message", e);
        }
    }

    protected boolean isCacheEnabled() {
        return false;
    }
    
	private void throwSoapServerFault(String message, Exception e) throws SOAPFaultException {
		throw new SOAPFaultException(SOAPFaultException.SOAP_FAULT_SERVER, message, e);
	}
    
	private void throwSoapClientFault(String message) throws SOAPFaultException {
		throw new SOAPFaultException(SOAPFaultException.SOAP_FAULT_CLIENT, message);
	}
}
