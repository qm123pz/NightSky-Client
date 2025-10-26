package nightsky.ui.mainmenu.altmanager.menus;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.Session;
import nightsky.ui.mainmenu.altmanager.GuiAltManager;
import nightsky.ui.mainmenu.altmanager.SessionUtils;
import nightsky.util.render.RenderUtil;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;

public class GuiSessionLogin extends GuiScreen {

    private GuiAltManager prevGui;
    private GuiButton loginButton;
    private GuiTextField sessionTokenField;
    private String status = "";

    public GuiSessionLogin(GuiAltManager prevGui) {
        this.prevGui = prevGui;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        loginButton = new GuiButton(1, width / 2 - 100, height / 2 - 60, "Login");
        buttonList.add(loginButton);
        buttonList.add(new GuiButton(0, width / 2 - 100, height / 2 - 30, "Back"));

        sessionTokenField = new GuiTextField(666, fontRendererObj, width / 2 - 100, height / 2 - 90, 200, 20);
        sessionTokenField.setFocused(false);
        sessionTokenField.setMaxStringLength(1000);

        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground(0);
        RenderUtil.drawRect(30, 30, width - 30, height - 30, Integer.MIN_VALUE);

        drawCenteredString(fontRendererObj, "Session Login", width / 2, height / 2 - 150, 0xffffff);
        drawCenteredString(fontRendererObj, status, width / 2, height / 2, 0xffffff);

        sessionTokenField.drawTextBox();

        if (sessionTokenField.getText().isEmpty() && !sessionTokenField.isFocused()) {
            fontRendererObj.drawStringWithShadow("§7Session Token", width / 2 - 60, height / 2 - 84, 0xffffff);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) return;

        switch (button.id) {
            case 0:
                mc.displayGuiScreen(prevGui);
                break;
            case 1:
                loginButton.enabled = false;
                status = "§aLogging in...";

                new Thread(() -> {
                    try {
                        String sessionToken = sessionTokenField.getText();
                        if (sessionToken.isEmpty()) {
                            status = "§cPlease enter a session token.";
                            loginButton.enabled = true;
                            return;
                        }

                        String[] parts = sessionToken.split("\\.");
                        if (parts.length < 3) {
                            status = "§cInvalid session token format.";
                            loginButton.enabled = true;
                            return;
                        }

                        String username = "Player" + System.currentTimeMillis() % 10000;
                        String uuid = java.util.UUID.randomUUID().toString().replace("-", "");

                        SessionUtils.setSession(new Session(username, uuid, sessionToken, "microsoft"));
                        status = "§aLogged into §f§l" + mc.getSession().getUsername() + "§a.";

                    } catch (Exception e) {
                        status = "§cLogin failed: " + e.getMessage();
                    }
                    loginButton.enabled = true;
                }).start();
                break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        switch (keyCode) {
            case Keyboard.KEY_ESCAPE:
                mc.displayGuiScreen(prevGui);
                return;
            case Keyboard.KEY_TAB:
                sessionTokenField.setFocused(true);
                return;
            case Keyboard.KEY_RETURN:
                actionPerformed(loginButton);
                return;
        }

        if (sessionTokenField.isFocused()) {
            sessionTokenField.textboxKeyTyped(typedChar, keyCode);
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        sessionTokenField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        sessionTokenField.updateCursorCounter();
        super.updateScreen();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();
    }
}
