package nightsky.module.modules.render;

import nightsky.NightSky;
import nightsky.module.Module;
import nightsky.util.render.ColorUtil;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.ColorValue;
import nightsky.value.values.FloatValue;
import nightsky.value.values.IntValue;
import nightsky.value.values.ModeValue;

public class PostProcessing extends Module {
    private static PostProcessing instance;

    public final BooleanValue blur = new BooleanValue("Blur", true);
    public final IntValue blurStrength = new IntValue("BlurStrength", 12, 1, 200);

    public final BooleanValue bloom = new BooleanValue("Bloom", false);
    public final ModeValue bloomColorMode = new ModeValue("BloomColorMode", 0, new String[]{"Custom", "Dynamic", "Rainbow", "Astolfo", "Fade", "Interface"});
    public final FloatValue bloomColorSpeed = new FloatValue("BloomColorSpeed", 2.0f, 1.0f, 10.0f);
    public final ColorValue bloomMainColor = new ColorValue("BloomColor", 0xFF000000);
    public final ColorValue bloomSecondColor = new ColorValue("BloomSecondColor", 0xFF000000);
    public final FloatValue bloomAstolfoOffset = new FloatValue("BloomAstolfoOffset", 5f, 0f, 20f);
    public final FloatValue bloomAstolfoIndex = new FloatValue("BloomAstolfoIndex", 107f, 0f, 200f);
    public final BooleanValue arrayListBloomFromInterface = new BooleanValue("ArrayListBloomFromInterface", true);
    public final IntValue bloomIterations = new IntValue("BloomIterations", 5, 1, 10);
    public final IntValue bloomOffset = new IntValue("BloomOffset", 3, 1, 10);

    public PostProcessing() {
        super("PostProcessing", true);
        instance = this;
    }

    public static PostProcessing getInstance() {
        return instance;
    }

    public static boolean isBlurEnabled() {
        return instance != null && instance.isEnabled() && instance.blur.getValue();
    }

    public static float getBlurStrength() {
        return instance != null ? instance.blurStrength.getValue().floatValue() : 0.0f;
    }

    public static boolean isBloomEnabled() {
        return instance != null && instance.isEnabled() && instance.bloom.getValue();
    }

    public static int getBloomIterations() {
        return instance != null ? instance.bloomIterations.getValue() : 1;
    }

    public static int getBloomColor() {
        return getBloomColor(0);
    }

    public static int getBloomColor(int counter) {
        if (instance == null) return 0xFF000000;
        String mode = instance.bloomColorMode.getModeString();
        if (mode.equals("Interface")) {
            Interface interfaceModule = (Interface) NightSky.moduleManager.getModule("Interface");
            return interfaceModule != null ? interfaceModule.color(counter) : instance.calculateBloomColor(counter, 255);
        }
        return instance.calculateBloomColor(counter, 255);
    }

    private int calculateBloomColor(int counter, float opacity) {
        long ms = (long) (bloomColorSpeed.getValue() * 1000L);
        float progress = (float) (System.currentTimeMillis() % ms) / ms;

        int color = new java.awt.Color(bloomMainColor.getValue()).getRGB();
        String mode = bloomColorMode.getModeString();
        switch (mode) {
            case "Custom":
                color = ColorUtil.applyOpacity(new java.awt.Color(bloomMainColor.getValue()).getRGB(), opacity);
                break;
            case "Fade":
                color = ColorUtil.fadeBetween(
                        new java.awt.Color(bloomMainColor.getValue()).getRGB(),
                        new java.awt.Color(bloomSecondColor.getValue()).getRGB(),
                        (float) ((System.currentTimeMillis() + (long) counter * 100L) % ms) / ((float) ms / 2.0f)
                );
                break;
            case "Rainbow":
                color = ColorUtil.swapAlpha(ColorUtil.getRainbow(counter), opacity);
                break;
            case "Astolfo":
                color = ColorUtil.applyOpacity(
                        new java.awt.Color(ColorUtil.astolfoRainbow(
                                (int) (counter + (progress * 100)),
                                bloomAstolfoOffset.getValue().intValue(),
                                bloomAstolfoIndex.getValue().intValue()
                        )).getRGB(),
                        opacity
                );
                break;
            case "Dynamic":
                int main = new java.awt.Color(bloomMainColor.getValue()).getRGB();
                color = ColorUtil.fadeBetween(
                        main,
                        ColorUtil.darker(main, 0.3f),
                        (float) ((System.currentTimeMillis() + (ms + (long) counter * 100L)) % ms) / ((float) ms / 2.0f)
                );
                break;
        }
        return color;
    }

    public static boolean isArrayListBloomFromInterface() {
        return instance != null && instance.arrayListBloomFromInterface.getValue();
    }

    public static int getBloomOffset() {
        return instance != null ? instance.bloomOffset.getValue() : 1;
    }
}
