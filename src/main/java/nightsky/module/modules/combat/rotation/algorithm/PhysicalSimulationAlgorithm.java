package nightsky.module.modules.combat.rotation.algorithm;

import nightsky.module.modules.combat.rotation.TurnSpeed;
import nightsky.module.modules.combat.rotation.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public class PhysicalSimulationAlgorithm implements TurnSpeedAlgorithm {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private SmoothRandom smoothRandom = new SmoothRandom();
    private Entity target;

    public void setTarget(Entity target) {
        this.target = target;
    }

    @Override
    public TurnSpeed calculateTurnSpeed(float diffYaw, float diffPitch, float maxDiffYaw, float maxDiffPitch) {
        float yawSpeed = calculateTotalSpeed(diffYaw, maxDiffYaw);
        float pitchSpeed = calculateTotalSpeed(diffYaw, maxDiffYaw);
        
        return new TurnSpeed(yawSpeed, pitchSpeed);
    }

    private float calculateTotalSpeed(float diff, float maxDiff) {
        float dist = target != null ? (float) RotationUtils.getDistanceToEntity(target) : 4.0f;

        float g1 = 0.48f * maxDiff;
        float g2 = RotationUtils.abs(0.0007f * RotationUtils.pow(maxDiff, 2) * RotationUtils.sin(20f * maxDiff));
        float g3 = 0.0012f * RotationUtils.pow(RotationUtils.negTo0(maxDiff - 90f), 2.1f);
        float initialSpeed = RotationUtils.clamp(g1 + g2 + g3, 0f, 180f);

        float floorBase = RotationUtils.clamp(
            RotationUtils.randomFloat(0f, 1.2f) * (30f / (2f * dist + 8f)), 
            0f, 180f
        );
        float ceilingBase = RotationUtils.clamp(
            RotationUtils.randomFloat(-4.25f, 8.5f) * (0.48f * 45f + 
            RotationUtils.abs(0.0007f * RotationUtils.pow(45f, 2) * RotationUtils.sin(20f * 45f)) + 
            0.0012f * RotationUtils.pow(RotationUtils.negTo0(45f - 90f), 2.1f)), 
            floorBase, 180f
        );

        int tick = RotationUtils.randomInt(-1, 2);
        float followingSpeed = RotationUtils.clamp(
            smoothRandom.randomFrom(floorBase, ceilingBase, tick) + (maxDiff / 4f), 
            0f, 180f
        );

        return diff < 45f ? followingSpeed : initialSpeed;
    }

    private static class SmoothRandom {
        private float lastValue = 0f;
        private int lastTick = -1;

        public float randomFrom(float min, float max, int tick) {
            if (tick != lastTick) {
                lastValue = RotationUtils.randomFloat(min, max);
                lastTick = tick;
            }
            return lastValue;
        }
    }
}
