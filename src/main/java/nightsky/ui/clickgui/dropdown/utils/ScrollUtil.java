package nightsky.ui.clickgui.dropdown.utils;

import nightsky.util.render.animations.advanced.Animation;
import nightsky.util.render.animations.advanced.Direction;
import nightsky.util.render.animations.advanced.impl.DecelerateAnimation;

public class ScrollUtil {
    private double scroll = 0;
    private double maxScroll = 0;
    private double target = 0;
    private final Animation scrollAnimation = new DecelerateAnimation(250, 1);

    public void onScroll(int amount) {
        target += amount;
        target = Math.max(-maxScroll, Math.min(0, target));
        scrollAnimation.setDirection(Direction.FORWARDS);
        scrollAnimation.reset();
    }

    public double getScroll() {
        if (!scrollAnimation.isDone()) {
            scroll += (target - scroll) * scrollAnimation.getOutput();
        } else {
            scroll = target;
        }
        return scroll;
    }

    public void setMaxScroll(double maxScroll) {
        this.maxScroll = maxScroll;
        target = Math.max(-maxScroll, Math.min(0, target));
    }

    public double getMaxScroll() {
        return maxScroll;
    }
}
