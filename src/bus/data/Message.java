package bus.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by apple on 1/4/17.
 */
public class Message {
    public int cmdId;
    public Map<Object,Object> params = new HashMap<Object, Object>();

    public Message(int cmdId, Map<Object, Object> params) {
        this.cmdId = cmdId;
        this.params = params;
    }

    public Message() {
        cmdId = -1;
    }
}
