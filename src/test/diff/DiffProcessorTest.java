package test.diff;

import diff.ArrayUtils;
import diff.DiffProcessor;
import diff.KKString;
import junit.framework.TestCase;

/**
 * Created by apple on 1/11/17.
 */
public class DiffProcessorTest extends TestCase {
    private DiffProcessor<String> processor;
    @Override
    protected void setUp() throws Exception {
        processor = new DiffProcessor<String>(String.class);
    }

    public void test_starts_with() throws Exception {
        KKString<Integer> longArr = new KKString<Integer>(Integer.class, 1,2,3,4,5);
        KKString<Integer> shortArr = new KKString<Integer>(Integer.class, 1,2);

        TestCase.assertEquals("test starts with. Found case ", true, longArr.startsWith(shortArr));

        shortArr = new KKString<Integer>(Integer.class, 2,3 );
        assertEquals("test starts with. Not found case ", false, longArr.startsWith(shortArr));

        assertEquals("test starts with. Out of index not found case", false,
                shortArr.startsWith(longArr));

        shortArr = new KKString<Integer>(Integer.class, 3,4,5);
        assertEquals("test ends with. Found case", true, longArr.endsWith(shortArr));

        shortArr = new KKString<Integer>(Integer.class, 3,4,6);
        assertEquals("test ends with. Not found case", false, longArr.endsWith(shortArr));

        assertEquals("test ends with. Out of index not found case", false,
                shortArr.startsWith(longArr));
    }
}
