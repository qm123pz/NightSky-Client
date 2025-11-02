package nightsky.util.render;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class RoundedUtil {

    public static void drawRound(float x, float y, float width, float height, float radius, Color color) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_POLYGON);
        
        double i = 0;
        double angleStep = Math.PI / (2 * 90);
        
        while (i <= Math.PI / 2) {
            glVertex2d(x + radius + Math.sin(i) * radius, y + radius + Math.cos(i) * radius);
            i += angleStep;
        }
        
        i = 0;
        while (i <= Math.PI / 2) {
            glVertex2d(x + radius + Math.sin(i) * radius, y + height - radius + Math.cos(i) * -radius);
            i += angleStep;
        }
        
        i = 0;
        while (i <= Math.PI / 2) {
            glVertex2d(x + width - radius + Math.sin(i) * -radius, y + height - radius + Math.cos(i) * -radius);
            i += angleStep;
        }
        
        i = 0;
        while (i <= Math.PI / 2) {
            glVertex2d(x + width - radius + Math.sin(i) * -radius, y + radius + Math.cos(i) * radius);
            i += angleStep;
        }
        
        glEnd();
        glEnable(GL_TEXTURE_2D);
        GlStateManager.disableBlend();
    }

    public static void drawGradientHorizontal(float x, float y, float width, float height, float radius, Color left, Color right) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glDisable(GL_TEXTURE_2D);
        glShadeModel(GL_SMOOTH);
        glBegin(GL_QUADS);
        
        glColor4f(left.getRed() / 255f, left.getGreen() / 255f, left.getBlue() / 255f, left.getAlpha() / 255f);
        glVertex2f(x, y);
        glVertex2f(x, y + height);
        
        glColor4f(right.getRed() / 255f, right.getGreen() / 255f, right.getBlue() / 255f, right.getAlpha() / 255f);
        glVertex2f(x + width, y + height);
        glVertex2f(x + width, y);
        
        glEnd();
        glShadeModel(GL_FLAT);
        glEnable(GL_TEXTURE_2D);
        GlStateManager.disableBlend();
    }

    public static void drawGradientVertical(float x, float y, float width, float height, float radius, Color top, Color bottom) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glDisable(GL_TEXTURE_2D);
        glShadeModel(GL_SMOOTH);
        glBegin(GL_QUADS);
        
        glColor4f(top.getRed() / 255f, top.getGreen() / 255f, top.getBlue() / 255f, top.getAlpha() / 255f);
        glVertex2f(x, y);
        glVertex2f(x + width, y);
        
        glColor4f(bottom.getRed() / 255f, bottom.getGreen() / 255f, bottom.getBlue() / 255f, bottom.getAlpha() / 255f);
        glVertex2f(x + width, y + height);
        glVertex2f(x, y + height);
        
        glEnd();
        glShadeModel(GL_FLAT);
        glEnable(GL_TEXTURE_2D);
        GlStateManager.disableBlend();
    }
}
