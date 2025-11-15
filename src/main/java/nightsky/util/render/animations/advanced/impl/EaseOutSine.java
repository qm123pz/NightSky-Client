package nightsky.util.render.animations.advanced.impl;

import nightsky.util.render.animations.advanced.Animation;
import nightsky.util.render.animations.advanced.Direction;

public class EaseOutSine extends Animation {

    public EaseOutSine(int ms, double endPoint) {
        super(ms, endPoint);
    }

    public EaseOutSine(int ms, double endPoint, Direction direction) {
        super(ms, endPoint, direction);
    }

    protected double getEquation(double x) {
        return Math.sin(x * (Math.PI / 2));
    }
}
