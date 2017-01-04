package bus.cmd;

import bus.controller.ICommand;
import bus.data.Message;
import controller.MainController;
import excelprocessor.workbook.WorkbookWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.util.List;

/**
 * Created by apple on 1/4/17.
 */
public class ChangeTabCommand implements ICommand {

    @Override
    public void execute(Message message) {
        TabPane[] tabPanes = (TabPane[])message.params.get("tabPanes");
        WorkbookWrapper[] workbooks = (WorkbookWrapper[])message.params.get("workbooks");
        TableView[] tableViews = (TableView[])message.params.get("tableViews");
        Tab newValue = (Tab)message.params.get("newValue");
        Tab oldValue = (Tab)message.params.get("oldValue");

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
