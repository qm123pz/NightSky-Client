package nightsky.ui.clickgui.dropdown.components.settings;

import nightsky.ui.clickgui.dropdown.components.SettingComponent;
import nightsky.ui.clickgui.dropdown.utils.DropdownFontRenderer;
import nightsky.util.render.ColorUtil;
import nightsky.util.render.animations.advanced.Animation;
import nightsky.util.render.animations.advanced.Direction;
import nightsky.util.render.animations.advanced.impl.EaseInOutQuad;
import nightsky.value.values.BooleanValue;

import java.awt.*;

public class BooleanComponent extends SettingComponent<BooleanValue> {

    private final Animation toggleAnimation = new EaseInOutQuad(250, 1);

    public BooleanComponent(BooleanValue booleanValue) {
        super(booleanValue);
    }

    @Override
    public void initGui() {
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
    }

    @Override
    public void updateCountSize() {
        countSize = 1;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        boolean enabled = getSetting().getValue();
        toggleAnimation.setDirection(enabled ? Direction.FORWARDS : Direction.BACKWARDS);
        
        float switchWidth = 20;
        float switchHeight = 10;
        float booleanX = x + width - (switchWidth + 6);
        float booleanY = y + 5;

        String name = getSetting().getName();
        
        float textAlpha = (float) (0.6 + 0.4 * toggleAnimation.getOutput());
        Color nameColor = ColorUtil.applyOpacity(textColor, alpha * textAlpha);
        
        DropdownFontRenderer.drawString(name, x + 5, y + DropdownFontRenderer.getMiddleOfBox(height, 18), nameColor.getRGB(), 18);

        Color accentColor = new Color(255, 255, 255);
        Color offColor = ColorUtil.applyOpacity(settingRectColor.brighter().brighter(), alpha);
        Color rectColor = ColorUtil.interpolateColorC(offColor, 
            ColorUtil.applyOpacity(accentColor, alpha), (float) toggleAnimation.getOutput());

        net.minecraft.client.gui.Gui.drawRect((int)booleanX, (int)booleanY, (int)(booleanX + switchWidth), (int)(booleanY + switchHeight), rectColor.getRGB());

        float circleSize = switchHeight - 4;
        float circleX = (float) (booleanX + 2 + (switchWidth - circleSize - 4) * toggleAnimation.getOutput());
        float circleY = booleanY + 2;
        net.minecraft.client.gui.Gui.drawRect((int)circleX, (int)circleY, (int)(circleX + circleSize), (int)(circleY + circleSize), 
            ColorUtil.applyOpacity(Color.WHITE, alpha).getRGB());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0 && isClickable(y + height) && isHoveringBox(mouseX, mouseY)) {
            getSetting().setValue(!getSetting().getValue());
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
    }

    private boolean isHovering(float x, float y, float width, float height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
