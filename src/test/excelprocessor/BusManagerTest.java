package test.excelprocessor;

import bus.controller.DoNothingCommand;
import bus.data.Message;
import bus.controller.BusManager;
import junit.framework.TestCase;
import services.Services;

/**
 * Created by apple on 1/4/17.
 */
public class BusManagerTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        BusManager busManager = new BusManager();
        Services.setService(busManager);
    }

    public void testUnsupportedCmdId() {
        boolean ret = false;
        try {
            Services.getService(BusManager.class).dispatch(new Message(0,null));
        }catch (Exception e) {
            ret = true;
        }
        assertTrue("testUnsupportedCmdId", ret);

    }

    public void testSupportedCmdId() {
        boolean ret = true;
        try {
            Services.getService(BusManager.class).registerCommand(0, DoNothingCommand.class);
            Services.getService(BusManager.class).dispatch(new Message(0,null));
        }catch (Exception e) {
            ret = false;
        }
        assertTrue("testSupportedCmdId", ret);

    }

    public void testDuplicateHandlerForSameId() {
        boolean ret = false;
        try {
            Services.getService(BusManager.class).registerCommand(0, DoNothingCommand.class);
            Services.getService(BusManager.class).registerCommand(0, DoNothingCommand.class);
        }catch (Exception e) {
            ret = true;
        }
        assertTrue("testDuplicateHandlerForSameId", ret);

    }

    public void testNormalCase() {
        boolean ret = true;
        try {
            Services.getService(BusManager.class).registerCommand(0, DoNothingCommand.class);
            Services.getService(BusManager.class).dispatch(new Message(0, null));
        }catch (Exception e) {
            ret = false;
        }
        assertTrue("testNormalCase", ret);

    }

}
