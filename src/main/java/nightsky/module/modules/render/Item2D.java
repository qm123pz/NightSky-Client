package nightsky.module.modules.render;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import nightsky.module.Module;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.FloatValue;
import nightsky.value.values.IntValue;

public class Item2D extends Module {
    private static Item2D instance;
    private final Minecraft mc = Minecraft.getMinecraft();
    public final FloatValue scale = new FloatValue("Scale", 1.2F, 0.5F, 3.0F);
    public final IntValue offsetX = new IntValue("OffsetX", 0, -200, 200);
    public final IntValue offsetY = new IntValue("OffsetY", -40, -200, 200);
    public final BooleanValue swing = new BooleanValue("Swing", true);

    public Item2D() {
        super("2DItem", false);
        instance = this;
    }

    public static Item2D getInstance() {
        return instance;
    }

    public ItemStack getItemStack() {
        return mc.thePlayer == null ? null : mc.thePlayer.getHeldItem();
    }
}
