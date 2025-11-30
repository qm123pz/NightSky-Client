package nightsky.module.modules.player;

import nightsky.NightSky;
import nightsky.event.EventTarget;
import nightsky.event.types.EventType;
import nightsky.events.UpdateEvent;
import nightsky.events.WindowClickEvent;
import nightsky.module.Module;
import nightsky.util.ChatUtil;
import nightsky.util.ItemUtil;
import nightsky.module.modules.render.dynamicisland.notification.ChestData;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.IntValue;
import nightsky.value.values.ModeValue;
import nightsky.value.values.FloatValue;
import nightsky.management.RotationState;
import nightsky.util.RotationUtil;
import nightsky.util.MoveUtil;
import nightsky.events.MoveInputEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import net.minecraft.world.WorldSettings.GameType;
import org.apache.commons.lang3.RandomUtils;

public class ChestStealer extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private int clickDelay = 0;
    private int oDelay = 0;
    private boolean inChest = false;
    private boolean warnedFull = false;
    private long lastAuraTime = 0;
    private BlockPos currentTarget = null;
    private float[] targetRotation = null;
    private boolean isRotating = false;
    private final java.util.Set<BlockPos> markedChests = new java.util.HashSet<>();
    
    public final IntValue minDelay = new IntValue("MinDelay", 1, 0, 20);
    public final IntValue maxDelay = new IntValue("MaxDelay", 2, 0, 20);
    public final IntValue openDelay = new IntValue("OpenDelay", 1, 0, 20);
    public final ModeValue mode = new ModeValue("Mode", 0, new String[]{"Normal", "Instant"});
    public final BooleanValue autoClose = new BooleanValue("AutoClose", false);
    public final BooleanValue nameCheck = new BooleanValue("NameCheck", true);
    public final BooleanValue skipTrash = new BooleanValue("SkipTrash", true);
    public final BooleanValue aura = new BooleanValue("Aura", false);
    public final FloatValue auraRange = new FloatValue("AuraRange", 5.0f, 1.0f, 6.0f);
    public final IntValue auraDelay = new IntValue("AuraDelay", 500, 0, 1000);
    public final ModeValue moveFix = new ModeValue("MoveFix", 0, new String[]{"None", "Silent", "Strict"});

    private boolean isValidGameMode() {
        GameType gameType = mc.playerController.getCurrentGameType();
        return gameType == GameType.SURVIVAL || gameType == GameType.ADVENTURE;
    }

    private void shiftClick(int integer1, int integer2) {
        mc.playerController.windowClick(integer1, integer2, 0, 1, mc.thePlayer);
    }

    public ChestStealer() {
        super("ChestStealer", false);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (event.getType() == EventType.PRE) {
            if (this.isEnabled() && aura.getValue() && !inChest && mc.currentScreen == null) {
                handleAura(event);
            }
            
            ChestData.getInstance().updateChestData();
            ChestData.getInstance().updateAnimations();
            if (this.clickDelay > 0) {
                this.clickDelay--;
            }
            if (this.oDelay > 0) {
                this.oDelay--;
            }
            if (!(mc.currentScreen instanceof GuiChest)) {
                this.inChest = false;
                this.currentTarget = null;
                this.targetRotation = null;
                this.isRotating = false;
            } else {
                Container container = ((GuiChest) mc.currentScreen).inventorySlots;
                if (!(container instanceof ContainerChest)) {
                    this.inChest = false;
                } else {
                    if (!this.inChest) {
                        this.inChest = true;
                        this.warnedFull = false;
                        this.oDelay = this.openDelay.getValue() + 1;
                    }
                    if (this.oDelay <= 0 && (this.mode.getValue() == 1 || this.clickDelay <= 0)) {
                        if (this.isEnabled() && this.isValidGameMode()) {
                            IInventory inventory = ((ContainerChest) container).getLowerChestInventory();
                            if (this.nameCheck.getValue()) {
                                String inventoryName = inventory.getName();
                                if (!inventoryName.equals(I18n.format("container.chest")) && !inventoryName.equals(I18n.format("container.chestDouble"))) {
                                    return;
                                }
                            }
                            if (mc.thePlayer.inventory.getFirstEmptyStack() == -1) {
                                if (!this.warnedFull) {
                                    ChatUtil.sendFormatted(String.format("%s%s: &cYour inventory is full!&r", NightSky.clientName, this.getName()));
                                    this.warnedFull = true;
                                }
                                if (this.autoClose.getValue()) {
                                    mc.thePlayer.closeScreen();
                                }
                            } else {
                                if (this.skipTrash.getValue()) {
                                    int bestSword = -1;
                                    double bestDamage = 0.0;
                                    int[] bestArmorSlots = new int[]{-1, -1, -1, -1};
                                    double[] bestArmorProtection = new double[]{0.0, 0.0, 0.0, 0.0};
                                    int bestPickaxeSlot = -1;
                                    float bestPickaxeEfficiency = 1.0F;
                                    int bestShovelSlot = -1;
                                    float bestShovelEfficiency = 1.0F;
                                    int bestAxeSlot = -1;
                                    float bestAxeEfficiency = 1.0F;
                                    boolean hasThrowables = false;
                                    for (int i = 0; i < inventory.getSizeInventory(); i++) {
                                        if (container.getSlot(i).getHasStack()) {
                                            ItemStack stack = container.getSlot(i).getStack();
                                            Item item = stack.getItem();
                                            if (item instanceof ItemSword) {
                                                double damage = ItemUtil.getAttackBonus(stack);
                                                if (bestSword == -1 || damage > bestDamage) {
                                                    bestSword = i;
                                                    bestDamage = damage;
                                                }
                                            } else if (item instanceof ItemArmor) {
                                                int armorType = ((ItemArmor) item).armorType;
                                                double protectionLevel = ItemUtil.getArmorProtection(stack);
                                                if (bestArmorSlots[armorType] == -1 || protectionLevel > bestArmorProtection[armorType]) {
                                                    bestArmorSlots[armorType] = i;
                                                    bestArmorProtection[armorType] = protectionLevel;
                                                }
                                            } else if (item instanceof ItemPickaxe) {
                                                float efficiency = ItemUtil.getToolEfficiency(stack);
                                                if (bestPickaxeSlot == -1 || efficiency > bestPickaxeEfficiency) {
                                                    bestPickaxeSlot = i;
                                                    bestPickaxeEfficiency = efficiency;
                                                }
                                            } else if (item instanceof ItemSpade) {
                                                float efficiency = ItemUtil.getToolEfficiency(stack);
                                                if (bestShovelSlot == -1 || efficiency > bestShovelEfficiency) {
                                                    bestShovelSlot = i;
                                                    bestShovelEfficiency = efficiency;
                                                }
                                            } else if (item instanceof ItemAxe) {
                                                float efficiency = ItemUtil.getToolEfficiency(stack);
                                                if (bestAxeSlot == -1 || efficiency > bestAxeEfficiency) {
                                                    bestAxeSlot = i;
                                                    bestAxeEfficiency = efficiency;
                                                }
                                            } else if (item instanceof ItemSnowball || item instanceof ItemEgg) {
                                                hasThrowables = true;
                                            }
                                        }
                                    }
                                    int swordInInventorySlot = ItemUtil.findSwordInInventorySlot(0, true);
                                    double damage = swordInInventorySlot != -1 ? ItemUtil.getAttackBonus(mc.thePlayer.inventory.getStackInSlot(swordInInventorySlot)) : 0.0;
                                    if (bestDamage > damage) {
                                        this.shiftClick(container.windowId, bestSword);
                                        ChestData.getInstance().addClickAnimation(bestSword);
                                        return;
                                    }
                                    for (int i = 0; i < 4; i++) {
                                        int slot = ItemUtil.findArmorInventorySlot(i, true);
                                        double protectionLevel = slot != -1
                                                ? ItemUtil.getArmorProtection(mc.thePlayer.inventory.getStackInSlot(slot))
                                                : 0.0;
                                        if (bestArmorProtection[i] > protectionLevel) {
                                            this.shiftClick(container.windowId, bestArmorSlots[i]);
                                            ChestData.getInstance().addClickAnimation(bestArmorSlots[i]);
                                            return;
                                        }
                                    }
                                    int pickaxeSlot = ItemUtil.findInventorySlot("pickaxe", 0, true);
                                    float pickaxeEfficiency = pickaxeSlot != -1 ? ItemUtil.getToolEfficiency(mc.thePlayer.inventory.getStackInSlot(pickaxeSlot)) : 1.0F;
                                    if (bestPickaxeEfficiency > pickaxeEfficiency) {
                                        this.shiftClick(container.windowId, bestPickaxeSlot);
                                        ChestData.getInstance().addClickAnimation(bestPickaxeSlot);
                                        return;
                                    }
                                    int shovelSlot = ItemUtil.findInventorySlot("shovel", 0, true);
                                    float shovelEfficiency = shovelSlot != -1 ? ItemUtil.getToolEfficiency(mc.thePlayer.inventory.getStackInSlot(shovelSlot)) : 1.0F;
                                    if (bestShovelEfficiency > shovelEfficiency) {
                                        this.shiftClick(container.windowId, bestShovelSlot);
                                        ChestData.getInstance().addClickAnimation(bestShovelSlot);
                                        return;
                                    }
                                    int axeSlot = ItemUtil.findInventorySlot("axe", 0, true);
                                    float efficiency = axeSlot != -1 ? ItemUtil.getToolEfficiency(mc.thePlayer.inventory.getStackInSlot(axeSlot)) : 1.0F;
                                    if (bestAxeEfficiency > efficiency) {
                                        this.shiftClick(container.windowId, bestAxeSlot);
                                        ChestData.getInstance().addClickAnimation(bestAxeSlot);
                                        return;
                                    }
                                    if (hasThrowables) {
                                        for (int i = 0; i < inventory.getSizeInventory(); i++) {
                                            if (container.getSlot(i).getHasStack()) {
                                                ItemStack stack = container.getSlot(i).getStack();
                                                Item item = stack.getItem();
                                                if (item instanceof ItemSnowball || item instanceof ItemEgg) {
                                                    this.shiftClick(container.windowId, i);
                                                    ChestData.getInstance().addClickAnimation(i);
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                }
                                if (this.mode.getValue() == 1) {
                                    java.util.ArrayList<Integer> itemsToSteal = new java.util.ArrayList<>();
                                    if (this.skipTrash.getValue()) {
                                        int bestSword = -1;
                                        double bestDamage = 0.0;
                                        int[] bestArmorSlots = new int[]{-1, -1, -1, -1};
                                        double[] bestArmorProtection = new double[]{0.0, 0.0, 0.0, 0.0};
                                        int bestPickaxeSlot = -1;
                                        float bestPickaxeEfficiency = 1.0F;
                                        int bestShovelSlot = -1;
                                        float bestShovelEfficiency = 1.0F;
                                        int bestAxeSlot = -1;
                                        float bestAxeEfficiency = 1.0F;
                                        boolean hasThrowables = false;
                                        for (int i = 0; i < inventory.getSizeInventory(); i++) {
                                            if (container.getSlot(i).getHasStack()) {
                                                ItemStack stack = container.getSlot(i).getStack();
                                                Item item = stack.getItem();
                                                if (item instanceof ItemSword) {
                                                    double damage = ItemUtil.getAttackBonus(stack);
                                                    if (bestSword == -1 || damage > bestDamage) {
                                                        bestSword = i;
                                                        bestDamage = damage;
                                                    }
                                                } else if (item instanceof ItemArmor) {
                                                    int armorType = ((ItemArmor) item).armorType;
                                                    double protectionLevel = ItemUtil.getArmorProtection(stack);
                                                    if (bestArmorSlots[armorType] == -1 || protectionLevel > bestArmorProtection[armorType]) {
                                                        bestArmorSlots[armorType] = i;
                                                        bestArmorProtection[armorType] = protectionLevel;
                                                    }
                                                } else if (item instanceof ItemPickaxe) {
                                                    float efficiency = ItemUtil.getToolEfficiency(stack);
                                                    if (bestPickaxeSlot == -1 || efficiency > bestPickaxeEfficiency) {
                                                        bestPickaxeSlot = i;
                                                        bestPickaxeEfficiency = efficiency;
                                                    }
                                                } else if (item instanceof ItemSpade) {
                                                    float efficiency = ItemUtil.getToolEfficiency(stack);
                                                    if (bestShovelSlot == -1 || efficiency > bestShovelEfficiency) {
                                                        bestShovelSlot = i;
                                                        bestShovelEfficiency = efficiency;
                                                    }
                                                } else if (item instanceof ItemAxe) {
                                                    float efficiency = ItemUtil.getToolEfficiency(stack);
                                                    if (bestAxeSlot == -1 || efficiency > bestAxeEfficiency) {
                                                        bestAxeSlot = i;
                                                        bestAxeEfficiency = efficiency;
                                                    }
                                                } else if (item instanceof ItemSnowball || item instanceof ItemEgg) {
                                                    hasThrowables = true;
                                                }
                                            }
                                        }
                                        int swordInInventorySlot = ItemUtil.findSwordInInventorySlot(0, true);
                                        double damage = swordInInventorySlot != -1 ? ItemUtil.getAttackBonus(mc.thePlayer.inventory.getStackInSlot(swordInInventorySlot)) : 0.0;
                                        if (bestDamage > damage) {
                                            itemsToSteal.add(bestSword);
                                        }
                                        for (int i = 0; i < 4; i++) {
                                            int slot = ItemUtil.findArmorInventorySlot(i, true);
                                            double protectionLevel = slot != -1
                                                    ? ItemUtil.getArmorProtection(mc.thePlayer.inventory.getStackInSlot(slot))
                                                    : 0.0;
                                            if (bestArmorProtection[i] > protectionLevel) {
                                                itemsToSteal.add(bestArmorSlots[i]);
                                            }
                                        }
                                        int pickaxeSlot = ItemUtil.findInventorySlot("pickaxe", 0, true);
                                        float pickaxeEfficiency = pickaxeSlot != -1 ? ItemUtil.getToolEfficiency(mc.thePlayer.inventory.getStackInSlot(pickaxeSlot)) : 1.0F;
                                        if (bestPickaxeEfficiency > pickaxeEfficiency) {
                                            itemsToSteal.add(bestPickaxeSlot);
                                        }
                                        int shovelSlot = ItemUtil.findInventorySlot("shovel", 0, true);
                                        float shovelEfficiency = shovelSlot != -1 ? ItemUtil.getToolEfficiency(mc.thePlayer.inventory.getStackInSlot(shovelSlot)) : 1.0F;
                                        if (bestShovelEfficiency > shovelEfficiency) {
                                            itemsToSteal.add(bestShovelSlot);
                                        }
                                        int axeSlot = ItemUtil.findInventorySlot("axe", 0, true);
                                        float efficiency = axeSlot != -1 ? ItemUtil.getToolEfficiency(mc.thePlayer.inventory.getStackInSlot(axeSlot)) : 1.0F;
                                        if (bestAxeEfficiency > efficiency) {
                                            itemsToSteal.add(bestAxeSlot);
                                        }
                                        if (hasThrowables) {
                                            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                                                if (container.getSlot(i).getHasStack()) {
                                                    ItemStack stack = container.getSlot(i).getStack();
                                                    Item item = stack.getItem();
                                                    if (item instanceof ItemSnowball || item instanceof ItemEgg) {
                                                        itemsToSteal.add(i);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    for (int i = 0; i < inventory.getSizeInventory(); i++) {
                                        if (container.getSlot(i).getHasStack()) {
                                            ItemStack stack = container.getSlot(i).getStack();
                                            if (!(java.lang.Boolean) this.skipTrash.getValue() || !ItemUtil.isNotSpecialItem(stack)) {
                                                itemsToSteal.add(i);
                                            }
                                        }
                                    }
                                    for (int slot : itemsToSteal) {
                                        this.shiftClick(container.windowId, slot);
                                        ChestData.getInstance().addClickAnimation(slot);
                                    }
                                    if (this.autoClose.getValue()) {
                                        mc.thePlayer.closeScreen();
                                    }
                                } else {
                                    for (int i = 0; i < inventory.getSizeInventory(); i++) {
                                        if (container.getSlot(i).getHasStack()) {
                                            ItemStack stack = container.getSlot(i).getStack();
                                            if (!(java.lang.Boolean) this.skipTrash.getValue() || !ItemUtil.isNotSpecialItem(stack)) {
                                                this.shiftClick(container.windowId, i);
                                                ChestData.getInstance().addClickAnimation(i);
                                                return;
                                            }
                                        }
                                    }
                                    if (this.autoClose.getValue()) {
                                        mc.thePlayer.closeScreen();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    public void onEnabled() {
        markedChests.clear();
    }

    @Override
    public void onDisabled() {
        markedChests.clear();
        currentTarget = null;
        targetRotation = null;
        isRotating = false;
        lastAuraTime = 0;
    }
    
    private void handleAura(UpdateEvent event) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAuraTime < auraDelay.getValue()) {
            return;
        }

        BlockPos nearestChest = findNearestUnmarkedChest();
        if (nearestChest != null) {
            currentTarget = nearestChest;

            double deltaX = nearestChest.getX() + 0.5 - mc.thePlayer.posX;
            double deltaY = nearestChest.getY() + 0.5 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
            double deltaZ = nearestChest.getZ() + 0.5 - mc.thePlayer.posZ;
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
            
            if (distance <= auraRange.getValue()) {
                targetRotation = RotationUtil.getRotationsTo(deltaX, deltaY, deltaZ, event.getYaw(), event.getPitch());

                event.setRotation(targetRotation[0], targetRotation[1], 0);
                if (moveFix.getValue() == 2) {
                    event.setPervRotation(targetRotation[0], 0);
                }
                
                isRotating = true;

                MovingObjectPosition mop = RotationUtil.rayTrace(targetRotation[0], targetRotation[1], auraRange.getValue(), 1.0f);
                if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mop.getBlockPos().equals(nearestChest)) {
                    Vec3 hitVec = mop.hitVec;
                    EnumFacing facing = mop.sideHit;
                    
                    if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), nearestChest, facing, hitVec)) {
                        mc.thePlayer.swingItem();
                        lastAuraTime = currentTime;
                        markedChests.add(nearestChest);
                        currentTarget = null;
                        targetRotation = null;
                        isRotating = false;
                    }
                }
            }
        }
    }
    
    private BlockPos findNearestUnmarkedChest() {
        BlockPos nearestChest = null;
        double minDistance = Double.MAX_VALUE;

        int range = (int) Math.ceil(auraRange.getValue());
        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    Block block = mc.theWorld.getBlockState(pos).getBlock();

                    if (block instanceof BlockChest || block instanceof BlockEnderChest) {
                        if (!isMarkedChest(pos)) {
                            double distance = mc.thePlayer.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                            if (distance < minDistance && distance <= auraRange.getValue() * auraRange.getValue()) {
                                minDistance = distance;
                                nearestChest = pos;
                            }
                        }
                    }
                }
            }
        }
        
        return nearestChest;
    }
    
    private boolean isMarkedChest(BlockPos pos) {
        return markedChests.contains(pos);
    }
    
    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (this.isEnabled() && moveFix.getValue() == 1 && isRotating && RotationState.isActived() && RotationState.getPriority() == 0.0F && MoveUtil.isForwardPressed()) {
            MoveUtil.fixStrafe(RotationState.getSmoothedYaw());
        }
    }
    
  @EventTarget
    public void onWindowClick(WindowClickEvent event) {
        this.clickDelay = RandomUtils.nextInt(this.minDelay.getValue() + 1, this.maxDelay.getValue() + 2);
        if (event.getSlotId() < 54) {
            ChestData.getInstance().addClickAnimation(event.getSlotId());
            
            nightsky.module.modules.render.ChestView chestView = 
                (nightsky.module.modules.render.ChestView) nightsky.NightSky.moduleManager.getModule("ChestView");
            if (chestView != null && chestView.isEnabled()) {
                chestView.addClickAnimation(event.getSlotId());
            }
        }
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
                break;
        }
    }
}
