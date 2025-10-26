package nightsky.module.modules.movement;

import nightsky.event.EventTarget;
import nightsky.event.types.EventType;
import nightsky.event.types.Priority;
import nightsky.events.TickEvent;
import nightsky.mixin.IAccessorEntityLivingBase;
import nightsky.module.Module;
import nightsky.value.values.IntValue;
import net.minecraft.client.Minecraft;

public class NoJumpDelay extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final IntValue delay = new IntValue("delay", 3, 0, 8);

    public NoJumpDelay() {
        super("NoJumpDelay", false);
    }

    @EventTarget(Priority.HIGHEST)
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            ((IAccessorEntityLivingBase) mc.thePlayer)
                    .setJumpTicks(Math.min(((IAccessorEntityLivingBase) mc.thePlayer).getJumpTicks(), this.delay.getValue() + 1));
        }
    }

    @Override
    public String[] getSuffix() {
        return new String[]{this.delay.getValue().toString()};
    }
}
