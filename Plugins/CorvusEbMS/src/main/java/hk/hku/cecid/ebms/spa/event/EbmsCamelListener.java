package hk.hku.cecid.ebms.spa.event;

import hk.hku.cecid.ebms.pkg.EbxmlMessage;
import hk.hku.cecid.ebms.pkg.MessageHeader;
import hk.hku.cecid.ebms.spa.EbmsProcessor;
import hk.hku.cecid.ebms.spa.dao.MessageDAO;
import hk.hku.cecid.ebms.spa.dao.MessageDVO;
import hk.hku.cecid.ebms.spa.dao.MessageServerDAO;
import hk.hku.cecid.ebms.spa.handler.MessageClassifier;
import hk.hku.cecid.ebms.spa.task.EbmsEventListener;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.spi.Synchronization;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.*;

/**
 * Created by aaronwalker on 8/07/2016.
 */
public class EbmsCamelListener extends EbmsEventListener implements Closeable {

    protected static final String MSG_PROPERTY_MSG_ID = "ebxml_message_id";
    protected static final String MSG_PROPERTY_CPA_ID = "ebxml_cpa_id";
    protected static final String MSG_PROPERTY_SERVICE = "ebxml_service";
    protected static final String MSG_PROPERTY_SERVICE_TYPE = "ebxml_service_type";
    protected static final String MSG_PROPERTY_ACTION = "ebxml_action";
    protected static final String MSG_PROPERTY_CONV_ID = "ebxml_conv_id";
    protected static final String MSG_PROPERTY_FROM_PARTY_ID = "ebxml_from_party_id";
    protected static final String MSG_PROPERTY_FROM_PARTY_TYPE = "ebxml_from_party_type";
    protected static final String MSG_PROPERTY_TO_PARTY_ID = "ebxml_to_party_id";
    protected static final String MSG_PROPERTY_TO_PARTY_TYPE = "ebxml_to_party_type";
    protected static final String MSG_PROPERTY_TIMESTAMP = "ebxml_timestamp";
    protected static final String MSG_PROPERTY_TIMETOLIVE = "ebxml_timestolive";
    protected static final String MSG_PROPERTY_PAYLOAD_ID = "ebxml_payload_id";
    protected static final String MSG_PROPERTY_PAYLOAD_TYPE = "ebxml_payload_type";
    protected static final String MSG_PROPERTY_REF_MSG_ID = "ebxml_ref_message_id";

    private CamelContext camelContext;
    private ProducerTemplate eventHandler;

    @Override
    protected void init() throws Exception {
        super.init();
        camelContext = new DefaultCamelContext();
        camelContext.start();
        eventHandler = camelContext.createProducerTemplate();
    }

    @Override
    public void messageSent(EbxmlMessage requestMessage) {
        //publish sent event
        send(requestMessage, "vm:ebmsSent");
    }

    @Override
    public void messageReceived(EbxmlMessage requestMessage) {
        //we only want to support a single inbound delivery endpoint
        send(requestMessage, "direct-vm:ebmsReceived", "?failIfNoConsumers=false");
    }

    @Override
    public void responseReceived(EbxmlMessage acknowledgement) {
        //publish ack received event
        send(acknowledgement, "vm:ebmsAckReceived");
    }

    @Override
    public void errorOccurred(EbxmlMessage errorMessage) {
        send(errorMessage, "vm:ebmsErrors");
    }

    @Override
    public void close() throws IOException {
        if(camelContext != null) {
            try {
                camelContext.stop();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    private void send(EbxmlMessage ebxml, final String endpoint) {
        send(ebxml,endpoint,"?discardIfNoConsumers=true&multipleConsumers=true");
    }

    private void send(EbxmlMessage ebxml, final String endpoint, final  String options) {
        try {
            List<Exchange> exchanges = convertEbxmlMessage(ebxml);
            for(Exchange exchange: exchanges) {
                exchange.getIn().setHeader("ebmsEvent", endpoint);
                eventHandler.asyncCallback(endpoint + options, exchange, new Synchronization() {
                    @Override
                    public void onComplete(Exchange exchange) {
                        EbmsProcessor.core.log.info(endpoint + ":successfully sent:" + exchange.getIn().getHeader(MSG_PROPERTY_MSG_ID));
                        if("direct-vm:ebmsReceived".equals(exchange.getIn().getHeader("ebmsEvent"))) {
                            updateDeliveryStatus(exchange.getIn().getHeader(MSG_PROPERTY_MSG_ID,String.class),
                                    MessageClassifier.INTERNAL_STATUS_DELIVERED,
                                    "Message is delivered");
                        }
                    }

                    @Override
                    public void onFailure(Exchange exchange) {
                        EbmsProcessor.core.log.warn(endpoint + ":failed to send:" + exchange.getIn().getHeader(MSG_PROPERTY_MSG_ID));
                        if("direct-vm:ebmsReceived".equals(exchange.getIn().getHeader("ebmsEvent"))) {
                            updateDeliveryStatus(exchange.getIn().getHeader(MSG_PROPERTY_MSG_ID,String.class),
                                    MessageClassifier.INTERNAL_STATUS_DELIVERY_FAILURE,
                                    "Failed Delivery:" + exchange.getException());
                        }
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updateDeliveryStatus(String messageId, String status, String description) {
        try {
            MessageDAO messageDao = (MessageDAO) EbmsProcessor.core.dao.createDAO(MessageDAO.class);
            MessageDVO messageDvo = (MessageDVO) messageDao.createDVO();
            messageDvo.setMessageId(messageId);
            messageDvo.setMessageBox(MessageClassifier.MESSAGE_BOX_INBOX);
            if (messageDao.findMessage(messageDvo)) {
                if (messageDvo.getStatus().equals(MessageClassifier.INTERNAL_STATUS_PROCESSED)) {
                    MessageServerDAO messageServerDao = (MessageServerDAO) EbmsProcessor.core.dao.createDAO(MessageServerDAO.class);
                    messageDvo.setStatus(status);
                    messageDvo.setStatusDescription(description);
                    messageServerDao.clearMessage(messageDvo);
                }
            }
        } catch (Exception e) {
            EbmsProcessor.core.log.error("",e);
        }

    }

    private List<Exchange> convertEbxmlMessage(EbxmlMessage ebxml) throws Exception {

        List<Exchange> exchanges = new ArrayList<Exchange>();
        Map<String,Object> header = new HashMap<String, Object>();
        header.put(MSG_PROPERTY_MSG_ID, ebxml.getMessageId());
        header.put(MSG_PROPERTY_CPA_ID, ebxml.getCpaId());
        header.put(MSG_PROPERTY_SERVICE, ebxml.getService());
        header.put(MSG_PROPERTY_SERVICE_TYPE, ebxml.getServiceType());
        header.put(MSG_PROPERTY_ACTION, ebxml.getAction());
        header.put(MSG_PROPERTY_CONV_ID, ebxml.getConversationId());
        header.put(MSG_PROPERTY_REF_MSG_ID,ebxml.getMessageHeader().getRefToMessageId());
        if (ebxml.getFromPartyIds().hasNext()) {
            MessageHeader.PartyId partyId = (MessageHeader.PartyId) ebxml.getFromPartyIds().next();
            header.put(MSG_PROPERTY_FROM_PARTY_ID,partyId.getId());
            header.put(MSG_PROPERTY_FROM_PARTY_TYPE, partyId.getType());
        }
        if (ebxml.getToPartyIds().hasNext()) {
            MessageHeader.PartyId partyId = (MessageHeader.PartyId) ebxml.getFromPartyIds().next();
            header.put(MSG_PROPERTY_TO_PARTY_ID,partyId.getId());
            header.put(MSG_PROPERTY_TO_PARTY_TYPE, partyId.getType());
        }
        header.put(MSG_PROPERTY_TIMESTAMP,ebxml.getTimestamp());
        header.put(MSG_PROPERTY_TIMETOLIVE,ebxml.getTimeToLive());

        if(ebxml.getPayloadCount() > 0) {

            SOAPMessage msg = ebxml.getSOAPMessage();
            Iterator it = msg.getAttachments();
            while(it.hasNext()) {
                DefaultExchange exchange = new DefaultExchange(camelContext);
                exchange.getIn().setHeaders(header);
                AttachmentPart part = (AttachmentPart)it.next();
                exchange.getIn().setHeader(MSG_PROPERTY_PAYLOAD_ID,part.getContentId());
                exchange.getIn().setHeader(MSG_PROPERTY_PAYLOAD_TYPE,part.getContentType());
                exchange.getIn().setBody(part.getRawContentBytes());
                exchanges.add(exchange);
            }
        } else {
            DefaultExchange exchange = new DefaultExchange(camelContext);
            exchange.setPattern(ExchangePattern.InOut);
            exchange.getIn().setHeaders(header);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ebxml.writeTo(bos);
            exchange.getIn().setBody(bos.toByteArray());
            exchanges.add(exchange);
        }
        return exchanges;
    }
}
