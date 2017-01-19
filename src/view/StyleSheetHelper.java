package view;

import data.CellValue;
import javafx.collections.ObservableList;

/**
 * Created by apple on 1/18/17.
 */
public class StyleSheetHelper {

    public static void clearAdditionalStyle(ObservableList<String> styleClasses) {
        for(CellValue.CellState cellState : CellValue.CellState.values()) {
            styleClasses.remove(cellState.toString());
        }
    }

    public static void clearCellAdditionalStyle(ObservableList<String> styleClasses) {
        for(CellValue.CellState cellState : CellValue.CellState.values()) {
            styleClasses.remove(cellState.toString() + "-CELL");
        }
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
