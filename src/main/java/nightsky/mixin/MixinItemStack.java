package nightsky.mixin;

import nightsky.NightSky;
import nightsky.module.modules.render.ESP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SideOnly(Side.CLIENT)
@Mixin({ItemStack.class})
public abstract class MixinItemStack {
    @Inject(
            method = {"hasEffect"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void hasEffect(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (NightSky.moduleManager != null) {
            ESP esp = (ESP) NightSky.moduleManager.modules.get(ESP.class);
            if (esp.isEnabled() && !esp.isGlowEnabled()) {
                callbackInfoReturnable.setReturnValue(false);
            }
        }
    }
}
