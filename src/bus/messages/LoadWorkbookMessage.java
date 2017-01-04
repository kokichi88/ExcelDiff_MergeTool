package bus.messages;

import bus.cmd.CmdId;
import bus.data.Message;
import excelprocessor.workbook.WorkbookWrapper;

/**
 * Created by apple on 1/4/17.
 */
public class LoadWorkbookMessage extends Message {
    public LoadWorkbookMessage(int index, String path, WorkbookWrapper[] workbooks) {
        cmdId = CmdId.LOAD_WORKBOOK;
        params.put("path", path);
        params.put("index", index);
        params.put("workbooks", workbooks);
    }
}
