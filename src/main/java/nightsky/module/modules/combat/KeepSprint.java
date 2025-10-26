package nightsky.module.modules.combat;

import nightsky.module.Module;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.PercentValue;
import net.minecraft.client.Minecraft;

public class KeepSprint extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final PercentValue slowdown = new PercentValue("Slowdown", 0);
    public final BooleanValue groundOnly = new BooleanValue("GroundOnly", false);
    public final BooleanValue reachOnly = new BooleanValue("ReachOnly", false);

    public KeepSprint() {
        super("KeepSprint", false);
    }

    public boolean shouldKeepSprint() {
        if (this.groundOnly.getValue() && !mc.thePlayer.onGround) {
            return false;
        } else {
            return !this.reachOnly.getValue() || mc.objectMouseOver.hitVec.distanceTo(mc.getRenderViewEntity().getPositionEyes(1.0F)) > 3.0;
        }
    }
}
