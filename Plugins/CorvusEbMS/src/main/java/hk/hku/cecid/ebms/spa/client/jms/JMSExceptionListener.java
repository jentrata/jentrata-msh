package hk.hku.cecid.ebms.spa.client.jms;

import hk.hku.cecid.ebms.spa.EbmsProcessor;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;

public class JMSExceptionListener implements ExceptionListener {
	private Connection jmsConnection;

	public JMSExceptionListener(Connection jmsConnecion) {
		this.jmsConnection = jmsConnection;
	}

	public void onException(JMSException e) {
		try {
			EbmsProcessor.core.log.warn(e);
			if (jmsConnection != null) {
				jmsConnection.stop();
				jmsConnection.start();
			}
		} catch (JMSException e1) {
			EbmsProcessor.core.log.error(e);
			e1.printStackTrace();
		}
	}

}