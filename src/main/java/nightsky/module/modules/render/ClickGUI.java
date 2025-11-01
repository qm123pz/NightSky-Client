package nightsky.module.modules.render;

import nightsky.NightSky;
import nightsky.module.Module;
import nightsky.module.ModuleCategory;
import nightsky.value.values.ModeValue;
import org.lwjgl.input.Keyboard;

public class ClickGUI extends Module {
    
    private final ModeValue mode = new ModeValue("Mode", 0, new String[]{"IDEA", "Augustus", "Best"});

    public ClickGUI() {
        super("ClickGUI", false);
        this.setKey(Keyboard.KEY_RSHIFT);
    }

    @Override
    public void onEnabled() {
        if (NightSky.guiManager != null) {
            String selectedMode = mode.getModeString();
            
            if (selectedMode.equals("IDEA")) {
                if (NightSky.guiManager.isClickGuiOpen()) {
                    NightSky.guiManager.closeClickGui();
                } else {
                    if (NightSky.guiManager.isAugustusGuiOpen() || NightSky.guiManager.isBestGuiOpen()) {
                        NightSky.guiManager.closeAugustusGui();
                        NightSky.guiManager.closeBestGui();
                    }
                    NightSky.guiManager.openClickGui();
                }
            } else if (selectedMode.equals("Augustus")) {
                if (NightSky.guiManager.isAugustusGuiOpen()) {
                    NightSky.guiManager.closeAugustusGui();
                } else {
                    if (NightSky.guiManager.isClickGuiOpen() || NightSky.guiManager.isBestGuiOpen()) {
                        NightSky.guiManager.closeClickGui();
                        NightSky.guiManager.closeBestGui();
                    }
                    NightSky.guiManager.openAugustusGui();
                }
            } else if (selectedMode.equals("Best")) {
                if (NightSky.guiManager.isBestGuiOpen()) {
                    NightSky.guiManager.closeBestGui();
                } else {
                    if (NightSky.guiManager.isClickGuiOpen() || NightSky.guiManager.isAugustusGuiOpen()) {
                        NightSky.guiManager.closeClickGui();
                        NightSky.guiManager.closeAugustusGui();
                    }
                    NightSky.guiManager.openBestGui();
                }
            }
        }
        this.setEnabled(false);
    }
    
    public ModeValue getMode() {
        return mode;
    }
    

    @Override
    public void onDisabled() {
    }

    @Override
    public ModuleCategory getCategory() {
        return ModuleCategory.RENDER;
    }
}
