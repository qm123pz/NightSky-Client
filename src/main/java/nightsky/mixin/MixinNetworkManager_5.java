package nightsky.mixin;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.vialoadingbase.netty.VLBPipeline;
import io.netty.channel.Channel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.network.NetworkManager$5")
public abstract class MixinNetworkManager_5 {
    @Inject(method = "initChannel", at = @At(value = "TAIL"))
    private void viaforge$addPipeline(Channel channel, CallbackInfo ci) {
        int targetVersion = ViaLoadingBase.getInstance().getTargetVersion().getVersion();
        int nativeVersion = ViaLoadingBase.getInstance().getNativeVersion();
        if (targetVersion != nativeVersion) {
            UserConnection user = new UserConnectionImpl(channel, true);
            new ProtocolPipelineImpl(user);
            channel.pipeline().addLast(new VLBPipeline(user) {
                @Override
                public String getDecoderHandlerName() {
                    return "decoder";
                }

                @Override
                public String getEncoderHandlerName() {
                    return "encoder";
                }

                @Override
                public String getDecompressionHandlerName() {
                    return "decompress";
                }

                @Override
                public String getCompressionHandlerName() {
                    return "compress";
                }
            });
        }
    }
}
