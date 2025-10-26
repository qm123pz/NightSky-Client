package nightsky.ui.clickgui.panel;

import nightsky.ui.Screen;
import nightsky.module.Module;
import nightsky.util.misc.HoverUtil;
import nightsky.util.render.Render2DUtil;
import nightsky.value.Value;
import nightsky.value.values.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.List;

public class SettingPanel implements Screen {
    private final Minecraft mc = Minecraft.getMinecraft();
    private float x, y;
    private Module module;
    private FontRenderer fontRenderer;
    private Module keyChangeModule;
    private TextValue editingTextValue;
    private String editingText = "";
    private float scrollOffset = 0f;
    private float maxScroll = 0f;

    @Override
    public void initGui() {
        fontRenderer = mc.fontRendererObj;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        if (module == null) return;

        final float xOffset = x + 120;
        float yOffset = y + fontRenderer.FONT_HEIGHT + fontRenderer.FONT_HEIGHT + 10;
        
        handleScrolling(mouseX, mouseY);
        
        final Color keywordColor = new Color(204, 120, 50);
        final Color textColor = new Color(186, 186, 186);
        final Color commentColor = new Color(128, 128, 128);
        final Color numberColor = new Color(104, 150, 186);
        final Color variableColor = new Color(152, 118, 170);

        float clipX = x + 100;
        float clipY = y + fontRenderer.FONT_HEIGHT + fontRenderer.FONT_HEIGHT + 10;
        float clipWidth = 400;
        float clipHeight = 320;
        Render2DUtil.scissorStart(clipX, clipY, clipWidth, clipHeight);

        float startY = yOffset;
        yOffset -= scrollOffset;
        
        String enableText1 = "public boolean ";
        String enableText2 = "enabled";
        String enableText3 = " = ";
        String enableText4 = module.isEnabled() + ";";
        
        float currentX = xOffset;
        fontRenderer.drawString(enableText1, (int)currentX, (int)yOffset, keywordColor.getRGB());
        currentX += fontRenderer.getStringWidth(enableText1);
        fontRenderer.drawString(enableText2, (int)currentX, (int)yOffset, variableColor.getRGB());
        currentX += fontRenderer.getStringWidth(enableText2);
        fontRenderer.drawString(enableText3, (int)currentX, (int)yOffset, textColor.getRGB());
        currentX += fontRenderer.getStringWidth(enableText3);
        fontRenderer.drawString(enableText4, (int)currentX, (int)yOffset, keywordColor.getRGB());
        
        yOffset += fontRenderer.FONT_HEIGHT + 2;

        String keyText1, keyText2 = "", keyText3 = "", keyText4 = "", keyText5 = "", keyText6 = "";
        
        currentX = xOffset;
        if (keyChangeModule == module) {
            keyText1 = "public int key = Listening...";
            fontRenderer.drawString(keyText1, (int)currentX, (int)yOffset, keywordColor.getRGB());
        } else {
            keyText2 = "public int ";
            keyText3 = "key ";
            keyText4 = "= ";
            keyText5 = "Keyboard.";
            keyText6 = Keyboard.getKeyName(module.getKey()) + ";";
            
            fontRenderer.drawString(keyText2, (int)currentX, (int)yOffset, keywordColor.getRGB());
            currentX += fontRenderer.getStringWidth(keyText2);
            fontRenderer.drawString(keyText3, (int)currentX, (int)yOffset, variableColor.getRGB());
            currentX += fontRenderer.getStringWidth(keyText3);
            fontRenderer.drawString(keyText4, (int)currentX, (int)yOffset, textColor.getRGB());
            currentX += fontRenderer.getStringWidth(keyText4);
            fontRenderer.drawString(keyText5, (int)currentX, (int)yOffset, textColor.getRGB());
            currentX += fontRenderer.getStringWidth(keyText5);
            fontRenderer.drawString(keyText6, (int)currentX, (int)yOffset, variableColor.getRGB());
        }

        yOffset += fontRenderer.FONT_HEIGHT + 6;
        List<Value<?>> values = getModuleValues(module);
        
        if (values.isEmpty()) {
            fontRenderer.drawString("No settings found for " + module.getName(), 
                (int)xOffset, (int)yOffset, textColor.getRGB());
            Render2DUtil.scissorEnd();
            return;
        }
        for (Value<?> value : values) {
            if (value instanceof BooleanValue) {
                drawBooleanValue((BooleanValue) value, xOffset, yOffset, keywordColor, variableColor, textColor);
            } else if (value instanceof FloatValue) {
                drawFloatValue((FloatValue) value, xOffset, yOffset, mouseX, mouseY, 
                    keywordColor, variableColor, textColor, numberColor, commentColor);
            } else if (value instanceof IntValue) {
                drawIntValue((IntValue) value, xOffset, yOffset, mouseX, mouseY, 
                    keywordColor, variableColor, textColor, numberColor, commentColor);
            } else if (value instanceof ModeValue) {
                drawModeValue((ModeValue) value, xOffset, yOffset, 
                    keywordColor, variableColor, textColor, commentColor);
            } else if (value instanceof ColorValue) {
                float extraLines = drawColorValue((ColorValue) value, x, xOffset, yOffset, mouseX, mouseY, 
                    keywordColor, variableColor, textColor, numberColor);
                yOffset += extraLines;
                continue;
            } else if (value instanceof PercentValue) {
                drawPercentValue((PercentValue) value, xOffset, yOffset, mouseX, mouseY,
                    keywordColor, variableColor, textColor, numberColor, commentColor);
            } else if (value instanceof TextValue) {
                drawTextValue((TextValue) value, xOffset, yOffset, mouseX, mouseY,
                    keywordColor, variableColor, textColor);
            }
            yOffset += fontRenderer.FONT_HEIGHT + 4;
        }
        
        float enabledKeyHeight = (fontRenderer.FONT_HEIGHT + 2) + (fontRenderer.FONT_HEIGHT + 6);
        float contentHeight = (yOffset - startY) + enabledKeyHeight;
        maxScroll = Math.max(0, contentHeight - clipHeight + 120);
        
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        Render2DUtil.scissorEnd();
    }

    private void drawBooleanValue(BooleanValue booleanValue, float xOffset, float yOffset,
                                Color keywordColor, Color variableColor, Color textColor) {
        String s1 = "public boolean ";
        String s2 = booleanValue.getName();
        String s3 = " = ";
        String s4 = booleanValue.getValue() + ";";
        
        float currentX = xOffset;
        fontRenderer.drawString(s1, (int)currentX, (int)yOffset, keywordColor.getRGB());
        currentX += fontRenderer.getStringWidth(s1);
        fontRenderer.drawString(s2, (int)currentX, (int)yOffset, variableColor.getRGB());
        currentX += fontRenderer.getStringWidth(s2);
        fontRenderer.drawString(s3, (int)currentX, (int)yOffset, textColor.getRGB());
        currentX += fontRenderer.getStringWidth(s3);
        fontRenderer.drawString(s4, (int)currentX, (int)yOffset, keywordColor.getRGB());
    }

    private void drawFloatValue(FloatValue floatValue, float xOffset, float yOffset, int mouseX, int mouseY,
                              Color keywordColor, Color variableColor, Color textColor, Color numberColor, Color commentColor) {
        float val = Math.min(Math.max(floatValue.getMinimum(), floatValue.getValue()), floatValue.getMaximum());
        String s1 = "public float ";
        String s2 = floatValue.getName();
        String s3 = " = ";
        String s4 = String.valueOf(val);
        String s5 = ";";
        String s6 = "//" + floatValue.getName() + ".range(" + floatValue.getMinimum() + "," + floatValue.getMaximum() + ")";
        
        float currentX = xOffset;
        fontRenderer.drawString(s1, (int)currentX, (int)yOffset, keywordColor.getRGB());
        currentX += fontRenderer.getStringWidth(s1);
        fontRenderer.drawString(s2, (int)currentX, (int)yOffset, variableColor.getRGB());
        currentX += fontRenderer.getStringWidth(s2);
        fontRenderer.drawString(s3, (int)currentX, (int)yOffset, textColor.getRGB());
        currentX += fontRenderer.getStringWidth(s3);
        fontRenderer.drawString(s4, (int)currentX, (int)yOffset, numberColor.getRGB());
        currentX += fontRenderer.getStringWidth(s4);
        fontRenderer.drawString(s5, (int)currentX, (int)yOffset, keywordColor.getRGB());
        currentX += fontRenderer.getStringWidth(s5);
        fontRenderer.drawString(s6, (int)currentX, (int)yOffset, commentColor.getRGB());
        
        float width = fontRenderer.getStringWidth(s1 + s2 + s3 + s4 + s5 + s6);
        boolean hoverValue = HoverUtil.isHovered(mouseX, mouseY, xOffset, yOffset, width, fontRenderer.FONT_HEIGHT);
        
        if (hoverValue) {
            int wheelDelta = Mouse.getDWheel();
            if (wheelDelta < 0) {
                floatValue.setValue(val - 0.1f);
            } else if (wheelDelta > 0) {
                floatValue.setValue(val + 0.1f);
            }
        }
    }

    private void drawIntValue(IntValue intValue, float xOffset, float yOffset, int mouseX, int mouseY,
                            Color keywordColor, Color variableColor, Color textColor, Color numberColor, Color commentColor) {
        int val = Math.min(Math.max(intValue.getMinimum(), intValue.getValue()), intValue.getMaximum());
        String s1 = "public int ";
        String s2 = intValue.getName();
        String s3 = " = ";
        String s4 = String.valueOf(val);
        String s5 = ";";
        String s6 = "//" + intValue.getName() + ".range(" + intValue.getMinimum() + "," + intValue.getMaximum() + ")";
        
        float currentX = xOffset;
        fontRenderer.drawString(s1, (int)currentX, (int)yOffset, keywordColor.getRGB());
        currentX += fontRenderer.getStringWidth(s1);
        fontRenderer.drawString(s2, (int)currentX, (int)yOffset, variableColor.getRGB());
        currentX += fontRenderer.getStringWidth(s2);
        fontRenderer.drawString(s3, (int)currentX, (int)yOffset, textColor.getRGB());
        currentX += fontRenderer.getStringWidth(s3);
        fontRenderer.drawString(s4, (int)currentX, (int)yOffset, numberColor.getRGB());
        currentX += fontRenderer.getStringWidth(s4);
        fontRenderer.drawString(s5, (int)currentX, (int)yOffset, keywordColor.getRGB());
        currentX += fontRenderer.getStringWidth(s5);
        fontRenderer.drawString(s6, (int)currentX, (int)yOffset, commentColor.getRGB());
        
        float width = fontRenderer.getStringWidth(s1 + s2 + s3 + s4 + s5 + s6);
        boolean hoverValue = HoverUtil.isHovered(mouseX, mouseY, xOffset, yOffset, width, fontRenderer.FONT_HEIGHT);
        
        if (hoverValue) {
            int wheelDelta = Mouse.getDWheel();
            if (wheelDelta < 0) {
                intValue.setValue(val - 1);
            } else if (wheelDelta > 0) {
                intValue.setValue(val + 1);
            }
        }
    }

    private void drawModeValue(ModeValue modeValue, float xOffset, float yOffset,
                             Color keywordColor, Color variableColor, Color textColor, Color commentColor) {
        String s1 = "public String ";
        String s2 = modeValue.getName();
        String s3 = " = ";
        String s4 = "\"" + modeValue.getModeString() + "\"";
        String s5 = ";";
        String s6 = "//modes: " + java.util.Arrays.toString(modeValue.getModes());
        
        float currentX = xOffset;
        fontRenderer.drawString(s1, (int)currentX, (int)yOffset, keywordColor.getRGB());
        currentX += fontRenderer.getStringWidth(s1);
        fontRenderer.drawString(s2, (int)currentX, (int)yOffset, variableColor.getRGB());
        currentX += fontRenderer.getStringWidth(s2);
        fontRenderer.drawString(s3, (int)currentX, (int)yOffset, textColor.getRGB());
        currentX += fontRenderer.getStringWidth(s3);
        fontRenderer.drawString(s4, (int)currentX, (int)yOffset, variableColor.getRGB());
        currentX += fontRenderer.getStringWidth(s4);
        fontRenderer.drawString(s5, (int)currentX, (int)yOffset, keywordColor.getRGB());
        currentX += fontRenderer.getStringWidth(s5);
        fontRenderer.drawString(s6, (int)currentX, (int)yOffset, commentColor.getRGB());
    }

    private float drawColorValue(ColorValue colorValue, float mainX, float xOffset, float yOffset, int mouseX, int mouseY,
                              Color keywordColor, Color variableColor, Color textColor, Color numberColor) {
        int color = colorValue.getValue();
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        
        Render2DUtil.drawRect(mainX + 100 + 10 - fontRenderer.FONT_HEIGHT / 2f, yOffset, 
            fontRenderer.FONT_HEIGHT, fontRenderer.FONT_HEIGHT, color);
        
        String redText1 = "public ";
        String redText2 = colorValue.getName() + "Red";
        String redText3 = " = ";
        String redText4 = red + ";";
        
        float currentX = xOffset;
        fontRenderer.drawString(redText1, (int)currentX, (int)yOffset, keywordColor.getRGB());
        currentX += fontRenderer.getStringWidth(redText1);
        fontRenderer.drawString(redText2, (int)currentX, (int)yOffset, variableColor.getRGB());
        currentX += fontRenderer.getStringWidth(redText2);
        fontRenderer.drawString(redText3, (int)currentX, (int)yOffset, textColor.getRGB());
        currentX += fontRenderer.getStringWidth(redText3);
        fontRenderer.drawString(redText4, (int)currentX, (int)yOffset, numberColor.getRGB());
        
        boolean hoverRed = HoverUtil.isHovered(mouseX, mouseY, xOffset, yOffset, fontRenderer.getStringWidth(redText1 + redText2 + redText3 + redText4), fontRenderer.FONT_HEIGHT);
        if (hoverRed) {
            int wheelDelta = Mouse.getDWheel();
            if (wheelDelta < 0 && red > 0) {
                red = Math.max(0, red - 5);
                colorValue.setValue((red << 16) | (green << 8) | blue);
            } else if (wheelDelta > 0 && red < 255) {
                red = Math.min(255, red + 5);
                colorValue.setValue((red << 16) | (green << 8) | blue);
            }
        }
        yOffset += fontRenderer.FONT_HEIGHT + 4;
        
        String greenText1 = "public ";
        String greenText2 = colorValue.getName() + "Green";
        String greenText3 = " = ";
        String greenText4 = green + ";";
        
        currentX = xOffset;
        fontRenderer.drawString(greenText1, (int)currentX, (int)yOffset, keywordColor.getRGB());
        currentX += fontRenderer.getStringWidth(greenText1);
        fontRenderer.drawString(greenText2, (int)currentX, (int)yOffset, variableColor.getRGB());
        currentX += fontRenderer.getStringWidth(greenText2);
        fontRenderer.drawString(greenText3, (int)currentX, (int)yOffset, textColor.getRGB());
        currentX += fontRenderer.getStringWidth(greenText3);
        fontRenderer.drawString(greenText4, (int)currentX, (int)yOffset, numberColor.getRGB());
        
        boolean hoverGreen = HoverUtil.isHovered(mouseX, mouseY, xOffset, yOffset, fontRenderer.getStringWidth(greenText1 + greenText2 + greenText3 + greenText4), fontRenderer.FONT_HEIGHT);
        if (hoverGreen) {
            int wheelDelta = Mouse.getDWheel();
            if (wheelDelta < 0 && green > 0) {
                green = Math.max(0, green - 5);
                colorValue.setValue((red << 16) | (green << 8) | blue);
            } else if (wheelDelta > 0 && green < 255) {
                green = Math.min(255, green + 5);
                colorValue.setValue((red << 16) | (green << 8) | blue);
            }
        }
        yOffset += fontRenderer.FONT_HEIGHT + 4;
        
        String blueText1 = "public ";
        String blueText2 = colorValue.getName() + "Blue";
        String blueText3 = " = ";
        String blueText4 = blue + ";";
        
        currentX = xOffset;
        fontRenderer.drawString(blueText1, (int)currentX, (int)yOffset, keywordColor.getRGB());
        currentX += fontRenderer.getStringWidth(blueText1);
        fontRenderer.drawString(blueText2, (int)currentX, (int)yOffset, variableColor.getRGB());
        currentX += fontRenderer.getStringWidth(blueText2);
        fontRenderer.drawString(blueText3, (int)currentX, (int)yOffset, textColor.getRGB());
        currentX += fontRenderer.getStringWidth(blueText3);
        fontRenderer.drawString(blueText4, (int)currentX, (int)yOffset, numberColor.getRGB());
        
        boolean hoverBlue = HoverUtil.isHovered(mouseX, mouseY, xOffset, yOffset, fontRenderer.getStringWidth(blueText1 + blueText2 + blueText3 + blueText4), fontRenderer.FONT_HEIGHT);
        if (hoverBlue) {
            int wheelDelta = Mouse.getDWheel();
            if (wheelDelta < 0 && blue > 0) {
                blue = Math.max(0, blue - 5);
                colorValue.setValue((red << 16) | (green << 8) | blue);
            } else if (wheelDelta > 0 && blue < 255) {
                blue = Math.min(255, blue + 5);
                colorValue.setValue((red << 16) | (green << 8) | blue);
            }
        }
        
        return 3 * (fontRenderer.FONT_HEIGHT + 4);
    }

    private void drawTextValue(TextValue textValue, float xOffset, float yOffset, int mouseX, int mouseY,
                             Color keywordColor, Color variableColor, Color textColor) {
        String s1 = "public String ";
        String s2 = textValue.getName();
        String s3 = " = ";
        String s4, s5;
        
        if (editingTextValue == textValue) {
            s4 = "\"" + editingText + "\"";
            s5 = "; //editing...";
        } else {
            s4 = "\"" + textValue.getValue() + "\"";
            s5 = ";";
        }
        
        float currentX = xOffset;
        fontRenderer.drawString(s1, (int)currentX, (int)yOffset, keywordColor.getRGB());
        currentX += fontRenderer.getStringWidth(s1);
        fontRenderer.drawString(s2, (int)currentX, (int)yOffset, variableColor.getRGB());
        currentX += fontRenderer.getStringWidth(s2);
        fontRenderer.drawString(s3, (int)currentX, (int)yOffset, textColor.getRGB());
        currentX += fontRenderer.getStringWidth(s3);
        fontRenderer.drawString(s4, (int)currentX, (int)yOffset, variableColor.getRGB());
        currentX += fontRenderer.getStringWidth(s4);
        fontRenderer.drawString(s5, (int)currentX, (int)yOffset, keywordColor.getRGB());
    }

    private void drawPercentValue(PercentValue percentValue, float xOffset, float yOffset, int mouseX, int mouseY,
                                Color keywordColor, Color variableColor, Color textColor, Color numberColor, Color commentColor) {
        int val = Math.min(Math.max(percentValue.getMinimum(), percentValue.getValue()), percentValue.getMaximum());
        String s1 = "public int ";
        String s2 = percentValue.getName();
        String s3 = " = ";
        String s4 = String.valueOf(val) + "%";
        String s5 = ";";
        String s6 = "//" + percentValue.getName() + ".range(" + percentValue.getMinimum() + "%-" + percentValue.getMaximum() + "%)";
        
        float currentX = xOffset;
        fontRenderer.drawString(s1, (int)currentX, (int)yOffset, keywordColor.getRGB());
        currentX += fontRenderer.getStringWidth(s1);
        fontRenderer.drawString(s2, (int)currentX, (int)yOffset, variableColor.getRGB());
        currentX += fontRenderer.getStringWidth(s2);
        fontRenderer.drawString(s3, (int)currentX, (int)yOffset, textColor.getRGB());
        currentX += fontRenderer.getStringWidth(s3);
        fontRenderer.drawString(s4, (int)currentX, (int)yOffset, numberColor.getRGB());
        currentX += fontRenderer.getStringWidth(s4);
        fontRenderer.drawString(s5, (int)currentX, (int)yOffset, keywordColor.getRGB());
        currentX += fontRenderer.getStringWidth(s5);
        fontRenderer.drawString(s6, (int)currentX, (int)yOffset, commentColor.getRGB());
        
        float width = fontRenderer.getStringWidth(s1 + s2 + s3 + s4 + s5 + s6);
        boolean hoverValue = HoverUtil.isHovered(mouseX, mouseY, xOffset, yOffset, width, fontRenderer.FONT_HEIGHT);
        
        if (hoverValue) {
            int wheelDelta = Mouse.getDWheel();
            if (wheelDelta < 0) {
                percentValue.setValue(val - 5);
            } else if (wheelDelta > 0) {
                percentValue.setValue(val + 5);
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (module == null) return;
        
        if (button == 0) {
            float xOffset = x + 120;
            float yOffset = y + fontRenderer.FONT_HEIGHT + fontRenderer.FONT_HEIGHT + 10 - scrollOffset;
            
            String enableText = "public boolean enabled = " + module.isEnabled() + ";";
            float enableWidth = fontRenderer.getStringWidth(enableText);
            if (HoverUtil.isHovered(mouseX, mouseY, xOffset, yOffset, enableWidth, fontRenderer.FONT_HEIGHT)) {
                module.toggle();
                return;
            }
            
            yOffset += fontRenderer.FONT_HEIGHT + 2;
            String keyBindText = keyChangeModule == module ? "public int key = Listening..." : 
                "public int key = Keyboard." + Keyboard.getKeyName(module.getKey()) + ";";
            float keyBindWidth = fontRenderer.getStringWidth(keyBindText);
            
            if (HoverUtil.isHovered(mouseX, mouseY, xOffset, yOffset, keyBindWidth, fontRenderer.FONT_HEIGHT)) {
                keyChangeModule = (keyChangeModule == module) ? null : module;
                return;
            }
        }
        
        List<Value<?>> values = getModuleValues(module);
        float yOffset = y + fontRenderer.FONT_HEIGHT + fontRenderer.FONT_HEIGHT + 10 + (fontRenderer.FONT_HEIGHT + 2) + (fontRenderer.FONT_HEIGHT + 6) - scrollOffset;
        
        for (Value<?> value : values) {
            float xOffset = x + 120;
            
            if (value instanceof BooleanValue) {
                String fullText = "public boolean " + value.getName() + " = " + ((BooleanValue) value).getValue() + ";";
                float width = fontRenderer.getStringWidth(fullText);
                
                if (HoverUtil.isHovered(mouseX, mouseY, xOffset, yOffset, width, fontRenderer.FONT_HEIGHT)) {
                    BooleanValue boolValue = (BooleanValue) value;
                    boolValue.setValue(!boolValue.getValue());
                }
            } else if (value instanceof ModeValue) {
                ModeValue modeValue = (ModeValue) value;
                String fullText = "public String " + value.getName() + " = \"" + modeValue.getModeString() + "\";";
                float width = fontRenderer.getStringWidth(fullText);
                
                if (HoverUtil.isHovered(mouseX, mouseY, xOffset, yOffset, width, fontRenderer.FONT_HEIGHT)) {
                        int currentIndex = modeValue.getValue();
                    int nextIndex = (currentIndex + 1) % modeValue.getModes().length;
                    modeValue.setValue(nextIndex);
                }
            } else if (value instanceof ColorValue) {
                yOffset += 3 * (fontRenderer.FONT_HEIGHT + 4);
                continue;
            } else if (value instanceof TextValue) {
                TextValue textValue = (TextValue) value;
                String s1 = "public String ";
                String s2 = textValue.getName();
                String s3 = " = ";
                String s4 = "\"" + textValue.getValue() + "\"";
                String s5 = ";";
                String fullText = s1 + s2 + s3 + s4 + s5;
                float width = fontRenderer.getStringWidth(fullText);
                
                if (HoverUtil.isHovered(mouseX, mouseY, xOffset, yOffset, width, fontRenderer.FONT_HEIGHT)) {
                    if (editingTextValue == textValue) {
                        editingTextValue = null;
                        textValue.setValue(editingText);
                        editingText = "";
                    } else {
                        editingTextValue = textValue;
                        editingText = textValue.getValue();
                    }
                }
            }
            
            yOffset += fontRenderer.FONT_HEIGHT + 4;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (keyChangeModule != null) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                keyChangeModule = null;
            } else if (keyCode != Keyboard.KEY_NONE) {
                keyChangeModule.setKey(keyCode);
                keyChangeModule = null;
            }
        } else if (editingTextValue != null) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                editingTextValue = null;
                editingText = "";
            } else if (keyCode == Keyboard.KEY_RETURN) {
                editingTextValue.setValue(editingText);
                editingTextValue = null;
                editingText = "";
            } else if (keyCode == Keyboard.KEY_BACK) {
                if (editingText.length() > 0) {
                    editingText = editingText.substring(0, editingText.length() - 1);
                }
            } else if (Character.isLetterOrDigit(typedChar) || Character.isSpaceChar(typedChar) || 
                      typedChar == '.' || typedChar == ',' || typedChar == '-' || typedChar == '_' || 
                      typedChar == '!' || typedChar == '?' || typedChar == ':' || typedChar == ';') {
                editingText += typedChar;
            }
        }
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    private List<Value<?>> getModuleValues(Module module) {
        List<Value<?>> values = new java.util.ArrayList<>();
        
        try {
            java.lang.reflect.Field[] fields = module.getClass().getDeclaredFields();
            
            for (java.lang.reflect.Field field : fields) {
                if (Value.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    Value<?> value = (Value<?>) field.get(module);
                    if (value != null) {
                        values.add(value);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to get module values: " + e.getMessage());
        }
        
        return values;
    }

    private void handleScrolling(int mouseX, int mouseY) {
        float panelX = x + 100;
        float panelY = y + fontRenderer.FONT_HEIGHT + fontRenderer.FONT_HEIGHT + 10;
        float panelWidth = 400;
        float panelHeight = 300 - (fontRenderer.FONT_HEIGHT + fontRenderer.FONT_HEIGHT + 10);
        
        boolean isInPanel = HoverUtil.isHovered(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight);
        
        if (isInPanel) {
            boolean isHoveringValue = isHoveringAdjustableValue(mouseX, mouseY);
            
            if (!isHoveringValue) {
                int wheelDelta = Mouse.getDWheel();
                if (wheelDelta != 0) {
                    float scrollSpeed = 20f;
                    if (wheelDelta > 0) {
                        scrollOffset -= scrollSpeed;
                    } else if (wheelDelta < 0) {
                        scrollOffset += scrollSpeed;
                    }
                    scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
                }
            }
        }
    }
    
    private boolean isHoveringAdjustableValue(int mouseX, int mouseY) {
        if (module == null) return false;
        
        List<Value<?>> values = getModuleValues(module);
        float xOffset = x + 120;
        float yOffset = y + fontRenderer.FONT_HEIGHT + fontRenderer.FONT_HEIGHT + 10 + (fontRenderer.FONT_HEIGHT + 2) + (fontRenderer.FONT_HEIGHT + 6) - scrollOffset;
        
        for (Value<?> value : values) {
            if (value instanceof FloatValue || value instanceof IntValue || value instanceof PercentValue) {
                String fullText = "";
                if (value instanceof FloatValue) {
                    FloatValue floatValue = (FloatValue) value;
                    float val = Math.min(Math.max(floatValue.getMinimum(), floatValue.getValue()), floatValue.getMaximum());
                    fullText = "public float " + value.getName() + " = " + val + ";//" + value.getName() + ".range(" + floatValue.getMinimum() + "," + floatValue.getMaximum() + ")";
                } else if (value instanceof IntValue) {
                    IntValue intValue = (IntValue) value;
                    int val = Math.min(Math.max(intValue.getMinimum(), intValue.getValue()), intValue.getMaximum());
                    fullText = "public int " + value.getName() + " = " + val + ";//" + value.getName() + ".range(" + intValue.getMinimum() + "," + intValue.getMaximum() + ")";
                } else if (value instanceof PercentValue) {
                    PercentValue percentValue = (PercentValue) value;
                    int val = Math.min(Math.max(percentValue.getMinimum(), percentValue.getValue()), percentValue.getMaximum());
                    fullText = "public int " + value.getName() + " = " + val + "%;//" + value.getName() + ".range(" + percentValue.getMinimum() + "%-" + percentValue.getMaximum() + "%)";
                }
                
                float width = fontRenderer.getStringWidth(fullText);
                if (HoverUtil.isHovered(mouseX, mouseY, xOffset, yOffset, width, fontRenderer.FONT_HEIGHT)) {
                    return true;
                }
                yOffset += fontRenderer.FONT_HEIGHT + 4;
            } else if (value instanceof ColorValue) {
                ColorValue colorValue = (ColorValue) value;
                int color = colorValue.getValue();
                int red = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int blue = color & 0xFF;
                
                String redText = "public " + colorValue.getName() + "Red = " + red + ";";
                if (HoverUtil.isHovered(mouseX, mouseY, xOffset, yOffset, fontRenderer.getStringWidth(redText), fontRenderer.FONT_HEIGHT)) {
                    return true;
                }
                yOffset += fontRenderer.FONT_HEIGHT + 4;
                
                String greenText = "public " + colorValue.getName() + "Green = " + green + ";";
                if (HoverUtil.isHovered(mouseX, mouseY, xOffset, yOffset, fontRenderer.getStringWidth(greenText), fontRenderer.FONT_HEIGHT)) {
                    return true;
                }
                yOffset += fontRenderer.FONT_HEIGHT + 4;
                
                String blueText = "public " + colorValue.getName() + "Blue = " + blue + ";";
                if (HoverUtil.isHovered(mouseX, mouseY, xOffset, yOffset, fontRenderer.getStringWidth(blueText), fontRenderer.FONT_HEIGHT)) {
                    return true;
                }
                yOffset += fontRenderer.FONT_HEIGHT + 4;
            } else {
                yOffset += fontRenderer.FONT_HEIGHT + 4;
            }
        }
        return false;
    }
}
