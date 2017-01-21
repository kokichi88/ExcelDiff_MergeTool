package excelprocessor.cmd;

import bus.controller.ICommand;
import excelprocessor.signals.PushLogSignal;
import controller.MainController;
import javafx.application.Platform;
import org.slf4j.Logger;
import services.Services;

/**
 * Created by apple on 1/6/17.
 */
public class WriteLogCmd implements ICommand<PushLogSignal> {

    @Override
    public void execute(PushLogSignal signal) throws Exception{
        final String content = signal.content;
        if(MainController.display != null) {
            Services.get(Logger.class).info(content);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    MainController.display.setText(content);
                }
            });
        }else {
            Services.get(Logger.class).warn("display haven't been initialized yet");
        }
    }
}
