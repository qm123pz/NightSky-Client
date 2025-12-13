package nightsky.module.modules.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import nightsky.events.Render2DEvent;
import nightsky.event.EventTarget;
import nightsky.module.Module;
import nightsky.value.values.IntValue;
import nightsky.util.render.RenderUtil;
import nightsky.util.render.PostProcessing;
import nightsky.font.FontTransformer;
import nightsky.font.CustomFontRenderer;

import java.awt.*;

import net.minecraft.client.shader.Framebuffer;

public class Session extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();
    public final IntValue offsetX = new IntValue("OffsetX", 5, -1000, 1000);
    public final IntValue offsetY = new IntValue("OffsetY", 5, -1000, 1000);
    private final long sessionStartTime;

    public Session() {
        super("Session", false);
        this.sessionStartTime = System.currentTimeMillis();
    }
    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!this.isEnabled()) return;
        ScaledResolution sr = new ScaledResolution(mc);
        FontTransformer transformer = FontTransformer.getInstance();
        Font titleFont = transformer.getFont("Arial", 53);
        Font textFont = transformer.getFont("Arial", 39);
        String title = "Session Information";
        String timeText = "Elapsed Time: " + getTimeString();
        String serverText = "Server Info: " + getServerInfo();
        String userText = "Username: " + getUsername();
        float titleWidth = CustomFontRenderer.getStringWidth(title, titleFont);
        float timeWidth = CustomFontRenderer.getStringWidth(timeText, textFont);
        float serverWidth = CustomFontRenderer.getStringWidth(serverText, textFont);
        float userWidth = CustomFontRenderer.getStringWidth(userText, textFont);
        float maxTextWidth = Math.max(Math.max(timeWidth, serverWidth), userWidth);
        float bgWidth = Math.max(maxTextWidth + 20, titleWidth + 20);
        float bgHeight = 100;
        float cornerRadius = 23;
        float x = sr.getScaledWidth() - bgWidth - 10 + offsetX.getValue();
        float y = 20 + offsetY.getValue();
        Framebuffer bloomBuffer = PostProcessing.beginBloom();
        if (bloomBuffer != null) {
            RenderUtil.drawRoundedRect(x, y, bgWidth, bgHeight, cornerRadius, nightsky.module.modules.render.PostProcessing.getBloomColor());
            mc.getFramebuffer().bindFramebuffer(false);
        }
        PostProcessing.drawBlurRect(x, y, x + bgWidth, y + bgHeight);
        RenderUtil.drawRoundedRect(x, y, bgWidth, bgHeight, cornerRadius, new Color(0, 0, 0, 145).getRGB());
        PostProcessing.endBloom(bloomBuffer);

        float titleX = x + (bgWidth - titleWidth) / 2;
        float titleY = y + 8;
        CustomFontRenderer.drawStringWithShadow(title, titleX, titleY, 0xFFFFFF, titleFont);
        float textX = x + 10;
        float timeY = y + 32;
        float serverY = y + 48;
        float userY = y + 64;
        CustomFontRenderer.drawStringWithShadow(timeText, textX, timeY, 0xFFFFFF, textFont);
        CustomFontRenderer.drawStringWithShadow(serverText, textX, serverY, 0xFFFFFF, textFont);
        CustomFontRenderer.drawStringWithShadow(userText, textX, userY, 0xFFFFFF, textFont);
    }
    private String getTimeString() {
        long currentTime = System.currentTimeMillis();
        long elapsedMillis = currentTime - sessionStartTime;
        long seconds = elapsedMillis / 1000 % 60;
        long minutes = elapsedMillis / 60000 % 60;
        long hours = elapsedMillis / 3600000;
        if (hours > 0) {
            return String.format("%d hour%s %d minute%s %d second%s",
                    hours, hours == 1 ? "" : "s",
                    minutes, minutes == 1 ? "" : "s",
                    seconds, seconds == 1 ? "" : "s");
        } else if (minutes > 0) {
            return String.format("%d minute%s %d second%s",
                    minutes, minutes == 1 ? "" : "s",
                    seconds, seconds == 1 ? "" : "s");
        } else {
            return String.format("%d second%s", seconds, seconds == 1 ? "" : "s");
        }
    }
    private String getServerInfo() {
        return mc.isSingleplayer() ? "Singleplayer" :
                (mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP : "Unknown");
    }
    private String getUsername() {
        return mc.getSession().getUsername();
    }
}