package nightsky.ui.mainmenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import nightsky.util.render.RenderUtil;

public class GuiMainButton {
    public enum ButtonType {
        SINGLEPLAYER(1, "singleplayer.png"),
        MULTIPLAYER(2, "multiplayer.png"),
        ALTMANAGER(3, "altmanager.png"),
        OPTIFINE(4, "option.png"),
        QUIT(5, "quit.png");
        
        public final int id;
        public final String iconPath;
        
        ButtonType(int id, String iconPath) {
            this.id = id;
            this.iconPath = iconPath;
        }
    }
    
    private final int x, y, width, height;
    private final ButtonType type;
    private final Runnable action;
    private float hoverAnimation = 0.0f;
    private long lastTime = System.currentTimeMillis();
    
    private static final ResourceLocation SHADOW_TEXTURE = new ResourceLocation("minecraft", "nightsky/texture/shadow/shadow.png");
    
    public GuiMainButton(int x, int y, int width, int height, ButtonType type, Runnable action) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.action = action;
    }
    
    public void drawButton(int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY);
        
        long currentTime = System.currentTimeMillis();
        float delta = (currentTime - lastTime) * 0.01f;
        lastTime = currentTime;
        
        float targetHover = hovered ? 1.0f : 0.0f;
        this.hoverAnimation += (targetHover - this.hoverAnimation) * delta * 0.1f;
        this.hoverAnimation = Math.max(0.0f, Math.min(1.0f, this.hoverAnimation));
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + width / 2.0f, y + height / 2.0f, 0);
        float scale = 1.0f + this.hoverAnimation * 0.1f;
        GlStateManager.scale(scale, scale, 1.0f);
        GlStateManager.translate(-(width / 2.0f), -(height / 2.0f), 0);
        
        ResourceLocation iconTexture = new ResourceLocation("minecraft", "nightsky/mainbutton/" + type.iconPath);
        
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 0.8f + this.hoverAnimation * 0.2f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(iconTexture);
        RenderUtil.drawTexturedRect(0, 0, width, height);
        GlStateManager.disableBlend();
        
        GlStateManager.popMatrix();
    }
    
    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    
    public void onClick() {
        if (action != null) {
            action.run();
        }
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public ButtonType getType() { return type; }
}
