package excelprocessor.signals;

import bus.data.ISignal;
import controller.MainController;
import excelprocessor.workbook.WorkbookWrapper;

/**
 * Created by apple on 1/6/17.
 */
public class UpdateTabPaneSignal implements ISignal {
    public int index;
    public WorkbookWrapper wb;
    public MainController controller;

    public UpdateTabPaneSignal(MainController controller, int index, WorkbookWrapper wb) {
        this.index = index;
        this.wb = wb;
        this.controller = controller;
    }
}
