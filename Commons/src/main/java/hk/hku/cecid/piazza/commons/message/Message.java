package hk.hku.cecid.piazza.commons.message;

import java.util.List;
import java.util.Map;

public interface Message {
    
    public static final String [] VALID_HEADER_KEYS = {"cpaId","service","serviceType",
                                            "action","conversationId","fromPartyId",
                                            "fromPartyType","toPartyId","toPartyType",
                                            "timeToLiveOffset","payload-contentId","payload-contentType"};

    Map<String,Object> getHeader();
    List<byte[]> getPayloads();
    public Object getSource();
}
