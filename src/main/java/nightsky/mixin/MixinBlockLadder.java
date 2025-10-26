package nightsky.mixin;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.block.BlockLadder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BlockLadder.class)
public abstract class MixinBlockLadder {
    @ModifyConstant(method = "setBlockBoundsBasedOnState", constant = @Constant(floatValue = 0.125F))
    private float viaforge$fixBlockBounds(float constant) {
        ProtocolVersion targetVersion = ViaLoadingBase.getInstance().getTargetVersion();
        return targetVersion.newerThanOrEqualTo(ProtocolVersion.v1_9) ? 0.1875F : constant;
    }
}
