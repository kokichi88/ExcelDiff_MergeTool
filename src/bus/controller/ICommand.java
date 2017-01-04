package bus.controller;

import bus.data.Message;

/**
 * Created by apple on 1/4/17.
 */
public interface ICommand {
    public void execute(Message message);
}
