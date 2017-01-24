package excelprocessor.cmd;

import bus.controller.ICommand;
import controller.MainController;
import data.CellValue;
import data.Record;
import diff.DiffProcessor;
import excelprocessor.signals.ChangeTabSignal;
import excelprocessor.workbook.WorkbookWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.util.Callback;
import services.Services;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by apple on 1/4/17.
 */
public class ChangeTabCommand implements ICommand<ChangeTabSignal> {

    @Override
    public void execute(ChangeTabSignal signal) throws Exception {
        WorkbookWrapper wb = signal.wb;
        TableView tableView = signal.tableView;
        Tab newValue = signal.newValue;
        Tab oldValue = signal.oldValue;
        int sheet = signal.sheet;

        List<String> columns = wb.getColumnsAtSheet(sheet);
        tableView.getColumns().clear();
        for(int i= 0 ; i < columns.size(); i++) {
            final int j = i;
            TableColumn<Record<String>, CellValue<String>> col = new TableColumn<Record<String>, CellValue<String>>(columns.get(i));
            col.setSortable(false);
            col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Record<String>, CellValue<String>>, ObservableValue<CellValue<String>>>() {
                @Override
                public ObservableValue<CellValue<String>> call(TableColumn.CellDataFeatures<Record<String>, CellValue<String>> param) {
                    return new SimpleObjectProperty(param.getValue().cells.get(j));
                }
            });
            col.setCellFactory(new Callback<TableColumn<Record<String>, CellValue<String>>, TableCell<Record<String>, CellValue<String>>>() {
                @Override
                public TableCell<Record<String>, CellValue<String>> call(TableColumn<Record<String>, CellValue<String>> param) {
                    return CellValue.createTableCell(String.class);
                }
            });
            tableView.getColumns().add(col);
        }
        ObservableList<Record<String>> records = wb.getRenderDataAtSheet(sheet);
        tableView.setItems(records);
        oldValue.setContent(null);
        newValue.setContent(tableView);
        MainController controller = Services.get(MainController.class);
        controller.setSelectedSheet(wb.getId(), sheet);
        int index = controller.indexOfWorkbookWrapper(wb);
        LinkedList<DiffProcessor.Diff<String>> kkDiffs = controller.getKKDiffsBySheet(sheet);
        if(kkDiffs != null) {
            DiffProcessor.Operation[] ops = index == MainController.OLD_FILE_INDEX ?
                    new DiffProcessor.Operation[]{DiffProcessor.Operation.DELETE,
                            DiffProcessor.Operation.EMPTY_DELETE,
                            DiffProcessor.Operation.EQUAL} :
                    new DiffProcessor.Operation[]{DiffProcessor.Operation.INSERT,
                            DiffProcessor.Operation.EMPTY_INSERT,
                            DiffProcessor.Operation.EQUAL};
            updateTableView(wb, kkDiffs, records, sheet, ops);
        }

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

}
