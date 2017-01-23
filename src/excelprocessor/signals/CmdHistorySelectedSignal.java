package excelprocessor.signals;

import bus.data.ISignal;
import controller.MainController;
import data.CmdHistoryElement;

/**
 * Created by apple on 1/18/17.
 */
public class CmdHistorySelectedSignal implements ISignal {
    public MainController controller;
    public int sheet;
    public int oldRow;
    public int newRow;
    public int oldCol;
    public int newCol;

    public  CmdHistorySelectedSignal(MainController controller, CmdHistoryElement element) {
        this.controller = controller;
        this.sheet = element.getSheetid();
        this.oldRow = element.getOldRow() - 1;
        this.newRow = element.getNewRow() - 1;
        this.oldCol = element.getOldCol();
        this.newCol = element.getNewCol();
    }
}
