package nightsky.module.modules.combat.rotation.algorithm;

import nightsky.module.modules.combat.rotation.TurnSpeed;
import nightsky.module.modules.combat.rotation.RotationUtils;

public class SmoothLinearAlgorithm implements TurnSpeedAlgorithm {
    private final float minTurnSpeedYaw;
    private final float maxTurnSpeedYaw;
    private final float minTurnSpeedPitch;
    private final float maxTurnSpeedPitch;
    
    private SmoothRandom smoothRandomYaw = new SmoothRandom();
    private SmoothRandom smoothRandomPitch = new SmoothRandom();

    public SmoothLinearAlgorithm(float minTurnSpeedYaw, float maxTurnSpeedYaw, 
                                float minTurnSpeedPitch, float maxTurnSpeedPitch) {
        this.minTurnSpeedYaw = minTurnSpeedYaw;
        this.maxTurnSpeedYaw = maxTurnSpeedYaw;
        this.minTurnSpeedPitch = minTurnSpeedPitch;
        this.maxTurnSpeedPitch = maxTurnSpeedPitch;
    }

    @Override
    public TurnSpeed calculateTurnSpeed(float diffYaw, float diffPitch, float maxDiffYaw, float maxDiffPitch) {
        int yawTick = RotationUtils.randomInt(0, 10);
        int pitchTick = RotationUtils.randomInt(0, 10);
        
        float yawSpeed = smoothRandomYaw.randomFrom(minTurnSpeedYaw, maxTurnSpeedYaw, yawTick);
        float pitchSpeed = smoothRandomPitch.randomFrom(minTurnSpeedPitch, maxTurnSpeedPitch, pitchTick);
        
        return new TurnSpeed(yawSpeed, pitchSpeed);
    }

    private static class SmoothRandom {
        private float lastValue = 0f;
        private int tickCounter = 0;

        public float randomFrom(float min, float max, int tick) {
            if (tickCounter <= 0 || tick != tickCounter) {
                lastValue = RotationUtils.randomFloat(min, max);
                tickCounter = tick;
            }
            return lastValue;
        }
    }
}
