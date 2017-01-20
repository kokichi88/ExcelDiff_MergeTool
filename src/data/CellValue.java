package data;

import excelprocessor.workbook.WorkbookWrapper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import view.StyleSheetHelper;


/**
 * Created by apple on 1/9/17.
 */
public class CellValue<T> {
    public enum CellState {
        IMMUTABLE,
        UNCHANGED,
        ADDED,
        REMOVED,
        MODIFIED
    }
    private final T value;
    private final ObjectProperty<CellState> cellStateProperty = new SimpleObjectProperty<CellState>();

    public CellValue(T value) {
        this.value = value;
        setCellState(CellState.UNCHANGED);
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
        final TextFieldTableCell<Record<String>, CellValue<T>> cell = new RecordCell<T>();
        cell.setConverter(new StringConverter<CellValue<T>>() {
            @Override
            public String toString(CellValue<T> item) {
                if(item == null) {
                    return "";
                }else {
                    return item.getValue().toString();
                }
            }

            @Override
            public CellValue<T> fromString(String string) {
                try {
                    T value = clazz.getConstructor(String.class).newInstance(string);
                    CellState cellState = cell.getItem() == null ? CellState.UNCHANGED : cell.getItem().getCellState() ;
                    return new CellValue<T>(value, cellState);
                }catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });
        return cell;
    }

    public static class RecordCell<T> extends TextFieldTableCell<Record<String>, CellValue<T>> {

        @Override
        public void updateItem(CellValue<T> value, boolean empty) {
            super.updateItem(value, empty);
            if(!empty) {
                setCellStyle(this, value.getCellState());
            }
        }
    }

    public static void setCellStyle(TableCell cell, CellState newValue) {
        ObservableList<String> styleClass = cell.getStyleClass();
        StyleSheetHelper.clearCellAdditionalStyle(styleClass);
        if(newValue == null) {
        }else {
            switch (newValue) {
                case UNCHANGED:
                    break;
                default :
                    StyleSheetHelper.addStyle(styleClass, newValue.toString() + "-CELL");
                    break;
            }

        }
    }


}
