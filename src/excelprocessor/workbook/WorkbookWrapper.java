package excelprocessor.workbook;

import data.CellDataWrapper;
import data.CellValue;
import data.Record;
import diff.KKString;
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
    private int id;
    private List<Integer> maxColumnsPerSheet = new ArrayList<Integer>();
    private List<ArrayList<String>> columnsNamePerSheet = new ArrayList<ArrayList<String>>();
    private List<String> sheetsName = new ArrayList<String>();
    private List<ArrayList<CellDataWrapper>> cellDataWrappersPerSheet;
    private List<ArrayList<String>> stringValuesPerSheet;
    private List<ObservableList<Record<String>>> rowDatasPerSheet;

    public WorkbookWrapper(String path, int id) throws IOException {
        FileInputStream file = new FileInputStream(new File(path));
        this.workbook = new XSSFWorkbook(file);
        this.path = path;
        this.id = id;
        init();
    }

    public int getId() {
        return id;
    }

    private void init() {
        buildCachedData();
    }

    public int[] getMaxRowAndColumnAtSheet(int index) {

        Sheet sheet = workbook.getSheetAt(index);
        Iterator<Row> rowIterator = sheet.iterator();
        int[] ret = new int[2];
        while(rowIterator.hasNext()) {
            Row row = rowIterator.next();
            int lastCellNum = row.getLastCellNum();
            ret[1] = lastCellNum > ret[1] ? lastCellNum : ret[1];
            ret[0]++;
        }
        return ret;
    }

    private void fill(ObservableList<CellValue<String>> cells, int size) {
        for(int i = 0; i < size; ++i) {
            cells.add(new CellValue<String>(""));
        }
    }

    private void fill(ArrayList<String> strVals, int size) {
        for(int i = 0; i < size; ++i) {
            strVals.add("");
        }
    }

    private void buildCachedData() {
        int numOfSheets = workbook.getNumberOfSheets();
        stringValuesPerSheet = new ArrayList<ArrayList<String>>();
        cellDataWrappersPerSheet = new ArrayList<ArrayList<CellDataWrapper>>();
        rowDatasPerSheet = new ArrayList<ObservableList<Record<String>>>();
        String default1stCell = "#";
        for(int i = 0; i < numOfSheets; ++i) {
            Sheet sheet = workbook.getSheetAt(i);
            Iterator<Row> rowIterator = sheet.iterator();
            int[] maxRowAndCol = getMaxRowAndColumnAtSheet(i);
            ArrayList<String> stringValues = new ArrayList<String>();
            ArrayList<CellDataWrapper> cellDataWrappers = new ArrayList<CellDataWrapper>();
            ObservableList<Record<String>> rowDatas = FXCollections.observableArrayList();
            int countRow = 0;
            fill(stringValues, maxRowAndCol[0] * maxRowAndCol[1]);
            while(rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                ObservableList<CellValue<String>> cells = FXCollections.observableArrayList();
//                record.add(String.valueOf(countRow));
                fill(cells, maxRowAndCol[1] + 1);
                cells.set(0, new CellValue<String>(String.valueOf(countRow + 1), CellValue.CellState.IMMUTABLE));
                while(cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    int colIndex = cell.getAddress().getColumn();
                    Object value = handlers.get(cell.getCellType()).getValue(cell);
                    String strVal = handlers.get(cell.getCellType()).stringValueOf(cell);
                    cells.set(colIndex + 1, new CellValue<String>(strVal));
                    stringValues.set(countRow * maxRowAndCol[1] + colIndex, strVal);
                    cellDataWrappers.add(new CellDataWrapper(value, cell.getAddress()));
                }
                rowDatas.add(new Record<String>(cells));
                countRow++;
            }
            stringValuesPerSheet.add(stringValues);
            cellDataWrappersPerSheet.add(cellDataWrappers);
            rowDatasPerSheet.add(rowDatas);
            maxColumnsPerSheet.add(maxRowAndCol[1]);
            ArrayList<String> columnsName = new ArrayList<String>();
            columnsName.add(default1stCell);
            for(int j = 0; j < maxRowAndCol[1]; ++j) {
                columnsName.add(CellReference.convertNumToColString(j));
            }
            columnsNamePerSheet.add(columnsName);
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

    public KKString<String> getKKStringAtSheet(int sheet) {
        ArrayList<String> datas = stringValuesPerSheet.get(sheet);
        KKString<String> ret = new KKString<String>(datas);
        return ret;
    }

    public List<String> getColumnsAtSheet(int sheet) {
        return columnsNamePerSheet.get(sheet);
    }

    public List<String> getSheetsName() {
        return sheetsName;
    }


    public ObservableList<Record<String>> getRenderDataAtSheet (int sheet) {
        return rowDatasPerSheet.get(sheet);
    }


    public void setCellValue(Cell cell, Object value) {
        handlers.get(cell.getCellType()).setValue(cell, value);
    }
}
