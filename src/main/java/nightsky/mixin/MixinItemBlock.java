package nightsky.mixin;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemBlock.class)
public abstract class MixinItemBlock {
    @Redirect(method = "onItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSoundEffect(DDDLjava/lang/String;FF)V"))
    private void viaforge$fixPlaceSound(World instance, double x, double y, double z, String sound, float volume, float pitch) {
        ProtocolVersion targetVersion = ViaLoadingBase.getInstance().getTargetVersion();
        if (targetVersion.newerThanOrEqualTo(ProtocolVersion.v1_9)) {
            instance.playSound(x, y, z, sound, volume, pitch, false);
            return;
        }

        instance.playSoundEffect(x, y, z, sound, volume, pitch);
    }
}
