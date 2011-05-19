/* 
 * Copyright(c) 2005 Center for E-Commerce Infrastructure Development, The
 * University of Hong Kong (HKU). All Rights Reserved.
 *
 * This software is licensed under the GNU GENERAL PUBLIC LICENSE Version 2.0 [1]
 * 
 * [1] http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt
 */

package hk.hku.cecid.piazza.commons.soap;

import hk.hku.cecid.piazza.commons.io.IOHandler;
import hk.hku.cecid.piazza.commons.net.ConnectionException;
import hk.hku.cecid.piazza.commons.net.HttpConnector;
import hk.hku.cecid.piazza.commons.util.Headers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * SOAPHttpConnector is an HTTP connector for making HTTP SOAP connections to an
 * endpoint.
 * 
 * @author Hugo Y. K. Lam
 *  
 */
public class SOAPHttpConnector extends HttpConnector {

    private static final Logger LOG = LoggerFactory.getLogger(SOAPHttpConnector.class);
    /**
     * Creates a new instance of SOAPHttpConnector.
     * 
     * @param endpoint the end point, either in String or URL format.
     * @throws MalformedURLException if the end point is malformed.
     */
    public SOAPHttpConnector(Object endpoint) throws MalformedURLException {
        super(endpoint);
    }
    
    /**
     * Sends an HTTP SOAP request.
     * 
     * @param request the SOAP request message.
     * @return the SOAP reply message responsed from the host. 
     * @throws ConnectionException if failed in sending the HTTP SOAP request or 
     *          creating a new connection.
     */
    public SOAPMessage send(SOAPMessage request) throws ConnectionException {
        return send(request, createConnection());
    }
    
    /**
     * Sends an HTTP SOAP request using the given HTTP connection.
     * 
     * @param request the SOAP request message.
     * @param connection the HTTP connection for sending the request.
     * @return the SOAP reply message responsed from the host. 
     * @throws ConnectionException if failed in sending the HTTP SOAP request.
     */
    public SOAPMessage send(SOAPMessage request, HttpURLConnection connection) throws ConnectionException {
        OutputStream outstream = null;
        try {
            Headers headers = new Headers(connection);
            
            ByteArrayOutputStream soapStream = new ByteArrayOutputStream();
            request.writeTo(soapStream);
            byte[] soapBytes = soapStream.toByteArray();
            soapStream.close();
            
            headers.putMimeHeaders(request.getMimeHeaders());

            InputStream instream;
            
            try {
                connection.setDoOutput(true);
                outstream = connection.getOutputStream();
                IOHandler.writeBytes(soapBytes, outstream);
                connection.connect();
                instream = connection.getInputStream();
            }
            catch (Exception e) {
            	// no guarantee server is connected or useful data is sent through error stream
            	/*
                if (connection.getResponseCode() >= 400) {
                    instream = connection.getErrorStream();
                }
                else throw e;
                */
                throw e;
            }

            MimeHeaders responseHeaders = headers.getMimeHeaders();
            MessageFactory msgFactory = MessageFactory.newInstance();
            byte[] responseBytes = IOHandler.readBytes(instream);
            int responseCode = connection.getResponseCode();
            if (responseBytes.length > 0 && responseCode != HttpServletResponse.SC_NO_CONTENT) {
                instream = new ByteArrayInputStream(responseBytes);
                try {
                    //This assumes that any response returned should be a SOAP response
                    //What if it isn't?
                    return msgFactory.createMessage(responseHeaders, instream);
                }
                catch(SOAPException ex) {
                    //TODO: Should probably throw a custom exception to communicate failure to parse response
                    LOG.error("HTTP Response code: {} - Unable to parse response: {}",responseCode,ex.getMessage());
                    LOG.debug("",ex);
                }
            }
            return null;
        }
        catch (Exception e) {
            throw new ConnectionException("Unable to send HTTP SOAP request", e);
        }
        finally {
            try {
                if (outstream != null) {
                    outstream.close();
                }
            } catch (Exception e) {
            }
        }        
    }
}
