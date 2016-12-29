package diff;

import java.lang.reflect.Array;

/**
 * Created by apple on 12/29/16.
 */
public class ArrayUtils {
    public static <T> String join(String separator, T[] arr) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for(int i = 0; i < arr.length; ++i) {
            builder.append(arr[i]).append(separator);
        }
        builder.replace(builder.length()-1, builder.length(), "]");
        return builder.toString();
    }

    public static <T> boolean isNullOrEmpty(T[] arr) {
        return arr == null || arr.length == 0;
    }

    public static <T> T[] concat(T[] arr1, T[] arr2) {
        if(arr1 == null || arr2 == null)
            throw new IllegalArgumentException("arr1 and arr2 must be not null " + arr1 + ", " + arr2);
        final T[] ret = (T[]) Array.newInstance(arr1.getClass().getComponentType(), arr1.length + arr2.length);
        System.arraycopy(arr1, 0, ret, 0, arr1.length);
        System.arraycopy(arr2, 0, ret, arr1.length, arr2.length);
        return ret;
    }

    public static <T> T[] concat(T element, T[] arr) {
        if(element == null || arr == null)
            throw new IllegalArgumentException("element and arr must be not null " + element + ", " + arr);
        final T[] ret = (T[]) Array.newInstance(element.getClass(), arr.length + 1);
        ret[0] = element;
        System.arraycopy(arr, 0, ret, 1, arr.length);
        return ret;
    }

    public static <T> T[] concat(T[] arr, T element) {
        if(element == null || arr == null)
            throw new IllegalArgumentException("arr and element must be not null " + arr + ", " + element);
        final T[] ret = (T[]) Array.newInstance(element.getClass(), arr.length + 1);
        System.arraycopy(arr, 0, ret, 0, arr.length);
        ret[arr.length] = element;
        return ret;
    }
}
