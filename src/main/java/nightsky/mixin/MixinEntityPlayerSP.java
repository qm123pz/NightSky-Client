package nightsky.mixin;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import nightsky.NightSky;
import nightsky.event.EventManager;
import nightsky.event.types.EventType;
import nightsky.events.LivingUpdateEvent;
import nightsky.events.MoveInputEvent;
import nightsky.events.PlayerUpdateEvent;
import nightsky.events.UpdateEvent;
import nightsky.management.RotationState;
import nightsky.module.modules.player.AntiDebuff;
import nightsky.module.modules.movement.NoSlow;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SideOnly(Side.CLIENT)
@Mixin({EntityPlayerSP.class})
public abstract class MixinEntityPlayerSP extends MixinEntityPlayer {
    @Unique
    private float overrideYaw = Float.NaN;
    @Unique
    private float overridePitch = Float.NaN;
    @Unique
    private float pendingYaw;
    @Unique
    private float pendingPitch;
    @Shadow
    private float lastReportedYaw;
    @Shadow
    private float lastReportedPitch;
    @Shadow
    public float renderArmYaw;
    @Shadow
    public float prevRenderArmYaw;

    @Inject(
            method = {"onUpdate"},
            at = {@At("HEAD")}
    )
    private void onUpdate(CallbackInfo callbackInfo) {
        if (this.worldObj.isBlockLoaded(new BlockPos(this.posX, 0.0, this.posZ))) {
            UpdateEvent event = new UpdateEvent(EventType.PRE, this.lastReportedYaw, this.lastReportedPitch, this.rotationYaw, this.rotationPitch);
            EventManager.call(event);
            RotationState.applyState(event.isRotated() && !this.isRiding(), event.getNewYaw(), event.getNewPitch(), event.getPreYaw(), event.isRotating());
            if (event.isRotated()) {
                this.pendingYaw = this.rotationYaw;
                this.pendingPitch = this.rotationPitch;
                this.overrideYaw = event.getNewYaw();
                this.overridePitch = event.getNewPitch();
            } else {
                this.pendingYaw = Float.NaN;
                this.pendingPitch = Float.NaN;
                this.overrideYaw = Float.NaN;
                this.overridePitch = Float.NaN;
            }
        }
    }

    @Inject(
            method = {"onUpdate"},
            at = {@At("RETURN")}
    )
    private void postUpdate(CallbackInfo callbackInfo) {
        if (this.worldObj.isBlockLoaded(new BlockPos(this.posX, 0.0, this.posZ))) {
            if (!Float.isNaN(this.pendingYaw) && !Float.isNaN(this.pendingPitch)) {
                this.lastReportedYaw = this.rotationYaw;
                this.lastReportedPitch = this.rotationPitch;
                this.rotationYaw = this.rotationYaw + MathHelper.wrapAngleTo180_float(this.pendingYaw - this.rotationYaw);
                this.rotationPitch = this.pendingPitch;
                this.prevRotationYaw = this.rotationYaw;
                this.prevRotationPitch = this.rotationPitch;
                this.prevRenderArmYaw = this.rotationYaw - (this.renderArmYaw - this.prevRenderArmYaw) * 2.0F;
                this.renderArmYaw = this.rotationYaw;
            }
            EventManager.call(new UpdateEvent(EventType.POST, this.lastReportedYaw, this.lastReportedPitch, this.rotationYaw, this.rotationPitch));
        }
    }

    @Redirect(
            method = {"onUpdate"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;isRiding()Z"
            )
    )
    private boolean onRidding(EntityPlayerSP entityPlayerSP) {
        if (!Float.isNaN(this.overrideYaw) && !Float.isNaN(this.overridePitch)) {
            this.rotationYaw = this.overrideYaw;
            this.rotationPitch = this.overridePitch;
        }
        return entityPlayerSP.isRiding();
    }

    @Inject(
            method = {"onUpdate"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;onUpdateWalkingPlayer()V"
            )}
    )
    private void onMotionUpdate(CallbackInfo callbackInfo) {
        EventManager.call(new PlayerUpdateEvent());
    }

    @Inject(
            method = {"onLivingUpdate"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/AbstractClientPlayer;onLivingUpdate()V"
            )}
    )
    private void onLivingUpdate(CallbackInfo callbackInfo) {
        EventManager.call(new LivingUpdateEvent());
    }

    @Inject(
            method = {"onLivingUpdate"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/MovementInput;updatePlayerMoveState()V",
                    shift = At.Shift.AFTER
            )}
    )
    private void updateMove(CallbackInfo callbackInfo) {
        EventManager.call(new MoveInputEvent());
    }

    @Redirect(
            method = {"onLivingUpdate"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;isUsingItem()Z"
            )
    )
    private boolean isUsing(EntityPlayerSP entityPlayerSP) {
        NoSlow noSlow = (NoSlow) NightSky.moduleManager.modules.get(NoSlow.class);
        return (!noSlow.isEnabled() || !noSlow.isAnyActive()) && entityPlayerSP.isUsingItem();
    }

    @Redirect(
            method = {"onLivingUpdate"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;isPotionActive(Lnet/minecraft/potion/Potion;)Z"
            )
    )
    private boolean checkPotion(EntityPlayerSP entityPlayerSP, Potion potion) {
        if (potion == Potion.confusion && NightSky.moduleManager != null) {
            AntiDebuff antiDebuff = (AntiDebuff) NightSky.moduleManager.modules.get(AntiDebuff.class);
            if (antiDebuff.isEnabled() && antiDebuff.nausea.getValue()) {
                return false;
            }
        }
        return ((IAccessorEntityLivingBase) entityPlayerSP).getActivePotionsMap().containsKey(potion.id);
    }

    @ModifyConstant(method = "onUpdateWalkingPlayer", constant = @Constant(doubleValue = 9.0E-4D))
    private double viaforge$fixPointThree(double constant) {
        ProtocolVersion targetVersion = ViaLoadingBase.getInstance().getTargetVersion();
        return targetVersion.newerThanOrEqualTo(ProtocolVersion.v1_18_2) ? 4.0E-8D : constant;
    }

    @ModifyConstant(method = "onUpdateWalkingPlayer", constant = @Constant(intValue = 20))
    private int viaforge$fixPosUpdateTicks(int constant) {
        ProtocolVersion targetVersion = ViaLoadingBase.getInstance().getTargetVersion();
        return targetVersion.newerThanOrEqualTo(ProtocolVersion.v1_9) ? 19 : constant;
    }
}
