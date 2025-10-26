package nightsky.util.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class Render2DUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void drawRect(float x, float y, float width, float height, Color color) {
        Gui.drawRect((int)x, (int)y, (int)(x + width), (int)(y + height), color.getRGB());
    }
    
    public static void drawRect(float x, float y, float width, float height, int color) {
        Gui.drawRect((int)x, (int)y, (int)(x + width), (int)(y + height), color);
    }

    public static void drawRainbowRect(float x, float y, float width, float height) {
        for (int ticks = 0; ticks < width; ticks++) {
            Color rainbowcolor = new Color(Color.HSBtoRGB((float) (ticks / width + Math.sin(1.6)) % 1.0f, 0.5f, 1.0f));
            drawRect(x + ticks, y, 1, height, rainbowcolor.getRGB());
        }
    }

    public static Color intToColor(Integer c) {
        return new Color(c >> 16 & 255, c >> 8 & 255, c & 255, c >> 24 & 255);
    }
    
    public static void drawLine(final double x, final double y, final double x1, final double y1, final float width) {
        glDisable(GL_TEXTURE_2D);
        glLineWidth(width);
        glBegin(GL_LINES);
        glVertex2d(x, y);
        glVertex2d(x1, y1);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }
    
    public static void bindTexture(int texture) {
        glBindTexture(GL_TEXTURE_2D, texture);
    }

    public static void drawImage(ResourceLocation image, float x, float y, float width, float height, float alpha) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDepthMask(false);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor4f(1.0F, 1.0F, 1.0F, alpha);
        mc.getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture((int)x, (int)y, 0, 0, (int)width, (int)height, (int)width, (int)height);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }
    
    public static void scaleStart(float x, float y, float scale) {
        glPushMatrix();
        glTranslatef(x, y, 0);
        glScalef(scale, scale, 1);
        glTranslatef(-x, -y, 0);
    }

    public static void scissorStart(double x, double y, double width, double height) {
        glEnable(GL_SCISSOR_TEST);
        ScaledResolution sr = new ScaledResolution(mc);
        final double scale = sr.getScaleFactor();
        double finalHeight = height * scale;
        double finalY = (sr.getScaledHeight() - y) * scale;
        double finalX = x * scale;
        double finalWidth = width * scale;
        glScissor((int) finalX, (int) (finalY - finalHeight), (int) finalWidth, (int) finalHeight);
    }

    public static void scissorEnd() {
        glDisable(GL_SCISSOR_TEST);
    }

    public static void scaleEnd() {
        glPopMatrix();
    }

    public static void startRotate(float x, float y, float rotate) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.rotate(rotate, 0, 0, -1);
        GlStateManager.translate(-x, -y, 0);
    }

    public static void endRotate() {
        GlStateManager.popMatrix();
    }
    
    public static void resetColor() {
        GlStateManager.color(1, 1, 1, 1);
    }
    
    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, (float) (limit * .01));
    }

    public static void color(int color, float alpha) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GlStateManager.color(r, g, b, alpha);
    }

    public static void color(int color) {
        color(color, (float) (color >> 24 & 255) / 255.0F);
    }
    
    public static void glColor(final int red, final int green, final int blue, final int alpha) {
        GL11.glColor4f(red / 255F, green / 255F, blue / 255F, alpha / 255F);
    }

    public static void glColor(final Color color) {
        glColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    private static void glColor(final int hex) {
        glColor(hex >> 16 & 0xFF, hex >> 8 & 0xFF, hex & 0xFF, hex >> 24 & 0xFF);
    }
}
