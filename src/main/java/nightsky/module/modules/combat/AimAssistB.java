/**
hSpeed/vSpeed: 水平/垂直转向速度，控制瞄准时的旋转速度
smoothing: 平滑度，让转向更自然
range: 瞄准范围
fov: 视野角度限制
weaponOnly/allowTools: 武器检测，防止空手时触发
botChecks/team: 机器人检测和队伍检测
advancedSafety: 启用高级安全机制
safetyJitter: 安全抖动间隔（毫秒），定期添加随机偏移
safetyThreshold: 安全阈值，控制随机偏移幅度
adaptiveTiming: 自适应时序，随机化触发时间
timingVariation: 时序变化范围
enableNoise: 启用噪声系统
noise1Intensity/Frequency: 第一层噪声强度和频率（低频）
noise2Intensity/Frequency: 第二层噪声强度和频率（中频）
noise3Intensity/Frequency: 第三层噪声强度和频率（高频）
dynamicNoise: 根据距离动态调整噪声强度
aiTraining: 启用AI训练转头
trainingAccuracy: 训练准确度，控制AI学习程度
learningRate: 学习率，影响AI适应速度
behavioralPatterns: 行为模式模拟
patternComplexity: 模式复杂度
rotationMode: 选择旋转算法模式
yawAlgorithm/pitchAlgorithm: Yaw/Pitch轴算法选择
simulateFriction: 模拟摩擦力
frictionAlgorithm: 摩擦力算法类型
debugTurnSpeed: 显示调试信息
recordMode: 记录模式
 */
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
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class AimAssistB extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final TimerUtil timer = new TimerUtil();
    private final Random random = new Random();


    public final FloatValue hSpeed = new FloatValue("HorizontalSpeed", 3.0F, 0.0F, 10.0F);
    public final FloatValue vSpeed = new FloatValue("VerticalSpeed", 0.0F, 0.0F, 10.0F);
    public final PercentValue smoothing = new PercentValue("Smoothing", 50);
    public final FloatValue range = new FloatValue("Range", 4.5F, 3.0F, 8.0F);
    public final IntValue fov = new IntValue("Fov", 90, 30, 360);
    public final BooleanValue weaponOnly = new BooleanValue("WeaponsOnly", true);
    public final BooleanValue allowTools = new BooleanValue("AllowTools", false, this.weaponOnly::getValue);
    public final BooleanValue botChecks = new BooleanValue("BotCheck", true);
    public final BooleanValue team = new BooleanValue("Teams", true);


    public final BooleanValue advancedSafety = new BooleanValue("AdvancedSafety", true);
    public final IntValue safetyJitter = new IntValue("SafetyJitter", 150, 50, 500);
    public final FloatValue safetyThreshold = new FloatValue("SafetyThreshold", 15.0F, 5.0F, 30.0F);
    public final BooleanValue adaptiveTiming = new BooleanValue("AdaptiveTiming", true);
    public final FloatValue timingVariation = new FloatValue("TimingVariation", 0.3F, 0.0F, 1.0F);


    public final BooleanValue enableNoise = new BooleanValue("EnableNoise", true);
    public final FloatValue noise1Intensity = new FloatValue("Noise1Intensity", 0.8F, 0.0F, 3.0F, enableNoise::getValue);
    public final FloatValue noise1Frequency = new FloatValue("Noise1Frequency", 0.5F, 0.0F, 2.0F, enableNoise::getValue);
    public final FloatValue noise2Intensity = new FloatValue("Noise2Intensity", 0.5F, 0.0F, 2.0F, enableNoise::getValue);
    public final FloatValue noise2Frequency = new FloatValue("Noise2Frequency", 1.2F, 0.0F, 3.0F, enableNoise::getValue);
    public final FloatValue noise3Intensity = new FloatValue("Noise3Intensity", 0.3F, 0.0F, 1.5F, enableNoise::getValue);
    public final FloatValue noise3Frequency = new FloatValue("Noise3Frequency", 2.0F, 0.0F, 5.0F, enableNoise::getValue);
    public final BooleanValue dynamicNoise = new BooleanValue("DynamicNoise", true, enableNoise::getValue);


    public final BooleanValue aiTraining = new BooleanValue("AITraining", true);
    public final FloatValue trainingAccuracy = new FloatValue("TrainingAccuracy", 0.95F, 0.7F, 1.0F, aiTraining::getValue);
    public final FloatValue learningRate = new FloatValue("LearningRate", 0.1F, 0.01F, 0.5F, aiTraining::getValue);
    public final BooleanValue behavioralPatterns = new BooleanValue("BehavioralPatterns", true, aiTraining::getValue);
    public final FloatValue patternComplexity = new FloatValue("PatternComplexity", 0.7F, 0.1F, 1.0F, () -> aiTraining.getValue() && behavioralPatterns.getValue());


    public final ModeValue rotationMode = new ModeValue("RotationMode", 0, new String[]{"Normal", "OP-Rotation", "AI-Enhanced"});
    public final ModeValue yawAlgorithm = new ModeValue("YawAlgorithm", 0,
            new String[]{"Linear", "SmoothLinear", "EIO", "Skewed-Unimodal", "Physical-Simulation", "Simple-NeuralNetwork", "Recorded-Features", "AI-Training"},
            () -> rotationMode.getModeString().equals("OP-Rotation") || rotationMode.getModeString().equals("AI-Enhanced"));
    public final ModeValue pitchAlgorithm = new ModeValue("PitchAlgorithm", 0,
            new String[]{"Linear", "SmoothLinear", "EIO", "Skewed-Unimodal", "Physical-Simulation", "Simple-NeuralNetwork", "Recorded-Features", "AI-Training"},
            () -> rotationMode.getModeString().equals("OP-Rotation") || rotationMode.getModeString().equals("AI-Enhanced"));
    public final BooleanValue simulateFriction = new BooleanValue("SimulateFriction", true,
            () -> rotationMode.getModeString().equals("OP-Rotation") || rotationMode.getModeString().equals("AI-Enhanced"));
    public final ModeValue frictionAlgorithm = new ModeValue("FrictionAlgorithm", 0,
            new String[]{"Time-Incremental", "CustomCurve", "TPAC", "AI-Adaptive"},
            () -> (rotationMode.getModeString().equals("OP-Rotation") || rotationMode.getModeString().equals("AI-Enhanced")) && simulateFriction.getValue());


    public final BooleanValue debugTurnSpeed = new BooleanValue("DebugTurnSpeed", false,
            () -> rotationMode.getModeString().equals("OP-Rotation") || rotationMode.getModeString().equals("AI-Enhanced"));
    public final BooleanValue recordMode = new BooleanValue("RecordMode", false,
            () -> rotationMode.getModeString().equals("OP-Rotation") || rotationMode.getModeString().equals("AI-Enhanced"));


    public final BooleanValue stopOnTarget = new BooleanValue("StopOnTarget", false);
    public final IntValue delayTick = new IntValue("DelayTick", 0, 0, 5);

    private final OPRotationSystem opRotationSystem = new OPRotationSystem();
    private int tickCounter = 0;
    private long lastRotationTime = 0;
    private float previousYaw = 0;
    private float previousPitch = 0;
    private int safetyCounter = 0;


    private double noise1Phase = 0;
    private double noise2Phase = 0;
    private double noise3Phase = 0;

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


    private float[] getRotationsToEntity(EntityPlayer entity) {
        double x = entity.posX - mc.thePlayer.posX;
        double y = (entity.posY + entity.getEyeHeight()) - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double z = entity.posZ - mc.thePlayer.posZ;

        double distance = Math.sqrt(x * x + z * z);

        float yaw = (float) (Math.toDegrees(Math.atan2(z, x)) - 90.0F);
        float pitch = (float) (-Math.toDegrees(Math.atan2(y, distance)));

        return new float[]{yaw, pitch};
    }


    private float generateNoise(float baseValue, FloatValue intensity, FloatValue frequency) {
        float noise = (float) Math.sin(noise1Phase) * intensity.getValue();
        noise1Phase += frequency.getValue();
        if (noise1Phase > Math.PI * 2) noise1Phase -= Math.PI * 2;
        return baseValue + noise;
    }


    private float applyAITraining(float targetRotation, float currentRotation, float accuracy) {
        float difference = targetRotation - currentRotation;
        float learnedAdjustment = difference * accuracy;


        float learningFactor = 1.0f - learningRate.getValue();
        float result = currentRotation + learnedAdjustment * learningFactor;

        return result;
    }


    private float applyBehavioralPatterns(float rotation, boolean isActive) {
        if (!behavioralPatterns.getValue() || !isActive) return rotation;


        float patternOffset = (float) (Math.sin(System.currentTimeMillis() / 1000.0) * 0.5 * patternComplexity.getValue());
        float randomOffset = (random.nextFloat() - 0.5f) * 0.3f * patternComplexity.getValue();

        return rotation + patternOffset + randomOffset;
    }


    private float getDynamicNoiseIntensity(FloatValue baseIntensity) {
        if (!dynamicNoise.getValue()) {
            return baseIntensity.getValue();
        }


        EntityPlayer nearestTarget = getNearestTarget();
        if (nearestTarget != null) {
            float distance = (float) RotationUtil.distanceToEntity(nearestTarget);
            float distanceFactor = 1.0f - (distance / range.getValue());
            return baseIntensity.getValue() * (0.5f + 0.5f * distanceFactor);
        }
        return baseIntensity.getValue();
    }

    private EntityPlayer getNearestTarget() {
        List<EntityPlayer> targets = mc.theWorld.loadedEntityList
                .stream()
                .filter(entity -> entity instanceof EntityPlayer)
                .map(entity -> (EntityPlayer) entity)
                .filter(this::isValidTarget)
                .sorted(Comparator.comparingDouble(RotationUtil::distanceToEntity))
                .collect(Collectors.toList());

        return targets.isEmpty() ? null : targets.get(0);
    }

    public AimAssistB() {
        super("AimAssistB", false);
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


                    if (adaptiveTiming.getValue()) {
                        long currentTime = System.currentTimeMillis();
                        long minDelay = (long)(safetyJitter.getValue() * (1.0 - timingVariation.getValue()));
                        long maxDelay = (long)(safetyJitter.getValue() * (1.0 + timingVariation.getValue()));
                        long actualDelay = ThreadLocalRandom.current().nextLong(minDelay, maxDelay + 1);

                        if (currentTime - lastRotationTime < actualDelay) {
                            return;
                        }
                        lastRotationTime = currentTime;
                    } else if (!this.timer.hasTimeElapsed(350L)) {
                        return;
                    }

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


                            if (advancedSafety.getValue()) {
                                safetyCounter++;
                                if (safetyCounter % safetyJitter.getValue() == 0) {

                                    float safetyNoise = (random.nextFloat() - 0.5f) * safetyThreshold.getValue() * 0.1f;
                                    NightSky.rotationManager.setRotation(
                                            mc.thePlayer.rotationYaw + safetyNoise,
                                            mc.thePlayer.rotationPitch + safetyNoise,
                                            0,
                                            false
                                    );
                                    return;
                                }
                            }

                            if (rotationMode.getModeString().equals("OP-Rotation") || rotationMode.getModeString().equals("AI-Enhanced")) {
                                updateOPRotationSettings();
                                opRotationSystem.conduct(player);

                                if (enableNoise.getValue()) {
                                    applyNoiseToRotation();
                                }

                                if (aiTraining.getValue()) {
                                    applyAITrainingRotation(player);
                                }

                                if (debugTurnSpeed.getValue()) {
                                    System.out.println("AimAssistB 转头速度: " + opRotationSystem.getTurnSpeedPublic());
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


                                float targetYaw = mc.thePlayer.rotationYaw + (rotation[0] - mc.thePlayer.rotationYaw) * 0.1F * yaw;
                                float targetPitch = mc.thePlayer.rotationPitch + (rotation[1] - mc.thePlayer.rotationPitch) * 0.1F * pitch;


                                if (enableNoise.getValue()) {
                                    targetYaw += (float) (Math.sin(noise1Phase) * getDynamicNoiseIntensity(noise1Intensity));
                                    targetPitch += (float) (Math.cos(noise2Phase) * getDynamicNoiseIntensity(noise2Intensity));

                                    noise1Phase += noise1Frequency.getValue();
                                    noise2Phase += noise2Frequency.getValue();
                                    noise3Phase += noise3Frequency.getValue();

                                    if (noise1Phase > Math.PI * 2) noise1Phase -= Math.PI * 2;
                                    if (noise2Phase > Math.PI * 2) noise2Phase -= Math.PI * 2;
                                    if (noise3Phase > Math.PI * 2) noise3Phase -= Math.PI * 2;
                                }


                                if (aiTraining.getValue()) {
                                    targetYaw = applyAITraining(targetYaw, mc.thePlayer.rotationYaw, trainingAccuracy.getValue());
                                    targetPitch = applyAITraining(targetPitch, mc.thePlayer.rotationPitch, trainingAccuracy.getValue());
                                    targetYaw = applyBehavioralPatterns(targetYaw, true);
                                    targetPitch = applyBehavioralPatterns(targetPitch, true);
                                }

                                NightSky.rotationManager.setRotation(targetYaw, targetPitch, 0, false);
                            }
                        }
                    }
                }
            }
        }
    }

    private void applyNoiseToRotation() {
        float currentYaw = mc.thePlayer.rotationYaw;
        float currentPitch = mc.thePlayer.rotationPitch;

        float noisyYaw = currentYaw + (float) (Math.sin(noise1Phase) * getDynamicNoiseIntensity(noise1Intensity));
        float noisyPitch = currentPitch + (float) (Math.cos(noise2Phase) * getDynamicNoiseIntensity(noise2Intensity));

        NightSky.rotationManager.setRotation(noisyYaw, noisyPitch, 0, false);
    }

    private void applyAITrainingRotation(EntityPlayer target) {
        float currentYaw = mc.thePlayer.rotationYaw;
        float currentPitch = mc.thePlayer.rotationPitch;


        float[] targetRots = getRotationsToEntity(target);

        float trainedYaw = applyAITraining(targetRots[0], currentYaw, trainingAccuracy.getValue());
        float trainedPitch = applyAITraining(targetRots[1], currentPitch, trainingAccuracy.getValue());

        NightSky.rotationManager.setRotation(trainedYaw, trainedPitch, 0, false);
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