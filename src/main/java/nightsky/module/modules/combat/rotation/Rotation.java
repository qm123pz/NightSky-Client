package nightsky.module.modules.combat.rotation;

public class Rotation {
    public float yaw;
    public float pitch;

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Rotation plus(Rotation rotation) {
        return new Rotation(yaw + rotation.yaw, pitch + rotation.pitch);
    }

    public Rotation plus(float num) {
        return new Rotation(yaw + num, pitch + num);
    }

    public Rotation minus(Rotation rotation) {
        return new Rotation(yaw - rotation.yaw, pitch - rotation.pitch);
    }

    public Rotation minus(float num) {
        return new Rotation(yaw - num, pitch - num);
    }

    public Rotation times(float num) {
        return new Rotation(yaw * num, pitch * num);
    }

    public Rotation div(float num) {
        return new Rotation(yaw / num, pitch / num);
    }

    public float getHypot() {
        return (float) Math.hypot(yaw, pitch);
    }

    public Rotation getCorrectRotation() {
        return new Rotation(wrapAngleTo180(yaw), clamp(pitch, -90f, 90f));
    }

    public float getDifference(Rotation toRotation) {
        Rotation correctedSource = this.getCorrectRotation();
        Rotation correctedDestination = toRotation.getCorrectRotation();
        return correctedDestination.minus(correctedSource).getHypot();
    }

    private float wrapAngleTo180(float angle) {
        angle %= 360.0f;
        if (angle >= 180.0f) {
            angle -= 360.0f;
        }
        if (angle < -180.0f) {
            angle += 360.0f;
        }
        return angle;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public String toString() {
        return "Rotation(" + yaw + ", " + pitch + ")";
    }
}
