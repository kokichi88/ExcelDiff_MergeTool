package excelprocessor.cmd;

import bus.controller.ICommand;
import controller.MainController;
import excelprocessor.signals.CmdHistorySelectedSignal;

/**
 * Created by apple on 1/18/17.
 */
public class FocusRowCommand implements ICommand<CmdHistorySelectedSignal> {
    @Override
    public void execute(CmdHistorySelectedSignal signal) throws Exception {
        MainController controller = signal.controller;
        controller.scrollTableViewTo(MainController.OLD_FILE_INDEX, signal.oldRow);
        controller.scrollTableViewTo(MainController.NEW_FILE_INDEX, signal.newRow);
    }
}
