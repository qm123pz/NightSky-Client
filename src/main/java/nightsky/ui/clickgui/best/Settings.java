package nightsky.ui.clickgui.best;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import nightsky.NightSky;
import nightsky.font.CustomFontRenderer;
import nightsky.util.render.RenderUtil;
import nightsky.value.Value;
import nightsky.value.values.*;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Settings {

    static final float SETTING_HEIGHT = 55f;
    static final float COLOR_SETTING_HEIGHT = 105f;
    private static final float SLIDER_WIDTH = 200f;
    private static final float SLIDER_HEIGHT = 3f;
    private static final float LABEL_CONTROL_SPACING = 12f;

    private static final Map<String, Float> toggleAnimations = new HashMap<>();
    private static final Map<String, Float> toggleBgAnimations = new HashMap<>();
    private static final Map<String, Float> toggleSizeAnimations = new HashMap<>();
    private static final float ANIMATION_SPEED = 0.15f;
    private static final float toggleBgRadius = 17f;
    private static final float toggleButtonRadius = 8f;

    public static class ColorPickerState {
        public float pickerX, pickerY;
        public float hueSliderY;
        public boolean draggingHue = false;
        public boolean draggingColor = false;
    }

    public static void drawSettingsMenu(BestClickGui gui, float guiX, float guiY, float bgWidth, float bgHeight, int mouseX, int mouseY) {
        if (gui.selectedModule == null) return;

        float titleWidth = CustomFontRenderer.getStringWidth(gui.selectedModule.getName(), gui.categoryFont);
        float totalWidth = BestClickGui.HOME_BUTTON_SIZE + 10 + titleWidth;
        float startX = guiX + bgWidth / 2 - totalWidth / 2;

        float homeY = guiY + BestClickGui.PADDING;
        boolean homeHovered = mouseX >= startX && mouseX <= startX + BestClickGui.HOME_BUTTON_SIZE &&
                mouseY >= homeY && mouseY <= homeY + BestClickGui.HOME_BUTTON_SIZE;

        if (homeHovered) {
            int hoverAlpha = Math.max(0, Math.min(255, (int)(30 * gui.openAnimation)));
            RenderUtil.drawRoundedRect(startX, homeY, BestClickGui.HOME_BUTTON_SIZE, BestClickGui.HOME_BUTTON_SIZE,
                    6f, new Color(0, 0, 0, hoverAlpha));
        }

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, gui.openAnimation);

        gui.mc.getTextureManager().bindTexture(gui.HOME_ICON);
        Gui.drawModalRectWithCustomSizedTexture((int) startX, (int)homeY, 0, 0,
                (int)BestClickGui.HOME_BUTTON_SIZE, (int)BestClickGui.HOME_BUTTON_SIZE,
                BestClickGui.HOME_BUTTON_SIZE, BestClickGui.HOME_BUTTON_SIZE);

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        float titleX = startX + BestClickGui.HOME_BUTTON_SIZE + 10;
        float titleY = guiY + BestClickGui.PADDING + BestClickGui.HOME_BUTTON_SIZE / 2 -
                (float) CustomFontRenderer.getFontHeight(gui.categoryFont) / 2;
        int titleAlpha = Math.max(0, Math.min(255, (int)(255 * gui.openAnimation)));
        int titleColor = 0xFFFFFF | (titleAlpha << 24);
        CustomFontRenderer.drawString(gui.selectedModule.getName(), titleX, titleY, titleColor, gui.categoryFont);

        float clipStartY = guiY + BestClickGui.PADDING + BestClickGui.HOME_BUTTON_SIZE + 10;
        float clipHeight = bgHeight - BestClickGui.PADDING - BestClickGui.HOME_BUTTON_SIZE - 10;

        net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(gui.mc);
        int scaleFactor = sr.getScaleFactor();
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);
        int scissorX = (int)(guiX * scaleFactor);
        int scissorY = (int)(gui.mc.displayHeight - (clipStartY + clipHeight) * scaleFactor);
        int scissorWidth = (int)(bgWidth * scaleFactor);
        int scissorHeight = (int)(clipHeight * scaleFactor);
        org.lwjgl.opengl.GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight);

        float settingsStartY = guiY + BestClickGui.PADDING + BestClickGui.HOME_BUTTON_SIZE + 15 - gui.settingsScrollOffset;

        ArrayList<Value<?>> values = NightSky.valueHandler.properties.get(gui.selectedModule.getClass());

        if (values == null || values.isEmpty()) {
            drawNoSettingsMessage(gui, guiX, guiY, bgWidth, bgHeight);
            org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);
            return;
        }

        int visibleCount = 0;
        for (Value<?> value : values) {
            if (value.isVisible()) visibleCount++;
        }

        if (visibleCount == 0) {
            drawNoSettingsMessage(gui, guiX, guiY, bgWidth, bgHeight);
            org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);
            return;
        }

        float currentY = settingsStartY;
        for (Value<?> value : values) {
            if (!value.isVisible()) continue;

            float itemHeight = (value instanceof ColorValue) ? COLOR_SETTING_HEIGHT : SETTING_HEIGHT;

            if (currentY + itemHeight < guiY || currentY > guiY + bgHeight) {
                currentY += itemHeight;
                continue;
            }

            drawSetting(gui, value, guiX + BestClickGui.PADDING, currentY,
                    bgWidth - BestClickGui.PADDING * 2, mouseX, mouseY);

            currentY += itemHeight;
        }

        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);
    }

    private static void drawNoSettingsMessage(BestClickGui gui, float guiX, float guiY, float bgWidth, float bgHeight) {
        String message = "Not found settings here";

        int textAlpha = Math.max(0, Math.min(255, (int)(180 * gui.openAnimation)));
        int textColor = 0x808080 | (textAlpha << 24);

        float messageWidth = CustomFontRenderer.getStringWidth(message, gui.settingFont);
        float messageX = guiX + bgWidth / 2 - messageWidth / 2;
        float messageY = guiY + BestClickGui.PADDING + BestClickGui.HOME_BUTTON_SIZE + 30;

        CustomFontRenderer.drawString(message, messageX, messageY, textColor, gui.settingFont);
    }

    private static void drawSetting(BestClickGui gui, Value<?> value, float x, float y, float width,
                                    int mouseX, int mouseY) {
        int textAlpha = Math.max(0, Math.min(255, (int)(255 * gui.openAnimation)));
        int textColor = 0xFFFFFF | (textAlpha << 24);

        float labelX = x + width / 2 - (float) CustomFontRenderer.getStringWidth(value.getName(), gui.settingFont) / 2;
        CustomFontRenderer.drawString(value.getName(), labelX, y, textColor, gui.settingFont);

        float valueY = y + CustomFontRenderer.getFontHeight(gui.settingFont) + LABEL_CONTROL_SPACING;

        if (value instanceof BooleanValue) {
            drawBooleanSetting(gui, (BooleanValue) value, x, valueY, width, mouseX, mouseY, textAlpha);
        } else if (value instanceof IntValue) {
            drawIntSetting(gui, (IntValue) value, x, valueY, width, mouseX, mouseY, textAlpha);
        } else if (value instanceof FloatValue) {
            drawFloatSetting(gui, (FloatValue) value, x, valueY, width, mouseX, mouseY, textAlpha);
        } else if (value instanceof PercentValue) {
            drawPercentSetting(gui, (PercentValue) value, x, valueY, width, mouseX, mouseY, textAlpha);
        } else if (value instanceof ModeValue) {
            drawModeSetting(gui, (ModeValue) value, x, valueY, width, mouseX, mouseY, textAlpha);
        } else if (value instanceof ColorValue) {
            drawColorSetting(gui, (ColorValue) value, x, valueY, width, mouseX, mouseY, textAlpha);
        } else if (value instanceof TextValue) {
            drawTextSetting(gui, (TextValue) value, x, valueY, width, mouseX, mouseY, textAlpha);
        }
    }

    private static void drawBooleanSetting(BestClickGui gui, BooleanValue value, float x, float y, float width,
                                           int mouseX, int mouseY, int textAlpha) {
        boolean enabled = value.getValue();
        float buttonHeight = 19f;
        float buttonWidth = 32f;
        float buttonRounded = toggleBgRadius;
        float buttonToButtonDistance = 4f;
        float smallButtonHeight = buttonHeight - buttonToButtonDistance * 2f;

        float toggleX = x + width / 2 - buttonWidth / 2;

        String uniqueKey = value.getName();
        updateToggleAnimations(uniqueKey, enabled);
        float switchProgress = toggleAnimations.getOrDefault(uniqueKey, enabled ? 1.0f : 0.0f);
        float bgProgress = toggleBgAnimations.getOrDefault(uniqueKey, enabled ? 1.0f : 0.0f);
        float sizeProgress = toggleSizeAnimations.getOrDefault(uniqueKey, enabled ? 1.0f : 0.7f);

        float animatedButtonWidth = smallButtonHeight * sizeProgress;
        float animatedButtonHeight = smallButtonHeight * sizeProgress;
        float animatedButtonRounded = toggleButtonRadius * sizeProgress;

        float leftPos = toggleX + buttonToButtonDistance;
        float rightPos = toggleX + buttonWidth - buttonToButtonDistance - animatedButtonWidth;
        float smallButtonStartX = leftPos + (rightPos - leftPos) * switchProgress;
        float smallButtonStartY = y + buttonToButtonDistance + (smallButtonHeight - animatedButtonHeight) / 2;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        nightsky.module.modules.render.Interface interfaceModule =
                (nightsky.module.modules.render.Interface) NightSky.moduleManager.getModule("Interface");

        int baseColor1 = interfaceModule != null ? interfaceModule.color(0) : 0x80FF95;
        int baseColor2 = interfaceModule != null ? interfaceModule.color(50) : 0x80FFFF;

        float alpha = gui.openAnimation;
        Color offColor = new Color(40, 40, 40, (int)(100 * alpha));
        Color onColor1 = new Color((baseColor1 >> 16) & 0xFF, (baseColor1 >> 8) & 0xFF, baseColor1 & 0xFF, (int)(180 * alpha));
        Color onColor2 = new Color((baseColor2 >> 16) & 0xFF, (baseColor2 >> 8) & 0xFF, baseColor2 & 0xFF, (int)(180 * alpha));

        int bgR = (int)(offColor.getRed() * (1 - bgProgress) + onColor1.getRed() * bgProgress);
        int bgG = (int)(offColor.getGreen() * (1 - bgProgress) + onColor1.getGreen() * bgProgress);
        int bgB = (int)(offColor.getBlue() * (1 - bgProgress) + onColor1.getBlue() * bgProgress);
        int bgA = (int)(offColor.getAlpha() * (1 - bgProgress) + onColor1.getAlpha() * bgProgress);

        Color bgColor = new Color(bgR, bgG, bgB, bgA);

        RenderUtil.drawRoundedRect(toggleX, y, buttonWidth, buttonHeight, buttonRounded, bgColor);

        if (enabled && bgProgress > 0.1f) {
            int gradR = (int)(onColor1.getRed() * (1 - switchProgress) + onColor2.getRed() * switchProgress);
            int gradG = (int)(onColor1.getGreen() * (1 - switchProgress) + onColor2.getGreen() * switchProgress);
            int gradB = (int)(onColor1.getBlue() * (1 - switchProgress) + onColor2.getBlue() * switchProgress);
            int gradA = (int)(onColor1.getAlpha() * bgProgress);

            Color gradientOverlay = new Color(gradR, gradG, gradB, gradA);
            RenderUtil.drawRoundedRect(toggleX, y, buttonWidth, buttonHeight, buttonRounded, gradientOverlay);
        }

        Color smallButtonOffColor = new Color(180, 180, 180, (int)(220 * alpha));
        Color smallButtonOnColor = new Color(255, 255, 255, (int)(175 * alpha));

        int knobR = (int)(smallButtonOffColor.getRed() * (1 - bgProgress) + smallButtonOnColor.getRed() * bgProgress);
        int knobG = (int)(smallButtonOffColor.getGreen() * (1 - bgProgress) + smallButtonOnColor.getGreen() * bgProgress);
        int knobB = (int)(smallButtonOffColor.getBlue() * (1 - bgProgress) + smallButtonOnColor.getBlue() * bgProgress);
        int knobA = (int)(smallButtonOffColor.getAlpha() * (1 - bgProgress) + smallButtonOnColor.getAlpha() * bgProgress);

        Color smallButtonColor = new Color(knobR, knobG, knobB, knobA);

        RenderUtil.drawRoundedRect(smallButtonStartX, smallButtonStartY, animatedButtonWidth, animatedButtonHeight, animatedButtonRounded, smallButtonColor);

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static void updateToggleAnimations(String key, boolean enabled) {
        float targetSwitch = enabled ? 1.0f : 0.0f;
        float targetBg = enabled ? 1.0f : 0.0f;
        float targetSize = enabled ? 1.0f : 0.7f;

        float currentSwitch = toggleAnimations.getOrDefault(key, targetSwitch);
        float currentBg = toggleBgAnimations.getOrDefault(key, targetBg);
        float currentSize = toggleSizeAnimations.getOrDefault(key, targetSize);

        if (Math.abs(currentSwitch - targetSwitch) > 0.01f) {
            currentSwitch += (targetSwitch - currentSwitch) * ANIMATION_SPEED;
            toggleAnimations.put(key, currentSwitch);
        } else {
            toggleAnimations.put(key, targetSwitch);
        }

        if (Math.abs(currentBg - targetBg) > 0.01f) {
            currentBg += (targetBg - currentBg) * ANIMATION_SPEED;
            toggleBgAnimations.put(key, currentBg);
        } else {
            toggleBgAnimations.put(key, targetBg);
        }

        if (Math.abs(currentSize - targetSize) > 0.01f) {
            currentSize += (targetSize - currentSize) * ANIMATION_SPEED;
            toggleSizeAnimations.put(key, currentSize);
        } else {
            toggleSizeAnimations.put(key, targetSize);
        }
    }

    private static void drawIntSetting(BestClickGui gui, IntValue value, float x, float y, float width,
                                       int mouseX, int mouseY, int textAlpha) {
        int min = value.getMinimum();
        int max = value.getMaximum();
        int current = value.getValue();

        float sliderX = x + width / 2 - SLIDER_WIDTH / 2;
        float progress = (float)(current - min) / (float)(max - min);

        int bgAlpha = Math.max(0, Math.min(255, (int)(30 * gui.openAnimation)));
        RenderUtil.drawRoundedRect(sliderX, y, SLIDER_WIDTH, SLIDER_HEIGHT, 2f, new Color(0, 0, 0, bgAlpha));

        int progressAlpha = Math.max(0, Math.min(255, (int)(150 * gui.openAnimation)));
        RenderUtil.drawRoundedRect(sliderX, y, SLIDER_WIDTH * progress, SLIDER_HEIGHT, 2f,
                new Color(100, 150, 255, progressAlpha));

        String valueText = String.valueOf(current);
        float valueX = sliderX + SLIDER_WIDTH + 8;
        float valueTextY = y - (float) CustomFontRenderer.getFontHeight(gui.settingFont) / 2;
        CustomFontRenderer.drawString(valueText, valueX, valueTextY, 0xFFFFFF | (textAlpha << 24), gui.settingFont);
    }

    private static void drawFloatSetting(BestClickGui gui, FloatValue value, float x, float y, float width,
                                         int mouseX, int mouseY, int textAlpha) {
        float min = value.getMinimum();
        float max = value.getMaximum();
        float current = value.getValue();

        float sliderX = x + width / 2 - SLIDER_WIDTH / 2;
        float progress = (current - min) / (max - min);

        int bgAlpha = Math.max(0, Math.min(255, (int)(30 * gui.openAnimation)));
        RenderUtil.drawRoundedRect(sliderX, y, SLIDER_WIDTH, SLIDER_HEIGHT, 2f, new Color(0, 0, 0, bgAlpha));

        int progressAlpha = Math.max(0, Math.min(255, (int)(150 * gui.openAnimation)));
        RenderUtil.drawRoundedRect(sliderX, y, SLIDER_WIDTH * progress, SLIDER_HEIGHT, 2f,
                new Color(100, 150, 255, progressAlpha));

        String valueText = String.format("%.2f", current);
        float valueX = sliderX + SLIDER_WIDTH + 8;
        float valueTextY = y - (float) CustomFontRenderer.getFontHeight(gui.settingFont) / 2;
        CustomFontRenderer.drawString(valueText, valueX, valueTextY, 0xFFFFFF | (textAlpha << 24), gui.settingFont);
    }

    private static void drawPercentSetting(BestClickGui gui, PercentValue value, float x, float y, float width,
                                           int mouseX, int mouseY, int textAlpha) {
        float current = value.getValue();
        float progress = current / 100f;

        float sliderX = x + width / 2 - SLIDER_WIDTH / 2;
        int bgAlpha = Math.max(0, Math.min(255, (int)(30 * gui.openAnimation)));
        RenderUtil.drawRoundedRect(sliderX, y, SLIDER_WIDTH, SLIDER_HEIGHT, 2f, new Color(0, 0, 0, bgAlpha));

        int progressAlpha = Math.max(0, Math.min(255, (int)(150 * gui.openAnimation)));
        RenderUtil.drawRoundedRect(sliderX, y, SLIDER_WIDTH * progress, SLIDER_HEIGHT, 2f,
                new Color(100, 150, 255, progressAlpha));

        String valueText = String.format("%.0f%%", current);
        float valueX = sliderX + SLIDER_WIDTH + 8;
        float valueTextY = y - (float) CustomFontRenderer.getFontHeight(gui.settingFont) / 2;
        CustomFontRenderer.drawString(valueText, valueX, valueTextY, 0xFFFFFF | (textAlpha << 24), gui.settingFont);
    }

    private static void drawModeSetting(BestClickGui gui, ModeValue value, float x, float y, float width,
                                        int mouseX, int mouseY, int textAlpha) {
        String current = value.getModeString();

        float modeWidth = Math.min(120f, CustomFontRenderer.getStringWidth(current, gui.settingFont) + 20);
        float modeHeight = 18f;
        float modeX = x + width / 2 - modeWidth / 2;

        boolean hovered = mouseX >= modeX && mouseX <= modeX + modeWidth &&
                mouseY >= y && mouseY <= y + modeHeight;

        int bgAlpha = Math.max(0, Math.min(255, (int)((hovered ? 50 : 30) * gui.openAnimation)));
        RenderUtil.drawRoundedRect(modeX, y, modeWidth, modeHeight, 6f, new Color(0, 0, 0, bgAlpha));

        float textX = modeX + modeWidth / 2 - (float) CustomFontRenderer.getStringWidth(current, gui.settingFont) / 2;
        float textY = y + modeHeight / 2 - (float) CustomFontRenderer.getFontHeight(gui.settingFont) / 2;
        CustomFontRenderer.drawString(current, textX, textY, 0xFFFFFF | (textAlpha << 24), gui.settingFont);
    }

    private static void drawColorSetting(BestClickGui gui, ColorValue value, float x, float y, float width,
                                         int mouseX, int mouseY, int textAlpha) {
        ColorPickerState state = gui.colorPickerStates.computeIfAbsent(value, k -> new ColorPickerState());

        float pickerWidth = 120f;
        float pickerHeight = 60f;
        float hueSliderHeight = 6f;
        float pickerX = x + width / 2 - pickerWidth / 2;

        state.pickerX = pickerX;
        state.pickerY = y;
        state.hueSliderY = y + pickerHeight + 5;

        int rgb = value.getValue();
        if ((rgb & 0xFFFFFF) == 0) {
            rgb = 0xFF8080FF;
            value.setValue(rgb);
        }

        Color hueColor = Color.getHSBColor(value.getHue(), 1, 1);
        int hueAlpha = Math.max(0, Math.min(255, (int)(255 * gui.openAnimation)));
        Color hueWithAlpha = new Color(hueColor.getRed(), hueColor.getGreen(), hueColor.getBlue(), hueAlpha);
        RenderUtil.drawRoundedRect(pickerX, y, pickerWidth, pickerHeight, 4f, hueWithAlpha);

        for (int xPos = 0; xPos < pickerWidth; xPos++) {
            float saturation = xPos / pickerWidth;
            Color whiteGradient = new Color(255, 255, 255, (int)(255 * (1 - saturation) * gui.openAnimation));
            RenderUtil.drawRect(pickerX + xPos, y, 1, pickerHeight, whiteGradient.getRGB());
        }

        for (int yPos = 0; yPos < pickerHeight; yPos++) {
            float brightness = 1 - (yPos / pickerHeight);
            Color blackGradient = new Color(0, 0, 0, (int)(255 * (1 - brightness) * gui.openAnimation));
            RenderUtil.drawRect(pickerX, y + yPos, pickerWidth, 1, blackGradient.getRGB());
        }

        for (float xPos = 0; xPos < pickerWidth; xPos++) {
            float hue = xPos / pickerWidth;
            Color c = Color.getHSBColor(hue, 1, 1);
            int alpha = Math.max(0, Math.min(255, (int)(255 * gui.openAnimation)));
            Color cWithAlpha = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
            RenderUtil.drawRect(pickerX + xPos, state.hueSliderY, 1, hueSliderHeight, cWithAlpha.getRGB());
        }

        float huePos = pickerX + (value.getHue() * pickerWidth);
        int whiteAlpha = Math.max(0, Math.min(255, (int)(255 * gui.openAnimation)));
        RenderUtil.drawRect(huePos - 1, state.hueSliderY - 1, 2, hueSliderHeight + 2, new Color(255, 255, 255, whiteAlpha).getRGB());
        RenderUtil.drawRect(huePos, state.hueSliderY, 1, hueSliderHeight, new Color(0, 0, 0, whiteAlpha).getRGB());

        float colorPosX = pickerX + (value.getSaturation() * pickerWidth);
        float colorPosY = y + ((1 - value.getBrightness()) * pickerHeight);
        RenderUtil.drawRect(colorPosX - 2, colorPosY - 2, 4, 4, new Color(255, 255, 255, whiteAlpha).getRGB());
    }

    private static void drawTextSetting(BestClickGui gui, TextValue value, float x, float y, float width,
                                        int mouseX, int mouseY, int textAlpha) {
        float boxWidth = 200f;
        float boxHeight = 20f;
        float boxX = x + width / 2 - boxWidth / 2;

        nightsky.ui.clickgui.augustus.TextField textField = gui.textFieldMap.computeIfAbsent(value, k -> {
            nightsky.ui.clickgui.augustus.TextField tf = new nightsky.ui.clickgui.augustus.TextField();
            tf.setText(value.getValue());
            tf.setCursorPositionZero();
            return tf;
        });

        int bgAlpha = Math.max(0, Math.min(255, (int)(30 * gui.openAnimation)));
        RenderUtil.drawRoundedRect(boxX, y, boxWidth, boxHeight, 6f, new Color(0, 0, 0, bgAlpha));

        textField.setXPosition(boxX + 5);
        textField.setYPosition(y + boxHeight / 2 - (float) CustomFontRenderer.getFontHeight(gui.settingFont) / 2);
        textField.setWidth(boxWidth - 10);
        textField.setHeight(boxHeight);
        textField.setEnableBackgroundDrawing(false);
        textField.updateCursorCounter();

        String displayText = textField.getText();
        if (displayText.isEmpty() && !textField.isFocused()) {
            int placeholderAlpha = Math.max(0, Math.min(255, (int)(120 * gui.openAnimation)));
            CustomFontRenderer.drawString("Enter text...", boxX + 5, y + boxHeight / 2 - (float) CustomFontRenderer.getFontHeight(gui.settingFont) / 2,
                    0x808080 | (placeholderAlpha << 24), gui.settingFont);
        } else {
            textField.drawTextBox();
        }

        value.setValue(textField.getText());
    }

    public static void handleSettingClick(BestClickGui gui, float guiX, float guiY, float bgWidth, int mouseX, int mouseY) {
        if (gui.selectedModule == null) return;

        float settingsStartY = guiY + BestClickGui.PADDING + BestClickGui.HOME_BUTTON_SIZE + 15 - gui.settingsScrollOffset;

        ArrayList<Value<?>> values = NightSky.valueHandler.properties.get(gui.selectedModule.getClass());
        if (values == null) return;

        float currentY = settingsStartY;
        for (Value<?> value : values) {
            if (!value.isVisible()) continue;

            float itemHeight = (value instanceof ColorValue) ? COLOR_SETTING_HEIGHT : SETTING_HEIGHT;
            float settingY = currentY;
            float valueY = settingY + CustomFontRenderer.getFontHeight(gui.settingFont) + LABEL_CONTROL_SPACING;
            float settingWidth = bgWidth - BestClickGui.PADDING * 2;

            if (value instanceof BooleanValue) {
                float toggleWidth = 32f;
                float toggleHeight = 19f;
                float toggleX = guiX + BestClickGui.PADDING + settingWidth / 2 - toggleWidth / 2;

                if (mouseX >= toggleX && mouseX <= toggleX + toggleWidth &&
                        mouseY >= valueY && mouseY <= valueY + toggleHeight) {
                    value.setValue(!((BooleanValue) value).getValue());
                    return;
                }
            } else if (value instanceof ModeValue) {
                ModeValue modeValue = (ModeValue) value;
                String current = modeValue.getModeString();
                float modeWidth = Math.min(120f, CustomFontRenderer.getStringWidth(current, gui.settingFont) + 20);
                float modeHeight = 18f;
                float modeX = guiX + BestClickGui.PADDING + settingWidth / 2 - modeWidth / 2;

                if (mouseX >= modeX && mouseX <= modeX + modeWidth &&
                        mouseY >= valueY && mouseY <= valueY + modeHeight) {
                    int currentIndex = modeValue.getValue();
                    int nextIndex = (currentIndex + 1) % modeValue.getModes().length;
                    modeValue.setValue(nextIndex);
                    return;
                }
            } else if (value instanceof ColorValue) {
                ColorValue colorValue = (ColorValue) value;
                ColorPickerState state = gui.colorPickerStates.get(colorValue);
                if (state != null) {
                    float pickerWidth = 120f;
                    float pickerHeight = 60f;
                    float hueSliderHeight = 6f;
                    float pickerX = guiX + BestClickGui.PADDING + settingWidth / 2 - pickerWidth / 2;

                    if (mouseX >= pickerX && mouseX <= pickerX + pickerWidth &&
                            mouseY >= state.hueSliderY && mouseY <= state.hueSliderY + hueSliderHeight) {
                        state.draggingHue = true;
                        float hue = net.minecraft.util.MathHelper.clamp_float((mouseX - pickerX) / pickerWidth, 0, 1);
                        colorValue.setHue(hue);
                        return;
                    } else if (mouseX >= pickerX && mouseX <= pickerX + pickerWidth &&
                            mouseY >= valueY && mouseY <= valueY + pickerHeight) {
                        state.draggingColor = true;
                        float saturation = net.minecraft.util.MathHelper.clamp_float((mouseX - pickerX) / pickerWidth, 0, 1);
                        float brightness = 1 - net.minecraft.util.MathHelper.clamp_float((mouseY - valueY) / pickerHeight, 0, 1);
                        colorValue.setSaturation(saturation);
                        colorValue.setBrightness(brightness);
                        return;
                    }
                }
            } else if (value instanceof TextValue) {
                TextValue textValue = (TextValue) value;
                nightsky.ui.clickgui.augustus.TextField textField = gui.textFieldMap.get(textValue);
                if (textField != null) {
                    float boxWidth = 200f;
                    float boxHeight = 20f;
                    float textFieldX = guiX + BestClickGui.PADDING + settingWidth / 2 - boxWidth / 2;

                    textField.setXPosition(textFieldX + 5);
                    textField.setYPosition(valueY + boxHeight / 2 - (float) CustomFontRenderer.getFontHeight(gui.settingFont) / 2);
                    textField.setWidth(boxWidth - 10);
                    textField.setHeight(boxHeight);
                    textField.mouseClicked(mouseX, mouseY, 0);
                }
            }

            currentY += itemHeight;
        }
    }

    public static void handleSettingDrag(BestClickGui gui, float guiX, float guiY, float bgWidth, int mouseX, int mouseY) {
        if (gui.selectedModule == null) return;

        float settingsStartY = guiY + BestClickGui.PADDING + BestClickGui.HOME_BUTTON_SIZE + 15 - gui.settingsScrollOffset;

        ArrayList<Value<?>> values = NightSky.valueHandler.properties.get(gui.selectedModule.getClass());
        if (values == null) return;

        float currentY = settingsStartY;
        for (Value<?> value : values) {
            if (!value.isVisible()) continue;

            float itemHeight = (value instanceof ColorValue) ? COLOR_SETTING_HEIGHT : SETTING_HEIGHT;
            float settingY = currentY;
            float valueY = settingY + CustomFontRenderer.getFontHeight(gui.settingFont) + LABEL_CONTROL_SPACING;
            float settingWidth = bgWidth - BestClickGui.PADDING * 2;
            float sliderX = guiX + BestClickGui.PADDING + settingWidth / 2 - SLIDER_WIDTH / 2;

            if (mouseX >= sliderX && mouseX <= sliderX + SLIDER_WIDTH &&
                    mouseY >= valueY - 5 && mouseY <= valueY + SLIDER_HEIGHT + 5) {

                float progress = Math.max(0, Math.min(1, (mouseX - sliderX) / SLIDER_WIDTH));

                if (value instanceof IntValue) {
                    IntValue intValue = (IntValue) value;
                    int newValue = (int)(intValue.getMinimum() + progress * (intValue.getMaximum() - intValue.getMinimum()));
                    intValue.setValue(newValue);
                } else if (value instanceof FloatValue) {
                    FloatValue floatValue = (FloatValue) value;
                    float newValue = floatValue.getMinimum() + progress * (floatValue.getMaximum() - floatValue.getMinimum());
                    floatValue.setValue(newValue);
                } else if (value instanceof PercentValue) {
                    ((PercentValue) value).setValue(progress * 100f);
                }
            }
            currentY += itemHeight;
        }
    }
}