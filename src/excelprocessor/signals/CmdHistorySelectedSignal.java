package excelprocessor.signals;

import bus.data.ISignal;
import controller.MainController;

/**
 * Created by apple on 1/18/17.
 */
public class CmdHistorySelectedSignal implements ISignal {
    public MainController controller;
    public int sheet;
    public int oldRow;
    public int newRow;

    public  CmdHistorySelectedSignal(MainController controller, int sheet, int oldRow, int newRow) {
        this.controller = controller;
        this.sheet = sheet;
        this.oldRow = oldRow;
        this.newRow = newRow;
    }
}
