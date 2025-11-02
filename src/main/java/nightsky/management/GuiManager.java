package nightsky.management;

import nightsky.ui.clickgui.ClickGui;
import nightsky.ui.clickgui.augustus.AugustusClickGui;
import net.minecraft.client.Minecraft;
import nightsky.ui.clickgui.best.BestClickGui;
import nightsky.ui.clickgui.dropdown.DropdownClickGui;

public class GuiManager {
    private final Minecraft mc = Minecraft.getMinecraft();
    private ClickGui clickGui;
    private AugustusClickGui augustusClickGui;
    private BestClickGui bestClickGui;
    private DropdownClickGui dropdownClickGui;
    
    public GuiManager() {
        this.clickGui = new ClickGui();
        this.augustusClickGui = new AugustusClickGui();
        this.bestClickGui = new BestClickGui();
        this.dropdownClickGui = new DropdownClickGui();
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
    
    public void openBestGui() {
        if (mc.currentScreen == null) {
            mc.displayGuiScreen(bestClickGui);
        }
    }
    
    public void closeBestGui() {
        if (mc.currentScreen == bestClickGui) {
            bestClickGui.closeGui();
        }
    }
    
    public boolean isBestGuiOpen() {
        return mc.currentScreen == bestClickGui;
    }
    
    public BestClickGui getBestClickGui() {
        return bestClickGui;
    }
    
    public void openDropdownGui() {
        if (mc.currentScreen == null) {
            mc.displayGuiScreen(dropdownClickGui);
        }
    }
    
    public void closeDropdownGui() {
        if (mc.currentScreen == dropdownClickGui) {
            mc.displayGuiScreen(null);
        }
    }
    
    public boolean isDropdownGuiOpen() {
        return mc.currentScreen == dropdownClickGui;
    }
    
    public DropdownClickGui getDropdownClickGui() {
        return dropdownClickGui;
    }
}
