package excelprocessor.cellhandler;

import org.apache.poi.ss.usermodel.Cell;

/**
 * Created by apple on 1/2/17.
 */
public class BooleanCellHandler implements ICellHandler<Boolean> {

    @Override
    public Boolean getValue(Cell cell) {
        return cell.getBooleanCellValue();
    }

    @Override
    public void setValue(Cell cell, Boolean value) {
        cell.setCellValue(value);
    }
}
