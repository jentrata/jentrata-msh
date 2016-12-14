/*
 * Created on Nov 3, 2004
 *
 */
package hk.hku.cecid.ebms.admin.listener;

import hk.hku.cecid.ebms.spa.EbmsProcessor;
import hk.hku.cecid.ebms.spa.dao.PartnershipDAO;
import hk.hku.cecid.ebms.spa.dao.PartnershipDVO;
import hk.hku.cecid.piazza.commons.dao.DAOException;
import hk.hku.cecid.piazza.commons.dao.DVO;
import hk.hku.cecid.piazza.commons.io.IOHandler;
import hk.hku.cecid.piazza.commons.module.ComponentException;
import hk.hku.cecid.piazza.commons.security.SMimeMessage;
import hk.hku.cecid.piazza.commons.util.PropertyTree;
import hk.hku.cecid.piazza.commons.util.UtilitiesException;
import hk.hku.cecid.piazza.corvus.admin.listener.AdminPageletAdaptor;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.dom4j.DocumentException;
import org.jentrata.ebxml.cpa.*;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Date;
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

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

        if (isMultipart) {
            FileItemFactory factory = new DiskFileItemFactory();
            try {
                FileItem realFileItem = null;
                boolean hasFileField = false;
                ServletFileUpload upload = new ServletFileUpload(factory);
                List<FileItem> fileItems = upload.parseRequest(request);
                FileItem verificationCert = null;
                FileItem encryptionCert = null;

                for(FileItem item : fileItems) {
                    if(item.isFormField() && item.getFieldName().equals("party_name")) {
                        selectedPartyName = item.getString();
                    } else if(item.getFieldName().equals("cpa")) {
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
                    } else if (item.getFieldName().equals("verify_cert") && !item.getName().equals("")) {
                        verificationCert = item;
                    } else if (item.getFieldName().equals("encrypt_cert") && !item.getName().equals("")) {
                        encryptionCert = item;
                    }
                }

                if (!hasFileField) {
                    request.setAttribute(ATTR_MESSAGE,"There is no file field in the request parameters");
                }

                if (selectedPartyName == null || selectedPartyName.isEmpty()) {
                    request.setAttribute(ATTR_MESSAGE, "There is no party name field in the request parameters");
                }

                if (realFileItem != null && !selectedPartyName.equalsIgnoreCase("")) {
                    String errorMessage = processUploadedXml(dom, realFileItem, verificationCert, encryptionCert);
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
     *
     * @param cpaFile
     * @param verificationCert
     *@param encryptionCert @throws IOException
     * @throws DocumentException
     * @throws UtilitiesException
     * @throws ComponentException
     * @throws DAOException
     */
    private String processUploadedXml(PropertyTree dom, FileItem cpaFile, FileItem verificationCert, FileItem encryptionCert) throws IOException, DocumentException,
            UtilitiesException, ComponentException {
        try {
            InputStream uploadedStream = cpaFile.getInputStream();
            CollaborationProtocolAgreement cpa = parseCPA(uploadedStream);
            PartyInfo partyInfo = findMatchingPartyInfo(cpa,selectedPartyName);
            if(partyInfo == null) {
                throw new RuntimeException("There is no party name match in the cpa");
            }
            List<PartnershipDVO> partnerships = addPartnerships(cpa, partyInfo, verificationCert,encryptionCert);
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

    private List<PartnershipDVO> addPartnerships(CollaborationProtocolAgreement cpa, PartyInfo partyInfo, FileItem verificationCert, FileItem encryptionCert) throws Exception {
        List<PartnershipDVO> partnerships = new ArrayList<PartnershipDVO>();
        List<PartnershipDVO> rv = new ArrayList<PartnershipDVO>();
        PartnershipDAO partnershipDAO = (PartnershipDAO) EbmsProcessor.core.dao.createDAO(PartnershipDAO.class);

        for(CollaborationRole collaborationRole : partyInfo.getCollaborationRole()) {
            String serviceName = collaborationRole.getServiceBinding().getService().getValue();

            // create partnerships where the selected party is a sender
            for(CanSend canSend : collaborationRole.getServiceBinding().getCanSend()) {
                ActionBindingType senderActionBinding = canSend.getThisPartyActionBinding();
                ActionBindingType receiverActionBinding = canSend.getOtherPartyActionBinding();
                DeliveryChannel channel = receiverActionBinding.getChannel();
                partnerships.add( createPartnership(cpa, channel, senderActionBinding, receiverActionBinding, verificationCert, encryptionCert, partnerships, serviceName));
            }

            // create partnerships where the selected party is a receiver
            for(CanReceive canReceive: collaborationRole.getServiceBinding().getCanReceive()) {
                ActionBindingType receiverActionBinding = canReceive.getThisPartyActionBinding();
                ActionBindingType senderActionBinding = canReceive.getOtherPartyActionBinding();
                DeliveryChannel channel = senderActionBinding.getChannel();
                partnerships.add( createPartnership(cpa, channel, senderActionBinding, receiverActionBinding, verificationCert, encryptionCert, partnerships, serviceName));
            }
        }

        // insert partnerships not found in DB.
        for (PartnershipDVO dvo: partnerships) {
            if (!partnershipDAO.retrieve(dvo)) {
                EbmsProcessor.core.log.info("Adding Partnership " + dvo.getPartnershipId());
                partnershipDAO.create(dvo);
                rv.add(dvo);
            } else {
                EbmsProcessor.core.log.info("Partnership " + dvo.getPartnershipId() + " already exists");
            }
        }
        // only return partnerships that were newly created. These will be displayed to the end-user.
        return rv;
    }

    private PartnershipDVO createPartnership(CollaborationProtocolAgreement cpa, DeliveryChannel channel,ActionBindingType senderActionBinding, ActionBindingType receiverActionBinding, FileItem verificationCert, FileItem encryptionCert, List<PartnershipDVO> partnerships, String serviceName) throws DAOException, CertificateException, IOException {
        PartnershipDAO partnershipDAO = (PartnershipDAO) EbmsProcessor.core.dao.createDAO(PartnershipDAO.class);
        PartnershipDVO partnershipDVO = (PartnershipDVO) partnershipDAO.createDVO();

        String action = senderActionBinding.getAction();

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
        partnershipDVO.setSignRequested(String.valueOf(receiverActionBinding.getBusinessTransactionCharacteristics().isIsNonRepudiationRequired()));
        partnershipDVO.setEncryptRequested("false");
        if(channel.getDocExchange().getEbXMLSenderBinding().getSenderNonRepudiation() != null) {
            partnershipDVO.setDsAlgorithm(channel.getDocExchange().getEbXMLSenderBinding().getSenderNonRepudiation().getSignatureAlgorithm().get(0).getValue());
            partnershipDVO.setMdAlgorithm(channel.getDocExchange().getEbXMLSenderBinding().getSenderNonRepudiation().getHashFunction());
            if(verificationCert != null) {
                partnershipDVO.setSignCert(loadCert(verificationCert));
            } else {
                partnershipDVO.setSignCert(null);
            }
        }

        if(channel.getDocExchange().getEbXMLSenderBinding().getSenderDigitalEnvelope() != null) {
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
            if(encryptionCert != null) {
                partnershipDVO.setSignCert(loadCert(encryptionCert));
            } else {
                partnershipDVO.setEncryptCert(null);
            }
        }
        return partnershipDVO;
    }

    private void render(List<PartnershipDVO> partnerships, PropertyTree dom) {
        for(int i=0;i<partnerships.size();i++) {
            PartnershipDVO partnership = partnerships.get(i);
            // in XPATH the first child of a node is index 1
            String partnershipOffset = "partnership[" + (i+1) + "]";
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


    private byte [] loadCert(FileItem cert) throws CertificateException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOHandler.pipe(cert.getInputStream(), baos);
        //validate cert
        CertificateFactory
                .getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(baos.toByteArray()));
        return baos.toByteArray();
    }

    private String emptyStringIfNull(String s) {
        return s != null ? s : "";
    }

}