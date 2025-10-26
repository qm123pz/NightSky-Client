package nightsky.ui.mainmenu.altmanager.menus;

import me.liuli.elixir.account.MicrosoftAccount;
import me.liuli.elixir.compat.OAuthServer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import nightsky.ui.mainmenu.altmanager.AccountsConfig;

import java.awt.*;
import java.io.IOException;
import java.net.BindException;
import java.util.function.Consumer;

public class GuiMicrosoftLoginProgress extends GuiScreen {

    private Consumer<String> updateStatus;
    private Runnable done;
    private OAuthServer oAuthServer;
    private String loginUrl;
    private boolean serverStopAlreadyCalled = false;

    public GuiMicrosoftLoginProgress(Consumer<String> updateStatus, Runnable done) {
        this.updateStatus = updateStatus;
        this.done = done;
    }

    @Override
    public void initGui() {
        try {
            oAuthServer = MicrosoftAccount.buildFromOpenBrowser(new MicrosoftAccount.OAuthHandler() {
                @Override
                public void authError(String error) {
                    serverStopAlreadyCalled = true;
                    errorAndDone(error);
                    loginUrl = null;
                }

                @Override
                public void authResult(MicrosoftAccount account) {
                    serverStopAlreadyCalled = true;
                    loginUrl = null;
                    
                    if (AccountsConfig.getInstance().accountExists(account)) {
                        errorAndDone("The account has already been added.");
                        return;
                    }

                    AccountsConfig.getInstance().addAccount(account);
                    AccountsConfig.getInstance().saveConfig();
                    successAndDone();
                }

                @Override
                public void openUrl(String url) {
                    loginUrl = url;
                    try {
                        Desktop.getDesktop().browse(java.net.URI.create(url));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            
            oAuthServer.start();
            
        } catch (Exception e) {
            errorAndDone("Failed to start login server: " + e.getMessage());
            e.printStackTrace();
        }

        buttonList.add(new GuiButton(0, width / 2 - 100, height / 2 + 60, "Open URL"));
        buttonList.add(new GuiButton(1, width / 2 - 100, height / 2 + 90, "Cancel"));

        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        
        drawCenteredString(fontRendererObj, "Logging into account", width / 2, height / 2 - 60, 0xffffff);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) {
            return;
        }

        switch (button.id) {
            case 0:
                if (loginUrl != null) {
                    try {
                        Desktop.getDesktop().browse(java.net.URI.create(loginUrl));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 1:
                errorAndDone("Login cancelled.");
                break;
        }

        super.actionPerformed(button);
    }

    @Override
    public void onGuiClosed() {
        if (!serverStopAlreadyCalled && oAuthServer != null) {
            oAuthServer.stop(false);
        }
        super.onGuiClosed();
    }

    private void successAndDone() {
        updateStatus.accept("§aSuccessfully logged in.");
        done.run();
    }

    private void errorAndDone(String error) {
        updateStatus.accept("§c" + error);
        done.run();
    }
}
