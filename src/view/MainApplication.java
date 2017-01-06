package view;

import excelprocessor.cmd.LoadWorkbookCommand;
import excelprocessor.cmd.WriteLogCmd;
import bus.controller.BusManager;
import excelprocessor.cmd.ChangeTabCommand;
import excelprocessor.signals.ChangeTabSignal;
import excelprocessor.signals.LoadWorkbookSignal;
import excelprocessor.signals.PushLogSignal;
import controller.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.Services;

import java.awt.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;


/**
 * Created by apple on 12/30/16.
 */
public class MainApplication extends Application {
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
        stage.setTitle("None File- Excel Compare");
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width =(int) (gd.getDisplayMode().getWidth() * ratio);
        int height =(int) (gd.getDisplayMode().getHeight() * ratio);
        stage.setScene(new Scene(root, width, height));
        stage.show();

        initController(loader);
    }

    private void initCmds() {
        BusManager busManager = new BusManager();
        Services.setService(busManager);
        try {
            busManager.registerCommand(ChangeTabSignal.class, ChangeTabCommand.class);
            busManager.registerCommand(LoadWorkbookSignal.class, LoadWorkbookCommand.class);
            busManager.registerCommand(PushLogSignal.class, WriteLogCmd.class);
        } catch (Exception e) {
            Services.getService(Logger.class).error(e.getMessage());
        }

    }

    private void initExecutor() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5);
        Services.setService(executor);
    }

    private void initController(FXMLLoader loader) {
        MainController controller = loader.getController();
        Services.setService(controller);
    }

    private void initLogger() {
        Services.setService(LoggerFactory.getLogger(MainApplication.class));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
