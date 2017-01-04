package bus.controller;

import bus.data.Message;

import java.util.*;

/**
 * Created by apple on 1/4/17.
 */
public class BusManager {

    private Queue<Message> queues = new ArrayDeque<Message>();
    private Map<Integer, ArrayList<ICommand>> mapCommands = new HashMap<Integer, ArrayList<ICommand>>();

    public BusManager() {

    }

    public <T extends ICommand> void registerCommand(int cmdId, Class<T> clazz) throws Exception {
            ArrayList<ICommand> cmds;
            if(mapCommands.containsKey(cmdId)) {
                cmds = mapCommands.get(cmdId);
            }else {
                cmds = new ArrayList<ICommand>();
                mapCommands.put(cmdId, cmds);
            }
            for(ICommand cmd : cmds) {
                if( cmd.getClass() == clazz) {
                    throw new IllegalArgumentException("cant load duplicate handler " + clazz);
                }
            }
            cmds.add(clazz.newInstance());
    }


    public void dispatch(Message msg) {
        queues.offer(msg);
        process();
    }

    private void process() {
        while(queues.size() > 0) {
            Message msg = queues.poll();
            List<ICommand> cmds = mapCommands.get(msg.cmdId);
            if(cmds != null) {
                for(ICommand cmd : cmds)
                    cmd.execute(msg);
            }else {
                throw new IllegalArgumentException("unsupported message cmdId " + msg.cmdId);
            }
        }
    }
}
