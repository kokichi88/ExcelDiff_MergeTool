package kk;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by apple on 1/2/17.
 */

public class Utils {
    public static String getCurrentWorkingDir() {
        return System.getProperty("user.dir");
    }

    public static Map<String, String> parseProgramArguments(String[] args) {
        Map<String, String> ret = new HashMap<String, String>();
        for(int i =0 ; i < args.length; ++i) {
            String pair = args[i];
            String[] arr = pair.split("=");
            if(arr.length != 2) {
                throw new IllegalArgumentException(pair + " doesn't use following format: key=value");
            }
            ret.put(arr[0], arr[1]);
        }
        return ret;
    }

    public static String getProgramArguments(Map<String,String> map, String key) {
        if(map.containsKey(key)) {
            return map.get(key);
        }else {
            System.out.print("missing argument " + key);
            return null;
        }
    }
}
