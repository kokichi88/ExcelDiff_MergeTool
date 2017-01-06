package bus.controller;

import bus.data.ISignal;

/**
 * Created by apple on 1/4/17.
 */
public interface ICommand<S extends ISignal> {
    public void execute(S signal) throws Exception;

}
