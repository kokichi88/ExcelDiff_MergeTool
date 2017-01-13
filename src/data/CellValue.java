package data;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import services.Services;


/**
 * Created by apple on 1/9/17.
 */
public class CellValue<T> {
    public enum CellState {
        UNCHANCED,
        ADDED,
        REMOVED,
        MODIFIED
    }
    private final T value;
    private final ObjectProperty<CellState> cellStateProperty = new SimpleObjectProperty<CellState>();

    public CellValue(T value) {
        this.value = value;
    }

    public CellValue(T value, CellState state) {
        this(value);
        setCellState(state);
    }

    public T getValue() {
        return value;
    }

    public final ObjectProperty<CellState> getStateProperty() {
        return cellStateProperty;
    }

    public final CellState getCellState() {
        return cellStateProperty.get();
    }

    public final void setCellState(CellState state) {
        cellStateProperty.setValue(state);
    }

    public static <T> TableCell<Record<String>, CellValue<T>> createTableCell(final Class<T> clazz){
        final TextFieldTableCell<Record<String>, CellValue<T>> cell = new TextFieldTableCell<Record<String>, CellValue<T>>();
        cell.setConverter(new StringConverter<CellValue<T>>() {
            @Override
            public String toString(CellValue<T> item) {
                return item == null ? "" : item.getValue().toString();
            }

            @Override
            public CellValue<T> fromString(String string) {
                try {
                    T value = clazz.getConstructor(String.class).newInstance(string);
                    CellState cellState = cell.getItem() == null ? CellState.UNCHANCED : cell.getItem().getCellState() ;
                    return new CellValue<T>(value, cellState);
                }catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });
        cell.itemProperty().addListener(new ChangeListener<CellValue<T>>() {
            @Override
            public void changed(ObservableValue<? extends CellValue<T>> observable, CellValue<T> oldValue, CellValue<T> newValue) {
                if(newValue != null)
                    setCellStyle(cell, newValue.getCellState());
            }
        });
        return cell;
    }

    public static <T> void setCellStyle(TextFieldTableCell<Record<String>, CellValue<T>> cell, CellState newValue) {
        if(newValue == null) {
            cell.setStyle("");
        }else {
            switch (newValue) {
                case UNCHANCED:
                    cell.setStyle("");
                    break;
                case ADDED:
                    cell.setStyle("-fx-background-color: #7fe084 ;");
                    break;
                case REMOVED:
                    cell.setStyle("-fx-background-color: #7f9ee0 ;");
                    break;
                case MODIFIED:
                    cell.setStyle("-fx-background-color: #e07f96 ;");
                    break;
            }
        }
    }


}
