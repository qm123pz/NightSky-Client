package nightsky.ui.mainmenu.altmanager.menus;

import me.liuli.elixir.account.CrackedAccount;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.Session;
import nightsky.ui.mainmenu.altmanager.AccountsConfig;
import nightsky.ui.mainmenu.altmanager.GuiAltManager;
import nightsky.ui.mainmenu.altmanager.SessionUtils;
import nightsky.util.render.RenderUtil;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.Random;

public class GuiLoginIntoAccount extends GuiScreen {

    private GuiAltManager prevGui;
    private boolean directLogin;
    private GuiButton addButton;
    private GuiTextField username;
    private String status = "";

    public GuiLoginIntoAccount(GuiAltManager prevGui) {
        this(prevGui, false);
    }

    public GuiLoginIntoAccount(GuiAltManager prevGui, boolean directLogin) {
        this.prevGui = prevGui;
        this.directLogin = directLogin;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        addButton = new GuiButton(1, width / 2 - 100, height / 2 - 60, directLogin ? "Login" : "Add");
        buttonList.add(addButton);

        buttonList.add(new GuiButton(2, width / 2 + 105, height / 2 - 90, 40, 20, "Random"));
        buttonList.add(new GuiButton(3, width / 2 - 100, height / 2, (directLogin ? "Login to" : "Add") + " a Microsoft account"));
        buttonList.add(new GuiButton(0, width / 2 - 100, height / 2 + 30, "Back"));

        username = new GuiTextField(2, fontRendererObj, width / 2 - 100, height / 2 - 90, 200, 20);
        username.setFocused(false);
        username.setMaxStringLength(16);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground(0);

        RenderUtil.drawRect(30, 30, width - 30, height - 30, Integer.MIN_VALUE);
        
        drawCenteredString(fontRendererObj, directLogin ? "Direct Login" : "Add Account", width / 2, height / 2 - 170, 0xffffff);
        drawCenteredString(fontRendererObj, "§7" + (directLogin ? "Login to" : "Add") + " an offline account", width / 2, height / 2 - 110, 0xffffff);
        drawCenteredString(fontRendererObj, status, width / 2, height / 2 - 30, 0xffffff);

        username.drawTextBox();

        if (username.getText().isEmpty() && !username.isFocused()) {
            fontRendererObj.drawStringWithShadow("§7Username", width / 2 - 72, height / 2 - 84, 0xffffff);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) {
            return;
        }

        switch (button.id) {
            case 0:
                mc.displayGuiScreen(prevGui);
                break;
            case 1:
                String usernameText = username.getText();
                checkAndAddAccount(usernameText);
                break;
            case 2:
                username.setText(randomUsername());
                break;
            case 3:
                mc.displayGuiScreen(new GuiMicrosoftLoginProgress(
                    (statusText) -> status = statusText,
                    () -> {
                        prevGui.status = status;
                        mc.displayGuiScreen(prevGui);
                    }
                ));
                break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        switch (keyCode) {
            case Keyboard.KEY_ESCAPE:
                mc.displayGuiScreen(prevGui);
                return;
            case Keyboard.KEY_RETURN:
                actionPerformed(addButton);
                return;
            case Keyboard.KEY_TAB:
                username.setFocused(true);
                return;
        }

        if (username.isFocused()) {
            username.textboxKeyTyped(typedChar, keyCode);
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        username.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        username.updateCursorCounter();
        super.updateScreen();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();
    }

    private void checkAndAddAccount(String usernameText) {
        if (usernameText.isEmpty() || usernameText.length() < 3) {
            status = "§cInput at least 3 characters long username.";
            return;
        }

        CrackedAccount crackedAccount = new CrackedAccount();
        try {
            java.lang.reflect.Field nameField = CrackedAccount.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(crackedAccount, usernameText);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (AccountsConfig.getInstance().accountExists(crackedAccount)) {
            status = "§cThis account already exists.";
            return;
        }

        addButton.enabled = false;

        if (directLogin) {
            SessionUtils.setSession(new Session(
                crackedAccount.getSession().getUsername(),
                crackedAccount.getSession().getUuid(),
                crackedAccount.getSession().getToken(),
                crackedAccount.getSession().getType()
            ));
            status = "§aLogged into §f§l" + mc.getSession().getUsername() + "§a.";
        } else {
            AccountsConfig.getInstance().addAccount(crackedAccount);
            AccountsConfig.getInstance().saveConfig();
            status = "§aThe account has been added.";
        }

        prevGui.status = status;
        mc.displayGuiScreen(prevGui);
    }

    private String randomUsername() {
        String[] names = {"Player", "User", "Gamer", "Pro", "Epic", "Cool", "Super", "Mega", "Ultra", "Best"};
        String[] suffixes = {"123", "456", "789", "2024", "Pro", "Gaming", "MC", "PvP", "God", "King"};
        Random random = new Random();
        return names[random.nextInt(names.length)] + suffixes[random.nextInt(suffixes.length)];
    }
}
