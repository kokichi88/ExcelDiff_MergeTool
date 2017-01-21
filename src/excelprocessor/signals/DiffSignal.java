package excelprocessor.signals;

import bus.data.ISignal;
import controller.MainController;

/**
 * Created by apple on 1/9/17.
 */
public class DiffSignal implements ISignal {

    public MainController controller;

    public DiffSignal(MainController controller) {
        this.controller = controller;
    }
}
