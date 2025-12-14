package nightsky.module.modules.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import nightsky.NightSky;
import nightsky.event.EventTarget;
import nightsky.events.Render2DEvent;
import nightsky.font.CustomFontRenderer;
import nightsky.font.FontTransformer;
import nightsky.module.Module;
import nightsky.util.render.ColorUtil;
import nightsky.util.render.PostProcessing;
import nightsky.util.render.RenderUtil;
import nightsky.value.values.IntValue;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.lwjgl.opengl.GL11;

public class PotionEffects extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public final IntValue offsetX = new IntValue("OffsetX", 5, -1000, 1000);
    public final IntValue offsetY = new IntValue("OffsetY", 80, -1000, 1000);

    private float currentHeight = 0.0f;

    private final ResourceLocation inventoryTexture = new ResourceLocation("textures/gui/container/inventory.png");

    public PotionEffects() {
        super("PotionEffects", false);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!this.isEnabled()) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        Collection<PotionEffect> active = mc.thePlayer.getActivePotionEffects();
        if (active == null || active.isEmpty()) return;

        List<PotionEffect> potions = new ArrayList<>(active);
        potions.sort(Comparator.comparingInt(PotionEffect::getDuration).reversed());

        FontTransformer transformer = FontTransformer.getInstance();
        Font font = transformer.getFont("OpenSansSemiBold", 32);
        if (font == null) {
            font = transformer.getFont("Arial", 32);
        }
        if (font == null) return;

        float padding = 5f;
        float fontHeight = CustomFontRenderer.getFontHeight(font);
        String title = "Potions";
        float titleWidth = CustomFontRenderer.getStringWidth(title, font);

        float maxWidth = titleWidth + padding * 2;
        float listHeight = 0f;

        for (PotionEffect effect : potions) {
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            if (potion == null) continue;
            String potionName = I18n.format(potion.getName());
            String amp = effect.getAmplifier() > 0 ? " " + I18n.format("enchantment.level." + (effect.getAmplifier() + 1)) : "";
            String nameText = potionName + amp;
            String durationText = Potion.getDurationString(effect);

            float nameW = CustomFontRenderer.getStringWidth(nameText, font);
            float durW = CustomFontRenderer.getStringWidth(durationText, font);
            float localWidth = nameW + durW + padding * 3 + 12f;

            if (localWidth > maxWidth) maxWidth = localWidth;
            listHeight += fontHeight + padding;
        }

        float width = Math.max(maxWidth, 80f);
        float headerHeight = fontHeight + padding * 2;
        float targetHeight = headerHeight + 1.25f + 7.5f + listHeight + 4.5f;

        if (currentHeight <= 0.0f) {
            currentHeight = targetHeight;
        } else {
            float speed = 0.22f;
            currentHeight += (targetHeight - currentHeight) * speed;
        }

        float height = Math.max(headerHeight, currentHeight);

        ScaledResolution sr = new ScaledResolution(mc);
        float x = sr.getScaledWidth() - width - 10 + offsetX.getValue();
        float y = 20 + offsetY.getValue();

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        Framebuffer bloomBuffer = PostProcessing.beginBloom();
        if (bloomBuffer != null) {
            RenderUtil.drawRoundedRect(x, y, width, height, 4, nightsky.module.modules.render.PostProcessing.getBloomColor());
            mc.getFramebuffer().bindFramebuffer(false);
        }

        PostProcessing.drawBlur(x, y, x + width, y + height, () -> () -> RenderUtil.drawRoundedRect(x, y, width, height, 4, -1));

        Interface interfaceModule = (Interface) NightSky.moduleManager.getModule("Interface");
        int accent = interfaceModule != null ? interfaceModule.color(0) : 0xFF80FF95;

        Color bg = new Color(0, 0, 0, 130);

        RenderUtil.drawRoundedRect(x, y, width, height, 4, bg);

        float titleX = x + width / 2f - titleWidth / 2f;
        CustomFontRenderer.drawStringWithShadow(title, titleX, y + padding + 2f, 0xFFFFFFFF, font);

        float iconSize = 10f;
        float iconX = x + width - iconSize - padding;
        drawPotionHeaderIcon(iconX, y + 6f, iconSize, iconSize, accent);

        float dividerY = y + headerHeight;
        Color dividerColor = new Color(ColorUtil.darker(accent, 0.4f), true);
        RenderUtil.drawRoundedRect(x + 0.5f, dividerY + 1.5f, width - 1f, 1.25f, 3, dividerColor);

        float listY = dividerY + 7.5f;

        RenderUtil.scissorStart(x, y, width, height);

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        for (PotionEffect effect : potions) {
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            if (potion == null) continue;

            String potionName = I18n.format(potion.getName());
            String amp = effect.getAmplifier() > 0 ? " " + I18n.format("enchantment.level." + (effect.getAmplifier() + 1)) : "";
            String nameText = potionName + amp;
            String durationText = Potion.getDurationString(effect);

            float durW = CustomFontRenderer.getStringWidth(durationText, font);

            float potionIconSize = 9f;
            float iconDrawX = x + padding;
            float iconDrawY = listY + 2f + (fontHeight - potionIconSize) / 2f - 2f;
            float textX = x + padding + potionIconSize + 4f;

            drawPotionStatusIcon(potion, iconDrawX, iconDrawY, potionIconSize);

            CustomFontRenderer.drawStringWithShadow(nameText, textX, listY + 2f, 0xFFFFFFFF, font);
            CustomFontRenderer.drawStringWithShadow(durationText, x + width - padding - durW, listY + 2f, 0xFFFFFFFF, font);

            listY += fontHeight + padding;
            if (listY > y + height - padding) break;
        }

        RenderUtil.scissorEnd();

        GlStateManager.disableBlend();
        GlStateManager.enableDepth();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        PostProcessing.endBloom(bloomBuffer);
    }

    private void drawPotionHeaderIcon(float x, float y, float w, float h, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();

        float cx = x + w / 2f;
        float cy = y + h / 2f;
        float r = w / 2.4f;
        Color c = new Color(color, true);
        GL11.glColor4f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(cx, cy);
        for (int i = 0; i <= 20; i++) {
            double a = Math.PI * 2.0 * i / 20.0;
            GL11.glVertex2f((float) (cx + Math.cos(a) * r), (float) (cy + Math.sin(a) * r));
        }
        GL11.glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawPotionStatusIcon(Potion potion, float x, float y, float size) {
        if (potion == null) return;
        if (!potion.hasStatusIcon()) return;

        int idx = potion.getStatusIconIndex();
        int u = (idx % 8) * 18;
        int v = 198 + (idx / 8) * 18;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1f, 1f, 1f, 1f);
        mc.getTextureManager().bindTexture(inventoryTexture);
        float scale = size / 18f;
        GlStateManager.translate(x, y, 0f);
        GlStateManager.scale(scale, scale, 1f);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, u, v, 18, 18, 256, 256);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}