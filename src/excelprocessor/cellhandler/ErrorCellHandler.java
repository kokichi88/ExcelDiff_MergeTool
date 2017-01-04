package excelprocessor.cellhandler;

import org.apache.poi.ss.usermodel.Cell;

/**
 * Created by apple on 1/2/17.
 */
public class ErrorCellHandler implements ICellHandler<Byte> {
    @Override
    public Byte getValue(Cell cell) {
        return cell.getErrorCellValue();
    }

    @Override
    public void setValue(Cell cell, Byte value) {
        cell.setCellValue(value);
    }
}
