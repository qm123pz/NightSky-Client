package nightsky.mixin;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.network.handshake.client.C00Handshake;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(C00Handshake.class)
public abstract class MixinC00Handshake {
    @ModifyConstant(method = "writePacketData", constant = @Constant(stringValue = "\u0000FML\u0000"))
    private String viaforge$fixHandshake(String constant) {
        ProtocolVersion targetVersion = ViaLoadingBase.getInstance().getTargetVersion();
        return targetVersion.newerThanOrEqualTo(ProtocolVersion.v1_9) ? "" : constant;
    }
}
