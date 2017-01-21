package excelprocessor.cmd;

import bus.controller.BusManager;
import bus.controller.ICommand;
import controller.MainController;
import data.CellValue;
import data.CmdHistoryElement;
import data.Record;
import diff.DiffProcessor;
import diff.KKString;
import excelprocessor.signals.ChangeTabSignal;
import excelprocessor.signals.DiffSignal;
import excelprocessor.signals.PushLogSignal;
import excelprocessor.workbook.WorkbookWrapper;
import javafx.collections.ObservableList;
import javafx.scene.control.Cell;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
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
        Services.get(BusManager.class).dispatch(new PushLogSignal("Start comparison"));
        MainController controller = signal.controller;

        WorkbookWrapper oldWb = controller.getWorkbookWrapper(MainController.OLD_FILE_INDEX);
        WorkbookWrapper newWb = controller.getWorkbookWrapper(MainController.NEW_FILE_INDEX);
        int numSheet1 = oldWb.getSheetsName().size();
        int numSheet2 = newWb.getSheetsName().size();
        int minSheet = Math.min(numSheet1, numSheet2);
        ArrayList<LinkedList<DiffProcessor.Diff<String>>> diffsPerSheet = new ArrayList<LinkedList<DiffProcessor.Diff<String>>>();
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
            diffsPerSheet.add(kkDiffs);
            historyElements.addAll(getCmdHistory(sheet, sheetName, kkDiffs,
                oldString, newString, oldColCount, newColCount ));
        }
        controller.buildKKDiffsPerSheet(diffsPerSheet);
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
        Services.get(BusManager.class).dispatch(new PushLogSignal("Done comparison"));

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

                String oldValue = "";
                String newValue = "";
                CellValue.CellState rowState = CellValue.CellState.UNCHANGED;
                int startRow = 0;
                int startCol = 0;
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
                            oldValue = node.text.toString();
                            rowStateFlag *= -1;
                            startRow = startRow1;
                            startCol = startCol1;
                            endRow = endRow1;
                            endCol = endCol1;
                            numOfChangedCell = stack1;
                            break;
                        case INSERT:
                            isCreateCmd = true;
                            newValue = node.text.toString();
                            rowStateFlag *= 2;
                            startRow = startRow2;
                            startCol = startCol2;
                            endRow = endRow2;
                            endCol = endCol2;
                            numOfChangedCell = stack2;
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
                    }else if(rowStateFlag == -1) {
                        rowState = CellValue.CellState.REMOVED;
                    }else if(rowStateFlag < -1) {
                        rowState = CellValue.CellState.MODIFIED;
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
                            startRow1, startRow2, oldValue, newValue, rowState, desc.toString());
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
