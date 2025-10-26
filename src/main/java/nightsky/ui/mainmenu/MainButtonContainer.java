package nightsky.ui.mainmenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiSelectWorld;
import nightsky.ui.mainmenu.altmanager.GuiAltManager;
import nightsky.util.render.BlurUtil;
import nightsky.util.render.RenderUtil;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class MainButtonContainer {
    private final List<GuiMainButton> buttons = new ArrayList<GuiMainButton>();
    private final int containerX;
    private final int containerY;
    private final int buttonSize = 40;
    private final int spacing = 65;
    
    public MainButtonContainer(int centerX, int centerY, Minecraft mc) {
        int totalWidth = 460;
        this.containerX = centerX - totalWidth / 2;
        this.containerY = centerY;
        int currentX = this.containerX;
        this.buttons.add(new GuiMainButton(currentX, this.containerY, 40, 40, GuiMainButton.ButtonType.SINGLEPLAYER, () -> mc.displayGuiScreen(new GuiSelectWorld(mc.currentScreen))));
        this.buttons.add(new GuiMainButton(currentX += 105, this.containerY, 40, 40, GuiMainButton.ButtonType.MULTIPLAYER, () -> mc.displayGuiScreen(new GuiMultiplayer(mc.currentScreen))));
        this.buttons.add(new GuiMainButton(currentX += 105, this.containerY, 40, 40, GuiMainButton.ButtonType.ALTMANAGER, () -> mc.displayGuiScreen(new GuiAltManager(mc.currentScreen))));
        this.buttons.add(new GuiMainButton(currentX += 105, this.containerY, 40, 40, GuiMainButton.ButtonType.OPTIFINE, () -> mc.displayGuiScreen(new GuiOptions(mc.currentScreen, mc.gameSettings))));
        this.buttons.add(new GuiMainButton(currentX += 105, this.containerY, 40, 40, GuiMainButton.ButtonType.QUIT, () -> mc.shutdown()));
    }
    
    public void drawContainer(int mouseX, int mouseY) {
        int totalWidth = 460;
        int bgPaddingWidth = 20;
        int bgPaddingHeight = 6;
        int bgX = this.containerX - bgPaddingWidth;
        int bgY = this.containerY - bgPaddingHeight;
        int bgWidth = totalWidth + bgPaddingWidth * 2;
        int bgHeight = 40 + bgPaddingHeight * 2;
        BlurUtil.blurAreaRounded(bgX, bgY, bgX + bgWidth, bgY + bgHeight, 40.0f, 80.0f);
        RenderUtil.drawRoundedRect((float)bgX, (float)bgY, (float)bgWidth, (float)bgHeight, 40.0f, new Color(0, 0, 0, 80));
        for (GuiMainButton button : this.buttons) {
            button.drawButton(mouseX, mouseY);
        }
    }
    
    public boolean handleClick(int mouseX, int mouseY) {
        for (GuiMainButton button : this.buttons) {
            if (button.isHovered(mouseX, mouseY)) {
                button.onClick();
                return true;
            }
        }
        return false;
    }
}