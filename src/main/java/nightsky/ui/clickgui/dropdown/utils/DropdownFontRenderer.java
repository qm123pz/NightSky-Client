package nightsky.ui.clickgui.dropdown.utils;

import nightsky.font.CustomFontRenderer;
import java.awt.*;
import java.io.InputStream;

public class DropdownFontRenderer {
    
    private static Font opensansSemibold35;
    private static Font opensansSemibold20;
    private static Font opensansSemibold18;
    
    static {
        try {
            InputStream is = DropdownFontRenderer.class.getResourceAsStream("/assets/minecraft/nightsky/font/OpenSans-SemiBold.ttf");
            if (is != null) {
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
                opensansSemibold35 = baseFont.deriveFont(Font.PLAIN, 35);
                opensansSemibold20 = baseFont.deriveFont(Font.PLAIN, 20);
                opensansSemibold18 = baseFont.deriveFont(Font.PLAIN, 18);
            } else {
                opensansSemibold35 = new Font("Arial", Font.PLAIN, 35);
                opensansSemibold20 = new Font("Arial", Font.PLAIN, 20);
                opensansSemibold18 = new Font("Arial", Font.PLAIN, 18);
            }
        } catch (Exception e) {
            e.printStackTrace();
            opensansSemibold35 = new Font("Arial", Font.PLAIN, 35);
            opensansSemibold20 = new Font("Arial", Font.PLAIN, 20);
            opensansSemibold18 = new Font("Arial", Font.PLAIN, 18);
        }
    }
    
    public static void drawString(String text, float x, float y, int color, int size) {
        Font font = size == 35 ? opensansSemibold35 : (size == 20 ? opensansSemibold20 : opensansSemibold18);
        CustomFontRenderer.drawString(text, x, y, color, font);
    }
    
    public static void drawCenteredString(String text, float x, float y, int color, int size) {
        Font font = size == 35 ? opensansSemibold35 : (size == 20 ? opensansSemibold20 : opensansSemibold18);
        float width = CustomFontRenderer.getStringWidth(text, font);
        CustomFontRenderer.drawString(text, x - width / 2f, y, color, font);
    }
    
    public static float getStringWidth(String text, int size) {
        Font font = size == 35 ? opensansSemibold35 : (size == 20 ? opensansSemibold20 : opensansSemibold18);
        return CustomFontRenderer.getStringWidth(text, font);
    }
    
    public static float getHeight(int size) {
        Font font = size == 35 ? opensansSemibold35 : (size == 20 ? opensansSemibold20 : opensansSemibold18);
        return CustomFontRenderer.getFontHeight(font);
    }
    
    public static float getMiddleOfBox(float boxHeight, int size) {
        return boxHeight / 2f - getHeight(size) / 2f;
    }
}
