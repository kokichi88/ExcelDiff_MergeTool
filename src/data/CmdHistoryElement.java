package data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by apple on 1/13/17.
 */
public class CmdHistoryElement {
    public static final String[] HISTORY_COLS = {"Sheet", "Row", "Old Value",
    "New Value", "Description"};

    public static final String[] PROPERTIES = {"sheetName", "startRow", "oldValue",
            "newValue", "desc"};

    private final StringProperty sheetName;
    private final StringProperty startRow;
    private final StringProperty oldValue;
    private final StringProperty newValue;
    private final StringProperty desc;

    public CmdHistoryElement(String sheetName, String startRow, String oldValue, String newValue, String desc) {
        this.sheetName = new SimpleStringProperty(sheetName);
        this.startRow = new SimpleStringProperty(startRow);
        this.oldValue = new SimpleStringProperty(oldValue);
        this.newValue = new SimpleStringProperty(newValue);
        this.desc = new SimpleStringProperty(desc);
    }

    public String getSheetName() {
        return sheetName.get();
    }

    public String getStartRow() {
        return startRow.get();
    }

    public String getOldValue() {
        return oldValue.get();
    }

    public String getNewValue() {
        return newValue.get();
    }

    public String getDesc() {
        return desc.get();
    }

}
