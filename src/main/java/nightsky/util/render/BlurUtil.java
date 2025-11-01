package nightsky.util.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;

import java.lang.reflect.Field;
import java.util.List;

public class BlurUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static ShaderGroup shaderGroup;
    private static net.minecraft.client.shader.Framebuffer framebuffer;
    private static net.minecraft.client.shader.Framebuffer frbuffer;

    private static int lastFactor = 0;
    private static int lastWidth = 0;
    private static int lastHeight = 0;

    private static float lastX = 0F;
    private static float lastY = 0F;
    private static float lastW = 0F;
    private static float lastH = 0F;

    private static float lastStrength = 5F;

    private static Field mainFramebufferField;
    private static Field listShadersField;
    private static Field timerField;

    static {
        try {
            mainFramebufferField = ShaderGroup.class.getDeclaredField("mainFramebuffer");
            mainFramebufferField.setAccessible(true);
        } catch (Exception e) {
            try {
                mainFramebufferField = ShaderGroup.class.getDeclaredField("field_148035_a");
                mainFramebufferField.setAccessible(true);
            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }
        
        try {
            listShadersField = ShaderGroup.class.getDeclaredField("listShaders");
            listShadersField.setAccessible(true);
        } catch (Exception e) {
            try {
                listShadersField = ShaderGroup.class.getDeclaredField("field_148031_d");
                listShadersField.setAccessible(true);
            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }
        
        try {
            timerField = Minecraft.class.getDeclaredField("timer");
            timerField.setAccessible(true);
        } catch (Exception e) {
            try {
                timerField = Minecraft.class.getDeclaredField("field_71428_T");
                timerField.setAccessible(true);
            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    private static void setupFramebuffers() {
        try {
            if (shaderGroup != null) {
                shaderGroup.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void initShaderIfNeeded() {
        if (shaderGroup == null && mainFramebufferField != null) {
            try {
                shaderGroup = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), new ResourceLocation("shaders/post/blurArea.json"));
                framebuffer = (net.minecraft.client.shader.Framebuffer) mainFramebufferField.get(shaderGroup);
                frbuffer = shaderGroup.getFramebufferRaw("result");
                
                if (framebuffer == null || frbuffer == null) {
                    shaderGroup = null;
                    framebuffer = null;
                    frbuffer = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                shaderGroup = null;
                framebuffer = null;
                frbuffer = null;
            }
        }
    }

    private static void setValues(float strength, float x, float y, float w, float h, float width, float height, boolean force) {
        if (!force && strength == lastStrength && lastX == x && lastY == y && lastW == w && lastH == h) return;
        lastStrength = strength;
        lastX = x;
        lastY = y;
        lastW = w;
        lastH = h;

        try {
            if (shaderGroup != null && listShadersField != null) {
                @SuppressWarnings("unchecked")
                List<net.minecraft.client.shader.Shader> listShaders = (List<net.minecraft.client.shader.Shader>) listShadersField.get(shaderGroup);
                if (listShaders != null && listShaders.size() >= 2) {
                    for (int i = 0; i <= 1; i++) {
                        net.minecraft.client.shader.Shader shader = listShaders.get(i);
                        if (shader != null && shader.getShaderManager() != null) {
                            if (shader.getShaderManager().getShaderUniform("Radius") != null) {
                                shader.getShaderManager().getShaderUniform("Radius").set(strength);
                            }
                            if (shader.getShaderManager().getShaderUniform("BlurXY") != null) {
                                shader.getShaderManager().getShaderUniform("BlurXY").set(x, height - y - h);
                            }
                            if (shader.getShaderManager().getShaderUniform("BlurCoord") != null) {
                                shader.getShaderManager().getShaderUniform("BlurCoord").set(w, h);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void blur(float posX, float posY, float posXEnd, float posYEnd, float blurStrength, boolean displayClipMask, Runnable triggerMethod) {
        if (!OpenGlHelper.isFramebufferEnabled()) return;

        initShaderIfNeeded();
        if (shaderGroup == null || framebuffer == null || frbuffer == null) return;

        float x = posX;
        float y = posY;
        float x2 = posXEnd;
        float y2 = posYEnd;

        if (x > x2) {
            float z = x;
            x = x2;
            x2 = z;
        }

        if (y > y2) {
            float z = y;
            y = y2;
            y2 = z;
        }

        ScaledResolution sc = new ScaledResolution(mc);
        int scaleFactor = sc.getScaleFactor();
        int width = sc.getScaledWidth();
        int height = sc.getScaledHeight();

        if (sizeHasChanged(scaleFactor, width, height)) {
            setupFramebuffers();
            setValues(blurStrength, x, y, x2 - x, y2 - y, width, height, true);
        }

        lastFactor = scaleFactor;
        lastWidth = width;
        lastHeight = height;

        setValues(blurStrength, x, y, x2 - x, y2 - y, width, height, false);

        framebuffer.bindFramebuffer(true);
        try {
            Timer timer = (Timer) timerField.get(mc);
            shaderGroup.loadShaderGroup(timer.renderPartialTicks);
        } catch (Exception e) {
            shaderGroup.loadShaderGroup(1.0F);
        }
        mc.getFramebuffer().bindFramebuffer(true);

        StencilUtil.write(displayClipMask);
        triggerMethod.run();

        StencilUtil.erase(true);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.pushMatrix();
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();
        frbuffer.bindFramebufferTexture();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        double f2 = frbuffer.framebufferWidth / (double) frbuffer.framebufferTextureWidth;
        double f3 = frbuffer.framebufferHeight / (double) frbuffer.framebufferTextureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        net.minecraft.client.renderer.WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(0.0, height, 0.0).tex(0.0, 0.0).color(255, 255, 255, 255).endVertex();
        worldrenderer.pos(width, height, 0.0).tex(f2, 0.0).color(255, 255, 255, 255).endVertex();
        worldrenderer.pos(width, 0.0, 0.0).tex(f2, f3).color(255, 255, 255, 255).endVertex();
        worldrenderer.pos(0.0, 0.0, 0.0).tex(0.0, f3).color(255, 255, 255, 255).endVertex();
        tessellator.draw();
        frbuffer.unbindFramebufferTexture();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.popMatrix();

        StencilUtil.dispose();
        
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void blurArea(float x, float y, float x2, float y2, float blurStrength) {
        blur(x, y, x2, y2, blurStrength, false, () -> {
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderUtil.setup2DRendering(() -> {
                net.minecraft.client.gui.Gui.drawRect((int)x, (int)y, (int)x2, (int)y2, -1);
            });
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        });
    }

    public static void blurAreaRounded(float x, float y, float x2, float y2, float rad, float blurStrength) {
        blur(x, y, x2, y2, blurStrength, false, () -> {
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            drawFastRoundedRect(x, y, x2, y2, rad);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        });
    }

    public static void blurAreaWithBackground(float x, float y, float x2, float y2, float blurStrength, int backgroundColor) {
        blurArea(x, y, x2, y2, blurStrength);
        
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderUtil.setup2DRendering(() -> {
            net.minecraft.client.gui.Gui.drawRect((int)x, (int)y, (int)x2, (int)y2, backgroundColor);
        });
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
    
    public static void blurArrayListShape(java.util.List<float[]> moduleRects, float blurStrength) {
        if (moduleRects.isEmpty()) return;
        
        float minX = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;
        
        for (float[] rect : moduleRects) {
            minX = Math.min(minX, rect[0]);
            maxX = Math.max(maxX, rect[0] + rect[2]);
            minY = Math.min(minY, rect[1]);
            maxY = Math.max(maxY, rect[1] + rect[3]);
        }

        blur(minX, minY, maxX, maxY, blurStrength, false, () -> {
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

            for (float[] rect : moduleRects) {
                net.minecraft.client.gui.Gui.drawRect((int)rect[0], (int)rect[1], 
                    (int)(rect[0] + rect[2]), (int)(rect[1] + rect[3]), -1);
            }
            
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        });
    }

    private static boolean sizeHasChanged(int scaleFactor, int width, int height) {
        return (lastFactor != scaleFactor || lastWidth != width || lastHeight != height);
    }

    private static void drawFastRoundedRect(float x0, float y0, float x1, float y1, float radius) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderUtil.drawRoundedRect(x0, y0, x1 - x0, y1 - y0, (int)radius, -1);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}