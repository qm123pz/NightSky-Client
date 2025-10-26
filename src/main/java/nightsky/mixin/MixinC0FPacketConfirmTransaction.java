package nightsky.mixin;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(C0FPacketConfirmTransaction.class)
public abstract class MixinC0FPacketConfirmTransaction {
    @Shadow
    private int windowId;

    @Inject(method = "writePacketData", at = @At(value = "HEAD"), cancellable = true)
    private void viaforge$fixC0F(PacketBuffer buf, CallbackInfo ci) {
        ProtocolVersion targetVersion = ViaLoadingBase.getInstance().getTargetVersion();
        if (targetVersion.newerThanOrEqualTo(ProtocolVersion.v1_17)) {
            buf.writeInt(windowId);
            ci.cancel();
        }
    }
}
