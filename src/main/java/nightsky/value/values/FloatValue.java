package nightsky.value.values;

import com.google.gson.JsonObject;
import nightsky.value.Value;

import java.util.function.BooleanSupplier;

public class FloatValue extends Value<Float> {
    private final Float minimum;
    private final Float maximum;

    public FloatValue(String name, Float value, Float minimum, Float maximum) {
        this(name, value, minimum, maximum, null);
    }

    public FloatValue(String string, Float value, Float minimum, Float maximum, BooleanSupplier check) {
        super(string, value, floatV -> floatV >= 0 && floatV <= Float.MAX_VALUE, check);
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Override
    public String getValuePrompt() {
        return String.format("%s-%s", this.minimum, this.maximum);
    }

    @Override
    public String formatValue() {
        return String.format("&6%s", this.getValue());
    }

    @Override
    public boolean parseString(String string) {
        return this.setValue(Float.parseFloat(string));
    }

    @Override
    public boolean read(JsonObject jsonObject) {
        return this.setValue(jsonObject.get(this.getName()).getAsNumber().floatValue());
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.addProperty(this.getName(), this.getValue());
    }
    
    public Float getMinimum() {
        return minimum;
    }
    
    public Float getMaximum() {
        return maximum;
    }
}
