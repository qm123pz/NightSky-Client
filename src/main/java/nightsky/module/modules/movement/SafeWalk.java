package nightsky.module.modules.movement;

import nightsky.NightSky;
import nightsky.event.EventTarget;
import nightsky.event.types.EventType;
import nightsky.events.SafeWalkEvent;
import nightsky.events.UpdateEvent;
import nightsky.module.Module;
import nightsky.module.modules.player.Scaffold;
import nightsky.util.ItemUtil;
import nightsky.util.MoveUtil;
import nightsky.util.PlayerUtil;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.FloatValue;
import net.minecraft.client.Minecraft;

public class SafeWalk extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final FloatValue motion = new FloatValue("Motion", 1.0F, 0.5F, 1.0F);
    public final FloatValue speedMotion = new FloatValue("SpeedMotion", 1.0F, 0.5F, 1.5F);
    public final BooleanValue air = new BooleanValue("Air", false);
    public final BooleanValue directionCheck = new BooleanValue("DirectionCheck", true);
    public final BooleanValue pitCheck = new BooleanValue("PitchCheck", true);
    public final BooleanValue requirePress = new BooleanValue("RequirePress", false);
    public final BooleanValue blocksOnly = new BooleanValue("BlocksOnly", true);

    private boolean canSafeWalk() {
        Scaffold scaffold = (Scaffold) NightSky.moduleManager.modules.get(Scaffold.class);
        if (scaffold.isEnabled()) {
            return false;
        } else if (this.directionCheck.getValue() && mc.gameSettings.keyBindForward.isKeyDown()) {
            return false;
        } else if (this.pitCheck.getValue() && mc.thePlayer.rotationPitch < 69.0F) {
            return false;
        } else if (this.blocksOnly.getValue() && !ItemUtil.isHoldingBlock()) {
            return false;
        } else {
            return (!this.requirePress.getValue() || mc.gameSettings.keyBindUseItem.isKeyDown()) && (mc.thePlayer.onGround && PlayerUtil.canMove(mc.thePlayer.motionX, mc.thePlayer.motionZ, -1.0)
                    || this.air.getValue() && PlayerUtil.canMove(mc.thePlayer.motionX, mc.thePlayer.motionZ, -2.0));
        }
    }

    public SafeWalk() {
        super("SafeWalk", false);
    }

    @EventTarget
    public void onMove(SafeWalkEvent event) {
        if (this.isEnabled()) {
            if (this.canSafeWalk()) {
                event.setSafeWalk(true);
            }
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            if (mc.thePlayer.onGround && MoveUtil.isForwardPressed() && this.canSafeWalk()) {
                if (MoveUtil.getSpeedLevel() <= 0) {
                    if (this.motion.getValue() != 1.0F) {
                        MoveUtil.setSpeed(MoveUtil.getSpeed() * (double) this.motion.getValue());
                    }
                } else if (this.speedMotion.getValue() != 1.0F) {
                    MoveUtil.setSpeed(MoveUtil.getSpeed() * (double) this.speedMotion.getValue());
                }
            }
        }
    }
}
