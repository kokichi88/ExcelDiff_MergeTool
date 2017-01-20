package diff;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by apple on 1/11/17.
 */
public class KKString<T> {
    /**
     * Shared empty array instance used for empty instances.
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};
    private int hash = 0;
    // make value immutable
    final Object[] value;

    public static boolean isNullOrEmpty(KKString kkString) {
        return kkString.value == null || kkString.value.equals(EMPTY_ELEMENTDATA);
    }

    public KKString() {
        value = EMPTY_ELEMENTDATA;
    }

    public KKString(T... values) {
        value = values;
    }

    public KKString(KKString<T> kkString) {
        value = kkString.value;
        hash = kkString.hash;
    }

    public KKString(Collection<? extends T> c) {
        Object[] tmp = c.toArray();
        if (tmp.length != 0) {
            if (tmp.getClass() != Object[].class)
                value = Arrays.copyOf(tmp, tmp.length, Object[].class);
            else
                value = tmp;
        } else {
            this.value = EMPTY_ELEMENTDATA;
        }
    }

    public KKString(T[] value, int offset, int count) {
        if (offset < 0) {
            throw new StringIndexOutOfBoundsException(offset);
        }
        if (count <= 0) {
            if (count < 0) {
                throw new StringIndexOutOfBoundsException(count);
            }
            if (offset <= value.length) {
                this.value = EMPTY_ELEMENTDATA;
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
        return (T)value[index];
    }

    public KKString<T> substring(int beginIndex) {
        if (beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(beginIndex);
        }
        int subLen = value.length - beginIndex;
        if (subLen < 0) {
            throw new StringIndexOutOfBoundsException(subLen);
        }
        return (beginIndex == 0) ? this : new KKString<T>((T[])value, beginIndex, subLen);
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
                : new KKString<T>((T[])value, beginIndex, subLen);
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
        T[] buf = (T[])Arrays.copyOf(value, len + otherLen);
        str.getChars(buf, len);
        return new KKString<T>(buf);
    }

    public KKString<T> concat(T element) {
        return concat(new KKString<T>(element));
    }

    public KKString<T> addFirst(T element) {
        return new KKString<T>(element).concat(this);
    }

    public boolean startsWith(KKString<T> prefix, int toffset) {
        T ta[] = (T[])value;
        int to = toffset;
        T pa[] = (T[])prefix.value;
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
                while (++i <= max && !source[i].equals(first));
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

    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("[");
        for(int i = 0; i < value.length; ++i) {
            builder.append(value[i].toString()).append(", ");
        }
        if(value.length > 0){
            builder.deleteCharAt(builder.length() - 1);
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append("]");
        return builder.toString();
    }

    public int hashCode() {
        int h = hash;
        if (h == 0 && value.length > 0) {
            Object[] val = value;
            for (int i = 0; i < value.length; i++) {
                h = 31 * h + val[i].hashCode();
            }
            hash = h;
        }
        return h;
    }

}
