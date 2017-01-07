package excelprocessor.cmd;

import bus.controller.ICommand;
import data.CellValue;
import excelprocessor.signals.ChangeTabSignal;
import excelprocessor.workbook.WorkbookWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.util.List;
import java.util.Objects;

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
            TableColumn<ObservableList<CellValue<String>>, CellValue<String>> col = new TableColumn<ObservableList<CellValue<String>>, CellValue<String>>(columns.get(i));
            col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList<CellValue<String>>, CellValue<String>>, ObservableValue<CellValue<String>>>() {
                @Override
                public ObservableValue<CellValue<String>> call(TableColumn.CellDataFeatures<ObservableList<CellValue<String>>, CellValue<String>> param) {
                    return new SimpleObjectProperty(param.getValue().get(j).getValue());
                }
            });
            col.setCellFactory(new Callback<TableColumn<ObservableList<CellValue<String>>, CellValue<String>>, TableCell<ObservableList<CellValue<String>>, CellValue<String>>>() {
                @Override
                public TableCell<ObservableList<CellValue<String>>, CellValue<String>> call(TableColumn<ObservableList<CellValue<String>>, CellValue<String>> param) {
                    return CellValue.createTableCell(String.class);
                }
            });
            tableView.getColumns().add(col);
        }
        ObservableList<ObservableList<CellValue<String>>> records = wb.getRenderDataAtSheet(sheet);
        tableView.setItems(records);
        oldValue.setContent(null);
        newValue.setContent(tableView);
    }

}
