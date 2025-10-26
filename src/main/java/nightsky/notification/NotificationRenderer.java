package nightsky.notification;

import nightsky.font.FontTransformer;
import nightsky.font.CustomFontRenderer;
import nightsky.util.render.RenderUtil;
import java.awt.Color;
import java.awt.Font;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class NotificationRenderer {

    private static final Map<String, Float> toggleAnimations = new HashMap<>();
    private static final Map<String, Float> toggleBgAnimations = new HashMap<>();
    private static final Map<String, Float> toggleSizeAnimations = new HashMap<>();
    private static final Map<String, Float> bedNukerProgressAnimations = new HashMap<>();
    private static final Map<String, Float> scaffoldProgressAnimations = new HashMap<>();
    private static final float ANIMATION_SPEED = 0.15f;
    
    public static float toggleBgRadius = 17f;
    public static float toggleButtonRadius = 13f;
    
    public static void drawMultipleNotifications(List<Notification> notifications, float x, float y, float width, float height, float scale) {
        if (notifications.isEmpty()) return;
        
        float totalHeight = 0;
        for (Notification notification : notifications) {
            totalHeight += calculateItemHeight(notification.getType()) * scale;
        }
        totalHeight = Math.min(totalHeight, height - 20 * scale);
        
        float startY = y + (height - totalHeight) / 2;
        float currentY = startY;
        
        boolean anySliding = false;
        for (Notification notification : notifications) {
            if (notification.isSlidingOut()) {
                anySliding = true;
                break;
            }
        }
        
        for (Notification notification : notifications) {
            float itemHeight = calculateItemHeight(notification.getType()) * scale;
            float slideOffset = 0;
            float alpha = 1.0f;
            
            if (anySliding) {
                long elapsed = System.currentTimeMillis() - notification.getSlideOutStartTime();
                float progress = Math.min(1.0f, elapsed / 300.0f);
                float eased = easeOutCubic(progress);
                slideOffset = width * eased;
                alpha = 1.0f - eased;
            }
            
            drawSingleNotificationScaled(notification, x + slideOffset, currentY, width, itemHeight, alpha, scale);
            currentY += itemHeight;
        }
    }
    
    private static float easeOutCubic(float t) {
        return 1 - (float)Math.pow(1 - t, 3);
    }
    
    private static void drawSingleNotificationScaled(Notification notification, float x, float y, float width, float height, float alpha, float scale) {
        switch (notification.getType()) {
            case SCAFFOLDING:
                drawScaffoldingNotificationScaled(notification, x, y, width, height, alpha, scale);
                break;
            case BED_NUKER:
                drawBedNukerNotificationScaled(notification, x, y, width, height, alpha, scale);
                break;
            case MODULE_ENABLED:
            case MODULE_DISABLED:
                drawModuleNotificationScaled(notification, x, y, width, height, alpha, scale);
                break;
            default:
                drawRegularNotificationScaled(notification, x, y, width, height, alpha, scale);
                break;
        }
    }
    
    private static void drawScaffoldingNotificationScaled(Notification notification, float x, float y, float width, float height, float alpha, float scale) {
        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.translate(x, y, 0);
        net.minecraft.client.renderer.GlStateManager.scale(scale, scale, 1.0f);
        
        float padding = 4;
        
        drawCurrentBlockIcon(padding, height / scale / 2 - 6, 12, 12, alpha);
        
        FontTransformer transformer = FontTransformer.getInstance();
        Font notifFont = transformer.getFont("OpenSansSemiBold", 35);
        
        String title = "Scaffold Toggled";
        Color titleColor = new Color(255, 105, 180, (int)(255 * alpha));
        CustomFontRenderer.drawStringWithShadow(title, padding + 18, 4, titleColor.getRGB(), notifFont);
        
        ScaffoldData scaffoldData = ScaffoldData.getInstance();
        String status = scaffoldData.getBlocksLeft() + " blocks left · " + 
                       String.format("%.2f", scaffoldData.getBlocksPerSecond()) + " block/s";
        Color statusColor = new Color(255, 255, 255, (int)(255 * alpha));
        CustomFontRenderer.drawStringWithShadow(status, padding + 18, 4 + CustomFontRenderer.getFontHeight(notifFont) + 2, statusColor.getRGB(), notifFont);
        
        float progressBarWidth = width / scale - padding * 2;
        drawAnimatedProgressBar(padding, height / scale - 6, progressBarWidth, 3, scaffoldData.getProgress(), alpha, progressBarWidth);
        
        net.minecraft.client.renderer.GlStateManager.popMatrix();
    }
    
    private static void drawModuleNotificationScaled(Notification notification, float x, float y, float width, float height, float alpha, float scale) {
        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.translate(x, y, 0);
        net.minecraft.client.renderer.GlStateManager.scale(scale, scale, 1.0f);
        
        String moduleName = notification.getTitle();
        boolean enabled = notification.getType() == Notification.NotificationType.MODULE_ENABLED;
        
        float padding = 4;
        float switchWidth = 32;
        float switchHeight = 16;
        float switchX = padding;
        float switchY = height / scale / 2 - switchHeight / 2;
        
        drawModernToggleButton(switchX, switchY, height / scale, enabled, alpha, moduleName);
        
        FontTransformer transformer = FontTransformer.getInstance();
        Font notifFont = transformer.getFont("OpenSansSemiBold", 37);
        
        String title = "ModuleToggled";
        Color titleColor = new Color(255, 255, 255, (int)(255 * alpha));
        CustomFontRenderer.drawStringWithShadow(title, switchX + switchWidth + 8, 4, titleColor.getRGB(), notifFont);
        
        String moduleNameText = moduleName;
        String hasBeenText = " has been ";
        String statusText = enabled ? "enabled" : "disabled";
        String exclamation = "!";
        
        Color moduleNameColor = new Color(255, 255, 255, (int)(255 * alpha));
        Color hasBeenColor = new Color(160, 160, 160, (int)(255 * alpha));
        Color statusColor = enabled ? 
            new Color(76, 175, 80, (int)(255 * alpha)) : 
            new Color(244, 67, 54, (int)(255 * alpha));
        Color exclamationColor = new Color(255, 255, 255, (int)(255 * alpha));
        
        float statusY = 4 + CustomFontRenderer.getFontHeight(notifFont) + 2;
        float currentX = switchX + switchWidth + 8;
        
        CustomFontRenderer.drawStringWithShadow(moduleNameText, currentX, statusY, moduleNameColor.getRGB(), notifFont);
        currentX += CustomFontRenderer.getStringWidth(moduleNameText, notifFont);
        
        CustomFontRenderer.drawStringWithShadow(hasBeenText, currentX, statusY, hasBeenColor.getRGB(), notifFont);
        currentX += CustomFontRenderer.getStringWidth(hasBeenText, notifFont);
        
        CustomFontRenderer.drawStringWithShadow(statusText, currentX, statusY, statusColor.getRGB(), notifFont);
        currentX += CustomFontRenderer.getStringWidth(statusText, notifFont);
        
        CustomFontRenderer.drawStringWithShadow(exclamation, currentX, statusY, exclamationColor.getRGB(), notifFont);
        
        net.minecraft.client.renderer.GlStateManager.popMatrix();
    }
    
    private static void drawBedNukerNotificationScaled(Notification notification, float x, float y, float width, float height, float alpha, float scale) {
        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.translate(x, y, 0);
        net.minecraft.client.renderer.GlStateManager.scale(scale, scale, 1.0f);
        
        float padding = 4;
        
        BedNukerData bedNukerData = BedNukerData.getInstance();
        
        drawBedNukerBlockIcon(padding, height / scale / 2 - 6, 12, 12, alpha);
        
        FontTransformer transformer = FontTransformer.getInstance();
        Font notifFont = transformer.getFont("OpenSansSemiBold", 35);
        
        String breakingText = "Breaking " + bedNukerData.getTargetBlockName();
        Color breakingColor = new Color(255, 255, 255, (int)(255 * alpha));
        CustomFontRenderer.drawStringWithShadow(breakingText, padding + 18, 4, breakingColor.getRGB(), notifFont);
        
        String progressText = "Break Progress: " + String.format("%.0f%%", bedNukerData.getBreakProgress() * 100);
        Color progressColor = new Color(255, 255, 255, (int)(255 * alpha));
        CustomFontRenderer.drawStringWithShadow(progressText, padding + 18, 4 + CustomFontRenderer.getFontHeight(notifFont) + 2, progressColor.getRGB(), notifFont);
        
        drawBedNukerProgressBar(padding, height / scale - 6, width / scale - padding * 2, 3, bedNukerData.getBreakProgress(), alpha);
        
        net.minecraft.client.renderer.GlStateManager.popMatrix();
    }
    
    private static void drawRegularNotificationScaled(Notification notification, float x, float y, float width, float height, float alpha, float scale) {
        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.translate(x, y, 0);
        net.minecraft.client.renderer.GlStateManager.scale(scale, scale, 1.0f);
        
        float padding = 4;
        
        FontTransformer transformer = FontTransformer.getInstance();
        Font notifFont = transformer.getFont("OpenSansSemiBold", 35);
        
        Color textColor = getColorFromNotification(notification);
        Color finalTextColor = new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), (int)(255 * alpha));
        Color typeColor = new Color(160, 160, 160, (int)(255 * alpha));
        
        String typeText = getTypeDisplayName(notification.getType());
        CustomFontRenderer.drawStringWithShadow(typeText, padding, 4, typeColor.getRGB(), notifFont);
        
        String displayText = notification.getTitle();
        if (!notification.getMessage().isEmpty()) {
            displayText += " " + notification.getMessage();
        }
        
        CustomFontRenderer.drawStringWithShadow(displayText, padding, 4 + CustomFontRenderer.getFontHeight(notifFont) + 2, finalTextColor.getRGB(), notifFont);
        
        net.minecraft.client.renderer.GlStateManager.popMatrix();
    }
    
    private static void updateToggleAnimations(String moduleKey, boolean enabled) {
        float targetSwitch = enabled ? 1.0f : 0.0f;
        float targetBg = enabled ? 1.0f : 0.0f;
        float targetSize = enabled ? 1.0f : 0.7f;
        
        float currentSwitch = toggleAnimations.getOrDefault(moduleKey, targetSwitch);
        float currentBg = toggleBgAnimations.getOrDefault(moduleKey, targetBg);
        float currentSize = toggleSizeAnimations.getOrDefault(moduleKey, targetSize);
        
        if (Math.abs(currentSwitch - targetSwitch) > 0.01f) {
            currentSwitch += (targetSwitch - currentSwitch) * ANIMATION_SPEED;
            toggleAnimations.put(moduleKey, currentSwitch);
        } else {
            toggleAnimations.put(moduleKey, targetSwitch);
        }
        
        if (Math.abs(currentBg - targetBg) > 0.01f) {
            currentBg += (targetBg - currentBg) * ANIMATION_SPEED;
            toggleBgAnimations.put(moduleKey, currentBg);
        } else {
            toggleBgAnimations.put(moduleKey, targetBg);
        }
        
        if (Math.abs(currentSize - targetSize) > 0.01f) {
            currentSize += (targetSize - currentSize) * ANIMATION_SPEED;
            toggleSizeAnimations.put(moduleKey, currentSize);
        } else {
            toggleSizeAnimations.put(moduleKey, targetSize);
        }
    }
    
    private static void drawBlockIcon(float x, float y, float width, float height, float alpha) {
        Color blockColor = new Color(139, 69, 19, (int)(255 * alpha));
        RenderUtil.drawRect(x, y, width, height, blockColor.getRGB());
        
        Color borderColor = new Color(160, 82, 45, (int)(255 * alpha));
        RenderUtil.drawRect(x, y, width, 2, borderColor.getRGB());
        RenderUtil.drawRect(x, y, 2, height, borderColor.getRGB());
        RenderUtil.drawRect(x + width - 2, y, 2, height, borderColor.getRGB());
        RenderUtil.drawRect(x, y + height - 2, width, 2, borderColor.getRGB());
    }
    
    private static void drawCurrentBlockIcon(float x, float y, int width, int height, float alpha) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        
        try {
            nightsky.module.modules.player.Scaffold scaffoldModule = 
                (nightsky.module.modules.player.Scaffold) nightsky.NightSky.moduleManager.getModule("Scaffold");
            
            if (scaffoldModule != null && scaffoldModule.isEnabled()) {
                net.minecraft.item.ItemStack scaffoldBlock = scaffoldModule.getCurrentScaffoldBlock();
                if (scaffoldBlock != null && scaffoldBlock.getItem() instanceof net.minecraft.item.ItemBlock) {
                    try {
                        net.minecraft.client.renderer.GlStateManager.pushMatrix();
                        net.minecraft.client.renderer.GlStateManager.enableRescaleNormal();
                        net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
                        net.minecraft.client.renderer.GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                        
                        mc.getRenderItem().renderItemAndEffectIntoGUI(scaffoldBlock, (int)x, (int)y);
                        
                        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
                        net.minecraft.client.renderer.GlStateManager.disableRescaleNormal();
                        net.minecraft.client.renderer.GlStateManager.popMatrix();
                        return;
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
        }
        
        if (mc.thePlayer != null && mc.thePlayer.getHeldItem() != null) {
            net.minecraft.item.ItemStack heldItem = mc.thePlayer.getHeldItem();
            if (heldItem.getItem() instanceof net.minecraft.item.ItemBlock) {
                try {
                    net.minecraft.client.renderer.GlStateManager.pushMatrix();
                    net.minecraft.client.renderer.GlStateManager.enableRescaleNormal();
                    net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
                    net.minecraft.client.renderer.GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                    
                    mc.getRenderItem().renderItemAndEffectIntoGUI(heldItem, (int)x, (int)y);
                    
                    net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
                    net.minecraft.client.renderer.GlStateManager.disableRescaleNormal();
                    net.minecraft.client.renderer.GlStateManager.popMatrix();
                } catch (Exception e) {
                    drawBlockIcon(x, y, width, height, alpha);
                }
                return;
            }
        }
        drawBlockIcon(x, y, width, height, alpha);
    }
    
    private static void drawBedNukerBlockIcon(float x, float y, int width, int height, float alpha) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        
        BedNukerData bedNukerData = BedNukerData.getInstance();
        net.minecraft.item.ItemStack targetBlockItem = bedNukerData.getTargetBlockItem();
        
        if (targetBlockItem != null) {
            try {
                net.minecraft.client.renderer.GlStateManager.pushMatrix();
                net.minecraft.client.renderer.GlStateManager.enableRescaleNormal();
                net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
                net.minecraft.client.renderer.GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);
                
                mc.getRenderItem().renderItemAndEffectIntoGUI(targetBlockItem, (int)x, (int)y);
                
                net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
                net.minecraft.client.renderer.GlStateManager.disableRescaleNormal();
                net.minecraft.client.renderer.GlStateManager.popMatrix();
                return;
            } catch (Exception e) {
            }
        }
        
        Color bedColor = new Color(200, 50, 50, (int)(255 * alpha));
        RenderUtil.drawRect(x, y, width, height, bedColor.getRGB());
        
        Color borderColor = new Color(150, 30, 30, (int)(255 * alpha));
        RenderUtil.drawRect(x, y, width, 2, borderColor.getRGB());
        RenderUtil.drawRect(x, y, 2, height, borderColor.getRGB());
        RenderUtil.drawRect(x + width - 2, y, 2, height, borderColor.getRGB());
        RenderUtil.drawRect(x, y + height - 2, width, 2, borderColor.getRGB());
    }
    
    private static void drawBedNukerProgressBar(float x, float y, float width, int height, float progress, float alpha) {
        Color bgColor = new Color(60, 60, 60, (int)(255 * alpha));
        RenderUtil.drawRect(x, y, width, height, bgColor.getRGB());
        
        String progressKey = "bednuker_progress";
        float currentProgress = bedNukerProgressAnimations.getOrDefault(progressKey, 0.0f);
        
        if (Math.abs(currentProgress - progress) > 0.005f) {
            float animationSpeed = 0.12f;
            currentProgress += (progress - currentProgress) * animationSpeed;
            bedNukerProgressAnimations.put(progressKey, currentProgress);
        } else {
            bedNukerProgressAnimations.put(progressKey, progress);
        }
        
        long time = System.currentTimeMillis();
        float wave = (float) Math.sin(time * 0.005) * 0.15f + 0.85f;
        Color progressColor = new Color((int)(255 * wave), (int)(100 * wave), (int)(100 * wave), (int)(255 * alpha));
        
        float animatedWidth = width * currentProgress;
        if (animatedWidth > 0) {
            RenderUtil.drawRect(x, y, animatedWidth, height, progressColor.getRGB());
            
            float glowWidth = Math.min(animatedWidth + 2, width);
            Color glowColor = new Color(255, 150, 150, (int)(100 * alpha * wave));
            if (glowWidth > animatedWidth) {
                RenderUtil.drawRect(x + animatedWidth, y, glowWidth - animatedWidth, height, glowColor.getRGB());
            }
        }
    }
    
    private static void drawAnimatedProgressBar(float x, float y, float width, int height, float progress, float alpha, float maxWidth) {
        String progressKey = "scaffold_progress";
        float currentProgress = scaffoldProgressAnimations.getOrDefault(progressKey, 0.0f);
        
        if (Math.abs(currentProgress - progress) > 0.005f) {
            float animationSpeed = 0.15f;
            currentProgress += (progress - currentProgress) * animationSpeed;
            scaffoldProgressAnimations.put(progressKey, currentProgress);
        } else {
            scaffoldProgressAnimations.put(progressKey, progress);
        }
        
        float clampedProgress = Math.max(0.0f, Math.min(1.0f, currentProgress));
        float animatedWidth = maxWidth * clampedProgress;
        
        if (animatedWidth > 0) {
            Color bgColor = new Color(60, 60, 60, (int)(255 * alpha));
            RenderUtil.drawRect(x, y, animatedWidth, height, bgColor.getRGB());
            
            long time = System.currentTimeMillis();
            float wave = (float) Math.sin(time * 0.004) * 0.2f + 0.8f;
            
            Color progressColor = new Color((int)(255 * wave), (int)(105 * wave), (int)(180 * wave), (int)(255 * alpha));
            RenderUtil.drawRect(x, y, animatedWidth, height, progressColor.getRGB());
        }
    }
    
    private static void drawModernToggleButton(float startX, float startY, float bigBoardHeight, boolean moduleState, float alpha, String uniqueKey) {
        float buttonHeight = 19f;
        float buttonWidth = 32f;
        float buttonRounded = toggleBgRadius;
        float buttonToButtonDistance = 4f;
        float smallButtonHeight = buttonHeight - buttonToButtonDistance * 2f;
        float smallButtonRounded = toggleButtonRadius;
        
        float smallButtonWidth = smallButtonHeight;
        float toBigBorderLen = 6f;
        float ButtonStartY = startY - 2f;
        
        String moduleKey = "toggle_button_" + uniqueKey;
        updateToggleAnimations(moduleKey, moduleState);
        float switchProgress = toggleAnimations.getOrDefault(moduleKey, moduleState ? 1.0f : 0.0f);
        float bgProgress = toggleBgAnimations.getOrDefault(moduleKey, moduleState ? 1.0f : 0.0f);
        float sizeProgress = toggleSizeAnimations.getOrDefault(moduleKey, moduleState ? 1.0f : 0.7f);
        
        float animatedButtonWidth = smallButtonWidth * sizeProgress;
        float animatedButtonHeight = smallButtonHeight * sizeProgress;
        float animatedButtonRounded = smallButtonRounded * sizeProgress;
        
        float leftPos = startX + toBigBorderLen + buttonToButtonDistance;
        float rightPos = startX + toBigBorderLen + buttonWidth - buttonToButtonDistance - animatedButtonWidth;
        float smallButtonStartX = leftPos + (rightPos - leftPos) * switchProgress;
        float smallButtonStartY = ButtonStartY + buttonToButtonDistance + (smallButtonHeight - animatedButtonHeight) / 2;
        
        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.enableBlend();
        net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_LINE_SMOOTH);
        org.lwjgl.opengl.GL11.glHint(org.lwjgl.opengl.GL11.GL_LINE_SMOOTH_HINT, org.lwjgl.opengl.GL11.GL_NICEST);
        
        nightsky.module.modules.render.Interface interfaceModule = 
            (nightsky.module.modules.render.Interface) nightsky.NightSky.moduleManager.getModule("Interface");
        
        int baseColor1 = interfaceModule != null ? interfaceModule.color(0) : 0x80FF95;
        int baseColor2 = interfaceModule != null ? interfaceModule.color(50) : 0x80FFFF;
        
        Color offColor = new Color(40, 40, 40, (int)(100 * alpha));
        Color onColor1 = new Color((baseColor1 >> 16) & 0xFF, (baseColor1 >> 8) & 0xFF, baseColor1 & 0xFF, (int)(180 * alpha));
        Color onColor2 = new Color((baseColor2 >> 16) & 0xFF, (baseColor2 >> 8) & 0xFF, baseColor2 & 0xFF, (int)(180 * alpha));
        
        int bgR = (int)(offColor.getRed() * (1 - bgProgress) + onColor1.getRed() * bgProgress);
        int bgG = (int)(offColor.getGreen() * (1 - bgProgress) + onColor1.getGreen() * bgProgress);
        int bgB = (int)(offColor.getBlue() * (1 - bgProgress) + onColor1.getBlue() * bgProgress);
        int bgA = (int)(offColor.getAlpha() * (1 - bgProgress) + onColor1.getAlpha() * bgProgress);
        
        Color bgColor = new Color(bgR, bgG, bgB, bgA);
        
        RenderUtil.drawRoundedRect(
            startX + toBigBorderLen, 
            ButtonStartY,
            buttonWidth, 
            buttonHeight, 
            buttonRounded, 
            bgColor
        );
        
        if (moduleState && bgProgress > 0.1f) {
            int gradR = (int)(onColor1.getRed() * (1 - switchProgress) + onColor2.getRed() * switchProgress);
            int gradG = (int)(onColor1.getGreen() * (1 - switchProgress) + onColor2.getGreen() * switchProgress);
            int gradB = (int)(onColor1.getBlue() * (1 - switchProgress) + onColor2.getBlue() * switchProgress);
            int gradA = (int)(onColor1.getAlpha() * bgProgress);
            
            Color gradientOverlay = new Color(gradR, gradG, gradB, gradA);
            RenderUtil.drawRoundedRect(
                startX + toBigBorderLen, 
                ButtonStartY,
                buttonWidth, 
                buttonHeight, 
                buttonRounded, 
                gradientOverlay
            );
        }
        
        Color smallButtonOffColor = new Color(180, 180, 180, (int)(220 * alpha));
        Color smallButtonOnColor = new Color(255, 255, 255, (int)(175 * alpha));
        
        int knobR = (int)(smallButtonOffColor.getRed() * (1 - bgProgress) + smallButtonOnColor.getRed() * bgProgress);
        int knobG = (int)(smallButtonOffColor.getGreen() * (1 - bgProgress) + smallButtonOnColor.getGreen() * bgProgress);
        int knobB = (int)(smallButtonOffColor.getBlue() * (1 - bgProgress) + smallButtonOnColor.getBlue() * bgProgress);
        int knobA = (int)(smallButtonOffColor.getAlpha() * (1 - bgProgress) + smallButtonOnColor.getAlpha() * bgProgress);
        
        Color smallButtonColor = new Color(knobR, knobG, knobB, knobA);
        
        RenderUtil.drawRoundedRect(
            smallButtonStartX,
            smallButtonStartY,
            animatedButtonWidth,
            animatedButtonHeight,
            animatedButtonRounded,
            smallButtonColor
        );
        
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_LINE_SMOOTH);
        net.minecraft.client.renderer.GlStateManager.disableBlend();
        net.minecraft.client.renderer.GlStateManager.popMatrix();
    }
    
    private static Color getColorFromNotification(Notification notification) {
        switch (notification.getType()) {
            case ERROR:
                return new Color(244, 67, 54);
            case WARNING:
                return new Color(255, 152, 0);
            case INFO:
                return new Color(33, 150, 243);
            default:
                return Color.WHITE;
        }
    }
    
    private static String getTypeDisplayName(Notification.NotificationType type) {
        switch (type) {
            case ERROR:
                return "Error";
            case WARNING:
                return "Warning";
            case INFO:
                return "Info";
            case SCAFFOLDING:
                return "Scaffolding";
            case BED_NUKER:
                return "BedNuker";
            default:
                return "Notification";
        }
    }
    
    private static float calculateItemHeight(Notification.NotificationType type) {
        switch (type) {
            case SCAFFOLDING:
            case BED_NUKER:
                return 40f;
            case MODULE_ENABLED:
            case MODULE_DISABLED:
            case INFO:
            case ERROR:
            case WARNING:
                return 30f;
            default:
                return 40f;
        }
    }
}