package data;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by apple on 1/13/17.
 */
public class CmdHistoryElement {
    public static final String[] HISTORY_COLS = {"Sheet Name", "Old Row", "New Row",  "Old Value",
    "New Value", "Description"};

    public static final String[] PROPERTIES = {"sheetName", "oldRow", "newRow", "oldValue",
            "newValue", "desc"};

    private int sheetid;
    private int oldCol;
    private int newCol;
    private CellValue.CellState state;
    private final StringProperty sheetName;
    private final IntegerProperty oldRow;
    private final IntegerProperty newRow;
    private final StringProperty oldValue;
    private final StringProperty newValue;
    private final StringProperty desc;

    public CmdHistoryElement(int sheetid, String sheetName, int oldRow, int newRow, int oldCol, int newCol, String oldValue, String newValue, CellValue.CellState state, String desc) {
        this.sheetid = sheetid;
        this.oldCol = oldCol;
        this.newCol = newCol;
        this.sheetName = new SimpleStringProperty(sheetName);
        this.oldRow = new SimpleIntegerProperty(oldRow);
        this.newRow = new SimpleIntegerProperty(newRow);
        this.oldValue = new SimpleStringProperty(oldValue);
        this.newValue = new SimpleStringProperty(newValue);
        this.state = state;
        this.desc = new SimpleStringProperty(desc);
    }

    public String getSheetName() {
        return sheetName.get();
    }

    public int getOldRow() {
        return oldRow.get() + 1;
    }

    public int getNewRow() {
        return newRow.get() + 1;
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

    public int getSheetid() {
        return sheetid;
    }

    public CellValue.CellState getState() {
        return state;
    }

    public int getOldCol() {
        return oldCol;
    }

    public int getNewCol() {
        return newCol;
    }

    public boolean equals(Object other) {
        if(other instanceof CmdHistoryElement) {
            CmdHistoryElement otherE = (CmdHistoryElement) other;
            return this.getSheetid() == otherE.getSheetid() &&
                    this.getState() == otherE.getState() &&
                    this.getSheetName().equals(otherE.getSheetName()) &&
                    this.getOldRow() == otherE.getOldRow() &&
                    this.getNewRow() == otherE.getNewRow() &&
                    this.getOldCol() == otherE.getOldCol() &&
                    this.getNewCol() == otherE.getNewCol() &&
                    this.getOldValue().equals(otherE.getOldValue()) &&
                    this.getNewValue().equals(otherE.getNewValue());
        }else {
            return false;
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getSheetid());
        builder.append(" ");
        builder.append(getSheetName());
        builder.append(" ");
        builder.append(getOldRow());
        builder.append(" ");
        builder.append(getNewRow());
        builder.append(" ");
        builder.append(getOldCol());
        builder.append(" ");
        builder.append(getNewCol());
        builder.append(" ");
        builder.append(getOldValue());
        builder.append(" ");
        builder.append(getNewValue());
        builder.append(" ");
        builder.append(getState());
        return builder.toString();
    }

}
