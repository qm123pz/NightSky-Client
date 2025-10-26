package nightsky.module.modules.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import nightsky.events.Render2DEvent;
import nightsky.event.EventTarget;
import nightsky.module.Module;
import nightsky.value.values.FloatValue;
import nightsky.value.values.ModeValue;
import nightsky.NightSky;

public class WaterMark extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();
    public final FloatValue scale = new FloatValue("Scale", 5.0F, 1.0F, 7.0F);
    public final ModeValue mode = new ModeValue("Mode", 2, new String[]{"Logo1", "Logo2","Logo3", "YuanShen", "Exhibition"});
    private final ResourceLocation logoTexture = new ResourceLocation("minecraft", "nightsky/logo/Logo1.png");
    private final ResourceLocation logoTexture2 = new ResourceLocation("minecraft", "nightsky/logo/Logo2.png");
    private final ResourceLocation logoTexture3 = new ResourceLocation("minecraft", "nightsky/logo/Logo3.png");
    private final ResourceLocation logoTexture4 = new ResourceLocation("minecraft", "nightsky/logo/op.png");
    public WaterMark() {
        super("WaterMark", true);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!this.isEnabled()) return;
        
        if (mode.getValue() == 4) {
            renderExhibitionMode();
            return;
        }
        
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        switch (mode.getValue()) {
            case 0:
                mc.getTextureManager().bindTexture(logoTexture);
                break;
            case 1:
                mc.getTextureManager().bindTexture(logoTexture2);
                break;
            case 2:
                mc.getTextureManager().bindTexture(logoTexture3);
                break;
            case 3:
                mc.getTextureManager().bindTexture(logoTexture4);
                break;
        }
        float scaleValue = scale.getValue();
        int logoSize = (int)(32 * scaleValue);
        int x = 5;
        int y = 1;
        if (scaleValue != 1.0F) {
            GlStateManager.scale(scaleValue, scaleValue, 1.0F);
            x = (int)(x / scaleValue);
            y = (int)(y / scaleValue);
            logoSize = 32;
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, logoSize, logoSize, logoSize, logoSize);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
    
    private void renderExhibitionMode() {
        Interface interfaceModule = (Interface) NightSky.moduleManager.getModule("Interface");
        int fps = Minecraft.getDebugFPS();
        
        float x = 5.0f;
        float y = 5.0f;
        
        int nColor = interfaceModule != null ? interfaceModule.color(0) : 0xFFFFFF;
        int whiteColor = 0xFFFFFF;
        int grayColor = 0xAAAAAA;
        
        mc.fontRendererObj.drawStringWithShadow("N", x, y, nColor);
        float nWidth = mc.fontRendererObj.getStringWidth("N");
        
        mc.fontRendererObj.drawStringWithShadow("ightSky ", x + nWidth, y, whiteColor);
        float nightSkyWidth = mc.fontRendererObj.getStringWidth("NightSky ");
        
        mc.fontRendererObj.drawStringWithShadow("[", x + nightSkyWidth, y, grayColor);
        float bracketWidth = mc.fontRendererObj.getStringWidth("[");
        
        String fpsText = fps + " FPS";
        mc.fontRendererObj.drawStringWithShadow(fpsText, x + nightSkyWidth + bracketWidth, y, whiteColor);
        float fpsWidth = mc.fontRendererObj.getStringWidth(fpsText);
        
        mc.fontRendererObj.drawStringWithShadow("]", x + nightSkyWidth + bracketWidth + fpsWidth, y, grayColor);
    }
}