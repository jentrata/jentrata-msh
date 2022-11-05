package hk.hku.cecid.ebms.spa.client.jms;

import hk.hku.cecid.ebms.pkg.EbxmlMessage;
import hk.hku.cecid.ebms.pkg.MessageHeader.PartyId;
import hk.hku.cecid.ebms.spa.EbmsProcessor;
import hk.hku.cecid.ebms.spa.task.EbmsEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;


public class MessageListenerJMSClient extends EbmsEventListener {

	private Connection connection;

	private Session session;

	private MessageProducer producer;

	private ConnectionFactory connectionFactory;

	private Queue queue;

	protected static final String MSG_PROPERTY_CPA_ID = "ebxml_cpa_id";
	protected static final String MSG_PROPERTY_SERVICE = "ebxml_service";
	protected static final String MSG_PROPERTY_SERVICE_TYPE = "ebxml_service_type";
	protected static final String MSG_PROPERTY_ACTION = "ebxml_action";
	protected static final String MSG_PROPERTY_CONV_ID = "ebxml_conv_id";
	protected static final String MSG_PROPERTY_PARTY_ID = "ebxml_party_id";
	protected static final String MSG_PROPERTY_PARTY_TYPE = "ebxml_party_type";

	@Override
	public void init() {
		connectionFactory = buildConnectionFactory();
	}

	@Override
	public void errorOccurred(EbxmlMessage errorMessage) {

	}

	@Override
	public void messageReceived(EbxmlMessage requestMessage) {
		try {
			initialiseJMSConnection();
			sendMessageToQueue(requestMessage);
			closeSession();
		} catch (JMSException e) {
			EbmsProcessor.core.log.error(e);
			connection = null;
		}
	}

	private void reconnect() throws JMSException {
		if (connection == null) {
			connection = connectionFactory.createConnection();
			connection.setExceptionListener(new JMSExceptionListener(connection));
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
		TextMessage textMessage = session.createTextMessage();

		textMessage.setStringProperty(MSG_PROPERTY_CPA_ID,
				requestMessage.getCpaId());
		textMessage.setStringProperty(MSG_PROPERTY_SERVICE,
				requestMessage.getService());
		textMessage.setStringProperty(MSG_PROPERTY_ACTION,
				requestMessage.getAction());
		textMessage.setStringProperty(MSG_PROPERTY_CONV_ID,
				requestMessage.getConversationId());
		if (requestMessage.getFromPartyIds().hasNext()) {
			PartyId partyId = (PartyId) requestMessage.getFromPartyIds().next();
			textMessage.setStringProperty(MSG_PROPERTY_PARTY_ID,
					partyId.getId());
			textMessage.setStringProperty(MSG_PROPERTY_PARTY_TYPE,
					partyId.getType());
		}
		textMessage.setStringProperty(MSG_PROPERTY_SERVICE_TYPE,
				requestMessage.getServiceType());

		SOAPMessage msg = requestMessage.getSOAPMessage();
		Iterator it = msg.getAttachments();

		while (it.hasNext()) {
			Object o = it.next();
			AttachmentPart attachment = (AttachmentPart) o;

			try {
				textMessage.setText(convertStreamToString(attachment.getRawContent()));
			} catch (SOAPException e) {
				EbmsProcessor.core.log.error("SOAP exception", e);
			}
			catch (IOException e) {
				EbmsProcessor.core.log.error("IO exception", e);
			}

		}
		producer.send(textMessage);
	}

	private String convertStreamToString(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	private void closeSession() throws JMSException {
		producer.close();
		session.close();
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

	protected ConnectionFactory buildConnectionFactory() {
		try {
			String connectionFactoryUrl = getConnectionUrl();
			Class connectionFactoryClass = getConnectionFactoryClass();
			ConnectionFactory cf = (ConnectionFactory)connectionFactoryClass.getConstructor(String.class).newInstance(new Object[]{connectionFactoryUrl});
			return cf;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected Class getConnectionFactoryClass() throws ClassNotFoundException {
		return Class.forName(getConnectionFactoryClassName());
	}

	protected String getConnectionFactoryClassName() {
		return getParameters().getProperty("connectionFactory");
	}

	protected String getConnectionUrl() {
		return getParameters().getProperty("connectionUrl");
	}

	protected String getUsername() {
		return getParameters().getProperty("username");	
	}

	protected String getPassword() {
		return getParameters().getProperty("password");	
	}
}
