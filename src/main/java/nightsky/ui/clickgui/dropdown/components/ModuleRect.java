package nightsky.ui.clickgui.dropdown.components;

import nightsky.NightSky;
import nightsky.module.Module;
import nightsky.ui.clickgui.dropdown.utils.DropdownFontRenderer;
import nightsky.util.render.ColorUtil;
import nightsky.util.render.RenderUtil;
import nightsky.util.render.animations.advanced.Animation;
import nightsky.util.render.animations.advanced.Direction;
import nightsky.util.render.animations.advanced.impl.DecelerateAnimation;
import nightsky.util.render.animations.advanced.impl.EaseInOutQuad;
import nightsky.util.render.animations.advanced.impl.EaseOutSine;
import nightsky.value.Value;
import nightsky.value.values.*;
import nightsky.ui.clickgui.dropdown.components.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleRect {

    public final Module module;
    protected final Minecraft mc = Minecraft.getMinecraft();
    private boolean expanded = false;
    private boolean typing;
    private final Animation toggleAnimation = new EaseInOutQuad(300, 1);
    private final Animation hoverAnimation = new EaseOutSine(250, 1, Direction.BACKWARDS);
    private final Animation settingAnimation = new DecelerateAnimation(250, 1, Direction.BACKWARDS);
    
    public float x, y, width, height, panelLimitY, alpha = 1f;

    private double settingSize = 0;
    private final List<SettingComponent> settingComponents;

    public ModuleRect(Module module) {
        this.module = module;
        settingComponents = new ArrayList<>();
        
        List<Value<?>> values = NightSky.valueHandler.properties.get(module.getClass());
        if (values != null) {
            for (Value<?> value : values) {
                if (!value.isVisible()) continue;
                
                if (value instanceof BooleanValue) {
                    settingComponents.add(new BooleanComponent((BooleanValue) value));
                }
                if (value instanceof ModeValue) {
                    settingComponents.add(new ModeComponent((ModeValue) value));
                }
                if (value instanceof FloatValue) {
                    settingComponents.add(new NumberComponent((FloatValue) value));
                }
                if (value instanceof IntValue) {
                    settingComponents.add(new NumberComponent((IntValue) value));
                }
                if (value instanceof ColorValue) {
                    settingComponents.add(new ColorComponent((ColorValue) value));
                }
            }
        }
    }

    public void initGui() {
        settingAnimation.setDirection(Direction.BACKWARDS);
        toggleAnimation.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
        if (settingComponents != null) {
            settingComponents.forEach(SettingComponent::initGui);
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (expanded) {
            for (SettingComponent settingComponent : settingComponents) {
                if (!settingComponent.getSetting().isVisible()) continue;
                settingComponent.keyTyped(typedChar, keyCode);
            }
        }
    }

    private double actualSettingCount;

    public void drawScreen(int mouseX, int mouseY) {
        toggleAnimation.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
        settingAnimation.setDirection(expanded ? Direction.FORWARDS : Direction.BACKWARDS);
        
        boolean hoveringModule = isHovering(x, y, width, height, mouseX, mouseY);
        hoverAnimation.setDirection(hoveringModule ? Direction.FORWARDS : Direction.BACKWARDS);

        Color bgColor = new Color(0, 0, 0, 0);
        Color textColor = ColorUtil.applyOpacity(Color.WHITE, alpha);

        if (module.isEnabled() || !toggleAnimation.isDone()) {
            Color toggleColor = new Color(255, 255, 255);
            bgColor = ColorUtil.interpolateColorC(new Color(0, 0, 0, 0), toggleColor, 
                (float) toggleAnimation.getOutput());
        }

        GlStateManager.enableBlend();
        float hoverBrightness = (float) (0.2f * hoverAnimation.getOutput());
        Color finalBgColor = ColorUtil.brighter(bgColor, hoverBrightness);
        
        Gui.drawRect((int)x, (int)y, (int)(x + width), (int)(y + height), 
            ColorUtil.applyOpacity(finalBgColor, alpha * 0.4f).getRGB());

        Color moduleTextColor = ColorUtil.applyOpacity(textColor, alpha * 0.95f);
        DropdownFontRenderer.drawCenteredString(module.getName(), x + width / 2f, 
            y + DropdownFontRenderer.getMiddleOfBox(height, 18), moduleTextColor.getRGB(), 18);

        Color settingRectColor = ColorUtil.applyOpacity(new Color(96, 96, 96), alpha * 0.6f);
        float settingRectHeight = 20;

        actualSettingCount = 0;
        typing = false;
        
        for (SettingComponent settingComponent : settingComponents) {
            if (!settingComponent.getSetting().isVisible()) continue;
            
            settingComponent.panelLimitY = panelLimitY;
            settingComponent.settingRectColor = settingRectColor;
            settingComponent.textColor = textColor;
            settingComponent.alpha = alpha;
            settingComponent.x = x;
            settingComponent.y = y + height;
            settingComponent.width = width;
            settingComponent.height = settingRectHeight;
            
            settingComponent.updateCountSize();
            actualSettingCount += settingComponent.countSize;
        }
        
        double settingHeight = actualSettingCount * settingAnimation.getOutput();
        
        if (expanded || !settingAnimation.isDone()) {
            Gui.drawRect((int)x, (int)(y + height), (int)(x + width), 
                (int)(y + height + (settingHeight * settingRectHeight)), settingRectColor.getRGB());

            if (!settingAnimation.isDone()) {
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                RenderUtil.scissorStart(x, y + height, width, (float) (settingHeight * settingRectHeight));
            }

            double currentCount = 0;
            for (SettingComponent settingComponent : settingComponents) {
                if (!settingComponent.getSetting().isVisible()) continue;

                settingComponent.y = (float) (y + height + (currentCount * settingRectHeight));
                settingComponent.height = settingRectHeight * settingComponent.countSize;

                settingComponent.drawScreen(mouseX, mouseY);

                if (settingComponent.typing) typing = true;

                currentCount += settingComponent.countSize;
            }

            if (!settingAnimation.isDone() || GL11.glIsEnabled(GL11.GL_SCISSOR_TEST)) {
                RenderUtil.scissorEnd();
            }
        }
        settingSize = settingHeight * settingRectHeight;
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        boolean hoveringModule = isClickable(y + height, panelLimitY) && isHovering(x, y, width, height, mouseX, mouseY);
        
        if (expanded) {
            for (SettingComponent settingComponent : settingComponents) {
                if (!settingComponent.getSetting().isVisible()) continue;
                settingComponent.mouseClicked(mouseX, mouseY, button);
            }
        }

        if (hoveringModule) {
            switch (button) {
                case 0:
                    module.toggle();
                    break;
                case 1:
                    expanded = !expanded;
                    break;
            }
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (expanded) {
            for (SettingComponent settingComponent : settingComponents) {
                if (!settingComponent.getSetting().isVisible()) continue;
                settingComponent.mouseReleased(mouseX, mouseY, state);
            }
        }
    }

    public void onGuiClosed() {
        if (settingComponents != null) {
            settingComponents.forEach(SettingComponent::onGuiClosed);
        }
    }

    public boolean isClickable(float y, float panelLimitY) {
        return y > panelLimitY && y < panelLimitY + 400;
    }

    public boolean isTyping() {
        return typing;
    }

    public double getSettingSize() {
        return settingSize;
    }

    private boolean isHovering(float x, float y, float width, float height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
