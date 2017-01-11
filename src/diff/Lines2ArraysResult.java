package diff;

import java.util.List;

/**
 * Created by apple on 1/11/17.
 */
public class Lines2ArraysResult<T> {
    public String arr1;
    public String arr2;
    protected List<T> lineArray;

    protected Lines2ArraysResult(String arr1, String arr2, List<T> lineArray) {
        this.arr1 = arr1;
        this.arr2 = arr2;
        this.lineArray = lineArray;
    }
}
