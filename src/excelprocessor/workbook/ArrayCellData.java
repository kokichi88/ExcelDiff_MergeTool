package excelprocessor.workbook;

import org.apache.poi.ss.util.CellAddress;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by apple on 1/2/17.
 */
public class ArrayCellData {
    String[] cellRenderValues;
    Object[] rawCellValues;
    List<Integer> cellTypes;
    List<CellAddress> cellAddresses;

    public ArrayCellData(String[] cellRenderValues, Object[] rawCellValues, List<Integer> cellTypes, List<CellAddress> cellAddresses) {
        this.cellRenderValues = cellRenderValues;
        this.rawCellValues = rawCellValues;
        this.cellTypes = cellTypes;
        this.cellAddresses = cellAddresses;
    }

    public String[] getCellRenderValues() {
        return Arrays.copyOf(cellRenderValues, cellRenderValues.length);
    }

    public Object[] getRawCellValues() {
        return Arrays.copyOf(rawCellValues, rawCellValues.length);
    }

    public List<Integer> getCellTypes() {
        return Collections.unmodifiableList(cellTypes);
    }

    public List<CellAddress> getCellAddresses() {
        return Collections.unmodifiableList(cellAddresses);
    }
}
