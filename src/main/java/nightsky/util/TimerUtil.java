package nightsky.util;

public class TimerUtil {
    public long lastMS = System.currentTimeMillis();

    public void reset() {
        lastMS = System.currentTimeMillis();
    }

    public boolean hasTimeElapsed(long time, boolean reset) {
        if (System.currentTimeMillis() - lastMS > time) {
            if (reset) reset();
            return true;
        }
        return false;
    }

    public boolean hasTimeElapsed(long time) {
        return System.currentTimeMillis() - lastMS > time;
    }

    public boolean hasTimeElapsed(double time) {
        return hasTimeElapsed((long) time);
    }

    public boolean reached(long currentTime) {
        return Math.max(0L, System.currentTimeMillis() - this.lastMS) >= currentTime;
    }

    public boolean hasReached(long delay) {
        return System.currentTimeMillis() - lastMS >= delay;
    }

    public long getTime() {
        return System.currentTimeMillis() - lastMS;
    }

    public void setTime(long time) {
        lastMS = time;
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - this.lastMS;
    }

    public void setTime() {
        this.lastMS = 0L;
    }
}
