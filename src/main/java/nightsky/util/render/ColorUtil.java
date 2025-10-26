package nightsky.util.render;

import net.minecraft.client.Minecraft;
import nightsky.util.MathUtil;

import java.awt.*;

public class ColorUtil {
    public static int applyOpacity(int color, float opacity) {
        Color old = new Color(color);
        return applyOpacity(old, opacity).getRGB();
    }

    public static Color applyOpacity(final Color color, float opacity) {
        opacity = Math.min(1.0f, Math.max(0.0f, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity));
    }

    public static Color applyOpacity3(int color, float opacity) {
        Color old = new Color(color);
        return applyOpacity(old, opacity);
    }

    public static Color reAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static int getRainbow(int counter) {
        return Color.HSBtoRGB(getRainbowHSB(counter)[0], getRainbowHSB(counter)[1], getRainbowHSB(counter)[2]);
    }

    public static float[] getRainbowHSB(int counter) {
        final int width = 20;
        double rainbowState = Math.ceil(System.currentTimeMillis() - (long) counter * width) / 8;
        rainbowState %= 360;
        float hue = (float) (rainbowState / 360);
        float saturation = 0.8f;
        float brightness = 0.9f;
        return new float[]{hue, saturation, brightness};
    }

    public static int astolfoRainbow(int delay, int offset, int index) {
        double rainbowDelay = Math.ceil(System.currentTimeMillis() + ((long) delay * index)) / offset;
        return Color.getHSBColor(
                (double) ((float) ((rainbowDelay %= 360.0) / 360.0)) < 0.5 ? -((float) (rainbowDelay / 360.0))
                        : (float) (rainbowDelay / 360.0),
                0.5F, 1).getRGB();
    }

    public static int fadeBetween(int startColor, int endColor, float progress) {
        if (progress > 1.0f) {
            progress = 1.0f - progress % 1.0f;
        }
        return fadeTo(startColor, endColor, progress);
    }

    public static int fadeTo(int startColor, int endColor, float progress) {
        float invert = 1.0f - progress;
        int r = (int)((float)(startColor >> 16 & 0xFF) * invert + (float)(endColor >> 16 & 0xFF) * progress);
        int g = (int)((float)(startColor >> 8 & 0xFF) * invert + (float)(endColor >> 8 & 0xFF) * progress);
        int b = (int)((float)(startColor & 0xFF) * invert + (float)(endColor & 0xFF) * progress);
        int a = (int)((float)(startColor >> 24 & 0xFF) * invert + (float)(endColor >> 24 & 0xFF) * progress);
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
    }

    public static int reAlpha(int color, float alpha) {
        Color c = new Color(color);
        float r = ((float) 1 / 255) * c.getRed();
        float g = ((float) 1 / 255) * c.getGreen();
        float b = ((float) 1 / 255) * c.getBlue();
        return new Color(r, g, b, alpha).getRGB();
    }

    public static Color getRainbow() {
        return new Color(Color.HSBtoRGB((float) ((double) Minecraft.getMinecraft().thePlayer.ticksExisted / 50.0 + Math.sin((double) 1 / 50.0 * 1.6)) % 1.0f, 0.5f, 1.0f));
    }

    public static int getRainbow(int speed, int offset) {
        return Color.HSBtoRGB((float)((System.currentTimeMillis() + (offset * 10L)) % (long)speed) / (float)speed, 0.55f, 0.9f);
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return interpolate(oldValue, newValue, (float) interpolationValue).intValue();
    }

    public static Double interpolate(double oldValue, double newValue, double interpolationValue) {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static int interpolateColor(int color1, int color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        Color cColor1 = new Color(color1);
        Color cColor2 = new Color(color2);
        return interpolateColorC(cColor1, cColor2, amount).getRGB();
    }
    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(interpolateInt(color1.getRed(), color2.getRed(), amount),
                interpolateInt(color1.getGreen(), color2.getGreen(), amount),
                interpolateInt(color1.getBlue(), color2.getBlue(), amount),
                interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static Color brighter(Color color, float FACTOR) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int alpha = color.getAlpha();
        int i = (int) (1.0 / (1.0 - FACTOR));
        if (r == 0 && g == 0 && b == 0) {
            return new Color(i, i, i, alpha);
        }
        if (r > 0 && r < i) r = i;
        if (g > 0 && g < i) g = i;
        if (b > 0 && b < i) b = i;

        return new Color(Math.min((int) (r / FACTOR), 255),
                Math.min((int) (g / FACTOR), 255),
                Math.min((int) (b / FACTOR), 255),
                alpha);
    }

    public static int swapAlpha(int color, float alpha) {
        int a = (int) (alpha);
        int r = (color >> 16) & 0xff;
        int g = (color >> 8) & 0xff;
        int b = color & 0xff;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }


    public static int getColorFromPercentage(float percentage) {
        if (percentage <= 0.5f) {
            int red = 255;
            int green = (int) (255 * percentage * 2);
            return new Color(red, green, 0).getRGB();
        } else {
            int red = (int) (255 * (1 - percentage) * 2);
            int green = 255;
            return new Color(red, green, 0).getRGB();
        }
    }

    public static int getOverallColorFrom(int color1, int color2, float factor) {
        return interpolateColor(color1, color2, factor);
    }

    public static int darker(int color, float factor) {
        Color c = new Color(color);
        return new Color(Math.max((int)(c.getRed() * (1 - factor)), 0),
                Math.max((int)(c.getGreen() * (1 - factor)), 0),
                Math.max((int)(c.getBlue() * (1 - factor)), 0),
                c.getAlpha()).getRGB();
    }
}
