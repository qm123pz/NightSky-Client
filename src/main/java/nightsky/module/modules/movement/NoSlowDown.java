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

public class NoSlowDown extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private int lastSlot = -1;
    
    public final ModeValue mode = new ModeValue("Mode", 0, new String[]{"Vanilla", "Blink", "Grim", "Prediction"});
    public final ModeValue swordMode = new ModeValue("Sword Mode", 0, new String[]{"None", "Vanilla", "Item", "Fake"});
    public final ModeValue itemMode = new ModeValue("Item Mode", 0, new String[]{"Vanilla", "Pre", "Post", "Alpha", "Float", "Blink"});
    public final PercentValue slowPercent = new PercentValue("Slow %", 80);
    public final BooleanValue disableSword = new BooleanValue("Disable Sword", false);
    public final BooleanValue disableBow = new BooleanValue("Disable Bow", false);

    public NoSlowDown() {
        super("NoSlowDown", false);
    }

    public boolean isSwordActive() {
        return this.swordMode.getValue() != 0 && ItemUtil.isHoldingSword();
    }

    public boolean isFoodActive() {
        return this.itemMode.getValue() != 0 && ItemUtil.isEating();
    }

    public boolean isBowActive() {
        return this.itemMode.getValue() != 0 && ItemUtil.isUsingBow();
    }

    public boolean isFloatMode() {
        return this.itemMode.getValue() == 4;
    }

    public boolean isAnyActive() {
        return mc.thePlayer.isUsingItem() && (this.isSwordActive() || this.isFoodActive() || this.isBowActive());
    }

    public boolean canSprint() {
        return true;
    }

    public float getSlowed() {
        if (mc.thePlayer.getHeldItem() == null || !this.isEnabled()) {
            return 1.0f;
        }
        if (ItemUtil.isUsingBow() && disableBow.getValue()) {
            return 1.0f;
        }
        if (ItemUtil.isHoldingSword() && disableSword.getValue()) {
            return 1.0f;
        }

        float slowValue = (float) slowPercent.getValue();

        if (slowValue == 0) {
            return 1.0f; // 100% 速度，不减速
        } else if (slowValue == 100) {
            return 0.0f; // 0% 速度，完全停止
        }

        // 正确的减速计算：slowValue 是减速百分比
        // 例如：slowValue = 80 表示减速80%，应该保留20%的速度
        return 1.0f - (slowValue / 100.0f);
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (this.isEnabled() && this.isAnyActive() && mode.getValue() == 0) {
            float multiplier = getSlowed();
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