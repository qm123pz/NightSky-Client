package nightsky.ui.auth;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import nightsky.ui.mainmenu.GuiMainMenu;
import nightsky.checkerLOL.AuthManager;
import nightsky.checkerLOL.KamiActivationManager;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiAuthSystem extends GuiScreen {

    public enum AuthMode {
        LOGIN,
        REGISTER,
        ACTIVATE
    }

    private AuthMode currentMode;
    private GuiTextField usernameField;
    private GuiTextField passwordField;
    private GuiTextField emailField;
    private GuiTextField keyField;
    private String statusMessage = "";
    private boolean isLoading = false;

    public GuiAuthSystem(AuthMode mode) {
        this.currentMode = mode;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        
        if (AuthManager.getInstance().isAuthenticated()) {
            mc.displayGuiScreen(new GuiMainMenu());
            return;
        }

        initInputFields();
    }

    private void initInputFields() {
        int fieldWidth = 200;
        int fieldHeight = 20;
        int centerX = width / 2 - fieldWidth / 2;

        usernameField = new GuiTextField(0, fontRendererObj, centerX, height / 2 - 40, fieldWidth, fieldHeight);
        usernameField.setMaxStringLength(50);
        usernameField.setFocused(true);

        passwordField = new GuiTextField(1, fontRendererObj, centerX, height / 2 - 10, fieldWidth, fieldHeight);
        passwordField.setMaxStringLength(100);

        emailField = new GuiTextField(2, fontRendererObj, centerX, height / 2 + 20, fieldWidth, fieldHeight);
        emailField.setMaxStringLength(100);

        keyField = new GuiTextField(3, fontRendererObj, centerX, height / 2 + 20, fieldWidth, fieldHeight);
        keyField.setMaxStringLength(64);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground(0);

        switch (currentMode) {
            case LOGIN:
                drawLoginScreen();
                break;
            case REGISTER:
                drawRegisterScreen();
                break;
            case ACTIVATE:
                drawActivateScreen();
                break;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawLoginScreen() {
        int panelWidth = 320;
        int panelHeight = 200;
        int panelX = width / 2 - panelWidth / 2;
        int panelY = height / 2 - panelHeight / 2;
        
        drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xCC000000);
        drawRect(panelX + 1, panelY + 1, panelX + panelWidth - 1, panelY + panelHeight - 1, 0x88333333);
        
        drawCenteredString(fontRendererObj, "Login", width / 2, panelY + 20, 0xFFFFFF);
        
        drawString(fontRendererObj, "Username:", usernameField.xPosition, usernameField.yPosition - 15, 0xCCCCCC);
        drawString(fontRendererObj, "Password:", passwordField.xPosition, passwordField.yPosition - 15, 0xCCCCCC);

        usernameField.drawTextBox();
        passwordField.drawTextBox();

        int buttonY = height / 2 + 40;
        drawRect(width / 2 - 40, buttonY - 5, width / 2 + 40, buttonY + 15, 0xFF4682B4);
        drawCenteredString(fontRendererObj, "Login", width / 2, buttonY, 0xFFFFFF);
        
        int backY = height / 2 + 65;
        drawRect(width / 2 - 25, backY - 5, width / 2 + 25, backY + 15, 0xFF666666);
        drawCenteredString(fontRendererObj, "Back", width / 2, backY, 0xFFFFFF);

        if (!statusMessage.isEmpty()) {
            drawCenteredString(fontRendererObj, statusMessage, width / 2, height / 2 + 90, 0xFF6666);
        }
    }

    private void drawRegisterScreen() {
        drawCenteredString(fontRendererObj, "Register", width / 2, height / 2 - 100, 0xFFFFFF);
        
        drawString(fontRendererObj, "Username:", usernameField.xPosition, usernameField.yPosition - 15, 0xFFFFFF);
        drawString(fontRendererObj, "Email:", emailField.xPosition, emailField.yPosition - 15, 0xFFFFFF);
        drawString(fontRendererObj, "Password:", passwordField.xPosition, passwordField.yPosition - 15, 0xFFFFFF);

        usernameField.drawTextBox();
        emailField.drawTextBox();
        passwordField.drawTextBox();

        drawCenteredString(fontRendererObj, "Register", width / 2, height / 2 + 60, 0x4682B4);
        drawCenteredString(fontRendererObj, "Back", width / 2, height / 2 + 80, 0x808080);

        if (!statusMessage.isEmpty()) {
            drawCenteredString(fontRendererObj, statusMessage, width / 2, height / 2 + 100, 0xFF0000);
        }
    }

    private void drawActivateScreen() {
        drawCenteredString(fontRendererObj, "Activate Key", width / 2, height / 2 - 100, 0xFFFFFF);
        
        drawString(fontRendererObj, "Username:", usernameField.xPosition, usernameField.yPosition - 15, 0xFFFFFF);
        drawString(fontRendererObj, "Password:", passwordField.xPosition, passwordField.yPosition - 15, 0xFFFFFF);
        drawString(fontRendererObj, "Key:", keyField.xPosition, keyField.yPosition - 15, 0xFFFFFF);

        usernameField.drawTextBox();
        passwordField.drawTextBox();
        keyField.drawTextBox();

        drawCenteredString(fontRendererObj, "Activate", width / 2, height / 2 + 60, 0x4682B4);
        drawCenteredString(fontRendererObj, "Back", width / 2, height / 2 + 80, 0x808080);

        if (!statusMessage.isEmpty()) {
            drawCenteredString(fontRendererObj, statusMessage, width / 2, height / 2 + 100, 0xFF0000);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (isLoading) return;

        usernameField.mouseClicked(mouseX, mouseY, mouseButton);
        passwordField.mouseClicked(mouseX, mouseY, mouseButton);
        
        if (currentMode == AuthMode.REGISTER) {
            emailField.mouseClicked(mouseX, mouseY, mouseButton);
        }
        
        if (currentMode == AuthMode.ACTIVATE) {
            keyField.mouseClicked(mouseX, mouseY, mouseButton);
        }

        int actionY = height / 2 + (currentMode == AuthMode.REGISTER ? 60 : 60);
        int backY = height / 2 + (currentMode == AuthMode.REGISTER ? 80 : 80);

        if (mouseX >= width / 2 - 30 && mouseX <= width / 2 + 30 && mouseY >= actionY - 5 && mouseY <= actionY + 10) {
            performAction();
        }

        if (mouseX >= width / 2 - 20 && mouseX <= width / 2 + 20 && mouseY >= backY - 5 && mouseY <= backY + 10) {
            mc.displayGuiScreen(new GuiAuth());
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_RETURN) {
            performAction();
            return;
        }

        if (usernameField.isFocused()) {
            usernameField.textboxKeyTyped(typedChar, keyCode);
        } else if (passwordField.isFocused()) {
            passwordField.textboxKeyTyped(typedChar, keyCode);
        } else if (currentMode == AuthMode.REGISTER && emailField.isFocused()) {
            emailField.textboxKeyTyped(typedChar, keyCode);
        } else if (currentMode == AuthMode.ACTIVATE && keyField.isFocused()) {
            keyField.textboxKeyTyped(typedChar, keyCode);
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen() {
        usernameField.updateCursorCounter();
        passwordField.updateCursorCounter();
        if (currentMode == AuthMode.REGISTER) {
            emailField.updateCursorCounter();
        }
        if (currentMode == AuthMode.ACTIVATE) {
            keyField.updateCursorCounter();
        }
    }

    private void performAction() {
        if (isLoading) return;

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        switch (currentMode) {
            case LOGIN:
                if (username.isEmpty() || password.isEmpty()) {
                    statusMessage = "Please fill all fields";
                    return;
                }
                performLogin(username, password);
                break;

            case REGISTER:
                String email = emailField.getText().trim();
                if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    statusMessage = "Please fill all fields";
                    return;
                }
                performRegister(username, password, email);
                break;

            case ACTIVATE:
                String key = keyField.getText().trim();
                if (username.isEmpty() || password.isEmpty() || key.isEmpty()) {
                    statusMessage = "Please fill all fields";
                    return;
                }
                performActivate(username, password, key);
                break;
        }
    }

    private void performLogin(String username, String password) {
        isLoading = true;
        statusMessage = "Logging in...";

        new Thread(() -> {
            try {
                boolean success = AuthManager.getInstance().login(username, password);
                if (success) {
                    mc.displayGuiScreen(new GuiMainMenu());
                } else {
                    statusMessage = AuthManager.getInstance().getLastLoginError();
                    isLoading = false;
                }
            } catch (Exception e) {
                statusMessage = "Login error: " + e.getMessage();
                isLoading = false;
            }
        }).start();
    }

    private void performRegister(String username, String password, String email) {
        isLoading = true;
        statusMessage = "Registering...";

        new Thread(() -> {
            try {
                boolean success = AuthManager.getInstance().register(username, password, email);
                if (success) {
                    statusMessage = "Registration successful! Please login.";
                    currentMode = AuthMode.LOGIN;
                    initInputFields();
                } else {
                    statusMessage = "Registration failed";
                }
                isLoading = false;
            } catch (Exception e) {
                statusMessage = "Registration error: " + e.getMessage();
                isLoading = false;
            }
        }).start();
    }

    private void performActivate(String username, String password, String key) {
        isLoading = true;
        statusMessage = "Activating...";

        new Thread(() -> {
            try {
                boolean success = KamiActivationManager.getInstance().activate(username, password, key);
                if (success) {
                    AuthManager.getInstance().login(username, password);
                    mc.displayGuiScreen(new GuiMainMenu());
                } else {
                    statusMessage = KamiActivationManager.getInstance().getLastActivationError();
                    isLoading = false;
                }
            } catch (Exception e) {
                statusMessage = "Activation error: " + e.getMessage();
                isLoading = false;
            }
        }).start();
    }
}
