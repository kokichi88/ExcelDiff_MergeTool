package excelprocessor.cmd;

import bus.controller.BusManager;
import bus.controller.ICommand;
import excelprocessor.signals.LoadWorkbookSignal;
import excelprocessor.signals.PushLogSignal;
import controller.MainController;
import excelprocessor.workbook.WorkbookWrapper;
import javafx.concurrent.Task;
import services.Services;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by apple on 1/4/17.
 */
public class LoadWorkbookCommand implements ICommand<LoadWorkbookSignal> {

    @Override
    public void execute(LoadWorkbookSignal signal) throws Exception {
        final String path = signal.path;
        final int index = signal.index;
        final MainController controller = signal.controller;
        Task<Void> loadFile = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                BusManager busManager = Services.getService(BusManager.class);
                busManager.dispatch(new PushLogSignal("Start loading file " + path));
                WorkbookWrapper wb = new WorkbookWrapper(path);
                controller.setWorkbooks(index, wb);
                busManager.dispatch(new PushLogSignal("File " + path + " is loaded successfully"));
                return null;
            }
        };
        Services.getService(ScheduledThreadPoolExecutor.class).submit(loadFile);
    }
}
