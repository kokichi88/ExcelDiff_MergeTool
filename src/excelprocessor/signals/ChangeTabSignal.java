package excelprocessor.signals;

import bus.data.ISignal;
import excelprocessor.workbook.WorkbookWrapper;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;

/**
 * Created by apple on 1/4/17.
 */
public class ChangeTabSignal implements ISignal {
    public WorkbookWrapper wb;
    public TableView tableView;
    public Tab newValue;
    public Tab oldValue;
    public int sheet;

    public ChangeTabSignal(WorkbookWrapper wb, TableView tableView, Tab newValue, Tab oldValue, int sheet) {
        this.wb = wb;
        this.tableView = tableView;
        this.newValue = newValue;
        this.oldValue = oldValue;
        this.sheet = sheet;
    }
}
