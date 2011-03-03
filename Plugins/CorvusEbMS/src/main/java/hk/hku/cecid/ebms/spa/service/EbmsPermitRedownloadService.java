package hk.hku.cecid.ebms.spa.service;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Element;

import hk.hku.cecid.ebms.spa.EbmsProcessor;
import hk.hku.cecid.ebms.spa.util.EbmsMessageStatusReverser;
import hk.hku.cecid.piazza.commons.dao.DAOException;
import hk.hku.cecid.piazza.commons.soap.SOAPFaultException;
import hk.hku.cecid.piazza.commons.soap.SOAPRequestException;
import hk.hku.cecid.piazza.commons.soap.SOAPResponse;
import hk.hku.cecid.piazza.commons.soap.WebServicesAdaptor;
import hk.hku.cecid.piazza.commons.soap.WebServicesRequest;
import hk.hku.cecid.piazza.commons.soap.WebServicesResponse;

public class EbmsPermitRedownloadService  extends WebServicesAdaptor{
	
	public static String NAMESPACE = "http://service.ebms.edi.cecid.hku.hk/";

	public void serviceRequested(WebServicesRequest request,
            WebServicesResponse response) throws SOAPException,
            DAOException {
		
		
		  Element[] bodies = request.getBodies();
		  String msgId = getText(bodies, "messageId");
		  
		  EbmsMessageStatusReverser 
		  	msgReverser = new EbmsMessageStatusReverser();
		  
		  
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
		  }catch(Exception exp){
			  EbmsProcessor.core.log.error(
					  "Fail to reset INBOX Message["+msgId+"] back to PS", exp);
			  genreateFault(response, msgId, exp.getMessage());
		  }
	}
	
	private void generateReply(WebServicesResponse response, String messageId)
		throws SOAPRequestException{
		try {
			SOAPElement rootElement = createText("messageId", messageId,
					NAMESPACE);
			response.setBodies(new SOAPElement[] { rootElement });
		} catch (SOAPException e) {
			throw new SOAPRequestException("Unable to generate reply message", e);
		}
	}
	
	private void genreateFault(WebServicesResponse response, String messageId, String errMsg)
		throws SOAPException{
		SOAPResponse soapResponse = (SOAPResponse) response.getTarget();
		soapResponse.addFault(SOAPFaultException.SOAP_FAULT_SERVER, null,
				"Failed to update message [" + messageId + "] : " + errMsg);
	}
	
}
