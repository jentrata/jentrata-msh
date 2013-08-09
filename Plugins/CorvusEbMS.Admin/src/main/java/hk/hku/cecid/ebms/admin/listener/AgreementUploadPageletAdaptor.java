/*
 * Created on Nov 3, 2004
 *
 */
package hk.hku.cecid.ebms.admin.listener;

import hk.hku.cecid.ebms.spa.EbmsProcessor;
import hk.hku.cecid.ebms.spa.dao.PartnershipDAO;
import hk.hku.cecid.ebms.spa.dao.PartnershipDVO;
import hk.hku.cecid.piazza.commons.dao.DAOException;
import hk.hku.cecid.piazza.commons.module.ComponentException;
import hk.hku.cecid.piazza.commons.security.SMimeMessage;
import hk.hku.cecid.piazza.commons.util.PropertyTree;
import hk.hku.cecid.piazza.commons.util.UtilitiesException;
import hk.hku.cecid.piazza.corvus.admin.listener.AdminPageletAdaptor;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.dom4j.DocumentException;
import org.jentrata.ebxml.cpa.*;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author Donahue Sze
 * 
 */
public class AgreementUploadPageletAdaptor extends AdminPageletAdaptor {

    private String selectedPartyName = null;

    /*
     * (non-Javadoc)
     * 
     * @see hk.hku.cecid.piazza.commons.pagelet.xslt.BorderLayoutPageletAdaptor#getCenterSource(javax.servlet.http.HttpServletRequest)
     */
    protected Source getCenterSource(HttpServletRequest request) {

        PropertyTree dom = new PropertyTree();
        dom.setProperty("/partnership", "");

        boolean isMultipart = FileUpload.isMultipartContent(request);

        if (isMultipart) {
            DiskFileUpload upload = new DiskFileUpload();
            try {
                FileItem realFileItem = null;
                boolean hasFileField = false;
                List fileItems = upload.parseRequest(request);

                Iterator iter = fileItems.iterator();
                while (iter.hasNext()) {
                    FileItem item = (FileItem) iter.next();

                    if (item.isFormField()) {
                        if (item.getFieldName().equals("party_name")) {
                            selectedPartyName = item.getString();
                        }
                    } else {
                        hasFileField = true;
                        if (item.getName().equals("")) {
                            request.setAttribute(ATTR_MESSAGE,"No file specified");
                        } else if (item.getSize() == 0) {
                            request.setAttribute(ATTR_MESSAGE,"The file is no content");
                        } else if (!item.getContentType().equalsIgnoreCase("text/xml")) {
                            request.setAttribute(ATTR_MESSAGE,"It is not a xml file");
                        } else {
                            realFileItem = item;
                        }
                    }
                }

                if (!hasFileField) {
                    request.setAttribute(ATTR_MESSAGE,"There is no file field in the request paramters");
                }

                if (selectedPartyName.equalsIgnoreCase("")) {
                    request.setAttribute(ATTR_MESSAGE, "There is no party name field in the request paramters");
                }

                if (realFileItem != null && !selectedPartyName.equalsIgnoreCase("")) {
                    String errorMessage = processUploadedXml(dom, realFileItem);
                    if (errorMessage != null) {
                        request.setAttribute(ATTR_MESSAGE, errorMessage);
                    }

                }
            } catch (Exception e) {
                EbmsProcessor.core.log.error("Exception throw when upload the file", e);
                request.setAttribute(ATTR_MESSAGE,"Exception throw when upload the file");
            }
        }
        return dom.getSource();
    }

    /**
     * @param item
     * @throws IOException
     * @throws DocumentException
     * @throws UtilitiesException
     * @throws ComponentException
     * @throws DAOException
     */
    private String processUploadedXml(PropertyTree dom, FileItem item) throws IOException, DocumentException,
            UtilitiesException, ComponentException {
        try {
            InputStream uploadedStream = item.getInputStream();
            CollaborationProtocolAgreement cpa = parseCPA(uploadedStream);
            PartyInfo partyInfo = findMatchingPartyInfo(cpa,selectedPartyName);
            if(partyInfo == null) {
                throw new RuntimeException("There is no party name match in the cpa");
            }
            List<PartnershipDVO> partnerships = addPartnerships(cpa, partyInfo);
            render(partnerships,dom);

        } catch (Exception e) {
            EbmsProcessor.core.log.error("Error in processing upploaded xml", e);
            return e.getMessage();
        }
        return null;
    }

    private PartyInfo findMatchingPartyInfo(CollaborationProtocolAgreement agreement, String partyName) {
        for(PartyInfo partyInfo : agreement.getPartyInfo()) {
            if(partyInfo.getPartyName().equals(partyName)) {
                return partyInfo;
            }
        }
        return null;
    }

    private CollaborationProtocolAgreement parseCPA(InputStream stream) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(CollaborationProtocolAgreement.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            CollaborationProtocolAgreement agreement = (CollaborationProtocolAgreement) unmarshaller.unmarshal(stream);
            return agreement;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private List<PartnershipDVO> addPartnerships(CollaborationProtocolAgreement cpa,  PartyInfo partyInfo) throws DAOException {
        List<PartnershipDVO> partnerships = new ArrayList<PartnershipDVO>();
        for(CollaborationRole collaborationRole : partyInfo.getCollaborationRole()) {
            String serviceName = collaborationRole.getServiceBinding().getService().getValue();
            for(CanSend canSend : collaborationRole.getServiceBinding().getCanSend()) {
                PartnershipDAO partnershipDAO = (PartnershipDAO) EbmsProcessor.core.dao.createDAO(PartnershipDAO.class);
                PartnershipDVO partnershipDVO = (PartnershipDVO) partnershipDAO.createDVO();

                String action = canSend.getThisPartyActionBinding().getAction();
                DeliveryChannel channel = canSend.getOtherPartyActionBinding().getChannel();
                partnershipDVO.setPartnershipId(cpa.getCpaid() + "," + channel.getChannelId() + "," + action);
                partnershipDVO.setCpaId(cpa.getCpaid());
                partnershipDVO.setService(serviceName);
                partnershipDVO.setAction(action);
                partnershipDVO.setDisabled("false");
                partnershipDVO.setIsHostnameVerified("false");

                //Agreement Messaging Characteristic
                partnershipDVO.setActor(channel.getMessagingCharacteristics().getActor().value());
                partnershipDVO.setAckRequested(channel.getMessagingCharacteristics().getAckRequested().value());
                partnershipDVO.setAckSignRequested(channel.getMessagingCharacteristics().getAckSignatureRequested().value());
                partnershipDVO.setDupElimination(channel.getMessagingCharacteristics().getDuplicateElimination().value());
                partnershipDVO.setSyncReplyMode(channel.getMessagingCharacteristics().getSyncReplyMode().value());

                //Agreement Transport
                partnershipDVO.setTransportProtocol(channel.getTransport().getTransportReceiver().getTransportProtocol().getValue());
                partnershipDVO.setTransportEndpoint(channel.getTransport().getTransportReceiver().getEndpoint().get(0).getUri());

                //Agreement DocExchange
                partnershipDVO.setMessageOrder(channel.getDocExchange().getEbXMLSenderBinding().getReliableMessaging().getMessageOrderSemantics().value());
                partnershipDVO.setPersistDuration(channel.getDocExchange().getEbXMLSenderBinding().getPersistDuration().toString());
                partnershipDVO.setRetryInterval((int) channel.getDocExchange().getEbXMLSenderBinding().getReliableMessaging().getRetryInterval().getTimeInMillis(new Date()));
                partnershipDVO.setRetries(channel.getDocExchange().getEbXMLSenderBinding().getReliableMessaging().getRetries().intValue());

                //Digital Signature & Encryption
                partnershipDVO.setSignRequested(String.valueOf(canSend.getOtherPartyActionBinding().getBusinessTransactionCharacteristics().isIsNonRepudiationRequired()));
                partnershipDVO.setEncryptRequested("false");
                if(channel.getDocExchange().getEbXMLSenderBinding().getSenderNonRepudiation() != null) {
                    partnershipDVO.setDsAlgorithm(channel.getDocExchange().getEbXMLSenderBinding().getSenderNonRepudiation().getSignatureAlgorithm().get(0).getValue());
                    partnershipDVO.setMdAlgorithm(channel.getDocExchange().getEbXMLSenderBinding().getSenderNonRepudiation().getHashFunction());
                }

                if(channel.getDocExchange().getEbXMLSenderBinding().getSenderDigitalEnvelope() != null) {
                    String digitalEnvelopeProtocol = channel.getDocExchange().getEbXMLSenderBinding().getSenderDigitalEnvelope().getDigitalEnvelopeProtocol().getValue();
                    String encryptionAlgorithm = channel.getDocExchange().getEbXMLSenderBinding().getSenderDigitalEnvelope().getEncryptionAlgorithm().get(0).getValue();
                    if (encryptionAlgorithm != null) {
                        if (encryptionAlgorithm.toLowerCase().contains("rc2")) {
                            encryptionAlgorithm = SMimeMessage.ENCRYPT_ALG_RC2_CBC;
                        } else {
                            encryptionAlgorithm = SMimeMessage.ENCRYPT_ALG_DES_EDE3_CBC;
                        }
                    }
                    partnershipDVO.setEncryptRequested("true");
                    partnershipDVO.setEncryptAlgorithm(encryptionAlgorithm);
                    partnershipDVO.setEncryptCert(null);
                }

                if(!partnershipDAO.retrieve(partnershipDVO)) {
                    EbmsProcessor.core.log.info("Adding Partnership " + partnershipDVO.getPartnershipId());
                    partnershipDAO.create(partnershipDVO);
                    partnerships.add(partnershipDVO);
                } else {
                    EbmsProcessor.core.log.info("Partnership " + partnershipDVO.getPartnershipId() + " already exists");
                }
            }
        }
        return partnerships;
    }

    private void render(List<PartnershipDVO> partnerships, PropertyTree dom) {
        for(int i=0;i<partnerships.size();i++) {
            PartnershipDVO partnership = partnerships.get(i);
            String partnershipOffset = "partnership[" + i + "]";
            dom.setProperty(partnershipOffset + "/agreement_added","" + true);
            dom.setProperty(partnershipOffset + "/partnership_id",partnership.getPartnershipId());
            dom.setProperty(partnershipOffset + "/cpa_id",partnership.getCpaId());
            dom.setProperty(partnershipOffset + "/service",partnership.getService());
            dom.setProperty(partnershipOffset + "/action_id",partnership.getAction());
            dom.setProperty(partnershipOffset + "/transport_protocol", emptyStringIfNull(partnership.getTransportProtocol()));
            dom.setProperty(partnershipOffset + "/transport_endpoint", emptyStringIfNull(partnership.getTransportEndpoint()));
            dom.setProperty(partnershipOffset + "/sync_reply_mode",emptyStringIfNull(partnership.getSyncReplyMode()));
            dom.setProperty(partnershipOffset + "/ack_requested",partnership.getAckRequested());
            dom.setProperty(partnershipOffset + "/ack_sign_requested", partnership.getAckSignRequested());
            dom.setProperty(partnershipOffset + "/dup_elimination", partnership.getDupElimination());
            dom.setProperty(partnershipOffset + "/actor", emptyStringIfNull(partnership.getActor()));
            dom.setProperty(partnershipOffset + "/disabled", partnership.getDisabled());
            dom.setProperty(partnershipOffset + "/retries", String.valueOf(partnership.getRetries()));
            dom.setProperty(partnershipOffset + "/retry_interval", String.valueOf(partnership.getRetryInterval()));
            dom.setProperty(partnershipOffset + "/persist_duration", emptyStringIfNull(partnership.getPersistDuration()));
            dom.setProperty(partnershipOffset + "/message_order",emptyStringIfNull(partnership.getMessageOrder()));
            dom.setProperty(partnershipOffset + "/sign_requested",partnership.getSignRequested());
            dom.setProperty(partnershipOffset + "/ds_algorithm",partnership.getDsAlgorithm());
            dom.setProperty(partnershipOffset + "/md_algorithm", emptyStringIfNull(partnership.getMdAlgorithm()));
            dom.setProperty(partnershipOffset + "/encrypt_requested",partnership.getEncryptRequested());
            dom.setProperty(partnershipOffset + "/encrypt_algorithm", emptyStringIfNull(partnership.getEncryptAlgorithm()));
        }
    }

    private String emptyStringIfNull(String s) {
        return s != null ? s : "";
    }

}