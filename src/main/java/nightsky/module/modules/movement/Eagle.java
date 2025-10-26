package nightsky.module.modules.movement;

import nightsky.event.EventTarget;
import nightsky.event.types.EventType;
import nightsky.event.types.Priority;
import nightsky.events.MoveInputEvent;
import nightsky.events.TickEvent;
import nightsky.module.Module;
import nightsky.util.ItemUtil;
import nightsky.util.MoveUtil;
import nightsky.util.PlayerUtil;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.IntValue;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.RandomUtils;

import java.util.Objects;

public class Eagle extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private int sneakDelay = 0;
    public final IntValue minDelay = new IntValue("MinDelay", 2, 0, 10);
    public final IntValue maxDelay = new IntValue("MaxDelay", 3, 0, 10);
    public final BooleanValue directionCheck = new BooleanValue("DirectionCheck", true);
    public final BooleanValue pitchCheck = new BooleanValue("PitchCheck", true);
    public final BooleanValue blocksOnly = new BooleanValue("BlocksOnly", true);

    private boolean canMoveSafely() {
        double[] offset = MoveUtil.predictMovement();
        return PlayerUtil.canMove(mc.thePlayer.motionX + offset[0], mc.thePlayer.motionZ + offset[1]);
    }

    private boolean shouldSneak() {
        if (this.directionCheck.getValue() && mc.gameSettings.keyBindForward.isKeyDown()) {
            return false;
        } else if (this.pitchCheck.getValue() && mc.thePlayer.rotationPitch < 69.0F) {
            return false;
        } else {
            return (!this.blocksOnly.getValue() || ItemUtil.isHoldingBlock()) && mc.thePlayer.onGround;
        }
    }

    public Eagle() {
        super("Eagle", false);
    }

    @EventTarget(Priority.LOWEST)
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            if (this.sneakDelay > 0) {
                this.sneakDelay--;
            }
            if (this.sneakDelay == 0 && this.canMoveSafely()) {
                this.sneakDelay = RandomUtils.nextInt(this.minDelay.getValue(), this.maxDelay.getValue() + 1);
            }
        }
    }

    @EventTarget(Priority.LOWEST)
    public void onMoveInput(MoveInputEvent event) {
        if (this.isEnabled() && mc.currentScreen == null && !mc.thePlayer.movementInput.sneak) {
            if (this.shouldSneak() && (this.sneakDelay > 0 || this.canMoveSafely())) {
                mc.thePlayer.movementInput.sneak = true;
                mc.thePlayer.movementInput.moveStrafe *= 0.3F;
                mc.thePlayer.movementInput.moveForward *= 0.3F;
            }
        }
    }

    @Override
    public void onDisabled() {
        this.sneakDelay = 0;
    }

    @Override
    public void verifyValue(String string) {
        switch (string) {
            case "min-delay":
                if (this.minDelay.getValue() > this.maxDelay.getValue()) {
                    this.maxDelay.setValue(this.minDelay.getValue());
                }
                break;
            case "max-delay":
                if (this.minDelay.getValue() > this.maxDelay.getValue()) {
                    this.minDelay.setValue(this.maxDelay.getValue());
                }
        }
    }

    @Override
    public String[] getSuffix() {
        return Objects.equals(this.minDelay.getValue(), this.maxDelay.getValue())
                ? new String[]{this.minDelay.getValue().toString()}
                : new String[]{String.format("%d-%d", this.minDelay.getValue(), this.maxDelay.getValue())};
    }
}
