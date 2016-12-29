package diff;

import java.util.Arrays;

/**
 * Created by apple on 12/29/16.
 */
public class Diff<T> {
    public ArrayDiff.Operation operation;

    public T[] array;

    public Diff(ArrayDiff.Operation operation, T[] array) {
        if(array == null) throw new IllegalArgumentException("array must be not null");
        // Construct a diff with the specified operation and text.
        this.operation = operation;
        this.array = array;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        builder.append(this.operation);
        builder.append(",");
        builder.append("[");
        for(int i = 0; i < array.length; ++i) {
            builder.append(array[i].toString()).append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]");
        builder.append(")");
        return builder.toString();
    }

    public boolean equals(Object other) {

        try {
            Diff otherDiff = (Diff) other;
            boolean ret = this.operation == otherDiff.operation
                    && Arrays.equals(this.array, otherDiff.array);
            return ret;
        } catch (ClassCastException e) {
            return false;
        }
    }
}
