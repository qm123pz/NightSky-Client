package nightsky.mixin;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {
    @Shadow
    public abstract void addToSendQueue(Packet<?> packet);

    @Inject(method = "handleConfirmTransaction", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/Packet;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V", shift = At.Shift.AFTER), cancellable = true)
    private void viaforge$sendC0F(S32PacketConfirmTransaction packet, CallbackInfo ci) {
        ProtocolVersion targetVersion = ViaLoadingBase.getInstance().getTargetVersion();
        if (targetVersion.newerThanOrEqualTo(ProtocolVersion.v1_17)) {
            addToSendQueue(new C0FPacketConfirmTransaction(packet.getWindowId(), (short) 0, false));
            ci.cancel();
        }
    }

    @ModifyArg(method = {"handleJoinGame", "handleRespawn"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V"))
    private GuiScreen viaforge$skipDownloadTerrainScreen(GuiScreen guiScreen) {
        return null;
    }
}
