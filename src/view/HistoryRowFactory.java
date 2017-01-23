package view;

import bus.controller.BusManager;
import controller.MainController;
import data.CellValue;
import data.CmdHistoryElement;
import excelprocessor.signals.CmdHistorySelectedSignal;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import services.Services;

/**
 * Created by apple on 1/18/17.
 */
public class HistoryRowFactory implements Callback<TableView<CmdHistoryElement>, TableRow<CmdHistoryElement>> {

    @Override
    public TableRow<CmdHistoryElement> call(TableView<CmdHistoryElement> param) {
        final CmdHistoryRow row = new CmdHistoryRow();
        row.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!row.isEmpty() && event.getButton()==MouseButton.PRIMARY
                        && event.getClickCount() == 1) {
                    CmdHistoryElement element = row.getItem();
                    CmdHistorySelectedSignal signal = new CmdHistorySelectedSignal(Services.get(MainController.class), element);
                    Services.get(BusManager.class).dispatch(signal);
                }
            }
        });

        return row;
    }


    public static class CmdHistoryRow extends TableRow<CmdHistoryElement> {

        protected void updateItem(CmdHistoryElement element, boolean empty) {
            super.updateItem(element, empty);
            ObservableList<String> styleClass = getStyleClass();
            StyleSheetHelper.clearAdditionalStyle(styleClass);
            if(empty) {
                return;
            }
            StyleSheetHelper.addStyle(styleClass, element.getState());
        }
    }
}

