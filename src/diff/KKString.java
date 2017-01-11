package diff;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by apple on 1/11/17.
 */
public class KKString<T> {
    Class<T> clazz;
    private final T[] value;

    public KKString() {
        value = (T[])new Object[0];
    }

    public KKString(Class<T> clazz) {
        this.clazz = clazz;
        value = (T[])Array.newInstance(clazz,0);
    }

    public KKString(Class<T> clazz, T... values) {
        this.clazz = clazz;
        value = values;
    }

    public KKString(KKString<T> kkString) {
        clazz = kkString.clazz;
        value = kkString.value;
    }

    public KKString(Class<T> clazz, T[] value, int offset, int count) {
        if (offset < 0) {
            throw new StringIndexOutOfBoundsException(offset);
        }
        if (count <= 0) {
            if (count < 0) {
                throw new StringIndexOutOfBoundsException(count);
            }
            if (offset <= value.length) {
                this.value = emptyValue(clazz);
                return;
            }
        }
        // Note: offset or count might be near -1>>>1.
        if (offset > value.length - count) {
            throw new StringIndexOutOfBoundsException(offset + count);
        }
        this.value = Arrays.copyOfRange(value, offset, offset+count);
    }

    public T charAt(int index) {
        if ((index < 0) || (index >= value.length)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return value[index];
    }

    public KKString<T> substring(int beginIndex) {
        if (beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(beginIndex);
        }
        int subLen = value.length - beginIndex;
        if (subLen < 0) {
            throw new StringIndexOutOfBoundsException(subLen);
        }
        return (beginIndex == 0) ? this : new KKString<T>(clazz, value, beginIndex, subLen);
    }

    public KKString<T> substring(int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(beginIndex);
        }
        if (endIndex > value.length) {
            throw new StringIndexOutOfBoundsException(endIndex);
        }
        int subLen = endIndex - beginIndex;
        if (subLen < 0) {
            throw new StringIndexOutOfBoundsException(subLen);
        }
        return ((beginIndex == 0) && (endIndex == value.length)) ? this
                : new KKString<T>(clazz, value, beginIndex, subLen);
    }


    void getChars(T[] dst, int dstBegin) {
        System.arraycopy(value, 0, dst, dstBegin, value.length);
    }

    public KKString<T> concat(KKString<T> str) {
        int otherLen = str.length();
        if (otherLen == 0) {
            return this;
        }
        int len = value.length;
        T[] buf = Arrays.copyOf(value, len + otherLen);
        str.getChars(buf, len);
        return new KKString<T>(this.clazz, buf);
    }

    public KKString<T> concat(T element) {
        return concat(new KKString<T>(clazz, element));
    }

    public KKString<T> addFirst(T element) {
        return new KKString<T>(clazz, element).concat(this);
    }

    public boolean startsWith(KKString<T> prefix, int toffset) {
        T ta[] = value;
        int to = toffset;
        T pa[] = prefix.value;
        int po = 0;
        int pc = prefix.value.length;
        // Note: toffset might be near -1>>>1.
        if ((toffset < 0) || (toffset > value.length - pc)) {
            return false;
        }
        while (--pc >= 0) {
            if (!ta[to++].equals(pa[po++])) {
                return false;
            }
        }
        return true;
    }

    public boolean startsWith(KKString<T> prefix) {
        return startsWith(prefix, 0);
    }

    public boolean endsWith(KKString<T> suffix) {
        return startsWith(suffix, value.length - suffix.value.length);
    }

    public int indexOf(KKString<T> str) {
        return indexOf(str, 0);
    }

    public int indexOf(KKString<T> str, int fromIndex) {
        return indexOf(value, 0, value.length,
                str.value, 0, str.value.length, fromIndex);
    }

    static <T> int indexOf(T[] source, int sourceOffset, int sourceCount,
                       T[] target, int targetOffset, int targetCount,
                       int fromIndex) {
        if (fromIndex >= sourceCount) {
            return (targetCount == 0 ? sourceCount : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }

        T first = target[targetOffset];
        int max = sourceOffset + (sourceCount - targetCount);

        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (source[i] != first) {
                while (++i <= max && source[i].equals(first));
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = targetOffset + 1; j < end &&
                        source[j].equals(target[k]); j++, k++);

                if (j == end) {
                    /* Found whole string. */
                    return i - sourceOffset;
                }
            }
        }
        return -1;
    }

    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof KKString) {
            KKString<T> anotherString = (KKString<T>)anObject;
            return Arrays.equals(this.value, anotherString.value);
        }
        return false;
    }

    public int length() {
        return value.length;
    }

    KKString<T> empty() {
        return new KKString<T>(clazz);
    }

    private T[] emptyValue(Class<T> clazz) {
        return (T[])Array.newInstance(clazz,0);
    }

}
