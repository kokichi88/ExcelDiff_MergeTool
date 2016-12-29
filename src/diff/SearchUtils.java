package diff;

import java.util.Arrays;

/**
 * Created by apple on 12/29/16.
 */
public class SearchUtils {
    public static <T> int[] kmp_build_tablelookup(T[] arr) {
        int[] table = new int[arr.length];
        Arrays.fill(table, 0);
        if(arr.length > 0) {
            table[0] = -1;
            if(arr.length > 1) {
                table[1] = 0;
                int i = 2;
                int count = 0;
                while(i < arr.length) {
                    if(arr[i-1].equals(arr[count])) {
                        ++count;
                        table[i] = count;
                        ++i;
                    }else if (count > 0) {
                        count = table[count];
                    }else {
                        table[i] = 0;
                        count = 0;
                        ++i;
                    }
                }
            }
        }
        return table;
    }

    public static <T> int kmp_search(T[] longArr, T[] shortArr, int begin) {
        int longArrLen = longArr.length;
        int shortArrLen = shortArr.length;
        int[] table = kmp_build_tablelookup(shortArr);
        int i = begin;
        int count = 0;
        while(i + count < longArrLen) {
            if(longArr[i + count].equals(shortArr[count])) {
                if(count == shortArrLen - 1) {
                    return i;
                }
                ++count;
            }else if(table[count] > -1) {
                i = i + count - table[count];
                count = table[count];
            }else {
                count = 0;
                ++i;
            }
        }
        return -1;
    }


}
