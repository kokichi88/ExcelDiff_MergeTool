package excelprocessor.cmd;

import bus.controller.BusManager;
import bus.controller.ICommand;
import controller.MainController;
import excelprocessor.signals.ChangeTabSignal;
import excelprocessor.signals.CmdHistorySelectedSignal;
import excelprocessor.workbook.WorkbookWrapper;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import services.Services;

/**
 * Created by apple on 1/18/17.
 */
public class FocusRowCommand implements ICommand<CmdHistorySelectedSignal> {
    @Override
    public void execute(CmdHistorySelectedSignal signal) throws Exception {
        MainController controller = signal.controller;
        for(int index = 0; index < MainController.MAX_FILE; ++index) {
            int curSheet = controller.getCurrentSelectedSheet(index);
            if(signal.sheet != curSheet) {
                TabPane tabpane = controller.getTabPane(index);
                tabpane.getSelectionModel().select(signal.sheet);
            }
        }

        controller.scrollTableViewTo(MainController.OLD_FILE_INDEX, signal.oldRow, signal.oldCol);
        controller.scrollTableViewTo(MainController.NEW_FILE_INDEX, signal.newRow, signal.newCol);
    }
}
