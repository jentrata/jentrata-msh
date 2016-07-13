package hk.hku.cecid.ebms.spa.event;

import hk.hku.cecid.ebms.pkg.EbxmlMessage;
import hk.hku.cecid.ebms.pkg.MessageHeader;
import hk.hku.cecid.ebms.spa.EbmsProcessor;
import hk.hku.cecid.ebms.spa.task.EbmsEventListener;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.spi.Synchronization;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPMessage;
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

    private CamelContext camelContext;
    private ProducerTemplate eventHandler;
    private String endpointOptions = null;

    @Override
    protected void init() throws Exception {
        super.init();
        camelContext = new DefaultCamelContext();
        eventHandler = camelContext.createProducerTemplate();
        endpointOptions = getEndpointOptions();
    }

    @Override
    public void messageSent(EbxmlMessage requestMessage) {
        send(requestMessage, "vm:ebmsSent");
    }

    @Override
    public void messageReceived(EbxmlMessage requestMessage) {
        send(requestMessage, "vm:ebmsReceived");
    }

    @Override
    public void responseReceived(EbxmlMessage acknowledgement) {
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
        try {
            List<Exchange> exchanges = convertEbxmlMessage(ebxml);
            for(Exchange exchange: exchanges) {
                eventHandler.asyncCallback(endpoint + endpointOptions, exchange, new Synchronization() {
                    @Override
                    public void onComplete(Exchange exchange) {
                        EbmsProcessor.core.log.info(endpoint + ":successfully sent:" + exchange.getIn().getHeader(MSG_PROPERTY_MSG_ID));
                    }

                    @Override
                    public void onFailure(Exchange exchange) {
                        EbmsProcessor.core.log.warn(endpoint + ":failed to send:" + exchange.getIn().getHeader(MSG_PROPERTY_MSG_ID));
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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
            exchange.getIn().setHeaders(header);
            exchanges.add(exchange);
        }
        return exchanges;
    }

    private String getEndpointOptions() {
        String options = getParameters().getProperty("endpointOptions","?discardIfNoConsumers=true&multipleConsumers=true");
        if(options.startsWith("?")) {
            return options;
        }
        return "?" + options;
    }
}
