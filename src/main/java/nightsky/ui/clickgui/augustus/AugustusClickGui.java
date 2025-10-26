package nightsky.ui.clickgui.augustus;

import nightsky.NightSky;
import nightsky.font.FontRenderer;
import nightsky.font.FontTransformer;
import nightsky.font.CustomFontRenderer;
import nightsky.module.ModuleCategory;
import nightsky.module.Module;
import nightsky.value.Value;
import nightsky.value.values.*;
import nightsky.util.misc.HoverUtil;
import nightsky.util.render.RenderUtil;
import nightsky.util.render.Render2DUtil;
import nightsky.util.render.ColorUtil;
import nightsky.util.render.animations.AnimationUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AugustusClickGui extends GuiScreen {
    boolean expandingRight;
    boolean dragging = false;
    boolean waitingForKey = false;
    boolean draggingSlider = false;
    private boolean isGuiOpen = false;
    private boolean isConfigOpen = false;
    private boolean configDragging = false;
    private boolean configResizing = false;
    private float configDraggingX, configDraggingY;
    private float configPosX = -1, configPosY = -1;
    private float configWidth = 500, configHeight = 400;
    private String selectedConfig = null;
    private boolean creatingNewConfig = false;
    private String newConfigName = "";
    private float guiOpenAnimation = 0.0f;
    private float targetCategoryLineX = 0.0f;
    private float currentCategoryLineX = 0.0f;
    private long lastAnimationTime = System.currentTimeMillis();
    float draggingX, draggingY;
    float posX, posY;
    float width, height;
    float valueScroll = 0F, moduleScroll = 0F;
    public static float lastPosX = -1337, lastPosY = -1337;
    public static float lastWidth = 600, lastHeight = 300;

    Module selectModule;
    ModuleCategory selectCategory;

    public enum GuiEvents {
        DRAW,
        CLICK,
        RELEASE
    }

    private static class ColorPickerState {
        float pickerX, pickerY;
        float hueSliderY;
        boolean draggingHue;
        boolean draggingColor;
        boolean isPickerOpen = false;
    }

    FloatValue currentDraggingSlider = null;
    IntValue currentDraggingIntSlider = null;
    PercentValue currentDraggingPercentSlider = null;

    private final HashMap<TextValue, TextField> textFieldMap = new HashMap<>();
    private final Map<ColorValue, ColorPickerState> colorPickerStates = new HashMap<>();
    private final HashMap<Value<?>, Float> numberSettingMap = new HashMap<>();

    private Font titleFont;
    private Font normalFont;
    private Font smallFont;
    private float lastFontScale = 1.0f;

    private void initFonts() {
        FontTransformer transformer = FontTransformer.getInstance();
        float fontScale = getFontScale();
        
        titleFont = transformer.getFont("ESP", (int)(24 * fontScale));
        normalFont = transformer.getFont("Consolas", (int)(20 * fontScale));
        smallFont = transformer.getFont("Consolas", (int)(18 * fontScale));
        
        if (normalFont == null) normalFont = transformer.getFont("Arial", (int)(20 * fontScale));
        if (smallFont == null) smallFont = transformer.getFont("Arial", (int)(18 * fontScale));
    }
    
    private float getFontScale() {
        return 2.15f;
    }

    private void drawTitleString(String text, float x, float y, int color) {
        if (titleFont != null) {
            CustomFontRenderer.drawString(text, x, y, color, titleFont);
        } else {
            FontRenderer.drawString(text, x, y, color);
        }
    }

    private void drawNormalString(String text, float x, float y, int color) {
        if (normalFont != null) {
            CustomFontRenderer.drawString(text, x, y, color, normalFont);
        } else {
            FontRenderer.drawString(text, x, y, color);
        }
    }

    private float getNormalStringWidth(String text) {
        if (normalFont != null) {
            return CustomFontRenderer.getStringWidth(text, normalFont);
        }
        return FontRenderer.getStringWidth(text);
    }

    private float getNormalFontHeight() {
        if (normalFont != null) {
            return (float) CustomFontRenderer.getFontHeight(normalFont);
        }
        return FontRenderer.getFontHeight();
    }

    private float getTitleStringWidth(String text) {
        if (titleFont != null) {
            return CustomFontRenderer.getStringWidth(text, titleFont);
        } else {
            return FontRenderer.getStringWidth(text);
        }
    }

    private void updateAnimations() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.max(0, Math.min(0.1f, (currentTime - lastAnimationTime) / 1000.0f));
        lastAnimationTime = currentTime;
        
        float animationSpeed = 8.0f;
        
        float targetGuiAnimation = isGuiOpen ? 1.0f : 0.0f;
        guiOpenAnimation = Math.max(0.0f, Math.min(1.0f, (float) AnimationUtil.animate(targetGuiAnimation, guiOpenAnimation, deltaTime * animationSpeed)));
        
        if (Math.abs(targetCategoryLineX - currentCategoryLineX) > 0.1f) {
            currentCategoryLineX = (float) AnimationUtil.animate(targetCategoryLineX, currentCategoryLineX, deltaTime * animationSpeed * 1.5f);
        }
    }

    public AugustusClickGui() {
        lastFontScale = getFontScale();
        initFonts();
        if (lastPosX == -1337 || lastPosY == -1337) {
            this.posX = super.width / 2F - this.width / 2;
            this.posY = super.height / 2F - this.height / 2;
            this.width = 600;
            this.height = 300;
            if (posX <= 0) {
                posX = 180;
            }

            if (posY <= 0) {
                posY = 100;
            }
        } else {
            this.posX = lastPosX;
            this.posY = lastPosY;
            this.height = lastHeight;
        }
        this.selectCategory = ModuleCategory.COMBAT;
    }

    private Color getGlobalColor() {
        try {
            nightsky.module.modules.render.Interface interfaceModule = 
                (nightsky.module.modules.render.Interface) NightSky.moduleManager.getModule("Interface");
            if (interfaceModule != null) {
                return new Color(interfaceModule.color());
            }
        } catch (Exception e) {
        }
        return new Color(81, 149, 219);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        updateAnimations();
        handle(mouseX, mouseY, -1, GuiEvents.DRAW);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        handle(mouseX, mouseY, mouseButton, GuiEvents.CLICK);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        handle(mouseX, mouseY, state, GuiEvents.RELEASE);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (creatingNewConfig) {
            if (keyCode == 1) {
                creatingNewConfig = false;
                newConfigName = "";
            } else if (keyCode == 28) {
                if (!newConfigName.trim().isEmpty()) {
                    nightsky.config.Config config = new nightsky.config.Config(newConfigName.trim(), true);
                    config.save();
                    selectedConfig = newConfigName.trim();
                }
                creatingNewConfig = false;
                newConfigName = "";
            } else if (keyCode == 14) {
                if (newConfigName.length() > 0) {
                    newConfigName = newConfigName.substring(0, newConfigName.length() - 1);
                }
            } else if (Character.isLetterOrDigit(typedChar) || typedChar == '_' || typedChar == '-') {
                if (newConfigName.length() < 20) {
                    newConfigName += typedChar;
                }
            }
            return;
        }

        if (keyCode == 1 && !waitingForKey) {
            this.mc.displayGuiScreen(null);
        }

        if (waitingForKey) {
            if (keyCode == 1) {
                selectModule.setKey(0);
            } else {
                selectModule.setKey(keyCode);
            }
            waitingForKey = false;
            return;
        }

        if (selectModule != null) {
            for (Map.Entry<TextValue, TextField> entry : textFieldMap.entrySet()) {
                entry.getValue().keyTyped(typedChar, keyCode);
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        lastPosX = this.posX;
        lastPosY = this.posY;
        lastWidth = this.width;
        lastHeight = this.height;
        super.onGuiClosed();
    }

    private void drawCheckerboard(float x, float y, float width, float height) {
        int squareSize = 4;
        for (float i = 0; i < width; i += squareSize) {
            for (float j = 0; j < height; j += squareSize) {
                boolean white = ((i + j) / squareSize) % 2 == 0;
                Color color = white ? new Color(220, 220, 220) : new Color(180, 180, 180);
                RenderUtil.drawRect(x + i, y + j, Math.min(squareSize, width - i), Math.min(squareSize, height - j), color.getRGB());
            }
        }
    }

    public void handle(int mouseX, int mouseY, int mouseButton, GuiEvents type) {
        IconMeasurements m = getIconMeasurements();

        float separatorWidth = getTitleStringWidth(" | ");
        float totalWidth = m.iconSWidth + separatorWidth + m.iconCWidth;
        float bgX = m.iconX - 8;
        float bgWidth = totalWidth + 16;
        float bgY = m.iconY - 21;

        if (type == GuiEvents.DRAW) {
            RenderUtil.drawRoundedRect(bgX, bgY, bgWidth, m.iconHeight, 3, new Color(34, 34, 34, 220).getRGB());

            float clickGuiX = m.iconX;
            float clickGuiY = m.iconY - 15;
            float clickGuiWidth = m.iconSWidth;

            float separatorX = clickGuiX + clickGuiWidth;
            float separatorY = clickGuiY;

            float configX = separatorX + separatorWidth;
            float configY = clickGuiY;
            float configWidth = m.iconCWidth;

            int clickGuiColor = new Color(200, 200, 200).getRGB();
            if (HoverUtil.isHovered(mouseX, mouseY, clickGuiX, clickGuiY, clickGuiWidth, m.iconHeight)) {
                clickGuiColor = getGlobalColor().getRGB();
            }

            int configColor = new Color(200, 200, 200).getRGB();
            if (HoverUtil.isHovered(mouseX, mouseY, configX, configY, configWidth, m.iconHeight)) {
                configColor = getGlobalColor().getRGB();
            }

            drawTitleString("ClickGui", clickGuiX, clickGuiY, clickGuiColor);
            drawTitleString(" | ", separatorX, separatorY, new Color(150, 150, 150).getRGB());
            drawTitleString("Config", configX, configY, configColor);
        }

        if (type == GuiEvents.CLICK) {
            float clickGuiX = m.iconX;
            float clickGuiY = m.iconY - 15;
            float clickGuiWidth = m.iconSWidth;

            float configX = clickGuiX + clickGuiWidth + separatorWidth;
            float configY = clickGuiY;
            float configWidth = m.iconCWidth;

            if (HoverUtil.isHovered(mouseX, mouseY, clickGuiX, clickGuiY, clickGuiWidth, m.iconHeight)) {
                isGuiOpen = !isGuiOpen;
                if (!isGuiOpen) {
                    this.dragging = false;
                }
            } else if (HoverUtil.isHovered(mouseX, mouseY, configX, configY, configWidth, m.iconHeight)) {
                isConfigOpen = !isConfigOpen;
            }
        }

        if (type == GuiEvents.RELEASE) {
            this.dragging = false;
            this.draggingSlider = false;
            this.currentDraggingSlider = null;
            this.currentDraggingIntSlider = null;
            this.currentDraggingPercentSlider = null;
            colorPickerStates.values().forEach(state -> {
                state.draggingHue = false;
                state.draggingColor = false;
            });
        }

        if (isConfigOpen) {
            drawConfigInterface(mouseX, mouseY, type);
        }

        if (isGuiOpen) {
            if (type == GuiEvents.RELEASE) {
                this.dragging = false;
                this.draggingSlider = false;
                this.currentDraggingSlider = null;
                this.currentDraggingIntSlider = null;
                this.currentDraggingPercentSlider = null;
                this.expandingRight = false;
                colorPickerStates.values().forEach(state -> {
                    state.draggingHue = false;
                    state.draggingColor = false;
                });
            }

            if (type == GuiEvents.CLICK) {
                if (HoverUtil.isHovered(mouseX, mouseY, posX, posY, width, 17) && mouseButton == 0) {
                    dragging = true;
                    draggingX = mouseX - posX;
                    draggingY = mouseY - posY;
                }

                if (HoverUtil.isHovered(mouseX, mouseY, posX + width - 8, posY + height - 8, 16, 16)) {
                    expandingRight = true;
                }
            }

            if (type == GuiEvents.DRAW) {
                if (expandingRight) {
                    float deltaY = mouseY - (posY + height);
                    height = Math.max(250, height + deltaY);
                    float deltaX = mouseX - (posX + width);
                    width = Math.max(480, width + deltaX);
                }
            }

            if (HoverUtil.isHovered(mouseX, mouseY, posX, posY, width, 17) && dragging && type == GuiEvents.DRAW) {
                posX = mouseX - draggingX;
                posY = mouseY - draggingY;
            }

            if (type == GuiEvents.DRAW) {
                RenderUtil.drawRoundedRect(posX, posY, width, height, 12, new Color(25, 25, 25, 180).getRGB());
                RenderUtil.drawRect(posX, posY, width, 17, new Color(34, 34, 34).getRGB());
                drawTitleString("CLICKGUI", posX + 5, posY + 6, new Color(200, 200, 200).getRGB());
                RenderUtil.drawRect(posX + 110, posY + 0.5f, 2, height, new Color(34, 34, 34).getRGB());
                RenderUtil.drawRect(posX + 110, posY + 50, width - 110 + 0.5f, 2, new Color(34, 34, 34).getRGB());
            }

            float categoryX = posX + 110 + 20;

            for (ModuleCategory category : ModuleCategory.values()) {
                if (type == GuiEvents.DRAW) {
                    boolean isSelected = category == this.selectCategory;
                    boolean isHovered = HoverUtil.isHovered(mouseX, mouseY, categoryX, posY + 24, getNormalStringWidth(category.name.toUpperCase()), getNormalFontHeight());
                    
                    int textColor;
                    if (isSelected) {
                        textColor = getGlobalColor().getRGB();
                    } else if (isHovered) {
                        textColor = new Color(220, 220, 220).getRGB();
                    } else {
                        textColor = new Color(180, 180, 180).getRGB();
                    }
                    
                    drawNormalString(category.name.toUpperCase(), categoryX, posY + 30, textColor);
                    
                    if (isSelected) {
                        targetCategoryLineX = categoryX;
                        if (currentCategoryLineX == 0) {
                            currentCategoryLineX = targetCategoryLineX;
                        }
                    }
                } else if (type == GuiEvents.CLICK) {
                    if (HoverUtil.isHovered(mouseX, mouseY, categoryX, posY + 24, getNormalStringWidth(category.name.toUpperCase()), getNormalFontHeight())) {
                        selectCategory = category;
                        selectModule = null;
                        valueScroll = 0f;
                        moduleScroll = 0f;
                        colorPickerStates.clear();
                        targetCategoryLineX = categoryX;
                    }
                }
                categoryX += getNormalStringWidth(category.name) + 20;
            }
            
            if (type == GuiEvents.DRAW && selectCategory != null) {
                float lineWidth = getNormalStringWidth(selectCategory.name.toUpperCase()) - 0.5f;
                float lineY = posY + 26 + getNormalFontHeight();
                RenderUtil.drawRect(currentCategoryLineX, lineY, lineWidth, 2, getGlobalColor().getRGB());
            }

            if (selectCategory != null) {
                if (type == GuiEvents.DRAW) {
                    Render2DUtil.scissorStart(posX, posY + 16.5f, 110, height - 16.5f);
                }
                if (HoverUtil.isHovered(mouseX, mouseY, posX, posY + 16.5f, 110, height - 16.5f) && type == GuiEvents.DRAW) {
                    moduleScroll = Math.min(0, moduleScroll + Mouse.getDWheel() / 10F);
                }

                float moduleY = posY + 25 + moduleScroll;
                for (Module module : NightSky.moduleManager.getModulesInCategory(selectCategory)) {
                    float moduleHeight = getNormalFontHeight();
                    if (type == GuiEvents.DRAW) {
                        float drawX = posX + 10;
                        if (module == selectModule) {
                            int arrowColor = module.isEnabled() ? this.getGlobalColor().getRGB() : new Color(200, 200, 200).getRGB();
                            drawNormalString(">", drawX, moduleY, arrowColor);
                            drawX += getNormalStringWidth(">") + 2;
                        }

                        drawNormalString(module.getName(), drawX, moduleY, module.isEnabled() ? this.getGlobalColor().getRGB() : new Color(200, 200, 200).getRGB());
                    } else if (type == GuiEvents.CLICK) {
                        if (HoverUtil.isHovered(mouseX, mouseY, posX + 10, moduleY - 4.1f, getNormalStringWidth(module.getName()), moduleHeight)) {
                            switch (mouseButton) {
                                case 0:
                                    module.toggle();
                                    break;
                                case 1:
                                    selectModule = module;
                                    valueScroll = 0f;
                                    colorPickerStates.clear();
                                    break;
                            }
                        }
                    }
                    moduleY += getNormalFontHeight() + 5;
                }
                if (type == GuiEvents.DRAW) {
                    Render2DUtil.scissorEnd();
                }
            }

            if (selectModule != null) {
                float initialValueY = posY + 50;
                float currentY = initialValueY + 10;

                if (type == GuiEvents.DRAW) {
                    drawNormalString(selectModule.getName() + ":", posX + 120, currentY, new Color(200, 200, 200).getRGB());
                }

                currentY += getNormalFontHeight() + 3;

                String keyName = selectModule.getKey() == 0 ? "None" : Keyboard.getKeyName(selectModule.getKey());

                boolean isKeyHovered = HoverUtil.isHovered(
                        mouseX, mouseY,
                        posX + 120,
                        currentY + 1,
                        getNormalStringWidth("Key: " + keyName),
                        getNormalFontHeight()
                );

                boolean isClicking = HoverUtil.isHovered(
                        mouseX, mouseY,
                        posX + 200,
                        currentY + 1,
                        getNormalStringWidth("Hide: " + selectModule.isHidden()),
                        getNormalFontHeight()
                );

                if (type == GuiEvents.DRAW) {
                    int hideTextColor = new Color(150, 150, 150).getRGB();

                    int stateColor;

                    if (selectModule.isHidden()) {
                        stateColor = new Color(0, 180, 0).getRGB();
                    } else {
                        stateColor = new Color(180, 0, 0).getRGB();
                    }

                    if (waitingForKey) {
                        drawNormalString("Key: ...", posX + 120, currentY + 1, getGlobalColor().getRGB());
                    } else {
                        drawNormalString("Key: " + keyName, posX + 120, currentY + 1, hideTextColor);
                    }

                    drawNormalString("Hide: ", posX + 200, currentY + 1, hideTextColor);
                    drawNormalString(
                            String.valueOf(selectModule.isHidden()),
                            posX + 200 + getNormalStringWidth("Hide: "),
                            currentY + 1,
                            stateColor
                    );
                } else if (type == GuiEvents.CLICK) {
                    if (isClicking && mouseButton == 0) {
                        selectModule.setHidden(!selectModule.isHidden());
                    }

                    if (isKeyHovered && mouseButton == 0) {
                        waitingForKey = !waitingForKey;
                    }
                }

                float headerHeight = (initialValueY + 10) - (posY + 50) + getNormalFontHeight() + 3 + getNormalFontHeight() + 10;

                if (HoverUtil.isHovered(mouseX, mouseY, posX + 110 + 1.5F, initialValueY + 1.5f + 1, width - (110 + 1.5F), height - (50 + 1.5f)) && type == GuiEvents.DRAW) {
                    valueScroll = Math.min(0, valueScroll + Mouse.getDWheel() / 10F);
                }

                if (type == GuiEvents.DRAW) {
                    Render2DUtil.scissorStart(posX + 110 + 1.5F + 0.5f, posY + 40 + headerHeight + 1.5f + 1, width - (110 + 1.5F), height - (41 + headerHeight + 1.5f));
                }

                currentY = initialValueY - 4 + headerHeight + valueScroll;

                java.util.ArrayList<Value<?>> moduleValues = NightSky.valueHandler.properties.get(selectModule.getClass());
                if (moduleValues != null) {
                for (Value<?> value : moduleValues) {
                    if (!value.isVisible()) continue;
                    

                    if (value instanceof BooleanValue) {
                        BooleanValue boolValue = (BooleanValue) value;
                        if (type == GuiEvents.DRAW) {
                            drawNormalString(value.getName() + ": ", posX + 120, currentY, new Color(200, 200, 200).getRGB());
                            drawNormalString(boolValue.getValue() ? "true" : "false", posX + 120 + getNormalStringWidth(value.getName() + ": "), currentY, boolValue.getValue() ? new Color(0, 180, 0).getRGB() : new Color(180, 0, 0).getRGB());
                        } else if (type == GuiEvents.CLICK) {
                            if (HoverUtil.isHovered(mouseX, mouseY, posX + 120, currentY, getNormalStringWidth(boolValue.getName() + ": " + (boolValue.getValue() ? "true" : "false")), getNormalFontHeight())) {
                                boolValue.setValue(!boolValue.getValue());
                            }
                        }
                        currentY += getNormalFontHeight() + 5;
                    } else if (value instanceof TextValue) {
                        TextValue textValue = (TextValue) value;
                        float textFieldX = posX + 120 + getNormalStringWidth(value.getName() + ": ");
                        float textFieldY = currentY - (getNormalFontHeight() - getNormalFontHeight()) / 2f - 2.5f;
                        float textFieldWidth = 120;
                        float textFieldHeight = getNormalFontHeight() + 2;
                        TextField textField = textFieldMap.computeIfAbsent(textValue, k -> {
                            TextField tf = new TextField();
                            tf.setText(textValue.getValue());
                            tf.setCursorPositionZero();
                            return tf;
                        });

                        if (type == GuiEvents.DRAW) {
                            drawNormalString(value.getName() + ": ", posX + 120, currentY, new Color(200, 200, 200).getRGB());
                            RenderUtil.drawRect(textFieldX - 1, textFieldY, textFieldWidth + 2, textFieldHeight, new Color(34, 34, 34).getRGB());
                            RenderUtil.drawRect(textFieldX, textFieldY + 1, textFieldWidth, textFieldHeight - 2, new Color(45, 45, 45, 200).getRGB());
                            
                            textField.setXPosition(textFieldX + 4);
                            textField.setYPosition(textFieldY + (textFieldHeight - getNormalFontHeight()) / 2f + 1);
                            textField.setWidth(textFieldWidth - 8);
                            textField.setHeight(textFieldHeight);
                            textField.setEnableBackgroundDrawing(false);
                            textField.updateCursorCounter();
                            
                            String displayText = textField.getText();
                            if (displayText.isEmpty() && !textField.isFocused()) {
                                drawNormalString("Enter text...", textFieldX + 4, textFieldY + (textFieldHeight - getNormalFontHeight()) / 2f + 1, new Color(120, 120, 120).getRGB());
                            } else {
                                textField.drawTextBox();
                            }
                            textValue.setValue(textField.getText());
                        } else if (type == GuiEvents.CLICK) {
                            textField.setXPosition(textFieldX);
                            textField.setYPosition(textFieldY + (textFieldHeight - getNormalFontHeight()) / 2f);
                            textField.setWidth(textFieldWidth - 8);
                            textField.setHeight(textFieldHeight);
                            textField.mouseClicked(mouseX, mouseY, mouseButton);
                        }
                        currentY += getNormalFontHeight() + 5;
                    } else if (value instanceof FloatValue) {
                        FloatValue sliderValue = (FloatValue) value;
                        float sliderX = posX + 120 + getNormalStringWidth(value.getName() + ": ") + 1;
                        float sliderY = currentY - 4;
                        float sliderWidth = 120 - 2;
                        float sliderHeight = 10;
                        if (type == GuiEvents.DRAW) {
                            drawNormalString(value.getName() + ": ", posX + 120, currentY, new Color(200, 200, 200).getRGB());

                            double targetLength = Math.max(0, Math.min(sliderWidth, (sliderValue.getValue() - sliderValue.getMinimum()) / (sliderValue.getMaximum() - sliderValue.getMinimum()) * sliderWidth));
                            double currentLength = numberSettingMap.getOrDefault(sliderValue, (float) targetLength);
                            double animatedLength = AnimationUtil.animate(targetLength, currentLength, 0.2);

                            numberSettingMap.put(sliderValue, (float) animatedLength);

                            RenderUtil.drawRect(sliderX, sliderY, sliderWidth + 1, sliderHeight + 2, new Color(34, 34, 34).getRGB());
                            RenderUtil.drawRect(sliderX + 1, sliderY + 1, (float) animatedLength - 1, sliderHeight - 0.75f, this.getGlobalColor().getRGB());
                            String valueStr = String.valueOf(Math.round(sliderValue.getValue() * 100.0) / 100.0);
                            drawNormalString(
                                    valueStr,
                                    sliderX + sliderWidth / 2 - getNormalStringWidth(valueStr) / 2,
                                    sliderY + sliderHeight / 2 - (float) getNormalFontHeight() / 2 + 4,
                                    new Color(200, 200, 200).getRGB()
                            );
                        } else if (type == GuiEvents.CLICK) {
                            if (mouseButton == 0 && HoverUtil.isHovered(mouseX, mouseY, sliderX, sliderY, sliderWidth, sliderHeight)) {
                                draggingSlider = true;
                                currentDraggingSlider = sliderValue;
                                updateSliderValue(mouseX, sliderX, sliderWidth, sliderValue);
                            }
                        } else if (type == GuiEvents.DRAW && draggingSlider && currentDraggingSlider == sliderValue) {
                            updateSliderValue(mouseX, sliderX, sliderWidth, sliderValue);
                        }
                        currentY += getNormalFontHeight() + 5;
                    } else if (value instanceof IntValue) {
                        IntValue intValue = (IntValue) value;
                        float sliderX = posX + 120 + getNormalStringWidth(value.getName() + ": ") + 1;
                        float sliderY = currentY - 4;
                        float sliderWidth = 120 - 2;
                        float sliderHeight = 10;
                        if (type == GuiEvents.DRAW) {
                            drawNormalString(value.getName() + ": ", posX + 120, currentY, new Color(200, 200, 200).getRGB());

                            double targetLength = Math.max(0, Math.min(sliderWidth, (intValue.getValue() - intValue.getMinimum()) / (double)(intValue.getMaximum() - intValue.getMinimum()) * sliderWidth));
                            double currentLength = numberSettingMap.getOrDefault(intValue, (float) targetLength);
                            double animatedLength = AnimationUtil.animate(targetLength, currentLength, 0.2);

                            numberSettingMap.put(intValue, (float) animatedLength);

                            RenderUtil.drawRect(sliderX, sliderY, sliderWidth + 1, sliderHeight + 2, new Color(34, 34, 34).getRGB());
                            RenderUtil.drawRect(sliderX + 1, sliderY + 1, (float) animatedLength - 1, sliderHeight - 0.75f, this.getGlobalColor().getRGB());
                            String valueStr = String.valueOf(intValue.getValue());
                            drawNormalString(
                                    valueStr,
                                    sliderX + sliderWidth / 2 - getNormalStringWidth(valueStr) / 2,
                                    sliderY + sliderHeight / 2 - (float) getNormalFontHeight() / 2 + 4,
                                    new Color(200, 200, 200).getRGB()
                            );
                        } else if (type == GuiEvents.CLICK) {
                            if (mouseButton == 0 && HoverUtil.isHovered(mouseX, mouseY, sliderX, sliderY, sliderWidth, sliderHeight)) {
                                draggingSlider = true;
                                currentDraggingIntSlider = intValue;
                                updateIntSliderValue(mouseX, sliderX, sliderWidth, intValue);
                            }
                        } else if (type == GuiEvents.DRAW && draggingSlider && currentDraggingIntSlider == intValue) {
                            updateIntSliderValue(mouseX, sliderX, sliderWidth, intValue);
                        }
                        currentY += getNormalFontHeight() + 5;
                    } else if (value instanceof PercentValue) {
                        PercentValue percentValue = (PercentValue) value;
                        float sliderX = posX + 120 + getNormalStringWidth(value.getName() + ": ") + 1;
                        float sliderY = currentY - 4;
                        float sliderWidth = 120 - 2;
                        float sliderHeight = 10;
                        if (type == GuiEvents.DRAW) {
                            drawNormalString(value.getName() + ": ", posX + 120, currentY, new Color(200, 200, 200).getRGB());

                            double targetLength = Math.max(0, Math.min(sliderWidth, (percentValue.getValue() - percentValue.getMinimum()) / (double)(percentValue.getMaximum() - percentValue.getMinimum()) * sliderWidth));
                            double currentLength = numberSettingMap.getOrDefault(percentValue, (float) targetLength);
                            double animatedLength = AnimationUtil.animate(targetLength, currentLength, 0.2);

                            numberSettingMap.put(percentValue, (float) animatedLength);

                            RenderUtil.drawRect(sliderX, sliderY, sliderWidth + 1, sliderHeight + 2, new Color(34, 34, 34).getRGB());
                            RenderUtil.drawRect(sliderX + 1, sliderY + 1, (float) animatedLength - 1, sliderHeight - 0.75f, this.getGlobalColor().getRGB());
                            String valueStr = percentValue.getValue() + "%";
                            drawNormalString(
                                    valueStr,
                                    sliderX + sliderWidth / 2 - getNormalStringWidth(valueStr) / 2,
                                    sliderY + sliderHeight / 2 - (float) getNormalFontHeight() / 2 + 4,
                                    new Color(200, 200, 200).getRGB()
                            );
                        } else if (type == GuiEvents.CLICK) {
                            if (mouseButton == 0 && HoverUtil.isHovered(mouseX, mouseY, sliderX, sliderY, sliderWidth, sliderHeight)) {
                                draggingSlider = true;
                                currentDraggingPercentSlider = percentValue;
                                updatePercentSliderValue(mouseX, sliderX, sliderWidth, percentValue);
                            }
                        } else if (type == GuiEvents.DRAW && draggingSlider && currentDraggingPercentSlider == percentValue) {
                            updatePercentSliderValue(mouseX, sliderX, sliderWidth, percentValue);
                        }
                        currentY += getNormalFontHeight() + 5;
                    } else if (value instanceof ModeValue) {
                        ModeValue modeValue = (ModeValue) value;
                        float modeX = posX + 120 + getNormalStringWidth(value.getName() + ": ");
                        if (type == GuiEvents.DRAW) {
                            drawNormalString(value.getName() + ": ", posX + 120, currentY, new Color(200, 200, 200).getRGB());
                        }
                        float tempY = currentY;
                        for (int i = 0; i < modeValue.getModes().length; i++) {
                            String mode = modeValue.getModes()[i];
                            if (modeX >= (posX + width - 50)) {
                                modeX = posX + 120 + getNormalStringWidth(value.getName() + ": ");
                                tempY += getNormalFontHeight() + 2;
                            }

                            if (type == GuiEvents.DRAW) {
                                if (modeValue.getValue().equals(i)) {
                                    drawNormalString(mode, modeX, tempY, this.getGlobalColor().getRGB());
                                } else {
                                    drawNormalString(mode, modeX, tempY, new Color(200, 200, 200).getRGB());
                                }
                            } else if (type == GuiEvents.CLICK) {
                                if (HoverUtil.isHovered(mouseX, mouseY, modeX, tempY, getNormalStringWidth(mode), getNormalFontHeight())) {
                                    modeValue.setValue(i);
                                }
                            }

                            modeX += getNormalStringWidth(mode);

                            if (i < modeValue.getModes().length - 1) {
                                if (type == GuiEvents.DRAW) {
                                    drawNormalString(", ", modeX, tempY, new Color(200, 200, 200).getRGB());
                                }
                                modeX += getNormalStringWidth(", ");
                            }
                        }
                        currentY = tempY + getNormalFontHeight() + 5;
                    } else if (value instanceof ColorValue) {
                        ColorValue colorValue = (ColorValue) value;
                        float pickerX = posX + 120;
                        ColorPickerState state = colorPickerStates.computeIfAbsent(colorValue, k -> new ColorPickerState());

                        state.pickerX = pickerX;
                        state.pickerY = currentY + getNormalFontHeight();
                        state.hueSliderY = state.pickerY + 60 + 5;

                        int rgb = colorValue.getValue();
                        if ((rgb & 0xFFFFFF) == 0) {
                            rgb = 0xFF8080FF;
                            colorValue.setValue(rgb);
                        }

                        if (type == GuiEvents.DRAW) {
                            drawNormalString(value.getName() + ": ", posX + 120, currentY, new Color(200, 200, 200).getRGB());
                        }

                        {
                            float pickerWidth = 120;
                            float pickerHeight = 60;
                            float hueSliderHeight = 6;

                            if (type == GuiEvents.DRAW) {
                                Color hueColor = Color.getHSBColor(colorValue.getHue(), 1, 1);
                                RenderUtil.drawRect(state.pickerX, state.pickerY, pickerWidth, pickerHeight, hueColor.getRGB());
                                
                                for (int x = 0; x < pickerWidth; x++) {
                                    float saturation = x / pickerWidth;
                                    Color whiteGradient = new Color(255, 255, 255, (int)(255 * (1 - saturation)));
                                    RenderUtil.drawRect(state.pickerX + x, state.pickerY, 1, pickerHeight, whiteGradient.getRGB());
                                }
                                
                                for (int y = 0; y < pickerHeight; y++) {
                                    float brightness = 1 - (y / pickerHeight);
                                    Color blackGradient = new Color(0, 0, 0, (int)(255 * (1 - brightness)));
                                    RenderUtil.drawRect(state.pickerX, state.pickerY + y, pickerWidth, 1, blackGradient.getRGB());
                                }

                                for (float x = 0; x < pickerWidth; x++) {
                                    float hue = x / pickerWidth;
                                    Color c = Color.getHSBColor(hue, 1, 1);
                                    RenderUtil.drawRect(state.pickerX + x, state.hueSliderY, 1, hueSliderHeight, c.getRGB());
                                }


                                float huePos = state.pickerX + (colorValue.getHue() * pickerWidth);
                                RenderUtil.drawRect(huePos - 1, state.hueSliderY - 1, 2, hueSliderHeight + 2, Color.WHITE.getRGB());
                                RenderUtil.drawRect(huePos, state.hueSliderY, 1, hueSliderHeight, Color.BLACK.getRGB());

                                float colorPosX = state.pickerX + (colorValue.getSaturation() * pickerWidth);
                                float colorPosY = state.pickerY + ((1 - colorValue.getBrightness()) * pickerHeight);
                                RenderUtil.drawRect(colorPosX - 2, colorPosY - 2, 4, 4, Color.WHITE.getRGB());
                            } else if (type == GuiEvents.CLICK) {
                                if (mouseButton == 0) {
                                    if (HoverUtil.isHovered(mouseX, mouseY, state.pickerX, state.hueSliderY, 120, 6)) {
                                        state.draggingHue = true;
                                        float hue = MathHelper.clamp_float((mouseX - state.pickerX) / 120, 0, 1);
                                        colorValue.setHue(hue);
                                    } else if (HoverUtil.isHovered(mouseX, mouseY, state.pickerX, state.pickerY, 120, 60)) {
                                        state.draggingColor = true;
                                        float saturation = MathHelper.clamp_float((mouseX - state.pickerX) / 120, 0, 1);
                                        float brightness = 1 - MathHelper.clamp_float((mouseY - state.pickerY) / 60, 0, 1);
                                        colorValue.setSaturation(saturation);
                                        colorValue.setBrightness(brightness);
                                    }
                                }
                            }
                        }
                        currentY += getNormalFontHeight() + 5;
                        currentY += 60 + 6 + 10;
                    }
                }
                if (type == GuiEvents.DRAW) {
                    Render2DUtil.scissorEnd();
                }
                }
            }


            if (type == GuiEvents.DRAW && (configDragging || configResizing)) {
                if (configDragging) {
                    configPosX = mouseX - configDraggingX;
                    configPosY = mouseY - configDraggingY;
                } else if (configResizing) {
                    configWidth = Math.max(400, mouseX - configPosX);
                    configHeight = Math.max(300, mouseY - configPosY);
                }
            }

            if (type == GuiEvents.DRAW) {

                for (Map.Entry<ColorValue, ColorPickerState> entry : colorPickerStates.entrySet()) {
                    ColorValue colorValue = entry.getKey();
                    ColorPickerState state = entry.getValue();
                    if (state.draggingHue) {
                        float hue = MathHelper.clamp_float((mouseX - state.pickerX) / 120, 0, 1);
                        colorValue.setHue(hue);
                    } else if (state.draggingColor) {
                        float saturation = MathHelper.clamp_float((mouseX - state.pickerX) / 120, 0, 1);
                        float brightness = 1 - MathHelper.clamp_float((mouseY - state.pickerY) / 60, 0, 1);
                        colorValue.setSaturation(saturation);
                        colorValue.setBrightness(brightness);
                    }
                }
            }
        }
    }

    private void updateSliderValue(int mouseX, float sliderX, float sliderWidth, FloatValue sliderValue) {
        double min = sliderValue.getMinimum();
        double max = sliderValue.getMaximum();
        double inc = 0.01;

        double rawValue = (mouseX - sliderX) * (max - min) / sliderWidth + min;

        double steppedValue = Math.round(rawValue / inc) * inc;

        double newValue = MathHelper.clamp_double(steppedValue, min, max);

        sliderValue.setValue((float) newValue);
    }

    private void updateIntSliderValue(int mouseX, float sliderX, float sliderWidth, IntValue intValue) {
        int min = intValue.getMinimum();
        int max = intValue.getMaximum();

        double rawValue = (mouseX - sliderX) * (max - min) / sliderWidth + min;
        int newValue = (int) MathHelper.clamp_double(Math.round(rawValue), min, max);

        intValue.setValue(newValue);
    }

    private void updatePercentSliderValue(int mouseX, float sliderX, float sliderWidth, PercentValue percentValue) {
        int min = percentValue.getMinimum();
        int max = percentValue.getMaximum();

        double rawValue = (mouseX - sliderX) * (max - min) / sliderWidth + min;
        int newValue = (int) MathHelper.clamp_double(Math.round(rawValue), min, max);

        percentValue.setValue(newValue);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private IconMeasurements getIconMeasurements() {
        float clickGuiWidth = getTitleStringWidth("ClickGui");
        float configWidth = getTitleStringWidth("Config");
        float separatorWidth = getTitleStringWidth(" | ");
        float totalWidth = clickGuiWidth + separatorWidth + configWidth;
        float iconHeight = 20;
        float iconX = (super.width - totalWidth) / 2;
        float iconY = 20;
        return new IconMeasurements(clickGuiWidth, configWidth, iconHeight, iconX, iconY);
    }

    private java.util.List<String> getAvailableConfigs() {
        java.util.List<String> configs = new java.util.ArrayList<>();
        java.io.File configDir = new java.io.File("./NightSky/");
        if (configDir.exists() && configDir.isDirectory()) {
            java.io.File[] files = configDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (java.io.File file : files) {
                    String configName = file.getName().replace(".json", "");
                    configs.add(configName);
                }
            }
        }
        if (configs.isEmpty()) {
            configs.add("default");
        }
        return configs;
    }

    private void drawConfigInterface(int mouseX, int mouseY, GuiEvents type) {
        if (configPosX == -1) {
            configPosX = super.width / 2f - configWidth / 2;
            configPosY = super.height / 2f - configHeight / 2;
        }

        if (type == GuiEvents.DRAW) {
            RenderUtil.drawRoundedRect(configPosX, configPosY, configWidth, configHeight, 7, new Color(25, 25, 25, 180).getRGB());
            
            float titleBarHeight = 25;
            RenderUtil.drawRoundedRect(configPosX, configPosY, configWidth, titleBarHeight, 7, new Color(35, 35, 35, 200).getRGB());
            RenderUtil.drawRect(configPosX, configPosY + titleBarHeight - 7, configWidth, 7, new Color(35, 35, 35, 200).getRGB());
            
            drawNormalString("Config Manager", configPosX + 10, configPosY + 8, new Color(200, 200, 200).getRGB());
            
            float leftPanelWidth = configWidth * 0.6f;
            float rightPanelWidth = configWidth * 0.4f;
            float contentY = configPosY + titleBarHeight + 10;
            float contentHeight = configHeight - titleBarHeight - 20;
            
            RenderUtil.drawRect(configPosX + 10, contentY, leftPanelWidth - 10, contentHeight, new Color(30, 30, 30, 150).getRGB());
            
            drawNormalString("Available Configs:", configPosX + 15, contentY + 5, new Color(180, 180, 180).getRGB());
            
            java.util.List<String> configs = getAvailableConfigs();
            float itemY = contentY + 25;
            float itemHeight = 20;
            
            for (String config : configs) {
                boolean hovered = HoverUtil.isHovered(mouseX, mouseY, configPosX + 15, itemY, leftPanelWidth - 20, itemHeight);
                boolean selected = config.equals(selectedConfig);
                
                if (selected) {
                    Color selectedColor = new Color(getGlobalColor().getRed(), getGlobalColor().getGreen(), getGlobalColor().getBlue(), 50);
                    RenderUtil.drawRect(configPosX + 15, itemY, leftPanelWidth - 20, itemHeight, selectedColor.getRGB());
                } else if (hovered) {
                    RenderUtil.drawRect(configPosX + 15, itemY, leftPanelWidth - 20, itemHeight, new Color(45, 45, 45, 100).getRGB());
                }
                
                int textColor = selected ? Color.WHITE.getRGB() : (hovered ? getGlobalColor().getRGB() : new Color(180, 180, 180).getRGB());
                drawNormalString(config, configPosX + 20, itemY + 6, textColor);
                itemY += itemHeight + 2;
            }
            
            float buttonX = configPosX + leftPanelWidth + 10;
            float buttonY = contentY + 10;
            float buttonWidth = rightPanelWidth - 20;
            float buttonHeight = 25;
            
            String[] buttons = {"Load", "Save", "Create", "Delete", "Folder"};
            for (String button : buttons) {
                boolean buttonHovered = HoverUtil.isHovered(mouseX, mouseY, buttonX, buttonY, buttonWidth, buttonHeight);
                
                Color buttonColor = buttonHovered ? new Color(60, 60, 60, 180) : new Color(40, 40, 40, 150);
                RenderUtil.drawRoundedRect(buttonX, buttonY, buttonWidth, buttonHeight, 3, buttonColor.getRGB());
                
                int textColor = buttonHovered ? getGlobalColor().getRGB() : new Color(200, 200, 200).getRGB();
                float textX = buttonX + buttonWidth / 2 - getNormalStringWidth(button) / 2;
                drawNormalString(button, textX, buttonY + 8, textColor);
                
                buttonY += buttonHeight + 8;
            }
            
            if (creatingNewConfig) {
                float inputY = buttonY + 10;
                RenderUtil.drawRoundedRect(buttonX, inputY, buttonWidth, buttonHeight, 3, new Color(30, 30, 30, 200).getRGB());
                RenderUtil.drawRoundedRect(buttonX + 1, inputY + 1, buttonWidth - 2, buttonHeight - 2, 2, new Color(20, 20, 20, 150).getRGB());
                
                String displayText = newConfigName.isEmpty() ? "Enter name..." : newConfigName;
                int inputTextColor = newConfigName.isEmpty() ? new Color(120, 120, 120).getRGB() : new Color(200, 200, 200).getRGB();
                drawNormalString(displayText, buttonX + 5, inputY + 8, inputTextColor);
                
                if (System.currentTimeMillis() % 1000 < 500) {
                    float cursorX = buttonX + 5 + getNormalStringWidth(newConfigName);
                    RenderUtil.drawRect(cursorX, inputY + 4, 1, buttonHeight - 8, new Color(200, 200, 200).getRGB());
                }
                
                float confirmY = inputY + buttonHeight + 5;
                boolean confirmHovered = HoverUtil.isHovered(mouseX, mouseY, buttonX, confirmY, buttonWidth / 2 - 2, 20);
                boolean cancelHovered = HoverUtil.isHovered(mouseX, mouseY, buttonX + buttonWidth / 2 + 2, confirmY, buttonWidth / 2 - 2, 20);
                
                Color confirmColor = confirmHovered ? new Color(0, 120, 0, 180) : new Color(0, 80, 0, 150);
                Color cancelColor = cancelHovered ? new Color(120, 0, 0, 180) : new Color(80, 0, 0, 150);
                
                RenderUtil.drawRoundedRect(buttonX, confirmY, buttonWidth / 2 - 2, 20, 2, confirmColor.getRGB());
                RenderUtil.drawRoundedRect(buttonX + buttonWidth / 2 + 2, confirmY, buttonWidth / 2 - 2, 20, 2, cancelColor.getRGB());
                
                float confirmTextX = buttonX + (buttonWidth / 2 - 2) / 2 - getNormalStringWidth("Create") / 2;
                float cancelTextX = buttonX + buttonWidth / 2 + 2 + (buttonWidth / 2 - 2) / 2 - getNormalStringWidth("Cancel") / 2;
                
                drawNormalString("Create", confirmTextX, confirmY + 6, Color.WHITE.getRGB());
                drawNormalString("Cancel", cancelTextX, confirmY + 6, Color.WHITE.getRGB());
            }
            
            float resizeSize = 10;
            RenderUtil.drawRect(configPosX + configWidth - resizeSize, configPosY + configHeight - resizeSize, resizeSize, resizeSize, new Color(60, 60, 60, 100).getRGB());
        }

        if (type == GuiEvents.CLICK) {
            float titleBarHeight = 25;
            
            if (HoverUtil.isHovered(mouseX, mouseY, configPosX, configPosY, configWidth, titleBarHeight)) {
                configDragging = true;
                configDraggingX = mouseX - configPosX;
                configDraggingY = mouseY - configPosY;
            }
            
            float resizeSize = 10;
            if (HoverUtil.isHovered(mouseX, mouseY, configPosX + configWidth - resizeSize, configPosY + configHeight - resizeSize, resizeSize, resizeSize)) {
                configResizing = true;
                configDraggingX = mouseX;
                configDraggingY = mouseY;
            }
            
            float leftPanelWidth = configWidth * 0.6f;
            float contentY = configPosY + titleBarHeight + 10;
            java.util.List<String> configs = getAvailableConfigs();
            float itemY = contentY + 25;
            float itemHeight = 20;
            
            for (String config : configs) {
                if (HoverUtil.isHovered(mouseX, mouseY, configPosX + 15, itemY, leftPanelWidth - 20, itemHeight)) {
                    selectedConfig = config;
                    break;
                }
                itemY += itemHeight + 2;
            }
            
            float buttonX = configPosX + leftPanelWidth + 10;
            float buttonY = contentY + 10;
            float buttonWidth = (configWidth * 0.4f) - 20;
            float buttonHeight = 25;
            
            String[] buttons = {"Load", "Save", "Create", "Delete", "Folder"};
            for (String button : buttons) {
                if (HoverUtil.isHovered(mouseX, mouseY, buttonX, buttonY, buttonWidth, buttonHeight)) {
                    handleConfigAction(button);
                    break;
                }
                buttonY += buttonHeight + 8;
            }
            
            if (creatingNewConfig) {
                float inputY = buttonY + 10;
                float confirmY = inputY + buttonHeight + 5;
                
                if (HoverUtil.isHovered(mouseX, mouseY, buttonX, confirmY, buttonWidth / 2 - 2, 20)) {
                    if (!newConfigName.trim().isEmpty()) {
                        nightsky.config.Config config = new nightsky.config.Config(newConfigName.trim(), true);
                        config.save();
                        selectedConfig = newConfigName.trim();
                    }
                    creatingNewConfig = false;
                    newConfigName = "";
                } else if (HoverUtil.isHovered(mouseX, mouseY, buttonX + buttonWidth / 2 + 2, confirmY, buttonWidth / 2 - 2, 20)) {
                    creatingNewConfig = false;
                    newConfigName = "";
                }
            }
        }


        if (type == GuiEvents.RELEASE) {
            configDragging = false;
            configResizing = false;
        }
    }

    private void handleConfigAction(String action) {
        switch (action) {
            case "Load":
                if (selectedConfig != null) {
                    nightsky.config.Config config = new nightsky.config.Config(selectedConfig, false);
                    config.load();
                }
                break;
            case "Save":
                if (selectedConfig != null) {
                    nightsky.config.Config config = new nightsky.config.Config(selectedConfig, false);
                    config.save();
                }
                break;
            case "Create":
                creatingNewConfig = true;
                newConfigName = "";
                break;
            case "Delete":
                if (selectedConfig != null) {
                    nightsky.config.Config config = new nightsky.config.Config(selectedConfig, false);
                    if (config.file.exists()) {
                        config.file.delete();
                    }
                    selectedConfig = null;
                }
                break;
            case "Folder":
                try {
                    java.awt.Desktop.getDesktop().open(new java.io.File("./NightSky/"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }


    private static class IconMeasurements {
        final float iconSWidth;
        final float iconCWidth;
        final float iconHeight;
        final float iconX;
        final float iconY;
        
        IconMeasurements(float iconSWidth, float iconCWidth, float iconHeight, float iconX, float iconY) {
            this.iconSWidth = iconSWidth;
            this.iconCWidth = iconCWidth;
            this.iconHeight = iconHeight;
            this.iconX = iconX;
            this.iconY = iconY;
        }
    }
}
