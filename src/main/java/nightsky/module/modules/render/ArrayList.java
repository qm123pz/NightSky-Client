package nightsky.module.modules.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import nightsky.NightSky;
import nightsky.events.Render2DEvent;
import nightsky.event.EventTarget;
import nightsky.module.Module;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.FloatValue;
import nightsky.value.values.IntValue;
import nightsky.value.values.ModeValue;
import nightsky.util.render.ColorUtil;
import nightsky.util.render.RenderUtil;
import nightsky.util.render.BlurUtil;
import nightsky.font.FontRenderer;
import nightsky.util.render.animations.Translate;
import nightsky.util.render.animations.advanced.Animation;
import nightsky.util.render.animations.advanced.Direction;

import java.awt.*;
import java.util.Comparator;

public class ArrayList extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();

    public final ModeValue animation = new ModeValue("Animation", 0, new String[]{"Scale In", "Move In", "Slide In"});
    public final ModeValue rectangleValue = new ModeValue("Rectangle", 1, new String[]{"None", "Top", "Side"});
    public final BooleanValue backgroundValue = new BooleanValue("Back Ground", true);
    public final IntValue bgAlpha = new IntValue("Back Ground Alpha", 100, 1, 255);
    public final BooleanValue blurBackground = new BooleanValue("Blur", true);
    public final IntValue blurStrength = new IntValue("Blur Strength", 10, 1, 70);
    public final IntValue positionOffset = new IntValue("Position", 0, -1, 100);
    public final FloatValue textHeight = new FloatValue("Text Height", 4f, 0f, 10f);

    public ArrayList() {
        super("Arraylist", true);
    }
    
    private String getFormattedTag(String tag) {
        if (tag == null || tag.isEmpty()) return "";
        return " " + tag;
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!this.isEnabled()) return;
        ScaledResolution sr = new ScaledResolution(mc);
        
        moduleList(sr);
    }
    
    public void moduleList(ScaledResolution sr) {
        int count = 1;
        float fontHeight = textHeight.getValue();
        float yValue = 1 + positionOffset.getValue();
        
        int screenWidth = sr.getScaledWidth();
        nightsky.module.modules.render.Interface interfaceModule = (nightsky.module.modules.render.Interface) NightSky.moduleManager.getModule("Interface");
        
        Comparator<Module> sort = (m1, m2) -> {
            double ab = FontRenderer.getStringWidth(m1.getName() + getFormattedTag(m1.getTag()));
            double bb = FontRenderer.getStringWidth(m2.getName() + getFormattedTag(m2.getTag()));
            return Double.compare(bb, ab);
        };

        java.util.ArrayList<Module> enabledMods = new java.util.ArrayList<>(NightSky.moduleManager.modules.values());
        
        java.util.ArrayList<float[]> moduleRects = new java.util.ArrayList<>();

        if (animation.getModeString().equals("Slide In")) {
            enabledMods.sort(sort);

            for (Module module : enabledMods) {
                if (module.isHidden()) continue;
                Translate translate = module.getTranslate();
                float moduleWidth = FontRenderer.getStringWidth(module.getName() + getFormattedTag(module.getTag()));

                if (module.isEnabled() && !module.isHidden()) {
                    translate.translate((screenWidth - moduleWidth - 1.0f - positionOffset.getValue()), yValue);
                    yValue += FontRenderer.getFontHeight() + fontHeight;
                } else {
                    translate.animate((screenWidth - 1) + positionOffset.getValue(), -25.0);
                }

                if (translate.getX() >= screenWidth) {
                    continue;
                }

                float bgWidth = moduleWidth + 4;
                float bottom = FontRenderer.getFontHeight() + fontHeight;
                float leftSide = (float) (translate.getX() - 0.7f - (bgWidth - moduleWidth) / 2.0f);

                if (backgroundValue.getValue()) {
                    moduleRects.add(new float[]{leftSide, (float) translate.getY(), bgWidth, bottom});
                }
            }



            if (backgroundValue.getValue() && blurBackground.getValue() && !moduleRects.isEmpty()) {
                BlurUtil.blurArrayListShape(moduleRects, blurStrength.getValue().floatValue());
            }

            if (backgroundValue.getValue()) {
                for (float[] rect : moduleRects) {
                    RenderUtil.drawRect(rect[0], rect[1], rect[2], rect[3],
                        new Color(21, 21, 21, (int)bgAlpha.getValue().floatValue()).getRGB());
                }
            }

//            if (test.getValue() && !moduleRects.isEmpty()) {
//                drawArrayListOutline(moduleRects);
//            }

            yValue = 1 + positionOffset.getValue();
            count = 1;

            for (Module module : enabledMods) {
                if (module.isHidden()) continue;
                Translate translate = module.getTranslate();
                float moduleWidth = FontRenderer.getStringWidth(module.getName() + getFormattedTag(module.getTag()));

                if (module.isEnabled() && !module.isHidden()) {
                    yValue += FontRenderer.getFontHeight() + fontHeight;
                } else {
                    continue;
                }

                if (translate.getX() >= screenWidth) {
                    continue;
                }

                float leftSide = (float) (translate.getX() - 2f);
                float bottom = FontRenderer.getFontHeight() + fontHeight;
                float textYOffset = (bottom - FontRenderer.getFontHeight()) / 2.0f;



                switch (rectangleValue.getModeString()) {
                    case "Top":
                        if (count == 1) {
                            Gui.drawRect((int)(translate.getX() - 5.35), (int)translate.getY(),
                                (int)(translate.getX() + moduleWidth), (int)(translate.getY() + 1),
                                interfaceModule.color(count));
                        }
                        break;
                    case "Side":
                        Gui.drawRect((int)(translate.getX() + moduleWidth + 0.1), (int)(translate.getY() + 0.2),
                            (int)(translate.getX() + moduleWidth + 1.1), (int)(translate.getY() + 0.2 + bottom),
                            interfaceModule.color(count));
                        break;
                }

                String moduleName = module.getName();
                String moduleTag = getFormattedTag(module.getTag());

                FontRenderer.drawStringWithShadow(moduleName,
                        (float) translate.getX() - 0.7f,
                        (float) translate.getY() + textYOffset,
                        interfaceModule.color(count));

                if (!moduleTag.isEmpty()) {
                    float nameWidth = FontRenderer.getStringWidth(moduleName);
                    FontRenderer.drawStringWithShadow(moduleTag,
                            (float) translate.getX() - 0.7f + nameWidth,
                            (float) translate.getY() + textYOffset,
                            0xFF888888);
                }

                count -= 1;
            }

        }

        if (!animation.getModeString().equals("Slide In")) {
            enabledMods.sort(sort);

            moduleRects.clear();
            yValue = 1 + positionOffset.getValue();

            for (Module module : enabledMods) {
                if (module.isHidden()) continue;

                Animation moduleAnimation = module.getAnimation();
                moduleAnimation.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
                if (!module.isEnabled() && moduleAnimation.finished(Direction.BACKWARDS)) continue;

                float moduleWidth = FontRenderer.getStringWidth(module.getName() + getFormattedTag(module.getTag()));
                float xValue = (screenWidth - moduleWidth - 1.0f - positionOffset.getValue());
                float bgWidth = moduleWidth + 6;
                float bottom = FontRenderer.getFontHeight() + fontHeight;
                float leftSide = xValue - 3.0f - (bgWidth - moduleWidth) / 2.0f;

                if (backgroundValue.getValue()) {
                    moduleRects.add(new float[]{leftSide, yValue, bgWidth, bottom});
                }

                yValue += (float) (moduleAnimation.getOutput() * (FontRenderer.getFontHeight() + fontHeight));
            }



            if (backgroundValue.getValue() && blurBackground.getValue() && !moduleRects.isEmpty()) {
                BlurUtil.blurArrayListShape(moduleRects, blurStrength.getValue().floatValue());
            }

            if (backgroundValue.getValue()) {
                for (float[] rect : moduleRects) {
                    RenderUtil.drawRect(rect[0], rect[1], rect[2], rect[3],
                        new Color(21, 21, 21, (int)bgAlpha.getValue().floatValue()).getRGB());
                }
            }

//            if (test.getValue() && !moduleRects.isEmpty()) {
//                drawArrayListOutline(moduleRects);
//            }

            yValue = 1 + positionOffset.getValue();

            for (Module module : enabledMods) {
                if (module.isHidden()) continue;

                Animation moduleAnimation = module.getAnimation();
                moduleAnimation.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
                if (!module.isEnabled() && moduleAnimation.finished(Direction.BACKWARDS)) continue;

                float moduleWidth = FontRenderer.getStringWidth(module.getName() + getFormattedTag(module.getTag()));
                float xValue = (screenWidth - moduleWidth - 1.0f - positionOffset.getValue());

                float alphaAnimation = 1.0f;

                switch (animation.getModeString()) {
                    case "Move In":
                        xValue += (float) Math.abs((moduleAnimation.getOutput() - 1.0) * (2.0 + moduleWidth));
                        break;
                    case "Scale In":
                        RenderUtil.scaleStart(xValue + (moduleWidth / 2.0f), yValue + FontRenderer.getFontHeight(),
                            (float) moduleAnimation.getOutput());
                        alphaAnimation = (float) moduleAnimation.getOutput();
                        break;
                }

                float leftSide = xValue - 2f;
                float bottom = FontRenderer.getFontHeight() + fontHeight;
                float textYOffset = (bottom - FontRenderer.getFontHeight()) / 2.0f;

                int textcolor = ColorUtil.swapAlpha(interfaceModule.color(count), alphaAnimation * 255);



                switch (rectangleValue.getModeString()) {
                    case "Top":
                        if (count == 1) {
                            Gui.drawRect((int)(xValue - 5.35), (int)yValue,
                                (int)(xValue + moduleWidth), (int)(yValue + 1), textcolor);
                        }
                        break;
                    case "Side":
                        Gui.drawRect((int)(xValue + moduleWidth + 0.2), (int)yValue,
                            (int)(xValue + moduleWidth + 1.2), (int)(yValue + bottom), textcolor);
                        break;
                }

                String moduleName = module.getName();
                String moduleTag = getFormattedTag(module.getTag());

                FontRenderer.drawStringWithShadow(moduleName,
                        xValue - 3.0f,
                        yValue + textYOffset,
                        textcolor);

                if (!moduleTag.isEmpty()) {
                    float nameWidth = FontRenderer.getStringWidth(moduleName);
                    int tagColor = ColorUtil.swapAlpha(0xFF888888, alphaAnimation * 255);
                    FontRenderer.drawStringWithShadow(moduleTag,
                            xValue - 3.0f + nameWidth,
                            yValue + textYOffset,
                            tagColor);
                }

                if (animation.getModeString().equals("Scale In")) {
                    RenderUtil.scaleEnd();
                }

                yValue += (float) (moduleAnimation.getOutput() * (FontRenderer.getFontHeight() + fontHeight));
                count -= 2;
            }
        }
    }
}