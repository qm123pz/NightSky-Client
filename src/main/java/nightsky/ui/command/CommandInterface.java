package nightsky.ui.command;

import nightsky.NightSky;
import nightsky.font.FontRenderer;
import nightsky.font.FontTransformer;
import nightsky.font.CustomFontRenderer;
import nightsky.module.Module;
import nightsky.powershell.PowerShell;
import nightsky.util.render.BlurUtil;
import nightsky.util.render.RenderUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
//dynamicisland . command
public class CommandInterface {
    private static final String[][] MAIN_KEYBOARD = {
            {"`", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "=", "BACKSPACE"},
            {"TAB", "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "[", "]", "\\"},
            {"CAPS", "A", "S", "D", "F", "G", "H", "J", "K", "L", ";", "'", "ENTER"},
            {"SHIFT", "Z", "X", "C", "V", "B", "N", "M", ",", ".", "/", "SHIFT"},
            {"CTRL", "WIN", "ALT", "SPACE", "ALT", "WIN", "MENU", "CTRL"}
    };
    private boolean isActive = false;
    private String currentInput = "";
    private int cursorPosition = 0;
    private List<String> suggestions = new ArrayList<>();
    private int selectedSuggestion = 0;
    private final List<String> commandHistory = new ArrayList<>();
    private int historyIndex = -1;
    private boolean showKeyboard = false;
    private String pendingModule = "";
    private int mouseX = 0;
    private int mouseY = 0;
    private float scrollOffset = 0;
    private float targetScrollOffset = 0;
    private boolean isMouseOverInterface = false;
    private float interfaceX = 0;
    private float interfaceY = 0;
    private float interfaceWidth = 0;
    private float interfaceHeight = 0;
    private int selectedKeyRow = -1;
    private int selectedKeyCol = -1;

    private float keyAlphaAnimation = 100f;
    private float targetKeyAlpha = 100f;
    private float keyBlurAnimation = 8f;
    private float targetKeyBlur = 8f;
    private long lastAnimationTime = System.currentTimeMillis();

    private float clickScaleAnimation = 1.0f;
    private float targetClickScale = 1.0f;
    private int clickedKeyRow = -1;
    private int clickedKeyCol = -1;
    private long clickAnimationStartTime = 0;

    private float rippleRadius = 0f;
    private float rippleAlpha = 0f;
    private float rippleX = 0f;
    private float rippleY = 0f;
    private boolean showRipple = false;

    public void activate() {
        isActive = true;
        currentInput = "";
        cursorPosition = 0;
        selectedSuggestion = 0;
        historyIndex = -1;
        showKeyboard = false;
        updateSuggestions();
    }

    public void deactivate() {
        isActive = false;
        showKeyboard = false;
        pendingModule = "";
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isInSearchMode() {
        return isActive && !showKeyboard && !currentInput.trim().isEmpty();
    }

    public boolean isShowingKeyboard() {
        return showKeyboard;
    }

    private void updateKeyAnimations() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastAnimationTime) / 1000.0f;
        lastAnimationTime = currentTime;

        float animationSpeed = 8.0f;

        float alphaDiff = targetKeyAlpha - keyAlphaAnimation;
        keyAlphaAnimation += alphaDiff * animationSpeed * deltaTime;

        float blurDiff = targetKeyBlur - keyBlurAnimation;
        keyBlurAnimation += blurDiff * animationSpeed * deltaTime;

        float scaleDiff = targetClickScale - clickScaleAnimation;
        clickScaleAnimation += scaleDiff * animationSpeed * deltaTime;

        if (showRipple) {
            float rippleProgress = (currentTime - clickAnimationStartTime) / 300.0f;
            if (rippleProgress <= 1.0f) {
                rippleRadius = rippleProgress * 50f;
                rippleAlpha = (1.0f - rippleProgress) * 0.6f;
            } else {
                showRipple = false;
                rippleRadius = 0f;
                rippleAlpha = 0f;
            }
        }

        if (Math.abs(alphaDiff) < 0.1f) {
            keyAlphaAnimation = targetKeyAlpha;
        }
        if (Math.abs(blurDiff) < 0.1f) {
            keyBlurAnimation = targetKeyBlur;
        }
        if (Math.abs(scaleDiff) < 0.01f) {
            clickScaleAnimation = targetClickScale;
        }

        if (clickAnimationStartTime > 0 && currentTime - clickAnimationStartTime > 350) {
            targetClickScale = 1.0f;
            if (Math.abs(clickScaleAnimation - 1.0f) < 0.01f) {
                clickedKeyRow = -1;
                clickedKeyCol = -1;
                clickAnimationStartTime = 0;
                if (!showRipple) {
                    showKeyboard = false;
                }
            }
        }
    }

    public void handleKeyInput(int keyCode, char typedChar) {
        if (!isActive) return;

        if (showKeyboard) {
            handleKeyboardNavigation(keyCode);
            return;
        }

        switch (keyCode) {
            case Keyboard.KEY_ESCAPE:
                deactivate();
                break;
            case Keyboard.KEY_RETURN:
                executeCommand();
                break;
            case Keyboard.KEY_BACK:
                if (cursorPosition > 0) {
                    currentInput = currentInput.substring(0, cursorPosition - 1) +
                            currentInput.substring(cursorPosition);
                    cursorPosition--;
                    updateSuggestions();
                }
                break;
            case Keyboard.KEY_DELETE:
                if (cursorPosition < currentInput.length()) {
                    currentInput = currentInput.substring(0, cursorPosition) +
                            currentInput.substring(cursorPosition + 1);
                    updateSuggestions();
                }
                break;
            case Keyboard.KEY_LEFT:
                if (cursorPosition > 0) {
                    cursorPosition--;
                }
                break;
            case Keyboard.KEY_RIGHT:
                if (cursorPosition < currentInput.length()) {
                    cursorPosition++;
                }
                break;
            case Keyboard.KEY_UP:
                if (!suggestions.isEmpty()) {
                    selectedSuggestion = Math.max(0, selectedSuggestion - 1);
                } else if (!commandHistory.isEmpty()) {
                    if (historyIndex == -1) {
                        historyIndex = commandHistory.size() - 1;
                    } else {
                        historyIndex = Math.max(0, historyIndex - 1);
                    }
                    currentInput = commandHistory.get(historyIndex);
                    cursorPosition = currentInput.length();
                    updateSuggestions();
                }
                break;
            case Keyboard.KEY_DOWN:
                if (!suggestions.isEmpty()) {
                    selectedSuggestion = Math.min(suggestions.size() - 1, selectedSuggestion + 1);
                } else if (historyIndex != -1) {
                    if (historyIndex < commandHistory.size() - 1) {
                        historyIndex++;
                        currentInput = commandHistory.get(historyIndex);
                    } else {
                        historyIndex = -1;
                        currentInput = "";
                    }
                    cursorPosition = currentInput.length();
                    updateSuggestions();
                }
                break;
            case Keyboard.KEY_TAB:
                handleSmartTabCompletion();
                break;
            default:
                char inputChar = getCharFromKeyCode(keyCode);
                if (inputChar != 0) {
                    currentInput = currentInput.substring(0, cursorPosition) +
                            inputChar +
                            currentInput.substring(cursorPosition);
                    cursorPosition++;
                    updateSuggestions();
                }
                break;
        }
    }

    private char getCharFromKeyCode(int keyCode) {
        switch (keyCode) {
            case Keyboard.KEY_A:
                return 'a';
            case Keyboard.KEY_B:
                return 'b';
            case Keyboard.KEY_C:
                return 'c';
            case Keyboard.KEY_D:
                return 'd';
            case Keyboard.KEY_E:
                return 'e';
            case Keyboard.KEY_F:
                return 'f';
            case Keyboard.KEY_G:
                return 'g';
            case Keyboard.KEY_H:
                return 'h';
            case Keyboard.KEY_I:
                return 'i';
            case Keyboard.KEY_J:
                return 'j';
            case Keyboard.KEY_K:
                return 'k';
            case Keyboard.KEY_L:
                return 'l';
            case Keyboard.KEY_M:
                return 'm';
            case Keyboard.KEY_N:
                return 'n';
            case Keyboard.KEY_O:
                return 'o';
            case Keyboard.KEY_P:
                return 'p';
            case Keyboard.KEY_Q:
                return 'q';
            case Keyboard.KEY_R:
                return 'r';
            case Keyboard.KEY_S:
                return 's';
            case Keyboard.KEY_T:
                return 't';
            case Keyboard.KEY_U:
                return 'u';
            case Keyboard.KEY_V:
                return 'v';
            case Keyboard.KEY_W:
                return 'w';
            case Keyboard.KEY_X:
                return 'x';
            case Keyboard.KEY_Y:
                return 'y';
            case Keyboard.KEY_Z:
                return 'z';
            case Keyboard.KEY_0:
                return '0';
            case Keyboard.KEY_1:
                return '1';
            case Keyboard.KEY_2:
                return '2';
            case Keyboard.KEY_3:
                return '3';
            case Keyboard.KEY_4:
                return '4';
            case Keyboard.KEY_5:
                return '5';
            case Keyboard.KEY_6:
                return '6';
            case Keyboard.KEY_7:
                return '7';
            case Keyboard.KEY_8:
                return '8';
            case Keyboard.KEY_9:
                return '9';
            case Keyboard.KEY_SPACE:
                return ' ';
            case Keyboard.KEY_MINUS:
                return '-';
            case Keyboard.KEY_EQUALS:
                return '=';
            default:
                return 0;
        }
    }

    private void handleKeyboardNavigation(int keyCode) {
        if (selectedKeyRow == -1 || selectedKeyCol == -1) {
            selectedKeyRow = 0;
            selectedKeyCol = 0;
            targetKeyAlpha = 145f;
            targetKeyBlur = 12f;
        }

        switch (keyCode) {
            case Keyboard.KEY_UP:
                selectedKeyRow = Math.max(0, selectedKeyRow - 1);
                break;
            case Keyboard.KEY_DOWN:
                selectedKeyRow = Math.min(MAIN_KEYBOARD.length - 1, selectedKeyRow + 1);
                break;
            case Keyboard.KEY_LEFT:
                selectedKeyCol = Math.max(0, selectedKeyCol - 1);
                break;
            case Keyboard.KEY_RIGHT:
                if (selectedKeyRow < MAIN_KEYBOARD.length) {
                    selectedKeyCol = Math.min(MAIN_KEYBOARD[selectedKeyRow].length - 1, selectedKeyCol + 1);
                }
                break;
            case Keyboard.KEY_RETURN:
                selectKey();
                break;
            case Keyboard.KEY_ESCAPE:
                showKeyboard = false;
                deactivate();
                break;
        }
    }

    private void selectKey() {
        if (selectedKeyRow < MAIN_KEYBOARD.length &&
                selectedKeyCol < MAIN_KEYBOARD[selectedKeyRow].length) {
            String key = MAIN_KEYBOARD[selectedKeyRow][selectedKeyCol];
            int keyCode = Keyboard.getKeyIndex(key);

            Module module = NightSky.moduleManager.getModule(pendingModule);
            if (module != null) {
                module.setKey(keyCode);
                commandHistory.add(".bind " + pendingModule + " " + key);

                showKeyBindSuccessMessage(pendingModule, key);
            }

            showKeyboard = false;
            deactivate();
        }
    }

    private void showKeyBindSuccessMessage(String moduleName, String keyName) {
        Module dynamicIslandModule = NightSky.moduleManager.getModule("DynamicIsland");
        if (dynamicIslandModule != null && dynamicIslandModule.isEnabled() &&
                dynamicIslandModule instanceof nightsky.module.modules.render.DynamicIsland) {
            nightsky.module.modules.render.DynamicIsland dynamicIsland =
                    (nightsky.module.modules.render.DynamicIsland) dynamicIslandModule;
            dynamicIsland.showCommandResult("Info", "Key Bound Successfully",
                    "Module '" + moduleName + "' bound to key: " + keyName + "\nPress " + keyName + " to toggle the module");
        }
    }

    private void executeCommand() {
        if (currentInput.trim().isEmpty()) {
            deactivate();
            return;
        }

        String command = currentInput.trim();
        if (!command.startsWith(".")) {
            command = "." + command;
        }

        if (command.toLowerCase().startsWith(".module")) {
            deactivate();
            return;
        }

        if (isBindCommand(command)) {
            String[] parts = command.split(" ");
            if (parts.length == 2) {
                String moduleName = parts[1];
                Module module = NightSky.moduleManager.getModule(moduleName);
                if (module != null) {
                    pendingModule = moduleName;
                    showKeyboard = true;
                    selectedKeyRow = -1;
                    selectedKeyCol = -1;

                    keyAlphaAnimation = 100f;
                    targetKeyAlpha = 100f;
                    keyBlurAnimation = 8f;
                    targetKeyBlur = 8f;
                    lastAnimationTime = System.currentTimeMillis();

                    showBindMessage(moduleName);
                    return;
                } else {
                    showBindErrorMessage(moduleName);
                    deactivate();
                    return;
                }
            } else {
                showBindUsageMessage();
                deactivate();
                return;
            }
        }

        commandHistory.add(command);
        if (commandHistory.size() > 50) {
            commandHistory.remove(0);
        }

        executeCommandWithNotification(command);
        deactivate();
    }

    private void executeCommandWithNotification(String command) {
        Module dynamicIslandModule = NightSky.moduleManager.getModule("DynamicIsland");
        boolean shouldInterceptChat = dynamicIslandModule != null && dynamicIslandModule.isEnabled();

        if (!shouldInterceptChat) {
            NightSky.handler.handleCommand(command);
            return;
        }

        String[] parts = command.split(" ");
        String cmd = parts[0].toLowerCase();
        if (cmd.equals(".bind") || cmd.equals(".b")) {
            if (parts.length >= 2 && (parts[1].equalsIgnoreCase("list") || parts[1].equalsIgnoreCase("l"))) {
            } else {
                return;
            }
        }

        nightsky.powershell.Handler.CommandResult result = NightSky.handler.handleCommandWithResult(command);

        if (dynamicIslandModule instanceof nightsky.module.modules.render.DynamicIsland) {
            nightsky.module.modules.render.DynamicIsland dynamicIsland =
                    (nightsky.module.modules.render.DynamicIsland) dynamicIslandModule;

            if (result.success) {
                dynamicIsland.showCommandResult("Info", result.title, result.message);
            } else {
                String resultType;
                if (result.title.equals("Unknown Command") ||
                        result.title.equals("Invalid Usage") ||
                        result.title.equals("Module Not Found")) {
                    resultType = "Warning";
                } else {
                    resultType = "Error";
                }
                dynamicIsland.showCommandResult(resultType, result.title, result.message);
            }
        }
    }


    private void showBindMessage(String moduleName) {
        Module dynamicIslandModule = NightSky.moduleManager.getModule("DynamicIsland");
        if (dynamicIslandModule != null && dynamicIslandModule.isEnabled() &&
                dynamicIslandModule instanceof nightsky.module.modules.render.DynamicIsland) {
            nightsky.module.modules.render.DynamicIsland dynamicIsland =
                    (nightsky.module.modules.render.DynamicIsland) dynamicIslandModule;
            dynamicIsland.showCommandResult("Info", "Key Binding",
                    "Key binding initiated for module: " + moduleName + "\nPress any key to bind...");
        }
    }

    private void showBindErrorMessage(String moduleName) {
        Module dynamicIslandModule = NightSky.moduleManager.getModule("DynamicIsland");
        if (dynamicIslandModule != null && dynamicIslandModule.isEnabled() &&
                dynamicIslandModule instanceof nightsky.module.modules.render.DynamicIsland) {
            nightsky.module.modules.render.DynamicIsland dynamicIsland =
                    (nightsky.module.modules.render.DynamicIsland) dynamicIslandModule;
            dynamicIsland.showCommandResult("Warning", "Module Not Found",
                    "Module '" + moduleName + "' not found\nUse .help to see available commands");
        }
    }

    private void showBindUsageMessage() {
        Module dynamicIslandModule = NightSky.moduleManager.getModule("DynamicIsland");
        if (dynamicIslandModule != null && dynamicIslandModule.isEnabled() &&
                dynamicIslandModule instanceof nightsky.module.modules.render.DynamicIsland) {
            nightsky.module.modules.render.DynamicIsland dynamicIsland =
                    (nightsky.module.modules.render.DynamicIsland) dynamicIslandModule;
            dynamicIsland.showCommandResult("Warning", "Invalid Usage",
                    "Usage: .bind <module>\nBinds a key to toggle the specified module");
        }
    }


    private void handleSmartTabCompletion() {
        if (suggestions.isEmpty()) return;

        String[] inputParts = currentInput.split(" ");
        String currentWord = "";
        int wordStartPos = 0;

        if (inputParts.length > 0) {
            int currentPos = 0;
            for (int i = 0; i < inputParts.length; i++) {
                int wordEnd = currentPos + inputParts[i].length();
                if (cursorPosition >= currentPos && cursorPosition <= wordEnd) {
                    currentWord = inputParts[i];
                    wordStartPos = currentPos;
                    break;
                }
                currentPos = wordEnd + 1; // +1 for space
            }
        }

        String bestMatch = findBestTabMatch(currentWord);
        if (bestMatch != null && !bestMatch.equals(currentWord)) {
            String completion = bestMatch;

            if (inputParts.length <= 1) {
                currentInput = completion;
                cursorPosition = completion.length();
            } else {
                String beforeWord = currentInput.substring(0, wordStartPos);
                String afterWord = currentInput.substring(wordStartPos + currentWord.length());
                currentInput = beforeWord + completion + afterWord;
                cursorPosition = wordStartPos + completion.length();
            }

            updateSuggestions();

            if (suggestions.size() > 1) {
                selectedSuggestion = (selectedSuggestion + 1) % suggestions.size();
            }
        }
    }

    private String findBestTabMatch(String currentWord) {
        if (suggestions.isEmpty()) return null;

        if (selectedSuggestion < suggestions.size()) {
            String selected = suggestions.get(selectedSuggestion);
            if (selected.toLowerCase().startsWith(currentWord.toLowerCase())) {
                return selected;
            }
        }

        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(currentWord.toLowerCase())) {
                return suggestion;
            }
        }

        return suggestions.isEmpty() ? null : suggestions.get(0);
    }

    private boolean isBindCommand(String command) {
        String[] parts = command.split(" ");
        return parts.length >= 2 &&
                (parts[0].equalsIgnoreCase(".bind") || parts[0].equalsIgnoreCase(".b"));
    }

    private void updateSuggestions() {
        suggestions.clear();
        selectedSuggestion = 0;

        if (currentInput.trim().isEmpty()) {
            for (PowerShell powerShell : NightSky.handler.powerShells) {
                for (String name : powerShell.names) {
                    if (!name.equalsIgnoreCase("module")) {
                        suggestions.add(name);
                    }
                }
            }
        } else {
            String[] parts = currentInput.split(" ");
            String lastPart = parts[parts.length - 1].toLowerCase();

            if (parts.length == 1) {
                for (PowerShell powerShell : NightSky.handler.powerShells) {
                    for (String name : powerShell.names) {
                        if (name.toLowerCase().startsWith(lastPart) && !name.equalsIgnoreCase("module")) {
                            suggestions.add(name);
                        }
                    }
                }
            } else if (parts.length == 2 && (parts[0].equalsIgnoreCase("bind") || parts[0].equalsIgnoreCase("b"))) {
                for (Module module : NightSky.moduleManager.modules.values()) {
                    if (module.getName().toLowerCase().startsWith(lastPart)) {
                        suggestions.add(module.getName());
                    }
                }
            }
        }

        suggestions = suggestions.stream().distinct().sorted().collect(Collectors.toList());

        scrollOffset = 0;
        targetScrollOffset = 0;
    }

    public void render(float x, float y, float width, float height) {
        interfaceX = x;
        interfaceY = y;
        interfaceWidth = width;
        interfaceHeight = height;

        if (showKeyboard) {
            renderKeyboard(x, y, width, height);
        } else {
            renderCommandInput(x, y, width, height);
        }
    }

    private void renderCommandInput(float x, float y, float width, float height) {
        float inputHeight = 32;

        float suggestionsHeight = 0;
        if (!suggestions.isEmpty()) {
            suggestionsHeight = Math.min(suggestions.size(), 8) * 20 + 10;
        }

        float inputY = y + 8;

        FontTransformer transformer = FontTransformer.getInstance();
        Font commandFont = transformer.getFont("RobotoRegular", 40);
        
        String displayText = "." + currentInput;
        float textX = x + 12;
        float textY = inputY + (inputHeight - CustomFontRenderer.getFontHeight(commandFont)) / 2;

        CustomFontRenderer.drawStringWithShadow(displayText, textX, textY, 0xFFFFFF, commandFont);

        long time = System.currentTimeMillis();
        float lineAlpha = (float) (Math.sin(time * 0.005) * 0.3 + 0.7);
        int lineColor = (int) (lineAlpha * 255) << 24 | 0xFFFFFF;
        RenderUtil.drawRect(x + 8, inputY + inputHeight - 2, width - 16, 1, lineColor);

        if ((time / 500) % 2 == 0) {
            String beforeCursor = "." + currentInput.substring(0, cursorPosition);
            float cursorX = textX + CustomFontRenderer.getStringWidth(beforeCursor, commandFont);
            RenderUtil.drawRect(cursorX, textY, 1, CustomFontRenderer.getFontHeight(commandFont), 0xFFFFFFFF);
        }

        if (!suggestions.isEmpty()) {
            renderSuggestions(x, inputY + inputHeight + 2, width);
        }
    }

    private void renderSuggestions(float x, float y, float width) {
        FontTransformer transformer = FontTransformer.getInstance();
        Font commandFont = transformer.getFont("RobotoRegular", 40);
        
        float suggestionHeight = 20;
        long time = System.currentTimeMillis();

        int startIndex = (int) scrollOffset;
        int visibleCount = Math.min(8, suggestions.size() - startIndex);

        for (int i = 0; i < visibleCount; i++) {
            int suggestionIndex = startIndex + i;
            if (suggestionIndex >= suggestions.size()) break;

            String suggestion = suggestions.get(suggestionIndex);
            float suggestionY = y + i * suggestionHeight;

            if (suggestionIndex == selectedSuggestion) {
                float indicatorAlpha = (float) (Math.sin(time * 0.008) * 0.2 + 0.8);
                int indicatorColor = (int) (indicatorAlpha * 255) << 24 | 0xFFFFFF;
                float indicatorWidth = (float) (Math.sin(time * 0.006) * 1 + 3);

                float targetY = suggestionY + 2;
                RenderUtil.drawRect(x + 8, targetY, indicatorWidth, suggestionHeight - 4, indicatorColor);
            }

            String[] parts = currentInput.split(" ");
            String lastPart = parts[parts.length - 1];
            String beforeMatch = suggestion.substring(0, Math.min(lastPart.length(), suggestion.length()));
            String afterMatch = suggestion.substring(Math.min(lastPart.length(), suggestion.length()));

            float textX = x + 16;
            float textY = suggestionY + (suggestionHeight - CustomFontRenderer.getFontHeight(commandFont)) / 2;

            CustomFontRenderer.drawStringWithShadow(beforeMatch, textX, textY, 0xFFFFFF, commandFont);
            CustomFontRenderer.drawStringWithShadow(afterMatch, textX + CustomFontRenderer.getStringWidth(beforeMatch, commandFont), textY, 0xAAAAAA, commandFont);
        }
    }

    private void renderKeyboard(float x, float y, float width, float height) {
        float keyboardWidth = 600;
        float keyboardHeight = 200;
        float keyboardX = x + (width - keyboardWidth) / 2;
        float keyboardY = y + 20;

        FontTransformer transformer = FontTransformer.getInstance();
        Font keyboardFont = transformer.getFont("ESPBold", 40);
        
        String title = "Select key to bind " + pendingModule;
        float titleX = keyboardX + (keyboardWidth - CustomFontRenderer.getStringWidth(title, keyboardFont)) / 2;
        CustomFontRenderer.drawStringWithShadow(title, titleX, keyboardY - 15, 0xFFFFFF, keyboardFont);

        long time = System.currentTimeMillis();

        renderKeyboardSection(MAIN_KEYBOARD, keyboardX, keyboardY + 20, 35, 25, 40, 30, time, 1);

        String instruction = "Use arrow keys to select, Enter to confirm, ESC to cancel";
        float instructionX = keyboardX + (keyboardWidth - CustomFontRenderer.getStringWidth(instruction, keyboardFont)) / 2;
        CustomFontRenderer.drawStringWithShadow(instruction, instructionX, keyboardY + keyboardHeight - 10, 0xAAAAAA, keyboardFont);

        if (showRipple && rippleAlpha > 0) {
            renderRippleEffect();
        }
    }

    private void renderRippleEffect() {
        if (!showRipple || rippleAlpha <= 0) return;

        Color rippleColor = new Color(255, 255, 255, (int) (rippleAlpha * 255));

        float diameter = rippleRadius * 2;
        RenderUtil.drawRoundedRect(rippleX - rippleRadius, rippleY - rippleRadius,
                diameter, diameter, (int) rippleRadius, rippleColor);
    }

    private void renderKeyboardSection(String[][] section, float startX, float startY,
                                       float keyWidth, float keyHeight, float keySpacingX, float keySpacingY,
                                       long time, int sectionId) {
        updateKeyAnimations();

        for (int row = 0; row < section.length; row++) {
            String[] rowKeys = section[row];
            float currentX = startX;

            for (int col = 0; col < rowKeys.length; col++) {
                String key = rowKeys[col];
                if (key.isEmpty()) {
                    currentX += keySpacingX;
                    continue;
                }

                float keyX = currentX;
                float keyY = startY + row * keySpacingY;

                float actualKeyWidth = getKeyWidth(key, keyWidth);

                Color keyColor;
                float blurStrength;
                float keyScale = 1.0f;

                if (sectionId == 1 && row == selectedKeyRow && col == selectedKeyCol) {

                    keyColor = new Color(255, 255, 255, (int) keyAlphaAnimation);
                    blurStrength = keyBlurAnimation;
                } else {
                    keyColor = new Color(255, 255, 255, 100);
                    blurStrength = 8f;
                }

                if (sectionId == 1 && row == clickedKeyRow && col == clickedKeyCol) {
                    keyScale = clickScaleAnimation;
                }

                float scaledWidth = actualKeyWidth * keyScale;
                float scaledHeight = keyHeight * keyScale;
                float scaleOffsetX = (actualKeyWidth - scaledWidth) / 2;
                float scaleOffsetY = (keyHeight - scaledHeight) / 2;
                float scaledKeyX = keyX + scaleOffsetX;
                float scaledKeyY = keyY + scaleOffsetY;

                BlurUtil.blurAreaRounded(scaledKeyX - 1, scaledKeyY - 1, scaledKeyX + scaledWidth + 1, scaledKeyY + scaledHeight + 1, 8, blurStrength);

                RenderUtil.drawRoundedRect(scaledKeyX, scaledKeyY, scaledWidth, scaledHeight, 8, keyColor);

                RenderUtil.drawRoundedRect(scaledKeyX, scaledKeyY, scaledWidth, scaledHeight, 8, new Color(255, 255, 255, 50));

                FontTransformer transformer = FontTransformer.getInstance();
                Font keyboardFont = transformer.getFont("ESPBold", 40);
                
                String displayText = getKeyDisplayText(key);
                if (!displayText.isEmpty()) {
                    float textWidth = CustomFontRenderer.getStringWidth(displayText, keyboardFont);
                    float fontHeight = CustomFontRenderer.getFontHeight(keyboardFont);
                    float textX = scaledKeyX + (scaledWidth - textWidth) / 2;
                    float textY = scaledKeyY + (scaledHeight - fontHeight) / 2 + 1;

                    CustomFontRenderer.drawStringWithShadow(displayText, textX, textY, 0xFFFFFF, keyboardFont);
                }

                currentX += actualKeyWidth + 5;
            }
        }
    }

    private float getKeyWidth(String key, float defaultWidth) {
        switch (key) {
            case "BACKSPACE":
                return defaultWidth * 2;
            case "TAB":
                return defaultWidth * 1.5f;
            case "CAPS":
                return defaultWidth * 1.8f;
            case "ENTER":
                return defaultWidth * 2.2f;
            case "SHIFT":
                return defaultWidth * 2.5f;
            case "CTRL":
                return defaultWidth * 1.2f;
            case "ALT":
                return defaultWidth * 1.2f;
            case "SPACE":
                return defaultWidth * 6;
            case "WIN":
                return defaultWidth * 1.2f;
            case "MENU":
                return defaultWidth * 1.2f;
            default:
                return defaultWidth;
        }
    }

    private String getKeyDisplayText(String key) {
        switch (key) {
            case "BACKSPACE":
                return "Back";
            case "TAB":
                return "Tab";
            case "CAPS":
                return "Caps";
            case "ENTER":
                return "Enter";
            case "SHIFT":
                return "Shift";
            case "CTRL":
                return "Ctrl";
            case "ALT":
                return "Alt";
            case "SPACE":
                return "Space";
            case "WIN":
                return "Win";
            case "MENU":
                return "Menu";
            case "ESC":
                return "Esc";
            case "INS":
                return "Ins";
            case "DEL":
                return "Del";
            case "HOME":
                return "Home";
            case "END":
                return "End";
            case "PGUP":
                return "PgUp";
            case "PGDN":
                return "PgDn";
            case "NUM":
                return "Num";
            case "↑":
                return "Up";
            case "↓":
                return "Down";
            case "←":
                return "Left";
            case "→":
                return "Right";
            default:
                return key;
        }
    }

    public double getExpandedWidth() {
        return isActive ? (showKeyboard ? 650 : 300) : 0;
    }

    public double getExpandedHeight() {
        if (!isActive) return 0;
        if (showKeyboard) return 250;

        float baseHeight = 50;
        if (!suggestions.isEmpty()) {
            baseHeight += Math.min(suggestions.size(), 8) * 20;
        }
        return baseHeight;
    }

    public void handleMouseMove(int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        isMouseOverInterface = mouseX >= interfaceX && mouseX <= interfaceX + interfaceWidth &&
                mouseY >= interfaceY && mouseY <= interfaceY + interfaceHeight;

        if (showKeyboard) {
            handleKeyboardHover(mouseX, mouseY);
        } else if (!suggestions.isEmpty() && isMouseOverInterface) {
            float inputY = interfaceY + 8;
            float suggestionY = inputY + 32 + 2;

            int startIndex = (int) scrollOffset;
            int visibleCount = Math.min(8, suggestions.size() - startIndex);

            for (int i = 0; i < visibleCount; i++) {
                float itemY = suggestionY + i * 20;
                if (mouseY >= itemY && mouseY <= itemY + 20) {
                    selectedSuggestion = startIndex + i;
                    break;
                }
            }
        }
    }

    private void handleKeyboardHover(int mouseX, int mouseY) {
        float keyboardWidth = 600;
        float keyboardHeight = 200;
        float keyboardX = interfaceX + (interfaceWidth - keyboardWidth) / 2;
        float keyboardY = interfaceY + 20;

        checkKeyboardSectionHover(MAIN_KEYBOARD, keyboardX, keyboardY + 20, 35, 25, 40, 30, mouseX, mouseY);
    }

    private void checkKeyboardSectionHover(String[][] section, float startX, float startY,
                                           float keyWidth, float keyHeight, float keySpacingX, float keySpacingY,
                                           int mouseX, int mouseY) {
        boolean foundHover = false;
        int newSelectedRow = selectedKeyRow;
        int newSelectedCol = selectedKeyCol;

        for (int row = 0; row < section.length; row++) {
            String[] rowKeys = section[row];
            float currentX = startX;

            for (int col = 0; col < rowKeys.length; col++) {
                String key = rowKeys[col];
                if (key.isEmpty()) {
                    currentX += keySpacingX;
                    continue;
                }

                float keyX = currentX;
                float keyY = startY + row * keySpacingY;
                float actualKeyWidth = getKeyWidth(key, keyWidth);

                if (mouseX >= keyX && mouseX <= keyX + actualKeyWidth &&
                        mouseY >= keyY && mouseY <= keyY + keyHeight) {
                    newSelectedRow = row;
                    newSelectedCol = col;
                    foundHover = true;
                    break;
                }

                currentX += actualKeyWidth + 5;
            }
            if (foundHover) break;
        }

        if (foundHover && (newSelectedRow != selectedKeyRow || newSelectedCol != selectedKeyCol)) {
            selectedKeyRow = newSelectedRow;
            selectedKeyCol = newSelectedCol;
            targetKeyAlpha = 145f;
            targetKeyBlur = 12f;
        } else if (!foundHover && (selectedKeyRow != -1 || selectedKeyCol != -1)) {
            selectedKeyRow = -1;
            selectedKeyCol = -1;
            targetKeyAlpha = 100f;
            targetKeyBlur = 8f;
        }
    }

    public void handleMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            if (!isMouseOverInterface) {
                deactivate();
                return;
            }

            if (showKeyboard) {
                handleKeyboardClick(mouseX, mouseY);
                return;
            }

            float inputY = interfaceY + 8;
            float suggestionY = inputY + 32 + 2;
            if (!suggestions.isEmpty() && mouseY >= suggestionY) {
                int clickedIndex = (int) ((mouseY - suggestionY) / 20);
                int startIndex = (int) scrollOffset;
                int visibleCount = Math.min(8, suggestions.size() - startIndex);

                if (clickedIndex >= 0 && clickedIndex < visibleCount) {
                    int actualIndex = startIndex + clickedIndex;
                    if (actualIndex < suggestions.size()) {
                        selectedSuggestion = actualIndex;
                        String suggestion = suggestions.get(selectedSuggestion);
                        String[] parts = currentInput.split(" ");
                        if (parts.length > 0) {
                            parts[parts.length - 1] = suggestion;
                            currentInput = String.join(" ", parts);
                            cursorPosition = currentInput.length();
                            updateSuggestions();
                        }
                    }
                }
            }
        }
    }

    private void handleKeyboardClick(int mouseX, int mouseY) {
        float keyboardWidth = 600;
        float keyboardX = interfaceX + (interfaceWidth - keyboardWidth) / 2;
        float keyboardY = interfaceY + 20;

        checkKeyboardSectionClick(MAIN_KEYBOARD, keyboardX, keyboardY + 20, 35, 25, 40, 30, mouseX, mouseY);
    }

    private boolean checkKeyboardSectionClick(String[][] section, float startX, float startY,
                                              float keyWidth, float keyHeight, float keySpacingX, float keySpacingY,
                                              int mouseX, int mouseY) {
        for (int row = 0; row < section.length; row++) {
            String[] rowKeys = section[row];
            float currentX = startX;

            for (int col = 0; col < rowKeys.length; col++) {
                String key = rowKeys[col];
                if (key.isEmpty()) {
                    currentX += keySpacingX;
                    continue;
                }

                float keyX = currentX;
                float keyY = startY + row * keySpacingY;
                float actualKeyWidth = getKeyWidth(key, keyWidth);

                if (mouseX >= keyX && mouseX <= keyX + actualKeyWidth &&
                        mouseY >= keyY && mouseY <= keyY + keyHeight) {
                    clickedKeyRow = row;
                    clickedKeyCol = col;
                    targetClickScale = 0.9f;
                    clickAnimationStartTime = System.currentTimeMillis();

                    rippleX = keyX + actualKeyWidth / 2;
                    rippleY = keyY + keyHeight / 2;
                    showRipple = true;
                    rippleRadius = 0f;
                    rippleAlpha = 0.6f;

                    if (!pendingModule.isEmpty()) {
                        int keyCode = Keyboard.getKeyIndex(key);
                        Module module = NightSky.moduleManager.getModule(pendingModule);
                        if (module != null) {
                            module.setKey(keyCode);
                            commandHistory.add(".bind " + pendingModule + " " + key);

                            showKeyBindSuccessMessage(pendingModule, key);
                        }

                        showKeyboard = false;
                        deactivate();
                        return true;
                    }
                }

                currentX += actualKeyWidth + 5;
            }
        }
        return false;
    }

    private int getKeyCodeFromString(String key) {
        switch (key.toUpperCase()) {
            case "ESC":
                return Keyboard.KEY_ESCAPE;
            case "F1":
                return Keyboard.KEY_F1;
            case "F2":
                return Keyboard.KEY_F2;
            case "F3":
                return Keyboard.KEY_F3;
            case "F4":
                return Keyboard.KEY_F4;
            case "F5":
                return Keyboard.KEY_F5;
            case "F6":
                return Keyboard.KEY_F6;
            case "F7":
                return Keyboard.KEY_F7;
            case "F8":
                return Keyboard.KEY_F8;
            case "F9":
                return Keyboard.KEY_F9;
            case "F10":
                return Keyboard.KEY_F10;
            case "F11":
                return Keyboard.KEY_F11;
            case "F12":
                return Keyboard.KEY_F12;
            case "BACKSPACE":
                return Keyboard.KEY_BACK;
            case "TAB":
                return Keyboard.KEY_TAB;
            case "ENTER":
                return Keyboard.KEY_RETURN;
            case "SHIFT":
                return Keyboard.KEY_LSHIFT;
            case "CTRL":
                return Keyboard.KEY_LCONTROL;
            case "ALT":
                return Keyboard.KEY_LMENU;
            case "SPACE":
                return Keyboard.KEY_SPACE;
            case "CAPS":
                return Keyboard.KEY_CAPITAL;
            default:
                return Keyboard.getKeyIndex(key.toUpperCase());
        }
    }

    public void handleMouseScroll() {
        if (!isMouseOverInterface || suggestions.isEmpty()) return;

        int scroll = Mouse.getDWheel();
        if (scroll != 0) {
            targetScrollOffset += scroll > 0 ? -1 : 1;
            targetScrollOffset = Math.max(0, Math.min(Math.max(0, suggestions.size() - 8), targetScrollOffset));

            int startIndex = (int) targetScrollOffset;
            int endIndex = startIndex + 7;

            if (selectedSuggestion < startIndex) {
                selectedSuggestion = startIndex;
            } else if (selectedSuggestion > endIndex && endIndex < suggestions.size() - 1) {
                selectedSuggestion = Math.min(endIndex, suggestions.size() - 1);
            }
        }
        scrollOffset += (targetScrollOffset - scrollOffset) * 0.3f;
    }
}