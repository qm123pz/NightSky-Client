package nightsky.module.modules.combat.rotation.algorithm;

import nightsky.module.modules.combat.rotation.TurnSpeed;
import nightsky.module.modules.combat.rotation.RotationUtils;

public class LinearAlgorithm implements TurnSpeedAlgorithm {
    private final float minTurnSpeedYaw;
    private final float maxTurnSpeedYaw;
    private final float minTurnSpeedPitch;
    private final float maxTurnSpeedPitch;

    public LinearAlgorithm(float minTurnSpeedYaw, float maxTurnSpeedYaw, 
                          float minTurnSpeedPitch, float maxTurnSpeedPitch) {
        this.minTurnSpeedYaw = minTurnSpeedYaw;
        this.maxTurnSpeedYaw = maxTurnSpeedYaw;
        this.minTurnSpeedPitch = minTurnSpeedPitch;
        this.maxTurnSpeedPitch = maxTurnSpeedPitch;
    }

    @Override
    public TurnSpeed calculateTurnSpeed(float diffYaw, float diffPitch, float maxDiffYaw, float maxDiffPitch) {
        float yawSpeed = RotationUtils.randomFloat(minTurnSpeedYaw, maxTurnSpeedYaw);
        float pitchSpeed = RotationUtils.randomFloat(minTurnSpeedPitch, maxTurnSpeedPitch);
        
        return new TurnSpeed(yawSpeed, pitchSpeed);
    }
}
