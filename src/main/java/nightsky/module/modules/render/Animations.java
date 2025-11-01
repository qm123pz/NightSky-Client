package nightsky.module.modules.render;

import nightsky.module.Module;
import nightsky.value.values.ModeValue;
import nightsky.value.values.FloatValue;

public class Animations extends Module {
    public static Animations INSTANCE;

    public final ModeValue swordMode = new ModeValue("Sword", 1, new String[]{"1.8", "Swing", "Old", "Push", "Dash", "Slash", "Slide", "Scale", "Swank", "Swang", "Swonk", "Stella", "Small", "Edit", "Rhys", "Stab", "Float", "Remix", "Avatar", "Xiv", "Winter", "Yamato", "SlideSwing", "SmallPush", "Reverse", "Invent", "Leaked", "Aqua", "Astro", "Fadeaway", "Astolfo", "AstolfoSpin", "Moon", "MoonPush", "Smooth", "Jigsaw", "Tap1", "Tap2", "Sigma3", "Sigma4"});
    public final FloatValue blockPosX = new FloatValue("BlockPos-X", 0f, -1f, 1f);
    public final FloatValue blockPosY = new FloatValue("BlockPos-Y", 0f, -1f, 1f);
    public final FloatValue blockPosZ = new FloatValue("BlockPos-Z", 0f, -1f, 1f);
    public final FloatValue scale = new FloatValue("Item-Size", 0f, -0.5f, 0.5f);

    public Animations() {
        super("Animations", false);
        INSTANCE = this;
    }

    public static Animations getInstance() {
        return INSTANCE;
    }
}