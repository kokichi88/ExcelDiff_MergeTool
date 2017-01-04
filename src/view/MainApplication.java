package view;

import bus.cmd.CmdId;
import bus.controller.BusManager;
import bus.cmd.ChangeTabCommand;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;


/**
 * Created by apple on 12/30/16.
 */
public class MainApplication extends Application {
    private final float ratio = 0.8f;
    final Logger logger = LoggerFactory.getLogger(MainApplication.class);
    @Override
    public void start(Stage stage) throws Exception {
        initCmds();

        Parent root = FXMLLoader.load(getClass().getResource("FirstUIFx.fxml"));
        stage.setTitle("None File- Excel Compare");
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width =(int) (gd.getDisplayMode().getWidth() * ratio);
        int height =(int) (gd.getDisplayMode().getHeight() * ratio);
        stage.setScene(new Scene(root, width, height));
        stage.show();


    }

    private void initCmds() {
        try {
            BusManager.getInstance().registerCommand(CmdId.CHANGE_TAB, ChangeTabCommand.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void main(String[] args) {
        launch(args);
    }
}
