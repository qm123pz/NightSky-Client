package nightsky.value.values;

import com.google.gson.JsonObject;
import nightsky.value.Value;

import java.awt.Color;
import java.util.function.BooleanSupplier;

public class ColorValue extends Value<Integer> {
    private float hue = 0.5f;
    private float saturation = 1.0f;
    private float brightness = 1.0f;

    public ColorValue(String name, Integer color) {
        this(name, color, null);
    }

    public ColorValue(String string, Integer color, BooleanSupplier check) {
        super(string, color | 0xFF000000, rgb -> true, check);
        updateHSBFromRGB(this.getValue());
    }

    private void updateHSBFromRGB(int rgb) {
        Color color = new Color(rgb & 0xFFFFFF);
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
    }

    private void updateRGBFromHSB() {
        Color color = Color.getHSBColor(hue, saturation, brightness);
        int rgb = 0xFF000000 | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
        super.setValue(rgb);
    }

    public float getHue() {
        return hue;
    }

    public void setHue(float hue) {
        this.hue = Math.max(0, Math.min(1, hue));
        updateRGBFromHSB();
    }

    public float getSaturation() {
        return saturation;
    }

    public void setSaturation(float saturation) {
        this.saturation = Math.max(0, Math.min(1, saturation));
        updateRGBFromHSB();
    }

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        this.brightness = Math.max(0, Math.min(1, brightness));
        updateRGBFromHSB();
    }


    @Override
    public boolean setValue(Object value) {
        if (value instanceof Integer) {
            int color = ((Integer) value) | 0xFF000000;
            boolean result = super.setValue(color);
            if (result) {
                updateHSBFromRGB(color);
            }
            return result;
        }
        return super.setValue(value);
    }

    @Override
    public String getValuePrompt() {
        return "RGB";
    }

    @Override
    public String formatValue() {
        String hex = String.format("%06X", this.getValue() & 0xFFFFFF);
        return String.format("&c%s&a%s&9%s", hex.substring(0, 2), hex.substring(2, 4), hex.substring(4, 6));
    }

    @Override
    public boolean parseString(String string) {
        return this.setValue(Integer.parseInt(string.replace("#", ""), 16));
    }

    @Override
    public boolean read(JsonObject jsonObject) {
        return this.parseString(jsonObject.get(this.getName()).getAsString());
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.addProperty(this.getName(), String.format("%06X", this.getValue() & 0xFFFFFF));
    }
}
