package hk.hku.cecid.ebms.spa.handler.jms;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import hk.hku.cecid.ebms.pkg.EbxmlMessage;
import hk.hku.cecid.ebms.pkg.MessageHeader.PartyId;
import hk.hku.cecid.ebms.spa.handler.MessageServiceHandler;
import hk.hku.cecid.ebms.spa.listener.EbmsRequest;
import hk.hku.cecid.ebms.spa.listener.EbmsResponse;
import hk.hku.cecid.piazza.commons.message.Message;
import hk.hku.cecid.piazza.commons.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class EbmsMessageHandlerTest {

    EbmsMessageHandler mh = null;
    MessageServiceHandler msh = null;
    
    @Before
    public void setup() throws Exception {
        mh = spy(new EbmsMessageHandler());
        doReturn(mock(Logger.class)).when(mh).log();
        doReturn(true).when(mh).checkValidChannel(anyString(), anyString(), anyString());
        msh = mock(MessageServiceHandler.class);
        doReturn(msh).when(mh).getMSH();
    }
    
    
    @Test
    public void testEbmsMessageHandlerValidMessage() throws Exception {
        Message msg = buildMessage("TestMessage");
        
        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object arg = invocation.getArguments()[0];
                assertThat(arg, instanceOf(EbmsRequest.class));
                EbmsRequest request = (EbmsRequest) invocation.getArguments()[0];
                assertThat(request, notNullValue());

                //Check if the ebxml message is valid
                EbxmlMessage ebxml = request.getMessage();
                assertThat(ebxml, notNullValue());
                assertThat(ebxml.getCpaId(),equalTo("cpaId"));
                assertThat(ebxml.getService(),equalTo("http://localhost/inbound"));
                assertThat(ebxml.getAction(),equalTo("action"));
                assertThat(ebxml.getConversationId(),equalTo("conversationId"));
                assertThat(first(ebxml.getFromPartyIds()),equalTo("test_a"));
                assertThat(first(ebxml.getToPartyIds()),equalTo("test_b"));
                assertThat(ebxml.getTimeToLive(),notNullValue());
                
                //Check the payload.
                assertThat(ebxml.getPayloadCount(),equalTo(1));
                assertThat(ebxml.getPayloadContainer("Payload-0").getContentType(),equalTo("text/xml; charset=UTF-8"));
                return null;
            }
        }).when(msh).processOutboundMessage((EbmsRequest) any(), (EbmsResponse) isNull());
        
        mh.onMessage(msg);
        
        verify(msh).processOutboundMessage((EbmsRequest) any(), (EbmsResponse) isNull());
    }
    
    public Message buildMessage(String data) {
        Message msg = mock(Message.class);
        
        Map<String,Object> headers = new HashMap<String, Object>();
        headers.put("cpaId","cpaId");
        headers.put("service","http://localhost/inbound");
        headers.put("action","action");
        headers.put("conversationId","conversationId");
        headers.put("fromPartyId","test_a");
        headers.put("fromPartyType","");
        headers.put("toPartyId","test_b");
        headers.put("toPartyType","");
        headers.put("timeToLiveOffset","10800");
        
        
        List<byte[]> payloads = new ArrayList<byte[]>();
        payloads.add("testmessage".getBytes());
        
        doReturn("").when(msg).getSource();
        doReturn(headers).when(msg).getHeader();
        doReturn(payloads).when(msg).getPayloads();
        
        return msg;
    }
    
    private String first(Iterator<?> iter) {
        Object o = iter.next();
        if(o != null) {
            PartyId partyId = (PartyId)o;
            return partyId.getId();
        }
        return null;
    }
    

}
