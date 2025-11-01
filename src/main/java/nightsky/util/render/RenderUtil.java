package nightsky.util.render;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class RenderUtil {

    public static void drawRect(float left, float top, float width, float height, int color) {
        Gui.drawRect((int)left, (int)top, (int)(left + width), (int)(top + height), color);
    }

    public static void drawRect(float left, float top, float width, float height, Color color) {
        drawRect(left, top, width, height, color.getRGB());
    }


    public static void scaleStart(float x, float y, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, 1);
        GlStateManager.translate(-x, -y, 0);
    }

    public static void scaleEnd() {
        GlStateManager.popMatrix();
    }

    public static void bindTexture(int texture) {
        GlStateManager.bindTexture(texture);
    }

    public static void glColor(int hex) {
        float alpha = (hex >> 24 & 0xFF) / 255.0F;
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public static void drawTexturedRect(float x, float y, float width, float height) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0).tex(0.0, 1.0).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0).tex(1.0, 1.0).endVertex();
        worldrenderer.pos(x + width, y, 0.0).tex(1.0, 0.0).endVertex();
        worldrenderer.pos(x, y, 0.0).tex(0.0, 0.0).endVertex();
        tessellator.draw();
    }

    public static void setup2DRendering(Runnable f) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_TEXTURE_2D);
        f.run();
        glEnable(GL_TEXTURE_2D);
        GlStateManager.disableBlend();
    }

    public static void setColor(int color) {
        GL11.glColor4ub((byte) (color >> 16 & 0xFF), (byte) (color >> 8 & 0xFF), (byte) (color & 0xFF), (byte) (color >> 24 & 0xFF));
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        float x1 = x + width;
        float y1 = y + height;
        final float f = (color >> 24 & 0xFF) / 255.0F;
        final float f1 = (color >> 16 & 0xFF) / 255.0F;
        final float f2 = (color >> 8 & 0xFF) / 255.0F;
        final float f3 = (color & 0xFF) / 255.0F;
        
        GL11.glPushAttrib(0);
        GL11.glScaled(0.5, 0.5, 0.5);

        x *= 2;
        y *= 2;
        x1 *= 2;
        y1 *= 2;

        glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(f1, f2, f3, f);
        GlStateManager.enableBlend();
        glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        GL11.glBegin(GL11.GL_POLYGON);
        final double v = Math.PI / 180;

        for (int i = 0; i <= 90; i += 1) {
            GL11.glVertex2d(x + radius + Math.sin(i * v) * (radius * -1), y + radius + Math.cos(i * v) * (radius * -1));
        }

        for (int i = 90; i <= 180; i += 1) {
            GL11.glVertex2d(x + radius + Math.sin(i * v) * (radius * -1), y1 - radius + Math.cos(i * v) * (radius * -1));
        }

        for (int i = 0; i <= 90; i += 1) {
            GL11.glVertex2d(x1 - radius + Math.sin(i * v) * radius, y1 - radius + Math.cos(i * v) * radius);
        }

        for (int i = 90; i <= 180; i += 1) {
            GL11.glVertex2d(x1 - radius + Math.sin(i * v) * radius, y + radius + Math.cos(i * v) * radius);
        }

        GL11.glEnd();

        glEnable(GL11.GL_TEXTURE_2D);
        glDisable(GL11.GL_LINE_SMOOTH);
        glEnable(GL11.GL_TEXTURE_2D);

        GL11.glScaled(2, 2, 2);

        GL11.glPopAttrib();
        GL11.glColor4f(1, 1, 1, 1);
    }
    public static void drawRoundedRect(float x, float y, float width, float height, float radius, Color color) {
        drawRoundedRect(x, y, width, height, radius, color.getRGB());
    }
}