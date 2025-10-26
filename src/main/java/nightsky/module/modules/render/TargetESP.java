package nightsky.module.modules.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import nightsky.event.EventTarget;
import nightsky.event.types.EventType;
import nightsky.events.PacketEvent;
import nightsky.events.UpdateEvent;
import nightsky.events.Render2DEvent;
import nightsky.events.Render3DEvent;
import nightsky.events.Shader2DEvent;
import nightsky.module.Module;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.ModeValue;
import nightsky.value.values.FloatValue;
import nightsky.util.Animation;
import nightsky.util.DecelerateAnimation;
import nightsky.util.MathUtil;
import nightsky.util.TimerUtil;
import nightsky.util.ColorUtil;
import nightsky.util.GLUtil;
import nightsky.util.RenderUtil;
import nightsky.mixin.IAccessorEntityRenderer;
import nightsky.mixin.IAccessorRenderManager;
import nightsky.mixin.IAccessorMinecraft;
import nightsky.NightSky;

import java.awt.*;

public class TargetESP extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private final ModeValue mode = new ModeValue("MarkMode", 0, new String[]{"Points", "Ghost", "Image", "Exhi", "Circle"});
    private final ModeValue imageMode = new ModeValue("ImageMode", 0, new String[]{"Rectangle", "QuadStapple", "TriangleStapple", "TriangleStipple"}, () -> mode.getValue() == 2);
    private final FloatValue circleSpeed = new FloatValue("CircleSpeed", 2.0F, 1.0F, 5.0F, () -> mode.getValue() == 4);
    private final BooleanValue onlyPlayer = new BooleanValue("OnlyPlayer", false);
    
    private EntityLivingBase target;
    private final TimerUtil timerUtil = new TimerUtil();
    private final TimerUtil displayTimer = new TimerUtil();
    private final TimerUtil animTimer = new TimerUtil();
    private long lastTime = System.currentTimeMillis();
    private boolean hasFullyFadedIn = false;
    private final Animation alphaAnim = new DecelerateAnimation(400, 1);
    private final ResourceLocation glowCircle = new ResourceLocation("minecraft", "nightsky/texture/targetesp/glow_circle.png");
    private final ResourceLocation rectangle = new ResourceLocation("minecraft", "nightsky/texture/targetesp/rectangle.png");
    private final ResourceLocation quadstapple = new ResourceLocation("minecraft", "nightsky/texture/targetesp/quadstapple.png");
    private final ResourceLocation trianglestapple = new ResourceLocation("minecraft", "nightsky/texture/targetesp/trianglestapple.png");
    private final ResourceLocation trianglestipple = new ResourceLocation("minecraft", "nightsky/texture/targetesp/trianglestipple.png");
    public double prevCircleStep;
    public double circleStep;
    
    public TargetESP() {
        super("TargetESP", false);
    }
    
    private Color getInterfaceColor() {
        Interface interfaceModule = (Interface) NightSky.moduleManager.getModule("Interface");
        return new Color(interfaceModule.color());
    }
    
    @Override
    public void onEnabled() {
        target = null;
        alphaAnim.reset();
        displayTimer.reset();
        animTimer.reset();
        lastTime = System.currentTimeMillis();
        hasFullyFadedIn = false;
        prevCircleStep = 0;
        circleStep = 0;
    }
    
    @Override 
    public void onDisabled() {
        target = null;
        alphaAnim.reset();
    }
    
    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getType() == EventType.SEND && event.getPacket() instanceof C02PacketUseEntity) {
            C02PacketUseEntity packet = (C02PacketUseEntity) event.getPacket();
            if (packet.getAction() != C02PacketUseEntity.Action.ATTACK) {
                return;
            }
            Entity entity = packet.getEntityFromWorld(mc.theWorld);
            if (entity instanceof EntityLivingBase && 
                (!onlyPlayer.getValue() || entity instanceof EntityPlayer)) {
                EntityLivingBase newTarget = (EntityLivingBase) entity;
                if (target != newTarget) {
                    target = newTarget;
                    lastTime = System.currentTimeMillis();
                    alphaAnim.reset();
                    alphaAnim.setDirection(Animation.Direction.FORWARDS);
                    animTimer.reset();
                    hasFullyFadedIn = false;
                }
                displayTimer.reset();
            }
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event){
        if(target != null && displayTimer.hasTimeElapsed(1000)){
            hasFullyFadedIn = false;
            target = null;
        }
    }

    private float getAlpha() {
        if (target == null) return 0.0f;

        long animElapsed = animTimer.getElapsedTime();
        long displayElapsed = displayTimer.getElapsedTime();

        if (!hasFullyFadedIn) {
            if (animElapsed < 200) {
                return animElapsed / 200.0f;
            } else {
                hasFullyFadedIn = true;
                return 1.0f;
            }
        } else {
            if (displayElapsed > 800) {
                return Math.max(0.0f, (1000 - displayElapsed) / 200.0f);
            } else {
                return 1.0f;
            }
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (target != null) {
            if (mode.getValue() == 0)
                points(event);

            if (mode.getValue() == 3) {
                float alpha = getAlpha();
                int baseAlpha = (int) (75 * alpha);
                int color = this.target.hurtTime > 3 ? new Color(200, 255, 100, baseAlpha).getRGB() : this.target.hurtTime < 3 ? new Color(235, 40, 40, baseAlpha).getRGB() : new Color(255, 255, 255, baseAlpha).getRGB();
                GlStateManager.pushMatrix();
                GL11.glShadeModel(7425);
                GL11.glHint(3154, 4354);
                ((IAccessorEntityRenderer) mc.entityRenderer).callSetupCameraTransform(event.getPartialTicks(), 2);
                double x = target.prevPosX + (target.posX - target.prevPosX) * (double) event.getPartialTicks() - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosX();
                double y = target.prevPosY + (target.posY - target.prevPosY) * (double) event.getPartialTicks() - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosY();
                double z = target.prevPosZ + (target.posZ - target.prevPosZ) * (double) event.getPartialTicks() - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosZ();
                double xMoved = target.posX - target.prevPosX;
                double yMoved = target.posY - target.prevPosY;
                double zMoved = target.posZ - target.prevPosZ;
                double motionX = 0.0;
                double motionY = 0.0;
                double motionZ = 0.0;
                GlStateManager.translate(x + (xMoved + motionX + (mc.thePlayer.motionX + 0.005)), y + (yMoved + motionY + (mc.thePlayer.motionY - 0.002)), z + (zMoved + motionZ + (mc.thePlayer.motionZ + 0.005)));
                AxisAlignedBB axisAlignedBB = target.getEntityBoundingBox();
                RenderUtil.drawAxisAlignedBB(new AxisAlignedBB(axisAlignedBB.minX - 0.1 - target.posX, axisAlignedBB.minY - 0.1 - target.posY, axisAlignedBB.minZ - 0.1 - target.posZ, axisAlignedBB.maxX + 0.1 - target.posX, axisAlignedBB.maxY + 0.2 - target.posY, axisAlignedBB.maxZ + 0.1 - target.posZ), true, color);
                GlStateManager.popMatrix();
            }

            if (mode.getValue() == 1) {
                GlStateManager.pushMatrix();
                GlStateManager.disableLighting();
                GlStateManager.depthMask(false);
                GlStateManager.enableBlend();
                GlStateManager.shadeModel(7425);
                GlStateManager.disableCull();
                GlStateManager.disableAlpha();
                GlStateManager.tryBlendFuncSeparate(770, 1, 0, 1);
                double radius = 0.67;
                float speed = 45;
                float size = 0.4f;
                double distance = 19;
                int lenght = 20;

                Vec3 interpolated = MathUtil.interpolate(new Vec3(target.lastTickPosX, target.lastTickPosY, target.lastTickPosZ), target.getPositionVector(), event.getPartialTicks());
                interpolated = new Vec3(interpolated.xCoord, interpolated.yCoord + 0.75f, interpolated.zCoord);

                RenderUtil.setupOrientationMatrix(interpolated.xCoord, interpolated.yCoord + 0.5f, interpolated.zCoord);

                float[] idk = new float[]{mc.getRenderManager().playerViewY, mc.getRenderManager().playerViewX};

                GL11.glRotated(-idk[0], 0.0, 1.0, 0.0);
                GL11.glRotated(idk[1], 1.0, 0.0, 0.0);
                
                for (int i = 0; i < lenght; i++) {
                    double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                    double s = Math.sin(angle) * radius;
                    double c = Math.cos(angle) * radius;
                    GlStateManager.translate(s, (c), -c);
                    GlStateManager.translate(-size / 2f, -size / 2f, 0);
                    GlStateManager.translate(size / 2f, size / 2f, 0);
                    int color = ColorUtil.applyOpacity(getInterfaceColor(), getAlpha()).getRGB();
                    RenderUtil.drawImage(glowCircle, 0f, 0f, -size, -size, color);
                    GlStateManager.translate(-size / 2f, -size / 2f, 0);
                    GlStateManager.translate(size / 2f, size / 2f, 0);
                    GlStateManager.translate(-(s), -(c), (c));
                }
                for (int i = 0; i < lenght; i++) {
                    double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                    double s = Math.sin(angle) * radius;
                    double c = Math.cos(angle) * radius;
                    GlStateManager.translate(-s, s, -c);
                    GlStateManager.translate(-size / 2f, -size / 2f, 0);
                    GlStateManager.translate(size / 2f, size / 2f, 0);
                    int color = ColorUtil.applyOpacity(getInterfaceColor(), getAlpha()).getRGB();
                    RenderUtil.drawImage(glowCircle, 0f, 0f, -size, -size, color);
                    GlStateManager.translate(-size / 2f, -size / 2f, 0);
                    GlStateManager.translate(size / 2f, size / 2f, 0);
                    GlStateManager.translate((s), -(s), (c));
                }
                for (int i = 0; i < lenght; i++) {
                    double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                    double s = Math.sin(angle) * radius;
                    double c = Math.cos(angle) * radius;
                    GlStateManager.translate(-(s), -(s), (c));
                    GlStateManager.translate(-size / 2f, -size / 2f, 0);
                    GlStateManager.translate(size / 2f, size / 2f, 0);
                    int color = ColorUtil.applyOpacity(getInterfaceColor(), getAlpha()).getRGB();
                    RenderUtil.drawImage(glowCircle, 0f, 0f, -size, -size, color);
                    GlStateManager.translate(-size / 2f, -size / 2f, 0);
                    GlStateManager.translate(size / 2f, size / 2f, 0);
                    GlStateManager.translate((s), (s), -(c));
                }
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.disableBlend();
                GlStateManager.enableCull();
                GlStateManager.enableAlpha();
                GlStateManager.depthMask(true);
                GlStateManager.popMatrix();
            }

            if (mode.getValue() == 4) {
                prevCircleStep = circleStep;
                circleStep += (double) this.circleSpeed.getValue() * RenderUtil.deltaTime() * 0.05;
                float eyeHeight = target.getEyeHeight();
                if (target.isSneaking()) {
                    eyeHeight -= 0.2F;
                }

                double cs = prevCircleStep + (circleStep - prevCircleStep) * (double) event.getPartialTicks();
                double prevSinAnim = Math.abs(1.0D + Math.sin(cs - 0.5D)) / 2.0D;
                double sinAnim = Math.abs(1.0D + Math.sin(cs)) / 2.0D;
                double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * (double) event.getPartialTicks() - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosX();
                double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * (double) event.getPartialTicks() - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosY() + prevSinAnim * (double) eyeHeight;
                double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * (double) event.getPartialTicks() - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosZ();
                double nextY = target.lastTickPosY + (target.posY - target.lastTickPosY) * (double) event.getPartialTicks() - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosY() + sinAnim * (double) eyeHeight;
                GL11.glPushMatrix();
                GL11.glDisable(2884);
                GL11.glDisable(3553);
                GL11.glEnable(3042);
                GL11.glDisable(2929);
                GL11.glDisable(3008);
                GL11.glShadeModel(7425);
                GL11.glBegin(8);

                int i;
                Color color;
                for (i = 0; i <= 360; ++i) {
                    color = getInterfaceColor();
                    float alpha = getAlpha();
                    GL11.glColor4f((float) color.getRed() / 255.0F, (float) color.getGreen() / 255.0F, (float) color.getBlue() / 255.0F, 0.6F * alpha);
                    GL11.glVertex3d(x + Math.cos(Math.toRadians(i)) * (double) target.width * 0.8D, nextY, z + Math.sin(Math.toRadians(i)) * (double) target.width * 0.8D);
                    GL11.glColor4f((float) color.getRed() / 255.0F, (float) color.getGreen() / 255.0F, (float) color.getBlue() / 255.0F, 0.01F * alpha);
                    GL11.glVertex3d(x + Math.cos(Math.toRadians(i)) * (double) target.width * 0.8D, y, z + Math.sin(Math.toRadians(i)) * (double) target.width * 0.8D);
                }

                GL11.glEnd();
                GL11.glEnable(2848);
                GL11.glBegin(2);

                for (i = 0; i <= 360; ++i) {
                    color = getInterfaceColor();
                    float alpha = getAlpha();
                    GL11.glColor4f((float) color.getRed() / 255.0F, (float) color.getGreen() / 255.0F, (float) color.getBlue() / 255.0F, 0.8F * alpha);
                    GL11.glVertex3d(x + Math.cos(Math.toRadians(i)) * (double) target.width * 0.8D, nextY, z + Math.sin(Math.toRadians(i)) * (double) target.width * 0.8D);
                }

                GL11.glEnd();
                GL11.glDisable(2848);
                GL11.glEnable(3553);
                GL11.glEnable(3008);
                GL11.glEnable(2929);
                GL11.glShadeModel(7424);
                GL11.glDisable(3042);
                GL11.glEnable(2884);
                GL11.glPopMatrix();
                GlStateManager.resetColor();
            }
        }
    }
    
    @EventTarget
    public void onRender2D(Render2DEvent event) {
        int index = 3;
        if (mode.getValue() == 2 && target != null) {
            float dst = mc.thePlayer.getDistanceToEntity(target);
            float[] pos = targetESPSPos(target, event);
            if (pos != null) {
                drawTargetESP2D(pos[0], pos[1],
                        (1.0f - MathHelper.clamp_float(Math.abs(dst - 6.0f) / 60.0f, 0.0f, 0.75f)) * 1, index);
            }
        }
    }

    @EventTarget
    public void onShader2D(Shader2DEvent event) {
        if (event.getShaderType() == Shader2DEvent.ShaderType.GLOW) {
            int index = 3;
            if (mode.getValue() == 2 && imageMode.getValue() == 0 && target != null) {
                float dst = mc.thePlayer.getDistanceToEntity(target);
                float[] pos = targetESPSPos(target, null);
                if (pos != null) {
                    drawTargetESP2D(pos[0], pos[1],
                            (1.0f - MathHelper.clamp_float(Math.abs(dst - 6.0f) / 60.0f, 0.0f, 0.75f)) * 1, index);
                }
            }
        }
    }

    private void points(Render3DEvent event) {
        if (target != null) {
            double markerX = MathUtil.interporate(event.getPartialTicks(), target.lastTickPosX, target.posX);
            double markerY = MathUtil.interporate(event.getPartialTicks(), target.lastTickPosY, target.posY) + target.height / 1.6f;
            double markerZ = MathUtil.interporate(event.getPartialTicks(), target.lastTickPosZ, target.posZ);
            float time = (float) ((((System.currentTimeMillis() - lastTime) / 1500F)) + (Math.sin((((System.currentTimeMillis() - lastTime) / 1500F))) / 10f));
            float alpha = 0.5f * 1;
            float pl = 0;
            boolean fa = false;
            
            for (int iteration = 0; iteration < 3; iteration++) {
                for (float i = time * 360; i < time * 360 + 90; i += 2) {
                    float max = time * 360 + 90;
                    float dc = MathUtil.normalize(i, time * 360 - 45, max);
                    float rf = 0.6f;
                    double radians = Math.toRadians(i);
                    double plY = pl + Math.sin(radians * 1.2f) * 0.1f;
                    int firstColor = ColorUtil.applyOpacity(getInterfaceColor(), getAlpha()).getRGB();
                    int secondColor = ColorUtil.applyOpacity(getInterfaceColor(), getAlpha()).getRGB();
                    GlStateManager.pushMatrix();
                    RenderUtil.setupOrientationMatrix(markerX, markerY, markerZ);

                    float[] idk = new float[]{mc.getRenderManager().playerViewY, mc.getRenderManager().playerViewX};

                    GL11.glRotated(-idk[0], 0.0, 1.0, 0.0);
                    GL11.glRotated(idk[1], 1.0, 0.0, 0.0);

                    GlStateManager.depthMask(false);
                    float q = (!fa ? 0.25f : 0.15f) * (Math.max(fa ? 0.25f : 0.15f, fa ? dc : (1f + (0.4f - dc)) / 2f) + 0.45f);
                    float size = q * (2f + ((0.5f - alpha) * 2));
                    RenderUtil.drawImage(
                            glowCircle,
                            (float) (Math.cos(radians) * rf - size / 2f),
                            (float) (plY - 0.7),
                            size, size,
                            firstColor);
                    GL11.glEnable(GL11.GL_DEPTH_TEST);
                    GlStateManager.depthMask(true);
                    GlStateManager.popMatrix();
                }
                time *= -1.025f;
                fa = !fa;
                pl += 0.45f;
            }
        }
    }

    private void drawTargetESP2D(float x, float y, float scale, int index) {
        long millis = (System.currentTimeMillis() - lastTime) + index * 400L;
        double angle = MathHelper.clamp_double((Math.sin(millis / 150.0) + 1.0) / 2.0 * 30.0, 0.0, 30.0);
        double scaled = MathHelper.clamp_double((Math.sin(millis / 500.0) + 1.0) / 2.0, 0.8, 1.0);
        double rotate = MathHelper.clamp_double((Math.sin(millis / 1000.0) + 1.0) / 2.0 * 360.0, 0.0, 360.0);
        rotate = (imageMode.getValue() == 1 ? 45 : 0) - (angle - 15.0) + rotate;
        
        int color = ColorUtil.applyOpacity(getInterfaceColor(), 1.0f).getRGB();
        int color2 = ColorUtil.applyOpacity(getInterfaceColor(), 1.0f).getRGB();
        int color3 = ColorUtil.applyOpacity(getInterfaceColor(), 1.0f).getRGB();
        int color4 = ColorUtil.applyOpacity(getInterfaceColor(), 1.0f).getRGB();

        rotate = 45 - (angle - 15.0) + rotate;
        float size = 128.0f * scale * (float) scaled;

        float renderX = x - size / 2.0f;
        float renderY = y - size / 2.0f;
        float x2 = renderX + size;
        float y2 = renderY + size;
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.rotate((float) rotate, 0, 0, 1);
        GlStateManager.translate(-x, -y, 0);
        GL11.glDisable(3008);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.shadeModel(7425);
        GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
        Color HUDColor = getInterfaceColor();
        float alpha = getAlpha();
        GL11.glColor4f(HUDColor.getRed() / 255.0f, HUDColor.getGreen() / 255.0f, HUDColor.getBlue() / 255.0f, alpha);
        switch (imageMode.getValue()){
            case 0:
                RenderUtil.drawImage(rectangle, renderX, renderY, x2, y2, color, color2, color3, color4);
                break;
            case 1:
                RenderUtil.drawImage(quadstapple, renderX, renderY, x2, y2, color, color2, color3, color4);
                break;
            case 2:
                RenderUtil.drawImage(trianglestapple, renderX, renderY, x2, y2, color, color2, color3, color4);
                break;
            case 3:
                RenderUtil.drawImage(trianglestipple, renderX, renderY, x2, y2, color, color2, color3, color4);
                break;
        }

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.resetColor();
        GlStateManager.shadeModel(7424);
        GlStateManager.depthMask(true);
        GL11.glEnable(3008);
        GlStateManager.popMatrix();
    }

    private float[] targetESPSPos(EntityLivingBase entity, Render2DEvent event) {
        EntityRenderer entityRenderer = mc.entityRenderer;
        float partialTicks = event != null ? event.getPartialTicks() : ((IAccessorMinecraft) mc).getTimer().renderPartialTicks;
        double x = MathUtil.interpolate(entity.prevPosX, entity.posX, partialTicks);
        double y = MathUtil.interpolate(entity.prevPosY, entity.posY, partialTicks) + entity.height * 0.4f;
        double z = MathUtil.interpolate(entity.prevPosZ, entity.posZ, partialTicks);
        double width = entity.width / 2.0f;
        double height = entity.height / 4.0f;
        AxisAlignedBB bb = new AxisAlignedBB(x - width, y - height, z - width, x + width, y + height, z + width);
        final double[][] vectors = {{bb.minX, bb.minY, bb.minZ},
                {bb.minX, bb.maxY, bb.minZ},
                {bb.minX, bb.maxY, bb.maxZ},
                {bb.minX, bb.minY, bb.maxZ},
                {bb.maxX, bb.minY, bb.minZ},
                {bb.maxX, bb.maxY, bb.minZ},
                {bb.maxX, bb.maxY, bb.maxZ},
                {bb.maxX, bb.minY, bb.maxZ}};
        ((IAccessorEntityRenderer) entityRenderer).callSetupCameraTransform(partialTicks, 0);
        float[] projection;
        final float[] position = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, -1.0F, -1.0F};
        for (final double[] vec : vectors) {
            projection = GLUtil.project2D((float) (vec[0] - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosX()), (float) (vec[1] - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosY()), (float) (vec[2] - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosZ()), new ScaledResolution(mc).getScaleFactor());
            if (projection != null && projection[2] >= 0.0F && projection[2] < 1.0F) {
                position[0] = Math.min(projection[0], position[0]);
                position[1] = Math.min(projection[1], position[1]);
                position[2] = Math.max(projection[0], position[2]);
                position[3] = Math.max(projection[1], position[3]);
            }
        }
        entityRenderer.setupOverlayRendering();
        float centerX = (position[0] + position[2]) / 2.0f;
        float centerY = (position[1] + position[3]) / 2.0f;
        return new float[]{centerX, centerY};
    }
}