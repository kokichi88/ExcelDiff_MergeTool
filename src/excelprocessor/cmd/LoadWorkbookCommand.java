package excelprocessor.cmd;

import bus.controller.BusManager;
import bus.controller.ICommand;
import excelprocessor.signals.LoadWorkbookSignal;
import excelprocessor.signals.PushLogSignal;
import controller.MainController;
import excelprocessor.signals.UpdateTabPaneSignal;
import excelprocessor.workbook.WorkbookWrapper;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import services.Services;

/**
 * Created by apple on 1/4/17.
 */
public class LoadWorkbookCommand implements ICommand<LoadWorkbookSignal> {

    @Override
    public void execute(LoadWorkbookSignal signal) throws Exception {
        final String path = signal.path;
        final int index = signal.index;
        final MainController controller = signal.controller;
        OpenWorkbookService service = new OpenWorkbookService(path, index);
        final BusManager busManager = Services.get(BusManager.class);
        service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                WorkbookWrapper wb = (WorkbookWrapper)event.getSource().getValue();
                busManager.dispatch(new PushLogSignal("File " + path + " is loaded"));
                busManager.dispatch(new UpdateTabPaneSignal(controller, index, wb));
                controller.setWorkbooks(index, wb);
                controller.setFileLableText(index, path);
                controller.increaseLoadedWorkbook();
            }
        });

        service.start();
    }

    public static class OpenWorkbookService extends Service<WorkbookWrapper> {
        private String path;
        private int id;

        public OpenWorkbookService(String path, int id) {
            this.path = path;
            this.id = id;
        }

        @Override
        protected Task<WorkbookWrapper> createTask() {
            return new Task<WorkbookWrapper>() {
                @Override
                protected WorkbookWrapper call() throws Exception {
                    final BusManager busManager = Services.get(BusManager.class);
                    Thread.sleep(id * 100);
                    busManager.dispatch(new PushLogSignal("Start loading file " + path));
                    WorkbookWrapper wb = new WorkbookWrapper(path, id);
                    return wb;
                }
            };
        }
    }
}

