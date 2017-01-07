package excelprocessor.workbook;

import data.CellDataWrapper;
import data.CellValue;
import excelprocessor.cellhandler.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by apple on 1/2/17.
 */
public class WorkbookWrapper {
    private static Map<Integer, ICellHandler> handlers;
    static {
        handlers = new HashMap<Integer, ICellHandler>();
        handlers.put(Cell.CELL_TYPE_NUMERIC, new DoubleCellHandler());
        handlers.put(Cell.CELL_TYPE_STRING, new StringCellHandler());
        handlers.put(Cell.CELL_TYPE_FORMULA, new StringCellHandler());
        handlers.put(Cell.CELL_TYPE_BLANK, new StringCellHandler());
        handlers.put(Cell.CELL_TYPE_BOOLEAN, new BooleanCellHandler());
        handlers.put(Cell.CELL_TYPE_ERROR, new ErrorCellHandler());
    }
    private Workbook workbook;
    private String path;
    private List<Integer> maxColumnsPerSheet = new ArrayList<Integer>();
    private List<ArrayList<String>> columnsPerSheet = new ArrayList<ArrayList<String>>();
    private List<String> sheetsName = new ArrayList<String>();
    private List<ArrayList<CellDataWrapper>> cellDataWrappersPerSheet;
    private List<ArrayList<String>> stringValuesPerSheet;
    private List<ObservableList<ObservableList<CellValue<String>>>> rowDatasPerSheet;

    public WorkbookWrapper(String path) throws IOException {
        FileInputStream file = new FileInputStream(new File(path));
        this.workbook = new XSSFWorkbook(file);
        this.path = path;
        init();
    }

    private void init() {
        buildCachedData();
    }


    private void buildCachedData() {
        int numOfSheets = workbook.getNumberOfSheets();
        stringValuesPerSheet = new ArrayList<ArrayList<String>>();
        cellDataWrappersPerSheet = new ArrayList<ArrayList<CellDataWrapper>>();
        rowDatasPerSheet = new ArrayList<ObservableList<ObservableList<CellValue<String>>>>();
        String default1stCell = "      ";
        for(int i = 0; i < numOfSheets; ++i) {
            Sheet sheet = workbook.getSheetAt(i);
            Iterator<Row> rowIterator = sheet.iterator();
            int maxColumn = -1;
            ArrayList<String> stringValues = new ArrayList<String>();
            ArrayList<CellDataWrapper> cellDataWrappers = new ArrayList<CellDataWrapper>();
            ObservableList<ObservableList<CellValue<String>>> rowDatas = FXCollections.observableArrayList();
            int countRow = 0;
            while(rowIterator.hasNext()) {
                Row row = rowIterator.next();
                int lastColumn = row.getLastCellNum() - 1;
                maxColumn = maxColumn < lastColumn ? lastColumn : maxColumn;
                Iterator<Cell> cellIterator = row.cellIterator();
                ObservableList<CellValue<String>> record = FXCollections.observableArrayList();
//                record.add(String.valueOf(countRow));
                while(cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    Object value = handlers.get(cell.getCellType()).getValue(cell);
                    String strVal = value.toString();
                    record.add(new CellValue<String>(strVal));
                    stringValues.add(strVal);
                    cellDataWrappers.add(new CellDataWrapper(value, cell.getAddress()));
                }
                rowDatas.add(record);
                countRow++;
            }
            stringValuesPerSheet.add(stringValues);
            cellDataWrappersPerSheet.add(cellDataWrappers);
            rowDatasPerSheet.add(rowDatas);
            maxColumnsPerSheet.add(maxColumn);
            ArrayList<String> columnsName = new ArrayList<String>();
//            columnsName.add(default1stCell);
            for(int j = 0; j < maxColumn; ++j) {
                columnsName.add(CellReference.convertNumToColString(j));
            }
            columnsPerSheet.add(columnsName);
            sheetsName.add(sheet.getSheetName());
        }
    }

    public int getDefaultSheetIndex() {
        if(workbook.getNumberOfSheets() > 0)
            return 0;
        else
            return -1;
    }

    public String[] getDataForDiffAtSheet(int sheet) {
        ArrayList<String> datas = stringValuesPerSheet.get(sheet);
        return datas.toArray(new String[]{});
    }

    public List<String> getColumnsAtSheet(int sheet) {
        return columnsPerSheet.get(sheet);
    }

    public List<String> getSheetsName() {
        return sheetsName;
    }


    public ObservableList<ObservableList<CellValue<String>>> getRenderDataAtSheet (int sheet) {
        return rowDatasPerSheet.get(sheet);
    }


    public void setCellValue(Cell cell, Object value) {
        handlers.get(cell.getCellType()).setValue(cell, value);
    }
}
