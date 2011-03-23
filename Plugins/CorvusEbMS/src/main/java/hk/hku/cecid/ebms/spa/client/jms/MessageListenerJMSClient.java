package hk.hku.cecid.ebms.spa.client.jms;

import hk.hku.cecid.ebms.pkg.EbxmlMessage;
import hk.hku.cecid.ebms.pkg.MessageHeader.PartyId;
import hk.hku.cecid.ebms.spa.EbmsProcessor;
import hk.hku.cecid.ebms.spa.task.EbmsEventListener;

import java.util.Iterator;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class MessageListenerJMSClient extends EbmsEventListener {

	private Connection connection;

	private Session session;

	private MessageProducer producer;

	private ConnectionFactory connectionFactory;

	private Queue queue;
	
	protected static final String MSG_PROPERTY_CPA_ID = "cpa_id";
	protected static final String MSG_PROPERTY_SERVICE = "service";
	protected static final String MSG_PROPERTY_SERVICE_TYPE = "service_type";
	protected static final String MSG_PROPERTY_ACTION = "action";
	protected static final String MSG_PROPERTY_CONV_ID = "conv_id";
	protected static final String MSG_PROPERTY_PARTY_ID = "party_id";
	protected static final String MSG_PROPERTY_PARTY_TYPE = "party_type";

	@Override
	public void init() {
		connectionFactory = new ActiveMQConnectionFactory(getConnectionUrl());
	}

	@Override
	public void errorOccurred(EbxmlMessage errorMessage) {

	}

	@Override
	public void messageReceived(EbxmlMessage requestMessage) {
		try {
			initialiseJMSConnection();
			sendMessageToQueue(requestMessage);
			closeJMSConnection();
		} catch (JMSException e) {
			EbmsProcessor.core.log.error(e);
		}
	}

	private void reconnect() throws JMSException {
		if (connection == null) {
			connection = connectionFactory.createConnection();
			connection.start();
		}
	}

	private void initialiseJMSConnection() throws JMSException {
		reconnect();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		queue = session.createQueue(getQueueName());
		producer = session.createProducer(queue);
	}

	private void sendMessageToQueue(EbxmlMessage requestMessage)
			throws JMSException {
		BytesMessage bytesMessage = session.createBytesMessage();
		
		bytesMessage.setStringProperty(MSG_PROPERTY_CPA_ID, requestMessage.getCpaId());
		bytesMessage.setStringProperty(MSG_PROPERTY_SERVICE, requestMessage.getService());
		bytesMessage.setStringProperty(MSG_PROPERTY_ACTION, requestMessage.getAction());
		bytesMessage.setStringProperty(MSG_PROPERTY_CONV_ID, requestMessage.getConversationId());
		if(requestMessage.getFromPartyIds().hasNext()){
			PartyId partyId = (PartyId) requestMessage.getFromPartyIds().next();
			bytesMessage.setStringProperty(MSG_PROPERTY_PARTY_ID, partyId.getId());
			bytesMessage.setStringProperty(MSG_PROPERTY_PARTY_TYPE, partyId.getType());
		}
		bytesMessage.setStringProperty(MSG_PROPERTY_SERVICE_TYPE, requestMessage.getServiceType());
		

		SOAPMessage msg = requestMessage.getSOAPMessage();
		Iterator it = msg.getAttachments();

		while (it.hasNext()) {
			Object o = it.next();
			AttachmentPart attachment = (AttachmentPart) o;

			try {
				bytesMessage.writeBytes(attachment.getRawContentBytes());
			} catch (SOAPException e) {
				EbmsProcessor.core.log.error("SOAP exception", e);
			}

		}
		producer.send(bytesMessage);
	}

	private void closeJMSConnection() throws JMSException {
		producer.close();
		session.close();
		connection.close();
	}

	@Override
	public void messageSent(EbxmlMessage requestMessage) {

	}

	@Override
	public void responseReceived(EbxmlMessage acknowledgement) {

	}

	public String getQueueName() {
		return getParameters().getProperty("queueName");
	}

	protected String getConnectionUrl() {
		return getParameters().getProperty("connectionUrl");
	}
}
