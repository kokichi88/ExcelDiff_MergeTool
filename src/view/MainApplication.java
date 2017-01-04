package view;

import bus.cmd.CmdId;
import bus.cmd.LoadWorkbookCommand;
import bus.controller.BusManager;
import bus.cmd.ChangeTabCommand;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
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
    final Logger logger = LoggerFactory.getLogger(MainApplication.class);
    @Override
    public void start(Stage stage) throws Exception {
        initCmds();
        initExecutor();

        Parent root = FXMLLoader.load(getClass().getResource("FirstUIFx.fxml"));
        stage.setTitle("None File- Excel Compare");
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width =(int) (gd.getDisplayMode().getWidth() * ratio);
        int height =(int) (gd.getDisplayMode().getHeight() * ratio);
        stage.setScene(new Scene(root, width, height));
        stage.show();
    }

    private void initCmds() {
        BusManager busManager = new BusManager();
        Services.setService(busManager);
        try {
            busManager.registerCommand(CmdId.CHANGE_TAB, ChangeTabCommand.class);
            busManager.registerCommand(CmdId.LOAD_WORKBOOK, LoadWorkbookCommand.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initExecutor() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5);
        Services.setService(executor);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
