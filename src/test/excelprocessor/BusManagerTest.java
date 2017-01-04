package test.excelprocessor;

import bus.controller.DoNothingCommand;
import bus.data.Message;
import bus.controller.BusManager;
import junit.framework.TestCase;

/**
 * Created by apple on 1/4/17.
 */
public class BusManagerTest extends TestCase {

    public void testUnsupportedCmdId() {
        boolean ret = false;
        try {
            BusManager.getInstance().dispatch(new Message(0,null));
        }catch (Exception e) {
            ret = true;
        }
        assertTrue("testUnsupportedCmdId", ret);

    }

    public void testSupportedCmdId() {
        boolean ret = true;
        try {
            BusManager.getInstance().registerCommand(0, DoNothingCommand.class);
            BusManager.getInstance().dispatch(new Message(0,null));
        }catch (Exception e) {
            ret = false;
        }
        assertTrue("testSupportedCmdId", ret);

    }

    public void testDuplicateHandlerForSameId() {
        boolean ret = false;
        try {
            BusManager.getInstance().registerCommand(0, DoNothingCommand.class);
            BusManager.getInstance().registerCommand(0, DoNothingCommand.class);
        }catch (Exception e) {
            ret = true;
        }
        assertTrue("testDuplicateHandlerForSameId", ret);

    }

    public void testNormalCase() {
        boolean ret = true;
        try {
            BusManager.getInstance().registerCommand(0, DoNothingCommand.class);
            BusManager.getInstance().dispatch(new Message(0, null));
        }catch (Exception e) {
            ret = false;
        }
        assertTrue("testNormalCase", ret);

    }

}
