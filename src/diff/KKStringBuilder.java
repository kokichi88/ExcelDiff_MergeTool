package diff;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by apple on 1/12/17.
 */
public class KKStringBuilder<T> {
    int size = 0;
    List<Object[]> values;

    public KKStringBuilder() {
        values = new ArrayList<Object[]>();
    }

    public KKStringBuilder<T> append(KKString<T> str) {
        values.add(str.value);
        size += str.value.length;
        return this;
    }

    public KKString<T> toKKString() {
        Object[] arr = new Object[size];
        int index = -1;
        for(Object[] value : values) {
            for(Object obj : value) {
                ++index;
                arr[index] = obj;
            }
        }
        return new KKString<T>((T[])arr);
    }
 }
