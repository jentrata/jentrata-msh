package hk.hku.cecid.edi.as2.service;

import hk.hku.cecid.edi.as2.AS2Exception;
import hk.hku.cecid.edi.as2.AS2PlusProcessor;
import hk.hku.cecid.edi.as2.util.AS2MessageStatusReverser;
import hk.hku.cecid.piazza.commons.dao.DAOException;
import hk.hku.cecid.piazza.commons.soap.SOAPFaultException;
import hk.hku.cecid.piazza.commons.soap.SOAPRequestException;
import hk.hku.cecid.piazza.commons.soap.SOAPResponse;
import hk.hku.cecid.piazza.commons.soap.WebServicesAdaptor;
import hk.hku.cecid.piazza.commons.soap.WebServicesRequest;
import hk.hku.cecid.piazza.commons.soap.WebServicesResponse;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Element;

public class AS2PermitRedownloadService extends WebServicesAdaptor {

	
	public static String NAMESPACE = "http://service.as2.edi.cecid.hku.hk/";
	
	 public void serviceRequested(WebServicesRequest request, WebServicesResponse response) 
		throws SOAPException, DAOException{
	  
		  Element[] bodies = request.getBodies();
		  String msgId = getText(bodies, "messageId");
		  
		  AS2PlusProcessor.getInstance().getLogger().debug("Message Reset Receiver service request - "+
       		  "MessageID : " + (msgId ==null?"NULL":msgId));
		  
		
		  AS2MessageStatusReverser msgReverser =
			  			new AS2MessageStatusReverser();
		  
		  try{
			  msgReverser.updateToDownload(msgId);
			  generateReply(response, msgId);
		  }catch(SOAPRequestException soapReqExp){
			  // This is unexpected exception may cause during
			  // generating response back to client
			  throw new SOAPException (soapReqExp);
		  }catch(DAOException daoExp){
			  // This is unexpected DAOException, which may cause by 
			  // 	1. Table Not Found
			  //	2. Database Connection Closed 
			  throw daoExp;
		  }catch(AS2Exception as2Exp){
			  // This exception is expected, which may cause by several Reason,
			  //   1. MessageId is not found in Message Table as inbox message
			  //   2. Message is found but not allowed to RESET back to PS
			  AS2PlusProcessor.getInstance().getLogger().error(
					  "Fail to Reset INBOX Message["+msgId+"] back to PS", as2Exp);
			  generateFault(response, msgId, as2Exp.getMessage());
		  }
	 }
	 
	 private void generateReply(WebServicesResponse response, String messageId)
	 throws SOAPRequestException{
		 try {
	       SOAPElement rootElement = createText("messageId", messageId,
	            		NAMESPACE);
	            response.setBodies(new SOAPElement[] { rootElement });
		 } catch (Exception e) {
			 throw new SOAPRequestException("Unable to generate reply message", e);
		 }
	 }
	 
	 private void generateFault(WebServicesResponse response, String msgId, String errMsg) throws SOAPException{
		SOAPResponse soapResponse = (SOAPResponse) response.getTarget();
		soapResponse.addFault(SOAPFaultException.SOAP_FAULT_SERVER, null,
				"Failed to update message [" + msgId + "] : " + errMsg);
	 }
	 
	 
	 
}
