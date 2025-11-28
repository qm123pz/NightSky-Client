package nightsky.module.modules.movement;

import nightsky.NightSky;
import nightsky.enums.FloatModules;
import nightsky.event.EventTarget;
import nightsky.events.LivingUpdateEvent;
import nightsky.events.PlayerUpdateEvent;
import nightsky.events.RightClickMouseEvent;
import nightsky.module.Module;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.IntValue;
import nightsky.value.values.ModeValue;
import nightsky.value.values.PercentValue;
import nightsky.util.BlockUtil;
import nightsky.util.ItemUtil;
import nightsky.util.PlayerUtil;
import nightsky.util.TeamUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class NoSlow
        extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private int lastSlot = -1;
    private boolean noslowSuccess = false;
    private long lastCheckTime = 0L;
    private boolean wasBlocking = false;
    private long lastBlockingTime = 0L;
    private static final long SPRINT_COOLDOWN_MS = 300L;
    private boolean isBlinking = false;
    private int blinkTimer = 0;
    public final ModeValue swordMode = new ModeValue("sword-mode", 1, new String[]{"NONE", "VANILLA", "BLINK"});
    public final PercentValue swordMotion = new PercentValue("sword-motion", 100, () -> (Integer)this.swordMode.getValue() != 0);
    public final BooleanValue swordSprint = new BooleanValue("sword-sprint", true, () -> (Integer)this.swordMode.getValue() != 0);
    public final IntValue swordBlinkDelay = new IntValue("sword-blink-delay", 2, 1, 10, () -> (Integer)this.swordMode.getValue() == 2);
    public final IntValue swordBlinkDuration = new IntValue("sword-blink-duration", 1, 1, 5, () -> (Integer)this.swordMode.getValue() == 2);
    public final ModeValue foodMode = new ModeValue("food-mode", 0, new String[]{"NONE", "VANILLA", "FLOAT", "BLINK"});
    public final PercentValue foodMotion = new PercentValue("food-motion", 100, () -> (Integer)this.foodMode.getValue() != 0);
    public final BooleanValue foodSprint = new BooleanValue("food-sprint", true, () -> (Integer)this.foodMode.getValue() != 0);
    public final IntValue foodBlinkDelay = new IntValue("food-blink-delay", 2, 1, 10, () -> (Integer)this.foodMode.getValue() == 3);
    public final IntValue foodBlinkDuration = new IntValue("food-blink-duration", 1, 1, 5, () -> (Integer)this.foodMode.getValue() == 3);
    public final ModeValue bowMode = new ModeValue("bow-mode", 0, new String[]{"NONE", "VANILLA", "FLOAT", "BLINK"});
    public final PercentValue bowMotion = new PercentValue("bow-motion", 100, () -> (Integer)this.bowMode.getValue() != 0);
    public final BooleanValue bowSprint = new BooleanValue("bow-sprint", true, () -> (Integer)this.bowMode.getValue() != 0);
    public final IntValue bowBlinkDelay = new IntValue("bow-blink-delay", 2, 1, 10, () -> (Integer)this.bowMode.getValue() == 3);
    public final IntValue bowBlinkDuration = new IntValue("bow-blink-duration", 1, 1, 5, () -> (Integer)this.bowMode.getValue() == 3);
    public final BooleanValue successDetection = new BooleanValue("success-detection", true, () -> (Integer)this.swordMode.getValue() == 1 || (Integer)this.swordMode.getValue() == 2);
    public final BooleanValue successMessage = new BooleanValue("success-message", true, () -> (Boolean)this.successDetection.getValue());

    public NoSlow() {
        super("NoSlow", false);
    }

    public boolean isSwordActive() {
        return (Integer)this.swordMode.getValue() != 0 && ItemUtil.isHoldingSword();
    }

    public boolean isFoodActive() {
        return (Integer)this.foodMode.getValue() != 0 && ItemUtil.isEating();
    }

    public boolean isBowActive() {
        return (Integer)this.bowMode.getValue() != 0 && ItemUtil.isUsingBow();
    }

    public boolean isFloatMode() {
        return (Integer)this.foodMode.getValue() == 2 && ItemUtil.isEating() || (Integer)this.bowMode.getValue() == 2 && ItemUtil.isUsingBow();
    }

    public boolean isBlinkMode() {
        return (Integer)this.swordMode.getValue() == 2 && ItemUtil.isHoldingSword() || (Integer)this.foodMode.getValue() == 3 && ItemUtil.isEating() || (Integer)this.bowMode.getValue() == 3 && ItemUtil.isUsingBow();
    }

    public boolean isAnyActive() {
        return NoSlow.mc.thePlayer.isUsingItem() && (this.isSwordActive() || this.isFoodActive() || this.isBowActive());
    }

    public boolean canSprint() {
        return this.isSwordActive() && (Boolean)this.swordSprint.getValue() != false || this.isFoodActive() && (Boolean)this.foodSprint.getValue() != false || this.isBowActive() && (Boolean)this.bowSprint.getValue() != false;
    }

    public int getMotionMultiplier() {
        if (ItemUtil.isHoldingSword()) {
            return (Integer)this.swordMotion.getValue();
        }
        if (ItemUtil.isEating()) {
            return (Integer)this.foodMotion.getValue();
        }
        return ItemUtil.isUsingBow() ? (Integer)this.bowMotion.getValue() : 100;
    }

    private boolean shouldBlink() {
        if (!this.isBlinkMode()) {
            return false;
        }
        ++this.blinkTimer;
        int delay = 2;
        int duration = 1;
        if (ItemUtil.isHoldingSword()) {
            delay = (Integer)this.swordBlinkDelay.getValue();
            duration = (Integer)this.swordBlinkDuration.getValue();
        } else if (ItemUtil.isEating()) {
            delay = (Integer)this.foodBlinkDelay.getValue();
            duration = (Integer)this.foodBlinkDuration.getValue();
        } else if (ItemUtil.isUsingBow()) {
            delay = (Integer)this.bowBlinkDelay.getValue();
            duration = (Integer)this.bowBlinkDuration.getValue();
        }
        int totalCycle = delay + duration;
        int currentPhase = this.blinkTimer % totalCycle;
        if (currentPhase < delay) {
            this.isBlinking = false;
            return false;
        }
        this.isBlinking = true;
        return true;
    }

    private boolean checkNoSlowSuccess() {
        boolean newSuccessState;
        if (!(this.isEnabled() && this.isSwordActive() && ((Boolean)this.successDetection.getValue()).booleanValue())) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastCheckTime < 500L) {
            return this.noslowSuccess;
        }
        this.lastCheckTime = currentTime;
        boolean wasSprinting = NoSlow.mc.thePlayer.isSprinting();
        boolean isMoving = Math.abs(NoSlow.mc.thePlayer.movementInput.moveForward) > 0.1f || Math.abs(NoSlow.mc.thePlayer.movementInput.moveStrafe) > 0.1f;
        boolean bl = newSuccessState = wasSprinting && isMoving && PlayerUtil.isUsingItem();
        if (newSuccessState != this.noslowSuccess && ((Boolean)this.successMessage.getValue()).booleanValue()) {
            if (newSuccessState) {
                NoSlow.mc.thePlayer.addChatMessage((IChatComponent)new ChatComponentText("§a[NoSlow] §fSuccess - Sword blocking without slowdown!"));
            } else {
                NoSlow.mc.thePlayer.addChatMessage((IChatComponent)new ChatComponentText("§c[NoSlow] §fFailed - Normal sword blocking slowdown"));
            }
        }
        this.noslowSuccess = newSuccessState;
        return this.noslowSuccess;
    }

    public boolean isNoSlowSuccess() {
        return this.checkNoSlowSuccess();
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        boolean isCurrentlyBlocking;
        if (!this.isEnabled()) {
            this.wasBlocking = false;
            return;
        }
        boolean bl = isCurrentlyBlocking = this.isSwordActive() && PlayerUtil.isUsingItem();
        if (this.isBlinkMode() && this.shouldBlink()) {
            if (this.isSwordActive()) {
                NoSlow.mc.thePlayer.stopUsingItem();
            }
            isCurrentlyBlocking = false;
            this.wasBlocking = false;
            return;
        }
        if (isCurrentlyBlocking) {
            this.wasBlocking = true;
            this.lastBlockingTime = System.currentTimeMillis();
        }
        boolean inSprintProtection = System.currentTimeMillis() - this.lastBlockingTime < 300L;
        boolean playerWantsToSprint = NoSlow.mc.gameSettings.keyBindSprint.isKeyDown();
        if (this.isAnyActive() || inSprintProtection) {
            if (this.isSwordActive() || inSprintProtection) {
                this.checkNoSlowSuccess();
            }
            float multiplier = (float)this.getMotionMultiplier() / 100.0f;
            if (this.isAnyActive()) {
                NoSlow.mc.thePlayer.movementInput.moveForward *= multiplier;
                NoSlow.mc.thePlayer.movementInput.moveStrafe *= multiplier;
            }
            if ((this.canSprint() || inSprintProtection) && playerWantsToSprint && NoSlow.mc.thePlayer.movementInput.moveForward > 0.1f) {
                NoSlow.mc.thePlayer.setSprinting(true);
            } else {
                NoSlow.mc.thePlayer.setSprinting(false);
            }
        } else {
            this.wasBlocking = false;
        }
    }

    @EventTarget(value=3)
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (this.isEnabled() && this.isFloatMode()) {
            int item = NoSlow.mc.thePlayer.inventory.currentItem;
            if (this.lastSlot != item && PlayerUtil.isUsingItem()) {
                this.lastSlot = item;
                NightSky.floatManager.setFloatState(true, FloatModules.NO_SLOW);
            }
        } else {
            this.lastSlot = -1;
            NightSky.floatManager.setFloatState(false, FloatModules.NO_SLOW);
        }
        if (this.isSwordActive() && ((Boolean)this.successDetection.getValue()).booleanValue()) {
            this.checkNoSlowSuccess();
        }
    }

    @EventTarget
    public void onRightClick(RightClickMouseEvent event) {
        if (this.isEnabled()) {
            if (NoSlow.mc.objectMouseOver != null) {
                switch (NoSlow.mc.objectMouseOver.typeOfHit) {
                    case BLOCK: {
                        BlockPos blockPos = NoSlow.mc.objectMouseOver.getBlockPos();
                        if (!BlockUtil.isInteractable(blockPos) || PlayerUtil.isSneaking()) break;
                        return;
                    }
                    case ENTITY: {
                        Entity entityHit = NoSlow.mc.objectMouseOver.entityHit;
                        if (entityHit instanceof EntityVillager) {
                            return;
                        }
                        if (!(entityHit instanceof EntityLivingBase) || !TeamUtil.isShop((EntityLivingBase)entityHit)) break;
                        return;
                    }
                }
            }
            if (this.isFloatMode() && !NightSky.floatManager.isPredicted() && NoSlow.mc.thePlayer.onGround) {
                event.setCancelled(true);
                NoSlow.mc.thePlayer.motionY = 0.42f;
            }
        }
    }

    public String checkAndReturnStatus() {
        boolean success = this.checkNoSlowSuccess();
        return success ? "success" : "failed";
    }

    @Override
    public void onEnabled() {
        this.blinkTimer = 0;
        this.isBlinking = false;
        this.noslowSuccess = false;
        this.lastCheckTime = 0L;
        this.wasBlocking = false;
        this.lastBlockingTime = 0L;
    }

    @Override
    public void onDisabled() {
        this.blinkTimer = 0;
        this.isBlinking = false;
        this.noslowSuccess = false;
        this.lastCheckTime = 0L;
        this.wasBlocking = false;
        this.lastBlockingTime = 0L;
        if (NoSlow.mc.thePlayer != null) {
            NoSlow.mc.thePlayer.stopUsingItem();
        }
    }
}