package nightsky.mixin;

import nightsky.NightSky;
import nightsky.module.modules.render.Chams;
import nightsky.module.modules.movement.ViewClip;
import nightsky.module.modules.render.Xray;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SideOnly(Side.CLIENT)
@Mixin({VisGraph.class})
public abstract class MixinVisGraph {
    @Inject(
            method = {"func_178606_a"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void func_178606_a(CallbackInfo callbackInfo) {
        if (NightSky.moduleManager != null) {
            if (NightSky.moduleManager.modules.get(Chams.class).isEnabled()
                    || NightSky.moduleManager.modules.get(ViewClip.class).isEnabled()
                    || NightSky.moduleManager.modules.get(Xray.class).isEnabled()) {
                callbackInfo.cancel();
            }
        }
    }

    @Inject(
            method = {"computeVisibility"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void computeVisibility(CallbackInfoReturnable<SetVisibility> callbackInfoReturnable) {
        if (NightSky.moduleManager != null) {
            if (NightSky.moduleManager.modules.get(Chams.class).isEnabled()
                    || NightSky.moduleManager.modules.get(ViewClip.class).isEnabled()
                    || NightSky.moduleManager.modules.get(Xray.class).isEnabled()) {
                SetVisibility setVisibility = new SetVisibility();
                setVisibility.setAllVisible(true);
                callbackInfoReturnable.setReturnValue(setVisibility);
            }
        }
    }
}
