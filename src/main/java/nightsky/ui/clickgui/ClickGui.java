package nightsky.ui.clickgui;

import nightsky.ui.clickgui.panel.MainPanel;
import net.minecraft.client.gui.GuiScreen;

public class ClickGui extends GuiScreen {

    private final MainPanel mainPanel = new MainPanel();

    @Override
    public void initGui() {
        mainPanel.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mainPanel.drawScreen(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        mainPanel.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        mainPanel.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        mainPanel.keyTyped(typedChar, keyCode);
    }
}
