package services;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by apple on 1/4/17.
 */
public class Services {
    private static Map<Class, Object> services = new HashMap<Class, Object>();

    public static <T> void setService(T instance) {
        if(services.containsKey(instance.getClass()))
            throw new IllegalArgumentException("can't have more than once service " + instance + " at the same time");
        services.put(instance.getClass(), instance);
    }

    public static <T> void unsetService(Class<T> clazz) {
        services.remove(clazz);
    }

    public static <T> T getService(Class<T> clazz) {
        return (T)services.get(clazz);
    }
}
