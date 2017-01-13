package test.diff;

import diff.*;
import junit.framework.TestCase;

import java.util.*;

/**
 * Created by apple on 1/11/17.
 */
public class DiffProcessorTest extends TestCase {
    private DiffProcessor<Character> processor;
    private DiffProcessor.Operation DELETE = DiffProcessor.Operation.DELETE;
    private DiffProcessor.Operation EQUAL = DiffProcessor.Operation.EQUAL;
    private DiffProcessor.Operation INSERT = DiffProcessor.Operation.INSERT;
    @Override
    protected void setUp() throws Exception {
        processor = new DiffProcessor<Character>(Character.class);
    }

    public void test_starts_with() throws Exception {
        KKString<Integer> longArr = new KKString<Integer>(1,2,3,4,5);
        KKString<Integer> shortArr = new KKString<Integer>(1,2);

        TestCase.assertEquals("test starts with. Found case ", true, longArr.startsWith(shortArr));

        shortArr = new KKString<Integer>( 2,3 );
        assertEquals("test starts with. Not found case ", false, longArr.startsWith(shortArr));

        assertEquals("test starts with. Out of index not found case", false,
                shortArr.startsWith(longArr));

        shortArr = new KKString<Integer>( 3,4,5);
        assertEquals("test ends with. Found case", true, longArr.endsWith(shortArr));

        shortArr = new KKString<Integer>( 3,4,6);
        assertEquals("test ends with. Not found case", false, longArr.endsWith(shortArr));

        assertEquals("test ends with. Out of index not found case", false,
                shortArr.startsWith(longArr));
    }

    public void testDiff_Diff_equals() throws Exception {
        DiffProcessor.Diff diff_a = new DiffProcessor.Diff(DELETE,
                new KKString<Integer>(1, 2, 3, 4));
        DiffProcessor.Diff diff_b = new DiffProcessor.Diff(DELETE,
                new KKString<Integer>(1, 2, 3, 4));
        assertEquals("Diff_equals: equals case. ", true, diff_a.equals(diff_b));

        DiffProcessor.Diff diff_c = new DiffProcessor.Diff(INSERT,
                new KKString<Integer>(1, 2, 3, 4));
        assertEquals("Diff_equals: not equals case. ", false, diff_a.equals(diff_c));

//        assertEquals("Diff_equals: equals case. ", false, diff_a.array.equals(diff_b.array));
    }

    public void testDiff_commonPrefix() {
        KKString<Character> abc = new KKString<Character>(ArrayUtils.toChars("abc"));
        KKString<Character> xyz = new KKString<Character>(ArrayUtils.toChars("xyz"));
        KKString<Character> _1234abcdef = new KKString<Character>(ArrayUtils.toChars("1234abcdef"));
        KKString<Character> _1234xyz = new KKString<Character>(ArrayUtils.toChars("1234xyz"));
        KKString<Character> _1234 = new KKString<Character>(ArrayUtils.toChars("1234"));

        assertEquals("diff_commonPrefix: Null case.", 0, processor.diff_commonPrefix(abc, xyz));

        assertEquals("diff_commonPrefix: Non-null case.", 4, processor.diff_commonPrefix(_1234abcdef, _1234xyz));

        assertEquals("diff_commonPrefix: Whole case.", 4, processor.diff_commonPrefix(_1234, _1234xyz));
        assertEquals("diff_commonPrefix: Whole case.", 4, processor.diff_commonPrefix(_1234xyz, _1234));
    }

    public void testDiff_commonSuffix() {
        KKString<Character> abc = new KKString<Character>(ArrayUtils.toChars("abc"));
        KKString<Character> xyz = new KKString<Character>(ArrayUtils.toChars("xyz"));
        KKString<Character> abcdef1234 = new KKString<Character>(ArrayUtils.toChars("abcdef1234"));
        KKString<Character> xyz1234 = new KKString<Character>(ArrayUtils.toChars("xyz1234"));
        KKString<Character> _1234 = new KKString<Character>(ArrayUtils.toChars("1234"));

        assertEquals("diff_commonSuffix: Null case.", 0, processor.diff_commonSuffix(abc, xyz));

        assertEquals("diff_commonSuffix: Non-null case.", 4, processor.diff_commonSuffix(abcdef1234, xyz1234));

        assertEquals("diff_commonSuffix: Whole case.", 4, processor.diff_commonSuffix(_1234, xyz1234));
    }

    public void testDiffHalfmatch() {
        // Detect a halfmatch.
        KKString<Character> _1234567890 = new KKString<Character>(ArrayUtils.toChars("1234567890"));
        KKString<Character> abcdef = new KKString<Character>(ArrayUtils.toChars("abcdef"));
        assertNull("diff_halfMatch: No match.", processor.diff_halfMatch(_1234567890, abcdef));
        KKString<Character> a345678z = new KKString<Character>(ArrayUtils.toChars("a345678z"));
//
        KKString<Character>[] expected = new KKString[] {
                new KKString<Character>(ArrayUtils.toChars("12")),
                new KKString<Character>(ArrayUtils.toChars("90")),
                new KKString<Character>(ArrayUtils.toChars("a")),
                new KKString<Character>(ArrayUtils.toChars("z")),
                new KKString<Character>(ArrayUtils.toChars("345678"))
                };
//
        KKString<Character>[] actual = processor.diff_halfMatch(_1234567890, a345678z);
        assertArrayEquals("diff_halfMatch: Single Match #1.", expected, actual);

        expected = new KKString[] {
                new KKString<Character>(ArrayUtils.toChars("a")),
                new KKString<Character>(ArrayUtils.toChars("z")),
                new KKString<Character>(ArrayUtils.toChars("12")),
                new KKString<Character>(ArrayUtils.toChars("90")),
                new KKString<Character>(ArrayUtils.toChars("345678"))
        };

        actual = processor.diff_halfMatch(a345678z, _1234567890);
        assertArrayEquals("diff_halfMatch: Single Match #2.", expected, actual);
//
        KKString<Character> _121231234123451234123121 = new KKString<Character>(ArrayUtils.toChars("121231234123451234123121"));
        KKString<Character> a1234123451234z = new KKString<Character>(ArrayUtils.toChars("a1234123451234z"));
        expected = new KKString[] {
                new KKString<Character>(ArrayUtils.toChars("12123")),
                new KKString<Character>(ArrayUtils.toChars("123121")),
                new KKString<Character>(ArrayUtils.toChars("a")),
                new KKString<Character>(ArrayUtils.toChars("z")),
                new KKString<Character>(ArrayUtils.toChars("1234123451234"))
        };
        actual = processor.diff_halfMatch(_121231234123451234123121, a1234123451234z);
        assertArrayEquals("diff_halfMatch: Multiple Matches #1.", expected, actual);

        KKString<Character> x = new KKString<Character>(ArrayUtils.toChars("x-=-=-=-=-=-=-=-=-=-=-=-="));
        KKString<Character> xx = new KKString<Character>(ArrayUtils.toChars("xx-=-=-=-=-=-=-="));
        expected = new KKString[] {
                new KKString<Character>(ArrayUtils.toChars("")),
                new KKString<Character>(ArrayUtils.toChars("-=-=-=-=-=")),
                new KKString<Character>(ArrayUtils.toChars("x")),
                new KKString<Character>(ArrayUtils.toChars("")),
                new KKString<Character>(ArrayUtils.toChars("x-=-=-=-=-=-=-="))
        };
        actual = processor.diff_halfMatch(x, xx);
        assertArrayEquals("diff_halfMatch: Multiple Matches #2.", expected, actual);

        KKString<Character> _y = new KKString<Character>(ArrayUtils.toChars("-=-=-=-=-=-=-=-=-=-=-=-=y"));
        KKString<Character> _yy = new KKString<Character>(ArrayUtils.toChars("-=-=-=-=-=-=-=yy"));
        expected = new KKString[] {
                new KKString<Character>(ArrayUtils.toChars("-=-=-=-=-=")),
                new KKString<Character>(ArrayUtils.toChars("")),
                new KKString<Character>(ArrayUtils.toChars("")),
                new KKString<Character>(ArrayUtils.toChars("y")),
                new KKString<Character>(ArrayUtils.toChars("-=-=-=-=-=-=-=y"))
        };
        actual = processor.diff_halfMatch(_y, _yy);
        assertArrayEquals("diff_halfMatch: Multiple Matches #3.", expected, actual);

    }

    public void test_diff_path() {
        // First, check footprints are different.
        assertTrue("diff_footprint:", processor.diff_footprint(1, 10) != processor.diff_footprint(10, 1));

        // Single letters.
        // Trace a path from back to front.
        List<Set<Long>> v_map;
        Set<Long> row_set;
        v_map = new ArrayList<Set<Long>>();
        {
            row_set = new HashSet<Long>();
            row_set.add(processor.diff_footprint(0, 0));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(processor.diff_footprint(0, 1));
            row_set.add(processor.diff_footprint(1, 0));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(processor.diff_footprint(0, 2));
            row_set.add(processor.diff_footprint(2, 0));
            row_set.add(processor.diff_footprint(2, 2));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(processor.diff_footprint(0, 3));
            row_set.add(processor.diff_footprint(2, 3));
            row_set.add(processor.diff_footprint(3, 0));
            row_set.add(processor.diff_footprint(4, 3));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(processor.diff_footprint(0, 4));
            row_set.add(processor.diff_footprint(2, 4));
            row_set.add(processor.diff_footprint(4, 0));
            row_set.add(processor.diff_footprint(4, 4));
            row_set.add(processor.diff_footprint(5, 3));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(processor.diff_footprint(0, 5));
            row_set.add(processor.diff_footprint(2, 5));
            row_set.add(processor.diff_footprint(4, 5));
            row_set.add(processor.diff_footprint(5, 0));
            row_set.add(processor.diff_footprint(6, 3));
            row_set.add(processor.diff_footprint(6, 5));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(processor.diff_footprint(0, 6));
            row_set.add(processor.diff_footprint(2, 6));
            row_set.add(processor.diff_footprint(4, 6));
            row_set.add(processor.diff_footprint(6, 6));
            row_set.add(processor.diff_footprint(7, 5));
            v_map.add(row_set);
        }
        LinkedList<DiffProcessor.Diff<Character>> diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, ArrayUtils.toChars("W")));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("A")));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("1")));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("B")));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("2")));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, ArrayUtils.toChars("X")));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("C")));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("3")));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("D")));
        assertEquals("diff_path1: Single letters.", diffs, processor.diff_path1(v_map,
                new KKString<Character>(ArrayUtils.toChars("A1B2C3D")),
                new KKString<Character>(ArrayUtils.toChars("W12X3"))));

        // Trace a path from front to back.
        v_map.remove(v_map.size() - 1);
        diffs.clear();
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("4")));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("E")));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, ArrayUtils.toChars("Y")));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("5")));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("F")));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("6")));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("G")));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, ArrayUtils.toChars("Z")));

        assertEquals("diff_path2: Single letters.", diffs, processor.diff_path2(v_map,
                new KKString<Character>(ArrayUtils.toChars("4E5F6G")),
                new KKString<Character>(ArrayUtils.toChars("4Y56Z"))));
//
        // Double letters.
        // Trace a path from back to front.
        v_map = new ArrayList<Set<Long>>();
        {
            row_set = new HashSet<Long>();
            row_set.add(processor.diff_footprint(0, 0));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(processor.diff_footprint(0, 1));
            row_set.add(processor.diff_footprint(1, 0));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(processor.diff_footprint(0, 2));
            row_set.add(processor.diff_footprint(1, 1));
            row_set.add(processor.diff_footprint(2, 0));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(processor.diff_footprint(0, 3));
            row_set.add(processor.diff_footprint(1, 2));
            row_set.add(processor.diff_footprint(2, 1));
            row_set.add(processor.diff_footprint(3, 0));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(processor.diff_footprint(0, 4));
            row_set.add(processor.diff_footprint(1, 3));
            row_set.add(processor.diff_footprint(3, 1));
            row_set.add(processor.diff_footprint(4, 0));
            row_set.add(processor.diff_footprint(4, 4));
            v_map.add(row_set);
        }
        diffs.clear();
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, ArrayUtils.toChars("WX")));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("AB")));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("12")));
        assertEquals("diff_path1: Double letters.", diffs, processor.diff_path1(v_map,
                new KKString<Character>(ArrayUtils.toChars("AB12")),
                new KKString<Character>(ArrayUtils.toChars("WX12"))));

        // Trace a path from front to back.
        v_map = new ArrayList<Set<Long>>();
        {
            row_set = new HashSet<Long>();
            row_set.add(processor.diff_footprint(0, 0));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(processor.diff_footprint(0, 1));
            row_set.add(processor.diff_footprint(1, 0));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(processor.diff_footprint(1, 1));
            row_set.add(processor.diff_footprint(2, 0));
            row_set.add(processor.diff_footprint(2, 4));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(processor.diff_footprint(2, 1));
            row_set.add(processor.diff_footprint(2, 5));
            row_set.add(processor.diff_footprint(3, 0));
            row_set.add(processor.diff_footprint(3, 4));
            v_map.add(row_set);
            row_set = new HashSet<Long>();
            row_set.add(processor.diff_footprint(2, 6));
            row_set.add(processor.diff_footprint(3, 5));
            row_set.add(processor.diff_footprint(4, 4));
            v_map.add(row_set);
        }
        diffs.clear();
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("CD")));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("34")));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, ArrayUtils.toChars("YZ")));
        assertEquals("diff_path2: Double letters.", diffs, processor.diff_path2(v_map,
                new KKString<Character>(ArrayUtils.toChars("CD34")),
                new KKString<Character>(ArrayUtils.toChars("34YZ"))));
    }

    public void test_diff_cleanupMerge() {
        // Cleanup a messy diff.
        LinkedList<DiffProcessor.Diff<Character>> diffs = diffList();
        processor.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: Null case.", diffList(), diffs);

        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("a")));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, ArrayUtils.toChars("b")));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("cd")));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("e")));
        LinkedList<DiffProcessor.Diff<Character>>diffs2 = diffList();
        diffs2.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("a")));
        diffs2.add(new DiffProcessor.Diff<Character>(INSERT, ArrayUtils.toChars("b")));
        diffs2.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("cd")));
        diffs2.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("e")));
        processor.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: No change case.", diffs2, diffs);

        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("a")));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("b")));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("c")));
        diffs2 = diffList();
        diffs2.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("abc")));
        processor.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: Merge equalities.", diffs2, diffs);

        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("a")));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("b")));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("c")));
        diffs2 = diffList();
        diffs2.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("abc")));
        processor.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: Merge deletions.", diffs2, diffs);

        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, ArrayUtils.toChars("a")));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, ArrayUtils.toChars("b")));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, ArrayUtils.toChars("c")));
        diffs2 = diffList();
        diffs2.add(new DiffProcessor.Diff<Character>(INSERT, ArrayUtils.toChars("abc")));
        processor.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: Merge insertions.", diffs2, diffs);
//
        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("a")));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, ArrayUtils.toChars("b")));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("c")));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, ArrayUtils.toChars("d")));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("e")));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("f")));
        diffs2 = diffList();
        diffs2.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("ac")));
        diffs2.add(new DiffProcessor.Diff<Character>(INSERT, ArrayUtils.toChars("bd")));
        diffs2.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("ef")));
        processor.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: Merge interweave.", diffs2, diffs);

        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("a")));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, ArrayUtils.toChars("abc")));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("dc")));
        diffs2 = diffList();
        diffs2.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("a")));
        diffs2.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("d")));
        diffs2.add(new DiffProcessor.Diff<Character>(INSERT, ArrayUtils.toChars("b")));
        diffs2.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("c")));
        processor.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: Prefix and suffix detection.", diffs2, diffs);

        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("a")));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, ArrayUtils.toChars("ba")));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("c")));
        diffs2 = diffList();
        diffs2.add(new DiffProcessor.Diff<Character>(INSERT, ArrayUtils.toChars("ab")));
        diffs2.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("ac")));
        processor.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: Slide edit left.", diffs2, diffs);

        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("c")));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, ArrayUtils.toChars("ab")));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("a")));
        diffs2 = diffList();
        diffs2.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("ca")));
        diffs2.add(new DiffProcessor.Diff<Character>(INSERT, ArrayUtils.toChars("ba")));
        processor.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: Slide edit right.", diffs2, diffs);

        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("a")));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("b")));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("c")));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("ac")));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("x")));
        diffs2 = diffList();
        diffs2.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("abc")));
        diffs2.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("acx")));
        processor.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: Slide edit left recursive.", diffs2, diffs);

        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("x")));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("ca")));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("c")));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("b")));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("a")));
        diffs2 = diffList();
        diffs2.add(new DiffProcessor.Diff<Character>(EQUAL, ArrayUtils.toChars("xca")));
        diffs2.add(new DiffProcessor.Diff<Character>(DELETE, ArrayUtils.toChars("cba")));
        processor.diff_cleanupMerge(diffs);
        assertEquals("diff_cleanupMerge: Slide edit right recursive.", diffs2, diffs);
    }

    public void testDiffLinesToChars() {
        // Convert lines down to characters.
        KKString<Character> text1 = new KKString<Character>(ArrayUtils.toChars("aabbaa"));
        KKString<Character> text2 = new KKString<Character>(ArrayUtils.toChars("bbaabb"));
        DiffProcessor.LinesToCharsResult actual = processor.diff_linesToChars(text1, text2, 2, 2);
        List<KKString<Character>> list = new LinkedList<KKString<Character>>();
        list.add(new KKString<Character>(ArrayUtils.toChars("aa")));
        list.add(new KKString<Character>(ArrayUtils.toChars("bb")));
        DiffProcessor.LinesToCharsResult expected = new DiffProcessor.LinesToCharsResult(
                new KKString<Integer>(0,1,0),
                new KKString<Integer>(1,0,1),
                list
        );
        assertLinesToCharsResultEquals("diff_linesToChars:", expected, actual);

        text1 = new KKString<Character>(ArrayUtils.toChars("a"));
        text2 = new KKString<Character>(ArrayUtils.toChars("b"));
        actual = processor.diff_linesToChars(text1, text2, 2, 2);
        list = new LinkedList<KKString<Character>>();
        list.add(new KKString<Character>(ArrayUtils.toChars("a")));
        list.add(new KKString<Character>(ArrayUtils.toChars("b")));
        expected = new DiffProcessor.LinesToCharsResult(
                new KKString<Integer>(0),
                new KKString<Integer>(1),
                list
        );

        assertLinesToCharsResultEquals("diff_linesToChars:", expected, actual);
    }

    public void testDiffCharsToLines() {
        // First check that Diff equality works.
        assertTrue("diff_charsToLines:", new DiffProcessor.Diff<Character>(EQUAL, "a").
                equals(new DiffProcessor.Diff<Character>(EQUAL, "a")));

        assertEquals("diff_charsToLines:", new DiffProcessor.Diff<Character>(EQUAL, "a"),
                new DiffProcessor.Diff<Character>(EQUAL, "a"));

        LinkedList<DiffProcessor.Diff<Integer>> intDiffs = diffList();
        intDiffs.add(new DiffProcessor.Diff<Integer>(EQUAL, new KKString<Integer>(0,1,0)));
        intDiffs.add(new DiffProcessor.Diff<Integer>(INSERT, new KKString<Integer>(1)));
        List<KKString<Character>> list = new LinkedList<KKString<Character>>();
        list.add(new KKString<Character>(ArrayUtils.toChars("aa")));
        list.add(new KKString<Character>(ArrayUtils.toChars("bb")));

        LinkedList<DiffProcessor.Diff<Character>> actual = processor.diff_charsToLines(intDiffs, list);
        LinkedList<DiffProcessor.Diff<Character>> expected = diffList();
        expected.add(new DiffProcessor.Diff<Character>(EQUAL, new KKString<Character>("aabbaa")));
        expected.add(new DiffProcessor.Diff<Character>(INSERT, new KKString<Character>("bb")));
        assertEquals("diff_charsToLines:", expected, actual);

    }

    public void test_diff_CleanupSemantic() {
        LinkedList<DiffProcessor.Diff<Character>> diffs = diffList();
        processor.diff_cleanupSemantic(diffs);
        assertEquals("diff_cleanupSemantic: Null case.", diffList(), diffs);

        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "a"));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, "b"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "cd"));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "e"));

        LinkedList<DiffProcessor.Diff<Character>> diffs2 = diffList();
        diffs2.add(new DiffProcessor.Diff<Character>(DELETE, "a"));
        diffs2.add(new DiffProcessor.Diff<Character>(INSERT, "b"));
        diffs2.add(new DiffProcessor.Diff<Character>(EQUAL, "cd"));
        diffs2.add(new DiffProcessor.Diff<Character>(DELETE, "e"));
        processor.diff_cleanupSemantic(diffs);
        assertEquals("diff_cleanupSemantic: No elimination.", diffs2, diffs);

        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "a"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "b"));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "c"));
        diffs2 = diffList();
        diffs2.add(new DiffProcessor.Diff<Character>(DELETE, "abc"));
        diffs2.add(new DiffProcessor.Diff<Character>(INSERT, "b"));
        processor.diff_cleanupSemantic(diffs);
        assertEquals("diff_cleanupSemantic: Simple elimination.", diffs2, diffs);

        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "ab"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "cd"));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "e"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "f"));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, "g"));
        diffs2 = diffList();
        diffs2.add(new DiffProcessor.Diff<Character>(DELETE, "abcdef"));
        diffs2.add(new DiffProcessor.Diff<Character>(INSERT, "cdfg"));
        processor.diff_cleanupSemantic(diffs);
        assertEquals("diff_cleanupSemantic: Backpass elimination.", diffs2, diffs);

        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, "1"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "A"));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "B"));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, "2"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "_"));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, "1"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "A"));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "B"));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, "2"));
        diffs2 = diffList();
        diffs2.add(new DiffProcessor.Diff<Character>(DELETE, "AB_AB"));
        diffs2.add(new DiffProcessor.Diff<Character>(INSERT, "1A2_1A2"));
        processor.diff_cleanupSemantic(diffs);
        assertEquals("diff_cleanupSemantic: Multiple elimination.", diffs2, diffs);

//        diffs = diffList();
//        diffs.add(new Diff<String>(ArrayDiff.Operation.EQUAL, ArrayUtils.toArray("The c")));
//        diffs.add(new Diff<String>(ArrayDiff.Operation.DELETE, ArrayUtils.toArray("ow and the c")));
//        diffs.add(new Diff<String>(ArrayDiff.Operation.EQUAL, ArrayUtils.toArray("at.")));
//        diffs2 = diffList();
//        diffs2.add(new Diff<String>(ArrayDiff.Operation.EQUAL, ArrayUtils.toArray("The ")));
//        diffs2.add(new Diff<String>(ArrayDiff.Operation.DELETE, ArrayUtils.toArray("cow and the ")));
//        diffs2.add(new Diff<String>(ArrayDiff.Operation.EQUAL, ArrayUtils.toArray("cat.")));
//        arrayDiff.diff_cleanupSemantic(diffs);
//        assertEquals("diff_cleanupSemantic: Word boundaries.", diffs2, diffs);
    }

    public void test_diff_main() {
        // Perform a trivial diff.
        LinkedList<DiffProcessor.Diff<Character>> diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "abc"));
        KKString<Character> text1 = new KKString<Character>("abc");
        KKString<Character> text2 = new KKString<Character>("abc");
        assertEquals("diff_main: Null case.", diffs, processor.diff_main(text1, text2));

        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "ab"));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, "123"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "c"));
        text1 = new KKString<Character>("abc");
        text2 = new KKString<Character>("ab123c");
        assertEquals("diff_main: Simple insertion.",diffs, processor.diff_main(text1, text2));

//
        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "a"));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "123"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "bc"));
        text1 = new KKString<Character>("a123bc");
        text2 = new KKString<Character>("abc");
        assertEquals("diff_main: Simple deletion.",diffs, processor.diff_main(text1, text2));

        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "a"));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, "123"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "b"));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, "456"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "c"));
        text1 = new KKString<Character>("abc");
        text2 = new KKString<Character>("a123b456c");
        assertEquals("diff_main: Two insertions.",diffs, processor.diff_main(text1, text2));

        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "a"));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "123"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "b"));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "456"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "c"));
        text1 = new KKString<Character>("a123b456c");
        text2 = new KKString<Character>("abc");
        assertEquals("diff_main: Two deletions.",diffs, processor.diff_main(text1, text2));
//
        // Perform a real diff.
        // Switch off the timeout.
        processor.Diff_Timeout = 0;
        processor.Diff_DualThreshold = 32;
        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "a"));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, "b"));
        text1 = new KKString<Character>("a");
        text2 = new KKString<Character>("b");
        assertEquals("diff_main: Simple case #1.", diffs, processor.diff_main(text1, text2));

        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "Apple"));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, "Banana"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "s are a"));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, "lso"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, " fruit."));
        text1 = new KKString<Character>("Apples are a fruit.");
        text2 = new KKString<Character>("Bananas are also fruit.");
        assertEquals("diff_main: Simple case #2.", diffs, processor.diff_main(text1, text2));

        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "a"));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, "\u0680"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "x"));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "\t"));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, "\000"));
        text1 = new KKString<Character>("ax\t");
        text2 = new KKString<Character>("\u0680x\000");
        assertEquals("diff_main: Simple case #3.", diffs, processor.diff_main(text1, text2));

        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "1"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "a"));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "y"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "b"));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "2"));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, "xab"));
        text1 = new KKString<Character>("1ayb2");
        text2 = new KKString<Character>("abxab");
        assertEquals("diff_main: Overlap #1.", diffs, processor.diff_main(text1, text2));

        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, "xaxcx"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "abc"));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "y"));
        text1 = new KKString<Character>("abcy");
        text2 = new KKString<Character>("xaxcxabc");
        assertEquals("diff_main: Overlap #2.", diffs, processor.diff_main(text1, text2));

        // Sub-optimal double-ended diff.
        processor.Diff_DualThreshold = 2;
        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, "x"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "a"));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "b"));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, "x"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "c"));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "y"));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, "xabc"));
        text1 = new KKString<Character>("abcy");
        text2 = new KKString<Character>("xaxcxabc");
        assertEquals("diff_main: Overlap #3.", diffs, processor.diff_main(text1, text2));

        processor.Diff_DualThreshold = 32;
        diffs = diffList();
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "F"));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "up"));
        diffs.add(new DiffProcessor.Diff<Character>(INSERT, "U"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "1b"));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "9"));
        diffs.add(new DiffProcessor.Diff<Character>(EQUAL, "2z"));
        diffs.add(new DiffProcessor.Diff<Character>(DELETE, "9"));
        text1 = new KKString<Character>("Fup1b92z9");
        text2 = new KKString<Character>("FU1b2z");
        assertEquals("diff_main: test 4.", diffs, processor.diff_main(text1, text2));
    }

    public void testString_mainDiff() {
        DiffProcessor<String> diffProcessor = new DiffProcessor<String>(String.class);
        LinkedList<DiffProcessor.Diff<String>> diffs = diffList();
        diffs.add(new DiffProcessor.Diff<String>(EQUAL, new String[]{"Facebook"}));
        diffs.add(new DiffProcessor.Diff<String>(DELETE, new String[]{"user Name",
                "password"}));
        diffs.add(new DiffProcessor.Diff<String>(INSERT, new String[]{"user Name 2"}));
        diffs.add(new DiffProcessor.Diff<String>(EQUAL, new String[]{"1",
                " bdhwmnj_huison_1473307995@tfbnw.net\n"}));
        diffs.add(new DiffProcessor.Diff<String>(DELETE, new String[]{"9furytest"}));
        diffs.add(new DiffProcessor.Diff<String>(EQUAL, new String[]{"2",
                "znpmdrz_goldmanberg_1473307997@tfbnw.net"}));
        diffs.add(new DiffProcessor.Diff<String>(DELETE, new String[]{"9furytest"}));

        KKString<String> text1 = new KKString<String>(new String[] {
                "Facebook",
                "user Name",
                "password",
                "1",
                " bdhwmnj_huison_1473307995@tfbnw.net\n",
                "9furytest",
                "2",
                "znpmdrz_goldmanberg_1473307997@tfbnw.net",
                "9furytest"
        });

        KKString<String> text2 = new KKString<String>(new String[] {
                "Facebook",
                "user Name 2",
                "1",
                " bdhwmnj_huison_1473307995@tfbnw.net\n",
                "2",
                "znpmdrz_goldmanberg_1473307997@tfbnw.net",
        });

        assertEquals("testString_mainDiff array ", diffs, diffProcessor.diff_main(text1, text2));

        ArrayList<String> list1 = new ArrayList<String>();
        list1.add(new String("Facebook"));
        list1.add(new String("user Name"));
        list1.add(new String("password"));
        list1.add(new String("1"));
        list1.add(new String(" bdhwmnj_huison_1473307995@tfbnw.net\n"));
        list1.add(new String("9furytest"));
        list1.add(new String("2"));
        list1.add(new String("znpmdrz_goldmanberg_1473307997@tfbnw.net"));
        list1.add(new String("9furytest"));

        ArrayList<String> list2 = new ArrayList<String>();
        list2.add(new String("Facebook"));
        list2.add(new String("user Name 2"));
        list2.add(new String("1"));
        list2.add(new String(" bdhwmnj_huison_1473307995@tfbnw.net\n"));
        list2.add(new String("2"));
        list2.add(new String("znpmdrz_goldmanberg_1473307997@tfbnw.net"));

        text1 = new KKString<String>(list1);
        text2 = new KKString<String>(list2);

        assertEquals("testString_mainDiff list ", diffs, diffProcessor.diff_main(text1, text2));
    }

    private <T> LinkedList<DiffProcessor.Diff<T>> diffList(DiffProcessor.Diff<T>... diffs) {
        LinkedList<DiffProcessor.Diff<T>> myDiffList = new LinkedList<DiffProcessor.Diff<T>>();
        for (DiffProcessor.Diff<T> myDiff : diffs) {
            myDiffList.add(myDiff);
        }
        return myDiffList;
    }

    private void assertArrayEquals(String error_msg, Object[] a, Object[] b) {
        List<Object> list_a = Arrays.asList(a);
        List<Object> list_b = Arrays.asList(b);
        assertEquals(error_msg, list_a, list_b);
    }

    private void assertLinesToCharsResultEquals(String error_msg,
                                                DiffProcessor.LinesToCharsResult a, DiffProcessor.LinesToCharsResult b) {
        assertEquals(error_msg, a.chars1, b.chars1);
        assertEquals(error_msg, a.chars2, b.chars2);
        assertEquals(error_msg, a.lineArray, b.lineArray);
    }

}
