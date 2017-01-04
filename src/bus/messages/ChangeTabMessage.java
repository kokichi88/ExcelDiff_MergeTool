package bus.messages;

import bus.cmd.CmdId;
import bus.data.Message;
import excelprocessor.workbook.WorkbookWrapper;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;

import java.util.Map;

/**
 * Created by apple on 1/4/17.
 */
public class ChangeTabMessage extends Message {

    public ChangeTabMessage(WorkbookWrapper[] workbookWrappers, TabPane[] tabPanes, TableView[] tableViews, Tab newValue, Tab oldValue) {
        cmdId = CmdId.CHANGE_TAB;
        params.put("workbooks", workbookWrappers);
        params.put("tabPanes", tabPanes);
        params.put("tableViews", tableViews);
        params.put("newValue", newValue);
        params.put("oldValue", oldValue);
    }
}
