package nightsky.module.modules.player;

import nightsky.event.EventTarget;
import nightsky.event.types.EventType;
import nightsky.events.TickEvent;
import nightsky.mixin.IAccessorPlayerControllerMP;
import nightsky.module.Module;
import nightsky.value.values.IntValue;
import nightsky.value.values.PercentValue;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

public class SpeedMine extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final PercentValue speed = new PercentValue("Speed", 15);
    public final IntValue delay = new IntValue("Delay", 0, 0, 4);

    public SpeedMine() {
        super("SpeedMine", false);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            if (!mc.playerController.isInCreativeMode()) {
                if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) {
                    ((IAccessorPlayerControllerMP) mc.playerController)
                            .setBlockHitDelay(Math.min(((IAccessorPlayerControllerMP) mc.playerController).getBlockHitDelay(), this.delay.getValue() + 1));
                    if (((IAccessorPlayerControllerMP) mc.playerController).getIsHittingBlock()) {
                        float curBlockDamageMP = ((IAccessorPlayerControllerMP) mc.playerController).getCurBlockDamageMP();
                        float damage = 0.3F * (this.speed.getValue().floatValue() / 100.0F);
                        if (curBlockDamageMP < damage) {
                            ((IAccessorPlayerControllerMP) mc.playerController).setCurBlockDamageMP(damage);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String[] getSuffix() {
        return new String[]{String.format("%d%%", this.speed.getValue())};
    }
}
