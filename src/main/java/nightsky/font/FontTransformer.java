package nightsky.font;

import net.minecraft.client.Minecraft;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FontTransformer {
    private static FontTransformer instance;
    private final Map<String, Font> fontCache = new HashMap<>();
    private String selectedFontName = "Arial";
    private float selectedFontSize = 18.0f;
    
    private FontTransformer() {
        loadFonts();
    }
    
    public static FontTransformer getInstance() {
        if (instance == null) {
            instance = new FontTransformer();
        }
        return instance;
    }
    
    private void loadFonts() {
        loadFont("Arial", "/assets/minecraft/nightsky/font/Arial.ttf");
        loadFont("ArialBold", "/assets/minecraft/nightsky/font/ArialBold.ttf");
        loadFont("RobotoMedium", "/assets/minecraft/nightsky/font/Roboto-Medium.ttf");
        loadFont("JetBrainsMono", "/assets/minecraft/nightsky/font/jetbrains.ttf");
        loadFont("MicrosoftYaHei", "/assets/minecraft/nightsky/font/msyh-regular.ttf");
        loadFont("MicrosoftYaHei Bold", "/assets/minecraft/nightsky/font/msyh-bold.ttf");
        loadFont("RalewayExtraBold", "/assets/minecraft/nightsky/font/raleway-extrabold.ttf");
        loadFont("RobotoBlack", "/assets/minecraft/nightsky/font/roboto-black.ttf");
        loadFont("RobotoRegular", "/assets/minecraft/nightsky/font/roboto-regular.ttf");
        loadFont("ESP", "/assets/minecraft/nightsky/font/esp-1.ttf");
        loadFont("ESPBold", "/assets/minecraft/nightsky/font/esp-bold-3.ttf");
        loadFont("ESPItalic", "/assets/minecraft/nightsky/font/esp-ital-4.ttf");
        loadFont("ESPBoldItalic", "/assets/minecraft/nightsky/font/esp-bdit-2.ttf");
        loadFont("Consolas", "/assets/minecraft/nightsky/font/consola-1.ttf");
        loadFont("OpenSansBold","/assets/minecraft/nightsky/font/OpenSans-Bold.ttf");
        loadFont("OpenSansBoldItalic","/assets/minecraft/nightsky/font/OpenSans-BoldItalic.ttf");
        loadFont("OpenSansExtraBold","/assets/minecraft/nightsky/font/OpenSans-ExtraBold.ttf");
        loadFont("OpenSansExtraBoldItalic","/assets/minecraft/nightsky/font/OpenSans-ExtraBoldItalic.ttf");
        loadFont("OpenSansItalic","/assets/minecraft/nightsky/font/OpenSans-Italic.ttf");
        loadFont("OpenSansLight","/assets/minecraft/nightsky/font/OpenSans-Light.ttf");
        loadFont("OpenSansLightItalic","/assets/minecraft/nightsky/font/OpenSans-LightItalic.ttf");
        loadFont("OpenSansRegular","/assets/minecraft/nightsky/font/OpenSans-Regular.ttf");
        loadFont("OpenSansSemiBold","/assets/minecraft/nightsky/font/OpenSans-Semibold.ttf");
        loadFont("OpenSansSemiBoldItalic","/assets/minecraft/nightsky/font/OpenSans-SemiboldItalic.ttf");
        loadFont("SuperJoyful","/assets/minecraft/nightsky/font/Super Joyful.ttf");
        loadFont("Cheri","/assets/minecraft/nightsky/font/Cheri.ttf");
        loadFont("Cherl","/assets/minecraft/nightsky/font/Cherl.ttf");
        loadFont("Fortalesia","/assets/minecraft/nightsky/font/Fortalesia.ttf");
        loadFont("HarmonyOSRegular","/assets/minecraft/nightsky/font/HarmonyOS-Regular.ttf");
        loadFont("HarmonyOSBold","/assets/minecraft/nightsky/font/HarmonyOS-Bold.ttf");
        loadFont("HarmonyOSBlack","/assets/minecraft/nightsky/font/HarmonyOS-Black.ttf");
    }
    
    private void loadFont(String name, String path) {
        try {
            InputStream fontStream = FontTransformer.class.getResourceAsStream(path);
            if (fontStream != null) {
                Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                fontCache.put(name, font);
                fontStream.close();
            }
        } catch (Exception e) {
        }
    }
    
    public void setFont(String fontName, float size) {
        this.selectedFontName = fontName;
        this.selectedFontSize = size;
    }
    
    public String getSelectedFontName() {
        return selectedFontName;
    }
    
    public float getSelectedFontSize() {
        return selectedFontSize;
    }
    
    public Font getFont(String fontName, float size) {
        if (fontName.equals("minecraft")) {
            return null;
        }
        
        Font baseFont = fontCache.get(fontName);
        if (baseFont == null) {
            return null;
        }
        
        float scaledSize = size * 0.5f;
        return baseFont.deriveFont(scaledSize);
    }
    
    public boolean isMinecraftFont() {
        return selectedFontName.equals("minecraft");
    }
    
    public String[] getAvailableFonts() {
        String[] fonts = new String[fontCache.size() + 1];
        fonts[0] = "minecraft";
        int i = 1;
        for (String fontName : fontCache.keySet()) {
            fonts[i++] = fontName;
        }
        return fonts;
    }
    
    
    public int getStringWidth(String text, String fontName, float size) {
        if (fontName.equals("minecraft")) {
            return Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
        }
        
        Font font = fontCache.get(fontName);
        if (font == null) {
            return Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
        }
        
        float scaledSize = size * 0.5f;
        font = font.deriveFont(scaledSize);
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), false, false);
        Rectangle2D bounds = font.getStringBounds(text, frc);
        return (int)Math.round(bounds.getWidth());
    }
    
    public int getFontHeight(String fontName, float size) {
        if (fontName.equals("minecraft")) {
            return Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
        }
        
        Font font = fontCache.get(fontName);
        if (font == null) {
            return Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
        }
        
        float scaledSize = size * 0.5f;
        font = font.deriveFont(scaledSize);
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), false, false);
        Rectangle2D bounds = font.getStringBounds("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", frc);
        return (int) Math.round(bounds.getHeight());
    }
    
}
