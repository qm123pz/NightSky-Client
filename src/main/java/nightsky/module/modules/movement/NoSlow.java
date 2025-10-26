package nightsky.module.modules.movement;

import nightsky.NightSky;
import nightsky.enums.FloatModules;
import nightsky.event.EventTarget;
import nightsky.event.types.Priority;
import nightsky.events.LivingUpdateEvent;
import nightsky.events.PlayerUpdateEvent;
import nightsky.events.RightClickMouseEvent;
import nightsky.module.Module;
import nightsky.util.BlockUtil;
import nightsky.util.ItemUtil;
import nightsky.util.PlayerUtil;
import nightsky.util.TeamUtil;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.PercentValue;
import nightsky.value.values.ModeValue;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.BlockPos;

public class NoSlow extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private int lastSlot = -1;
    public final ModeValue swordMode = new ModeValue("SwordMode", 1, new String[]{"NONE", "Vanilla"});
    public final PercentValue swordMotion = new PercentValue("SwordMotion", 100, () -> this.swordMode.getValue() != 0);
    public final BooleanValue swordSprint = new BooleanValue("SwordSprint", true, () -> this.swordMode.getValue() != 0);
    public final ModeValue foodMode = new ModeValue("FoodMode", 0, new String[]{"NONE", "Vanilla", "Float"});
    public final PercentValue foodMotion = new PercentValue("FoodMotion", 100, () -> this.foodMode.getValue() != 0);
    public final BooleanValue foodSprint = new BooleanValue("FoodSprint", true, () -> this.foodMode.getValue() != 0);
    public final ModeValue bowMode = new ModeValue("BowMode", 0, new String[]{"NONE", "Vanilla", "Float"});
    public final PercentValue bowMotion = new PercentValue("BowMotion", 100, () -> this.bowMode.getValue() != 0);
    public final BooleanValue bowSprint = new BooleanValue("BowSprint", true, () -> this.bowMode.getValue() != 0);

    public NoSlow() {
        super("NoSlow", false);
    }

    public boolean isSwordActive() {
        return this.swordMode.getValue() != 0 && ItemUtil.isHoldingSword();
    }

    public boolean isFoodActive() {
        return this.foodMode.getValue() != 0 && ItemUtil.isEating();
    }

    public boolean isBowActive() {
        return this.bowMode.getValue() != 0 && ItemUtil.isUsingBow();
    }

    public boolean isFloatMode() {
        return this.foodMode.getValue() == 2 && ItemUtil.isEating()
                || this.bowMode.getValue() == 2 && ItemUtil.isUsingBow();
    }

    public boolean isAnyActive() {
        return mc.thePlayer.isUsingItem() && (this.isSwordActive() || this.isFoodActive() || this.isBowActive());
    }

    public boolean canSprint() {
        return this.isSwordActive() && this.swordSprint.getValue()
                || this.isFoodActive() && this.foodSprint.getValue()
                || this.isBowActive() && this.bowSprint.getValue();
    }

    public int getMotionMultiplier() {
        if (ItemUtil.isHoldingSword()) {
            return this.swordMotion.getValue();
        } else if (ItemUtil.isEating()) {
            return this.foodMotion.getValue();
        } else {
            return ItemUtil.isUsingBow() ? this.bowMotion.getValue() : 100;
        }
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (this.isEnabled() && this.isAnyActive()) {
            float multiplier = (float) this.getMotionMultiplier() / 100.0F;
            mc.thePlayer.movementInput.moveForward *= multiplier;
            mc.thePlayer.movementInput.moveStrafe *= multiplier;
            if (!this.canSprint()) {
                mc.thePlayer.setSprinting(false);
            }
        }
    }

    @EventTarget(Priority.LOW)
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (this.isEnabled() && this.isFloatMode()) {
            int item = mc.thePlayer.inventory.currentItem;
            if (this.lastSlot != item && PlayerUtil.isUsingItem()) {
                this.lastSlot = item;
                NightSky.floatManager.setFloatState(true, FloatModules.NO_SLOW);
            }
        } else {
            this.lastSlot = -1;
            NightSky.floatManager.setFloatState(false, FloatModules.NO_SLOW);
        }
    }

    @EventTarget
    public void onRightClick(RightClickMouseEvent event) {
        if (this.isEnabled()) {
            if (mc.objectMouseOver != null) {
                switch (mc.objectMouseOver.typeOfHit) {
                    case BLOCK:
                        BlockPos blockPos = mc.objectMouseOver.getBlockPos();
                        if (BlockUtil.isInteractable(blockPos) && !PlayerUtil.isSneaking()) {
                            return;
                        }
                        break;
                    case ENTITY:
                        Entity entityHit = mc.objectMouseOver.entityHit;
                        if (entityHit instanceof EntityVillager) {
                            return;
                        }
                        if (entityHit instanceof EntityLivingBase && TeamUtil.isShop((EntityLivingBase) entityHit)) {
                            return;
                        }
                }
            }
            if (this.isFloatMode() && !NightSky.floatManager.isPredicted() && mc.thePlayer.onGround) {
                event.setCancelled(true);
                mc.thePlayer.motionY = 0.42F;
            }
        }
    }
}