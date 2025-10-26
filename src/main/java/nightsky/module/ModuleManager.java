package nightsky.module;

import net.minecraft.util.ResourceLocation;
import nightsky.NightSky;
import nightsky.event.EventTarget;
import nightsky.event.types.EventType;
import nightsky.events.KeyEvent;
import nightsky.events.TickEvent;
import nightsky.util.ChatUtil;
import nightsky.util.SoundUtil;

import java.util.LinkedHashMap;

public class ModuleManager {
    private boolean sound = false;
    private boolean soundEnabled = false;
    public final LinkedHashMap<Class<?>, Module> modules = new LinkedHashMap<>();

    public Module getModule(String string) {
        return this.modules.values().stream().filter(mD -> mD.getName().equalsIgnoreCase(string)).findFirst().orElse(null);
    }

    public java.util.List<Module> getModulesInCategory(ModuleCategory category) {
        return this.modules.values().stream().filter(module -> module.getCategory() == category).collect(java.util.stream.Collectors.toList());
    }

    public void playSound(boolean enabled) {
        this.sound = true;
        this.soundEnabled = enabled;
    }

    @EventTarget
    public void onKey(KeyEvent event) {
        for (Module module : this.modules.values()) {
            if (module.getKey() != event.getKey()) {
                continue;
            }
            module.toggle();
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getType() == EventType.PRE) {
            if (this.sound) {
                this.sound = false;
                playToggleSound(this.soundEnabled);
            }
        }
    }
    
    private void playToggleSound(boolean enabled) {
        nightsky.module.modules.render.Interface interfaceModule = 
            (nightsky.module.modules.render.Interface) getModule("Interface");
        
        if (interfaceModule == null) {
            System.out.println("[ModuleManager] Interface module is null, playing vanilla sound");
            SoundUtil.playSound("random.click");
            return;
        }
        
        String mode = interfaceModule.toggleSound.getModeString();
        String soundFile = enabled ? "enable" : "disable";
        System.out.println("[ModuleManager] ToggleSound mode: " + mode + ", enabled: " + enabled);
        
        switch (mode) {
            case "Augustus":
                SoundUtil.playSound(new ResourceLocation("nightsky/sounds/augustus/" + soundFile + ".wav"), 1.0f);
                break;
            case "Jello":
                SoundUtil.playSound(new ResourceLocation("nightsky/sounds/jello/" + soundFile + ".wav"), 1.0f);
                break;
            case "Other":
                SoundUtil.playSound(new ResourceLocation("nightsky/sounds/other/" + soundFile + ".wav"), 1.0f);
                break;
            case "Vanilla":
            default:
                SoundUtil.playSound("random.click");
                break;
        }
    }
}