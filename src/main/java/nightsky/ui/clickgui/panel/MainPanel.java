package nightsky.ui.clickgui.panel;

import nightsky.ui.Screen;
import nightsky.util.misc.HoverUtil;
import nightsky.util.object.Drag;
import nightsky.util.render.Render2DUtil;
import nightsky.util.render.animations.advanced.Animation;
import nightsky.util.render.animations.advanced.Direction;
import nightsky.util.render.animations.advanced.impl.DecelerateAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class MainPanel implements Screen {
    private final ModulePanel modulePanel = new ModulePanel();
    private final Minecraft mc = Minecraft.getMinecraft();
    public ScaledResolution sr;
    private Animation openAnimation;
    private Drag drag;
    private FontRenderer fontRenderer;

    @Override
    public void initGui() {
        sr = new ScaledResolution(mc);
        drag = new Drag(sr.getScaledWidth() / 2f - 250f, sr.getScaledHeight() / 2f - 150f);
        openAnimation = new DecelerateAnimation(150, 1);
        openAnimation.setDirection(Direction.FORWARDS);
        fontRenderer = mc.fontRendererObj;

        modulePanel.initGui();
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) {
            openAnimation.setDirection(Direction.BACKWARDS);
        }
        modulePanel.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        drag.onDraw(mouseX, mouseY);
        
        if (openAnimation.getDirection().equals(Direction.BACKWARDS) && openAnimation.isDone()) {
            mc.displayGuiScreen(null);
            return;
        }
        
        final float x = drag.getX(), y = drag.getY();
        final float openOutput = (float) openAnimation.getOutput();

        final Color baseColor = new Color(60, 60, 60);
        final Color lineColor = new Color(120, 120, 120);
        final Color textColor = new Color(186, 186, 186);
        final Color insideColor = new Color(43, 43, 43);
        
        final float firstRectHeight = fontRenderer.FONT_HEIGHT + 4;
        final float secondRectHeight = fontRenderer.FONT_HEIGHT + 6;

        Render2DUtil.scaleStart(x + 250, y + 150, openOutput);
        Render2DUtil.drawRect(x - 0.5f, y - 0.5f, 501, 351, lineColor);
        Render2DUtil.drawRect(x, y, 500, 350, baseColor);
        Render2DUtil.drawRect(x, y + firstRectHeight + secondRectHeight, 500, 350 - (firstRectHeight + secondRectHeight), insideColor);
        Render2DUtil.drawRect(x, y + firstRectHeight + secondRectHeight + 0.5f, 100, 350 - (firstRectHeight + secondRectHeight + 0.5f), baseColor);

        fontRenderer.drawString("File  Edit  View  Navigate  Code  Refactor  Build  Run  Tools  VCS  Window  Help", (int)(x + 2), (int)(y + 2), textColor.getRGB());
        fontRenderer.drawString("NightSky Client      |      IdeaClickGui By Instance漫杏", (int)(x + 4), (int)(y + firstRectHeight + 3), textColor.getRGB());
        modulePanel.setX(drag.getX());
        modulePanel.setY(drag.getY());
        modulePanel.drawScreen(mouseX, mouseY);
        
        Render2DUtil.scaleEnd();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        final float x = drag.getX(), y = drag.getY();
        final float headerHeight = fontRenderer.FONT_HEIGHT + fontRenderer.FONT_HEIGHT + 10;
        drag.onClick(mouseX, mouseY, button, 
            HoverUtil.isHovered(mouseX, mouseY, x, y, 500, headerHeight));
        modulePanel.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        drag.onRelease(state);
        modulePanel.mouseReleased(mouseX, mouseY, state);
    }
}