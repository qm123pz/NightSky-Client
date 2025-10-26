package nightsky.module.modules.render;

import nightsky.font.FontTransformer;
import nightsky.module.Module;
import nightsky.module.ModuleCategory;
import nightsky.value.values.FloatValue;
import nightsky.value.values.ModeValue;

import java.awt.Font;

public class GlobalFont extends Module {
    private final FontTransformer fontTransformer = FontTransformer.getInstance();
    
    public final ModeValue fontType = new ModeValue("Font", 0, fontTransformer.getAvailableFonts());
    public final FloatValue fontSize = new FloatValue("Size", 40.0f, 18.0f, 120.0f);
    
    public GlobalFont() {
        super("GlobalFont", true);
    }
    
    @Override
    public ModuleCategory getCategory() {
        return ModuleCategory.RENDER;
    }
    
    @Override
    public void onEnabled() {
        updateFont();
    }
    
    @Override
    public void onDisabled() {
        fontTransformer.setFont("minecraft", 18.0f);
    }
    
    @Override
    public void verifyValue(String valueName) {
        if (isEnabled()) {
            updateFont();
        }
    }
    
    private void updateFont() {
        String selectedFont = fontType.getModeString();
        float size = fontSize.getValue();
        fontTransformer.setFont(selectedFont, size);
    }
    
    public Font getCurrentFont() {
        return fontTransformer.getFont(fontType.getModeString(), fontSize.getValue());
    }
    
    public boolean isCustomFont() {
        return isEnabled() && !fontTransformer.isMinecraftFont();
    }
    
    public String getCurrentFontName() {
        return isEnabled() ? fontType.getModeString() : "minecraft";
    }
    
    public float getCurrentFontSize() {
        return isEnabled() ? fontSize.getValue() : 18.0f;
    }
}
