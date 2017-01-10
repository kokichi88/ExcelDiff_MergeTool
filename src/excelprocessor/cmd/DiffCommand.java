package excelprocessor.cmd;

import bus.controller.ICommand;
import controller.MainController;
import diff.ArrayDiff;
import diff.Diff;
import excelprocessor.signals.DiffSignal;
import excelprocessor.workbook.WorkbookWrapper;
import org.slf4j.Logger;
import services.Services;

import java.util.LinkedList;

/**
 * Created by apple on 1/9/17.
 */
public class DiffCommand implements ICommand<DiffSignal> {
    @Override
    public void execute(DiffSignal signal) throws Exception {
        MainController controller = signal.controller;
        WorkbookWrapper oldWb = controller.getWorkbookWrapper(MainController.OLD_FILE_INDEX);
        WorkbookWrapper newWb = controller.getWorkbookWrapper(MainController.NEW_FILE_INDEX);

        String[] oldData = oldWb.getDataForDiffAtSheet(0);
        String[] newData = newWb.getDataForDiffAtSheet(0);

        ArrayDiff diffProcessor = new ArrayDiff();
        LinkedList<Diff<String>> diffs = diffProcessor.diff_main(oldData, newData);

        Services.get(Logger.class).info("DiffCommand {}",diffs.toString());
    }
}
