package excelprocessor.signals;

import bus.data.ISignal;
import excelprocessor.workbook.WorkbookWrapper;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;

/**
 * Created by apple on 1/4/17.
 */
public class ChangeTabSignal implements ISignal {
    public WorkbookWrapper[] workbookWrappers;
    public TableView[] tableViews;
    public Tab newValue;
    public Tab oldValue;

    public ChangeTabSignal(WorkbookWrapper[] workbookWrappers, TableView[] tableViews, Tab newValue, Tab oldValue) {
        this.workbookWrappers = workbookWrappers;
        this.tableViews = tableViews;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }
}
