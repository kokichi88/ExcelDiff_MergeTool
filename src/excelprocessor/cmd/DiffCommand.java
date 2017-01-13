package excelprocessor.cmd;

import bus.controller.ICommand;
import controller.MainController;
import data.CellValue;
import data.Record;
import diff.ArrayDiff;
import diff.Diff;
import diff.DiffProcessor;
import diff.KKString;
import excelprocessor.signals.DiffSignal;
import excelprocessor.workbook.WorkbookWrapper;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import services.Services;

import java.util.*;

/**
 * Created by apple on 1/9/17.
 */
public class DiffCommand implements ICommand<DiffSignal> {
    @Override
    public void execute(DiffSignal signal) throws Exception {
        MainController controller = signal.controller;
        int sheet = signal.sheet;
        WorkbookWrapper oldWb = controller.getWorkbookWrapper(MainController.OLD_FILE_INDEX);
        WorkbookWrapper newWb = controller.getWorkbookWrapper(MainController.NEW_FILE_INDEX);

        ObservableList<Record<String>> oldRecords = oldWb.getRenderDataAtSheet(sheet);
        ObservableList<Record<String>> newRecords = newWb.getRenderDataAtSheet(sheet);

        KKString<String> oldString = oldWb.getKKStringAtSheet(sheet);
        KKString<String> newString = newWb.getKKStringAtSheet(sheet);
        DiffProcessor<String> stringDiffProcessor = new DiffProcessor<String>(String.class);
        LinkedList<DiffProcessor.Diff<String>> kkDiffs = stringDiffProcessor.diff_main(oldString, newString);

        updateTableView(oldWb, kkDiffs, oldRecords,sheet, new DiffProcessor.Operation[]{DiffProcessor.Operation.DELETE,
                DiffProcessor.Operation.EQUAL});

        updateTableView(newWb, kkDiffs, newRecords, sheet, new DiffProcessor.Operation[]{DiffProcessor.Operation.INSERT,
                DiffProcessor.Operation.EQUAL});

        List<Record<String>> changes = getChangeRecords(oldWb, kkDiffs, oldRecords,sheet, new DiffProcessor.Operation[]{DiffProcessor.Operation.DELETE,
                DiffProcessor.Operation.EQUAL});

        if(changes.size() > 0)
            controller.scrollTableViewTo(MainController.OLD_FILE_INDEX, changes.get(0));
    }

    private List<Record<String>> getChangeRecords(WorkbookWrapper wb, List<DiffProcessor.Diff<String>> diffs, ObservableList<Record<String>> records,
                                                 int sheet, DiffProcessor.Operation[] opFilters) {
        List<Record<String>> changes = new ArrayList<Record<String>>();
        int[] maxRowAndCol = wb.getMaxRowAndColumnAtSheet(sheet);
        int maxCol = maxRowAndCol[1];
        int index = -1;
        int lastRowIndex = -1;
        for(DiffProcessor.Diff<String> diff : diffs) {
            if(isValidOp(opFilters, diff.operation)) {
                for(int i = 0; i < diff.text.length(); ++i) {
                    index++;
                    if(diff.operation == DiffProcessor.Operation.DELETE) {
                        int rowIndex = index / maxCol;
                        if(rowIndex != lastRowIndex) {
                            changes.add(records.get(rowIndex));
                            lastRowIndex = rowIndex;
                        }
                    }
                }
            }
        }
        return changes;
    }

    private void updateTableView(WorkbookWrapper wb, List<DiffProcessor.Diff<String>> diffs, ObservableList<Record<String>> records,
                                 int sheet, DiffProcessor.Operation[] opFilters) {
        int[] maxRowAndCol = wb.getMaxRowAndColumnAtSheet(sheet);
        int maxCol = maxRowAndCol[1];
        int index = -1;
        for(DiffProcessor.Diff<String> diff : diffs) {
            if(isValidOp(opFilters, diff.operation)) {
                for(int i = 0; i < diff.text.length(); ++i) {
                    index++;
                    int row = index / maxCol;
                    int col = index % maxCol + 1;
                    CellValue<String> cellValue = records.get(row).cells.get(col);
                    switch (diff.operation) {
                        case EQUAL:
                            cellValue.setCellState(CellValue.CellState.UNCHANCED);
                            break;
                        case DELETE:
                            cellValue.setCellState(CellValue.CellState.REMOVED);
                            break;
                        case INSERT:
                            cellValue.setCellState(CellValue.CellState.ADDED);
                            break;
                    }
                }
            }
        }
    }

    boolean isValidOp(DiffProcessor.Operation[] validOps, DiffProcessor.Operation op) {
        for(DiffProcessor.Operation operation : validOps) {
            if(operation == op) return true;
        }
        return false;
    }
}
