package nightsky.value;

import nightsky.module.Module;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ValueHandler {
    public LinkedHashMap<Class<?>, ArrayList<Value<?>>> properties = new LinkedHashMap<>();

    public Value<?> getProperty(Module module, String string) {
        for (Value<?> value : properties.get(module.getClass())) {
            if (value.getName().replace("-", "").equalsIgnoreCase(string.replace("-", ""))) {
                return value;
            }
        }
        return null;
    }
}
