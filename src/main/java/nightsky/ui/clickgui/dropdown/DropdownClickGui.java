package nightsky.ui.clickgui.dropdown;

import nightsky.NightSky;
import nightsky.module.ModuleCategory;
import nightsky.util.render.RenderUtil;
import nightsky.util.render.ColorUtil;
import nightsky.util.render.animations.advanced.Animation;
import nightsky.util.render.animations.advanced.Direction;
import nightsky.util.render.animations.advanced.impl.EaseBackIn;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DropdownClickGui extends GuiScreen {

    private final Animation fadeAnimation = new nightsky.util.render.animations.advanced.impl.EaseInOutQuad(400, 1);
    private final Animation scaleAnimation = new EaseBackIn(500, 1, 1.7f);
    private final Animation rotateAnimation = new nightsky.util.render.animations.advanced.impl.EaseOutSine(450, 1);
    private List<CategoryPanel> categoryPanels;
    public boolean binding;

    @Override
    public void initGui() {
        fadeAnimation.setDirection(Direction.FORWARDS);
        scaleAnimation.setDirection(Direction.FORWARDS);
        rotateAnimation.setDirection(Direction.FORWARDS);
        
        if (categoryPanels == null) {
            categoryPanels = new ArrayList<>();
            for (ModuleCategory category : ModuleCategory.values()) {
                categoryPanels.add(new CategoryPanel(category, fadeAnimation));
            }
        }

        for (CategoryPanel catPanels : categoryPanels) {
            catPanels.initGui();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE && !binding) {
            fadeAnimation.setDirection(Direction.BACKWARDS);
            scaleAnimation.setDirection(Direction.BACKWARDS);
            rotateAnimation.setDirection(Direction.BACKWARDS);
            return;
        }
        categoryPanels.forEach(categoryPanel -> categoryPanel.keyTyped(typedChar, keyCode));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        binding = categoryPanels.stream().anyMatch(CategoryPanel::isTyping);

        if (scaleAnimation.finished(Direction.BACKWARDS)) {
            mc.displayGuiScreen(null);
            return;
        }

        float alpha = (float) Math.min(1, fadeAnimation.getOutput());
        ScaledResolution sr = new ScaledResolution(mc);
        
        Gui.drawRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), 
            ColorUtil.applyOpacity(new Color(0, 0, 0), alpha * 0.6f).getRGB());

        float centerX = sr.getScaledWidth() / 2f;
        float centerY = sr.getScaledHeight() / 2f;
        float scale = (float) scaleAnimation.getOutput();
        float rotation = (float) ((1 - rotateAnimation.getOutput()) * 15);

        org.lwjgl.opengl.GL11.glPushMatrix();
        org.lwjgl.opengl.GL11.glTranslatef(centerX, centerY, 0);
        org.lwjgl.opengl.GL11.glRotatef(rotation, 0, 0, 1);
        org.lwjgl.opengl.GL11.glScalef(scale, scale, scale);
        org.lwjgl.opengl.GL11.glTranslatef(-centerX, -centerY, 0);

        for (CategoryPanel catPanels : categoryPanels) {
            catPanels.drawScreen(mouseX, mouseY);
        }

        org.lwjgl.opengl.GL11.glPopMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        categoryPanels.forEach(cat -> cat.mouseClicked(mouseX, mouseY, mouseButton));
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        categoryPanels.forEach(cat -> cat.mouseReleased(mouseX, mouseY, state));
    }

    @Override
    public void onGuiClosed() {
        if (categoryPanels != null) {
            for (CategoryPanel panel : categoryPanels) {
                panel.onGuiClosed();
            }
        }
    }
    
    public void onDrag(int mouseX, int mouseY) {
        if (categoryPanels != null) {
            for (CategoryPanel catPanels : categoryPanels) {
                catPanels.onDrag(mouseX, mouseY);
            }
        }
    }
}
