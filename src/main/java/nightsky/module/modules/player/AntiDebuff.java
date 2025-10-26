package nightsky.module.modules.player;

import nightsky.module.Module;
import nightsky.value.values.BooleanValue;

public class AntiDebuff extends Module {
    public final BooleanValue blindness = new BooleanValue("Blindness", true);
    public final BooleanValue nausea = new BooleanValue("Nausea", true);

    public AntiDebuff() {
        super("AntiDebuff", false);
    }
}
