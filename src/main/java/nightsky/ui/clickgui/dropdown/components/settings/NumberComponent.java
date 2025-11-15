package nightsky.ui.clickgui.dropdown.components.settings;

import nightsky.ui.clickgui.dropdown.components.SettingComponent;
import nightsky.ui.clickgui.dropdown.utils.DropdownFontRenderer;
import nightsky.util.render.ColorUtil;
import nightsky.util.render.animations.advanced.Animation;
import nightsky.util.render.animations.advanced.Direction;
import nightsky.util.render.animations.advanced.impl.DecelerateAnimation;
import nightsky.value.Value;
import nightsky.value.values.FloatValue;
import nightsky.value.values.IntValue;

import java.awt.*;

public class NumberComponent extends SettingComponent {

    private boolean dragging;
    private final Animation hoverAnimation = new DecelerateAnimation(200, 1, Direction.BACKWARDS);

    public NumberComponent(Value numberValue) {
        super(numberValue);
    }

    @Override
    public void initGui() {
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
    }

    @Override
    public void updateCountSize() {
        countSize = 1.2f;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        double value = 0;
        double min = 0;
        double max = 100;
        double increment = 1;

        if (getSetting() instanceof FloatValue) {
            FloatValue fv = (FloatValue) getSetting();
            value = fv.getValue();
            min = fv.getMinimum();
            max = fv.getMaximum();
            increment = 0.1;
        } else if (getSetting() instanceof IntValue) {
            IntValue iv = (IntValue) getSetting();
            value = iv.getValue();
            min = iv.getMinimum();
            max = iv.getMaximum();
            increment = 1;
        }

        String valueStr = String.format("%.2f", value).replaceAll("0*$", "").replaceAll("\\.$", "");

        float sliderX = x + 5;
        float sliderWidth = width - 10;
        float sliderY = y + 11;
        float sliderHeight = 4;

        boolean hovering = isHovering(sliderX - 2, sliderY - 3, sliderWidth + 4, sliderHeight + 6, mouseX, mouseY);
        hoverAnimation.setDirection(hovering || dragging ? Direction.FORWARDS : Direction.BACKWARDS);

        Color nameColor = ColorUtil.applyOpacity(textColor, alpha * 0.7f);
        DropdownFontRenderer.drawString(getSetting().getName(), sliderX, y + 1, nameColor.getRGB(), 18);
        
        Color valueColor = ColorUtil.applyOpacity(textColor, alpha);
        String displayText = ": " + valueStr;
        DropdownFontRenderer.drawString(displayText, 
            sliderX + DropdownFontRenderer.getStringWidth(getSetting().getName(), 18), 
            y + 1, valueColor.getRGB(), 18);

        Color bgColor = ColorUtil.applyOpacity(settingRectColor.brighter(), alpha * 0.8f);
        net.minecraft.client.gui.Gui.drawRect((int)sliderX, (int)sliderY, (int)(sliderX + sliderWidth), (int)(sliderY + sliderHeight), bgColor.getRGB());

        if (dragging) {
            float percent = Math.min(1, Math.max(0, (mouseX - sliderX) / sliderWidth));
            double newValue = min + (max - min) * percent;
            newValue = Math.round(newValue / increment) * increment;
            
            if (getSetting() instanceof FloatValue) {
                ((FloatValue) getSetting()).setValue((float) newValue);
            } else if (getSetting() instanceof IntValue) {
                ((IntValue) getSetting()).setValue((int) newValue);
            }
        }

        float widthPercentage = (float) ((value - min) / (max - min));
        float animatedWidth = sliderWidth * widthPercentage;

        Color accentColor = ColorUtil.applyOpacity(new Color(255, 255, 255), alpha);
        net.minecraft.client.gui.Gui.drawRect((int)sliderX, (int)sliderY, (int)(sliderX + animatedWidth), (int)(sliderY + sliderHeight), accentColor.getRGB());

        float size = (float) (8f + 2f * hoverAnimation.getOutput());
        float knobX = sliderX + animatedWidth - size / 2f;
        float knobY = sliderY + sliderHeight / 2f - size / 2f;
        
        net.minecraft.client.gui.Gui.drawRect((int)knobX, (int)knobY, (int)(knobX + size), (int)(knobY + size), 
            ColorUtil.applyOpacity(new Color(40, 40, 40), alpha * 0.3f).getRGB());
        
        float innerSize = size - 2;
        net.minecraft.client.gui.Gui.drawRect((int)(knobX + 1), (int)(knobY + 1), (int)(knobX + 1 + innerSize), (int)(knobY + 1 + innerSize), 
            ColorUtil.applyOpacity(Color.WHITE, alpha).getRGB());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        float sliderX = x + 5;
        float sliderWidth = width - 10;
        float sliderY = y + 11;
        float sliderHeight = 4;

        if (isClickable(sliderY + sliderHeight) && isHovering(sliderX - 2, sliderY - 3, sliderWidth + 4, sliderHeight + 6, mouseX, mouseY) && button == 0) {
            dragging = true;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (dragging) dragging = false;
    }

    private boolean isHovering(float x, float y, float width, float height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
