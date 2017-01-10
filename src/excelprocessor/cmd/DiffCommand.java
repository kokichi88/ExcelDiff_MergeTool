package excelprocessor.cmd;

import bus.controller.ICommand;
import controller.MainController;
import data.CellValue;
import data.Record;
import diff.ArrayDiff;
import diff.Diff;
import excelprocessor.signals.DiffSignal;
import excelprocessor.workbook.WorkbookWrapper;
import javafx.collections.ObservableList;
import javafx.scene.control.Cell;
import org.slf4j.Logger;
import services.Services;

import java.util.LinkedList;
import java.util.List;

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

        String[] oldData = oldWb.getDataForDiffAtSheet(sheet);
        String[] newData = newWb.getDataForDiffAtSheet(sheet);

        ArrayDiff<String> diffProcessor = new ArrayDiff<String>(String.class);
        LinkedList<Diff<String>> diffs = diffProcessor.diff_main(oldData, newData);

        ObservableList<Record<String>> oldRecords = oldWb.getRenderDataAtSheet(sheet);
        ObservableList<Record<String>> newRecords = newWb.getRenderDataAtSheet(sheet);

        // process oldRecord
        processOldRecord(oldWb, diffs, oldRecords, sheet);

        int newMaxCol = newWb.getMaxRowAndColumnAtSheet(sheet)[1];
        int oldMaxCol = newWb.getMaxRowAndColumnAtSheet(sheet)[1];

//        processOldRecord(newWb, diffs, newRecords, sheet);
    }

    private void processOldRecord(WorkbookWrapper oldWb, List<Diff<String>> diffs, ObservableList<Record<String>> oldRecords, int sheet) {
        int countUnchanged = 0;
        int countRemoved = 0;
        int[] maxRowAndCol = oldWb.getMaxRowAndColumnAtSheet(sheet);
        int maxCol = maxRowAndCol[1];
        int maxRow = maxRowAndCol[0];
        int totalCell = maxRow * maxCol;
        for(Diff<String> diff : diffs) {
//            if(countUnchanged >= totalCell) break;
            switch (diff.operation) {
                case UNCHANGED:
                    for(int i = 0; i < diff.array.length; ++i) {
                        countUnchanged++;
                        int row = (countUnchanged - 1) / maxCol;
                        int col = (countUnchanged - 1) % maxCol;
                        CellValue<String> cellValue = oldRecords.get(row).cells.get(col);
                        cellValue.setCellState(CellValue.CellState.UNCHANCED);
                    }
                    countRemoved = 0;
                    break;
                case REMOVED:
                    for(int i = 0; i < diff.array.length; ++i) {
                        countUnchanged++;
                        int row = (countUnchanged - 1) / maxCol;
                        int col = (countUnchanged - 1) % maxCol;
                        CellValue<String> cellValue = oldRecords.get(row).cells.get(col);
                        cellValue.setCellState(CellValue.CellState.REMOVED);
                    }
                    countRemoved = diff.array.length;
                    break;
                case ADDED:
//                    for(int i = 0; i < diff.array.length; ++i) {
//                        int dt = i - countRemoved;
//                        int index;
//                        if(dt > -1) {
//                            countUnchanged++;
//                            index = countUnchanged - 1;
//                        }else {
//                            index = countUnchanged + dt;
//                        }
//                        int row = index / maxCol;
//                        int col = index % maxCol;
//                        CellValue<String> cellValue = oldRecords.get(row).cells.get(col);
//                        cellValue.setCellState(dt > -1? CellValue.CellState.ADDED : CellValue.CellState.MODIFIED);
//                    }
            }
        }
    }
}
