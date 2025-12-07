package nightsky.module.modules.combat;

import com.google.common.base.CaseFormat;
import net.minecraft.network.play.client.C02PacketUseEntity;
import nightsky.NightSky;
import nightsky.enums.BlinkModules;
import nightsky.enums.DelayModules;
import nightsky.event.EventTarget;
import nightsky.event.types.EventType;
import nightsky.events.*;
import nightsky.management.BlinkManager;
import nightsky.mixin.IAccessorEntity;
import nightsky.module.Module;
import nightsky.module.modules.movement.LongJump;
import nightsky.util.ChatUtil;
import nightsky.util.MoveUtil;
import nightsky.value.values.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.potion.Potion;

public class Velocity extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private int chanceCounter = 0;
    private int delayChanceCounter = 0;
    private boolean pendingExplosion = false;
    private boolean allowNext = true;
    private boolean jumpFlag = false;
    private boolean reverseFlag = false;
    private boolean delayActive = false;
    long lastAttackTime;
    long blinkStartTime = System.currentTimeMillis();
    long blinkDuration = 95;

    public final ModeValue mode = new ModeValue("Mode", 2, new String[]{"Vanilla", "Jump", "Prediction", "Reverse"});
    public final IntValue delayTicks = new IntValue("DelayTicks", 3, 1, 20, () -> this.mode.getValue() == 2);
    public final PercentValue delayChance = new PercentValue("DelayChange", 100, () -> this.mode.getValue() == 2);
    public final BooleanValue jumpReset = new BooleanValue("JumpReset", true, () -> this.mode.getValue() == 2);
    public final BooleanValue reduce = new BooleanValue("Reduce", false);
    public final IntValue reduceHurttime = new IntValue("ReduceHurttime", 9, 1, 10, () -> this.mode.getValue() == 2);
    public final FloatValue reduceFactor = new FloatValue("ReduceFactor", 0.6f, 0.1f, 1f, () -> this.mode.getValue() == 2);
    public final BooleanValue test = new BooleanValue("Test", true, () -> this.mode.getValue() == 2);
    public final PercentValue chance = new PercentValue("Change", 100);
    public final PercentValue horizontal = new PercentValue("Horizontal", 100);
    public final PercentValue vertical = new PercentValue("Vertical", 100);
    public final PercentValue explosionHorizontal = new PercentValue("ExplosionsHorizontal", 100);
    public final PercentValue explosionVertical = new PercentValue("ExplosionsVertical", 100);
    public final BooleanValue fakeCheck = new BooleanValue("FakeCheck", true);
    public final BooleanValue debugLog = new BooleanValue("DebugLog", false);

    private boolean isInLiquidOrWeb() {
        return mc.thePlayer.isInWater() || mc.thePlayer.isInLava() || ((IAccessorEntity) mc.thePlayer).getIsInWeb();
    }

    private boolean canDelay() {
        KillAura killAura = (KillAura) NightSky.moduleManager.modules.get(KillAura.class);
        return mc.thePlayer.onGround && (!killAura.isEnabled() || !killAura.shouldAutoBlock());
    }

    public Velocity() {
        super("Velocity", false);
    }

    @EventTarget
    public void onKnockback(KnockbackEvent event) {
        if (!this.isEnabled() || event.isCancelled()) {
            this.pendingExplosion = false;
            this.allowNext = true;
        } else if (!this.allowNext || !(Boolean) this.fakeCheck.getValue()) {
            this.allowNext = true;
            if (this.pendingExplosion) {
                this.pendingExplosion = false;
                if (this.explosionHorizontal.getValue() > 0) {
                    event.setX(event.getX() * (double) this.explosionHorizontal.getValue() / 100.0);
                    event.setZ(event.getZ() * (double) this.explosionHorizontal.getValue() / 100.0);
                } else {
                    event.setX(mc.thePlayer.motionX);
                    event.setZ(mc.thePlayer.motionZ);
                }
                if (this.explosionVertical.getValue() > 0) {
                    event.setY(event.getY() * (double) this.explosionVertical.getValue() / 100.0);
                } else {
                    event.setY(mc.thePlayer.motionY);
                }
            } else {
                this.chanceCounter = this.chanceCounter % 100 + this.chance.getValue();
                if (this.chanceCounter >= 100) {
                    this.jumpFlag = (this.mode.getValue() == 1 || this.mode.getValue() == 2) && event.getY() > 0.0;
                    this.delayActive = this.mode.getValue() == 3;
                    if (this.horizontal.getValue() > 0) {
                        event.setX(event.getX() * (double) this.horizontal.getValue() / 100.0);
                        event.setZ(event.getZ() * (double) this.horizontal.getValue() / 100.0);
                    } else {
                        event.setX(mc.thePlayer.motionX);
                        event.setZ(mc.thePlayer.motionZ);
                    }
                    if (this.vertical.getValue() > 0) {
                        event.setY(event.getY() * (double) this.vertical.getValue() / 100.0);
                    } else {
                        event.setY(mc.thePlayer.motionY);
                    }
                }
            }
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (event.getType() == EventType.POST) {
            if (this.reverseFlag
                    && (
                    this.canDelay()
                            || this.isInLiquidOrWeb()
                            || NightSky.delayManager.getDelay() >= (long) this.delayTicks.getValue()
            )) {
                NightSky.delayManager.setDelayState(false, DelayModules.VELOCITY);
                this.reverseFlag = false;
            }
            if (this.delayActive) {
                MoveUtil.setSpeed(MoveUtil.getSpeed(), MoveUtil.getMoveYaw());
                this.delayActive = false;
            }
        }
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (this.jumpFlag) {
            this.jumpFlag = false;
            if (mc.thePlayer.onGround && mc.thePlayer.isSprinting() && !mc.thePlayer.isPotionActive(Potion.jump) && !this.isInLiquidOrWeb()) {
                mc.thePlayer.movementInput.jump = true;
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (this.isEnabled() && event.getType() == EventType.RECEIVE && !event.isCancelled()) {
            if (event.getPacket() instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity packet = (S12PacketEntityVelocity) event.getPacket();
                if (packet.getEntityID() == mc.thePlayer.getEntityId()) {
                    LongJump longJump = (LongJump) NightSky.moduleManager.modules.get(LongJump.class);
                    if (this.mode.getValue() == 2
                            && !this.reverseFlag
                            && !this.canDelay()
                            && !this.isInLiquidOrWeb()
                            && !this.pendingExplosion
                            && (!this.allowNext || !(Boolean) this.fakeCheck.getValue())
                            && (!longJump.isEnabled() || !longJump.canStartJump())) {
                        this.delayChanceCounter = this.delayChanceCounter % 100 +
                                this.delayChance.getValue();
                        if (this.delayChanceCounter >= 100) {
                            NightSky.delayManager.setDelayState(true, DelayModules.VELOCITY);
                            NightSky.delayManager.delayedPacket.offer(packet);
                            event.setCancelled(true);
                            this.reverseFlag = true;
                            if (jumpReset.getValue()) {
                                int start = 0;
                                if (mc.thePlayer.hurtTime >= 8) {
                                    mc.gameSettings.keyBindJump.isPressed();
                                }
                                if (mc.thePlayer.hurtTime >= 7 && !mc.gameSettings.keyBindForward.isPressed()) {
                                    mc.gameSettings.keyBindForward.isPressed();
                                    start = 1;
                                }
                                if (mc.thePlayer.hurtTime < 7 && mc.thePlayer.hurtTime > 0) {
                                    mc.gameSettings.keyBindJump.isPressed();
                                    if (start == 1) {
                                        mc.gameSettings.keyBindForward.isPressed();
                                    }
                                }
                            }
                            if (event.getType() == EventType.SEND && event.getPacket() instanceof C02PacketUseEntity){
                            C02PacketUseEntity packet2 = (C02PacketUseEntity) event.getPacket();
                            if(packet2.getAction() == C02PacketUseEntity.Action.ATTACK && reduce.getValue() && mode.getValue() == 2) {
                                if (mc.thePlayer.hurtTime == reduceHurttime.getValue() && System.currentTimeMillis() - lastAttackTime <= 8000) {
                                    mc.thePlayer.motionX *= reduceFactor.getValue();
                                    mc.thePlayer.motionZ *= reduceFactor.getValue();
                                    if (debugLog.getValue()) {
                                        ChatUtil.sendFormatted("Reduce!");
                                    }
                                }
                                lastAttackTime = System.currentTimeMillis();
                                }
                            }
                            if(test.getValue()){
                                if(System.currentTimeMillis() - blinkStartTime < blinkDuration){
                                    NightSky.blinkManager.setBlinkState(true, BlinkModules.BLINK);
                                } else {
                                    NightSky.blinkManager.setBlinkState(false, BlinkModules.BLINK);
                                }
                            }
                            return;
                        }
                    }
                    if (this.debugLog.getValue()) {
                        ChatUtil.sendFormatted(
                                String.format(
                                        "%sVelocity (&otick: %d, x: %.2f, y: %.2f, z: %.2f&r)&r",
                                        NightSky.clientName,
                                        mc.thePlayer.ticksExisted,
                                        (double) packet.getMotionX() / 8000.0,
                                        (double) packet.getMotionY() / 8000.0,
                                        (double) packet.getMotionZ() / 8000.0
                                )
                        );
                    }
                }
            } else if (!(event.getPacket() instanceof S27PacketExplosion)) {
                if (event.getPacket() instanceof S19PacketEntityStatus) {
                    S19PacketEntityStatus packet = (S19PacketEntityStatus) event.getPacket();
                    Entity entity = packet.getEntity(mc.theWorld);
                    if (entity != null && entity.equals(mc.thePlayer) && packet.getOpCode() == 2) {
                        this.allowNext = false;
                    }
                }
            } else {
                S27PacketExplosion packet = (S27PacketExplosion) event.getPacket();
                if (packet.func_149149_c() != 0.0F || packet.func_149144_d() != 0.0F || packet.func_149147_e() != 0.0F) {
                    this.pendingExplosion = true;
                    if (this.explosionHorizontal.getValue() == 0 || this.explosionVertical.getValue() == 0) {
                        event.setCancelled(true);
                    }
                    if (this.debugLog.getValue()) {
                        ChatUtil.sendFormatted(
                                String.format(
                                        "%sExplosion (&otick: %d, x: %.2f, y: %.2f, z: %.2f&r)&r",
                                        NightSky.clientName,
                                        mc.thePlayer.ticksExisted,
                                        mc.thePlayer.motionX + (double) packet.func_149149_c(),
                                        mc.thePlayer.motionY + (double) packet.func_149144_d(),
                                        mc.thePlayer.motionZ + (double) packet.func_149147_e()
                                )
                        );
                    }
                }
            }
        }
    }

    @EventTarget
    public void onLoadWorld(LoadWorldEvent event) {
        this.onDisabled();
    }

    @Override
    public void onDisabled() {
        this.pendingExplosion = false;
        this.allowNext = true;
    }

    @Override
    public String[] getSuffix() {
        return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeString())};
    }
}