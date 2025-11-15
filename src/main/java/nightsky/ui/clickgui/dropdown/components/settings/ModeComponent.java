package nightsky.ui.clickgui.dropdown.components.settings;

import nightsky.ui.clickgui.dropdown.components.SettingComponent;
import nightsky.ui.clickgui.dropdown.utils.DropdownFontRenderer;
import nightsky.util.render.ColorUtil;
import nightsky.util.render.animations.advanced.Animation;
import nightsky.util.render.animations.advanced.Direction;
import nightsky.util.render.animations.advanced.impl.DecelerateAnimation;
import nightsky.value.values.ModeValue;

import java.awt.*;

public class ModeComponent extends SettingComponent<ModeValue> {

    private boolean opened;
    private final Animation openAnimation = new DecelerateAnimation(250, 1, Direction.BACKWARDS);
    public float normalCount = 1;

    public ModeComponent(ModeValue modeValue) {
        super(modeValue);
    }

    @Override
    public void initGui() {
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
    }

    @Override
    public void updateCountSize() {
        float rectHeight = 12;
        float rectCount = 0;
        ModeValue modeSetting = getSetting();
        for (String mode : modeSetting.getModes()) {
            if (!mode.equals(modeSetting.getModeString())) {
                rectCount++;
            }
        }
        double animation = openAnimation.getOutput();
        countSize = (float) (1 + (rectCount * rectHeight / 20f * animation));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        openAnimation.setDirection(opened ? Direction.FORWARDS : Direction.BACKWARDS);
        
        ModeValue modeSetting = getSetting();

        float nameHeight = 10;
        float boxHeight = 12;
        float boxY = y + nameHeight + 1;
        float boxX = x + 4;
        float boxWidth = width - 8;

        boolean hoveringBox = isHovering(boxX, boxY, boxWidth, boxHeight, mouseX, mouseY);

        Color outlineColor = opened ? ColorUtil.applyOpacity(new Color(255, 255, 255, 255), alpha * 0.8f) :
            ColorUtil.applyOpacity(settingRectColor.brighter(), alpha);
        if (hoveringBox) {
            outlineColor = ColorUtil.brighter(outlineColor, 0.2f);
        }

        Color rectColor = ColorUtil.applyOpacity(settingRectColor.brighter(), alpha * 0.8f);

        net.minecraft.client.gui.Gui.drawRect((int)boxX, (int)boxY, (int)(boxX + boxWidth), (int)(boxY + boxHeight), outlineColor.getRGB());
        net.minecraft.client.gui.Gui.drawRect((int)(boxX + 0.5f), (int)(boxY + 0.5f), (int)(boxX + boxWidth - 0.5f), (int)(boxY + boxHeight - 0.5f), rectColor.getRGB());

        Color nameColor = ColorUtil.applyOpacity(textColor, alpha * 0.7f);
        DropdownFontRenderer.drawString(modeSetting.getName(), boxX + 2, y + 2, nameColor.getRGB(), 18);
        
        Color valueColor = ColorUtil.applyOpacity(textColor, alpha);
        DropdownFontRenderer.drawString(modeSetting.getModeString(), boxX + 2, 
            boxY + 2, valueColor.getRGB(), 18);

        float rectHeight = 12;
        float rectCount = 0;
        for (String mode : modeSetting.getModes()) {
            if (!mode.equals(modeSetting.getModeString())) {
                rectCount++;
            }
        }
        
        normalCount = 1 + rectCount;
        
        double animation = openAnimation.getOutput();
        float modeHeight = (float) (rectCount * rectHeight * animation);
        float modeY = boxY + boxHeight;
        float modeX = boxX;
        
        if (opened || !openAnimation.isDone()) {
            if (modeHeight > 0.5f) {
                net.minecraft.client.gui.Gui.drawRect((int)modeX, (int)modeY, (int)(modeX + boxWidth), (int)(modeY + modeHeight), 
                    ColorUtil.applyOpacity(settingRectColor.brighter(), alpha * 0.9f).getRGB());

                float currentY = 0;
                for (String mode : modeSetting.getModes()) {
                    if (mode.equals(modeSetting.getModeString())) continue;
                    
                    float itemY = modeY + currentY;
                    boolean hoveringMode = isHovering(modeX, itemY, boxWidth, rectHeight, mouseX, mouseY);

                    if (hoveringMode && animation > 0.5) {
                        net.minecraft.client.gui.Gui.drawRect((int)(modeX + 0.5f), (int)(itemY + 0.5f), (int)(modeX + boxWidth - 0.5f), (int)(itemY + rectHeight - 0.5f), 
                            ColorUtil.applyOpacity(settingRectColor.brighter().brighter(), alpha * 0.5f).getRGB());
                    }

                    Color modeColor = ColorUtil.applyOpacity(textColor, alpha * (float) animation);
                    DropdownFontRenderer.drawString(mode, modeX + 2, 
                        itemY + 1, modeColor.getRGB(), 18);

                    currentY += rectHeight;
                }
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        ModeValue modeSetting = getSetting();
        float nameHeight = 10;
        float boxHeight = 12;
        float boxY = y + nameHeight + 1;
        float boxX = x + 4;
        float boxWidth = width - 8;

        if (isClickable(boxY + boxHeight) && isHovering(boxX, boxY, boxWidth, boxHeight, mouseX, mouseY) && button == 1) {
            opened = !opened;
        }

        if (opened && openAnimation.getOutput() > 0.5) {
            float rectHeight = 12;
            float rectCount = 0;
            float modeY = boxY + boxHeight;
            float modeX = boxX;
            for (String mode : modeSetting.getModes()) {
                if (mode.equals(modeSetting.getModeString())) continue;
                boolean hoveringMode = isHovering(modeX, modeY + rectCount * rectHeight, boxWidth, rectHeight, mouseX, mouseY);
                if (isClickable((modeY + rectCount * rectHeight) + rectHeight) && hoveringMode && button == 0) {
                    modeSetting.parseString(mode);
                    opened = false;
                    return;
                }
                rectCount++;
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
    }

    private boolean isHovering(float x, float y, float width, float height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
