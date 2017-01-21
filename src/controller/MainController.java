package controller;

import bus.controller.BusManager;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import data.CellValue;
import data.CmdHistoryElement;
import data.Record;
import diff.DiffProcessor;
import excelprocessor.signals.ChangeTabSignal;
import excelprocessor.signals.CmdHistorySelectedSignal;
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
import view.HistoryRowFactory;

import java.net.URL;
import java.util.*;

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
    private Label oldFileLb;

    @FXML
    private Label newFileLb;

    @FXML
    private TableView cmdHistoryTableView;

    @FXML
    private Label outputContent;

    private WorkbookWrapper[] workbooks = new WorkbookWrapper[MAX_FILE];
    private TableView<Record<String>>[] tableViews = new TableView[MAX_FILE];
    private TabPane[] tabPanes = new TabPane[MAX_FILE];
    private Map<Integer, LinkedList<DiffProcessor.Diff<String>>> kkDiffsPerSheet = new HashMap<Integer, LinkedList<DiffProcessor.Diff<String>>>();
    private int[] selectedSheets = new int[MAX_FILE];
    private int loadedWorkbooks = 0;
    private Label[] fileLabels = new Label[MAX_FILE];
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTableView();
        initTabPane();
        initLable();
        Arrays.fill(selectedSheets, -1);
        display = outputContent;
    }

    private void initLable() {
        assert oldFileLb == null : "can't load fx:id=oldFileLb";
        assert newFileLb == null : "can't load fx:id=newFileLb";
        fileLabels[OLD_FILE_INDEX] = oldFileLb;
        fileLabels[NEW_FILE_INDEX] = newFileLb;
    }

    public void setFileLableText(int index, String fname) {
        fileLabels[index].setText(fname);
    }

    public void buildKKDiffsPerSheet(List<LinkedList<DiffProcessor.Diff<String>>> list) {
        kkDiffsPerSheet.clear();
        for(int i = 0 ; i < list.size(); ++i) {
            kkDiffsPerSheet.put(i, list.get(i));
        }
    }

    public LinkedList<DiffProcessor.Diff<String>> getKKDiffsBySheet(int sheet) {
        return kkDiffsPerSheet.get(sheet);
    }

    public void setSelectedSheet(int index, int sheet) {
        selectedSheets[index] = sheet;
    }

    public int getCurrentSelectedSheet(int index) {
        return selectedSheets[index];
    }

    public void setWorkbooks(int index, WorkbookWrapper wb) {
        assert index < MAX_FILE : "index must be lesser than " + MAX_FILE;
        workbooks[index] = wb;

    }

    public void increaseLoadedWorkbook() {
        synchronized (this) {
            loadedWorkbooks++;
        }
        if(loadedWorkbooks % MAX_FILE == 0) {

            Services.get(BusManager.class).dispatch(new DiffSignal(this));
        }
    }

    public void clearLoadedWorkbook() {
        synchronized (this) {
            loadedWorkbooks = 0;
        }
    }

    public WorkbookWrapper getWorkbookWrapper(int index) {
        return workbooks[index];
    }

    public int indexOfWorkbookWrapper(WorkbookWrapper wb) {
        for(int i = 0; i < workbooks.length; ++i) {
            if(wb == workbooks[i])
                return i;
        }
        return -1;
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
        cmdHistoryTableView.setRowFactory(new HistoryRowFactory());
        cmdHistoryTableView.getColumns().clear();
        for(int i= 0 ; i < CmdHistoryElement.HISTORY_COLS.length; i++) {
            final int j = i;
            TableColumn<CmdHistoryElement, String> col = new TableColumn<CmdHistoryElement, String>(CmdHistoryElement.HISTORY_COLS[i]);
            col.setSortable(false);
            col.setCellValueFactory(
                    new PropertyValueFactory<CmdHistoryElement, String>(CmdHistoryElement.PROPERTIES[i]));
            cmdHistoryTableView.getColumns().add(col);
        }

        cmdHistoryTableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if(cmdHistoryTableView.getSelectionModel().getSelectedItem() != null) {
                    TableView.TableViewSelectionModel selectionModel = cmdHistoryTableView.getSelectionModel();
                    TablePosition tablePosition = (TablePosition)selectionModel.getSelectedCells().get(0);
                    CmdHistoryElement element = (CmdHistoryElement)cmdHistoryTableView.getItems().get(tablePosition.getRow());
                    CmdHistorySelectedSignal signal = new CmdHistorySelectedSignal(Services.get(MainController.class), element.getSheetid(),element.getOldRow() -1 ,
                            element.getNewRow() -1);
                    Services.get(BusManager.class).dispatch(signal);
                }
            }
        });
    }

    public void updateHistoryTableView(List<CmdHistoryElement> data) {
        ObservableList<CmdHistoryElement> renderData = FXCollections.observableArrayList(data);
        cmdHistoryTableView.setItems(renderData);
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

//        tableView.
//                setStyle("-fx-selection-bar: #; -fx-selection-bar-non-focused: #E6ED95; -fx-focus-color: transparent; ");
    }

    public TableView getTableView(int index) {
        return tableViews[index];
    }

    public void scrollTableViewTo(int index, int row) {
        TableView tableView = tableViews[index];
        tableView.getSelectionModel().select(row);
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
