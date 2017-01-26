package diff;

import java.util.*;

/**
 * Created by apple on 1/11/17.
 */
public class DiffProcessor<T> {
    /**
     * Number of seconds to map a diff before giving up (0 for infinity).
     */
    public float Diff_Timeout = 0f;
    /**
     * Cost of an empty edit operation in terms of edit characters.
     */
    public short Diff_EditCost = 4;
    /**
     * The size beyond which the double-ended diff activates.
     * Double-ending is twice as fast, but less accurate.
     */
    public short Diff_DualThreshold = 32;

    public int chunkSize1 = Integer.MAX_VALUE;
    public int chunkSize2 = Integer.MAX_VALUE;

    private Class<T> clazz;
    public static class LinesToCharsResult<T> {
        public KKString<Integer> chars1;
        public KKString<Integer> chars2;
        public List<KKString<T>> lineArray;

        public LinesToCharsResult(KKString<Integer> chars1, KKString<Integer> chars2,
                                     List<KKString<T>> lineArray) {
            this.chars1 = chars1;
            this.chars2 = chars2;
            this.lineArray = lineArray;
        }
    }

    public enum Operation {
        DELETE, INSERT, EQUAL, EMPTY_DELETE, EMPTY_INSERT
    }

    public DiffProcessor(Class<T> clazz) {
        this.clazz = clazz;
    }

    public LinkedList<Diff<T>> diff_main(KKString<T> text1, KKString<T> text2) {
        return diff_main(text1, text2, true);
    }

    public LinkedList<Diff<T>> diff_main(KKString<T> text1, KKString<T> text2, int chunkSize1, int chunkSize2) {
        this.chunkSize1 = chunkSize1;
        this.chunkSize2 = chunkSize2;
        return diff_main(text1, text2, true);
    }

    public LinkedList<Diff<T>> diff_main(KKString<T> text1, KKString<T> text2,
                                      boolean checklines) {
        // Check for equality (speedup)
        LinkedList<Diff<T>> diffs;
        if (text1.equals(text2)) {
            diffs = new LinkedList<Diff<T>>();
            diffs.add(new Diff(Operation.EQUAL, text1));
            return diffs;
        }

        // Trim off common prefix (speedup)
        int commonlength = diff_commonPrefix(text1, text2);
        KKString<T> commonprefix = text1.substring(0, commonlength);
        text1 = text1.substring(commonlength);
        text2 = text2.substring(commonlength);

        // Trim off common suffix (speedup)
        commonlength = diff_commonSuffix(text1, text2);
        KKString<T> commonsuffix = text1.substring(text1.length() - commonlength);
        text1 = text1.substring(0, text1.length() - commonlength);
        text2 = text2.substring(0, text2.length() - commonlength);

        // Compute the diff on the middle block
        diffs = diff_compute(text1, text2, checklines);

        // Restore the prefix and suffix
        if (commonprefix.length() != 0) {
            diffs.addFirst(new Diff<T>(Operation.EQUAL, commonprefix));
        }
        if (commonsuffix.length() != 0) {
            diffs.addLast(new Diff<T>(Operation.EQUAL, commonsuffix));
        }

        diff_cleanupMerge(diffs);
        return diffs;
    }

    protected LinkedList<Diff<T>> diff_compute(KKString<T> text1, KKString<T> text2,
                                            boolean checklines) {
        LinkedList<Diff<T>> diffs = new LinkedList<Diff<T>>();

        if (text1.length() == 0) {
            // Just add some text (speedup)
            diffs.add(new Diff(Operation.INSERT, text2));
            return diffs;
        }

        if (text2.length() == 0) {
            // Just delete some text (speedup)
            diffs.add(new Diff(Operation.DELETE, text1));
            return diffs;
        }

        KKString<T> longtext = text1.length() > text2.length() ? text1 : text2;
        KKString<T> shorttext = text1.length() > text2.length() ? text2 : text1;
        int i = longtext.indexOf(shorttext);
        if (i != -1) {
            // Shorter text is inside the longer text (speedup)
            Operation op = (text1.length() > text2.length()) ?
                    Operation.DELETE : Operation.INSERT;
            diffs.add(new Diff(op, longtext.substring(0, i)));
            diffs.add(new Diff(Operation.EQUAL, shorttext));
            diffs.add(new Diff(op, longtext.substring(i + shorttext.length())));
            return diffs;
        }
        longtext = shorttext = null;  // Garbage collect.

        // Check to see if the problem can be split in two.
        KKString<T>[] hm = diff_halfMatch(text1, text2);
        if (hm != null) {
            // A half-match was found, sort out the return data.
            KKString<T> text1_a = hm[0];
            KKString<T> text1_b = hm[1];
            KKString<T> text2_a = hm[2];
            KKString<T> text2_b = hm[3];
            KKString<T> mid_common = hm[4];
            // Send both pairs off for separate processing.
            LinkedList<Diff<T>> diffs_a = diff_main(text1_a, text2_a, checklines);
            LinkedList<Diff<T>> diffs_b = diff_main(text1_b, text2_b, checklines);
            // Merge the results.
            diffs = diffs_a;
            diffs.add(new Diff(Operation.EQUAL, mid_common));
            diffs.addAll(diffs_b);
            return diffs;
        }

        // Perform a real diff.
        if (checklines && (text1.length() < 100 || text2.length() < 100)) {
            checklines = false;  // Too trivial for the overhead.
        }
        List<KKString<T>> linearray = null;
        KKString<Integer> char1;
        KKString<Integer> char2;
        if (!checklines) {
            // Scan the text on a line-by-line basis first.
            diffs = diff_map(text1, text2);
            if (diffs == null) {
                // No acceptable result.
                diffs = new LinkedList<Diff<T>>();
                diffs.add(new Diff(Operation.DELETE, text1));
                diffs.add(new Diff(Operation.INSERT, text2));
                diff_cleanupMerge(diffs);
            }
        }else {
            LinesToCharsResult b = diff_linesToChars(text1, text2, chunkSize1, chunkSize2); /// change chunk size later
            char1 = b.chars1;
            char2 = b.chars2;
            linearray = b.lineArray;

            DiffProcessor<Integer> intDiffProcessor = new DiffProcessor<Integer>(Integer.class);
            List<Diff<Integer>> intDiffs = intDiffProcessor.diff_map(char1, char2);
            if (intDiffs == null) {
                // No acceptable result.
                intDiffs = new LinkedList<Diff<Integer>>();
                intDiffs.add(new Diff(Operation.DELETE, char1));
                intDiffs.add(new Diff(Operation.INSERT, char2));
            }

            diffs = diff_charsToLines(intDiffs, linearray);
            diff_cleanupSemantic(diffs);
            diffs.add(new Diff<T>(Operation.EQUAL, new KKString<T>()));
            int count_delete = 0;
            int count_insert = 0;
            KKString<T> text_delete = new KKString<T>();
            KKString<T> text_insert = new KKString<T>();
            ListIterator<Diff<T>> pointer = diffs.listIterator();
            Diff thisDiff = pointer.next();
            while (thisDiff != null) {
                switch (thisDiff.operation) {
                    case INSERT:
                        count_insert++;
                        text_insert = text_insert.concat(thisDiff.text);
                        break;
                    case DELETE:
                        count_delete++;
                        text_delete = text_insert.concat(thisDiff.text);
                        break;
                    case EQUAL:
                        // Upon reaching an equality, check for prior redundancies.
                        if (count_delete >= 1 && count_insert >= 1) {
                            // Delete the offending records and add the merged ones.
                            pointer.previous();
                            for (int j = 0; j < count_delete + count_insert; j++) {
                                pointer.previous();
                                pointer.remove();
                            }
                            for (Diff newDiff : diff_main(text_delete, text_insert, false)) {
                                pointer.add(newDiff);
                            }
                        }
                        count_insert = 0;
                        count_delete = 0;
                        text_delete = new KKString<T>();
                        text_insert = new KKString<T>();
                        break;
                }
                thisDiff = pointer.hasNext() ? pointer.next() : null;
            }
            diffs.removeLast();
        }

        return diffs;
    }

    protected LinkedList<Diff<T>> diff_map(KKString<T> text1, KKString<T> text2) {
        long ms_end = System.currentTimeMillis() + (long) (Diff_Timeout * 1000);
        // Cache the text lengths to prevent multiple calls.
        int text1_length = text1.length();
        int text2_length = text2.length();
        int max_d = text1_length + text2_length - 1;
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
        boolean front = ((text1_length + text2_length) % 2 == 1);
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
                while (!done && x < text1_length && y < text2_length
                        && text1.charAt(x).equals(text2.charAt(y))) {
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
                if (x == text1_length && y == text2_length) {
                    // Reached the end in single-path mode.
                    return diff_path1(v_map1, text1, text2);
                } else if (done) {
                    // Front path ran over reverse path.
                    v_map2 = v_map2.subList(0, footsteps.get(footstep) + 1);
                    LinkedList<Diff<T>> a = diff_path1(v_map1, text1.substring(0, x),
                            text2.substring(0, y));
                    a.addAll(diff_path2(v_map2, text1.substring(x), text2.substring(y)));
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
                    footstep = diff_footprint(text1_length - x, text2_length - y);
                    if (!front && (footsteps.containsKey(footstep))) {
                        done = true;
                    }
                    if (front) {
                        footsteps.put(footstep, d);
                    }
                    while (!done && x < text1_length && y < text2_length
                            && text1.charAt(text1_length - x - 1).equals(
                            text2.charAt(text2_length - y - 1))) {
                        x++;
                        y++;
                        footstep = diff_footprint(text1_length - x, text2_length - y);
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
                                = diff_path1(v_map1, text1.substring(0, text1_length - x),
                                text2.substring(0, text2_length - y));
                        a.addAll(diff_path2(v_map2, text1.substring(text1_length - x),
                                text2.substring(text2_length - y)));
                        return a;
                    }
                }
            }
        }
        // Number of diffs equals number of characters, no commonality at all.
        return null;
    }

    public LinkedList<Diff<T>> diff_path1(List<Set<Long>> v_map,
                                          KKString<T> text1, KKString<T> text2) {
        LinkedList<Diff<T>> path = new LinkedList<Diff<T>>();
        int x = text1.length();
        int y = text2.length();
        Operation last_op = null;
        for (int d = v_map.size() - 2; d >= 0; d--) {
            while (true) {
                if (v_map.get(d).contains(diff_footprint(x - 1, y))) {
                    x--;
                    if (last_op == Operation.DELETE) {
                        path.getFirst().text = path.getFirst().text.addFirst(text1.charAt(x));
                    } else {
                        path.addFirst(new Diff<T>(Operation.DELETE,
                                text1.substring(x, x + 1)));
                    }
                    last_op = Operation.DELETE;
                    break;
                } else if (v_map.get(d).contains(diff_footprint(x, y - 1))) {
                    y--;
                    if (last_op == Operation.INSERT) {
                        path.getFirst().text =  path.getFirst().text.addFirst(text2.charAt(y));
                    } else {
                        path.addFirst(new Diff(Operation.INSERT,
                                text2.substring(y, y + 1)));
                    }
                    last_op = Operation.INSERT;
                    break;
                } else {
                    x--;
                    y--;
                    assert (text1.charAt(x).equals(text2.charAt(y)))
                            : "No diagonal.  Can't happen. (diff_path1)";
                    if (last_op == Operation.EQUAL) {
                        path.getFirst().text = path.getFirst().text.addFirst(text1.charAt(x));
                    } else {
                        path.addFirst(new Diff(Operation.EQUAL, text1.substring(x, x + 1)));
                    }
                    last_op = Operation.EQUAL;
                }
            }
        }
        return path;
    }

    public LinkedList<Diff<T>> diff_path2(List<Set<Long>> v_map,
                                          KKString<T> text1, KKString<T> text2) {
        LinkedList<Diff<T>> path = new LinkedList<Diff<T>>();
        int x = text1.length();
        int y = text2.length();
        Operation last_op = null;
        for (int d = v_map.size() - 2; d >= 0; d--) {
            while (true) {
                if (v_map.get(d).contains(diff_footprint(x - 1, y))) {
                    x--;
                    if (last_op == Operation.DELETE) {
                        path.getLast().text = path.getLast().text.concat(
                                text1.charAt(text1.length() - x - 1));
                    } else {
                        path.addLast(new Diff(Operation.DELETE,
                                text1.substring(text1.length() - x - 1, text1.length() - x)));
                    }
                    last_op = Operation.DELETE;
                    break;
                } else if (v_map.get(d).contains(diff_footprint(x, y - 1))) {
                    y--;
                    if (last_op == Operation.INSERT) {
                        path.getLast().text = path.getLast().text.concat(
                                text2.charAt(text2.length() - y - 1));
                    } else {
                        path.addLast(new Diff(Operation.INSERT,
                                text2.substring(text2.length() - y - 1, text2.length() - y)));
                    }
                    last_op = Operation.INSERT;
                    break;
                } else {
                    x--;
                    y--;
                    assert (text1.charAt(text1.length() - x - 1).equals(
                            text2.charAt(text2.length() - y - 1)))
                            : "No diagonal.  Can't happen. (diff_path2)";
                    if (last_op == Operation.EQUAL) {
                        path.getLast().text = path.getLast().text.concat(
                                text1.charAt(text1.length() - x - 1));
                    } else {
                        path.addLast(new Diff(Operation.EQUAL,
                                text1.substring(text1.length() - x - 1, text1.length() - x)));
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

    public KKString<T>[] diff_halfMatch(KKString<T> text1, KKString<T> text2) {
        KKString<T> longtext = text1.length() > text2.length() ? text1 : text2;
        KKString<T> shorttext = text1.length() > text2.length() ? text2 : text1;
        if (longtext.length() < 10 || shorttext.length() < 1) {
            return null;  // Pointless.
        }

        // First check if the second quarter is the seed for a half-match.
        KKString<T>[] hm1 = diff_halfMatchI(longtext, shorttext,
                (longtext.length() + 3) / 4);
        // Check again based on the third quarter.
        KKString<T>[] hm2 = diff_halfMatchI(longtext, shorttext,
                (longtext.length() + 1) / 2);
        KKString<T>[] hm;
        if (hm1 == null && hm2 == null) {
            return null;
        } else if (hm2 == null) {
            hm = hm1;
        } else if (hm1 == null) {
            hm = hm2;
        } else {
            // Both matched.  Select the longest.
            hm = hm1[4].length() > hm2[4].length() ? hm1 : hm2;
        }

        // A half-match was found, sort out the return data.
        if (text1.length() > text2.length()) {
            return hm;
            //return new String[]{hm[0], hm[1], hm[2], hm[3], hm[4]};
        } else {
            return new KKString[]{hm[2], hm[3], hm[0], hm[1], hm[4]};
        }
    }

    private KKString<T>[] diff_halfMatchI(KKString<T> longtext, KKString<T> shorttext, int i) {
        // Start with a 1/4 length substring at position i as a seed.
        KKString<T> seed = longtext.substring(i, i + longtext.length() / 4);
        int j = -1;
        KKString<T> best_common = new KKString<T>();
        KKString<T> best_longtext_a = new KKString<T>(), best_longtext_b = new KKString<T>();
        KKString<T> best_shorttext_a = new KKString<T>(), best_shorttext_b = new KKString<T>();
        while ((j = shorttext.indexOf(seed, j + 1)) != -1) {
            int prefixLength = diff_commonPrefix(longtext.substring(i),
                    shorttext.substring(j));
            int suffixLength = diff_commonSuffix(longtext.substring(0, i),
                    shorttext.substring(0, j));
            if (best_common.length() < suffixLength + prefixLength) {
                best_common = shorttext.substring(j - suffixLength, j).concat(
                        shorttext.substring(j, j + prefixLength));
                best_longtext_a = longtext.substring(0, i - suffixLength);
                best_longtext_b = longtext.substring(i + prefixLength);
                best_shorttext_a = shorttext.substring(0, j - suffixLength);
                best_shorttext_b = shorttext.substring(j + prefixLength);
            }
        }
        if (best_common.length() >= longtext.length() / 2) {
            return new KKString[]{best_longtext_a, best_longtext_b,
                    best_shorttext_a, best_shorttext_b, best_common};
        } else {
            return null;
        }
    }

    public int diff_commonPrefix(KKString<T> text1, KKString<T> text2) {
        // Performance analysis: http://neil.fraser.name/news/2007/10/09/
        int n = Math.min(text1.length(), text2.length());
        for (int i = 0; i < n; i++) {
            if (!text1.charAt(i).equals(text2.charAt(i))) {
                return i;
            }
        }
        return n;
    }

    public int diff_commonSuffix(KKString<T> text1, KKString<T> text2) {
        // Performance analysis: http://neil.fraser.name/news/2007/10/09/
        int text1_length = text1.length();
        int text2_length = text2.length();
        int n = Math.min(text1_length, text2_length);
        for (int i = 1; i <= n; i++) {
            if (!text1.charAt(text1_length - i).equals(text2.charAt(text2_length - i))) {
                return i - 1;
            }
        }
        return n;
    }

    public void diff_cleanupMerge(LinkedList<Diff<T>> diffs) {
        diffs.add(new Diff<T>(Operation.EQUAL, new KKString<T>()));  // Add a dummy entry at the end.
        ListIterator<Diff<T>> pointer = diffs.listIterator();
        int count_delete = 0;
        int count_insert = 0;
        KKString<T> text_delete = new KKString<T>();
        KKString<T> text_insert = new KKString<T>();
        Diff thisDiff = pointer.next();
        Diff prevEqual = null;
        int commonlength;
        while (thisDiff != null) {
            switch (thisDiff.operation) {
                case INSERT:
                    count_insert++;
                    text_insert = text_insert.concat(thisDiff.text);
                    prevEqual = null;
                    break;
                case DELETE:
                    count_delete++;
                    text_delete = text_delete.concat(thisDiff.text);
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
                            commonlength = diff_commonPrefix(text_insert, text_delete);
                            if (commonlength != 0) {
                                if (pointer.hasPrevious()) {
                                    thisDiff = pointer.previous();
                                    assert thisDiff.operation == Operation.EQUAL
                                            : "Previous diff should have been an equality.";
                                    thisDiff.text = thisDiff.text.concat(text_insert.substring(0, commonlength));
                                    pointer.next();
                                } else {
                                    pointer.add(new Diff(Operation.EQUAL,
                                            text_insert.substring(0, commonlength)));
                                }
                                text_insert = text_insert.substring(commonlength);
                                text_delete = text_delete.substring(commonlength);
                            }
                            // Factor out any common suffixies.
                            commonlength = diff_commonSuffix(text_insert, text_delete);
                            if (commonlength != 0) {
                                thisDiff = pointer.next();
                                thisDiff.text = text_insert.substring(text_insert.length()
                                        - commonlength).concat(thisDiff.text);
                                text_insert = text_insert.substring(0, text_insert.length()
                                        - commonlength);
                                text_delete = text_delete.substring(0, text_delete.length()
                                        - commonlength);
                                pointer.previous();
                            }
                        }
                        // Insert the merged records.
                        if (text_delete.length() != 0) {
                            pointer.add(new Diff(Operation.DELETE, text_delete));
                        }
                        if (text_insert.length() != 0) {
                            pointer.add(new Diff(Operation.INSERT, text_insert));
                        }
                        // Step forward to the equality.
                        thisDiff = pointer.hasNext() ? pointer.next() : null;
                    } else if (prevEqual != null) {
                        // Merge this equality with the previous one.
                        prevEqual.text = prevEqual.text.concat(thisDiff.text);
                        pointer.remove();
                        thisDiff = pointer.previous();
                        pointer.next();  // Forward direction
                    }
                    count_insert = 0;
                    count_delete = 0;
                    text_delete = new KKString<T>();
                    text_insert = new KKString<T>();
                    prevEqual = thisDiff;
                    break;
            }
            thisDiff = pointer.hasNext() ? pointer.next() : null;
        }
        // System.out.println(diff);
        if (diffs.getLast().text.length() == 0) {
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
                if (thisDiff.text.endsWith(prevDiff.text)) {
                    // Shift the edit over the previous equality.
                    thisDiff.text = prevDiff.text.concat(
                             thisDiff.text.substring(0, thisDiff.text.length()
                            - prevDiff.text.length()));
                    nextDiff.text = prevDiff.text.concat(nextDiff.text);
                    pointer.previous(); // Walk past nextDiff.
                    pointer.previous(); // Walk past thisDiff.
                    pointer.previous(); // Walk past prevDiff.
                    pointer.remove(); // Delete prevDiff.
                    pointer.next(); // Walk past thisDiff.
                    thisDiff = pointer.next(); // Walk past nextDiff.
                    nextDiff = pointer.hasNext() ? pointer.next() : null;
                    changes = true;
                } else if (thisDiff.text.startsWith(nextDiff.text)) {
                    // Shift the edit over the next equality.
                    prevDiff.text = prevDiff.text.concat(nextDiff.text);
                    thisDiff.text = thisDiff.text.substring(nextDiff.text.length()).concat(nextDiff.text);
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

    public LinesToCharsResult diff_linesToChars(KKString<T> text1, KKString<T> text2, int chunkSize1, int chunkSize2) {
        List<KKString<T>> lineArray = new ArrayList<KKString<T>>();
        Map<KKString<T>, Integer> lineHash = new HashMap<KKString<T>, Integer>();
        KKString<Integer> chars1 = diff_linesToCharsMunge(text1, lineArray, lineHash, chunkSize1);
        KKString<Integer> chars2 = diff_linesToCharsMunge(text2, lineArray, lineHash, chunkSize2);
        return new LinesToCharsResult(chars1, chars2, lineArray);
    }

    public KKString<Integer> diff_linesToCharsMunge(KKString<T> text, List<KKString<T>> lineArray,
                                          Map<KKString<T>, Integer> lineHash, int chunkSize) {
        int lineStart = 0;
        int lineEnd = 0;
        KKString<T> line;
        KKStringBuilder chars = new KKStringBuilder();

        while (lineEnd < text.length()) {
            lineEnd = lineStart + chunkSize;
            if (lineEnd >= text.length()) {
                lineEnd = text.length();
            }
            line = text.substring(lineStart, lineEnd);
            lineStart = lineEnd;

            if (lineHash.containsKey(line)) {
                chars.append(new KKString<Integer>(lineHash.get(line)));
            } else {
                lineArray.add(line);
                lineHash.put(line, lineArray.size() - 1);
                chars.append(new KKString<Integer>((lineArray.size() - 1)));
            }
        }
        return chars.toKKString();
    }

    public LinkedList<Diff<T>> diff_charsToLines(List<Diff<Integer>> diffs,
                                     List<KKString<T>> lineArray) {
        KKStringBuilder text;
        LinkedList<Diff<T>> ret = new LinkedList<Diff<T>>();
        for (Diff<Integer> diff : diffs) {
            text = new KKStringBuilder<T>();
            for (int y = 0; y < diff.text.length(); y++) {
                text.append(lineArray.get(diff.text.charAt(y)));
            }
            ret.add(new Diff<T>(diff.operation, text.toKKString()));
        }
        return ret;
    }

    public void diff_cleanupSemantic(LinkedList<Diff<T>> diffs) {
        if (diffs.isEmpty()) {
            return;
        }
        boolean changes = false;
        Stack<Diff<T>> equalities = new Stack<Diff<T>>();  // Stack of qualities.
        KKString<T> lastequality = null; // Always equal to equalities.lastElement().text
        ListIterator<Diff<T>> pointer = diffs.listIterator();
        // Number of characters that changed prior to the equality.
        int length_changes1 = 0;
        // Number of characters that changed after the equality.
        int length_changes2 = 0;
        Diff<T> thisDiff = pointer.next();
        while (thisDiff != null) {
            if (thisDiff.operation == Operation.EQUAL) {
                // equality found
                equalities.push(thisDiff);
                length_changes1 = length_changes2;
                length_changes2 = 0;
                lastequality = thisDiff.text;
            } else {
                // an insertion or deletion
                length_changes2 += thisDiff.text.length();
                if (lastequality != null && (lastequality.length() <= length_changes1)
                        && (lastequality.length() <= length_changes2)) {
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


    /**
     * Look for single edits surrounded on both sides by equalities
     * which can be shifted sideways to align the edit to a word boundary.
     * e.g: The c<ins>at c</ins>ame. -> The <ins>cat </ins>came.
     * @param diffs LinkedList of Diff objects.
     */
    public void diff_cleanupSemanticLossless(LinkedList<Diff<T>> diffs) {
        KKString<T> equality1, edit, equality2;
        KKString<T> commonString;
        int commonOffset;
        int score, bestScore;
        KKString<T> bestEquality1, bestEdit, bestEquality2;
        // Create a new iterator at the start.
        ListIterator<Diff<T>> pointer = diffs.listIterator();
        Diff<T> prevDiff = pointer.hasNext() ? pointer.next() : null;
        Diff<T> thisDiff = pointer.hasNext() ? pointer.next() : null;
        Diff<T> nextDiff = pointer.hasNext() ? pointer.next() : null;
        // Intentionally ignore the first and last element (don't need checking).
        while (nextDiff != null) {
            if (prevDiff.operation == Operation.EQUAL &&
                    nextDiff.operation == Operation.EQUAL) {
                // This is a single edit surrounded by equalities.
                equality1 = prevDiff.text;
                edit = thisDiff.text;
                equality2 = nextDiff.text;

                // First, shift the edit as far left as possible.
                commonOffset = diff_commonSuffix(equality1, edit);
                if (commonOffset != 0) {
                    commonString = edit.substring(edit.length() - commonOffset);
                    equality1 = equality1.substring(0, equality1.length() - commonOffset);
                    edit = commonString.concat(edit.substring(0, edit.length() - commonOffset));
                    equality2 = commonString.concat(equality2);
                }

                // Second, step character by character right, looking for the best fit.
                bestEquality1 = equality1;
                bestEdit = edit;
                bestEquality2 = equality2;
                bestScore = diff_cleanupSemanticScore(equality1, edit)
                        + diff_cleanupSemanticScore(edit, equality2);
                while (edit.length() != 0 && equality2.length() != 0
                        && edit.charAt(0).equals(equality2.charAt(0))) {
                    equality1 = equality1.concat(edit.charAt(0));
                    edit = edit.substring(1).concat(equality2.charAt(0));
                    equality2 = equality2.substring(1);
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

                if (!prevDiff.text.equals(bestEquality1)) {
                    // We have an improvement, save it back to the diff.
                    if (bestEquality1.length() != 0) {
                        prevDiff.text = bestEquality1;
                    } else {
                        pointer.previous(); // Walk past nextDiff.
                        pointer.previous(); // Walk past thisDiff.
                        pointer.previous(); // Walk past prevDiff.
                        pointer.remove(); // Delete prevDiff.
                        pointer.next(); // Walk past thisDiff.
                        pointer.next(); // Walk past nextDiff.
                    }
                    thisDiff.text = bestEdit;
                    if (bestEquality2.length() != 0) {
                        nextDiff.text = bestEquality2;
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


    /**
     * Given two strings, compute a score representing whether the internal
     * boundary falls on logical boundaries.
     * Scores range from 5 (best) to 0 (worst).
     * @param one First string.
     * @param two Second string.
     * @return The score.
     */
    private int diff_cleanupSemanticScore(KKString<T> one, KKString<T> two) {
        if (one.length() == 0 || two.length() == 0) {
            // Edges are the best.
            return 5;
        }

        // Each port of this function behaves slightly differently due to
        // subtle differences in each language's definition of things like
        // 'whitespace'.  Since this function's purpose is largely cosmetic,
        // the choice has been made to use each language's native features
        // rather than force total conformity.
        int score = 0;
//        // One point for non-alphanumeric.
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



    public static class Diff<T> {
        /**
         * One of: INSERT, DELETE or EQUAL.
         */
        public Operation operation;
        /**
         * The text associated with this diff operation.
         */
        public KKString<T> text;

        /**
         * Constructor.  Initializes the diff with the provided values.
         * @param operation One of INSERT, DELETE or EQUAL.
         * @param text The text being applied.
         */
        public Diff(Operation operation, T... text) {
            // Construct a diff with the specified operation and text.
            this.operation = operation;
            this.text = new KKString<T>(text);
        }


        public Diff(Operation operation, KKString<T> text) {
            // Construct a diff with the specified operation and text.
            this.operation = operation;
            this.text = text;
        }

        public LinkedList<Diff<T>> split(KKString<T> separator) {
            int i = -1;
            KKString<T> source = text;
            LinkedList<Diff<T>> diffs = new LinkedList<Diff<T>>();
            switch (operation) {
                case INSERT:
                case DELETE:
                    while((i = source.indexOf(separator, i)) > -1) {
                        if(i != 0)
                            diffs.add(new Diff<T>(operation, source.substring(0, i)));
                        diffs.add(new Diff<T>(operation == Operation.INSERT ? Operation.EMPTY_INSERT: Operation.EMPTY_DELETE, separator));
                        if(i < source.length() - 1)
                            source = source.substring(i + 1);
                        else
                            source = new KKString<T>();
                    }
                break;
            }

            if(!KKString.isNullOrEmpty(source))
                diffs.add(new Diff<T>(operation,source));

            return diffs;
        }
        /**
         * Display a human-readable version of this Diff.
         * @return text version.
         */
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("(");
            builder.append(this.operation);
            builder.append(",");
            builder.append("[");
            for(int i = 0; i < text.length(); ++i) {
                builder.append(text.charAt(i).toString()).append(", ");
            }
            if(text.length() > 0) {
                builder.deleteCharAt(builder.length() - 1);
                builder.deleteCharAt(builder.length() - 1);
            }
            builder.append("]");
            builder.append(")");
            return builder.toString();
        }


        /**
         * Is this Diff equivalent to another Diff?
         * @param d Another Diff to compare against.
         * @return true or false.
         */
        public boolean equals(Object d) {
            try {
                return (((Diff) d).operation == this.operation)
                        && (((Diff) d).text.equals(this.text));
            } catch (ClassCastException e) {
                return false;
            }
        }
    }

}
