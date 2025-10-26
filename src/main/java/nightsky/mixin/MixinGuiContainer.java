package nightsky.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import nightsky.NightSky;
import nightsky.module.modules.player.ChestStealer;
import nightsky.module.modules.render.ChestView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer {

    @Inject(method = "drawScreen", at = @At("HEAD"), cancellable = true)
    public void onDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (((Object) this) instanceof GuiChest) {
            ChestView chestView = (ChestView) NightSky.moduleManager.getModule("ChestView");
            ChestStealer chestStealer = (ChestStealer) NightSky.moduleManager.getModule("ChestStealer");
            if (chestView != null && chestView.isEnabled() && 
                chestStealer != null && chestStealer.isEnabled()) {
                Minecraft.getMinecraft().setIngameFocus();
                Minecraft.getMinecraft().currentScreen = (GuiChest) ((Object) this);
                ci.cancel();
            }
        }
    }
}