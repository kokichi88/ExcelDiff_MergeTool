package excelprocessor.signals;

import bus.data.ISignal;
import javafx.scene.control.TableView;

/**
 * Created by apple on 1/13/17.
 */
public class UpdateCmdHistorySignal implements ISignal {
    public TableView tableView;

    public UpdateCmdHistorySignal(TableView tableView) {
        this.tableView = tableView;
    }
}
