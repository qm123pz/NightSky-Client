package nightsky.ui.clickgui.panel;

import nightsky.NightSky;
import nightsky.ui.Screen;
import nightsky.module.Module;
import nightsky.module.ModuleCategory;
import nightsky.util.misc.HoverUtil;
import nightsky.util.render.Render2DUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModulePanel implements Screen {
    private final SettingPanel settingPanel = new SettingPanel();
    private final Minecraft mc = Minecraft.getMinecraft();
    
    public ModuleCategory currentModuleCategory;
    public Module currentModule;
    private float x, y;
    private FontRenderer fontRenderer;
    private float scrollOffset = 0f;
    private float maxScroll = 0f;

    @Override
    public void initGui() {
        currentModuleCategory = ModuleCategory.COMBAT;
        currentModule = null;
        fontRenderer = mc.fontRendererObj;
        settingPanel.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        final float firstRectHeight = fontRenderer.FONT_HEIGHT + 4;
        final float secondRectHeight = fontRenderer.FONT_HEIGHT + 6;
        final Color textColor = new Color(186, 186, 186);
        final Color blueColor = new Color(75, 110, 174);
        final Color hoverColor = new Color(85, 120, 184);
        final float rectHeight = fontRenderer.FONT_HEIGHT + 5;
        final float xOffset = x;
        final float headerOffset = firstRectHeight + secondRectHeight + 0.5f;
        float yOffset = y + headerOffset;
        
        handleLeftPanelScrolling(mouseX, mouseY);
        float clipHeight = 350 - headerOffset - 10;
        Render2DUtil.scissorStart(x, y + headerOffset, 100, clipHeight);
        
        yOffset -= scrollOffset;
        float totalContentHeight = 0;
        
        for (ModuleCategory category : ModuleCategory.values()) {
            boolean isSelected = category == currentModuleCategory;
            boolean isHovered = HoverUtil.isHovered(mouseX, mouseY, xOffset, yOffset, 100, rectHeight);
            if (isSelected) {
                Render2DUtil.drawRect(xOffset, yOffset, 100, rectHeight, blueColor);
            } else if (isHovered) {
                Render2DUtil.drawRect(xOffset, yOffset, 100, rectHeight, hoverColor);
            }
            
            ResourceLocation packageIcon = new ResourceLocation("minecraft", "nightsky/clickgui/package.png");
            float iconAlpha = isSelected ? 1.0f : 0.8f;
            Render2DUtil.drawImage(packageIcon, xOffset + 2, yOffset + 2, rectHeight - 4, rectHeight - 4, iconAlpha);
            fontRenderer.drawString(category.name, (int)(xOffset + rectHeight + 2), 
                (int)(yOffset + (rectHeight - fontRenderer.FONT_HEIGHT) / 2), textColor.getRGB());

            yOffset += rectHeight;
            totalContentHeight += rectHeight;
            
            if (category == currentModuleCategory) {
                List<Module> modules = getModulesByCategory(currentModuleCategory);
                for (Module module : modules) {
                    boolean isModuleSelected = module == currentModule;
                    boolean isModuleHovered = HoverUtil.isHovered(mouseX, mouseY, xOffset, yOffset, 100, rectHeight);
                    if (isModuleSelected) {
                        Render2DUtil.drawRect(xOffset, yOffset, 100, rectHeight, blueColor);
                    } else if (isModuleHovered) {
                        Render2DUtil.drawRect(xOffset, yOffset, 100, rectHeight, hoverColor);
                    }
                    ResourceLocation classIcon = new ResourceLocation("minecraft", "nightsky/clickgui/class.png");
                    float moduleAlpha = isModuleSelected ? 1.0f : 0.8f;
                    Render2DUtil.drawImage(classIcon, xOffset + rectHeight + 2, yOffset + 2, 
                        rectHeight - 4, rectHeight - 4, moduleAlpha);
                    fontRenderer.drawString(module.getName(), (int)(xOffset + rectHeight * 2 + 4), 
                        (int)(yOffset + (rectHeight - fontRenderer.FONT_HEIGHT) / 2), textColor.getRGB());
                    
                    yOffset += rectHeight;
                    totalContentHeight += rectHeight;
                }
            }
        }
        
        maxScroll = Math.max(0, totalContentHeight - clipHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        
        Render2DUtil.scissorEnd();
        
        if (currentModule != null) {
            settingPanel.setX(x);
            settingPanel.setY(y);
            settingPanel.setModule(currentModule);
            settingPanel.drawScreen(mouseX, mouseY);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        final float firstRectHeight = fontRenderer.FONT_HEIGHT + 4;
        final float secondRectHeight = fontRenderer.FONT_HEIGHT + 6;
        final float rectHeight = fontRenderer.FONT_HEIGHT + 5;
        final float xOffset = x;
        final float headerOffset = firstRectHeight + secondRectHeight + 0.5f;
        float yOffset = y + headerOffset - scrollOffset;

        for (ModuleCategory category : ModuleCategory.values()) {
            boolean hoveredCategory = HoverUtil.isHovered(mouseX, mouseY, xOffset, yOffset, 100, rectHeight);
            if (hoveredCategory && button == 0) {
                currentModuleCategory = category;
                currentModule = null;
                return;
            }

            yOffset += rectHeight;

            if (category == currentModuleCategory) {
                List<Module> modules = getModulesByCategory(currentModuleCategory);
                for (Module module : modules) {
                    boolean hoveredModule = HoverUtil.isHovered(mouseX, mouseY, xOffset, yOffset, 100, rectHeight);
                    
                    if (hoveredModule) {
                        if (button == 0) {
                            currentModule = module;
                        } else if (button == 1) {
                            module.toggle();
                        }
                    }
                    if (module == currentModule) {
                        settingPanel.setX(x);
                        settingPanel.setY(y);
                        settingPanel.setModule(currentModule);
                        settingPanel.mouseClicked(mouseX, mouseY, button);
                    }
                    yOffset += rectHeight;
                }
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (currentModule != null) {
            settingPanel.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (currentModule != null) {
            settingPanel.keyTyped(typedChar, keyCode);
        }
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    private List<Module> getModulesByCategory(ModuleCategory category) {
        List<Module> modules = new ArrayList<>();
        
        try {
            if (NightSky.moduleManager != null) {
                for (Module module : NightSky.moduleManager.modules.values()) {
                    if (module.getCategory() == category) {
                        modules.add(module);
                    }
                }
            }
        } catch (Exception e) {
        }
        
        return modules;
    }

    private void handleLeftPanelScrolling(int mouseX, int mouseY) {
        final float firstRectHeight = fontRenderer.FONT_HEIGHT + 4;
        final float secondRectHeight = fontRenderer.FONT_HEIGHT + 6;
        final float headerOffset = firstRectHeight + secondRectHeight + 0.5f;
        
        float panelX = x;
        float panelY = y + headerOffset;
        float panelWidth = 100;
        float panelHeight = 350 - headerOffset - 10;
        
        boolean isInLeftPanel = HoverUtil.isHovered(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight);
        
        if (isInLeftPanel) {
            int wheelDelta = Mouse.getDWheel();
            if (wheelDelta != 0) {
                float scrollSpeed = 20f;
                if (wheelDelta > 0) {
                    scrollOffset -= scrollSpeed;
                } else if (wheelDelta < 0) {
                    scrollOffset += scrollSpeed;
                }
                
                scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
            }
        }
    }
}
