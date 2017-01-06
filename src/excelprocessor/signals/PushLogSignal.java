package excelprocessor.signals;


import bus.data.ISignal;

/**
 * Created by apple on 1/4/17.
 */
public class PushLogSignal implements ISignal {
    public String content;

    public PushLogSignal(String content) {
        this.content = content;
    }
}
