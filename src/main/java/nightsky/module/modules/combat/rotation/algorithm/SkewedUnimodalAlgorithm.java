package nightsky.module.modules.combat.rotation.algorithm;

import nightsky.module.modules.combat.rotation.TurnSpeed;
import nightsky.module.modules.combat.rotation.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public class SkewedUnimodalAlgorithm implements TurnSpeedAlgorithm {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private SmoothRandom smoothRandom = new SmoothRandom();
    private Entity target;

    public void setTarget(Entity target) {
        this.target = target;
    }

    @Override
    public TurnSpeed calculateTurnSpeed(float diffYaw, float diffPitch, float maxDiffYaw, float maxDiffPitch) {
        float yawSpeed = shouldTrack(diffYaw) ? 
            calculateFollowingSpeed(maxDiffYaw) : calculateInitialSpeed(maxDiffYaw);
        float pitchSpeed = shouldTrack(diffYaw) ? 
            calculateFollowingSpeed(maxDiffYaw) : calculateInitialSpeed(maxDiffYaw);
        
        return new TurnSpeed(yawSpeed, pitchSpeed);
    }

    private float calculateInitialSpeed(float maxDiff) {
        float g1 = 0.48f * maxDiff;
        float g2 = RotationUtils.abs(0.0007f * RotationUtils.pow(maxDiff, 2) * RotationUtils.sin(20f * maxDiff));
        float g3 = 0.0012f * RotationUtils.pow(RotationUtils.negTo0(maxDiff - 90f), 2.1f);
        float basicSpeed = g1 + g2 + g3;
        
        return RotationUtils.clamp(basicSpeed, 0f, 180f);
    }

    private float calculateFollowingSpeed(float maxDiff) {
        float dist = target != null ? (float) RotationUtils.getDistanceToEntity(target) : 4.0f;

        float floorBase = RotationUtils.clamp(
            RotationUtils.randomFloat(0f, 1.2f) * (30f / (2f * dist + 8f)), 
            0f, 180f
        );
        float ceilingBase = RotationUtils.clamp(
            RotationUtils.randomFloat(-4.25f, 8.5f) * (80f / (0.5f * dist + 3.5f)), 
            floorBase, 180f
        );

        int tick = RotationUtils.randomInt(-1, 3);
        float basicSpeed = smoothRandom.randomFrom(floorBase, ceilingBase, tick);
        
        basicSpeed += maxDiff / 4f;
        
        return RotationUtils.clamp(basicSpeed, 0f, 180f);
    }

    private boolean shouldTrack(float diff) {
        return diff < 45f;
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
