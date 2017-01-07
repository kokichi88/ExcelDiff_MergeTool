package excelprocessor.cmd;

import bus.controller.ICommand;
import excelprocessor.signals.UpdateTabPaneSignal;

/**
 * Created by apple on 1/6/17.
 */
public class UpdateTabPaneCmd implements ICommand<UpdateTabPaneSignal> {

    @Override
    public void execute(final UpdateTabPaneSignal signal) throws Exception {
        signal.controller.updateTabPane(signal.index, signal.wb);
    }
}
