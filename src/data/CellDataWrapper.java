package data;
import org.apache.poi.ss.util.CellAddress;

/**
 * Created by apple on 1/2/17.
 */
public class CellDataWrapper {
    public Object value;
    public CellAddress address;

    public CellDataWrapper(Object value, CellAddress address) {
        this.value = value;
        this.address = address;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            CellDataWrapper other = (CellDataWrapper) obj;
            return this.value.equals(other.value) && this.address.equals(other.address);
        }catch (Exception e) {
            return false;
        }
    }
}
