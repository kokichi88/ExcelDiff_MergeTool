package diff;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by apple on 12/29/16.
 */
public class ArrayDiff<T> {
    /**
     * Number of seconds to map a diff before giving up (0 for infinity).
     */
    public float Diff_Timeout = 1.0f;
    /**
     * Cost of an empty edit operation in terms of edit characters.
     */
    public short Diff_EditCost = 4;
    /**
     * The size beyond which the double-ended diff activates.
     * Double-ending is twice as fast, but less accurate.
     */
    public short Diff_DualThreshold = 32;

    public enum Operation {
        DELETE, INSERT, EQUAL
    }

    private Class<T> clazz;

    public ArrayDiff(Class<T> clazz) {
        this.clazz = clazz;
    }

    public LinkedList<Diff<T>> diff_main(T[] arr1, T[] arr2) {
        LinkedList<Diff<T>> diffs = new LinkedList<Diff<T>>();

        if(ArrayUtils.isNullOrEmpty(arr1) && ArrayUtils.isNullOrEmpty(arr2)) {
            return diffs;
        }else if(ArrayUtils.isNullOrEmpty(arr1)) {
            diffs.add(new Diff(Operation.INSERT, arr2));
            return diffs;
        }else if(ArrayUtils.isNullOrEmpty(arr2)) {
            diffs.add(new Diff(Operation.DELETE, arr1));
            return diffs;
        }

        if(Arrays.equals(arr1, arr2)) {
            diffs.add(new Diff(Operation.EQUAL, arr1));
            return diffs;
        }

        // trim off common prefix
        int commonLength = diff_commonPrefix(arr1, arr2);
        T[] commonPrefix = Arrays.copyOf(arr1, commonLength);
        arr1 = Arrays.copyOfRange(arr1, commonLength, arr1.length);
        arr2 = Arrays.copyOfRange(arr2, commonLength, arr2.length);

        // trim off common suffix
        commonLength = diff_commonSuffix(arr1, arr2);
        T[] commonSuffix = Arrays.copyOfRange(arr1, arr1.length - commonLength, arr1.length);
        arr1 = Arrays.copyOf(arr1, arr1.length - commonLength);
        arr2 = Arrays.copyOf(arr2, arr2.length - commonLength);

        diffs = diff_compute(arr1, arr2);

        if(commonPrefix.length > 0) {
            diffs.addFirst(new Diff(Operation.EQUAL, commonPrefix));
        }

        if(commonSuffix.length > 0) {
            diffs.addLast(new Diff(Operation.EQUAL, commonSuffix));
        }
        diff_cleanupMerge(diffs);
        return diffs;
    }

    public LinkedList<Diff<T>> diff_compute(T[] arr1, T[] arr2) {
        LinkedList<Diff<T>> diffs = new LinkedList<Diff<T>>();

        if(arr1.length == 0) {
            diffs.add(new Diff(Operation.INSERT, arr2));
            return diffs;
        }

        if(arr2.length == 0) {
            diffs.add(new Diff(Operation.DELETE, arr1));
            return diffs;
        }

        T[] longArr = arr1.length > arr2.length ? arr1 : arr2;
        T[] shortArr = arr1.length > arr2.length ? arr2 : arr1;
        int i = SearchUtils.kmp_search(longArr, shortArr, 0);
        if(i > -1) {
            Operation op = arr1.length > arr2.length ? Operation.DELETE : Operation.INSERT;
            diffs.add(new Diff(op, Arrays.copyOf(longArr,i)));
            diffs.add(new Diff(Operation.EQUAL, shortArr));
            diffs.add(new Diff(op, Arrays.copyOfRange(longArr,i + shortArr.length, longArr.length)));
            return diffs;
        }
        longArr = shortArr = null;
        List<T[]> hm = diff_halfMatch(arr1, arr2);
        if(hm != null) {
            T[] arr1_a = hm.get(0);
            T[] arr1_b = hm.get(1);
            T[] arr2_a = hm.get(2);
            T[] arr2_b = hm.get(3);
            T[] mid_common = hm.get(4);

            LinkedList<Diff<T>> diffs_a = diff_main(arr1_a, arr2_a);
            LinkedList<Diff<T>> diffs_b = diff_main(arr1_b, arr2_b);
            diffs = diffs_a;
            diffs.add(new Diff(Operation.EQUAL, mid_common));
            diffs.addAll(diffs_b);
            return diffs;
        }

        // perform a real diff
        diffs = diff_map(arr1, arr2);
        if (diffs == null) {
            // No acceptable result.
            diffs = new LinkedList<Diff<T>>();
            diffs.add(new Diff(Operation.DELETE, arr1));
            diffs.add(new Diff(Operation.INSERT, arr2));
        }
        diff_cleanupSemantic(diffs);
        return diffs;
    }


    public int diff_commonPrefix(T[] arr1, T[] arr2) {
        int n = Math.min(arr1.length, arr2.length);
        for (int i = 0; i < n; i++) {
            if(!arr1[i].equals(arr2[i])) {
                return i;
            }

        }
        return n;
    }

    public int diff_commonSuffix(T[] arr1, T[] arr2) {
        int arr1_length = arr1.length;
        int arr2_length = arr2.length;
        int n = Math.min(arr1_length, arr2_length);
        for (int i = 1; i <= n; i++) {
            if(!arr1[arr1_length - i].equals(arr2[arr2_length - i])) {
                return i - 1;
            }
        }
        return n;
    }

    public void diff_cleanupMerge(LinkedList<Diff<T>> diffs) {
        T[] dummyArray = (T[]) Array.newInstance(clazz, 0);
        diffs.add(new Diff(Operation.EQUAL, dummyArray));  // Add a dummy entry at the end.
        ListIterator<Diff<T>> pointer = diffs.listIterator();
        int count_delete = 0;
        int count_insert = 0;
        T[] arr_delete = (T[]) Array.newInstance(clazz, 0);
        T[] arr_insert = (T[]) Array.newInstance(clazz, 0);
        Diff<T> thisDiff = pointer.next();
        Diff<T> prevEqual = null;
        int commonlength;
        while (thisDiff != null) {
            switch (thisDiff.operation) {
                case INSERT:
                    count_insert++;
                    arr_insert = ArrayUtils.concat(arr_insert, thisDiff.array);
                    prevEqual = null;
                    break;
                case DELETE:
                    count_delete++;
                    arr_delete = ArrayUtils.concat(arr_delete, thisDiff.array);
                    prevEqual = null;
                    break;
                case EQUAL:
                    if (count_delete != 0 || count_insert != 0) {
                        // Delete the offending records.
                        pointer.previous();  // Reverse direction.
                        while (count_delete-- > 0) {
                            pointer.previous();
                            pointer.remove();
                        }
                        while (count_insert-- > 0) {
                            pointer.previous();
                            pointer.remove();
                        }
                        if (count_delete != 0 && count_insert != 0) {
                            // Factor out any common prefixies.
                            commonlength = diff_commonPrefix(arr_insert, arr_delete);
                            if (commonlength != 0) {
                                if (pointer.hasPrevious()) {
                                    thisDiff = pointer.previous();
                                    assert thisDiff.operation == Operation.EQUAL
                                            : "Previous diff should have been an equality.";
                                    thisDiff.array = ArrayUtils.concat(thisDiff.array,
                                            Arrays.copyOfRange(arr_insert, 0, commonlength));
                                    pointer.next();
                                } else {
                                    pointer.add(new Diff(Operation.EQUAL,
                                            Arrays.copyOfRange(arr_insert, 0, commonlength)));
                                }
                                arr_insert = Arrays.copyOfRange(arr_insert, commonlength, arr_insert.length);
                                arr_delete = Arrays.copyOfRange(arr_delete, commonlength, arr_delete.length);
                            }
                            // Factor out any common suffixies.
                            commonlength = diff_commonSuffix(arr_insert, arr_delete);
                            if (commonlength != 0) {
                                thisDiff = pointer.next();
                                thisDiff.array = ArrayUtils.concat(
                                        Arrays.copyOfRange(arr_insert, arr_insert.length - commonlength, arr_insert.length)
                                        , thisDiff.array);
                                arr_insert = Arrays.copyOfRange(arr_insert, 0, arr_insert.length - commonlength);
                                arr_delete = Arrays.copyOfRange(arr_delete, 0, arr_delete.length - commonlength);
                                pointer.previous();
                            }
                        }
                        // Insert the merged records.
                        if (arr_delete.length != 0) {
                            pointer.add(new Diff(Operation.DELETE, arr_delete));
                        }
                        if (arr_insert.length != 0) {
                            pointer.add(new Diff(Operation.INSERT, arr_insert));
                        }
                        // Step forward to the equality.
                        thisDiff = pointer.hasNext() ? pointer.next() : null;
                    } else if (prevEqual != null) {
                        // Merge this equality with the previous one.
                        prevEqual.array = ArrayUtils.concat(prevEqual.array, thisDiff.array);
                        pointer.remove();
                        thisDiff = pointer.previous();
                        pointer.next();  // Forward direction
                    }
                    count_insert = 0;
                    count_delete = 0;
                    arr_delete = (T[]) Array.newInstance(clazz, 0);
                    arr_insert = (T[]) Array.newInstance(clazz, 0);
                    prevEqual = thisDiff;
                    break;
            }
            thisDiff = pointer.hasNext() ? pointer.next() : null;
        }
        // System.out.println(diff);
        if (diffs.getLast().array.length == 0) {
            diffs.removeLast();  // Remove the dummy entry at the end.
        }

    /*
     * Second pass: look for single edits surrounded on both sides by equalities
     * which can be shifted sideways to eliminate an equality.
     * e.g: A<ins>BA</ins>C -> <ins>AB</ins>AC
     */
        boolean changes = false;
        // Create a new iterator at the start.
        // (As opposed to walking the current one back.)
        pointer = diffs.listIterator();
        Diff prevDiff = pointer.hasNext() ? pointer.next() : null;
        thisDiff = pointer.hasNext() ? pointer.next() : null;
        Diff nextDiff = pointer.hasNext() ? pointer.next() : null;
        // Intentionally ignore the first and last element (don't need checking).
        while (nextDiff != null) {
            if (prevDiff.operation == Operation.EQUAL &&
                    nextDiff.operation == Operation.EQUAL) {
                // This is a single edit surrounded by equalities.
                if (ArrayUtils.endsWith(thisDiff.array, prevDiff.array)) {
                    // Shift the edit over the previous equality.
                    thisDiff.array = (T[])ArrayUtils.concat(prevDiff.array,
                                    Arrays.copyOfRange(thisDiff.array, 0,
                                    thisDiff.array.length - prevDiff.array.length));
                    nextDiff.array = ArrayUtils.concat(prevDiff.array, nextDiff.array);
                    pointer.previous(); // Walk past nextDiff.
                    pointer.previous(); // Walk past thisDiff.
                    pointer.previous(); // Walk past prevDiff.
                    pointer.remove(); // Delete prevDiff.
                    pointer.next(); // Walk past thisDiff.
                    thisDiff = pointer.next(); // Walk past nextDiff.
                    nextDiff = pointer.hasNext() ? pointer.next() : null;
                    changes = true;
                } else if (ArrayUtils.startsWith(thisDiff.array, nextDiff.array)) {
                    // Shift the edit over the next equality.
                    prevDiff.array = ArrayUtils.concat(prevDiff.array, nextDiff.array);
                    thisDiff.array = (T[])ArrayUtils.concat(Arrays.copyOfRange(thisDiff.array, nextDiff.array.length,
                            thisDiff.array.length), nextDiff.array);
                    pointer.remove(); // Delete nextDiff.
                    nextDiff = pointer.hasNext() ? pointer.next() : null;
                    changes = true;
                }
            }
            prevDiff = thisDiff;
            thisDiff = nextDiff;
            nextDiff = pointer.hasNext() ? pointer.next() : null;
        }
        // If shifts were made, the diff needs reordering and another shift sweep.
        if (changes) {
            diff_cleanupMerge(diffs);
        }
    }

    public List<T[]> diff_halfMatch(T[] arr1, T[] arr2) {
        T[] longArr = arr1.length > arr2.length ? arr1 : arr2;
        T[] shortArr = arr1.length > arr2.length ? arr2 : arr1;
        if(longArr.length < 10 || shortArr.length < 1) {
            return null;
        }
        List<T[]> hm1 = diff_halfMatchI(longArr, shortArr, (longArr.length + 3)/4);
        List<T[]> hm2 = diff_halfMatchI(longArr, shortArr, (longArr.length + 1)/2);
        List<T[]> hm;
        if (hm1 == null && hm2 == null) {
            return null;
        } else if (hm2 == null) {
            hm = hm1;
        } else if (hm1 == null) {
            hm = hm2;
        } else {
            // Both matched.  Select the longest.
            hm = hm1.get(4).length > hm2.get(4).length ? hm1 : hm2;
        }

        if(arr1.length > arr2.length) {
            return hm;
        }else {
            List<T[]> ret= new ArrayList<T[]>();
            ret.add(hm.get(2));
            ret.add(hm.get(3));
            ret.add(hm.get(0));
            ret.add(hm.get(1));
            ret.add(hm.get(4));
            return ret;
        }
    }

    public List<T[]> diff_halfMatchI(T[] longArr, T[] shortArr, int i) {
        T[] seed = Arrays.copyOfRange(longArr, i, i + longArr.length/4);
        int j = -1;
        T[] best_common = null;
        T[] best_longArr_a = null;
        T[] best_longArr_b = null;
        T[] best_shortArr_a = null;
        T[] best_shortArr_b = null;
        while( (j = SearchUtils.kmp_search(shortArr, seed, j + 1)) != - 1) {
            int prefixLength = diff_commonPrefix(Arrays.copyOfRange(longArr, i, longArr.length),
                    Arrays.copyOfRange(shortArr, j, shortArr.length));
            int suffixLength = diff_commonSuffix(Arrays.copyOfRange(longArr, 0, i),
                    Arrays.copyOfRange(shortArr, 0, j));
            if (ArrayUtils.isNullOrEmpty(best_common) || best_common.length < suffixLength + prefixLength) {
                best_common = ArrayUtils.concat(Arrays.copyOfRange(shortArr, j - suffixLength, j),
                    Arrays.copyOfRange(shortArr, j, j + prefixLength));
                best_longArr_a = Arrays.copyOfRange(longArr, 0, i - suffixLength);
                best_longArr_b = Arrays.copyOfRange(longArr, i + prefixLength, longArr.length);
                best_shortArr_a = Arrays.copyOfRange(shortArr, 0, j - suffixLength);
                best_shortArr_b = Arrays.copyOfRange(shortArr, j + prefixLength, shortArr.length);
            }
        }
        if(!ArrayUtils.isNullOrEmpty(best_common) && best_common.length >= longArr.length / 2) {
            List<T[]> ret = new ArrayList<T[]>();
            ret.add(best_longArr_a);
            ret.add(best_longArr_b);
            ret.add(best_shortArr_a);
            ret.add(best_shortArr_b);
            ret.add(best_common);
            return ret;
        }else {
            return null;
        }
    }

    public LinkedList<Diff<T>> diff_map(T[] arr1, T[] arr2) {
        long ms_end = System.currentTimeMillis() + (long) (Diff_Timeout * 1000);
        int arr1_length = arr1.length;
        int arr2_length = arr2.length;
        int max_d = arr1_length + arr2_length - 1;
        boolean doubleEnd = Diff_DualThreshold * 2 < max_d;
        List<Set<Long>> v_map1 = new ArrayList<Set<Long>>();
        List<Set<Long>> v_map2 = new ArrayList<Set<Long>>();
        Map<Integer, Integer> v1 = new HashMap<Integer, Integer>();
        Map<Integer, Integer> v2 = new HashMap<Integer, Integer>();
        v1.put(1, 0);
        v2.put(1, 0);
        int x, y;
        Long footstep = 0L;  // Used to track overlapping paths.
        Map<Long, Integer> footsteps = new HashMap<Long, Integer>();
        boolean done = false;
        // If the total number of characters is odd, then the front path will
        // collide with the reverse path.
        boolean front = ((arr1_length + arr2_length) % 2 == 1);
        for (int d = 0; d < max_d; d++) {
            // Bail out if timeout reached.
            if (Diff_Timeout > 0 && System.currentTimeMillis() > ms_end) {
                return null;
            }

            // Walk the front path one step.
            v_map1.add(new HashSet<Long>());  // Adds at index 'd'.
            for (int k = -d; k <= d; k += 2) {
                if (k == -d || k != d && v1.get(k - 1) < v1.get(k + 1)) {
                    x = v1.get(k + 1);
                } else {
                    x = v1.get(k - 1) + 1;
                }
                y = x - k;
                if (doubleEnd) {
                    footstep = diff_footprint(x, y);
                    if (front && (footsteps.containsKey(footstep))) {
                        done = true;
                    }
                    if (!front) {
                        footsteps.put(footstep, d);
                    }
                }
                while (!done && x < arr1_length && y < arr2_length
                        && arr1[x].equals(arr2[y])) {
                    x++;
                    y++;
                    if (doubleEnd) {
                        footstep = diff_footprint(x, y);
                        if (front && (footsteps.containsKey(footstep))) {
                            done = true;
                        }
                        if (!front) {
                            footsteps.put(footstep, d);
                        }
                    }
                }
                v1.put(k, x);
                v_map1.get(d).add(diff_footprint(x, y));
                if (x == arr1_length && y == arr2_length) {
                    // Reached the end in single-path mode.
                    return diff_path1(v_map1, arr1, arr2);
                } else if (done) {
                    // Front path ran over reverse path.
                    v_map2 = v_map2.subList(0, footsteps.get(footstep) + 1);
                    LinkedList<Diff<T>> a = diff_path1(v_map1, Arrays.copyOfRange(arr1, 0, x),
                            Arrays.copyOfRange(arr2, 0, y));
                    a.addAll(diff_path2(v_map2, Arrays.copyOfRange(arr1,x, arr1_length),
                            Arrays.copyOfRange(arr2, y, arr2_length)));
                    return a;
                }
            }

            if (doubleEnd) {
                // Walk the reverse path one step.
                v_map2.add(new HashSet<Long>());  // Adds at index 'd'.
                for (int k = -d; k <= d; k += 2) {
                    if (k == -d || k != d && v2.get(k - 1) < v2.get(k + 1)) {
                        x = v2.get(k + 1);
                    } else {
                        x = v2.get(k - 1) + 1;
                    }
                    y = x - k;
                    footstep = diff_footprint(arr1_length - x, arr2_length - y);
                    if (!front && (footsteps.containsKey(footstep))) {
                        done = true;
                    }
                    if (front) {
                        footsteps.put(footstep, d);
                    }
                    while (!done && x < arr1_length && y < arr2_length
                            && arr1[arr1_length - x - 1].equals(arr2[arr2_length - y - 1])) {
                        x++;
                        y++;
                        footstep = diff_footprint(arr1_length - x, arr2_length - y);
                        if (!front && (footsteps.containsKey(footstep))) {
                            done = true;
                        }
                        if (front) {
                            footsteps.put(footstep, d);
                        }
                    }
                    v2.put(k, x);
                    v_map2.get(d).add(diff_footprint(x, y));
                    if (done) {
                        // Reverse path ran over front path.
                        v_map1 = v_map1.subList(0, footsteps.get(footstep) + 1);
                        LinkedList<Diff<T>> a
                                = diff_path1(v_map1, Arrays.copyOfRange(arr1, 0, arr1_length - x ),
                                Arrays.copyOfRange(arr2, 0, arr2_length - y));
                        a.addAll(diff_path2(v_map2, Arrays.copyOfRange(arr1, arr1_length - x, arr1_length),
                                Arrays.copyOfRange(arr2, arr2_length - y, arr2_length)));
                        return a;
                    }
                }
            }
        }
        return null;
    }

    public LinkedList<Diff<T>> diff_path1(List<Set<Long>> v_map,
                                          T[] arr1, T[] arr2) {
        LinkedList<Diff<T>> path = new LinkedList<Diff<T>>();
        int x = arr1.length;
        int y = arr2.length;
        Operation last_op = null;
        for (int d = v_map.size() - 2; d >= 0; d--) {
            while (true) {
                if (v_map.get(d).contains(diff_footprint(x - 1, y))) {
                    x--;
                    if (last_op == Operation.DELETE) {
                        path.getFirst().array = ArrayUtils.concat(arr1[x], path.getFirst().array);
                    } else {
                        path.addFirst(new Diff(Operation.DELETE,
                                Arrays.copyOfRange(arr1, x, x + 1)));
                    }
                    last_op = Operation.DELETE;
                    break;
                } else if (v_map.get(d).contains(diff_footprint(x, y - 1))) {
                    y--;
                    if (last_op == Operation.INSERT) {
                        path.getFirst().array = ArrayUtils.concat(arr2[y], path.getFirst().array);
                    } else {
                        path.addFirst(new Diff(Operation.INSERT,
                                Arrays.copyOfRange(arr2, y, y + 1)));
                    }
                    last_op = Operation.INSERT;
                    break;
                } else {
                    x--;
                    y--;
                    assert (arr1[x].equals(arr2[y]))
                            : "No diagonal.  Can't happen. (diff_path1)";
                    if (last_op == Operation.EQUAL) {
                        path.getFirst().array = ArrayUtils.concat(arr1[x], path.getFirst().array);
                    } else {
                        path.addFirst(new Diff(Operation.EQUAL,
                                Arrays.copyOfRange(arr1, x, x + 1)));
                    }
                    last_op = Operation.EQUAL;
                }
            }
        }
        return path;
    }

    public LinkedList<Diff<T>> diff_path2(List<Set<Long>> v_map,
                                          T[] arr1, T[] arr2) {
        LinkedList<Diff<T>> path = new LinkedList<Diff<T>>();
        int x = arr1.length;
        int y = arr2.length;
        Operation last_op = null;
        for (int d = v_map.size() - 2; d >= 0; d--) {
            while (true) {
                if (v_map.get(d).contains(diff_footprint(x - 1, y))) {
                    x--;
                    if (last_op == Operation.DELETE) {
                        path.getLast().array = ArrayUtils.concat(path.getLast().array,
                                arr1[arr1.length - x- 1]);
                    } else {
                        path.addLast(new Diff(Operation.DELETE,
                                Arrays.copyOfRange(arr1, arr1.length - x -1, arr1.length - x)));
                    }
                    last_op = Operation.DELETE;
                    break;
                } else if (v_map.get(d).contains(diff_footprint(x, y - 1))) {
                    y--;
                    if (last_op == Operation.INSERT) {
                        path.getLast().array = ArrayUtils.concat(path.getLast().array,
                                arr2[arr2.length - y - 1]);
                    } else {
                        path.addLast(new Diff(Operation.INSERT,
                                Arrays.copyOfRange(arr2, arr2.length - y - 1, arr2.length - y)));
                    }
                    last_op = Operation.INSERT;
                    break;
                } else {
                    x--;
                    y--;
                    assert (arr1[arr1.length - x - 1].equals(arr2[arr2.length - y - 1]))
                            : "No diagonal.  Can't happen. (diff_path2)";
                    if (last_op == Operation.EQUAL) {
                        path.getLast().array = ArrayUtils.concat(path.getLast().array,
                                arr1[arr1.length - x- 1]);
                    } else {
                        path.addLast(new Diff(Operation.EQUAL,
                                Arrays.copyOfRange(arr1, arr1.length - x -1, arr1.length - x)));
                    }
                    last_op = Operation.EQUAL;
                }
            }
        }
        return path;
    }

    public long diff_footprint(int x, int y) {
        // The maximum size for a long is 9,223,372,036,854,775,807
        // The maximum size for an int is 2,147,483,647
        // Two ints fit nicely in one long.
        long result = x;
        result = result << 32;
        result += y;
        return result;
    }

    public void diff_cleanupSemantic(LinkedList<Diff<T>> diffs) {
        if (diffs.isEmpty()) {
            return;
        }
        boolean changes = false;
        Stack<Diff<T>> equalities = new Stack<Diff<T>>();  // Stack of qualities.
        T[] lastequality = null; // Always equal to equalities.lastElement().text
        ListIterator<Diff<T>> pointer = diffs.listIterator();
        // Number of characters that changed prior to the equality.
        int length_changes1 = 0;
        // Number of characters that changed after the equality.
        int length_changes2 = 0;
        Diff thisDiff = pointer.next();
        while (thisDiff != null) {
            if (thisDiff.operation == Operation.EQUAL) {
                // equality found
                equalities.push(thisDiff);
                length_changes1 = length_changes2;
                length_changes2 = 0;
                lastequality = (T[])thisDiff.array;
            } else {
                // an insertion or deletion
                length_changes2 += thisDiff.array.length;
                if (lastequality != null && (lastequality.length <= length_changes1)
                        && (lastequality.length <= length_changes2)) {
                    //System.out.println("Splitting: '" + lastequality + "'");
                    // Walk back to offending equality.
                    while (thisDiff != equalities.lastElement()) {
                        thisDiff = pointer.previous();
                    }
                    pointer.next();

                    // Replace equality with a delete.
                    pointer.set(new Diff(Operation.DELETE, lastequality));
                    // Insert a corresponding an insert.
                    pointer.add(new Diff(Operation.INSERT, lastequality));

                    equalities.pop();  // Throw away the equality we just deleted.
                    if (!equalities.empty()) {
                        // Throw away the previous equality (it needs to be reevaluated).
                        equalities.pop();
                    }
                    if (equalities.empty()) {
                        // There are no previous equalities, walk back to the start.
                        while (pointer.hasPrevious()) {
                            pointer.previous();
                        }
                    } else {
                        // There is a safe equality we can fall back to.
                        thisDiff = equalities.lastElement();
                        while (thisDiff != pointer.previous()) {
                            // Intentionally empty loop.
                        }
                    }

                    length_changes1 = 0;  // Reset the counters.
                    length_changes2 = 0;
                    lastequality = null;
                    changes = true;
                }
            }
            thisDiff = pointer.hasNext() ? pointer.next() : null;
        }

        if (changes) {
            diff_cleanupMerge(diffs);
        }
        diff_cleanupSemanticLossless(diffs);
    }

    public void diff_cleanupSemanticLossless(LinkedList<Diff<T>> diffs) {
        T[] equality1, edit, equality2;
        T[] commonArray;
        int commonOffset;
        int score, bestScore;
        T[] bestEquality1, bestEdit, bestEquality2;
        // Create a new iterator at the start.
        ListIterator<Diff<T>> pointer = diffs.listIterator();
        Diff prevDiff = pointer.hasNext() ? pointer.next() : null;
        Diff thisDiff = pointer.hasNext() ? pointer.next() : null;
        Diff nextDiff = pointer.hasNext() ? pointer.next() : null;
        // Intentionally ignore the first and last element (don't need checking).
        while (nextDiff != null) {
            if (prevDiff.operation == Operation.EQUAL &&
                    nextDiff.operation == Operation.EQUAL) {
                // This is a single edit surrounded by equalities.
                equality1 = (T[])prevDiff.array;
                edit = (T[])thisDiff.array;
                equality2 = (T[])nextDiff.array;

                // First, shift the edit as far left as possible.
                commonOffset = diff_commonSuffix(equality1, edit);
                if (commonOffset != 0) {
                    commonArray = Arrays.copyOfRange(edit, edit.length - commonOffset, edit.length);
                    equality1 = Arrays.copyOfRange(equality1, 0, equality1.length - commonOffset);
                    edit = ArrayUtils.concat(commonArray,
                            Arrays.copyOfRange(edit, 0, edit.length - commonOffset));
                    equality2 = ArrayUtils.concat(commonArray, equality2);
                }

                // Second, step character by character right, looking for the best fit.
                bestEquality1 = equality1;
                bestEdit = edit;
                bestEquality2 = equality2;
                bestScore = diff_cleanupSemanticScore(equality1, edit)
                        + diff_cleanupSemanticScore(edit, equality2);
                while (edit.length != 0 && equality2.length != 0
                        && edit[0].equals(equality2[0])) {
                    equality1 = ArrayUtils.concat(equality1, edit[0]);
                    edit = ArrayUtils.concat(Arrays.copyOfRange(edit, 1, edit.length),
                            equality2[0]);
                    equality2 = Arrays.copyOfRange(equality2, 1, equality2.length);
                    score = diff_cleanupSemanticScore(equality1, edit)
                            + diff_cleanupSemanticScore(edit, equality2);
                    // The >= encourages trailing rather than leading whitespace on edits.
                    if (score >= bestScore) {
                        bestScore = score;
                        bestEquality1 = equality1;
                        bestEdit = edit;
                        bestEquality2 = equality2;
                    }
                }

                if (!prevDiff.array.equals(bestEquality1)) {
                    // We have an improvement, save it back to the diff.
                    if (bestEquality1.length != 0) {
                        prevDiff.array = bestEquality1;
                    } else {
                        pointer.previous(); // Walk past nextDiff.
                        pointer.previous(); // Walk past thisDiff.
                        pointer.previous(); // Walk past prevDiff.
                        pointer.remove(); // Delete prevDiff.
                        pointer.next(); // Walk past thisDiff.
                        pointer.next(); // Walk past nextDiff.
                    }
                    thisDiff.array = bestEdit;
                    if (bestEquality2.length != 0) {
                        nextDiff.array = bestEquality2;
                    } else {
                        pointer.remove(); // Delete nextDiff.
                        nextDiff = thisDiff;
                        thisDiff = prevDiff;
                    }
                }
            }
            prevDiff = thisDiff;
            thisDiff = nextDiff;
            nextDiff = pointer.hasNext() ? pointer.next() : null;
        }
    }

    private int diff_cleanupSemanticScore(T[] one, T[] two) {
        if (one.length == 0 || two.length == 0) {
            // Edges are the best.
            return 5;
        }

        // Each port of this function behaves slightly differently due to
        // subtle differences in each language's definition of things like
        // 'whitespace'.  Since this function's purpose is largely cosmetic,
        // the choice has been made to use each language's native features
        // rather than force total conformity.
        int score = 0;
        // One point for non-alphanumeric.
//        if (!Character.isLetterOrDigit(one.charAt(one.length() - 1))
//                || !Character.isLetterOrDigit(two.charAt(0))) {
//            score++;
//            // Two points for whitespace.
//            if (Character.isWhitespace(one.charAt(one.length() - 1))
//                    || Character.isWhitespace(two.charAt(0))) {
//                score++;
//                // Three points for line breaks.
//                if (Character.getType(one.charAt(one.length() - 1)) == Character.CONTROL
//                        || Character.getType(two.charAt(0)) == Character.CONTROL) {
//                    score++;
//                    // Four points for blank lines.
//                    if (BLANKLINEEND.matcher(one).find()
//                            || BLANKLINESTART.matcher(two).find()) {
//                        score++;
//                    }
//                }
//            }
//        }
        return score;
    }

}
