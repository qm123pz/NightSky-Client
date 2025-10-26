package nightsky.notification;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import java.util.HashMap;
import java.util.Map;

public class ChestData {
    private static ChestData instance;
    private final Minecraft mc = Minecraft.getMinecraft();
    private IInventory chestInventory;
    private boolean isChestOpen = false;
    private int chestSize = 0;
    private Map<Integer, ClickAnimation> clickAnimations = new HashMap<>();
    
    public static ChestData getInstance() {
        if (instance == null) {
            instance = new ChestData();
        }
        return instance;
    }
    
    public void updateChestData() {
        if (mc.currentScreen instanceof GuiChest) {
            GuiChest guiChest = (GuiChest) mc.currentScreen;
            if (guiChest.inventorySlots instanceof ContainerChest) {
                ContainerChest container = (ContainerChest) guiChest.inventorySlots;
                chestInventory = container.getLowerChestInventory();
                chestSize = chestInventory.getSizeInventory();
                isChestOpen = true;
                return;
            }
        }
        isChestOpen = false;
        chestInventory = null;
        chestSize = 0;
    }
    
    public boolean isChestOpen() {
        return isChestOpen;
    }
    
    public IInventory getChestInventory() {
        return chestInventory;
    }
    
    public int getChestSize() {
        return chestSize;
    }
    
    public boolean isChestEmpty() {
        if (chestInventory == null) return true;
        for (int i = 0; i < chestSize; i++) {
            ItemStack stack = chestInventory.getStackInSlot(i);
            if (stack != null) {
                return false;
            }
        }
        return true;
    }
    
    public ItemStack getItemInSlot(int slot) {
        if (chestInventory == null || slot >= chestSize) return null;
        return chestInventory.getStackInSlot(slot);
    }
    
    public void addClickAnimation(int slot) {
        clickAnimations.put(slot, new ClickAnimation());
    }
    
    public Map<Integer, ClickAnimation> getClickAnimations() {
        return clickAnimations;
    }
    
    public void updateAnimations() {
        clickAnimations.entrySet().removeIf(entry -> {
            ClickAnimation animation = entry.getValue();
            animation.update();
            return animation.isFinished();
        });
    }

    public static class ClickAnimation {
        private long startTime;
        private static final long DURATION = 1000;
        
        public ClickAnimation() {
            this.startTime = System.currentTimeMillis();
        }
        
        public void update() {
        }
        
        public float getAlpha() {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= DURATION) return 0.0f;
            
            float progress = (float) elapsed / DURATION;
            if (progress < 0.5f) {
                return progress * 2.0f * 120.0f / 255.0f;
            } else {
                return (2.0f - progress * 2.0f) * 120.0f / 255.0f;
            }
        }
        
        public boolean isFinished() {
            return System.currentTimeMillis() - startTime >= DURATION;
        }
    }
}
