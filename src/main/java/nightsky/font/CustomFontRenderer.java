package nightsky.font;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CustomFontRenderer {
    private static final FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
    private static final Map<Font, CustomFontRenderer> fontRenderers = new HashMap<>();
    
    private final byte[][] charwidth = new byte[256][];
    private final int[] textures = new int[256];
    private Font font;
    private int fontWidth;
    private int fontHeight; 
    private int textureWidth;
    private int textureHeight;
    
    private CustomFontRenderer(Font font) {
        this.font = font;
        Arrays.fill(textures, -1);
        Rectangle2D maxBounds = font.getMaxCharBounds(frc);
        this.fontWidth = (int) Math.ceil(maxBounds.getWidth());
        this.fontHeight = (int) Math.ceil(maxBounds.getHeight());
        this.textureWidth = resizeToOpenGLSupportResolution(fontWidth * 16);
        this.textureHeight = resizeToOpenGLSupportResolution(fontHeight * 16);
    }
    
    public static CustomFontRenderer getRenderer(Font font) {
        return fontRenderers.computeIfAbsent(font, CustomFontRenderer::new);
    }
    
    private int resizeToOpenGLSupportResolution(int size) {
        int power = 0;
        while (size > 1 << power) power++;
        return 1 << power;
    }
    
    public static void drawString(String text, float x, float y, int color, Font font) {
        if (text == null || text.isEmpty() || font == null) {
            return;
        }
        
        CustomFontRenderer renderer = getRenderer(font);
        renderer.drawStringInternal(text, x, y, color);
    }
    
    private void drawStringInternal(String text, float x, float y, int color) {
        y = y - 2;
        x *= 2;
        y *= 2;
        y -= 2;
        
        float r = (color >> 16 & 0xFF) / 255f;
        float g = (color >> 8 & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = (color >> 24 & 0xFF) / 255f;
        if (a == 0) a = 1;
        
        GlStateManager.color(r, g, b, a);
        GL11.glPushMatrix();
        GL11.glScaled(0.5, 0.5, 0.5);
        
        int offset = 0;
        char[] chars = text.toCharArray();
        for (char chr : chars) {
            offset += drawChar(chr, x + offset, y);
        }
        
        GL11.glPopMatrix();
    }
    
    private int drawChar(char chr, float x, float y) {
        int region = chr >> 8;
        int id = chr & 0xFF;
        int xTexCoord = (id & 0xF) * fontWidth;
        int yTexCoord = (id >> 4) * fontHeight;
        int width = getOrGenerateCharWidthMap(region)[id];
        
        GlStateManager.bindTexture(getOrGenerateCharTexture(region));
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2d(wrapTextureCoord(xTexCoord, textureWidth), wrapTextureCoord(yTexCoord, textureHeight));
        GL11.glVertex2f(x, y);
        GL11.glTexCoord2d(wrapTextureCoord(xTexCoord, textureWidth), wrapTextureCoord(yTexCoord + fontHeight, textureHeight));
        GL11.glVertex2f(x, y + fontHeight);
        GL11.glTexCoord2d(wrapTextureCoord(xTexCoord + width, textureWidth), wrapTextureCoord(yTexCoord + fontHeight, textureHeight));
        GL11.glVertex2f(x + width, y + fontHeight);
        GL11.glTexCoord2d(wrapTextureCoord(xTexCoord + width, textureWidth), wrapTextureCoord(yTexCoord, textureHeight));
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();
        
        return width;
    }
    
    public static void drawStringWithShadow(String text, float x, float y, int color, Font font) {
        if (text == null || text.isEmpty() || font == null) {
            return;
        }
        
        int shadowColor = (color & 0xFF000000) != 0 ? 
            (((color >> 16) & 0xFF) / 4 << 16) | (((color >> 8) & 0xFF) / 4 << 8) | ((color & 0xFF) / 4) | (color & 0xFF000000) : 
            0x404040;
        
        drawString(text, x + 0.5f, y + 0.5f, shadowColor, font);
        drawString(text, x, y, color, font);
    }
    
    private int getOrGenerateCharTexture(int id) {
        if (textures[id] == -1)
            return textures[id] = generateCharTexture(id);
        return textures[id];
    }
    
    private byte[] getOrGenerateCharWidthMap(int id) {
        if (charwidth[id] == null)
            return charwidth[id] = generateCharWidthMap(id);
        return charwidth[id];
    }
    
    private double wrapTextureCoord(int coord, int size) {
        return coord / (double) size;
    }
    
    private int generateCharTexture(int id) {
        int textureId = GL11.glGenTextures();
        int offset = id << 8;
        BufferedImage img = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setFont(font);
        g.setColor(Color.WHITE);
        FontMetrics fontMetrics = g.getFontMetrics();
        for (int y = 0; y < 16; y++)
            for (int x = 0; x < 16; x++) {
                String chr = String.valueOf((char) ((y << 4 | x) | offset));
                g.drawString(chr, x * fontWidth, y * fontHeight + fontMetrics.getAscent());
            }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, textureWidth, textureHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, imageToBuffer(img));
        return textureId;
    }
    
    private byte[] generateCharWidthMap(int id) {
        int offset = id << 8;
        byte[] widthmap = new byte[256];
        for (int i = 0; i < widthmap.length; i++) {
            widthmap[i] = (byte) Math.ceil(font.getStringBounds(String.valueOf((char) (i | offset)), frc).getWidth());
        }
        return widthmap;
    }
    
    private static ByteBuffer imageToBuffer(BufferedImage img) {
        int[] arr = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
        ByteBuffer buf = ByteBuffer.allocateDirect(4 * arr.length);
        for (int i : arr) {
            buf.putInt(i << 8 | i >> 24 & 0xFF);
        }
        buf.flip();
        return buf;
    }
    
    public static int getStringWidth(String text, Font font) {
        if (text == null || text.isEmpty() || font == null) {
            return 0;
        }
        
        CustomFontRenderer renderer = getRenderer(font);
        int width = 0;
        char[] chars = text.toCharArray();
        for (char chr : chars) {
            width += renderer.getOrGenerateCharWidthMap(chr >> 8)[chr & 0xFF];
        }
        return width / 2;
    }
    
    public static int getFontHeight(Font font) {
        if (font == null) {
            return 9;
        }
        CustomFontRenderer renderer = getRenderer(font);
        return renderer.fontHeight / 2;
    }
}
