package view;

import data.CellValue;
import data.CmdHistoryElement;
import javafx.collections.ObservableList;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;

/**
 * Created by apple on 1/18/17.
 */
public class HistoryRowFactory implements Callback<TableView<CmdHistoryElement>, TableRow<CmdHistoryElement>> {

    @Override
    public TableRow<CmdHistoryElement> call(TableView<CmdHistoryElement> param) {
        CmdHistoryRow row = new CmdHistoryRow();
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

