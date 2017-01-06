package excelprocessor.cmd;

import bus.controller.ICommand;
import excelprocessor.signals.ChangeTabSignal;
import excelprocessor.workbook.WorkbookWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.util.List;

/**
 * Created by apple on 1/4/17.
 */
public class ChangeTabCommand implements ICommand<ChangeTabSignal> {

    @Override
    public void execute(ChangeTabSignal signal) throws Exception {
        WorkbookWrapper[] workbooks = signal.workbookWrappers;
        TableView[] tableViews = signal.tableViews;
        Tab newValue = signal.newValue;
        Tab oldValue = signal.oldValue;

        int sheet = Integer.parseInt(newValue.getId());
        int fileId = Integer.parseInt(newValue.getTabPane().getId());
        WorkbookWrapper wb = workbooks[fileId];
        TableView tableView = tableViews[fileId];
        List<String> columns = wb.getColumnsAtSheet(sheet);
        tableView.getColumns().clear();
        for(int i= 0 ; i < columns.size(); i++) {
            final int j = i;
            TableColumn col = new TableColumn(columns.get(i));
            col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList,String>,ObservableValue<String>>(){
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                    return new SimpleStringProperty(param.getValue().get(j).toString());
                }
            });
            tableView.getColumns().add(col);
        }
        ObservableList<ObservableList<String>> records = wb.getRenderDataAtSheet(sheet);
        tableView.setItems(records);
        oldValue.setContent(null);
        newValue.setContent(tableView);
    }

}
