package nightsky.ui.clickgui.dropdown.components.settings;

import nightsky.ui.clickgui.dropdown.components.SettingComponent;
import nightsky.ui.clickgui.dropdown.utils.DropdownFontRenderer;
import nightsky.util.render.ColorUtil;
import nightsky.util.render.RoundedUtil;
import nightsky.util.render.animations.advanced.Animation;
import nightsky.util.render.animations.advanced.Direction;
import nightsky.util.render.animations.advanced.impl.DecelerateAnimation;
import nightsky.value.values.ColorValue;

import java.awt.*;

public class ColorComponent extends SettingComponent<ColorValue> {

    private boolean opened;
    private boolean draggingPicker;
    private boolean draggingHue;
    private final Animation openAnimation = new DecelerateAnimation(250, 1, Direction.BACKWARDS);
    public float realHeight = 1;

    private float hue = 0;
    private float saturation = 1;
    private float brightness = 1;

    public ColorComponent(ColorValue colorValue) {
        super(colorValue);
        Color color = new Color(colorValue.getValue());
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
    }

    @Override
    public void initGui() {
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
    }

    @Override
    public void updateCountSize() {
        float pickerHeight = 50;
        float hueBarHeight = 5;
        float spacing2 = 3;
        float totalExpandedHeight = pickerHeight + spacing2 + hueBarHeight;
        double animation = openAnimation.getOutput();
        countSize = (float) (1 + (totalExpandedHeight / 20f) * animation);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        openAnimation.setDirection(opened ? Direction.FORWARDS : Direction.BACKWARDS);
        
        ColorValue colorSetting = getSetting();

        Color nameColor = ColorUtil.applyOpacity(textColor, alpha * 0.7f);
        DropdownFontRenderer.drawString(colorSetting.getName(), x + 5, y + 5, nameColor.getRGB(), 18);

        float spacing = 6;
        float colorHeight = 10;
        float colorWidth = 35;
        float colorX = x + width - (colorWidth + spacing);
        float colorY = y + 5;

        boolean hovered = isHovering(colorX - 2, colorY - 2, colorWidth + 4, colorHeight + 4, mouseX, mouseY);

        Color actualColor = new Color(colorSetting.getValue());
        Color displayColor = ColorUtil.applyOpacity(hovered ? ColorUtil.brighter(actualColor, 0.1f) : actualColor, alpha);

        net.minecraft.client.gui.Gui.drawRect((int)(colorX - 1), (int)(colorY - 1), (int)(colorX + colorWidth + 1), (int)(colorY + colorHeight + 1), 
            ColorUtil.applyOpacity(Color.WHITE, alpha * 0.3f).getRGB());
        net.minecraft.client.gui.Gui.drawRect((int)colorX, (int)colorY, (int)(colorX + colorWidth), (int)(colorY + colorHeight), displayColor.getRGB());

        double animation = openAnimation.getOutput();
        float pickerHeight = 50;
        float hueBarHeight = 5;
        float spacing2 = 3;
        float totalExpandedHeight = pickerHeight + spacing2 + hueBarHeight;
        
        if (opened || !openAnimation.isDone()) {
            float gradientX = x + 6;
            float gradientY = y + 20;
            float gradientWidth = width - 12;
            float gradientHeight = (float) (pickerHeight * animation);

            if (gradientHeight > 2) {
                if (draggingHue) {
                    hue = Math.min(1, Math.max(0, (mouseX - gradientX) / gradientWidth));
                }

                if (draggingPicker) {
                    brightness = Math.min(1, Math.max(0, 1 - ((mouseY - gradientY) / gradientHeight)));
                    saturation = Math.min(1, Math.max(0, (mouseX - gradientX) / gradientWidth));
                }

                Color firstColor = ColorUtil.applyOpacity(Color.getHSBColor(hue, 1, 1), alpha);
                net.minecraft.client.gui.Gui.drawRect((int)gradientX, (int)gradientY, (int)(gradientX + gradientWidth), (int)(gradientY + gradientHeight), firstColor.getRGB());

                Color secondColor = Color.getHSBColor(hue, 0, 1);
                RoundedUtil.drawGradientHorizontal(gradientX, gradientY, gradientWidth, gradientHeight, 0, 
                    ColorUtil.applyOpacity(secondColor, alpha), 
                    ColorUtil.applyOpacity(new Color(secondColor.getRed(), secondColor.getGreen(), secondColor.getBlue(), 0), alpha));

                Color thirdColor = Color.getHSBColor(hue, 1, 0);
                RoundedUtil.drawGradientVertical(gradientX, gradientY, gradientWidth, gradientHeight, 0, 
                    ColorUtil.applyOpacity(new Color(thirdColor.getRed(), thirdColor.getGreen(), thirdColor.getBlue(), 0), alpha), 
                    ColorUtil.applyOpacity(thirdColor, alpha));

                float pickerY = gradientY + (gradientHeight * (1 - brightness));
                float pickerX = gradientX + (gradientWidth * saturation);
                pickerY = Math.max(Math.min(gradientY + gradientHeight - 2, pickerY), gradientY);
                pickerX = Math.max(Math.min(gradientX + gradientWidth - 2, pickerX), gradientX);

                net.minecraft.client.gui.Gui.drawRect((int)(pickerX - 2), (int)(pickerY - 2), (int)(pickerX + 2), (int)(pickerY + 2), ColorUtil.applyOpacity(Color.WHITE, alpha).getRGB());

                float hueY = gradientY + gradientHeight + spacing2;
                float actualHueHeight = (float) (hueBarHeight * animation);
                
                if (actualHueHeight > 1) {
                    drawHueBar(gradientX, hueY, gradientWidth, actualHueHeight);

                    float sliderSize = 7f;
                    float sliderX = gradientX + (gradientWidth * hue) - (sliderSize / 2);
                    net.minecraft.client.gui.Gui.drawRect((int)sliderX, (int)(hueY + ((actualHueHeight / 2f) - sliderSize / 2f)), (int)(sliderX + sliderSize), (int)(hueY + ((actualHueHeight / 2f) + sliderSize / 2f)), 
                        ColorUtil.applyOpacity(Color.WHITE, alpha).getRGB());
                    float miniSize = 5f;
                    float movement = (sliderSize / 2f - miniSize / 2f);
                    net.minecraft.client.gui.Gui.drawRect((int)(sliderX + movement), (int)(hueY + (((actualHueHeight / 2f) - miniSize / 2f))), (int)(sliderX + movement + miniSize), (int)(hueY + (((actualHueHeight / 2f) + miniSize / 2f))), 
                        ColorUtil.applyOpacity(firstColor, alpha).getRGB());
                }

                colorSetting.setValue(Color.HSBtoRGB(hue, saturation, brightness));
            }
        }
    }

    private void drawHueBar(float x, float y, float width, float height) {
        float segmentWidth = width / 6;
        Color[] colors = {
            ColorUtil.applyOpacity(Color.RED, alpha),
            ColorUtil.applyOpacity(Color.YELLOW, alpha),
            ColorUtil.applyOpacity(Color.GREEN, alpha),
            ColorUtil.applyOpacity(Color.CYAN, alpha),
            ColorUtil.applyOpacity(Color.BLUE, alpha),
            ColorUtil.applyOpacity(Color.MAGENTA, alpha),
            ColorUtil.applyOpacity(Color.RED, alpha)
        };

        for (int i = 0; i < 6; i++) {
            RoundedUtil.drawGradientHorizontal(x + i * segmentWidth, y, segmentWidth, height, 0, colors[i], colors[i + 1]);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        float spacing = 6;
        float colorHeight = 10;
        float colorWidth = 35;
        float colorX = x + width - (colorWidth + spacing);
        float colorY = y + 5;

        boolean hovered = isClickable(colorY + colorHeight) && isHovering(colorX - 2, colorY - 2, colorWidth + 4, colorHeight + 4, mouseX, mouseY);

        if (hovered && button == 1) {
            opened = !opened;
        }

        if (opened && openAnimation.getOutput() > 0.5) {
            float gradientX = x + 6;
            float gradientY = y + 20;
            float gradientWidth = width - 12;
            float gradientHeight = 50;

            if (button == 0) {
                float hueY = gradientY + gradientHeight + 3;
                if (isClickable(hueY + 5) && isHovering(gradientX, hueY, gradientWidth, 5, mouseX, mouseY)) {
                    draggingHue = true;
                }
                if (isClickable(gradientY + gradientHeight) && isHovering(gradientX, gradientY, gradientWidth, gradientHeight, mouseX, mouseY)) {
                    draggingPicker = true;
                }
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        draggingHue = false;
        draggingPicker = false;
    }

    private boolean isHovering(float x, float y, float width, float height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
