package nightsky.module.modules.render;

import nightsky.module.Module;
import nightsky.util.render.ColorUtil;
import nightsky.value.values.ColorValue;
import nightsky.value.values.ModeValue;
import nightsky.value.values.FloatValue;

public class Interface extends Module {
    public final ModeValue colorMode = new ModeValue("Color Mode", 4, new String[]{"Custom", "Dynamic", "Rainbow", "Astolfo", "Fade"});
    public final FloatValue speedValue = new FloatValue("Speed", 2.0f, 1.0f, 10.0f);
    public final ColorValue mainColorValue = new ColorValue("Main Color", new java.awt.Color(128, 255, 149).getRGB());
    private final ColorValue secondColorValue = new ColorValue("Second Color", new java.awt.Color(128, 255, 255).getRGB());
    public final FloatValue astolfoOffsetValue = new FloatValue("Offset", 5f, 0f, 20f);
    public final FloatValue astolfoIndexValue = new FloatValue("Index", 107f, 0f, 200f);
    public final ModeValue toggleSound = new ModeValue("ToggleSound", 0, new String[]{"Vanilla", "Augustus", "Jello", "Other"});

    public Interface() {
        super("Interface", true, true);
    }

    @Override
    public void onEnabled(){
    }
    
    @Override
    public void onDisabled(){
        this.setEnabled(true);
    }

    public java.awt.Color getMainColor() {
        return new java.awt.Color(mainColorValue.getValue());
    }

    public java.awt.Color getSecondColor() {
        return new java.awt.Color(secondColorValue.getValue());
    }

    public int color() {
        return color(0);
    }

    public int color(int counter) {
        java.awt.Color c = new java.awt.Color(mainColorValue.getValue());
        return color(counter, c.getAlpha());
    }

    public int color(int counter, float opacity) {
        long ms = (long)(speedValue.getValue() * 1000L);
        float progress = (float)(System.currentTimeMillis() % ms) / ms;

        int color = getMainColor().getRGB();
        String mode = colorMode.getModeString();
        switch (mode) {
            case "Custom":
                color = ColorUtil.applyOpacity(getMainColor().getRGB(), opacity);
                break;
            case "Fade":
                color = ColorUtil.fadeBetween(this.getMainColor().getRGB(), this.getSecondColor().getRGB(),
                        (float)((System.currentTimeMillis() + (long)counter * 100L) % ms) / ((float)ms / 2.0f));
                break;
            case "Rainbow":
                color = ColorUtil.swapAlpha(ColorUtil.getRainbow(counter), opacity);
                break;
            case "Astolfo":
                color = ColorUtil.applyOpacity(
                        new java.awt.Color(ColorUtil.astolfoRainbow(
                                (int)(counter + (progress * 100)),
                                astolfoOffsetValue.getValue().intValue(),
                                astolfoIndexValue.getValue().intValue()
                        )).getRGB(),
                        opacity
                );
                break;
            case "Dynamic":
                color = ColorUtil.fadeBetween(this.mainColorValue.getValue(), ColorUtil.darker(this.getMainColor().getRGB(),
                        0.3f), (float)((System.currentTimeMillis() + (ms + (long)counter * 100L)) % ms) / ((float)ms / 2.0f));
                break;
        }
        return color;
    }
}
