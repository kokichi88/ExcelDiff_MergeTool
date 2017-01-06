package bus.controller;


import bus.data.EmptySignal;

/**
 * Created by apple on 1/4/17.
 */
public class DoNothingCommand implements ICommand<EmptySignal> {
    @Override
    public void execute(EmptySignal signal) {
        // test purpose
    }
}
