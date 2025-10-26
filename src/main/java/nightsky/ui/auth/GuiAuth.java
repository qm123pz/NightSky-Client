package nightsky.ui.auth;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import nightsky.font.FontRenderer;
import nightsky.util.render.BlurUtil;
import nightsky.util.render.RenderUtil;
import org.lwjgl.input.Keyboard;

import java.awt.Color;
import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;

public class GuiAuth extends GuiScreen {
    
    private AuthButton loginButton;
    private AuthButton activateButton;
    private AuthButton registerButton;
    private AuthButton exitButton;
    
    //dev模式 true=skip false=正常验证
    //auth ui只是我懒得删，但这并不代表你可以拿这个端当底base去二改圈钱
    private static final boolean devtest = true;

    public static boolean isDevTestMode() {
        return devtest;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        
        ScaledResolution sr = new ScaledResolution(this.mc);
        int centerX = sr.getScaledWidth() / 2;
        int centerY = sr.getScaledHeight() / 2;
        
        loginButton = new AuthButton(centerX - 100, centerY - 40, 200, 30, "Login", () -> {
            this.mc.displayGuiScreen(new GuiAuthSystem(GuiAuthSystem.AuthMode.LOGIN));
        });
        
        activateButton = new AuthButton(centerX - 100, centerY, 200, 30, "Activate Key", () -> {
            this.mc.displayGuiScreen(new GuiAuthSystem(GuiAuthSystem.AuthMode.ACTIVATE));
        });
        
        registerButton = new AuthButton(centerX - 100, centerY + 40, 200, 30, "Register", () -> {
            this.mc.displayGuiScreen(new GuiAuthSystem(GuiAuthSystem.AuthMode.REGISTER));
        });
        
        exitButton = new AuthButton(sr.getScaledWidth() - 80, sr.getScaledHeight() - 40, 70, 30, "Exit", () -> {
            this.mc.shutdown();
        });
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground();
        
        ScaledResolution sr = new ScaledResolution(this.mc);
        int centerX = sr.getScaledWidth() / 2;
        int centerY = sr.getScaledHeight() / 2;
        
        int panelWidth = 350;
        int panelHeight = 280;
        int panelX = centerX - panelWidth / 2;
        int panelY = centerY - panelHeight / 2;
        
        BlurUtil.blurAreaRounded(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 15, 80f);
        
        RenderUtil.drawRoundedRect(panelX, panelY, panelWidth, panelHeight, 15, new Color(15, 15, 25, 200));
        RenderUtil.drawRoundedRect(panelX + 2, panelY + 2, panelWidth - 4, panelHeight - 4, 13, new Color(25, 25, 35, 150));
        
        RenderUtil.drawRoundedRect(panelX + 20, panelY + 15, panelWidth - 40, 2, 1, new Color(100, 150, 255, 180));
        
        String title = "NightSky";
        int titleWidth = FontRenderer.getStringWidth(title);
        FontRenderer.drawString(title, centerX - titleWidth / 2, panelY + 30, new Color(255, 255, 255, 255).getRGB());
        
        String subtitle = "Authentication System";
        int subtitleWidth = FontRenderer.getStringWidth(subtitle);
        FontRenderer.drawString(subtitle, centerX - subtitleWidth / 2, panelY + 50, new Color(180, 180, 180, 200).getRGB());
        
        if (loginButton != null) loginButton.drawButton(mouseX, mouseY);
        if (activateButton != null) activateButton.drawButton(mouseX, mouseY);
        if (registerButton != null) registerButton.drawButton(mouseX, mouseY);
        if (exitButton != null) exitButton.drawButton(mouseX, mouseY);
        
        String version = "v1.0";
        FontRenderer.drawString(version, panelX + panelWidth - 30, panelY + panelHeight - 15, new Color(120, 120, 120, 150).getRGB());
    }
    
    private void drawBackground() {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        long time = System.currentTimeMillis();
        float animTime = (time % 10000) / 10000.0f;
        
        Color topLeft = new Color(10, 15, 35);
        Color topRight = new Color(25, 35, 60);
        Color bottomLeft = new Color(15, 25, 45);
        Color bottomRight = new Color(35, 45, 75);
        
        float wave = (float) (Math.sin(animTime * Math.PI * 2) * 0.1f + 0.9f);
        
        int tl = blendColors(topLeft, new Color(20, 30, 50), wave);
        int tr = blendColors(topRight, new Color(40, 50, 80), wave);
        int bl = blendColors(bottomLeft, new Color(25, 35, 55), wave);
        int br = blendColors(bottomRight, new Color(50, 60, 90), wave);
        
        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(0, this.height, 0).color((tl >> 16) & 0xFF, (tl >> 8) & 0xFF, tl & 0xFF, 255).endVertex();
        worldRenderer.pos(this.width, this.height, 0).color((br >> 16) & 0xFF, (br >> 8) & 0xFF, br & 0xFF, 255).endVertex();
        worldRenderer.pos(this.width, 0, 0).color((tr >> 16) & 0xFF, (tr >> 8) & 0xFF, tr & 0xFF, 255).endVertex();
        worldRenderer.pos(0, 0, 0).color((bl >> 16) & 0xFF, (bl >> 8) & 0xFF, bl & 0xFF, 255).endVertex();
        tessellator.draw();
        
        drawStars();
        
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
    
    private int blendColors(Color c1, Color c2, float ratio) {
        int r = (int) (c1.getRed() * (1 - ratio) + c2.getRed() * ratio);
        int g = (int) (c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
        int b = (int) (c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio);
        return (r << 16) | (g << 8) | b;
    }
    
    private void drawStars() {
        long time = System.currentTimeMillis();
        float animTime = (time % 20000) / 20000.0f;
        
        for (int i = 0; i < 50; i++) {
            float x = (i * 123.456f) % this.width;
            float y = (i * 789.123f) % this.height;
            float alpha = (float) (Math.sin(animTime * Math.PI * 2 + i * 0.1f) * 0.3f + 0.7f);
            
            int starColor = new Color(255, 255, 255, (int) (alpha * 100)).getRGB();
            RenderUtil.drawRoundedRect((int) x, (int) y, 2, 2, 1, new Color(starColor));
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (loginButton != null && loginButton.isHovered(mouseX, mouseY)) {
            loginButton.onClick();
        }
        if (activateButton != null && activateButton.isHovered(mouseX, mouseY)) {
            activateButton.onClick();
        }
        if (registerButton != null && registerButton.isHovered(mouseX, mouseY)) {
            registerButton.onClick();
        }
        if (exitButton != null && exitButton.isHovered(mouseX, mouseY)) {
            exitButton.onClick();
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
    
    public static class AuthButton {
        private final int x, y, width, height;
        private final String text;
        private final Runnable action;
        private float hoverAnimation = 0.0f;
        private long lastTime = System.currentTimeMillis();
        
        public AuthButton(int x, int y, int width, int height, String text, Runnable action) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
            this.action = action;
        }
        
        public void drawButton(int mouseX, int mouseY) {
            boolean hovered = isHovered(mouseX, mouseY);
            
            long currentTime = System.currentTimeMillis();
            float delta = (currentTime - lastTime) * 0.01f;
            lastTime = currentTime;
            
            float targetHover = hovered ? 1.0f : 0.0f;
            this.hoverAnimation += (targetHover - this.hoverAnimation) * delta * 0.15f;
            this.hoverAnimation = Math.max(0.0f, Math.min(1.0f, this.hoverAnimation));
            
            Color baseColor = new Color(45, 55, 85, 180);
            Color hoverColor = new Color(70, 130, 255, 200);
            Color borderColor = new Color(100, 150, 255, (int)(100 + this.hoverAnimation * 100));
            
            int r = (int)(baseColor.getRed() * (1 - this.hoverAnimation) + hoverColor.getRed() * this.hoverAnimation);
            int g = (int)(baseColor.getGreen() * (1 - this.hoverAnimation) + hoverColor.getGreen() * this.hoverAnimation);
            int b = (int)(baseColor.getBlue() * (1 - this.hoverAnimation) + hoverColor.getBlue() * this.hoverAnimation);
            int a = (int)(baseColor.getAlpha() * (1 - this.hoverAnimation) + hoverColor.getAlpha() * this.hoverAnimation);
            
            Color bgColor = new Color(r, g, b, a);
            
            RenderUtil.drawRoundedRect(x, y, width, height, 8, bgColor);
            RenderUtil.drawRoundedRect(x, y, width, 1, 1, borderColor);
            
            if (hovered) {
                RenderUtil.drawRoundedRect(x + 1, y + 1, width - 2, height - 2, 7, new Color(255, 255, 255, 20));
            }
            
            int textColor = new Color(255, 255, 255, (int)(200 + this.hoverAnimation * 55)).getRGB();
            int textWidth = FontRenderer.getStringWidth(text);
            FontRenderer.drawString(text, x + width / 2 - textWidth / 2, y + height / 2 - 4, textColor);
        }
        
        public boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
        public void onClick() {
            if (action != null) {
                action.run();
            }
        }
    }
}
