package excelprocessor.cellhandler;

import org.apache.poi.ss.usermodel.Cell;

/**
 * Created by apple on 1/2/17.
 */
public interface ICellHandler<T> {
    T getValue(Cell cell);
    void setValue(Cell cell, T value);
}
