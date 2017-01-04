package excelprocessor.cellhandler;

import org.apache.poi.ss.usermodel.Cell;

/**
 * Created by apple on 1/2/17.
 */
public class DoubleCellHandler implements ICellHandler<Double> {
    @Override
    public Double getValue(Cell cell) {
        return cell.getNumericCellValue();
    }

    @Override
    public void setValue(Cell cell, Double value) {
        cell.setCellValue(value);
    }
}
