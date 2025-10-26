package nightsky.module.modules.render;

import nightsky.module.Module;
import nightsky.value.values.PercentValue;

public class NoHurtCam extends Module {
    public final PercentValue multiplier = new PercentValue("Multiplier", 0);

    public NoHurtCam() {
        super("NoHurtCam", false, true);
    }
}
