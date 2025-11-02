package nightsky.ui.clickgui.dropdown.components;

import nightsky.value.Value;
import net.minecraft.client.Minecraft;

import java.awt.*;

public abstract class SettingComponent<T extends Value> {
    protected final T setting;
    protected final Minecraft mc = Minecraft.getMinecraft();
    
    public float x, y, width, height;
    public boolean typing;
    public float panelLimitY;
    public Color settingRectColor, textColor;
    public float countSize = 1;
    public float alpha = 1f;

    public SettingComponent(T setting) {
        this.setting = setting;
    }

    public T getSetting() {
        return setting;
    }

    public abstract void initGui();
    
    public abstract void keyTyped(char typedChar, int keyCode);
    
    public abstract void updateCountSize();
    
    public abstract void drawScreen(int mouseX, int mouseY);
    
    public abstract void mouseClicked(int mouseX, int mouseY, int button);
    
    public abstract void mouseReleased(int mouseX, int mouseY, int state);

    public boolean isHoveringBox(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean isClickable(float bottomY) {
        return bottomY > panelLimitY && bottomY < panelLimitY + 400;
    }

    public void onGuiClosed() {
    }
}
