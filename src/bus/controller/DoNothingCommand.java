package bus.controller;

import bus.data.Message;

/**
 * Created by apple on 1/4/17.
 */
public class DoNothingCommand implements ICommand {
    @Override
    public void execute(Message message) {
        // test purpose
    }
}
