package bus.controller;

import bus.data.ISignal;
import java.util.*;

/**
 * Created by apple on 1/4/17.
 */
public class BusManager {

    private Queue<ISignal> queues = new ArrayDeque<ISignal>();
    private Map<Class, ArrayList<ICommand>> mapCommands = new HashMap<Class, ArrayList<ICommand>>();
    private int numCommandExecuted = 0;

    public BusManager() {

    }

    public <S extends ISignal, T extends ICommand> void register(Class<S> signal, Class<T> clazz) throws Exception {
            ArrayList<ICommand> cmds;
            if(mapCommands.containsKey(signal)) {
                cmds = mapCommands.get(signal);
            }else {
                cmds = new ArrayList<ICommand>();
                mapCommands.put(signal, cmds);
            }
            for(ICommand cmd : cmds) {
                if( cmd.getClass() == clazz) {
                    throw new IllegalArgumentException("cant load duplicate handler " + clazz);
                }
            }
            cmds.add(clazz.newInstance());
    }


    public void dispatch(ISignal signal) {
        queues.offer(signal);
        process();
    }

    public void process() {
        try {
            while(queues.size() > 0) {
                ISignal signal = queues.poll();
                if(signal != null) {
                    List<ICommand> cmds = mapCommands.get(signal.getClass());
                    if(cmds != null) {
                        for(ICommand cmd : cmds) {
                            cmd.execute(signal);
                            ++numCommandExecuted;
                        }
                    }else {
                        throw new Exception("unsupported signal " + signal.getClass());
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int getNumCommandExecuted() {
        return numCommandExecuted;
    }
}
