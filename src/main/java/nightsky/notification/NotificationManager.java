package nightsky.notification;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.ArrayList;

public class NotificationManager {
    private static NotificationManager instance;
    private final List<Notification> notifications = new CopyOnWriteArrayList<>();
    private final int maxNotifications = 5;
    
    private NotificationManager() {}
    
    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }
    
    public void addNotification(String title, String message, Notification.NotificationType type, long duration) {
        Notification notification = new Notification(title, message, type, duration);
        notifications.add(0, notification);
        
        if (notifications.size() > maxNotifications) {
            notifications.remove(notifications.size() - 1);
        }
    }
    
    public void addModuleNotification(String moduleName, boolean enabled) {
        for (int i = 0; i < notifications.size(); i++) {
            Notification existing = notifications.get(i);
            if (existing.getTitle().equals(moduleName) &&
                (existing.getType() == Notification.NotificationType.MODULE_ENABLED ||
                 existing.getType() == Notification.NotificationType.MODULE_DISABLED)) {
                notifications.remove(i);
                break;
            }
        }
        
        String title = moduleName;
        String message = enabled ? "Enabled" : "Disabled";
        Notification.NotificationType type = enabled ? 
            Notification.NotificationType.MODULE_ENABLED : 
            Notification.NotificationType.MODULE_DISABLED;
        
        addNotification(title, message, type, 800);
    }
    
    public void addInfoNotification(String title, String message) {
        addNotification(title, message, Notification.NotificationType.INFO, 3000);
    }
    
    public void addCommandResultNotification(String command, String result) {
        notifications.removeIf(notification -> 
            notification.getType() == Notification.NotificationType.COMMAND_RESULT);
        
        addNotification("Command Result", result, Notification.NotificationType.COMMAND_RESULT, 4000);
    }
    
    public void addWarningNotification(String title, String message) {
        addNotification(title, message, Notification.NotificationType.WARNING, 3000);
    }
    
    public void addErrorNotification(String title, String message) {
        addNotification(title, message, Notification.NotificationType.ERROR, 3000);
    }
    
    public void addScaffoldingNotification(String title, String message) {
        addNotification(title, message, Notification.NotificationType.SCAFFOLDING, 4000);
    }
    
    public List<Notification> getActiveNotifications() {
        List<Notification> activeNotifications = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        long slideOutDuration = 300;
        
        for (int i = notifications.size() - 1; i >= 0; i--) {
            Notification notification = notifications.get(i);
            long elapsed = currentTime - notification.getTimestamp();
            
            if (elapsed < notification.getDuration() - slideOutDuration) {
                activeNotifications.add(notification);
            } else if (elapsed < notification.getDuration()) {
                if (!notification.isSlidingOut()) {
                    notification.startSlideOut();
                }
                activeNotifications.add(notification);
            } else {
                notifications.remove(i);
            }
        }
        
        return getFilteredNotifications(activeNotifications);
    }
    
    private List<Notification> getFilteredNotifications(List<Notification> notifications) {
        ModuleStateManager moduleStateManager = ModuleStateManager.getInstance();
        List<Notification> result = new ArrayList<>();
        
        notifications.removeIf(n -> n.getType() == Notification.NotificationType.SCAFFOLDING || n.getType() == Notification.NotificationType.BED_NUKER);
        
        if (moduleStateManager.isScaffoldActive()) {
            ScaffoldData scaffoldData = ScaffoldData.getInstance();
            Notification scaffoldNotification = new Notification("Scaffold Active", 
                scaffoldData.getBlocksLeft() + " blocks left · " + 
                String.format("%.2f", scaffoldData.getBlocksPerSecond()) + " block/s", 
                Notification.NotificationType.SCAFFOLDING, 99999999);
            List<Notification> scaffoldOnly = new ArrayList<>();
            scaffoldOnly.add(scaffoldNotification);
            return scaffoldOnly;
        }
        
        if (moduleStateManager.isBedNukerActive()) {
            BedNukerData bedNukerData = BedNukerData.getInstance();
            if (bedNukerData.isBreaking()) {
                Notification bedNukerNotification = new Notification("BedNuker Active", 
                    "Breaking " + bedNukerData.getTargetBlockName() + " - " + String.format("%.0f%%", bedNukerData.getBreakProgress() * 100), 
                    Notification.NotificationType.BED_NUKER, 99999999);
                result.add(bedNukerNotification);
            }
        }
        
        for (Notification n : notifications) {
            if (n.getType() == Notification.NotificationType.INFO ||
                n.getType() == Notification.NotificationType.WARNING ||
                n.getType() == Notification.NotificationType.ERROR ||
                n.getType() == Notification.NotificationType.MODULE_ENABLED ||
                n.getType() == Notification.NotificationType.MODULE_DISABLED) {
                result.add(n);
            }
        }
        
        result.sort((n1, n2) -> {
            NotificationPriority p1 = NotificationPriority.fromNotificationType(n1.getType());
            NotificationPriority p2 = NotificationPriority.fromNotificationType(n2.getType());
            return Integer.compare(p2.getPriority(), p1.getPriority());
        });
        
        return result;
    }
    
    public Notification getCurrentNotification() {
        List<Notification> active = getActiveNotifications();
        return active.isEmpty() ? null : active.get(0);
    }
}
