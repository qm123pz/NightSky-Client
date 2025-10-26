package nightsky.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class GLUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();
    
    public static float[] project2D(float x, float y, float z, int scaleFactor) {
        IntBuffer viewport = GLUtil.createIntBuffer(16);
        FloatBuffer modelView = GLUtil.createFloatBuffer(16);
        FloatBuffer projection = GLUtil.createFloatBuffer(16);
        FloatBuffer result = GLUtil.createFloatBuffer(4);
        
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
        
        if (GLU.gluProject(x, y, z, modelView, projection, viewport, result)) {
            ScaledResolution sr = new ScaledResolution(mc);
            return new float[]{
                result.get(0) / scaleFactor,
                (sr.getScaledHeight() - result.get(1) / scaleFactor),
                result.get(2)
            };
        }
        return null;
    }
    
    private static IntBuffer createIntBuffer(int size) {
        return org.lwjgl.BufferUtils.createIntBuffer(size);
    }
    
    private static FloatBuffer createFloatBuffer(int size) {
        return org.lwjgl.BufferUtils.createFloatBuffer(size);
    }
}
