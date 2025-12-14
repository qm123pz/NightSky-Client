package nightsky.module.modules.combat;

import com.google.common.base.CaseFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import nightsky.NightSky;
import nightsky.enums.BlinkModules;
import nightsky.enums.DelayModules;
import nightsky.event.EventTarget;
import nightsky.event.types.EventType;
import nightsky.events.KnockbackEvent;
import nightsky.events.LivingUpdateEvent;
import nightsky.events.LoadWorldEvent;
import nightsky.events.PacketEvent;
import nightsky.events.UpdateEvent;
import nightsky.mixin.IAccessorEntity;
import nightsky.module.Module;
import nightsky.module.modules.movement.LongJump;
import nightsky.util.ChatUtil;
import nightsky.util.MoveUtil;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.FloatValue;
import nightsky.value.values.IntValue;
import nightsky.value.values.ModeValue;
import nightsky.value.values.PercentValue;

public class Velocity extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private int chanceCounter = 0;
    private int delayChanceCounter = 0;
    private boolean pendingExplosion = false;
    private boolean allowNext = true;
    private boolean reverseFlag = false;
    private boolean delayActive = false;
    private boolean jumpFlag = false;
    private long lastAttackTime = 0L;
    private long blinkStartTime = System.currentTimeMillis();
    private final long blinkDuration = 95L;
    private long reverseStartTime = 0L;
    public final ModeValue mode = new ModeValue("Mode", 2, new String[]{"Vanilla", "Jump", "Prediction"});
    public final PercentValue chance = new PercentValue("Chance", 100);
    public final PercentValue horizontal = new PercentValue("Horizontal", 100);
    public final PercentValue vertical = new PercentValue("Vertical", 100);
    public final PercentValue explosionHorizontal = new PercentValue("ExplosionsHorizontal", 100);
    public final PercentValue explosionVertical = new PercentValue("ExplosionsVertical", 100);
    public final BooleanValue fakeCheck = new BooleanValue("FakeCheck", true);
    public final BooleanValue debugLog = new BooleanValue("DebugLog", true);
    public final IntValue delayTicks = new IntValue("DelayTicks", 1, 1, 20, () -> this.mode.getValue() == 2);
    public final PercentValue delayChance = new PercentValue("DelayChange", 100, () -> this.mode.getValue() == 2);
    public final BooleanValue jumpReset = new BooleanValue("JumpReset", true, () -> this.mode.getValue() == 2);
    public final IntValue hurt = new IntValue("ReduceHurtTime", 10, 1, 10, () -> this.mode.getValue() == 2);
    public final FloatValue astolftor = new FloatValue("ReduceFactor", 0.6F, 0.1F, 1.0F, () -> this.mode.getValue() == 2);
    public final BooleanValue test = new BooleanValue("Test", true, () -> this.mode.getValue() == 2);
    public final BooleanValue userDp = new BooleanValue("USer", false, () -> this.mode.getValue() == 1);

    public Velocity() {
        super("Velocity", false);
    }

    private boolean isInLiquidOrWeb() {
        return mc.thePlayer != null && (mc.thePlayer.isInWater() || mc.thePlayer.isInLava() || ((IAccessorEntity) mc.thePlayer).getIsInWeb());
    }

    private boolean canDelay() {
        KillAura killAura = (KillAura) NightSky.moduleManager.modules.get(KillAura.class);
        return mc.thePlayer.onGround && (killAura == null || !killAura.isEnabled() || !killAura.shouldAutoBlock());
    }

    @EventTarget
    public void onKnockback(KnockbackEvent event) {
        if (!this.isEnabled() || event.isCancelled() || mc.thePlayer == null) {
            this.pendingExplosion = false;
            this.allowNext = true;
            return;
        }
        if (this.mode.getValue() == 2) {
            if (!this.allowNext || !this.fakeCheck.getValue()) {
                this.allowNext = true;
                if (this.pendingExplosion) {
                    this.pendingExplosion = false;
                    this.handleExplosion(event);
                } else {
                    if (this.jumpReset.getValue() && event.getY() > 0.0) {
                        this.jumpFlag = true;
                        if (this.debugLog.getValue()) {
                            ChatUtil.sendFormatted(String.format("%s[Prediction] jr! &r", NightSky.clientName));
                        }
                    }
                    this.applyMotion(event, this.horizontal.getValue(), this.vertical.getValue());
                }
            }
            return;
        }
        if (!this.allowNext || !this.fakeCheck.getValue()) {
            this.allowNext = true;
            if (this.pendingExplosion) {
                this.pendingExplosion = false;
                this.handleExplosion(event);
            } else {
                this.chanceCounter = this.chanceCounter % 100 + this.chance.getValue();
                if (this.chanceCounter >= 100) {
                    boolean jumpMode = this.mode.getValue() == 1 && event.getY() > 0.0;
                    this.jumpFlag = jumpMode;
                    if (jumpMode) {
                        this.applyMotion(event, this.horizontal.getValue(), this.vertical.getValue());
                    } else {
                        this.applyVanilla(event);
                    }
                }
            }
        }
    }

    private void applyMotion(KnockbackEvent event, int horizontalPct, int verticalPct) {
        if (horizontalPct > 0) {
            event.setX(event.getX() * horizontalPct / 100.0);
            event.setZ(event.getZ() * horizontalPct / 100.0);
        } else {
            event.setX(mc.thePlayer.motionX);
            event.setZ(mc.thePlayer.motionZ);
        }
        if (verticalPct > 0) {
            event.setY(event.getY() * verticalPct / 100.0);
        } else {
            event.setY(mc.thePlayer.motionY);
        }
    }

    private void applyVanilla(KnockbackEvent event) {
        this.applyMotion(event, this.horizontal.getValue(), this.vertical.getValue());
    }

    private void handleExplosion(KnockbackEvent event) {
        this.applyMotion(event, this.explosionHorizontal.getValue(), this.explosionVertical.getValue());
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (!this.isEnabled() || mc.thePlayer == null) {
            return;
        }
        if (event.getType() == EventType.RECEIVE && !event.isCancelled()) {
            if (event.getPacket() instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity packet = (S12PacketEntityVelocity) event.getPacket();
                if (packet.getEntityID() != mc.thePlayer.getEntityId()) {
                    return;
                }
                if (this.mode.getValue() == 2) {
                    LongJump longJump = (LongJump) NightSky.moduleManager.modules.get(LongJump.class);
                    boolean canStartJump = longJump != null && longJump.isEnabled() && longJump.canStartJump();
                    if (!this.reverseFlag && !this.isInLiquidOrWeb() && !this.pendingExplosion && !(this.allowNext && this.fakeCheck.getValue()) && !canStartJump) {
                        this.delayChanceCounter = this.delayChanceCounter % 100 + this.delayChance.getValue();
                        if (this.delayChanceCounter >= 100) {
                            NightSky.delayManager.setDelayState(true, DelayModules.VELOCITY);
                            NightSky.delayManager.delayedPacket.offer(packet);
                            event.setCancelled(true);
                            this.reverseFlag = true;
                            this.reverseStartTime = System.currentTimeMillis();
                            if (this.test.getValue()) {
                                this.blinkStartTime = System.currentTimeMillis();
                                NightSky.blinkManager.setBlinkState(true, BlinkModules.BLINK);
                            }
                            this.delayChanceCounter = 0;
                            return;
                        }
                    }
                    if (this.debugLog.getValue()) {
                        ChatUtil.sendFormatted(String.format("%sVelocity (tick: %d, x: %.2f, y: %.2f, z: %.2f)&r", NightSky.clientName, mc.thePlayer.ticksExisted, packet.getMotionX() / 8000.0, packet.getMotionY() / 8000.0, packet.getMotionZ() / 8000.0));
                    }
                    return;
                }
                if (this.mode.getValue() == 1 && this.userDp.getValue() && !mc.thePlayer.onGround) {
                    NightSky.delayManager.setDelayState(true, DelayModules.VELOCITY);
                    NightSky.delayManager.delayedPacket.offer(packet);
                    event.setCancelled(true);
                    if (this.debugLog.getValue()) {
                        ChatUtil.sendFormatted(String.format("%s[Jump] air delay!&r", NightSky.clientName));
                    }
                    return;
                }
                if (this.debugLog.getValue()) {
                    ChatUtil.sendFormatted(String.format("%sVelocity (tick: %d, x: %.2f, y: %.2f, z: %.2f)&r", NightSky.clientName, mc.thePlayer.ticksExisted, packet.getMotionX() / 8000.0, packet.getMotionY() / 8000.0, packet.getMotionZ() / 8000.0));
                }
            } else if (event.getPacket() instanceof S19PacketEntityStatus) {
                S19PacketEntityStatus packet = (S19PacketEntityStatus) event.getPacket();
                World world = mc.theWorld;
                if (world != null) {
                    Entity entity = packet.getEntity(world);
                    if (entity != null && entity.equals(mc.thePlayer) && packet.getOpCode() == 2) {
                        this.allowNext = false;
                    }
                }
            } else if (event.getPacket() instanceof S27PacketExplosion) {
                S27PacketExplosion packet = (S27PacketExplosion) event.getPacket();
                if (packet.func_149149_c() != 0.0F || packet.func_149144_d() != 0.0F || packet.func_149147_e() != 0.0F) {
                    this.pendingExplosion = true;
                    if (this.explosionHorizontal.getValue() == 0 || this.explosionVertical.getValue() == 0) {
                        event.setCancelled(true);
                    }
                    if (this.debugLog.getValue()) {
                        ChatUtil.sendFormatted(String.format("%sExplosion (tick: %d, x: %.2f, y: %.2f, z: %.2f)&r", NightSky.clientName, mc.thePlayer.ticksExisted, mc.thePlayer.motionX + packet.func_149149_c(), mc.thePlayer.motionY + packet.func_149144_d(), mc.thePlayer.motionZ + packet.func_149147_e()));
                    }
                }
            }
        } else if (event.getType() == EventType.SEND && !event.isCancelled() && this.mode.getValue() == 2 && event.getPacket() instanceof C02PacketUseEntity) {
            C02PacketUseEntity packet = (C02PacketUseEntity) event.getPacket();
            if (packet.getAction() == C02PacketUseEntity.Action.ATTACK) {
                this.lastAttackTime = System.currentTimeMillis();
            }
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != EventType.POST || this.mode.getValue() != 2) {
            return;
        }
        if (this.astolftor.getValue() < 1.0F && mc.thePlayer.hurtTime == this.hurt.getValue() && System.currentTimeMillis() - this.lastAttackTime <= 8000L) {
            mc.thePlayer.motionX *= this.astolftor.getValue();
            mc.thePlayer.motionZ *= this.astolftor.getValue();
        }
        if (this.reverseFlag) {
            boolean shouldRelease = false;
            int delayValue = this.delayTicks.getValue();
            if (delayValue >= 1 && delayValue <= 3) {
                long requiredDelay = delayValue == 1 ? 60L : (delayValue == 2 ? 95L : 100L);
                if (System.currentTimeMillis() - this.reverseStartTime >= requiredDelay) {
                    shouldRelease = true;
                }
            } else {
                shouldRelease = this.canDelay() || this.isInLiquidOrWeb() || NightSky.delayManager.getDelay() >= delayValue;
            }
            if (shouldRelease) {
                NightSky.delayManager.setDelayState(false, DelayModules.VELOCITY);
                this.reverseFlag = false;
                NightSky.blinkManager.setBlinkState(false, BlinkModules.BLINK);
            }
        }
        if (this.delayActive) {
            MoveUtil.setSpeed(MoveUtil.getSpeed(), MoveUtil.getMoveYaw());
            this.delayActive = false;
        }
        if (this.test.getValue()) {
            if (System.currentTimeMillis() - this.blinkStartTime < this.blinkDuration) {
                NightSky.blinkManager.setBlinkState(true, BlinkModules.BLINK);
            } else {
                NightSky.blinkManager.setBlinkState(false, BlinkModules.BLINK);
            }
        }
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (this.jumpFlag) {
            this.jumpFlag = false;
            if (mc.thePlayer.onGround && mc.thePlayer.isSprinting() && !mc.thePlayer.isPotionActive(Potion.jump) && !this.isInLiquidOrWeb()) {
                mc.thePlayer.movementInput.jump = true;
                if (this.debugLog.getValue()) {
                    ChatUtil.sendFormatted(String.format("%s[Prediction] jr successfully! &r", NightSky.clientName));
                }
            }
        }
    }

    @EventTarget
    public void onLoadWorld(LoadWorldEvent event) {
        this.onDisabled();
    }

    @Override
    public void onEnabled() {
        this.pendingExplosion = false;
        this.allowNext = true;
        this.chanceCounter = 0;
        this.delayChanceCounter = 0;
        this.reverseFlag = false;
        this.delayActive = false;
        this.lastAttackTime = 0L;
        this.blinkStartTime = System.currentTimeMillis();
        this.reverseStartTime = 0L;
        this.jumpFlag = false;
    }

    @Override
    public void onDisabled() {
        this.pendingExplosion = false;
        this.allowNext = true;
        this.chanceCounter = 0;
        this.delayChanceCounter = 0;
        this.reverseFlag = false;
        this.delayActive = false;
        this.lastAttackTime = 0L;
        this.reverseStartTime = 0L;
        this.jumpFlag = false;
        if (NightSky.delayManager.getDelayModule() == DelayModules.VELOCITY) {
            NightSky.delayManager.setDelayState(false, DelayModules.VELOCITY);
        }
        NightSky.delayManager.delayedPacket.clear();
        NightSky.blinkManager.setBlinkState(false, BlinkModules.BLINK);
    }

    @Override
    public String[] getSuffix() {
        String modeName = this.mode.getModeString();
        return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, modeName)};
    }
}