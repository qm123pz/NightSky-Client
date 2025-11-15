package nightsky.util.render.animations.advanced.impl;

import nightsky.util.render.animations.advanced.Animation;
import nightsky.util.render.animations.advanced.Direction;

public class EaseBackIn extends Animation {
    
    private final float easeAmount;

    public EaseBackIn(int ms, double endPoint, float easeAmount) {
        super(ms, endPoint);
        this.easeAmount = easeAmount;
    }

    public EaseBackIn(int ms, double endPoint, float easeAmount, Direction direction) {
        super(ms, endPoint, direction);
        this.easeAmount = easeAmount;
    }

    protected double getEquation(double x) {
        double shrink = easeAmount + 1;
        return Math.pow(x, 2) * ((shrink + 1) * x - shrink);
    }
}
