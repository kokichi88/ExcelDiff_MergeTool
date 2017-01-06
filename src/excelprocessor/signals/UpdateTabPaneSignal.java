package excelprocessor.signals;

import bus.data.ISignal;
import excelprocessor.workbook.WorkbookWrapper;

/**
 * Created by apple on 1/6/17.
 */
public class UpdateTabPaneSignal implements ISignal {
    public int index;
    public WorkbookWrapper wb;

    public UpdateTabPaneSignal(int index, WorkbookWrapper wb) {
        this.index = index;
        this.wb = wb;
    }
}
