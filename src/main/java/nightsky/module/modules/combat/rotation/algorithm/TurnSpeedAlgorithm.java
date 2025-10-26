package nightsky.module.modules.combat.rotation.algorithm;

import nightsky.module.modules.combat.rotation.TurnSpeed;

public interface TurnSpeedAlgorithm {
    TurnSpeed calculateTurnSpeed(float diffYaw, float diffPitch, float maxDiffYaw, float maxDiffPitch);
}
