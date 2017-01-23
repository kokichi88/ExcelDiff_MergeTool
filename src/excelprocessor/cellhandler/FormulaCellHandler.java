package excelprocessor.cellhandler;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Created by apple on 1/23/17.
 */
public class FormulaCellHandler implements ICellHandler<String> {
    @Override
    public String getValue(Cell cell) {
        return cell.getCellFormula();
    }

    @Override
    public String stringValueOf(Cell cell) {
        Workbook wb = cell.getSheet().getWorkbook();
        FormulaEvaluator formulaEval =  wb.getCreationHelper().createFormulaEvaluator();
        String value= formulaEval.evaluate(cell).formatAsString();
        StringBuilder builder = new StringBuilder();
        builder.append("").append(value).append("\n").append("").append(getValue(cell));
        return builder.toString();
    }

    @Override
    public void setValue(Cell cell, String value) {
        cell.setCellFormula(value);
    }
}
