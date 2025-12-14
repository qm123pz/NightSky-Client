package nightsky.module.modules.player;

import nightsky.NightSky;
import nightsky.event.EventTarget;
import nightsky.event.types.EventType;
import nightsky.events.UpdateEvent;
import nightsky.module.Module;
import nightsky.util.TimerUtil;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.FloatValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class Refill extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private final TimerUtil timer = new TimerUtil();
    private final FloatValue delay = new FloatValue("Delay", 100.0F, 50.0F, 1000.0F);
    private final BooleanValue soup = new BooleanValue("Soup", false);
    private final BooleanValue pot = new BooleanValue("Pot", false);
    private final BooleanValue onInv = new BooleanValue("OnInv", false);
    private Item targetItem = null;
    public Refill() {
        super("Refill", false);
    }
    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!isEnabled() || event.getType() != EventType.PRE) {
            return;
        }
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }
        if (mc.thePlayer.getHealth() <= 0.0F) {
            return;
        }
        if (mc.theWorld.isRemote && mc.theWorld.getLastLightningBolt() > 0) {
             return;
        }
        if (soup.getValue()) {
            targetItem = Items.mushroom_stew;
        } else if (pot.getValue()) {
            targetItem = Items.potionitem;
        } else {
            return;
        }
        if (!onInv.getValue() || mc.currentScreen instanceof GuiInventory) {
            if (timer.hasTimeElapsed(delay.getValue())) {
                if (!isHotbarFull()) {
                    int sourceSlot = findSourceSlot(targetItem);
                    if (sourceSlot != -1) {
                        if (isOperationBlocked()) {
                            return;
                        }
                        performRefill(sourceSlot);
                        timer.reset();
                    }
                    timer.reset();
                } else {
                     timer.reset();
                }
            }
        }
    }
    private boolean isHotbarFull() {
        InventoryPlayer inventory = mc.thePlayer.inventory;
        for (int i = 0; i < 9; i++) {
            if (inventory.getStackInSlot(i) == null) {
                return false;
            }
        }
        return true;
    }
    private int findSourceSlot(Item item) {
        if (item == null) return -1;
        InventoryPlayer inventory = mc.thePlayer.inventory;
        for (int i = 9; i < 36; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }
    private void performRefill(int sourceSlot) {
        if (sourceSlot < 9 || sourceSlot > 35) {
            System.err.println("Refill Error: Attempting to refill from invalid slot: " + sourceSlot);
            return;
        }
        mc.playerController.windowClick(0, sourceSlot, 0, 1, mc.thePlayer);
    }
    private boolean isOperationBlocked() {
        if (NightSky.playerStateManager.digging || NightSky.playerStateManager.placing) {
            return true;
        }
        if (mc.thePlayer != null && mc.thePlayer.isUsingItem()) {
            return true;
        }
        if (NightSky.moduleManager.getModule("Scaffold").isEnabled() ||
            NightSky.moduleManager.getModule("BedNuker").isEnabled()) {
            return true;
        }
        if (onInv.getValue() && !(mc.currentScreen instanceof GuiInventory)) {
            return false;
        }
        if (((nightsky.mixin.IAccessorPlayerControllerMP) mc.playerController).getIsHittingBlock()) {
            return true;
        }
        return false;
    }
}