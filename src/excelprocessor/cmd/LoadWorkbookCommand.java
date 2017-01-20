package excelprocessor.cmd;

import bus.controller.BusManager;
import bus.controller.ICommand;
import excelprocessor.signals.ChangeTabSignal;
import excelprocessor.signals.LoadWorkbookSignal;
import excelprocessor.signals.PushLogSignal;
import controller.MainController;
import excelprocessor.signals.UpdateTabPaneSignal;
import excelprocessor.workbook.WorkbookWrapper;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
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
                controller.setWorkbooks(index, wb);
                busManager.dispatch(new PushLogSignal("File " + path + " is loaded"));
                busManager.dispatch(new UpdateTabPaneSignal(controller, index, wb));

                TabPane tabPane = controller.getTabPane(index);
                int sheet = controller.getDefaultSheetIndexOf(index);
                TableView tableView = controller.getTableView(index);
                if(sheet > -1) {
                    Tab tab = tabPane.getTabs().get(sheet);
                    ChangeTabSignal msg = new ChangeTabSignal(wb, tableView, tab, tab, sheet);
                    Services.get(BusManager.class).dispatch(msg);
                }
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
                    Thread.sleep(id * 500);
                    busManager.dispatch(new PushLogSignal("Start loading file " + path));
                    WorkbookWrapper wb = new WorkbookWrapper(path, id);
                    return wb;
                }
            };
        }
    }
}

