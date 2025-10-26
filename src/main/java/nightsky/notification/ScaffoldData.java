package nightsky.notification;

public class ScaffoldData {
    private static ScaffoldData instance;
    private int blocksLeft = 64;
    private float blocksPerSecond = 0.0f;
    private boolean isActive = false;
    
    private ScaffoldData() {}
    
    public static ScaffoldData getInstance() {
        if (instance == null) {
            instance = new ScaffoldData();
        }
        return instance;
    }
    
    public void setBlocksLeft(int blocks) {
        this.blocksLeft = Math.max(0, blocks);
        updateNotification();
    }
    
    public void setBlocksPerSecond(float bps) {
        this.blocksPerSecond = Math.max(0, bps);
        updateNotification();
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
        if (active) {
            updateNotification();
        }
    }
    
    private void updateNotification() {
        if (isActive) {
            String status = blocksLeft + " blocks left · " + String.format("%.1f", blocksPerSecond) + " block/s";
            NotificationManager.getInstance().addScaffoldingNotification("Scaffold Active", status);
        }
    }
    
    public int getBlocksLeft() {
        return blocksLeft;
    }
    
    public float getBlocksPerSecond() {
        return blocksPerSecond;
    }
    
    public float getProgress() {
        return Math.min(1.0f, blocksLeft / 64.0f);
    }
}
