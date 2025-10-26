package nightsky.notification;
//why。do。i。do。it。🤔
public class Notification {
    private final String title;
    private final String message;
    private final NotificationType type;
    private final long createTime;
    private final long duration;
    private boolean slidingOut = false;
    private long slideOutStartTime = 0;
    
    public Notification(String title, String message, NotificationType type, long duration) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.createTime = System.currentTimeMillis();
        this.duration = duration;
    }
    
    public void startSlideOut() {
        if (!slidingOut) {
            slidingOut = true;
            slideOutStartTime = System.currentTimeMillis();
        }
    }
    
    public boolean isSlidingOut() {
        return slidingOut;
    }
    
    public long getSlideOutStartTime() {
        return slideOutStartTime;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public NotificationType getType() {
        return type;
    }
    
    public long getDuration() {
        return duration;
    }
    
    public long getTimestamp() {
        return createTime;
    }
    
    public enum NotificationType {
        INFO,
        WARNING,
        ERROR,
        MODULE_ENABLED,
        MODULE_DISABLED,
        SCAFFOLDING,
        BED_NUKER,
        COMMAND_RESULT
    }
}
