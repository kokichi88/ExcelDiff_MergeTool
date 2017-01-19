package excelprocessor.cmd;

import bus.controller.ICommand;
import controller.MainController;
import data.CellValue;
import data.CmdHistoryElement;
import data.Record;
import diff.DiffProcessor;
import diff.KKString;
import excelprocessor.signals.DiffSignal;
import excelprocessor.workbook.WorkbookWrapper;
import javafx.collections.ObservableList;
import javafx.scene.control.Cell;
import org.apache.poi.ss.util.CellReference;
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

        controller.getTableView(MainController.OLD_FILE_INDEX).refresh();
        controller.getTableView(MainController.NEW_FILE_INDEX).refresh();
        String sheetName = oldWb.getSheetsName().get(sheet);
        int oldColCount = oldWb.getMaxRowAndColumnAtSheet(sheet)[1];
        int newColCount = newWb.getMaxRowAndColumnAtSheet(sheet)[1];
        LinkedList<CmdHistoryElement> historyElements = getCmdHistory(sheet, sheetName, kkDiffs,
                oldString, newString, oldColCount, newColCount );
        controller.updateHistoryTableView(historyElements);

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
                            cellValue.setCellState(CellValue.CellState.UNCHANGED);
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

    public LinkedList<CmdHistoryElement> getCmdHistory(int sheetid, String sheetName,LinkedList<DiffProcessor.Diff<String>> diffs, KKString<String> text1, KKString<String> text2, int text1ColCount,
                                                        int text2ColCount) throws Exception {

        LinkedList<CmdHistoryElement> ret = new LinkedList<CmdHistoryElement>();
        int index1 = -1;
        int index2 = -1;
        int stack1 = 0;
        int stack2 = 0;
        List<DiffProcessor.Diff<String>> visitedDiffs = new LinkedList<DiffProcessor.Diff<String>>();
        for(int i = 0 ; i < diffs.size(); ++i) {
            DiffProcessor.Diff<String> diff = diffs.get(i);
            boolean canAdd = false;
            switch (diff.operation) {
                case EQUAL:
                    canAdd = true;
                    break;
                case DELETE:
                    visitedDiffs.add(diff);
                    stack1 += diff.text.length();
                    break;
                case INSERT:
                    visitedDiffs.add(diff);
                    stack2 += diff.text.length();
                    break;
            }
            if(visitedDiffs.size() > 2) {
                throw new Exception("it cant happen");
            }
            if(i == diffs.size() - 1 && !canAdd) {
                canAdd = true;
            }
            if(visitedDiffs.size() > 0 && canAdd) {
                int previousRow1 = Math.max(index1 + 1, 0) / text1ColCount;
                int previousCol1 = Math.max(index1 + 1, 0) % text1ColCount;
                int previousRow2 = Math.max(index2 + 1, 0) / text2ColCount;
                int previousCol2 = Math.max(index2 + 1, 0) % text2ColCount;
                index1 += stack1;
                index2 += stack2;

                int endRow1 = Math.max(index1, 0) / text1ColCount;
                int startRow1 = previousRow1 >= endRow1 ? endRow1 : previousRow1;
                int endCol1 = Math.max(index1, 0) % text1ColCount;
                int startCol1 = previousCol1;

                int endRow2 = Math.max(index2, 0) / text2ColCount;
                int startRow2 = previousRow2 >= endRow2 ? endRow2 : previousRow2;
                int endCol2 = Math.max(index2, 0) % text2ColCount;
                int startCol2 = previousCol2;

                String oldValue = "";
                String newValue = "";
                CellValue.CellState rowState = CellValue.CellState.UNCHANGED;
                int startRow = 0;
                int startCol = 0;
                int endRow = 0;
                int endCol = 0;
                int numOfChangedCell = 0;
                for(DiffProcessor.Diff<String> node : visitedDiffs) {
                    switch (node.operation) {
                        case DELETE:
                            oldValue = node.text.toString();
                            rowState = rowState == CellValue.CellState.UNCHANGED ?
                                    CellValue.CellState.REMOVED : CellValue.CellState.MODIFIED;
                            startRow = startRow1;
                            startCol = startCol1;
                            endRow = endRow1;
                            endCol = endCol1;
                            numOfChangedCell = stack1;
                            break;
                        case INSERT:
                            newValue = node.text.toString();
                            rowState = rowState == CellValue.CellState.UNCHANGED ?
                                    CellValue.CellState.ADDED : CellValue.CellState.MODIFIED;
                            startRow = startRow2;
                            startCol = startCol2;
                            endRow = endRow2;
                            endCol = endCol2;
                            numOfChangedCell = stack2;
                            break;
                    }
                }
                StringBuilder desc = new StringBuilder();
                desc.append(rowState);

                if(numOfChangedCell > 1) {
                    desc.append(" ").append(numOfChangedCell).append(" cells from ")
                            .append(CellReference.convertNumToColString(startCol))
                            .append(":").append(startRow + 1).append(" to ")
                            .append(CellReference.convertNumToColString(endCol))
                            .append(":").append(endRow + 1);
                }else {
                    desc.append(" ").append(numOfChangedCell).append(" cell ")
                            .append(CellReference.convertNumToColString(startCol))
                            .append(":").append(startRow + 1);
                }

                CmdHistoryElement element = new CmdHistoryElement(sheetid, sheetName,
                        startRow1, startRow2, oldValue, newValue, rowState, desc.toString());
                ret.add(element);
                visitedDiffs.clear();
                stack1 = 0;
                stack2 = 0;
            }
            if(diff.operation == DiffProcessor.Operation.EQUAL) {
                index1 += diff.text.length();
                index2 += diff.text.length();
            }

        }
        return ret;

    }
}
