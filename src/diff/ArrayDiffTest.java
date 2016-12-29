package diff;

import junit.framework.TestCase;

public class ArrayDiffTest extends TestCase {
    private ArrayDiff arrayDiff;
    @Override
    protected void setUp() throws Exception {
        arrayDiff = new ArrayDiff();
    }

    public void test_kmp_build_table() throws Exception {
        String expectedValue = ArrayUtils.join(",", new Integer[]{-1, 0, 0, 0, 0, 1, 2});
        int[] table = SearchUtils.kmp_build_tablelookup(new Integer[]{1,2,3,4,1,2,3});
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

    public void testDiff_main_diff() throws Exception {
        assertEquals("test main_diff ",null,arrayDiff.diff_main(new Object[]{1,2,3,4,5,6,7},
                new Object[]{1,2,2,4,6,7}));
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
        assertEquals("diff_commonPrefix: Empty case.", 0,
                arrayDiff.diff_commonPrefix(new Object[]{1,2,3}, new Object[]{4,5,6}));

        assertEquals("diff_commonPrefix: Not empty case.", 2,
                arrayDiff.diff_commonPrefix(new Object[]{1,2,3}, new Object[]{1,2,6}));

        assertEquals("diff_commonPrefix: Whole case.", 3,
                arrayDiff.diff_commonPrefix(new Object[]{1,2,3}, new Object[]{1,2,3,5,6}));
    }

    public void testDiff_commonSuffix() throws Exception {
        assertEquals("diff_commonPrefix: Empty case.", 0,
                arrayDiff.diff_commonSuffix(new Object[]{1, 2, 3}, new Object[]{4, 5, 6}));

        assertEquals("diff_commonPrefix: Not empty case.", 2,
                arrayDiff.diff_commonSuffix(new Object[]{223, 3, 4, 2, 3}, new Object[]{3, 4, 5, 1, 2, 3}));

        assertEquals("diff_commonPrefix: Whole case.", 3,
                arrayDiff.diff_commonSuffix(new Object[]{1, 3, 5, 6}, new Object[]{1, 2, 3, 5, 6}));
    }
}