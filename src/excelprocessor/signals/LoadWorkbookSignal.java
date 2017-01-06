package excelprocessor.signals;

import bus.data.ISignal;
import controller.MainController;

/**
 * Created by apple on 1/4/17.
 */
public class LoadWorkbookSignal implements ISignal {
    public int index;
    public String path;
    public MainController controller;

    public LoadWorkbookSignal(int index, String path, MainController controller) {
        this.index = index;
        this.path = path;
        this.controller = controller;
    }
}
