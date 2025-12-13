package nightsky.ui.clickgui.dropdown;

import nightsky.NightSky;
import nightsky.module.Module;
import nightsky.module.ModuleCategory;
import nightsky.ui.clickgui.dropdown.components.ModuleRect;
import nightsky.ui.clickgui.dropdown.utils.DropdownFontRenderer;
import nightsky.ui.clickgui.dropdown.utils.ScrollUtil;
import nightsky.util.render.ColorUtil;
import nightsky.util.DragUtil;
import net.minecraft.client.gui.Gui;
import nightsky.util.render.PostProcessing;
import nightsky.util.render.StencilUtil;
import nightsky.util.render.animations.advanced.Animation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CategoryPanel {

    private final ModuleCategory category;
    private final float rectWidth = 110;
    private final float categoryRectHeight = 18;
    private boolean typing;
    private List<ModuleRect> moduleRects;
    private final DragUtil drag;
    private final ScrollUtil scrollUtil = new ScrollUtil();
    protected final Minecraft mc = Minecraft.getMinecraft();
    private final Animation fadeAnimation;

    public CategoryPanel(ModuleCategory category, Animation fadeAnimation) {
        this.category = category;
        this.fadeAnimation = fadeAnimation;
        this.drag = new DragUtil(50 + category.ordinal() * 130, 50);
    }

    public void initGui() {
        if (moduleRects == null) {
            moduleRects = new ArrayList<>();
            for (Module module : NightSky.moduleManager.modules.values()) {
                if (module.getCategory() == category) {
                    moduleRects.add(new ModuleRect(module));
                }
            }
            moduleRects.sort(Comparator.comparing(rect -> rect.module.getName()));
        }

        if (moduleRects != null) {
            moduleRects.forEach(ModuleRect::initGui);
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (moduleRects != null) {
            moduleRects.forEach(moduleRect -> moduleRect.keyTyped(typedChar, keyCode));
        }
    }

    public void onDrag(int mouseX, int mouseY) {
        drag.onDraw(mouseX, mouseY);
    }

    float actualHeight = 0;

    public void drawScreen(int mouseX, int mouseY) {
        if (moduleRects == null) {
            return;
        }

        float alpha = (float) Math.min(1, fadeAnimation.getOutput());
        float alphaValue = alpha * 0.4f;

        float x = drag.getX();
        float y = drag.getY();
        
        ScaledResolution sr = new ScaledResolution(mc);
        float allowedHeight = 2 * sr.getScaledHeight() / 3f;

        boolean hoveringMods = isHovering(x, y + categoryRectHeight, rectWidth, allowedHeight, mouseX, mouseY);

        float realHeight = Math.min(actualHeight, allowedHeight);

        PostProcessing.drawBlurRect(x, y, x + rectWidth, y + realHeight + categoryRectHeight);
        
        GlStateManager.enableBlend();
        
        Gui.drawRect((int)x, (int)y, (int)(x + rectWidth), (int)(y + realHeight + categoryRectHeight), 
            ColorUtil.applyOpacity(new Color(20, 20, 20), alphaValue * 0.8f).getRGB());
        
        Gui.drawRect((int)x, (int)y, (int)(x + rectWidth), (int)(y + categoryRectHeight), 
            ColorUtil.applyOpacity(new Color(255, 255, 255, 255), alphaValue * 0.8f).getRGB());

        StencilUtil.write(false);
        Gui.drawRect((int)(x + 1), (int)(y + categoryRectHeight + 1), (int)(x + rectWidth - 1), (int)(y + realHeight + categoryRectHeight - 1), 
            new Color(0, 0, 0, 255).getRGB());
        StencilUtil.erase(true);

        double scroll = scrollUtil.getScroll();
        double count = 0;
        float rectHeight = 16F;

        for (ModuleRect moduleRect : moduleRects) {
            moduleRect.alpha = alpha;
            moduleRect.x = x;
            moduleRect.height = rectHeight;
            moduleRect.panelLimitY = y + categoryRectHeight;
            moduleRect.y = (float) (y + categoryRectHeight + (count * rectHeight) + scroll);
            moduleRect.width = rectWidth;
            moduleRect.drawScreen(mouseX, mouseY);

            count += 1 + (moduleRect.getSettingSize() / rectHeight);
        }

        typing = moduleRects.stream().anyMatch(ModuleRect::isTyping);

        actualHeight = (float) (count * rectHeight);

        if (hoveringMods) {
            int wheel = org.lwjgl.input.Mouse.getDWheel();
            if (wheel != 0) {
                scrollUtil.onScroll(wheel > 0 ? 30 : -30);
                float hiddenHeight = (float) ((count * rectHeight) - allowedHeight);
                scrollUtil.setMaxScroll(Math.max(0, hiddenHeight));
            }
        }

        StencilUtil.dispose();

        Color textColor = ColorUtil.applyOpacity(Color.WHITE, alpha);
        DropdownFontRenderer.drawCenteredString(category.name, x + rectWidth / 2f, 
            y + DropdownFontRenderer.getMiddleOfBox(categoryRectHeight, 20), textColor.getRGB(), 20);
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        float x = drag.getX();
        float y = drag.getY();
        
        boolean canDrag = isHovering(x, y, rectWidth, categoryRectHeight, mouseX, mouseY);
        drag.onClick(mouseX, mouseY, button, canDrag);
        
        if (moduleRects != null) {
            moduleRects.forEach(moduleRect -> moduleRect.mouseClicked(mouseX, mouseY, button));
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        drag.onRelease(state);
        if (moduleRects != null) {
            moduleRects.forEach(moduleRect -> moduleRect.mouseReleased(mouseX, mouseY, state));
        }
    }

    public void onGuiClosed() {
        if (moduleRects != null) {
            moduleRects.forEach(ModuleRect::onGuiClosed);
        }
    }

    public boolean isTyping() {
        return typing;
    }

    private boolean isHovering(float x, float y, float width, float height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
