package nightsky.value.values;

import com.google.gson.JsonObject;
import nightsky.value.Value;

import java.util.function.BooleanSupplier;

public class PercentValue extends Value<Integer> {
    private final Integer minimum;
    private final Integer maximum;

    public PercentValue(String name, Integer value) {
        this(name, value, null);
    }

    public PercentValue(String name, Integer value, BooleanSupplier check) {
        this(name, value, 0, 100, check);
    }

    public PercentValue(String name, Integer value, Integer minimum, Integer maximum, BooleanSupplier booleanSupplier) {
        super(name, value, value1 -> value1 >= minimum && value1 <= maximum, booleanSupplier);
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Override
    public String getValuePrompt() {
        return String.format("%d-%d%%", this.minimum, this.maximum);
    }

    @Override
    public String formatValue() {
        return String.format("&b%d%%", this.getValue());
    }

    @Override
    public boolean parseString(String string) {
        return this.setValue(Integer.parseInt(string.replace("%", "")));
    }

    @Override
    public boolean read(JsonObject jsonObject) {
        return this.setValue(jsonObject.get(this.getName()).getAsNumber().intValue());
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.addProperty(this.getName(), this.getValue());
    }

    public Integer getMinimum() {
        return this.minimum;
    }

    public Integer getMaximum() {
        return this.maximum;
    }
}
