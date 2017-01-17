package controller;

import bus.controller.BusManager;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import data.CellValue;
import data.CmdHistoryElement;
import data.Record;
import excelprocessor.signals.ChangeTabSignal;
import excelprocessor.signals.DiffSignal;
import excelprocessor.signals.PushLogSignal;
import excelprocessor.workbook.WorkbookWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.Services;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Created by apple on 1/2/17.
 */
public class MainController implements Initializable {
    private Logger logger = LoggerFactory.getLogger(MainController.class);
    public static Label display;
    public static int MAX_FILE = 2;
    public static int OLD_FILE_INDEX = 0;
    public static int NEW_FILE_INDEX = 1;

    @FXML
    private TabPane oldTabPane;

    @FXML
    private TabPane newTabPane;

    @FXML
    private TableView cmdHistoryTableView;

    @FXML
    private Label outputContent;

    private WorkbookWrapper[] workbooks = new WorkbookWrapper[MAX_FILE];
    private TableView<Record<String>>[] tableViews = new TableView[MAX_FILE];
    private TabPane[] tabPanes = new TabPane[MAX_FILE];
    private int[] selectedSheets = new int[MAX_FILE];
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTableView();
        initTabPane();
        Arrays.fill(selectedSheets, -1);
        display = outputContent;
    }

    public void setSelectedSheet(int index, int sheet) {
        selectedSheets[index] = sheet;
        int value = -1;
        for(int i = 0; i < selectedSheets.length; ++i) {
            if(value == -1 && i == 0)
                value = selectedSheets[i];
            else if(value == -1 || value != selectedSheets[i]) {
                if(value > -1 && selectedSheets[i] > -1) {
                    String msg = "Cant compare two different sheets id: " +
                            workbooks[OLD_FILE_INDEX].getSheetsName().get(selectedSheets[OLD_FILE_INDEX]) + "  -  " +
                            workbooks[NEW_FILE_INDEX].getSheetsName().get(selectedSheets[NEW_FILE_INDEX]) ;
                    Services.get(BusManager.class).dispatch(new PushLogSignal(msg));
                }
                return;
            }
        }
        String msg = "Compare two sheets id: " +
                workbooks[OLD_FILE_INDEX].getSheetsName().get(selectedSheets[OLD_FILE_INDEX]) + "  -  " +
                workbooks[NEW_FILE_INDEX].getSheetsName().get(selectedSheets[NEW_FILE_INDEX]) ;
        Services.get(BusManager.class).dispatch(new PushLogSignal(msg));
        Services.get(BusManager.class).dispatch(new DiffSignal(this, sheet));
    }

    public void setWorkbooks(int index, WorkbookWrapper wb) {
        assert index < MAX_FILE : "index must be lesser than " + MAX_FILE;
        workbooks[index] = wb;
    }

    public WorkbookWrapper getWorkbookWrapper(int index) {
        return workbooks[index];
    }

    public int getDefaultSheetIndexOf(int index) {
        WorkbookWrapper wb = workbooks[index];
        return wb.getDefaultSheetIndex();
    }

    private void initTableView() {
        for(int i = 0; i < tableViews.length; ++i) {
            final int j = i;
            tableViews[j] = new TableView<Record<String>>();
            configureTableViewStyle(tableViews[j]);
        }
        configureTableViewStyle(cmdHistoryTableView);

        cmdHistoryTableView.getColumns().clear();
        for(int i= 0 ; i < CmdHistoryElement.HISTORY_COLS.length; i++) {
            final int j = i;
            TableColumn<CmdHistoryElement, String> col = new TableColumn<CmdHistoryElement, String>(CmdHistoryElement.HISTORY_COLS[i]);
            col.setSortable(false);
            col.setCellValueFactory(
                    new PropertyValueFactory<CmdHistoryElement, String>(CmdHistoryElement.PROPERTIES[i]));
            cmdHistoryTableView.getColumns().add(col);
        }

        ObservableList<CmdHistoryElement> datas = FXCollections.observableArrayList();
        datas.addAll(new CmdHistoryElement("a",1, 1,"c","d","desc"));
        datas.addAll(new CmdHistoryElement("a",1, 2,"c","d","desc"));
        datas.addAll(new CmdHistoryElement("a",1, 3,"c","d","desc"));
        datas.addAll(new CmdHistoryElement("a",1, 4,"c","d","desc"));

        cmdHistoryTableView.setItems(datas);
    }

    private void configureTableViewStyle(final TableView tableView) {
        tableView.widthProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> source, Number oldWidth, Number newWidth)
            {
                final TableHeaderRow header = (TableHeaderRow) tableView.lookup("TableHeaderRow");
                header.reorderingProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        header.setReordering(false);
                    }
                });
            }
        });

        tableView.
                setStyle("-fx-selection-bar: #E6ED95; -fx-selection-bar-non-focused: #E6ED95;" +
                        "-fx-selection-bar-text: firebrick;");
    }

    public TableView getTableView(int index) {
        return tableViews[index];
    }

    public void scrollTableViewTo(int index, Record<String> row) {
        TableView tableView = tableViews[index];
        tableView.scrollTo(row);
        tableView.getSelectionModel().select(row);
    }

    private void initTabPane() {
        assert oldTabPane == null : "can't load fx:id=oldTabPane";
        assert newTabPane == null : "can't load fx:id=newTabPane";
        tabPanes[OLD_FILE_INDEX] = oldTabPane;
        tabPanes[NEW_FILE_INDEX] = newTabPane;
        for(int i = 0; i < tabPanes.length; ++i) {
            if(tabPanes[i] != null) {
                TabPane tabPane = tabPanes[i];
                tabPane.setId(String.valueOf(i));
                tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                    @Override
                    public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                        if(newValue != null && newValue.getTabPane() != null) {
                            int fileId = Integer.parseInt(newValue.getTabPane().getId());
                            WorkbookWrapper wb = workbooks[fileId];
                            TableView tableView = tableViews[fileId];
                            int sheet = Integer.parseInt(newValue.getId());
                            ChangeTabSignal msg = new ChangeTabSignal(wb, tableView, newValue, oldValue, sheet);
                            Services.get(BusManager.class).dispatch(msg);
                        }
                    }
                });
            }
        }

    }

    public void updateTabPane(int index, WorkbookWrapper wb) {
        TabPane tabPane = tabPanes[index];
        ObservableList<Tab> tabs = tabPane.getTabs();
        tabs.clear();
        List<String> sheetsName = wb.getSheetsName();
        for(int j = 0; j < sheetsName.size(); ++j) {
            Tab tab = new Tab(sheetsName.get(j));
            tab.setId(String.valueOf(j));
            tabs.add(tab);
        }
    }

    public TabPane getTabPane(int index) {
        return tabPanes[index];
    }

}
