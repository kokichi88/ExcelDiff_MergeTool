package view;

import data.CellValue;
import javafx.collections.ObservableList;

/**
 * Created by apple on 1/18/17.
 */
public class StyleSheetHelper {

    public static void clearAdditionalStyle(ObservableList<String> styleClasses) {
        styleClasses.remove(CellValue.CellState.ADDED.toString());
        styleClasses.remove(CellValue.CellState.REMOVED.toString());
        styleClasses.remove(CellValue.CellState.MODIFIED.toString());
    }

    public static void addStyle(ObservableList<String> styleClasses, CellValue.CellState state) {
        clearAdditionalStyle(styleClasses);
        styleClasses.add(state.toString());
    }

    public static void addStyle(ObservableList<String> styleClasses, String style) {
        clearAdditionalStyle(styleClasses);
        styleClasses.add(style);
    }

    public static void setStyle(ObservableList<String> styleClasses, String style) {
        styleClasses.clear();
        styleClasses.add(style);
    }
}
