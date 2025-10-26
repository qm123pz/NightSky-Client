package nightsky.module;

import nightsky.NightSky;
import nightsky.module.modules.render.NotificationDisplay;
import nightsky.notification.NotificationManager;
import nightsky.util.KeyBindUtil;
import nightsky.util.render.animations.Translate;
import nightsky.util.render.animations.advanced.Animation;
import nightsky.util.render.animations.advanced.impl.DecelerateAnimation;

public abstract class Module {
    protected final String name;
    protected final boolean defaultEnabled;
    protected final int defaultKey;
    protected final boolean defaultHidden;
    protected boolean enabled;
    protected int key;
    protected boolean hidden;
    
    private final Translate translate = new Translate(0, 0);
    private final Animation animation = new DecelerateAnimation(300, 1.0);

    public Module(String name, boolean enabled) {
        this(name, enabled, false);
    }

    public Module(String name, boolean enabled, boolean hidden) {
        this.name = name;
        this.enabled = this.defaultEnabled = enabled;
        this.key = this.defaultKey = 0;
        this.hidden = this.defaultHidden = hidden;
    }

    protected Module(String name, boolean defaultEnabled, int defaultKey, boolean defaultHidden) {
        this.name = name;
        this.defaultEnabled = defaultEnabled;
        this.defaultKey = defaultKey;
        this.defaultHidden = defaultHidden;
    }

    public String getName() {
        return this.name;
    }

    public String formatModule() {
        return String.format(
                "%s%s &r(%s&r)",
                this.key == 0 ? "" : String.format("&l[%s] &r", KeyBindUtil.getKeyName(this.key)),
                this.name,
                this.enabled
        );
    }

    public String[] getSuffix() {
        return new String[0];
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                this.onEnabled();
            } else {
                this.onDisabled();
            }
        }
    }

    public boolean toggle() {
        boolean enabled = !this.enabled;
        this.setEnabled(enabled);
        if (this.enabled == enabled) {
            NightSky.moduleManager.playSound(enabled);
            
            NotificationManager.getInstance().addModuleNotification(this.name, enabled);
            
            NotificationDisplay notifDisplay =
                NotificationDisplay.getInstance();
            
            if (notifDisplay != null) {
                notifDisplay.onModuleToggle(this.name, enabled);
            }
            return true;
        } else {
            return false;
        }
    }

    public int getKey() {
        return this.key;
    }

    public void setKey(int integer) {
        this.key = integer;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean boolean1) {
        this.hidden = boolean1;
    }

    public void onEnabled() {
    }

    public void onDisabled() {
    }

    public void verifyValue(String string) {
    }
    
    public Translate getTranslate() {
        return translate;
    }
    
    public Animation getAnimation() {
        return animation;
    }
    
    public String getTag() {
        String[] suffix = getSuffix();
        if (suffix.length > 0) {
            return " " + String.join(" ", suffix);
        }
        return "";
    }
    
    public ModuleCategory getCategory() {
        String packageName = this.getClass().getPackage().getName();
        if (packageName.contains("combat")) {
            return ModuleCategory.COMBAT;
        } else if (packageName.contains("movement")) {
            return ModuleCategory.MOVEMENT;
        } else if (packageName.contains("player")) {
            return ModuleCategory.PLAYER;
        } else if (packageName.contains("render")) {
            return ModuleCategory.RENDER;
        } else if (packageName.contains("misc")) {
            return ModuleCategory.MISC;
        } else {
            return ModuleCategory.MISC;
        }
    }
}