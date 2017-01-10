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
    public String stringValueOf(Cell cell) {
        Double value = getValue(cell);
        if ((value == Math.floor(value)) && !Double.isInfinite(value)) {
            Long longVal = value.longValue();
            return longVal.toString();
        }else
            return null;
    }

    @Override
    public void setValue(Cell cell, Double value) {
        cell.setCellValue(value);
    }

}
