package nightsky.notification;

public enum NotificationPriority {
    BED_NUKER(5),
    SCAFFOLDING(4),
    ERROR(3),
    WARNING(2),
    INFO(1),
    MODULE(0);
    
    private final int priority;
    
    NotificationPriority(int priority) {
        this.priority = priority;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public static NotificationPriority fromNotificationType(Notification.NotificationType type) {
        switch (type) {
            case BED_NUKER:
                return BED_NUKER;
            case SCAFFOLDING:
                return SCAFFOLDING;
            case ERROR:
                return ERROR;
            case WARNING:
                return WARNING;
            case INFO:
                return INFO;
            case MODULE_ENABLED:
            case MODULE_DISABLED:
                return MODULE;
            default:
                return INFO;
        }
    }
}