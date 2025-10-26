package nightsky.ui.auth;

import nightsky.util.render.RenderUtil;
import nightsky.font.FontRenderer;
import org.lwjgl.input.Keyboard;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

public class CustomTextField {
    private int x, y, width, height;
    private String text = "";
    private String placeholder;
    private boolean focused = false;
    private boolean password = false;
    private int cursorPosition = 0;
    private int selectionEnd = 0;
    private long lastCursorTime = System.currentTimeMillis();
    private boolean cursorVisible = true;
    private float focusAnimation = 0.0f;
    private long lastTime = System.currentTimeMillis();
    
    public CustomTextField(int x, int y, int width, int height, String placeholder) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.placeholder = placeholder;
    }
    
    public void draw(int alpha) {
        updateAnimations();
        
        Color bgColor = new Color(20, 20, 30, (int)(alpha * 0.8f));
        Color borderColor = focused ? 
            new Color(100, 150, 255, (int)(alpha * (0.5f + focusAnimation * 0.5f))) : 
            new Color(60, 60, 80, (int)(alpha * 0.4f));
        
        RenderUtil.drawRoundedRect(x, y, width, height, 6, bgColor);
        RenderUtil.drawRoundedRect(x, y, width, 2, 1, borderColor);
        
        String displayText = password ? repeatString("*", text.length()) : text;
        String renderText = displayText;
        
        if (text.isEmpty() && !focused) {
            renderText = placeholder;
        }
        
        int textX = x + 8;
        int textY = y + height / 2 - 4;
        
        if (text.isEmpty() && !focused) {
            FontRenderer.drawString(renderText, textX, textY, new Color(120, 120, 120, alpha).getRGB());
        } else {
            FontRenderer.drawString(renderText, textX, textY, new Color(255, 255, 255, alpha).getRGB());
            
            if (focused && cursorVisible) {
                String beforeCursor = displayText.substring(0, Math.min(cursorPosition, displayText.length()));
                int cursorX = textX + FontRenderer.getStringWidth(beforeCursor);
                RenderUtil.drawRoundedRect(cursorX, textY - 1, 1, 10, 0, new Color(255, 255, 255, alpha));
            }
        }
    }
    
    private void updateAnimations() {
        long currentTime = System.currentTimeMillis();
        float delta = (currentTime - lastTime) / 1000.0f;
        lastTime = currentTime;
        
        float targetFocus = focused ? 1.0f : 0.0f;
        focusAnimation += (targetFocus - focusAnimation) * delta * 5.0f;
        focusAnimation = Math.max(0.0f, Math.min(1.0f, focusAnimation));
        
        if (currentTime - lastCursorTime > 500) {
            cursorVisible = !cursorVisible;
            lastCursorTime = currentTime;
        }
    }
    
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        boolean wasClicked = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        setFocused(wasClicked);
        
        if (wasClicked && mouseButton == 0) {
            String displayText = password ? repeatString("*", text.length()) : text;
            int relativeX = mouseX - x - 8;
            
            int newCursorPos = 0;
            int currentWidth = 0;
            
            for (int i = 0; i <= displayText.length(); i++) {
                if (currentWidth >= relativeX) {
                    newCursorPos = i;
                    break;
                }
                if (i < displayText.length()) {
                    currentWidth += FontRenderer.getStringWidth(String.valueOf(displayText.charAt(i)));
                }
                newCursorPos = i;
            }
            
            cursorPosition = Math.max(0, Math.min(text.length(), newCursorPos));
            selectionEnd = cursorPosition;
        }
    }
    
    public void keyTyped(char typedChar, int keyCode) {
        if (!focused) return;
        
        if (keyCode == Keyboard.KEY_BACK) {
            if (hasSelection()) {
                deleteSelection();
            } else if (cursorPosition > 0) {
                text = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition);
                cursorPosition--;
                selectionEnd = cursorPosition;
            }
        } else if (keyCode == Keyboard.KEY_DELETE) {
            if (hasSelection()) {
                deleteSelection();
            } else if (cursorPosition < text.length()) {
                text = text.substring(0, cursorPosition) + text.substring(cursorPosition + 1);
            }
        } else if (keyCode == Keyboard.KEY_LEFT) {
            if (cursorPosition > 0) {
                cursorPosition--;
                selectionEnd = cursorPosition;
            }
        } else if (keyCode == Keyboard.KEY_RIGHT) {
            if (cursorPosition < text.length()) {
                cursorPosition++;
                selectionEnd = cursorPosition;
            }
        } else if (keyCode == Keyboard.KEY_HOME) {
            cursorPosition = 0;
            selectionEnd = cursorPosition;
        } else if (keyCode == Keyboard.KEY_END) {
            cursorPosition = text.length();
            selectionEnd = cursorPosition;
        } else if (keyCode == Keyboard.KEY_TAB) {
            return;
        } else if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
            if (keyCode == Keyboard.KEY_V) {
                pasteFromClipboard();
            } else if (keyCode == Keyboard.KEY_C) {
                copyToClipboard();
            } else if (keyCode == Keyboard.KEY_X) {
                cutToClipboard();
            } else if (keyCode == Keyboard.KEY_A) {
                selectAll();
            }
        } else if (isPrintableChar(typedChar)) {
            insertText(String.valueOf(typedChar));
        }
        
        resetCursor();
    }
    
    private boolean isPrintableChar(char c) {
        return c >= 32 && c < 127;
    }
    
    private String repeatString(String str, int count) {
        if (count <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    private void resetCursor() {
        cursorVisible = true;
        lastCursorTime = System.currentTimeMillis();
    }
    
    public void updateCursorCounter() {
        if (focused) {
            resetCursor();
        }
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
        cursorPosition = Math.min(cursorPosition, text.length());
        selectionEnd = cursorPosition;
        resetCursor();
    }
    
    public boolean isFocused() {
        return focused;
    }
    
    public void setFocused(boolean focused) {
        this.focused = focused;
        if (focused) {
            resetCursor();
        }
    }
    
    public void setPassword(boolean password) {
        this.password = password;
    }
    
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    private void insertText(String insertText) {
        if (hasSelection()) {
            deleteSelection();
        }
        text = text.substring(0, cursorPosition) + insertText + text.substring(cursorPosition);
        cursorPosition += insertText.length();
        selectionEnd = cursorPosition;
    }
    
    private void pasteFromClipboard() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            String clipboardText = (String) clipboard.getData(DataFlavor.stringFlavor);
            if (clipboardText != null) {
                insertText(clipboardText);
            }
        } catch (Exception e) {
        }
    }
    
    private void copyToClipboard() {
        if (hasSelection()) {
            try {
                String selectedText = getSelectedText();
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(selectedText), null);
            } catch (Exception e) {
            }
        }
    }
    
    private void cutToClipboard() {
        if (hasSelection()) {
            copyToClipboard();
            deleteSelection();
        }
    }
    
    private void selectAll() {
        cursorPosition = 0;
        selectionEnd = text.length();
    }
    
    private boolean hasSelection() {
        return cursorPosition != selectionEnd;
    }
    
    private String getSelectedText() {
        int start = Math.min(cursorPosition, selectionEnd);
        int end = Math.max(cursorPosition, selectionEnd);
        return text.substring(start, end);
    }
    
    private void deleteSelection() {
        int start = Math.min(cursorPosition, selectionEnd);
        int end = Math.max(cursorPosition, selectionEnd);
        text = text.substring(0, start) + text.substring(end);
        cursorPosition = start;
        selectionEnd = start;
    }
}
