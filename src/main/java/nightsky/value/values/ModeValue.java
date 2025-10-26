package nightsky.value.values;

import com.google.gson.JsonObject;
import nightsky.value.Value;

import java.util.function.BooleanSupplier;

public class ModeValue extends Value<Integer> {
    private final String[] modes;

    public ModeValue(String name, Integer value, String[] modes) {
        this(name, value, modes, null);
    }

    public ModeValue(String name, Integer value, String[] modes, BooleanSupplier check) {
        super(name, value, check);
        this.modes = modes;
    }

    @Override
    public String getValuePrompt() {
        return String.join(", ", this.modes);
    }

    public String getModeString() {
        int index = this.getValue();
        return index >= 0 && index < this.modes.length ? this.modes[index] : "";
    }

    @Override
    public String formatValue() {
        String index = this.getModeString();
        return index.isEmpty() ? "&4?" : String.format("&9%s", index);
    }

    @Override
    public boolean parseString(String string) {
        String valueStr = string.replace("_", "");
        for (int i = 0; i < this.modes.length; i++) {
            if (valueStr.equalsIgnoreCase(this.modes[i].replace("_", ""))) {
                return this.setValue(i);
            }
        }
        return false;
    }

    @Override
    public boolean read(JsonObject jsonObject) {
        return this.parseString(jsonObject.get(this.getName()).getAsString());
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.addProperty(this.getName(), this.getModeString());
    }
    
    public String[] getModes() {
        return modes;
    }
}
