package test.excelprocessor;

import bus.controller.DoNothingCommand;
import bus.data.EmptySignal;
import bus.controller.BusManager;
import junit.framework.TestCase;

/**
 * Created by apple on 1/4/17.
 */
public class BusManagerTest extends TestCase {
    BusManager busManager;
    @Override
    protected void setUp() throws Exception {
        busManager = new BusManager();
    }

    public void testUnsupportedSignal() {
        busManager.dispatch(new EmptySignal());
        assertEquals("testUnsupportedSignal", 0, busManager.getNumCommandExecuted());
    }

    public void testDuplicateCommandForSameSignal() {
        boolean ret = false;
        try {
            busManager.register(EmptySignal.class, DoNothingCommand.class);
            busManager.register(EmptySignal.class, DoNothingCommand.class);
        }catch (Exception e) {
            ret = true;
        }
        assertTrue("testDuplicateCommandForSameSignal", ret);

    }

    public void testNormalCase() {
        try {
            busManager.register(EmptySignal.class, DoNothingCommand.class);
            busManager.dispatch(new EmptySignal());
        }catch (Exception e) {
        }
        assertEquals("testNormalCase", 1, busManager.getNumCommandExecuted());

    }

}
