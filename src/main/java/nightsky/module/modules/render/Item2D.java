package nightsky.module.modules.render;

import nightsky.event.EventTarget;
import nightsky.events.Render3DEvent;
import nightsky.mixin.IAccessorRenderManager;
import nightsky.module.Module;
import nightsky.util.RenderUtil;
import nightsky.value.values.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class Item2D extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public final FloatValue size = new FloatValue("Size", 18.0F, 8.0F, 48.0F);
    public final FloatValue maxDistance = new FloatValue("MaxDistance", 48.0F, 8.0F, 128.0F);

    public Item2D() {
        super("Item2D", false);
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (!this.isEnabled()) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        double maxDist = this.maxDistance.getValue().doubleValue();
        float partialTicks = event.getPartialTicks();

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityItem)) continue;

            EntityItem item = (EntityItem) entity;
            if (item.ticksExisted < 1) continue;

            double dist = mc.getRenderViewEntity().getDistance(item.posX, item.posY, item.posZ);
            if (dist > maxDist) continue;
            if (!(item.ignoreFrustumCheck || RenderUtil.isInViewFrustum(item.getEntityBoundingBox(), 0.125))) continue;

            ItemStack stack = item.getEntityItem();
            if (stack == null || stack.stackSize <= 0) continue;

            double worldX = RenderUtil.lerpDouble(item.posX, item.lastTickPosX, partialTicks);
            double worldY = RenderUtil.lerpDouble(item.posY, item.lastTickPosY, partialTicks);
            double worldZ = RenderUtil.lerpDouble(item.posZ, item.lastTickPosZ, partialTicks);

            double x = worldX - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosX();
            double y = worldY - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosY();
            double z = worldZ - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosZ();

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y + 0.15, z);
            GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            float flip = mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F;
            GlStateManager.rotate(mc.getRenderManager().playerViewX * flip, 1.0F, 0.0F, 0.0F);

            float s = this.size.getValue() / 16.0f;
            GlStateManager.scale(s, s, s);

            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
            GlStateManager.disableCull();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);

            mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

            TextureAtlasSprite sprite = mc.getRenderItem().getItemModelMesher().getParticleIcon(stack.getItem());
            if (sprite != null) {
                float u0 = sprite.getMinU();
                float v0 = sprite.getMinV();
                float u1 = sprite.getMaxU();
                float v1 = sprite.getMaxV();

                float half = 0.5f;
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer wr = tessellator.getWorldRenderer();
                wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                wr.pos(-half, -half, 0.0).tex(u0, v1).endVertex();
                wr.pos(half, -half, 0.0).tex(u1, v1).endVertex();
                wr.pos(half, half, 0.0).tex(u1, v0).endVertex();
                wr.pos(-half, half, 0.0).tex(u0, v0).endVertex();
                tessellator.draw();
            }

            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }
}