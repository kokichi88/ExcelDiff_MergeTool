package test.diff;

import diff.ArrayDiff;
import diff.ArrayUtils;
import diff.Diff;
import diff.SearchUtils;
import junit.framework.TestCase;

import java.util.*;

public class ArrayDiffTest extends TestCase {
    private ArrayDiff arrayDiff;
    @Override
    protected void setUp() throws Exception {
        arrayDiff = new ArrayDiff();
    }

    public void test_starts_with() throws Exception {
        Object[] longArr = {1,2,3,4,5};
        Object[] shortArr = {1,2};

        TestCase.assertEquals("test starts with. Found case ", true, ArrayUtils.startsWith(longArr, shortArr));

        shortArr = new Object[]{2,3};
        assertEquals("test starts with. Not found case ", false, ArrayUtils.startsWith(longArr, shortArr));

        assertEquals("test starts with. Out of index not found case", false,
                ArrayUtils.startsWith(shortArr, longArr));

        shortArr = new Object[]{3,4,5};
        assertEquals("test ends with. Found case", true, ArrayUtils.endsWith(longArr,shortArr));

        shortArr = new Object[]{3,4,6};
        assertEquals("test ends with. Not found case", false, ArrayUtils.endsWith(longArr,shortArr));

        assertEquals("test ends with. Out of index not found case", false,
                ArrayUtils.startsWith(shortArr, longArr));
    }

    public void test_kmp_build_table() throws Exception {
        String expectedValue = ArrayUtils.join(",", new Object[]{-1, 0, 0, 0, 0, 1, 2});
        int[] table = SearchUtils.kmp_build_tablelookup(new Object[]{1, 2, 3, 4, 1, 2, 3});
        Integer[] I_table = new Integer[table.length];
        for(int i = 0; i < table.length; ++i)
            I_table[i] = table[i];
        String realValue = ArrayUtils.join(",", I_table);
        assertEquals("test kmp build table: ",expectedValue, realValue);
    }

    public void test_kmp_search() throws Exception {
        String[] arr = {"a","a","b","a","a","a","b"};
        String[] word = {"a","b"};
        assertEquals("test kmp build table: found case",1, SearchUtils.kmp_search(arr,word,0));

        String[] word2 = {"b","b"};
        assertEquals("test kmp build table: not found case",-1, SearchUtils.kmp_search(arr,word2,0));
    }


    public void testDiff_Diff_equals() throws Exception {
        Diff diff_a = new Diff(ArrayDiff.Operation.ADDED, new Object[]{1,2,3,4});
        Diff diff_b = new Diff(ArrayDiff.Operation.ADDED, new Object[]{1,2,3,4});
        assertEquals("Diff_equals: equals case. ", true, diff_a.equals(diff_b));

        Diff diff_c = new Diff(ArrayDiff.Operation.ADDED, new Object[]{1,2,3});
        assertEquals("Diff_equals: not equals case. ", false, diff_a.equals(diff_c));

//        assertEquals("Diff_equals: equals case. ", false, diff_a.array.equals(diff_b.array));
    }

    public void testDiff_commonPrefix() throws Exception {
        String[] abc = ArrayUtils.toArray("abc");
        String[] xyz = ArrayUtils.toArray("xyz");
        String[] _1234abcdef = ArrayUtils.toArray("1234abcdef");
        String[] _1234xyz = ArrayUtils.toArray("1234xyz");
        String[] _1234 = ArrayUtils.toArray("1234");

        assertEquals("diff_commonPrefix: Null case.", 0, arrayDiff.diff_commonPrefix(abc, xyz));

        assertEquals("diff_commonPrefix: Non-null case.", 4, arrayDiff.diff_commonPrefix(_1234abcdef, _1234xyz));

        assertEquals("diff_commonPrefix: Whole case.", 4, arrayDiff.diff_commonPrefix(_1234, _1234xyz));
        assertEquals("diff_commonPrefix: Whole case.", 4, arrayDiff.diff_commonPrefix(_1234xyz, _1234));
    }

    public void testDiff_commonSuffix() throws Exception {
        String[] abc = ArrayUtils.toArray("abc");
        String[] xyz = ArrayUtils.toArray("xyz");
        String[] abcdef1234 = ArrayUtils.toArray("abcdef1234");
        String[] xyz1234 = ArrayUtils.toArray("xyz1234");
        String[] _1234 = ArrayUtils.toArray("1234");

        assertEquals("diff_commonSuffix: Null case.", 0, arrayDiff.diff_commonSuffix(abc, xyz));

        assertEquals("diff_commonSuffix: Non-null case.", 4, arrayDiff.diff_commonSuffix(abcdef1234, xyz1234));

        assertEquals("diff_commonSuffix: Whole case.", 4, arrayDiff.diff_commonSuffix(_1234, xyz1234));

    }

    public void testDiffHalfmatch() {
        // Detect a halfmatch.
        String[] _1234567890 = ArrayUtils.toArray("1234567890");
        String[] abcdef = ArrayUtils.toArray("abcdef");
        assertNull("diff_halfMatch: No match.", arrayDiff.diff_halfMatch(_1234567890, abcdef));
        String[] a345678z = ArrayUtils.toArray("a345678z");

        List<String[]> expected = new ArrayList<String[]>();
        expected.add(ArrayUtils.toArray("12"));
        expected.add(ArrayUtils.toArray("90"));
        expected.add(ArrayUtils.toArray("a"));
        expected.add(ArrayUtils.toArray("z"));
        expected.add(ArrayUtils.toArray("345678"));

        List<String[]> actual = new ArrayList<String[]>();
        actual = arrayDiff.diff_halfMatch(_1234567890, a345678z);
        assertArrayTEquals("diff_halfMatch: Single Match #1.", expected, actual);

        expected.clear();
        expected.add(ArrayUtils.toArray("a"));
        expected.add(ArrayUtils.toArray("z"));
        expected.add(ArrayUtils.toArray("12"));
        expected.add(ArrayUtils.toArray("90"));
        expected.add(ArrayUtils.toArray("345678"));
        actual = arrayDiff.diff_halfMatch(a345678z, _1234567890);
        assertArrayTEquals("diff_halfMatch: Single Match #2.", expected, actual);

        String[] _121231234123451234123121 = ArrayUtils.toArray("121231234123451234123121");
        String[] a1234123451234z = ArrayUtils.toArray("a1234123451234z");
        expected.clear();
        expected.add(ArrayUtils.toArray("12123"));
        expected.add(ArrayUtils.toArray("123121"));
        expected.add(ArrayUtils.toArray("a"));
        expected.add(ArrayUtils.toArray("z"));
        expected.add(ArrayUtils.toArray("1234123451234"));
        actual = arrayDiff.diff_halfMatch(_121231234123451234123121, a1234123451234z);
        assertArrayTEquals("diff_halfMatch: Multiple Matches #1.", expected, actual);

        String[] x = ArrayUtils.toArray("x-=-=-=-=-=-=-=-=-=-=-=-=");
        String[] xx = ArrayUtils.toArray("xx-=-=-=-=-=-=-=");
        expected.clear();
        expected.add(ArrayUtils.toArray(""));
        expected.add(ArrayUtils.toArray("-=-=-=-=-="));
        expected.add(ArrayUtils.toArray("x"));
        expected.add(ArrayUtils.toArray(""));
        expected.add(ArrayUtils.toArray("x-=-=-=-=-=-=-="));
        actual = arrayDiff.diff_halfMatch(x, xx);
        assertArrayTEquals("diff_halfMatch: Multiple Matches #2.", expected, actual);

        String[] _y = ArrayUtils.toArray("-=-=-=-=-=-=-=-=-=-=-=-=y");
        String[] _yy = ArrayUtils.toArray("-=-=-=-=-=-=-=yy");
        expected.clear();
        expected.add(ArrayUtils.toArray("-=-=-=-=-="));
        expected.add(ArrayUtils.toArray(""));
        expected.add(ArrayUtils.toArray(""));
        expected.add(ArrayUtils.toArray("y"));
        expected.add(ArrayUtils.toArray("-=-=-=-=-=-=-=y"));
        actual = arrayDiff.diff_halfMatch(_y, _yy);
        assertArrayTEquals("diff_halfMatch: Multiple Matches #3.", expected, actual);

    }

    public void test_diff_cleanupMerge() {
        // Cleanup a messy diff.
        LinkedList<Diff<String>> diffs = diffList();
        arrayDiff.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: Null case.", diffList(), diffs);

        diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("a")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("b")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("c")));
        LinkedList<Diff<String>> diffs2 = diffList();
        diffs2.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("a")));
        diffs2.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("b")));
        diffs2.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("c")));
        arrayDiff.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: No change case.", diffs2, diffs);

        diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("a")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("b")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("c")));
        diffs2 = diffList();
        diffs2.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("abc")));
        arrayDiff.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: Merge equalities.", diffs2, diffs);

        diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("a")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("b")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("c")));
        diffs2 = diffList();
        diffs2.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("abc")));
        arrayDiff.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: Merge deletions.", diffs2, diffs);

        diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("a")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("b")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("c")));
        diffs2 = diffList();
        diffs2.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("abc")));
        arrayDiff.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: Merge insertions.", diffs2, diffs);

        diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("a")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("b")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("c")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("d")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("e")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("f")));
        diffs2 = diffList();
        diffs2.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("ac")));
        diffs2.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("bd")));
        diffs2.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("ef")));
        arrayDiff.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: Merge interweave.", diffs2, diffs);

        diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("a")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("abc")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("dc")));
        diffs2 = diffList();
        diffs2.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("a")));
        diffs2.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("d")));
        diffs2.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("b")));
        diffs2.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("c")));
        arrayDiff.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: Prefix and suffix detection.", diffs2, diffs);

        diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("a")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("ba")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("c")));
        diffs2 = diffList();
        diffs2.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("ab")));
        diffs2.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("ac")));
        arrayDiff.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: Slide edit left.", diffs2, diffs);

        diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("c")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("ab")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("a")));
        diffs2 = diffList();
        diffs2.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("ca")));
        diffs2.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("ba")));
        arrayDiff.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: Slide edit right.", diffs2, diffs);

        diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("a")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("b")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("c")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("ac")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("x")));
        diffs2 = diffList();
        diffs2.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("abc")));
        diffs2.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("acx")));
        arrayDiff.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: Slide edit left recursive.", diffs2, diffs);

        diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("x")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("ca")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("c")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("b")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("a")));
        diffs2 = diffList();
        diffs2.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("xca")));
        diffs2.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("cba")));
        arrayDiff.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: Slide edit right recursive.", diffs2, diffs);
    }

    public void test_diff_path() {
        // First, check footprints are different.
        assertTrue("diff_footprint:", arrayDiff.diff_footprint(1, 10) != arrayDiff.diff_footprint(10, 1));

        // Single letters.
        // Trace a path from back to front.
        List<Set<Long>> v_map;
        Set<Long> row_set;
        v_map = new ArrayList<Set<Long>>();
        {
            row_set = new HashSet<Long>();
            row_set.add(arrayDiff.diff_footprint(0, 0));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(arrayDiff.diff_footprint(0, 1));
            row_set.add(arrayDiff.diff_footprint(1, 0));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(arrayDiff.diff_footprint(0, 2));
            row_set.add(arrayDiff.diff_footprint(2, 0));
            row_set.add(arrayDiff.diff_footprint(2, 2));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(arrayDiff.diff_footprint(0, 3));
            row_set.add(arrayDiff.diff_footprint(2, 3));
            row_set.add(arrayDiff.diff_footprint(3, 0));
            row_set.add(arrayDiff.diff_footprint(4, 3));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(arrayDiff.diff_footprint(0, 4));
            row_set.add(arrayDiff.diff_footprint(2, 4));
            row_set.add(arrayDiff.diff_footprint(4, 0));
            row_set.add(arrayDiff.diff_footprint(4, 4));
            row_set.add(arrayDiff.diff_footprint(5, 3));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(arrayDiff.diff_footprint(0, 5));
            row_set.add(arrayDiff.diff_footprint(2, 5));
            row_set.add(arrayDiff.diff_footprint(4, 5));
            row_set.add(arrayDiff.diff_footprint(5, 0));
            row_set.add(arrayDiff.diff_footprint(6, 3));
            row_set.add(arrayDiff.diff_footprint(6, 5));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(arrayDiff.diff_footprint(0, 6));
            row_set.add(arrayDiff.diff_footprint(2, 6));
            row_set.add(arrayDiff.diff_footprint(4, 6));
            row_set.add(arrayDiff.diff_footprint(6, 6));
            row_set.add(arrayDiff.diff_footprint(7, 5));
            v_map.add(row_set);
        }
        LinkedList<Diff<String>> diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("W")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("A")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("1")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("B")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("2")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("X")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("C")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("3")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("D")));
        assertEquals("diff_path1: Single letters.", diffs, arrayDiff.diff_path1(v_map,
                ArrayUtils.toArray("A1B2C3D"), ArrayUtils.toArray("W12X3")));

        // Trace a path from front to back.
        v_map.remove(v_map.size() - 1);
        diffs.clear();
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("4")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("E")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("Y")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("5")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("F")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("6")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("G")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("Z")));
        assertEquals("diff_path2: Single letters.", diffs, arrayDiff.diff_path2(v_map,
                ArrayUtils.toArray("4E5F6G"), ArrayUtils.toArray("4Y56Z")));

        // Double letters.
        // Trace a path from back to front.
        v_map = new ArrayList<Set<Long>>();
        {
            row_set = new HashSet<Long>();
            row_set.add(arrayDiff.diff_footprint(0, 0));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(arrayDiff.diff_footprint(0, 1));
            row_set.add(arrayDiff.diff_footprint(1, 0));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(arrayDiff.diff_footprint(0, 2));
            row_set.add(arrayDiff.diff_footprint(1, 1));
            row_set.add(arrayDiff.diff_footprint(2, 0));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(arrayDiff.diff_footprint(0, 3));
            row_set.add(arrayDiff.diff_footprint(1, 2));
            row_set.add(arrayDiff.diff_footprint(2, 1));
            row_set.add(arrayDiff.diff_footprint(3, 0));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(arrayDiff.diff_footprint(0, 4));
            row_set.add(arrayDiff.diff_footprint(1, 3));
            row_set.add(arrayDiff.diff_footprint(3, 1));
            row_set.add(arrayDiff.diff_footprint(4, 0));
            row_set.add(arrayDiff.diff_footprint(4, 4));
            v_map.add(row_set);
        }
        diffs.clear();
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("WX")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("AB")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("12")));
        assertEquals("diff_path1: Double letters.", diffs, arrayDiff.diff_path1(v_map,
                ArrayUtils.toArray("AB12"), ArrayUtils.toArray("WX12")));

        // Trace a path from front to back.
        v_map = new ArrayList<Set<Long>>();
        {
            row_set = new HashSet<Long>();
            row_set.add(arrayDiff.diff_footprint(0, 0));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(arrayDiff.diff_footprint(0, 1));
            row_set.add(arrayDiff.diff_footprint(1, 0));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(arrayDiff.diff_footprint(1, 1));
            row_set.add(arrayDiff.diff_footprint(2, 0));
            row_set.add(arrayDiff.diff_footprint(2, 4));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(arrayDiff.diff_footprint(2, 1));
            row_set.add(arrayDiff.diff_footprint(2, 5));
            row_set.add(arrayDiff.diff_footprint(3, 0));
            row_set.add(arrayDiff.diff_footprint(3, 4));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(arrayDiff.diff_footprint(2, 6));
            row_set.add(arrayDiff.diff_footprint(3, 5));
            row_set.add(arrayDiff.diff_footprint(4, 4));
            v_map.add(row_set);
        }
        diffs.clear();
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("CD")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("34")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("YZ")));
        assertEquals("diff_path2: Double letters.", diffs, arrayDiff.diff_path2(v_map,
                ArrayUtils.toArray("CD34"), ArrayUtils.toArray("34YZ")));
    }

    public void test_diff_main() {
        // Perform a trivial diff.
        LinkedList<Diff<String>> diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("abc")));
        assertEquals("diff_main: Null case.", diffs, arrayDiff.diff_main(ArrayUtils.toArray("abc"),
                ArrayUtils.toArray("abc")));

        diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("ab")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("123")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("c")));
        assertEquals("diff_main: Simple insertion.", diffs, arrayDiff.diff_main(ArrayUtils.toArray("abc"),
                ArrayUtils.toArray("ab123c")));

        diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("a")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("123")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("bc")));
        assertEquals("diff_main: Simple deletion.", diffs, arrayDiff.diff_main(ArrayUtils.toArray("a123bc"),
                ArrayUtils.toArray("abc")));

        diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("a")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("123")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("b")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("456")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("c")));
        assertEquals("diff_main: Two insertions.", diffs, arrayDiff.diff_main(ArrayUtils.toArray("abc"),
                ArrayUtils.toArray("a123b456c")));

        diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("a")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("123")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("b")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("456")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("c")));
        assertEquals("diff_main: Two deletions.", diffs, arrayDiff.diff_main(ArrayUtils.toArray("a123b456c"),
                ArrayUtils.toArray("abc")));

        // Perform a real diff.
        // Switch off the timeout.
        arrayDiff.Diff_Timeout = 0;
        arrayDiff.Diff_DualThreshold = 32;
        diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("a")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("b")));
        assertEquals("diff_main: Simple case #1.", diffs, arrayDiff.diff_main(ArrayUtils.toArray("a"),
                ArrayUtils.toArray("b")));

        diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("Apple")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("Banana")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("s are a")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("lso")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray(" fruit.")));
        assertEquals("diff_main: Simple case #2.", diffs, arrayDiff.diff_main(ArrayUtils.toArray("Apples are a fruit."),
                ArrayUtils.toArray("Bananas are also fruit.")));

        diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("a")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("\u0680")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("x")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("\t")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("\000")));
        assertEquals("diff_main: Simple case #3.", diffs, arrayDiff.diff_main(ArrayUtils.toArray("ax\t"),
                ArrayUtils.toArray("\u0680x\000")));

        diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("1")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("a")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("y")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("b")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("2")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("xab")));
        assertEquals("diff_main: Overlap #1.", diffs, arrayDiff.diff_main(ArrayUtils.toArray("1ayb2"),
                ArrayUtils.toArray("abxab")));

        diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("xaxcx")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("abc")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("y")));
        assertEquals("diff_main: Overlap #2.", diffs, arrayDiff.diff_main(ArrayUtils.toArray("abcy"),
                ArrayUtils.toArray("xaxcxabc")));

        // Sub-optimal double-ended diff.
        arrayDiff.Diff_DualThreshold = 2;
        diffs = diffList();
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("x")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("a")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("b")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("x")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.UNCHANGED, ArrayUtils.toArray("c")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.REMOVED, ArrayUtils.toArray("y")));
        diffs.add(new Diff<String>(ArrayDiff.Operation.ADDED, ArrayUtils.toArray("xabc")));
        assertEquals("diff_main: Overlap #3.", diffs, arrayDiff.diff_main(ArrayUtils.toArray("abcy"),
                ArrayUtils.toArray("xaxcxabc")));
    }

    public <T> void assertArrayTEquals(String message, List<T[]> expected, List<T[]> actual) {
        boolean ret = expected.size() == actual.size();
        if(ret) {
            for(int i = 0; i < expected.size(); ++i) {
                for(int j = 0; j < expected.get(i).length; ++j) {
                    ret = expected.get(i)[j].equals(actual.get(i)[j]);
                    if(!ret) break;
                }
            }
        }
        assertTrue(message, ret);
    }

    private static <T> LinkedList<Diff<T>> diffList(Diff... diffs) {
        LinkedList<Diff<T>> myDiffList = new LinkedList<Diff<T>>();
        for (Diff myDiff : diffs) {
            myDiffList.add(myDiff);
        }
        return myDiffList;
    }
}