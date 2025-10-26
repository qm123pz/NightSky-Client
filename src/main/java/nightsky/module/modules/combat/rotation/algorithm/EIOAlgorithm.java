package nightsky.module.modules.combat.rotation.algorithm;

import nightsky.module.modules.combat.rotation.TurnSpeed;
import nightsky.module.modules.combat.rotation.RotationUtils;

public class EIOAlgorithm implements TurnSpeedAlgorithm {

    @Override
    public TurnSpeed calculateTurnSpeed(float diffYaw, float diffPitch, float maxDiffYaw, float maxDiffPitch) {
        float yawSpeed = calculateSpeed(diffYaw, maxDiffYaw);
        float pitchSpeed = calculateSpeed(diffPitch, maxDiffPitch);
        
        return new TurnSpeed(yawSpeed, pitchSpeed);
    }

    private float calculateSpeed(float diff, float maxDiff) {
        float speed = 9.59746e-19f * RotationUtils.pow(diff, 10) +
                     -1.22653e-15f * RotationUtils.pow(diff, 9) +
                     6.28782e-13f * RotationUtils.pow(diff, 8) +
                     -1.71458e-10f * RotationUtils.pow(diff, 7) +
                     2.72758e-8f * RotationUtils.pow(diff, 6) +
                     -2.575768e-6f * RotationUtils.pow(diff, 5) +
                     1.388576e-4f * RotationUtils.pow(diff, 4) +
                     -3.853797e-3f * RotationUtils.pow(diff, 3) +
                     0.04978519f * RotationUtils.pow(diff, 2) +
                     0.1323985f * diff;
        
        speed = RotationUtils.clamp(speed, 8f, 180f);

        float factor;
        if (maxDiff == 0f) {
            factor = 1f;
        } else {
            float pi = (float) Math.PI;
            if (diff < maxDiff / 2f) {
                factor = (2.07f * (1 - RotationUtils.cos((2f * pi * diff) / maxDiff))) + 0.1f;
            } else {
                factor = (5.8f * (1 - RotationUtils.cos((2f * pi * diff) / maxDiff))) + 0.1f;
            }
            factor = RotationUtils.clamp(factor, 0f, 1f);
        }

        return speed * factor;
    }
}
