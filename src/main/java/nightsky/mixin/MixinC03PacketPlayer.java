package nightsky.mixin;

import nightsky.module.modules.misc.Disabler;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(C03PacketPlayer.class)
public abstract class MixinC03PacketPlayer {

    @Shadow
    public boolean onGround;

    @Inject(method = "isOnGround", at = @At("HEAD"), cancellable = true)
    private void onGetOnGround(CallbackInfoReturnable<Boolean> cir) {
        if (Disabler.shouldForceGroundState()) {
            cir.cancel();
            cir.setReturnValue(true);
        }
    }
}