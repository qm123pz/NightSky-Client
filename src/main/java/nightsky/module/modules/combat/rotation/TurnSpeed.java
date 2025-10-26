package nightsky.module.modules.combat.rotation;

public class TurnSpeed {
    public float yaw;
    public float pitch;

    public TurnSpeed(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public TurnSpeed plus(TurnSpeed turnSpeed) {
        return new TurnSpeed(yaw + turnSpeed.yaw, pitch + turnSpeed.pitch);
    }

    public TurnSpeed plus(float num) {
        return new TurnSpeed(yaw + num, pitch + num);
    }

    public TurnSpeed minus(TurnSpeed turnSpeed) {
        return new TurnSpeed(yaw - turnSpeed.yaw, pitch - turnSpeed.pitch);
    }

    public TurnSpeed minus(float num) {
        return new TurnSpeed(yaw - num, pitch - num);
    }

    public TurnSpeed times(float num) {
        return new TurnSpeed(yaw * num, pitch * num);
    }

    public TurnSpeed div(float num) {
        return new TurnSpeed(yaw / num, pitch / num);
    }

    public float getHypot() {
        return (float) Math.hypot(yaw, pitch);
    }

    @Override
    public String toString() {
        return "TurnSpeed(" + yaw + ", " + pitch + ")";
    }
}
