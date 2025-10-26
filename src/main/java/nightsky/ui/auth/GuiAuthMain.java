package nightsky.ui.auth;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import nightsky.ui.mainmenu.GuiMainMenu;
import nightsky.checkerLOL.AuthManager;
import nightsky.checkerLOL.KamiActivationManager;
import nightsky.util.render.BlurUtil;
import nightsky.util.render.RenderUtil;
import nightsky.font.FontRenderer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.Color;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

public class GuiAuthMain extends GuiScreen {

    public enum AuthMode {
        MAIN, LOGIN, REGISTER, ACTIVATE
    }

    private AuthMode currentMode = AuthMode.MAIN;
    private AuthMode targetMode = AuthMode.MAIN;
    private float modeTransition = 1.0f;
    private long lastTime = System.currentTimeMillis();
    
    private CustomTextField usernameField;
    private CustomTextField passwordField;
    private CustomTextField emailField;
    private CustomTextField keyField;
    
    private String statusMessage = "";
    private float statusAlpha = 0.0f;
    private boolean isLoading = false;
    private float loadingRotation = 0.0f;
    private boolean loginSuccess = false;
    private float successAlpha = 1.0f;
    private long successStartTime = 0;

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        
        ScaledResolution sr = new ScaledResolution(this.mc);
        int centerX = sr.getScaledWidth() / 2;
        int centerY = sr.getScaledHeight() / 2;
        
        usernameField = new CustomTextField(centerX - 100, centerY - 40, 200, 25, "Username");
        passwordField = new CustomTextField(centerX - 100, centerY - 10, 200, 25, "Password");
        passwordField.setPassword(true);
        emailField = new CustomTextField(centerX - 100, centerY + 20, 200, 25, "Email");
        keyField = new CustomTextField(centerX - 100, centerY + 20, 200, 25, "Activation Key");
        
        usernameField.setFocused(true);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GuiMainMenu mainMenu = new GuiMainMenu();
        mainMenu.setWorldAndResolution(mc, width, height);
        mainMenu.drawScreen(mouseX, mouseY, partialTicks);
        
        updateAnimations();
        
        ScaledResolution sr = new ScaledResolution(this.mc);
        int centerX = sr.getScaledWidth() / 2;
        int centerY = sr.getScaledHeight() / 2;
        
        int panelWidth = 400;
        int panelHeight = 300;
        int panelX = centerX - panelWidth / 2;
        int panelY = centerY - panelHeight / 2;
        
        float panelAlpha = loginSuccess ? successAlpha : 1.0f;
        BlurUtil.blurAreaRounded(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 15, 50f * panelAlpha);
        RenderUtil.drawRoundedRect(panelX, panelY, panelWidth, panelHeight, 15, new Color(0, 0, 0, (int)(80 * panelAlpha)));
        
        drawCurrentMode(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight);
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    private void updateAnimations() {
        long currentTime = System.currentTimeMillis();
        float delta = (currentTime - lastTime) / 1000.0f;
        lastTime = currentTime;
        
        if (currentMode != targetMode) {
            modeTransition -= delta * 3.0f;
            if (modeTransition <= 0.0f) {
                modeTransition = 0.0f;
                currentMode = targetMode;
                modeTransition = 1.0f;
                initFieldsForMode();
            }
        } else if (modeTransition < 1.0f) {
            modeTransition += delta * 3.0f;
            if (modeTransition >= 1.0f) {
                modeTransition = 1.0f;
            }
        }
        
        if (!statusMessage.isEmpty()) {
            statusAlpha = Math.min(1.0f, statusAlpha + delta * 2.0f);
        } else {
            statusAlpha = Math.max(0.0f, statusAlpha - delta * 2.0f);
        }
        
        if (isLoading) {
            loadingRotation += delta * 360.0f;
            if (loadingRotation >= 360.0f) {
                loadingRotation -= 360.0f;
            }
        }
        
        if (loginSuccess) {
            long elapsed = currentTime - successStartTime;
            if (elapsed < 2000) {
                successAlpha = 1.0f - (elapsed / 2000.0f);
            } else {
                mc.displayGuiScreen(new GuiMainMenu());
            }
        }
    }
    
    private void drawCurrentMode(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight) {
        int alpha = (int)(modeTransition * 255);
        if (loginSuccess) {
            alpha = (int)(alpha * successAlpha);
        }
        
        switch (currentMode) {
            case MAIN:
                drawMainMode(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight, alpha);
                break;
            case LOGIN:
                drawLoginMode(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight, alpha);
                break;
            case REGISTER:
                drawRegisterMode(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight, alpha);
                break;
            case ACTIVATE:
                drawActivateMode(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight, alpha);
                break;
        }
        
        if (statusAlpha > 0.0f) {
            int statusAlphaInt = (int)(statusAlpha * 255);
            int textWidth = FontRenderer.getStringWidth(statusMessage);
            FontRenderer.drawString(statusMessage, panelX + panelWidth / 2 - textWidth / 2, panelY + panelHeight - 30, 
                new Color(255, 100, 100, statusAlphaInt).getRGB());
        }
        
        if (isLoading) {
            drawLoading(panelX + panelWidth / 2, panelY + panelHeight - 50);
        }
    }
    
    private void drawMainMode(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight, int alpha) {
        String title = "NightSky Authentication";
        int titleWidth = FontRenderer.getStringWidth(title);
        FontRenderer.drawString(title, panelX + panelWidth / 2 - titleWidth / 2, panelY + 30, 
            new Color(255, 255, 255, alpha).getRGB());
        
        String subtitle = "Please choose an option";
        int subtitleWidth = FontRenderer.getStringWidth(subtitle);
        FontRenderer.drawString(subtitle, panelX + panelWidth / 2 - subtitleWidth / 2, panelY + 55, 
            new Color(180, 180, 180, alpha).getRGB());
        
        drawButton("Login", panelX + 50, panelY + 100, panelWidth - 100, 35, mouseX, mouseY, alpha, () -> switchToMode(AuthMode.LOGIN));
        drawButton("Register", panelX + 50, panelY + 145, panelWidth - 100, 35, mouseX, mouseY, alpha, this::performRegister);
        drawButton("Activate Key", panelX + 50, panelY + 190, panelWidth - 100, 35, mouseX, mouseY, alpha, () -> switchToMode(AuthMode.ACTIVATE));
        drawButton("Exit", panelX + 50, panelY + 235, panelWidth - 100, 35, mouseX, mouseY, alpha, () -> mc.shutdown());
    }
    
    private void drawLoginMode(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight, int alpha) {
        String loginTitle = "Login";
        int loginTitleWidth = FontRenderer.getStringWidth(loginTitle);
        FontRenderer.drawString(loginTitle, panelX + panelWidth / 2 - loginTitleWidth / 2, panelY + 30, 
            new Color(255, 255, 255, alpha).getRGB());
        
        usernameField.draw(alpha);
        passwordField.draw(alpha);
        
        drawButton("Login", panelX + 50, panelY + 180, panelWidth - 100, 35, mouseX, mouseY, alpha, this::performLogin);
        drawButton("Back", panelX + 50, panelY + 225, panelWidth - 100, 30, mouseX, mouseY, alpha, () -> switchToMode(AuthMode.MAIN));
    }
    
    private void drawRegisterMode(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight, int alpha) {
        String registerTitle = "Register";
        int registerTitleWidth = FontRenderer.getStringWidth(registerTitle);
        FontRenderer.drawString(registerTitle, panelX + panelWidth / 2 - registerTitleWidth / 2, panelY + 30, 
            new Color(255, 255, 255, alpha).getRGB());
        
        usernameField.draw(alpha);
        emailField.draw(alpha);
        passwordField.draw(alpha);
        
        drawButton("Register", panelX + 50, panelY + 200, panelWidth - 100, 35, mouseX, mouseY, alpha, this::performRegister);
        drawButton("Back", panelX + 50, panelY + 245, panelWidth - 100, 30, mouseX, mouseY, alpha, () -> switchToMode(AuthMode.MAIN));
    }
    
    private void drawActivateMode(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight, int alpha) {
        String activateTitle = "Activate Key";
        int activateTitleWidth = FontRenderer.getStringWidth(activateTitle);
        FontRenderer.drawString(activateTitle, panelX + panelWidth / 2 - activateTitleWidth / 2, panelY + 30, 
            new Color(255, 255, 255, alpha).getRGB());
        
        usernameField.draw(alpha);
        passwordField.draw(alpha);
        keyField.draw(alpha);
        
        drawButton("Activate", panelX + 50, panelY + 200, panelWidth - 100, 35, mouseX, mouseY, alpha, this::performActivate);
        drawButton("Back", panelX + 50, panelY + 245, panelWidth - 100, 30, mouseX, mouseY, alpha, () -> switchToMode(AuthMode.MAIN));
    }
    
    private void drawButton(String text, int x, int y, int width, int height, int mouseX, int mouseY, int alpha, Runnable action) {
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        
        Color bgColor = hovered ? new Color(70, 130, 255, (int)(alpha * 0.8f)) : new Color(50, 50, 70, (int)(alpha * 0.6f));
        Color borderColor = new Color(100, 150, 255, (int)(alpha * 0.5f));
        
        RenderUtil.drawRoundedRect(x, y, width, height, 8, bgColor);
        RenderUtil.drawRoundedRect(x, y, width, 1, 1, borderColor);
        
        int textColor = new Color(255, 255, 255, alpha).getRGB();
        int textWidth = FontRenderer.getStringWidth(text);
        FontRenderer.drawString(text, x + width / 2 - textWidth / 2, y + height / 2 - 4, textColor);
    }
    
    private void drawLoading(int centerX, int centerY) {
        float radius = 8.0f;
        int segments = 12;
        
        for (int i = 0; i < segments; i++) {
            float angle = (float)(i * 2 * Math.PI / segments + Math.toRadians(loadingRotation));
            float x = centerX + (float)(Math.cos(angle) * radius);
            float y = centerY + (float)(Math.sin(angle) * radius);
            
            float alpha = 1.0f - (i / (float)segments);
            int alphaInt = (int)(alpha * 255);
            
            RenderUtil.drawRoundedRect((int)x - 1, (int)y - 1, 2, 2, 1, new Color(255, 255, 255, alphaInt));
        }
    }
    
    private void switchToMode(AuthMode mode) {
        if (isLoading) return;
        targetMode = mode;
        statusMessage = "";
    }
    
    private void initFieldsForMode() {
        usernameField.setText("");
        passwordField.setText("");
        emailField.setText("");
        keyField.setText("");
        
        switch (currentMode) {
            case LOGIN:
                usernameField.setFocused(true);
                break;
            case REGISTER:
                usernameField.setFocused(true);
                break;
            case ACTIVATE:
                usernameField.setFocused(true);
                break;
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (isLoading) return;
        
        ScaledResolution sr = new ScaledResolution(this.mc);
        int centerX = sr.getScaledWidth() / 2;
        int centerY = sr.getScaledHeight() / 2;
        int panelX = centerX - 200;
        int panelY = centerY - 150;
        
        switch (currentMode) {
            case MAIN:
                handleMainModeClick(mouseX, mouseY, panelX, panelY);
                break;
            case LOGIN:
                usernameField.mouseClicked(mouseX, mouseY, mouseButton);
                passwordField.mouseClicked(mouseX, mouseY, mouseButton);
                handleLoginModeClick(mouseX, mouseY, panelX, panelY);
                break;
            case REGISTER:
                usernameField.mouseClicked(mouseX, mouseY, mouseButton);
                emailField.mouseClicked(mouseX, mouseY, mouseButton);
                passwordField.mouseClicked(mouseX, mouseY, mouseButton);
                handleRegisterModeClick(mouseX, mouseY, panelX, panelY);
                break;
            case ACTIVATE:
                usernameField.mouseClicked(mouseX, mouseY, mouseButton);
                passwordField.mouseClicked(mouseX, mouseY, mouseButton);
                keyField.mouseClicked(mouseX, mouseY, mouseButton);
                handleActivateModeClick(mouseX, mouseY, panelX, panelY);
                break;
        }
        
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    private void handleMainModeClick(int mouseX, int mouseY, int panelX, int panelY) {
        if (isButtonClicked(mouseX, mouseY, panelX + 50, panelY + 100, 300, 35)) {
            switchToMode(AuthMode.LOGIN);
        } else if (isButtonClicked(mouseX, mouseY, panelX + 50, panelY + 145, 300, 35)) {
            performRegister();
        } else if (isButtonClicked(mouseX, mouseY, panelX + 50, panelY + 190, 300, 35)) {
            switchToMode(AuthMode.ACTIVATE);
        } else if (isButtonClicked(mouseX, mouseY, panelX + 50, panelY + 235, 300, 35)) {
            mc.shutdown();
        }
    }
    
    private void handleLoginModeClick(int mouseX, int mouseY, int panelX, int panelY) {
        if (isButtonClicked(mouseX, mouseY, panelX + 50, panelY + 180, 300, 35)) {
            performLogin();
        } else if (isButtonClicked(mouseX, mouseY, panelX + 50, panelY + 225, 300, 30)) {
            switchToMode(AuthMode.MAIN);
        }
    }
    
    private void handleRegisterModeClick(int mouseX, int mouseY, int panelX, int panelY) {
        if (isButtonClicked(mouseX, mouseY, panelX + 50, panelY + 200, 300, 35)) {
            performRegister();
        } else if (isButtonClicked(mouseX, mouseY, panelX + 50, panelY + 245, 300, 30)) {
            switchToMode(AuthMode.MAIN);
        }
    }
    
    private void handleActivateModeClick(int mouseX, int mouseY, int panelX, int panelY) {
        if (isButtonClicked(mouseX, mouseY, panelX + 50, panelY + 200, 300, 35)) {
            performActivate();
        } else if (isButtonClicked(mouseX, mouseY, panelX + 50, panelY + 245, 300, 30)) {
            switchToMode(AuthMode.MAIN);
        }
    }
    
    private boolean isButtonClicked(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (currentMode != AuthMode.MAIN) {
                switchToMode(AuthMode.MAIN);
                return;
            }
        }
        
        if (keyCode == Keyboard.KEY_RETURN) {
            switch (currentMode) {
                case LOGIN:
                    performLogin();
                    break;
                case REGISTER:
                    performRegister();
                    break;
                case ACTIVATE:
                    performActivate();
                    break;
            }
            return;
        }
        
        switch (currentMode) {
            case LOGIN:
                usernameField.keyTyped(typedChar, keyCode);
                passwordField.keyTyped(typedChar, keyCode);
                break;
            case REGISTER:
                usernameField.keyTyped(typedChar, keyCode);
                emailField.keyTyped(typedChar, keyCode);
                passwordField.keyTyped(typedChar, keyCode);
                break;
            case ACTIVATE:
                usernameField.keyTyped(typedChar, keyCode);
                passwordField.keyTyped(typedChar, keyCode);
                keyField.keyTyped(typedChar, keyCode);
                break;
        }
        
        super.keyTyped(typedChar, keyCode);
    }
    
    @Override
    public void updateScreen() {
        switch (currentMode) {
            case LOGIN:
                usernameField.updateCursorCounter();
                passwordField.updateCursorCounter();
                break;
            case REGISTER:
                usernameField.updateCursorCounter();
                emailField.updateCursorCounter();
                passwordField.updateCursorCounter();
                break;
            case ACTIVATE:
                usernameField.updateCursorCounter();
                passwordField.updateCursorCounter();
                keyField.updateCursorCounter();
                break;
        }
    }
    
    private void performLogin() {
        if (isLoading) return;
        
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            statusMessage = "Please fill all fields";
            return;
        }
        
        isLoading = true;
        statusMessage = "Logging in...";
        
        new Thread(() -> {
            try {
                boolean success = AuthManager.getInstance().login(username, password);
                if (success) {
                    statusMessage = "Login successful!";
                    isLoading = false;
                    loginSuccess = true;
                    successStartTime = System.currentTimeMillis();
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
    
    private void performRegister() {
        if (isLoading) return;
        
        try {
            Desktop.getDesktop().browse(URI.create("http://lifey.icu/user/register"));
        } catch (Exception e) {
            statusMessage = "Failed to open browser: " + e.getMessage();
        }
    }
    
    private void performActivate() {
        if (isLoading) return;
        
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String key = keyField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty() || key.isEmpty()) {
            statusMessage = "Please fill all fields";
            return;
        }
        
        isLoading = true;
        statusMessage = "Activating...";
        
        new Thread(() -> {
            try {
                boolean success = KamiActivationManager.getInstance().activate(username, password, key);
                if (success) {
                    AuthManager.getInstance().login(username, password);
                    statusMessage = "Activation successful!";
                    isLoading = false;
                    loginSuccess = true;
                    successStartTime = System.currentTimeMillis();
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
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
