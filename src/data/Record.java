package data;

import javafx.collections.ObservableList;

/**
 * Created by apple on 1/10/17.
 */
public class Record<T> {
    public ObservableList<CellValue<T>> cells;

    public Record(ObservableList<CellValue<T>> cells) {
        this.cells = cells;
    }
}
