package excelprocessor.cellhandler;

import org.apache.poi.ss.usermodel.Cell;

/**
 * Created by apple on 1/2/17.
 */
public class StringCellHandler implements ICellHandler<String> {
    @Override
    public String getValue(Cell cell) {
        return cell.getStringCellValue();
    }

    @Override
    public String stringValueOf(Cell cell) {
        return getValue(cell);
    }

    @Override
    public void setValue(Cell cell, String value) {
        cell.setCellValue(value);
    }
}
