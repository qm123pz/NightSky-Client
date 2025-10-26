package nightsky.module.modules.combat.rotation;

import nightsky.module.modules.combat.rotation.RotationUtils;
import net.minecraft.client.Minecraft;

public class FrictionSimulator {
    private static final Minecraft mc = Minecraft.getMinecraft();
    
    private Friction friction = new Friction(1f, 1f);
    private Rotation lastAimRotation = new Rotation(0f, 0f);
    private Rotation currAimRotation = new Rotation(0f, 0f);
    private long yawTickTimer = 0;
    private long pitchTickTimer = 0;
    private int yawTick = 1;
    private int pitchTick = 1;
    private boolean startYawStatus = false;
    private boolean startPitchStatus = false;
    private String frictionAlgorithm = "Time-Incremental";

    public void setFrictionAlgorithm(String algorithm) {
        this.frictionAlgorithm = algorithm;
    }

    public void conduct(Rotation aimRotation) {
        lastAimRotation = currAimRotation;
        currAimRotation = aimRotation;

        friction.yaw = RotationUtils.clamp(calculateFriction(updateYawTick()), 0f, 1f);
        friction.pitch = RotationUtils.clamp(calculateFriction(updatePitchTick()), 0f, 1f);
    }

    private int updateYawTick() {
        float diffYaw = RotationUtils.getAngleDiff(currAimRotation.yaw, lastAimRotation.yaw);
        if (diffYaw != 0f) startYawStatus = true;

        if (startYawStatus) {
            if (hasTickPassed(yawTickTimer)) {
                yawTick++;
                yawTickTimer = System.currentTimeMillis();
            }

            if (yawTick >= 3 && diffYaw == 0f) {
                yawTick = 1;
                yawTickTimer = System.currentTimeMillis();
                startYawStatus = false;
            }
        }

        if (!startYawStatus) {
            yawTick = 1;
            yawTickTimer = System.currentTimeMillis();
            return 3;
        }

        return yawTick;
    }

    private int updatePitchTick() {
        float diffPitch = RotationUtils.getAngleDiff(currAimRotation.pitch, lastAimRotation.pitch);
        if (diffPitch != 0f) startPitchStatus = true;

        if (startPitchStatus) {
            if (hasTickPassed(pitchTickTimer)) {
                pitchTick++;
                pitchTickTimer = System.currentTimeMillis();
            }

            if (pitchTick >= 3 && diffPitch == 0f) {
                pitchTick = 1;
                pitchTickTimer = System.currentTimeMillis();
                startPitchStatus = false;
            }
        }

        if (!startPitchStatus) {
            pitchTick = 1;
            pitchTickTimer = System.currentTimeMillis();
            return 3;
        }

        return pitchTick;
    }

    private boolean hasTickPassed(long timer) {
        return System.currentTimeMillis() - timer >= 50;
    }

    private float calculateFriction(int tick) {
        if (tick >= 3) {
            return 1f;
        }

        switch (frictionAlgorithm) {
            case "Time-Incremental": {
                double k = -RotationUtils.randomFloat(30.0f, 80.0f) + RotationUtils.randomFloat(-10.0f, 10.0f);
                double exp = Math.pow(tick / 3.0, 10);
                return RotationUtils.clamp((float)(1.0 - Math.pow(Math.E, exp * k)), 0.0001f, 1f);
            }

            case "CustomCurve": {
                double k = RotationUtils.randomInt(300, 500) + RotationUtils.randomInt(-10, 10);
                double exp = k * 0.08;
                return RotationUtils.clamp(
                    (float)Math.pow(Math.abs(Math.pow(1.0 - (tick / 3.0), 4) - 1), exp), 
                    0.0001f, 1f
                );
            }

            case "TPAC": {
                double exp = RotationUtils.randomFloat(3.0f, 7.5f) + RotationUtils.randomFloat(-0.3f, 0.5f);
                return (float)pointedCurve(tick, 0.0, 0.0, 3.0, 1.0, exp);
            }
        }

        return 1f;
    }

    private double pointedCurve(double x, double x1, double y1, double x2, double y2, double exp) {
        double t = (x - x1) / (x2 - x1);
        t = Math.pow(t, exp);
        return y1 + t * (y2 - y1);
    }

    public void reset() {
        friction = new Friction(1f, 1f);
        lastAimRotation = RotationUtils.getCurrentRotation();
        currAimRotation = RotationUtils.getCurrentRotation();
        yawTickTimer = System.currentTimeMillis();
        pitchTickTimer = System.currentTimeMillis();
        yawTick = 1;
        pitchTick = 1;
        startYawStatus = false;
        startPitchStatus = false;
    }

    public Friction getFriction() {
        return friction;
    }

    public static class Friction {
        public float yaw;
        public float pitch;

        public Friction(float yaw, float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }
}
