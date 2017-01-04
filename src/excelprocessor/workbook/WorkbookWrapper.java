package excelprocessor.workbook;

import excelprocessor.cellhandler.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
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

    public WorkbookWrapper(String path) throws IOException {
        FileInputStream file = new FileInputStream(new File(path));
        this.workbook = new XSSFWorkbook(file);
        this.path = path;
        init();
    }

    private void init() {
        buildMaxColumnsData();
    }


    private void buildMaxColumnsData() {
        int numOfSheets = workbook.getNumberOfSheets();
        for(int i = 0; i < numOfSheets; ++i) {
            Sheet sheet = workbook.getSheetAt(i);
            Iterator<Row> rowIterator = sheet.iterator();
            int maxColumn = -1;
            while(rowIterator.hasNext()) {
                Row row = rowIterator.next();
                int lastColumn = row.getLastCellNum() - 1;
                maxColumn = maxColumn < lastColumn ? lastColumn : maxColumn;
            }
            maxColumnsPerSheet.add(maxColumn);
            ArrayList<String> columnsName = new ArrayList<String>();
            for(int j = 0; j < maxColumn; ++j) {
                columnsName.add(CellReference.convertNumToColString(j));
            }
            columnsPerSheet.add(columnsName);
            sheetsName.add(sheet.getSheetName());
        }
    }

    public List<String> getColumnsAtSheet(int index) {
        return columnsPerSheet.get(index);
    }

    public List<String> getSheetsName() {
        return sheetsName;
    }


    public ObservableList<ObservableList<String>> getRenderDataAtSheet (int index) {
        Sheet sheet = workbook.getSheetAt(index);
        ObservableList<ObservableList<String>> ret = FXCollections.observableArrayList();
        Iterator<Row> rowIterator = sheet.iterator();
        while(rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            ObservableList<String> record = FXCollections.observableArrayList();
            while(cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                Object value = handlers.get(cell.getCellType()).getValue(cell);
                record.add(value.toString());
            }
            ret.add(record);
        }
        return ret;
    }

    public ArrayCellData getArrayCellValuesAtSheet(int index) {
        Sheet sheet = workbook.getSheetAt(index);
        List<String> renderCellValues = new LinkedList<String>();
        List<Object> rawCellValues = new LinkedList<Object>();
        List<Integer> listCellTypes = new ArrayList<Integer>();
        List<CellAddress> listCellAddress = new ArrayList<CellAddress>();
        Iterator<Row> rowIterator = sheet.iterator();
        while(rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            while(cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                Object value = handlers.get(cell.getCellType()).getValue(cell);
                renderCellValues.add(value.toString());
                rawCellValues.add(value);
                listCellTypes.add(cell.getCellType());
                listCellAddress.add(cell.getAddress());
            }
        }

        ArrayCellData ret = new ArrayCellData(renderCellValues.toArray(new String[]{}),
                rawCellValues.toArray(),
                listCellTypes, listCellAddress);
        return ret;
    }

    public void setCellValue(Cell cell, Object value) {
        handlers.get(cell.getCellType()).setValue(cell, value);
    }
}
