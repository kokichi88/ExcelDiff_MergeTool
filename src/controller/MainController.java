package controller;

import bus.controller.BusManager;
import excelprocessor.signals.ChangeTabSignal;
import excelprocessor.signals.LoadWorkbookSignal;
import excelprocessor.workbook.WorkbookWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import kk.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.Services;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

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
    private Label outputContent;

    private WorkbookWrapper[] workbooks = new WorkbookWrapper[MAX_FILE];
    private TableView<ObservableList<String>>[] tableViews = new TableView[MAX_FILE];
    private TabPane[] tabPanes = new TabPane[MAX_FILE];

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTableView();
        initTabPane();
        display = outputContent;

        Services.getService(BusManager.class).dispatch(
                new LoadWorkbookSignal(OLD_FILE_INDEX, Utils.getCurrentWorkingDir() + File.separator + "resources"
                        + File.separator + "HeroConfig.xlsx", this));

    }

    public void setWorkbooks(int index, WorkbookWrapper wb) {
        assert index < MAX_FILE : "index must be lesser than " + MAX_FILE;
        workbooks[index] = wb;
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


    private void initTableView() {
        for(int i = 0; i < tableViews.length; ++i) {
            tableViews[i] = new TableView<ObservableList<String>>();
        }
    }

    private void initTabPane() {
        assert oldTabPane != null : "can't load fx:id=oldTabPane";
        tabPanes[OLD_FILE_INDEX] = oldTabPane;
        for(int i = 0; i < tabPanes.length; ++i) {
            if(tabPanes[i] != null) {
                TabPane tabPane = tabPanes[i];
                tabPane.setId(String.valueOf(i));
                tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                    @Override
                    public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                        ChangeTabSignal msg = new ChangeTabSignal(workbooks, tableViews, newValue, oldValue);
                        Services.getService(BusManager.class).dispatch(msg);
                    }
                });
            }
        }
    }

}
