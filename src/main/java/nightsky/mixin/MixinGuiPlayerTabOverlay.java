package nightsky.mixin;

import nightsky.NightSky;
import nightsky.module.modules.render.DynamicIsland;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SideOnly(Side.CLIENT)
@Mixin({GuiPlayerTabOverlay.class})
public abstract class MixinGuiPlayerTabOverlay {
    @Inject(
            method = {"renderPlayerlist"},
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRenderPlayerlist(int width, net.minecraft.scoreboard.Scoreboard scoreboard, ScoreObjective scoreObjective, CallbackInfo ci) {
        if (NightSky.moduleManager != null) {
            DynamicIsland dynamicIsland = (DynamicIsland) NightSky.moduleManager.modules.get(DynamicIsland.class);
            if (dynamicIsland != null && dynamicIsland.isEnabled() && dynamicIsland.shouldHideTabList()) {
                ci.cancel();
            }
        }
    }
}
