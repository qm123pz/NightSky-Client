package nightsky.notification;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

public class BedNukerData {
    private static BedNukerData instance;
    
    private boolean isBreaking = false;
    private float breakProgress = 0.0f;
    private BlockPos targetBlock = null;
    private ItemStack targetBlockItem = null;
    private String targetBlockName = "";
    
    private BedNukerData() {}
    
    public static BedNukerData getInstance() {
        if (instance == null) {
            instance = new BedNukerData();
        }
        return instance;
    }
    
    public void setBreaking(boolean breaking) {
        this.isBreaking = breaking;
    }
    
    public boolean isBreaking() {
        return isBreaking;
    }
    
    public void setBreakProgress(float progress) {
        this.breakProgress = Math.max(0.0f, Math.min(1.0f, progress));
    }
    
    public float getBreakProgress() {
        return breakProgress;
    }
    
    public void setTargetBlock(BlockPos blockPos, Block block) {
        this.targetBlock = blockPos;
        if (block != null) {
            this.targetBlockItem = new ItemStack(block);
            this.targetBlockName = block.getLocalizedName();
        } else {
            this.targetBlockItem = null;
            this.targetBlockName = "";
        }
    }
    
    public ItemStack getTargetBlockItem() {
        return targetBlockItem;
    }
    
    public String getTargetBlockName() {
        return targetBlockName;
    }
    
    public void reset() {
        this.isBreaking = false;
        this.breakProgress = 0.0f;
        this.targetBlock = null;
        this.targetBlockItem = null;
        this.targetBlockName = "";
    }
}
