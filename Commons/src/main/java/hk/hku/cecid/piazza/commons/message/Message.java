package hk.hku.cecid.piazza.commons.message;

import java.util.List;
import java.util.Map;

public interface Message {

    Map<String,Object> getHeader();
    List<byte[]> getPayloads();
    public Object getSource();
}
