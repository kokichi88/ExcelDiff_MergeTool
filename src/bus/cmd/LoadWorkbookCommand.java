package bus.cmd;

import bus.controller.ICommand;
import bus.data.Message;
import excelprocessor.workbook.WorkbookWrapper;
import javafx.concurrent.Task;
import services.Services;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by apple on 1/4/17.
 */
public class LoadWorkbookCommand implements ICommand {
    @Override
    public void execute(Message message) {
        final String path = (String)message.params.get("path");
        final int index = (Integer)message.params.get("index");
        final WorkbookWrapper[] workbooks = (WorkbookWrapper[])message.params.get("workbooks");
        Task<Void> loadFile = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                workbooks[index] = new WorkbookWrapper(path);
                return null;
            }
        };

        Services.getService(ScheduledThreadPoolExecutor.class).execute(loadFile);
    }
}
