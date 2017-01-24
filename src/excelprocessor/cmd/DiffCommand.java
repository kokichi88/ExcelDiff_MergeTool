package excelprocessor.cmd;

import bus.controller.BusManager;
import bus.controller.ICommand;
import controller.MainController;
import data.CellValue;
import data.CmdHistoryElement;
import diff.DiffProcessor;
import diff.KKString;
import excelprocessor.signals.ChangeTabSignal;
import excelprocessor.signals.DiffSignal;
import excelprocessor.signals.PushLogSignal;
import excelprocessor.workbook.WorkbookWrapper;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import org.apache.poi.ss.util.CellReference;
import services.Services;

import java.util.*;

/**
 * Created by apple on 1/9/17.
 */
public class DiffCommand implements ICommand<DiffSignal> {
    @Override
    public void execute(DiffSignal signal) throws Exception {
        long currentTime = System.currentTimeMillis();
        Services.get(BusManager.class).dispatch(new PushLogSignal("Start comparison "));
        MainController controller = signal.controller;

        WorkbookWrapper oldWb = controller.getWorkbookWrapper(MainController.OLD_FILE_INDEX);
        WorkbookWrapper newWb = controller.getWorkbookWrapper(MainController.NEW_FILE_INDEX);
        int numSheet1 = oldWb.getSheetsName().size();
        int numSheet2 = newWb.getSheetsName().size();
        int minSheet = Math.min(numSheet1, numSheet2);
        Map<Integer, LinkedList<DiffProcessor.Diff<String>>> kkDiffsPerSheet = new HashMap<Integer, LinkedList<DiffProcessor.Diff<String>>>();
        LinkedList<CmdHistoryElement> historyElements = new LinkedList<CmdHistoryElement>();
        for(int sheet = 0; sheet < minSheet; ++ sheet) {
            String sheetName = oldWb.getSheetsName().get(sheet);
            KKString<String> oldString = oldWb.getKKStringAtSheet(sheet);
            KKString<String> newString = newWb.getKKStringAtSheet(sheet);
            int oldColCount = oldWb.getMaxRowAndColumnAtSheet(sheet)[1];
            int newColCount = newWb.getMaxRowAndColumnAtSheet(sheet)[1];
            DiffProcessor<String> stringDiffProcessor = new DiffProcessor<String>(String.class);
            LinkedList<DiffProcessor.Diff<String>> kkDiffs = stringDiffProcessor.diff_main(oldString, newString);
            kkDiffs = cleanUpKKString(kkDiffs, WorkbookWrapper.SEPARATOR);
            kkDiffsPerSheet.put(sheet, kkDiffs);
            historyElements.addAll(getCmdHistory(sheet, sheetName, kkDiffs,
                oldString, newString, oldColCount, newColCount ));
        }
        controller.setKKDiffsPerSheet(kkDiffsPerSheet);
        controller.updateHistoryTableView(historyElements);

        for(int index = 0; index < MainController.MAX_FILE; ++index) {
            TabPane tabPane = controller.getTabPane(index);
            int sheet = controller.getDefaultSheetIndexOf(index);
            TableView tableView = controller.getTableView(index);
            WorkbookWrapper wb = controller.getWorkbookWrapper(index);
            if(sheet > -1) {
                Tab tab = tabPane.getTabs().get(sheet);
                ChangeTabSignal msg = new ChangeTabSignal(wb, tableView, tab, tab, sheet);
                Services.get(BusManager.class).dispatch(msg);
            }
        }
        long elapsedTime = System.currentTimeMillis() - currentTime;
        Services.get(BusManager.class).dispatch(new PushLogSignal("Comparison's done in " + elapsedTime + " ms"));

    }

    public LinkedList<DiffProcessor.Diff<String>> cleanUpKKString(LinkedList<DiffProcessor.Diff<String>> kkDiffs, KKString<String> separator) {
        LinkedList<DiffProcessor.Diff<String>> ret = new LinkedList<DiffProcessor.Diff<String>>();
        for(DiffProcessor.Diff<String> diff : kkDiffs) {
            ret.addAll(diff.split(separator));
        }
        return ret;
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
                case EMPTY_DELETE:
                    visitedDiffs.add(diff);
                    stack1 += diff.text.length();
                    break;
                case INSERT:
                case EMPTY_INSERT:
                    visitedDiffs.add(diff);
                    stack2 += diff.text.length();
                    break;
            }

            if(i == diffs.size() - 1 && !canAdd) {
                canAdd = true;
            }
            if(visitedDiffs.size() > 0 && canAdd) {
                DiffProcessor.Diff<String> firstDiff = visitedDiffs.get(0);
                if(firstDiff.operation == DiffProcessor.Operation.EMPTY_DELETE) {
                    index1++;
                    stack1--;
                    visitedDiffs.remove(0);
                }else if(firstDiff.operation == DiffProcessor.Operation.EMPTY_INSERT) {
                    index2++;
                    stack2--;
                    visitedDiffs.remove(0);
                }

                int previousRow1 = Math.max(index1 + 1, 0) / text1ColCount;
                int previousCol1 = Math.max(index1 + 1, 0) % text1ColCount;
                int previousRow2 = Math.max(index2 + 1, 0) / text2ColCount;
                int previousCol2 = Math.max(index2 + 1, 0) % text2ColCount;
                index1 += stack1;
                index2 += stack2;
                int d1 = 0;
                int d2 = 0;
                if(visitedDiffs.size() > 1) {
                    DiffProcessor.Diff<String> lastDiff = visitedDiffs.get(visitedDiffs.size() - 1);
                    if(lastDiff.operation == DiffProcessor.Operation.EMPTY_DELETE) {
                        stack1--;
                        d1 = -1;
                        visitedDiffs.remove(lastDiff);
                    }else if(lastDiff.operation == DiffProcessor.Operation.EMPTY_INSERT) {
                        stack2--;
                        d2 = -1;
                        visitedDiffs.remove(lastDiff);
                    }
                }
                int endRow1 = Math.max(index1 + d1, 0) / text1ColCount;
                int startRow1 = previousRow1 >= endRow1 ? endRow1 : previousRow1;
                int endCol1 = Math.max(index1 + d1, 0) % text1ColCount;
                int startCol1 = previousCol1;

                int endRow2 = Math.max(index2 + d2, 0) / text2ColCount;
                int startRow2 = previousRow2 >= endRow2 ? endRow2 : previousRow2;
                int endCol2 = Math.max(index2 + d2, 0) % text2ColCount;
                int startCol2 = previousCol2;

                KKString<String> oldValue = new KKString<String>();
                KKString<String> newValue = new KKString<String>();
                CellValue.CellState rowState = CellValue.CellState.UNCHANGED;
                int startRow = 0;
                int startCol = 0;
                int oldStartCol = -1;
                int newStartCol = -1;
                int endRow = 0;
                int endCol = 0;
                int numOfChangedCell = 0;
                int d3 = 0;
                boolean isCreateCmd = false;
                int rowStateFlag = 1;
                for(DiffProcessor.Diff<String> node : visitedDiffs) {
                    switch (node.operation) {
                        case DELETE:
                            isCreateCmd = true;
                            oldValue = oldValue.concat(node.text);
                            rowStateFlag *= -1;
                            startRow = startRow1;
                            startCol = startCol1;
                            endRow = Math.max(endRow1, endRow);
                            endCol = Math.max(endCol1, endCol);
                            numOfChangedCell = Math.max(stack1, numOfChangedCell);
                            break;
                        case INSERT:
                            isCreateCmd = true;
                            newValue = newValue.concat(node.text);
                            rowStateFlag *= 2;
                            startRow = startRow2;
                            startCol = startCol2;
                            endRow = Math.max(endRow2, endRow);
                            endCol = Math.max(endCol2, endCol);
                            numOfChangedCell = Math.max(stack2, numOfChangedCell);
                            break;
                        case EMPTY_DELETE:
                        case EMPTY_INSERT:
                            d3++;
                            break;
                    }
                }
                if(isCreateCmd) {
                    if(rowStateFlag > 0) {
                        rowState = CellValue.CellState.ADDED;
                        oldStartCol = Math.max(0, startCol - 1);
                        newStartCol = startCol;
                    }else if(rowStateFlag == -1) {
                        rowState = CellValue.CellState.REMOVED;
                        oldStartCol = startCol;
                        newStartCol = Math.max(0, startCol - 1);
                    }else if(rowStateFlag < -1) {
                        rowState = CellValue.CellState.MODIFIED;
                        oldStartCol = newStartCol = startCol;
                    }
                    numOfChangedCell -= d3;
                    StringBuilder desc = new StringBuilder();
                    desc.append(rowState);

                    if(numOfChangedCell > 1) {
                        desc.append(" ").append(numOfChangedCell).append(" cells from ")
                                .append(CellReference.convertNumToColString(startCol))
                                .append(startRow + 1).append(" to ")
                                .append(CellReference.convertNumToColString(endCol))
                                .append(endRow + 1);
                    }else {
                        desc.append(" ").append(numOfChangedCell).append(" cell ")
                                .append(CellReference.convertNumToColString(startCol))
                                .append(startRow + 1);
                    }

                    CmdHistoryElement element = new CmdHistoryElement(sheetid, sheetName,
                            startRow1, startRow2, oldStartCol, newStartCol, oldValue.toString(), newValue.toString(), rowState, desc.toString());
                    ret.add(element);
                }
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
