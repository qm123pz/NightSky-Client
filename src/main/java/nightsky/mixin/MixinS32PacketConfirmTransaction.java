package nightsky.mixin;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(S32PacketConfirmTransaction.class)
public abstract class MixinS32PacketConfirmTransaction {
    @Shadow
    private int windowId;

    @Inject(method = "readPacketData", at = @At(value = "HEAD"), cancellable = true)
    private void viaforge$fixS32(PacketBuffer buf, CallbackInfo ci) {
        ProtocolVersion targetVersion = ViaLoadingBase.getInstance().getTargetVersion();
        if (targetVersion.newerThanOrEqualTo(ProtocolVersion.v1_17)) {
            windowId = buf.readInt();
            ci.cancel();
        }
    }
}
