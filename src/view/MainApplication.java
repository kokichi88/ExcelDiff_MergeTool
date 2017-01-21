package view;

import diff.ArrayUtils;
import excelprocessor.cmd.*;
import bus.controller.BusManager;
import excelprocessor.signals.*;
import controller.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import kk.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.Services;

import java.awt.*;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;


/**
 * Created by apple on 12/30/16.
 */
public class MainApplication extends Application {
    private static boolean isCustomLoadFile = false;
    private static String file1;
    private static String file2;
    private final float ratio = 0.8f;
    @Override
    public void start(Stage stage) throws Exception {
        initLogger();
        initCmds();
        initExecutor();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FirstUIFx.fxml"));
        Parent root = loader.load();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.exit();
                System.exit(0);
            }
        });
        stage.setTitle("Excel Compare");
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width =(int) (gd.getDisplayMode().getWidth() * ratio);
        int height =(int) (gd.getDisplayMode().getHeight() * ratio);
        stage.setScene(new Scene(root, width, height));
        stage.getScene().getStylesheets().add(getClass().getResource("table.css").toExternalForm());
        stage.show();

        initController(loader);

//        Services.get(BusManager.class).dispatch(
//                new LoadWorkbookSignal(MainController.OLD_FILE_INDEX, Utils.getCurrentWorkingDir() + File.separator + "resources"
//                        + File.separator + "TestAccount1.xlsx", Services.get(MainController.class)));
//
//        Services.get(BusManager.class).dispatch(
//                new LoadWorkbookSignal(MainController.NEW_FILE_INDEX, Utils.getCurrentWorkingDir() + File.separator + "resources"
//                        + File.separator + "TestAccount2.xlsx", Services.get(MainController.class)));
        if(isCustomLoadFile) {
            Services.get(BusManager.class).dispatch(
                    new LoadWorkbookSignal(MainController.OLD_FILE_INDEX, file1, Services.get(MainController.class)));
            Services.get(BusManager.class).dispatch(
                    new LoadWorkbookSignal(MainController.NEW_FILE_INDEX, file2, Services.get(MainController.class)));
        }

    }

    private void initCmds() {
        BusManager busManager = new BusManager();
        Services.set(busManager);
        try {
            busManager.register(ChangeTabSignal.class, ChangeTabCommand.class);
            busManager.register(LoadWorkbookSignal.class, LoadWorkbookCommand.class);
            busManager.register(PushLogSignal.class, WriteLogCmd.class);
            busManager.register(UpdateTabPaneSignal.class, UpdateTabPaneCmd.class);
            busManager.register(DiffSignal.class, DiffCommand.class);
            busManager.register(CmdHistorySelectedSignal.class, FocusRowCommand.class);
        } catch (Exception e) {
            Services.get(Logger.class).error(e.getMessage());
        }

    }

    private void initExecutor() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5);
        Services.set(executor);
    }

    private void initController(FXMLLoader loader) {
        MainController controller = loader.getController();
        Services.set(controller);
    }

    private void initLogger() {
        Services.bind(Logger.class, LoggerFactory.getLogger(MainApplication.class));
    }

    public static void main(String[] args) {
        Map<String, String> mapArgs = Utils.parseProgramArguments(args);
        file1 = Utils.getProgramArguments(mapArgs, "file1");
        file2 = Utils.getProgramArguments(mapArgs, "file2");

        if(file1 != null && file2 != null) {
            isCustomLoadFile = true;
        }
        launch(args);
    }
}
