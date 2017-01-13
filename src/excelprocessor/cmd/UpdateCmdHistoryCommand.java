package excelprocessor.cmd;


import bus.controller.ICommand;
import excelprocessor.signals.UpdateCmdHistorySignal;
import javafx.scene.control.TableView;

/**
 * Created by apple on 1/13/17.
 */
public class UpdateCmdHistoryCommand implements ICommand<UpdateCmdHistorySignal> {

    @Override
    public void execute(UpdateCmdHistorySignal signal) throws Exception {
        TableView tableView = signal.tableView;


    }
}
