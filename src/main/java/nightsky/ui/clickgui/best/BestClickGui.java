package nightsky.ui.clickgui.best;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import nightsky.NightSky;
import nightsky.font.CustomFontRenderer;
import nightsky.font.FontTransformer;
import nightsky.module.Module;
import nightsky.module.ModuleCategory;
import nightsky.util.render.BlurUtil;
import nightsky.util.render.RenderUtil;
import nightsky.value.Value;
import nightsky.value.values.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BestClickGui extends GuiScreen {

    static final float RECT_WIDTH = 600f;
    static final float RECT_HEIGHT = 380f;
    static final float CORNER_RADIUS = 20f;
    static final int BG_ALPHA = 90;
    static final float ANIMATION_DURATION = 300f;
    static final float CATEGORY_WIDTH = 120f;
    static final float CATEGORY_ITEM_HEIGHT = 35f;
    static final float MODULE_ITEM_WIDTH = 140f;
    static final float MODULE_ITEM_HEIGHT = 45f;
    static final float PADDING = 15f;
    static final int MODULES_PER_ROW = 3;
    static final float ICON_SIZE = 16f;
    static final float CATEGORY_LINE_WIDTH = 3f;
    static final float TITLE_HEIGHT = 40f;
    static final float TITLE_OFFSET = 15f;

    private static final ResourceLocation ENABLE_ICON = new ResourceLocation("nightsky/texture/toggle/enable.png");
    private static final ResourceLocation DISABLE_ICON = new ResourceLocation("nightsky/texture/toggle/disable.png");

    private final float shadowRadius = 5f;
    private final float shadowSpread = 0.1f;
    private final float shadowAlpha = 0.02f;

    float openAnimation = 0f;
    private float bgDarkAnimation = 0f;
    private long openTime = 0;
    private boolean closing = false;
    private long closeTime = 0;
    private ModuleCategory selectedCategory = ModuleCategory.COMBAT;
    private float categoryLineY = 0f;
    private float targetCategoryLineY = 0f;
    float moduleScrollOffset = 0f;
    private float categoryAlpha = 1f;
    private float targetCategoryAlpha = 1f;

    Font titleFont;
    Font categoryFont;
    Font moduleFont;
    Font settingFont;

    private final Map<Module, Float> moduleBgAlphaMap = new HashMap<>();

    boolean inSettingsMenu = false;
    Module selectedModule = null;
    float settingsScrollOffset = 0f;
    final ResourceLocation HOME_ICON = new ResourceLocation("nightsky/texture/other/home.png");
    final ResourceLocation SEARCH_ICON = new ResourceLocation("nightsky/texture/other/search.png");
    static final float HOME_BUTTON_SIZE = 20f;
    static final float SEARCH_ICON_SIZE = 16f;
    static final float SEARCH_BAR_HEIGHT = 20f;
    static final float SEARCH_BAR_OFFSET = 30f;

    private boolean searchActive = false;
    private nightsky.ui.clickgui.augustus.TextField searchField = null;

    private float currentBgWidth = RECT_WIDTH;
    private float targetBgWidth = RECT_WIDTH;
    private float currentBgHeight = RECT_HEIGHT;
    private float targetBgHeight = RECT_HEIGHT;
    private static final float BG_ANIMATION_SPEED = 0.2f;

    final Map<ColorValue, Settings.ColorPickerState> colorPickerStates = new HashMap<>();
    final Map<TextValue, nightsky.ui.clickgui.augustus.TextField> textFieldMap = new HashMap<>();

    @Override
    public void initGui() {
        super.initGui();
        openTime = System.currentTimeMillis();
        openAnimation = 0f;
        bgDarkAnimation = 0f;
        closing = false;

        FontTransformer transformer = FontTransformer.getInstance();
        titleFont = transformer.getFont("SuperJoyful", 120);
        categoryFont = transformer.getFont("OpenSansSemiBold", 38);
        moduleFont = transformer.getFont("OpenSansSemiBold", 34);
        settingFont = transformer.getFont("OpenSansSemiBold", 32);

        searchField = new nightsky.ui.clickgui.augustus.TextField();
        searchField.setMaxStringLength(50);
        searchField.setCursorBlinkSpeed(30);
        searchActive = false;

        moduleScrollOffset = 0f;

        int categoryIndex = 0;
        for (ModuleCategory cat : ModuleCategory.values()) {
            if (cat == selectedCategory) break;
            categoryIndex++;
        }

        float guiY = (new ScaledResolution(mc).getScaledHeight() - RECT_HEIGHT) / 2;
        categoryLineY = guiY + PADDING + TITLE_HEIGHT + categoryIndex * CATEGORY_ITEM_HEIGHT;
        targetCategoryLineY = categoryLineY;

        categoryAlpha = 1f;
        targetCategoryAlpha = 1f;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            if (inSettingsMenu) {
                settingsScrollOffset += (wheel > 0 ? -20f : 20f);

                if (selectedModule != null) {
                    java.util.ArrayList<Value<?>> values = NightSky.valueHandler.properties.get(selectedModule.getClass());
                    if (values != null) {
                        float totalContentHeight = 0;
                        for (Value<?> value : values) {
                            if (!value.isVisible()) continue;
                            totalContentHeight += (value instanceof ColorValue) ?
                                    Settings.COLOR_SETTING_HEIGHT :
                                    Settings.SETTING_HEIGHT;
                        }

                        float headerHeight = PADDING + HOME_BUTTON_SIZE + 15;
                        float availableHeight = getBgHeight() - headerHeight - PADDING;
                        float maxScroll = Math.max(0, totalContentHeight - availableHeight);
                        settingsScrollOffset = Math.max(0, Math.min(maxScroll, settingsScrollOffset));
                    }
                }
            } else {
                moduleScrollOffset += (wheel > 0 ? -20f : 20f);

                List<Module> modules = NightSky.moduleManager.getModulesInCategory(selectedCategory);
                int rows = (int)Math.ceil((double)modules.size() / MODULES_PER_ROW);
                float maxScroll = Math.max(0, rows * (MODULE_ITEM_HEIGHT + 10) - RECT_HEIGHT + PADDING * 2);

                moduleScrollOffset = Math.max(0, Math.min(maxScroll, moduleScrollOffset));
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        updateAnimations();

        ScaledResolution sr = new ScaledResolution(mc);
        float screenWidth = sr.getScaledWidth();
        float screenHeight = sr.getScaledHeight();

        drawDarkBackground(screenWidth, screenHeight);

        float bgWidth = getBgWidth();
        float bgHeight = getBgHeight();
        float x = (screenWidth - bgWidth) / 2;
        float y = (screenHeight - bgHeight) / 2;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        float centerX = x + bgWidth / 2;
        float centerY = y + bgHeight / 2;

        GlStateManager.translate(centerX, centerY, 0);
        GlStateManager.scale(openAnimation, openAnimation, 1);
        GlStateManager.translate(-centerX, -centerY, 0);

        drawDropShadow(x, y, bgWidth, bgHeight, openAnimation);

        BlurUtil.blurAreaRounded(x, y, x + bgWidth, y + bgHeight, CORNER_RADIUS, 12f * openAnimation);

        Color bgColor = new Color(255, 255, 255, (int)(BG_ALPHA * openAnimation));
        RenderUtil.drawRoundedRect(x, y, bgWidth, bgHeight, CORNER_RADIUS, bgColor);

        if (inSettingsMenu) {
            drawSettingsMenu(x, y, bgWidth, bgHeight, mouseX, mouseY);
        } else {
            drawTitle(x, y);

            ScaledResolution sr2 = new ScaledResolution(mc);
            int scaleFactor = sr2.getScaleFactor();
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            float moduleAreaY = y + PADDING + TITLE_HEIGHT;
            float moduleAreaHeight = bgHeight - PADDING - TITLE_HEIGHT;
            int scissorX = (int)(x * scaleFactor);
            int scissorY = (int)(mc.displayHeight - (moduleAreaY + moduleAreaHeight) * scaleFactor);
            int scissorWidth = (int)(bgWidth * scaleFactor);
            int scissorHeight = (int)(moduleAreaHeight * scaleFactor);
            GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight);

            drawCategories(x, y, mouseX, mouseY);
            drawModules(x, y, mouseX, mouseY);

            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        if (closing && openAnimation <= 0.01f) {
            mc.displayGuiScreen(null);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void updateAnimations() {
        long currentTime = System.currentTimeMillis();

        if (closing) {
            long elapsed = currentTime - closeTime;
            float progress = Math.min(1.0f, elapsed / ANIMATION_DURATION);
            float easing = easeInCubic(progress);
            openAnimation = 1.0f - easing;
            bgDarkAnimation = 1.0f - easing;
        } else {
            long elapsed = currentTime - openTime;
            float progress = Math.min(1.0f, elapsed / ANIMATION_DURATION);
            openAnimation = easeOutBack(progress);
            bgDarkAnimation = easeOutCubic(progress);
        }

        float lineDiff = targetCategoryLineY - categoryLineY;
        if (Math.abs(lineDiff) > 0.1f) {
            categoryLineY += lineDiff * 0.2f;
        } else {
            categoryLineY = targetCategoryLineY;
        }

        categoryAlpha = targetCategoryAlpha;

        updateBgSizeTargets();

        float widthDiff = targetBgWidth - currentBgWidth;
        if (Math.abs(widthDiff) > 0.5f) {
            currentBgWidth += widthDiff * BG_ANIMATION_SPEED;
        } else {
            currentBgWidth = targetBgWidth;
        }

        float heightDiff = targetBgHeight - currentBgHeight;
        if (Math.abs(heightDiff) > 0.5f) {
            currentBgHeight += heightDiff * BG_ANIMATION_SPEED;
        } else {
            currentBgHeight = targetBgHeight;
        }


        List<Module> modules = NightSky.moduleManager.getModulesInCategory(selectedCategory);
        for (Module module : modules) {
            float targetBgAlpha = module.isEnabled() ? 50f : 20f;
            float currentBgAlpha = moduleBgAlphaMap.getOrDefault(module, targetBgAlpha);
            float bgDiff = targetBgAlpha - currentBgAlpha;
            if (Math.abs(bgDiff) > 0.1f) {
                moduleBgAlphaMap.put(module, currentBgAlpha + bgDiff * 0.15f);
            } else {
                moduleBgAlphaMap.put(module, targetBgAlpha);
            }
        }
    }

    private void drawDarkBackground(float screenWidth, float screenHeight) {
        int alpha = (int)(120 * bgDarkAnimation);
        if (alpha > 0) {
            Gui.drawRect(0, 0, (int)screenWidth, (int)screenHeight, new Color(0, 0, 0, alpha).getRGB());
        }
    }

    private float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return 1 + c3 * (float)Math.pow(t - 1, 3) + c1 * (float)Math.pow(t - 1, 2);
    }

    private float easeOutCubic(float t) {
        return 1 - (float)Math.pow(1 - t, 3);
    }

    private float easeInCubic(float t) {
        return t * t * t;
    }

    private void updateBgSizeTargets() {
        targetBgWidth = calculateBgWidth();
        targetBgHeight = calculateBgHeight();
    }

    private float getBgWidth() {
        return currentBgWidth;
    }

    private float getBgHeight() {
        return currentBgHeight;
    }

    private float calculateBgWidth() {
        if (!inSettingsMenu || selectedModule == null) return RECT_WIDTH;

        ArrayList<Value<?>> values = NightSky.valueHandler.properties.get(selectedModule.getClass());
        float maxContentWidth = 0;

        if (values != null) {
            for (Value<?> value : values) {
                if (!value.isVisible()) continue;
                maxContentWidth = Math.max(maxContentWidth,
                        CustomFontRenderer.getStringWidth(value.getName(), settingFont) + 220f);
            }
        }

        float titleWidth = CustomFontRenderer.getStringWidth(selectedModule.getName(), categoryFont);
        float minWidth = Math.max(350f, HOME_BUTTON_SIZE + 10 + titleWidth + PADDING * 2);

        return Math.min(RECT_WIDTH, Math.max(maxContentWidth + PADDING * 2, minWidth));
    }

    private float calculateBgHeight() {
        if (!inSettingsMenu || selectedModule == null) return RECT_HEIGHT;

        ArrayList<Value<?>> values = NightSky.valueHandler.properties.get(selectedModule.getClass());
        if (values == null) return PADDING + HOME_BUTTON_SIZE + PADDING * 3;

        float headerHeight = PADDING + HOME_BUTTON_SIZE + 15;
        float contentHeight = 0;
        int visibleCount = 0;

        for (Value<?> value : values) {
            if (!value.isVisible()) continue;
            visibleCount++;
            contentHeight += (value instanceof ColorValue) ?
                    Settings.COLOR_SETTING_HEIGHT :
                    Settings.SETTING_HEIGHT;
        }

        if (visibleCount == 0) return headerHeight + PADDING * 2;
        return Math.min(RECT_HEIGHT, headerHeight + contentHeight + PADDING);
    }

    private float getGuiX() {
        return (new ScaledResolution(mc).getScaledWidth() - getBgWidth()) / 2;
    }

    private float getGuiY() {
        return (new ScaledResolution(mc).getScaledHeight() - getBgHeight()) / 2;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        float guiX = getGuiX();
        float guiY = getGuiY();

        if (!inSettingsMenu && mouseButton == 0) {
            if (handleSearchClick(guiX, guiY, mouseX, mouseY)) {
                return;
            }
        }

        if (inSettingsMenu) {
            if (mouseButton == 0) {
                handleHomeButtonClick(guiX, guiY, mouseX, mouseY);
                handleSettingClick(guiX, guiY, mouseX, mouseY);
            }
        } else {
            if (mouseButton == 0) {
                handleCategoryClick(guiX, guiY, mouseX, mouseY);
                handleModuleClick(guiX, guiY, mouseX, mouseY);
            } else if (mouseButton == 1) {
                handleModuleRightClick(guiX, guiY, mouseX, mouseY);
            }
        }
    }


    private boolean handleSearchClick(float guiX, float guiY, int mouseX, int mouseY) {
        float titleWidth = CustomFontRenderer.getStringWidth("NightSky", titleFont);
        float searchStartX = guiX + TITLE_OFFSET + titleWidth + 20;
        float searchY = guiY + TITLE_OFFSET + (float) CustomFontRenderer.getFontHeight(titleFont) / 2 - SEARCH_BAR_HEIGHT / 2;
        float barX = searchStartX + SEARCH_ICON_SIZE + 5;
        float barWidth = getBgWidth() - (barX - guiX) - SEARCH_BAR_OFFSET;

        if (mouseX >= searchStartX && mouseX <= barX + barWidth &&
                mouseY >= searchY && mouseY <= searchY + SEARCH_BAR_HEIGHT) {
            searchActive = true;
            searchField.setFocused(true);
            searchField.mouseClicked(mouseX, mouseY, 0);
            return true;
        }

        if (searchActive) {
            searchActive = false;
            searchField.setFocused(false);
        }

        return false;
    }

    private void handleCategoryClick(float guiX, float guiY, int mouseX, int mouseY) {
        float categoryX = guiX + PADDING;
        float categoryY = guiY + PADDING + TITLE_HEIGHT;

        int index = 0;
        for (ModuleCategory category : ModuleCategory.values()) {
            float itemY = categoryY + index * CATEGORY_ITEM_HEIGHT;

            if (mouseX >= categoryX && mouseX <= categoryX + CATEGORY_WIDTH &&
                    mouseY >= itemY && mouseY <= itemY + CATEGORY_ITEM_HEIGHT) {
                if (selectedCategory != category) {
                    selectedCategory = category;
                    targetCategoryLineY = itemY;
                    targetCategoryAlpha = 0f;
                    moduleScrollOffset = 0f;
                }
                break;
            }

            index++;
        }

        if (targetCategoryAlpha < 1f) {
            targetCategoryAlpha = 1f;
        }
    }

    private void handleModuleClick(float guiX, float guiY, int mouseX, int mouseY) {
        float moduleStartX = guiX + PADDING + CATEGORY_WIDTH + PADDING;
        float moduleStartY = guiY + PADDING + TITLE_HEIGHT - moduleScrollOffset;

        List<Module> modules = getFilteredModules();

        int index = 0;
        for (Module module : modules) {
            int row = index / MODULES_PER_ROW;
            int col = index % MODULES_PER_ROW;

            float moduleX = moduleStartX + col * (MODULE_ITEM_WIDTH + 10);
            float moduleY = moduleStartY + row * (MODULE_ITEM_HEIGHT + 10);

            if (mouseX >= moduleX && mouseX <= moduleX + MODULE_ITEM_WIDTH &&
                    mouseY >= moduleY && mouseY <= moduleY + MODULE_ITEM_HEIGHT) {
                module.toggle();
                break;
            }

            index++;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        if (inSettingsMenu) {
            for (Settings.ColorPickerState pickerState : colorPickerStates.values()) {
                pickerState.draggingHue = false;
                pickerState.draggingColor = false;
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            closeGui();
            return;
        }

        if (keyCode == Keyboard.KEY_F && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && !inSettingsMenu) {
            searchActive = !searchActive;
            if (searchActive) {
                searchField.setFocused(true);
            }
            return;
        }

        if (searchActive && !inSettingsMenu) {
            searchField.keyTyped(typedChar, keyCode);
            return;
        }

        if (inSettingsMenu && selectedModule != null) {
            for (nightsky.ui.clickgui.augustus.TextField textField : textFieldMap.values()) {
                textField.keyTyped(typedChar, keyCode);
            }
        }

        super.keyTyped(typedChar, keyCode);
    }

    public void closeGui() {
        if (!closing) {
            closing = true;
            closeTime = System.currentTimeMillis();
        }
    }

    private void drawDropShadow(float x, float y, float width, float height, float alphaMultiplier) {
        float radius = shadowRadius;
        float spread = shadowSpread;
        float baseAlpha = shadowAlpha * alphaMultiplier;

        if (baseAlpha <= 0) return;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();

        float spreadDistance = radius * spread;
        float blurDistance = radius - spreadDistance;

        int samples = 10;

        for (int i = 0; i < samples; i++) {
            float t = (float)i / (float)(samples - 1);

            float currentSpread = t * spreadDistance;
            float currentBlur = t * blurDistance;
            float totalOffset = currentSpread + currentBlur;

            float alpha;
            if (t <= spread) {
                alpha = 0.5f * baseAlpha;
            } else {
                float blurProgress = (t - spread) / (1.0f - spread);
                alpha = 0.5f * baseAlpha * (1.0f - blurProgress);
            }

            float shadowX = x - totalOffset;
            float shadowY = y - totalOffset;
            float shadowWidth = width + totalOffset * 2.0f;
            float shadowHeight = height + totalOffset * 2.0f;

            drawGaussianBlurredRect(shadowX, shadowY, shadowWidth, shadowHeight, alpha);
        }

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawGaussianBlurredRect(float x, float y, float width, float height, float alpha) {
        int blurLayers = 40;
        float blurSpread = 10.0f;

        for (int layer = 0; layer < blurLayers; layer++) {
            float layerProgress = (float)layer / (float)blurLayers;
            float layerAlpha = alpha * (1.0f - layerProgress * 0.35f);
            float layerExpand = layerProgress * blurSpread;

            float layerX = x - layerExpand;
            float layerY = y - layerExpand;
            float layerWidth = width + layerExpand * 2.0f;
            float layerHeight = height + layerExpand * 2.0f;

            RenderUtil.drawRoundedRect(
                    layerX, layerY, layerWidth, layerHeight,
                    BestClickGui.CORNER_RADIUS,
                    new Color(0, 0, 0, (int)(layerAlpha * 255))
            );
        }
    }

    private void drawTitle(float guiX, float guiY) {
        float titleX = guiX + TITLE_OFFSET;
        float titleY = guiY + TITLE_OFFSET;

        int titleAlpha = Math.max(0, Math.min(255, (int)(255 * openAnimation)));
        int titleColor = 0xFFFFFF | (titleAlpha << 24);

        CustomFontRenderer.drawString("NightSky", titleX, titleY, titleColor, titleFont);

        drawSearchBar(guiX, guiY);
    }

    private void drawSearchBar(float guiX, float guiY) {
        float titleWidth = CustomFontRenderer.getStringWidth("NightSky", titleFont);
        float searchStartX = guiX + TITLE_OFFSET + titleWidth + 20;
        float searchY = guiY + TITLE_OFFSET + (float) CustomFontRenderer.getFontHeight(titleFont) / 2 - SEARCH_BAR_HEIGHT / 2;

        float iconY = searchY + (SEARCH_BAR_HEIGHT - SEARCH_ICON_SIZE) / 2;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0f, 1.0f, 1.0f, openAnimation);
        mc.getTextureManager().bindTexture(SEARCH_ICON);
        net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture((int) searchStartX, (int)iconY, 0, 0,
                (int)SEARCH_ICON_SIZE, (int)SEARCH_ICON_SIZE, SEARCH_ICON_SIZE, SEARCH_ICON_SIZE);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        float barX = searchStartX + SEARCH_ICON_SIZE + 5;
        float barWidth = getBgWidth() - (barX - guiX) - SEARCH_BAR_OFFSET;

        int bgAlpha = Math.max(0, Math.min(255, (int)(30 * openAnimation)));
        RenderUtil.drawRoundedRect(barX, searchY, barWidth, SEARCH_BAR_HEIGHT, 6f, new Color(0, 0, 0, bgAlpha));

        searchField.setXPosition(barX + 5);
        searchField.setYPosition(searchY + SEARCH_BAR_HEIGHT / 2 - (float) CustomFontRenderer.getFontHeight(settingFont) / 2);
        searchField.setWidth(barWidth - 10);
        searchField.setHeight(SEARCH_BAR_HEIGHT);
        searchField.setEnableBackgroundDrawing(false);
        searchField.setFocused(searchActive);
        searchField.updateCursorCounter();

        if (searchField.getText().isEmpty() && !searchActive) {
            int placeholderAlpha = Math.max(0, Math.min(255, (int)(120 * openAnimation)));
            CustomFontRenderer.drawString("Search...", barX + 5, searchY + SEARCH_BAR_HEIGHT / 2 - (float) CustomFontRenderer.getFontHeight(settingFont) / 2,
                    0x808080 | (placeholderAlpha << 24), settingFont);
        } else {
            searchField.drawTextBox();
        }
    }

    private List<Module> getFilteredModules() {
        String searchQuery = searchField.getText().toLowerCase().trim();

        if (searchQuery.isEmpty()) {
            return NightSky.moduleManager.getModulesInCategory(selectedCategory);
        }

        List<Module> allModules = new ArrayList<>();
        for (ModuleCategory category : ModuleCategory.values()) {
            allModules.addAll(NightSky.moduleManager.getModulesInCategory(category));
        }

        List<Module> filtered = new ArrayList<>();
        for (Module module : allModules) {
            if (module.getName().toLowerCase().contains(searchQuery)) {
                filtered.add(module);
            }
        }

        return filtered;
    }

    private void drawCategories(float guiX, float guiY, int mouseX, int mouseY) {
        float categoryX = guiX + PADDING;
        float categoryY = guiY + PADDING + TITLE_HEIGHT;

        float lineX = categoryX - 5;
        float lineHeight = CATEGORY_ITEM_HEIGHT * 0.6f;
        int lineAlpha = Math.max(0, Math.min(255, (int)(255 * openAnimation)));
        Color lineColor = new Color(255, 255, 255, lineAlpha);
        RenderUtil.drawRoundedRect(lineX, categoryLineY + (CATEGORY_ITEM_HEIGHT - lineHeight) / 2,
                CATEGORY_LINE_WIDTH, lineHeight, 1.5f, lineColor);

        int index = 0;
        for (ModuleCategory category : ModuleCategory.values()) {
            float itemY = categoryY + index * CATEGORY_ITEM_HEIGHT;

            boolean hovered = mouseX >= categoryX && mouseX <= categoryX + CATEGORY_WIDTH &&
                    mouseY >= itemY && mouseY <= itemY + CATEGORY_ITEM_HEIGHT;
            boolean selected = category == selectedCategory;

            if (selected || hovered) {
                float bgAlpha = selected ? 40f : 20f;
                int finalBgAlpha = Math.max(0, Math.min(255, (int)(bgAlpha * openAnimation)));
                Color itemBg = new Color(0, 0, 0, finalBgAlpha);
                RenderUtil.drawRoundedRect(categoryX, itemY, CATEGORY_WIDTH, CATEGORY_ITEM_HEIGHT, 8f, itemBg);
            }

            float textAlphaValue = selected ? 1.0f : 0.6f;
            int textAlpha = Math.max(0, Math.min(255, (int)(255 * openAnimation * textAlphaValue)));
            int textColor = 0xFFFFFF | (textAlpha << 24);

            float textX = categoryX + CATEGORY_WIDTH / 2 - (float) CustomFontRenderer.getStringWidth(category.name, categoryFont) / 2;
            float textY = itemY + CATEGORY_ITEM_HEIGHT / 2 - (float) CustomFontRenderer.getFontHeight(categoryFont) / 2;

            CustomFontRenderer.drawString(category.name, textX, textY, textColor, categoryFont);

            index++;
        }
    }

    private void drawModules(float guiX, float guiY, int mouseX, int mouseY) {
        float moduleStartX = guiX + PADDING + CATEGORY_WIDTH + PADDING;
        float moduleStartY = guiY + PADDING + TITLE_HEIGHT - moduleScrollOffset;

        List<Module> modules = getFilteredModules();

        int index = 0;
        for (Module module : modules) {
            int row = index / MODULES_PER_ROW;
            int col = index % MODULES_PER_ROW;

            float moduleX = moduleStartX + col * (MODULE_ITEM_WIDTH + 10);
            float moduleY = moduleStartY + row * (MODULE_ITEM_HEIGHT + 10);

            if (moduleY + MODULE_ITEM_HEIGHT < guiY || moduleY > guiY + RECT_HEIGHT) {
                index++;
                continue;
            }

            boolean hovered = mouseX >= moduleX && mouseX <= moduleX + MODULE_ITEM_WIDTH &&
                    mouseY >= moduleY && mouseY <= moduleY + MODULE_ITEM_HEIGHT;

            float moduleBgAlpha = moduleBgAlphaMap.getOrDefault(module, module.isEnabled() ? 50f : 20f);
            if (hovered) moduleBgAlpha = Math.max(moduleBgAlpha, 30f);

            if (module.isEnabled()) {
                drawModuleShadow(moduleX, moduleY, openAnimation * categoryAlpha);
            }

            int finalModuleBgAlpha = Math.max(0, Math.min(255, (int)(moduleBgAlpha * openAnimation * categoryAlpha)));
            Color moduleBg = new Color(0, 0, 0, finalModuleBgAlpha);
            RenderUtil.drawRoundedRect(moduleX, moduleY, MODULE_ITEM_WIDTH, MODULE_ITEM_HEIGHT, 8f, moduleBg);

            float textAlphaMultiplier = module.isEnabled() ? 1.0f : 0.6f;
            int textAlpha = Math.max(0, Math.min(255, (int)(255 * openAnimation * categoryAlpha * textAlphaMultiplier)));
            int textColor = 0xFFFFFF | (textAlpha << 24);

            ResourceLocation icon = module.isEnabled() ? ENABLE_ICON : DISABLE_ICON;
            float iconX = moduleX + 8;
            float iconY = moduleY + (MODULE_ITEM_HEIGHT - ICON_SIZE) / 2;
            drawIcon(icon, iconX, iconY, openAnimation * categoryAlpha);

            float textX = iconX + ICON_SIZE + 6;
            float textY = moduleY + MODULE_ITEM_HEIGHT / 2 - (float) CustomFontRenderer.getFontHeight(moduleFont) / 2;

            CustomFontRenderer.drawString(module.getName(), textX, textY, textColor, moduleFont);

            index++;
        }
    }

    private void drawIcon(ResourceLocation icon, float x, float y, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableTexture2D();
        float clampedAlpha = Math.max(0f, Math.min(1f, alpha));
        GlStateManager.color(1.0f, 1.0f, 1.0f, clampedAlpha);

        mc.getTextureManager().bindTexture(icon);

        Gui.drawModalRectWithCustomSizedTexture((int)x, (int)y, 0, 0, (int) BestClickGui.ICON_SIZE, (int) BestClickGui.ICON_SIZE, BestClickGui.ICON_SIZE, BestClickGui.ICON_SIZE);

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawModuleShadow(float x, float y, float alphaMultiplier) {
        float shadowSize = 3f;
        int layers = 6;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        for (int i = 0; i < layers; i++) {
            float progress = (float)i / (float)layers;
            float offset = progress * shadowSize;
            float alpha = (1.0f - progress) * 0.15f * alphaMultiplier;

            int shadowAlpha = Math.max(0, Math.min(255, (int)(alpha * 255)));
            Color shadowColor = new Color(255, 255, 255, shadowAlpha);

            RenderUtil.drawRoundedRect(
                    x - offset,
                    y - offset,
                    BestClickGui.MODULE_ITEM_WIDTH + offset * 2,
                    BestClickGui.MODULE_ITEM_HEIGHT + offset * 2,
                    (float) 8.0 + offset * 0.5f,
                    shadowColor
            );
        }

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawSettingsMenu(float guiX, float guiY, float bgWidth, float bgHeight, int mouseX, int mouseY) {
        Settings.drawSettingsMenu(this, guiX, guiY, bgWidth, bgHeight, mouseX, mouseY);
    }

    private void handleHomeButtonClick(float guiX, float guiY, int mouseX, int mouseY) {
        if (selectedModule == null) return;

        float titleWidth = CustomFontRenderer.getStringWidth(selectedModule.getName(), categoryFont);
        float totalWidth = HOME_BUTTON_SIZE + 10 + titleWidth;
        float startX = guiX + getBgWidth() / 2 - totalWidth / 2;

        float homeY = guiY + PADDING;

        if (mouseX >= startX && mouseX <= startX + HOME_BUTTON_SIZE &&
                mouseY >= homeY && mouseY <= homeY + HOME_BUTTON_SIZE) {
            inSettingsMenu = false;
            selectedModule = null;
            settingsScrollOffset = 0f;
            colorPickerStates.clear();
            textFieldMap.clear();
        }
    }

    private void handleSettingClick(float guiX, float guiY, int mouseX, int mouseY) {
        Settings.handleSettingClick(this, guiX, guiY, getBgWidth(), mouseX, mouseY);
    }

    private void handleModuleRightClick(float guiX, float guiY, int mouseX, int mouseY) {
        float moduleStartX = guiX + PADDING + CATEGORY_WIDTH + PADDING;
        float moduleStartY = guiY + PADDING + TITLE_HEIGHT - moduleScrollOffset;

        List<Module> modules = getFilteredModules();

        int index = 0;
        for (Module module : modules) {
            int row = index / MODULES_PER_ROW;
            int col = index % MODULES_PER_ROW;

            float moduleX = moduleStartX + col * (MODULE_ITEM_WIDTH + 10);
            float moduleY = moduleStartY + row * (MODULE_ITEM_HEIGHT + 10);

            if (mouseX >= moduleX && mouseX <= moduleX + MODULE_ITEM_WIDTH &&
                    mouseY >= moduleY && mouseY <= moduleY + MODULE_ITEM_HEIGHT) {
                inSettingsMenu = true;
                selectedModule = module;
                settingsScrollOffset = 0f;
                break;
            }

            index++;
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        if (inSettingsMenu && clickedMouseButton == 0) {
            float guiX = getGuiX();
            float guiY = getGuiY();

            for (java.util.Map.Entry<ColorValue, Settings.ColorPickerState> entry : colorPickerStates.entrySet()) {
                ColorValue colorValue = entry.getKey();
                Settings.ColorPickerState state = entry.getValue();
                if (state.draggingHue) {
                    float hue = net.minecraft.util.MathHelper.clamp_float((mouseX - state.pickerX) / 120f, 0, 1);
                    colorValue.setHue(hue);
                } else if (state.draggingColor) {
                    float saturation = net.minecraft.util.MathHelper.clamp_float((mouseX - state.pickerX) / 120f, 0, 1);
                    float brightness = 1 - net.minecraft.util.MathHelper.clamp_float((mouseY - state.pickerY) / 60f, 0, 1);
                    colorValue.setSaturation(saturation);
                    colorValue.setBrightness(brightness);
                }
            }

            Settings.handleSettingDrag(this, guiX, guiY, getBgWidth(), mouseX, mouseY);
        }
    }
}
