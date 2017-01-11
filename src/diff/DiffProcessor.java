package diff;

import java.util.*;

/**
 * Created by apple on 1/11/17.
 */
public class DiffProcessor<T> {
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

    private Class<T> clazz;
    protected static class LinesToCharsResult<T> {
        protected KKString<T> chars1;
        protected KKString<T> chars2;
        protected List<KKString<T>> lineArray;

        protected LinesToCharsResult(KKString<T> chars1, KKString<T> chars2,
                                     List<KKString<T>> lineArray) {
            this.chars1 = chars1;
            this.chars2 = chars2;
            this.lineArray = lineArray;
        }
    }

    public enum Operation {
        DELETE, INSERT, EQUAL
    }

    public DiffProcessor(Class<T> clazz) {
        this.clazz = clazz;
    }

    public LinkedList<Diff<T>> diff_main(KKString<T> text1, KKString<T> text2) {
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
//
//        // Restore the prefix and suffix
//        if (commonprefix.length() != 0) {
//            diffs.addFirst(new Diff(Operation.EQUAL, commonprefix));
//        }
//        if (commonsuffix.length() != 0) {
//            diffs.addLast(new Diff(Operation.EQUAL, commonsuffix));
//        }
//
//        diff_cleanupMerge(diffs);
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
        List<String> linearray = null;
//        if (checklines) {
//            // Scan the text on a line-by-line basis first.
//            LinesToCharsResult b = diff_linesToChars(text1, text2);
//            text1 = b.chars1;
//            text2 = b.chars2;
//            linearray = b.lineArray;
//        }
//
        diffs = diff_map(text1, text2);
        if (diffs == null) {
            // No acceptable result.
            diffs = new LinkedList<Diff<T>>();
            diffs.add(new Diff(Operation.DELETE, text1));
            diffs.add(new Diff(Operation.INSERT, text2));
        }
//
//        if (checklines) {
//            // Convert the diff back to original text.
//            diff_charsToLines(diffs, linearray);
//            // Eliminate freak matches (e.g. blank lines)
//            diff_cleanupSemantic(diffs);
//
//            // Rediff any replacement blocks, this time character-by-character.
//            // Add a dummy entry at the end.
//            diffs.add(new Diff(Operation.EQUAL, ""));
//            int count_delete = 0;
//            int count_insert = 0;
//            String text_delete = "";
//            String text_insert = "";
//            ListIterator<Diff> pointer = diffs.listIterator();
//            Diff thisDiff = pointer.next();
//            while (thisDiff != null) {
//                switch (thisDiff.operation) {
//                    case INSERT:
//                        count_insert++;
//                        text_insert += thisDiff.text;
//                        break;
//                    case DELETE:
//                        count_delete++;
//                        text_delete += thisDiff.text;
//                        break;
//                    case EQUAL:
//                        // Upon reaching an equality, check for prior redundancies.
//                        if (count_delete >= 1 && count_insert >= 1) {
//                            // Delete the offending records and add the merged ones.
//                            pointer.previous();
//                            for (int j = 0; j < count_delete + count_insert; j++) {
//                                pointer.previous();
//                                pointer.remove();
//                            }
//                            for (Diff newDiff : diff_main(text_delete, text_insert, false)) {
//                                pointer.add(newDiff);
//                            }
//                        }
//                        count_insert = 0;
//                        count_delete = 0;
//                        text_delete = "";
//                        text_insert = "";
//                        break;
//                }
//                thisDiff = pointer.hasNext() ? pointer.next() : null;
//            }
//            diffs.removeLast();  // Remove the dummy entry at the end.
//        }
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
                        && text1.charAt(x) == text2.charAt(y)) {
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
                            && text1.charAt(text1_length - x - 1)
                            == text2.charAt(text2_length - y - 1)) {
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

    protected LinkedList<Diff<T>> diff_path1(List<Set<Long>> v_map,
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
                    assert (text1.charAt(x) == text2.charAt(y))
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

    protected LinkedList<Diff<T>> diff_path2(List<Set<Long>> v_map,
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
                    assert (text1.charAt(text1.length() - x - 1)
                            == text2.charAt(text2.length() - y - 1))
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

    protected long diff_footprint(int x, int y) {
        // The maximum size for a long is 9,223,372,036,854,775,807
        // The maximum size for an int is 2,147,483,647
        // Two ints fit nicely in one long.
        long result = x;
        result = result << 32;
        result += y;
        return result;
    }

    protected KKString<T>[] diff_halfMatch(KKString<T> text1, KKString<T> text2) {
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
        KKString<T> best_common = new KKString<T>(clazz);
        KKString<T> best_longtext_a = new KKString<T>(clazz), best_longtext_b = new KKString<T>(clazz);
        KKString<T> best_shorttext_a = new KKString<T>(clazz), best_shorttext_b = new KKString<T>(clazz);
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
            if (text1.charAt(i) != text2.charAt(i)) {
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
            if (text1.charAt(text1_length - i) != text2.charAt(text2_length - i)) {
                return i - 1;
            }
        }
        return n;
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
        public Diff(Operation operation, KKString<T> text) {
            // Construct a diff with the specified operation and text.
            this.operation = operation;
            this.text = text;
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
                builder.append(text.charAt(i).toString()).append(",");
            }
            builder.deleteCharAt(builder.length() - 1);
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
