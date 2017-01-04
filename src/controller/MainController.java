package controller;

import bus.controller.BusManager;
import bus.messages.ChangeTabMessage;
import excelprocessor.workbook.WorkbookWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;
import kk.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static javafx.scene.control.TableColumn.CellDataFeatures;

/**
 * Created by apple on 1/2/17.
 */
public class MainController implements Initializable {
    private Logger logger = LoggerFactory.getLogger(MainController.class);

    public static int MAX_FILE = 2;
    public static int OLD_FILE_INDEX = 0;
    public static int NEW_FILE_INDEX = 1;

    @FXML
    private TableView<ObservableList<String>> oldFileTableView;

    @FXML
    private TabPane oldTabPane;

    @FXML
    private TextField outputContent;

    private WorkbookWrapper[] workbooks = new WorkbookWrapper[MAX_FILE];
    private TableView<ObservableList<String>>[] tableViews = new TableView[MAX_FILE];
    private TabPane[] tabPanes = new TabPane[MAX_FILE];

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadWorkbook(OLD_FILE_INDEX,Utils.getCurrentWorkingDir() + File.separator + "resources"
                + File.separator + "HeroConfig.xlsx" );
        loadWorkbook(NEW_FILE_INDEX, Utils.getCurrentWorkingDir() + File.separator + "resources"
                + File.separator + "HeroConfig2.xlsx");

        initTableView();
        initTabPane();
    }

    private void loadWorkbook(int index, String path) {
        assert index < MAX_FILE : "index must be lesser than 2";
        try {
            workbooks[index] = new WorkbookWrapper(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initTableView() {
        for(int i = 0; i < tableViews.length; ++i) {
            tableViews[i] = new TableView<ObservableList<String>>();
        }
    }

    private void loadTableView(int index, int sheet) {

    }

    private void initTabPane() {
        assert oldTabPane != null : "can't load fx:id=oldTabPane";
        tabPanes[OLD_FILE_INDEX] = oldTabPane;
        for(int i = 0; i < tabPanes.length; ++i) {
            if(tabPanes[i] != null && workbooks[i] != null) {
                TabPane tabPane = tabPanes[i];
                tabPane.getTabs().clear();
                List<String> sheetsName = workbooks[i].getSheetsName();
                for(int j = 0; j < sheetsName.size(); ++j) {
                    Tab tab = new Tab(sheetsName.get(j));
                    tab.setId(String.valueOf(j));
                    tabPane.getTabs().add(tab);
                }
                if(tabPane.getTabs().size() > 0) {
                    ChangeTabMessage msg = new ChangeTabMessage(workbooks, tabPanes, tableViews,
                            tabPane.getTabs().get(0), tabPane.getTabs().get(0));
                    BusManager.getInstance().dispatch(msg);
                }
                tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                    @Override
                    public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                        ChangeTabMessage msg = new ChangeTabMessage(workbooks, tabPanes, tableViews, newValue, oldValue);
                        BusManager.getInstance().dispatch(msg);
                    }
                });
            }
        }


    }

}
