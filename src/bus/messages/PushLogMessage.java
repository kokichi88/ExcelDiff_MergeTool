package bus.messages;

import bus.data.Message;

/**
 * Created by apple on 1/4/17.
 */
public class PushLogMessage extends Message {

    public PushLogMessage(String content) {
        params.put("content", content);
    }
}
