package nightsky.module.modules.combat;

import nightsky.NightSky;
import nightsky.event.EventTarget;
import nightsky.event.types.EventType;
import nightsky.events.KeyEvent;
import nightsky.events.TickEvent;
import nightsky.module.Module;
import nightsky.util.*;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.FloatValue;
import nightsky.value.values.PercentValue;
import nightsky.value.values.IntValue;
import nightsky.value.values.ModeValue;
import nightsky.module.modules.combat.rotation.OPRotationSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AimAssist extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final TimerUtil timer = new TimerUtil();
    public final FloatValue hSpeed = new FloatValue("HorizontalSpeed", 3.0F, 0.0F, 10.0F);
    public final FloatValue vSpeed = new FloatValue("VerticalSpeed", 0.0F, 0.0F, 10.0F);
    public final PercentValue smoothing = new PercentValue("Smoothing", 50);
    public final FloatValue range = new FloatValue("Range", 4.5F, 3.0F, 8.0F);
    public final IntValue fov = new IntValue("Fov", 90, 30, 360);
    public final BooleanValue weaponOnly = new BooleanValue("WeaponsOnly", true);
    public final BooleanValue allowTools = new BooleanValue("AllowTools", false, this.weaponOnly::getValue);
    public final BooleanValue botChecks = new BooleanValue("BotCheck", true);
    public final BooleanValue team = new BooleanValue("Teams", true);
    
    public final ModeValue rotationMode = new ModeValue("RotationMode", 0, new String[]{"Normal", "OP-Rotation"});
    public final ModeValue yawAlgorithm = new ModeValue("YawAlgorithm", 0, 
        new String[]{"Linear", "SmoothLinear", "EIO", "Skewed-Unimodal", "Physical-Simulation", "Simple-NeuralNetwork", "Recorded-Features"}, 
        () -> rotationMode.getModeString().equals("OP-Rotation"));
    public final ModeValue pitchAlgorithm = new ModeValue("PitchAlgorithm", 0, 
        new String[]{"Linear", "SmoothLinear", "EIO", "Skewed-Unimodal", "Physical-Simulation", "Simple-NeuralNetwork", "Recorded-Features"}, 
        () -> rotationMode.getModeString().equals("OP-Rotation"));
    public final BooleanValue simulateFriction = new BooleanValue("SimulateFriction", true, 
        () -> rotationMode.getModeString().equals("OP-Rotation"));
    public final ModeValue frictionAlgorithm = new ModeValue("FrictionAlgorithm", 0, 
        new String[]{"Time-Incremental", "CustomCurve", "TPAC"}, 
        () -> rotationMode.getModeString().equals("OP-Rotation") && simulateFriction.getValue());
    public final BooleanValue debugTurnSpeed = new BooleanValue("DebugTurnSpeed", false, 
        () -> rotationMode.getModeString().equals("OP-Rotation"));
    public final BooleanValue recordMode = new BooleanValue("RecordMode", false, 
        () -> rotationMode.getModeString().equals("OP-Rotation") && 
              (yawAlgorithm.getModeString().equals("Recorded-Features") || 
               pitchAlgorithm.getModeString().equals("Recorded-Features")));
    
    public final BooleanValue stopOnTarget = new BooleanValue("StopOnTarget", false);
    public final IntValue delayTick = new IntValue("DelayTick", 0, 0, 5);
    
    private final OPRotationSystem opRotationSystem = new OPRotationSystem();
    private int tickCounter = 0;

    private boolean isValidTarget(EntityPlayer entityPlayer) {
        if (entityPlayer != mc.thePlayer && entityPlayer != mc.thePlayer.ridingEntity) {
            if (entityPlayer == mc.getRenderViewEntity() || entityPlayer == mc.getRenderViewEntity().ridingEntity) {
                return false;
            } else if (entityPlayer.deathTime > 0) {
                return false;
            } else if (RotationUtil.distanceToEntity(entityPlayer) > (double) this.range.getValue()) {
                return false;
            } else if (RotationUtil.angleToEntity(entityPlayer) > (float) this.fov.getValue()) {
                return false;
            } else if (RotationUtil.rayTrace(entityPlayer) != null) {
                return false;
            } else if (TeamUtil.isFriend(entityPlayer)) {
                return false;
            } else {
                return (!this.team.getValue() || !TeamUtil.isSameTeam(entityPlayer)) && (!this.botChecks.getValue() || !TeamUtil.isBot(entityPlayer));
            }
        } else {
            return false;
        }
    }

    private boolean isInReach(EntityPlayer entityPlayer) {
        Reach reach = (Reach) NightSky.moduleManager.modules.get(Reach.class);
        double distance = reach.isEnabled() ? (double) reach.range.getValue() : 3.0;
        return RotationUtil.distanceToEntity(entityPlayer) <= distance;
    }

    private boolean isLookingAtBlock() {
        return mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK;
    }
    
    private boolean isLookingAtPlayer(EntityPlayer player) {
        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectType.ENTITY) {
            if (mc.objectMouseOver.entityHit == player) {
                float randomChance = RandomUtil.nextFloat(0.7f, 0.95f);
                return Math.random() < randomChance;
            }
        }
        return false;
    }

    public AimAssist() {
        super("AimAssist", false);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.POST && mc.currentScreen == null) {
            
            if (delayTick.getValue() > 0) {
                tickCounter++;
                if (tickCounter < delayTick.getValue()) {
                    return;
                }
                tickCounter = 0;
            }
            if (!(java.lang.Boolean) this.weaponOnly.getValue()
                    || ItemUtil.hasRawUnbreakingEnchant()
                    || this.allowTools.getValue() && ItemUtil.isHoldingTool()) {
                boolean attacking = PlayerUtil.isAttacking();
                if (!attacking || !this.isLookingAtBlock()) {
                    if (attacking || !this.timer.hasTimeElapsed(350L)) {
                        List<EntityPlayer> inRange = mc.theWorld
                                .loadedEntityList
                                .stream()
                                .filter(entity -> entity instanceof EntityPlayer)
                                .map(entity -> (EntityPlayer) entity)
                                .filter(this::isValidTarget)
                                .sorted(Comparator.comparingDouble(RotationUtil::distanceToEntity))
                                .collect(Collectors.toList());
                        if (!inRange.isEmpty()) {
                            if (inRange.stream().anyMatch(this::isInReach)) {
                                inRange.removeIf(entityPlayer -> !this.isInReach(entityPlayer));
                            }
                            EntityPlayer player = inRange.get(0);
                            if (!(RotationUtil.distanceToEntity(player) <= 0.0)) {
                                
                                if (stopOnTarget.getValue() && isLookingAtPlayer(player)) {
                                    return;
                                }
                                
                                if (rotationMode.getModeString().equals("OP-Rotation")) {
                                    updateOPRotationSettings();
                                    opRotationSystem.conduct(player);
                                    
                                    if (debugTurnSpeed.getValue()) {
                                        System.out.println("转头速度: " + opRotationSystem.getTurnSpeedPublic());
                                        System.out.println("角度差异: Yaw=" + opRotationSystem.getDiffRotsData().diffYaw + 
                                                         ", Pitch=" + opRotationSystem.getDiffRotsData().diffPitch);
                                        System.out.println("最大角度差异: Yaw=" + opRotationSystem.getMaxDiffRotsData().maxDiffYaw + 
                                                         ", Pitch=" + opRotationSystem.getMaxDiffRotsData().maxDiffPitch);
                                    }
                                } else {
                                    AxisAlignedBB axisAlignedBB = player.getEntityBoundingBox();
                                    double collisionBorderSize = player.getCollisionBorderSize();
                                    float[] rotation = RotationUtil.getRotationsToBox(
                                            axisAlignedBB.expand(collisionBorderSize, collisionBorderSize, collisionBorderSize),
                                            mc.thePlayer.rotationYaw,
                                            mc.thePlayer.rotationPitch,
                                            180.0F,
                                            (float) this.smoothing.getValue() / 100.0F
                                    );
                                    float yaw = Math.min(Math.abs(this.hSpeed.getValue()), 10.0F);
                                    float pitch = Math.min(Math.abs(this.vSpeed.getValue()), 10.0F);
                                    NightSky.rotationManager
                                            .setRotation(
                                                    mc.thePlayer.rotationYaw + (rotation[0] - mc.thePlayer.rotationYaw) * 0.1F * yaw,
                                                    mc.thePlayer.rotationPitch + (rotation[1] - mc.thePlayer.rotationPitch) * 0.1F * pitch,
                                                    0,
                                                    false
                                            );
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void updateOPRotationSettings() {
        opRotationSystem.setYawAlgorithm(yawAlgorithm.getModeString());
        opRotationSystem.setPitchAlgorithm(pitchAlgorithm.getModeString());
        opRotationSystem.setSimulateFriction(simulateFriction.getValue());
        opRotationSystem.setDebugTurnSpeed(debugTurnSpeed.getValue());
        opRotationSystem.setFrictionAlgorithm(frictionAlgorithm.getModeString());
        opRotationSystem.setRecordMode(recordMode.getValue());
    }

    @EventTarget
    public void onPress(KeyEvent event) {
        if (event.getKey() == mc.gameSettings.keyBindAttack.getKeyCode() && !NightSky.moduleManager.modules.get(AutoClicker.class).isEnabled()) {
            this.timer.reset();
        }
    }
}

