package nightsky.module.modules.player;

import nightsky.NightSky;
import nightsky.enums.BlinkModules;
import nightsky.event.EventTarget;
import nightsky.event.types.EventType;
import nightsky.event.types.Priority;
import nightsky.events.LoadWorldEvent;
import nightsky.events.TickEvent;
import nightsky.module.Module;
import nightsky.value.values.IntValue;
import nightsky.value.values.ModeValue;

public class Blink extends Module {
    public final ModeValue mode = new ModeValue("mode", 0, new String[]{"DEFAULT", "PULSE"});
    public final IntValue ticks = new IntValue("ticks", 20, 0, 1200);

    public Blink() {
        super("Blink", false);
    }

    @EventTarget(Priority.LOWEST)
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.POST) {
            if (!NightSky.blinkManager.getBlinkingModule().equals(BlinkModules.BLINK)) {
                this.setEnabled(false);
            } else {
                if (this.ticks.getValue() > 0 && NightSky.blinkManager.countMovement() > (long) this.ticks.getValue()) {
                    switch (this.mode.getValue()) {
                        case 0:
                            this.setEnabled(false);
                            break;
                        case 1:
                            NightSky.blinkManager.setBlinkState(false, BlinkModules.BLINK);
                            NightSky.blinkManager.setBlinkState(true, BlinkModules.BLINK);
                    }
                }
            }
        }
    }

    @EventTarget
    public void onWorldLoad(LoadWorldEvent event) {
        this.setEnabled(false);
    }

    @Override
    public void onEnabled() {
        NightSky.blinkManager.setBlinkState(false, NightSky.blinkManager.getBlinkingModule());
        NightSky.blinkManager.setBlinkState(true, BlinkModules.BLINK);
    }

    @Override
    public void onDisabled() {
        NightSky.blinkManager.setBlinkState(false, BlinkModules.BLINK);
    }
}