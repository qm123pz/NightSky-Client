package nightsky.management;

import nightsky.ui.clickgui.ClickGui;
import nightsky.ui.clickgui.augustus.AugustusClickGui;
import net.minecraft.client.Minecraft;

public class GuiManager {
    private final Minecraft mc = Minecraft.getMinecraft();
    private ClickGui clickGui;
    private AugustusClickGui augustusClickGui;
    
    public GuiManager() {
        this.clickGui = new ClickGui();
        this.augustusClickGui = new AugustusClickGui();
    }
    
    public void openClickGui() {
        if (mc.currentScreen == null) {
            mc.displayGuiScreen(clickGui);
        }
    }
    
    public void openAugustusGui() {
        if (mc.currentScreen == null) {
            mc.displayGuiScreen(augustusClickGui);
        }
    }
    
    public void closeClickGui() {
        if (mc.currentScreen == clickGui) {
            mc.displayGuiScreen(null);
        }
    }
    
    public void closeAugustusGui() {
        if (mc.currentScreen == augustusClickGui) {
            mc.displayGuiScreen(null);
        }
    }
    
    public boolean isClickGuiOpen() {
        return mc.currentScreen == clickGui;
    }
    
    public boolean isAugustusGuiOpen() {
        return mc.currentScreen == augustusClickGui;
    }
    
    public ClickGui getClickGui() {
        return clickGui;
    }
    
    public AugustusClickGui getAugustusClickGui() {
        return augustusClickGui;
    }
}
