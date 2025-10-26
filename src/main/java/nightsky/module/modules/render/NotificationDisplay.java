package nightsky.module.modules.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import nightsky.event.EventTarget;
import nightsky.events.Render2DEvent;
import nightsky.module.Module;
import nightsky.NightSky;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.IntValue;
import nightsky.font.FontTransformer;
import nightsky.font.CustomFontRenderer;
import nightsky.util.render.BlurUtil;
import nightsky.util.render.RenderUtil;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationDisplay extends Module {

    private final BooleanValue blur = new BooleanValue("Blur", true);
    private final IntValue blurStrength = new IntValue("BlurStrength", 12, 1, 70);
    private final BooleanValue shadow = new BooleanValue("Shadow", false);
    private final IntValue bgAlpha = new IntValue("BackgroundAlpha", 45, 1, 255);

    private final Minecraft mc = Minecraft.getMinecraft();

    private  final float animationSpeed = 10;

    private final float shadowRadius = 5f;
    private final float shadowSpread = 0.03f;
    private final float shadowAlpha = 0.03f;

    private final Map<ModuleToggleNotification, NotificationState> notificationStates = new HashMap<>();
    private final List<ModuleToggleNotification> displayQueue = new ArrayList<>();

    private static final float NOTIFICATION_WIDTH = 99f;
    private static final float NOTIFICATION_HEIGHT = 28f;
    private static final float NOTIFICATION_SPACING = 6f;
    private static final float NOTIFICATION_RADIUS = 6f;
    private static final float SLIDE_DURATION = 600f;
    private static final long NOTIFICATION_DURATION = 2000L;
    
    private static final ResourceLocation ENABLE_ICON = new ResourceLocation("nightsky/texture/toggle/enable.png");
    private static final ResourceLocation DISABLE_ICON = new ResourceLocation("nightsky/texture/toggle/disable.png");

    private static NotificationDisplay instance;

    public NotificationDisplay() {
        super("NotificationDisplay", false);
        instance = this;
    }

    public static NotificationDisplay getInstance() {
        return instance;
    }

    public void onModuleToggle(String moduleName, boolean enabled) {
        if (!this.isEnabled()) return;
        
        ModuleToggleNotification notification = new ModuleToggleNotification(moduleName, enabled);
        NotificationState state = new NotificationState();
        notificationStates.put(notification, state);
        displayQueue.add(0, notification);
        
        if (displayQueue.size() > 5) {
            ModuleToggleNotification oldest = displayQueue.get(displayQueue.size() - 1);
            NotificationState oldestState = notificationStates.get(oldest);
            if (oldestState != null) {
                oldestState.removing = true;
            }
        }
    }

    private static class ModuleToggleNotification {
        final String moduleName;
        final boolean enabled;
        final long timestamp;

        ModuleToggleNotification(String moduleName, boolean enabled) {
            this.moduleName = moduleName;
            this.enabled = enabled;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private static class NotificationState {
        float offsetX = NOTIFICATION_WIDTH + 20;
        float targetOffsetX = 0;
        float alpha = 0f;
        float targetAlpha = 1f;
        float scale = 1.1f;
        float targetScale = 1.1f;
        long startTime = System.currentTimeMillis();
        int position = 0;
        boolean removing = false;
        boolean justCreated = true;
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        updateNotificationStates();

        ScaledResolution sr = new ScaledResolution(mc);
        float screenWidth = sr.getScaledWidth();
        float screenHeight = sr.getScaledHeight();

        float rightMargin = 15f;
        float bottomMargin = 15f;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        int position = 0;
        for (ModuleToggleNotification notification : displayQueue) {
            NotificationState state = notificationStates.get(notification);
            if (state == null) continue;

            state.position = position;

            float yOffset = position * (NOTIFICATION_HEIGHT + NOTIFICATION_SPACING);
            float x = screenWidth - NOTIFICATION_WIDTH - rightMargin + state.offsetX;
            float y = screenHeight - NOTIFICATION_HEIGHT - bottomMargin - yOffset;

            updateAnimation(state);

            if (state.alpha > 0.02f) {
                GlStateManager.pushMatrix();
                
                float centerX = x + NOTIFICATION_WIDTH / 2;
                float centerY = y + NOTIFICATION_HEIGHT / 2;
                GlStateManager.translate(centerX, centerY, 0);
                GlStateManager.scale(state.scale, state.scale, 1);
                GlStateManager.translate(-centerX, -centerY, 0);
                
                float renderAlpha = Math.max(0f, Math.min(1f, state.alpha));
                drawNotification(notification, x, y, renderAlpha);
                
                GlStateManager.popMatrix();
                position++;
            }
        }

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        displayQueue.removeIf(notification -> {
            NotificationState state = notificationStates.get(notification);
            return state == null || (state.removing && state.alpha < 0.01f);
        });
    }

    private void updateNotificationStates() {
        long currentTime = System.currentTimeMillis();
        
        List<ModuleToggleNotification> toRemove = new ArrayList<>();
        
        for (ModuleToggleNotification notification : displayQueue) {
            NotificationState state = notificationStates.get(notification);
            if (state != null) {
                long totalElapsed = currentTime - notification.timestamp;
                
                if (!state.removing) {
                    if (totalElapsed > NOTIFICATION_DURATION) {
                        state.removing = true;
                        state.targetAlpha = 0f;
                        state.targetOffsetX = NOTIFICATION_WIDTH + 30;
                        state.startTime = currentTime;
                    }
                } else {
                    long removeElapsed = currentTime - state.startTime;
                    
                    if (state.alpha <= 0.02f && state.offsetX >= NOTIFICATION_WIDTH + 10) {
                        toRemove.add(notification);
                    } else if (removeElapsed > 1000) {
                        toRemove.add(notification);
                    }
                }
                
                if (totalElapsed > NOTIFICATION_DURATION + 1500) {
                    toRemove.add(notification);
                }
            } else {
                toRemove.add(notification);
            }
        }

        for (ModuleToggleNotification notification : toRemove) {
            displayQueue.remove(notification);
            notificationStates.remove(notification);
        }
    }

    private void updateAnimation(NotificationState state) {
        long elapsed = System.currentTimeMillis() - state.startTime;
        float speed = animationSpeed * 0.08f;
        
        if (state.justCreated && elapsed > 50) {
            state.justCreated = false;
        }

        float offsetDiff = state.targetOffsetX - state.offsetX;
        if (state.removing) {
            long removeElapsed = elapsed;
            float removeProgress = Math.min(1.0f, removeElapsed / 400f);
            float easing = easeInCubic(removeProgress);
            state.offsetX = easing * (NOTIFICATION_WIDTH + 30);
            
            if (removeProgress > 0.6f) {
                float fadeProgress = (removeProgress - 0.6f) / 0.4f;
                state.alpha = 1.0f - fadeProgress;
            }
        } else {
            float easing = easeOutCubic(Math.min(1.0f, elapsed / SLIDE_DURATION));
            state.offsetX = (NOTIFICATION_WIDTH + 20) * (1 - easing);
        }
        if (Math.abs(offsetDiff) < 0.3f && !state.removing) {
            state.offsetX = state.targetOffsetX;
        }

        if (!state.removing) {
            float alphaDiff = state.targetAlpha - state.alpha;
            if (Math.abs(alphaDiff) < 0.01f) {
                state.alpha = state.targetAlpha;
            } else {
                state.alpha += alphaDiff * speed * 2.0f;
                state.alpha = Math.max(0f, Math.min(1f, state.alpha));
            }
        }
        
        float scaleDiff = state.targetScale - state.scale;
        if (!state.removing && state.justCreated) {
            float scaleEasing = easeOutBack(Math.min(1.0f, elapsed / (SLIDE_DURATION * 0.7f)));
            state.scale = 0.935f + (state.targetScale - 0.935f) * scaleEasing;
        } else if (state.removing) {
            long removeElapsed = elapsed;
            float removeProgress = Math.min(1.0f, removeElapsed / 400f);
            state.scale = 1.1f - removeProgress * 0.165f;
        } else {
            state.scale += scaleDiff * speed * 1.2f;
        }
        if (Math.abs(scaleDiff) < 0.005f && !state.removing) {
            state.scale = state.targetScale;
        }
    }
    
    private float easeInCubic(float t) {
        return t * t * t;
    }
    
    private float easeOutCubic(float t) {
        return 1 - (float)Math.pow(1 - t, 3);
    }
    
    private float easeOutBack(float t) {
        float c1 = 1.40158f;
        float c3 = c1 + 1;
        return 1 + c3 * (float)Math.pow(t - 1, 3) + c1 * (float)Math.pow(t - 1, 2);
    }

    private void drawNotification(ModuleToggleNotification notification, float x, float y, float alpha) {
        int bgAlphaValue = (int)(bgAlpha.getValue() * alpha);
        int textAlphaValue = (int)(255 * alpha);

        if (shadow.getValue()) {
            drawDropShadow(x, y, NOTIFICATION_WIDTH, NOTIFICATION_HEIGHT, NOTIFICATION_RADIUS, alpha);
        }

        if (blur.getValue()) {
            BlurUtil.blurAreaRounded(x, y, x + NOTIFICATION_WIDTH, y + NOTIFICATION_HEIGHT,
                    NOTIFICATION_RADIUS, blurStrength.getValue().floatValue() * alpha);
        }

        Color bgColor = new Color(0, 0, 0, bgAlphaValue);
        RenderUtil.drawRoundedRect(x, y, NOTIFICATION_WIDTH, NOTIFICATION_HEIGHT, NOTIFICATION_RADIUS, bgColor);

        drawNotificationContent(notification, x, y, textAlphaValue);
    }

    private void drawNotificationContent(ModuleToggleNotification notification, float x, float y, int alpha) {
        FontTransformer transformer = FontTransformer.getInstance();
        Font titleFont = transformer.getFont("OpenSansSemiBold", 32);
        Font contentFont = transformer.getFont("OpenSansSemiBold", 26);

        float padding = 6f;
        float iconSize = 12f;
        float iconPadding = 5f;

        float iconX = x + padding;
        float iconY = y + (NOTIFICATION_HEIGHT - iconSize) / 2;
        drawToggleIcon(iconX, iconY, iconSize, notification.enabled, alpha);

        float textX = iconX + iconSize + iconPadding;
        float titleY = y + padding + 2;

        String title = notification.moduleName;
        int titleColor = (alpha << 24) | 0xFFFFFF;
        CustomFontRenderer.drawStringWithShadow(title, textX, titleY, titleColor, titleFont);

        String message = notification.enabled ? "Enabled" : "Disabled";
        float messageY = titleY + CustomFontRenderer.getFontHeight(titleFont) + 2;
        int messageColor = (alpha << 24) | (notification.enabled ? 0x00FF00 : 0xFF0000);

        CustomFontRenderer.drawStringWithShadow(message, textX, messageY, messageColor, contentFont);

        float progressBarHeight = 1.5f;
        float progressBarY = y + NOTIFICATION_HEIGHT - progressBarHeight - 0.3f;
        float progressBarX = x + 1f;
        float progressBarWidth = NOTIFICATION_WIDTH - 2;

        long elapsed = System.currentTimeMillis() - notification.timestamp;
        float progress = 1.0f - Math.min(1.0f, (float)elapsed / (float)NOTIFICATION_DURATION);

        if (progress > 0) {
            float filledWidth = progressBarWidth * progress;
            
            Interface interfaceModule = (Interface) NightSky.moduleManager.getModule("Interface");
            int baseColor = interfaceModule != null ? interfaceModule.color(0) : 0x8080FF;
            
            int progressAlpha = (int)(alpha * 0.6f);
            Color progressColor = new Color(
                    (baseColor >> 16) & 0xFF,
                    (baseColor >> 8) & 0xFF,
                    baseColor & 0xFF,
                    progressAlpha
            );

            RenderUtil.drawRoundedRect(progressBarX, progressBarY, filledWidth, progressBarHeight, 1f, progressColor);
        }
    }
    
    private void drawToggleIcon(float x, float y, float size, boolean enabled, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);
        
        mc.getTextureManager().bindTexture(enabled ? ENABLE_ICON : DISABLE_ICON);
        
        net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture(
            (int)x, (int)y, 0, 0, (int)size, (int)size, size, size
        );
        
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }


    private void drawDropShadow(float x, float y, float width, float height, float cornerRadius, float alphaMultiplier) {
        float radius = shadowRadius;
        float spread = shadowSpread;
        float baseAlpha = shadowAlpha * alphaMultiplier;

        if (radius <= 0 || baseAlpha <= 0) return;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();

        float spreadDistance = radius * spread;
        float blurDistance = radius - spreadDistance;

        int samples = Math.max(10, Math.min(24, (int)(radius * 1.8f)));

        for (int i = 0; i < samples; i++) {
            float t = (float)i / (float)(samples - 1);

            float currentSpread = t * spreadDistance;
            float currentBlur = t * blurDistance;
            float totalOffset = currentSpread + currentBlur;

            float alpha;
            if (t <= spread) {
                alpha = 0.6f * baseAlpha;
            } else {
                float blurProgress = (t - spread) / (1.0f - spread);
                alpha = 0.6f * baseAlpha * (1.0f - blurProgress * blurProgress);
            }

            float shadowX = x - totalOffset;
            float shadowY = y - totalOffset;
            float shadowWidth = width + totalOffset * 2.0f;
            float shadowHeight = height + totalOffset * 2.0f;

            drawGaussianBlurredRect(shadowX, shadowY, shadowWidth, shadowHeight, cornerRadius, alpha);
        }

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawGaussianBlurredRect(float x, float y, float width, float height, float cornerRadius, float alpha) {
        int blurLayers = 3;
        float blurSpread = 2.5f;

        for (int layer = 0; layer < blurLayers; layer++) {
            float layerProgress = (float)layer / (float)blurLayers;
            float layerAlpha = alpha * (1.0f - layerProgress * 0.35f);
            float layerExpand = layerProgress * blurSpread;

            float layerX = x - layerExpand;
            float layerY = y - layerExpand;
            float layerWidth = width + layerExpand * 2.0f;
            float layerHeight = height + layerExpand * 2.0f;

            RenderUtil.drawRoundedRect(
                    layerX, layerY, layerWidth, layerHeight,
                    cornerRadius,
                    new Color(0, 0, 0, (int)(layerAlpha * 255))
            );
        }
    }
}
