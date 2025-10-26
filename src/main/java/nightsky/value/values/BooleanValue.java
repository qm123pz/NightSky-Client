package nightsky.value.values;

import com.google.gson.JsonObject;
import nightsky.value.Value;

import java.util.function.BooleanSupplier;

public class BooleanValue extends Value<Boolean> {
    public BooleanValue(String name, java.lang.Boolean value) {
        this(name, value, null);
    }

    public BooleanValue(String name, java.lang.Boolean value, BooleanSupplier booleanSupplier) {
        super(name, value, booleanSupplier);
    }

    @Override
    public String getValuePrompt() {
        return "true/false";
    }

    @Override
    public String formatValue() {
        return this.getValue() ? "&atrue" : "&cfalse";
    }

    @Override
    public boolean parseString(String string) {
        if (string == null) {
            return this.setValue(!(java.lang.Boolean) this.getValue());
        } else if (string.equalsIgnoreCase("true") || string.equalsIgnoreCase("on") || string.equalsIgnoreCase("1")) {
            return this.setValue(true);
        } else {
            return (string.equalsIgnoreCase("false") || string.equalsIgnoreCase("off") || string.equalsIgnoreCase("0")) && this.setValue(false);
        }
    }

    @Override
    public boolean read(JsonObject jsonObject) {
        return this.setValue(jsonObject.get(this.getName()).getAsBoolean());
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.addProperty(this.getName(), this.getValue());
    }
}
