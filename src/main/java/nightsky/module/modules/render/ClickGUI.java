package nightsky.module.modules.render;

import nightsky.NightSky;
import nightsky.module.Module;
import nightsky.module.ModuleCategory;
import nightsky.value.values.FloatValue;
import nightsky.value.values.ModeValue;
import org.lwjgl.input.Keyboard;

public class ClickGUI extends Module {
    
    private final ModeValue mode = new ModeValue("Mode", 0, new String[]{"IDEA", "Augustus"});

    public ClickGUI() {
        super("ClickGUI", false);
        this.setKey(Keyboard.KEY_RSHIFT);
    }

    @Override
    public void onEnabled() {
        if (NightSky.guiManager != null) {
            if (NightSky.guiManager.isClickGuiOpen() || NightSky.guiManager.isAugustusGuiOpen()) {
                NightSky.guiManager.closeClickGui();
                NightSky.guiManager.closeAugustusGui();
            }
            
            String selectedMode = mode.getModeString();
            if (selectedMode.equals("IDEA")) {
                NightSky.guiManager.openClickGui();
            } else if (selectedMode.equals("Augustus")) {
                NightSky.guiManager.openAugustusGui();
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
