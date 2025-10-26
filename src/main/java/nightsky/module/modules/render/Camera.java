package nightsky.module.modules.render;

import nightsky.module.Module;
import nightsky.value.values.FloatValue;
import nightsky.value.values.BooleanValue;
//hyw。 没错就是一个很ez的smooth。
public class Camera extends Module {
    public final FloatValue smoothness = new FloatValue("Smoothness", 0.05f, 0.01f, 0.1f);
    public final BooleanValue onlyThirdPerson = new BooleanValue("OnlyThirdPerson", true);
    
    private double smoothX = 0.0;
    private double smoothY = 0.0;
    private double smoothZ = 0.0;
    
    private boolean initialized = false;
    
    public Camera() {
        super("Camera", false);
    }
    
    public void updateSmoothedPosition(double targetX, double targetY, double targetZ) {
        if (!this.isEnabled()) {
            smoothX = targetX;
            smoothY = targetY;
            smoothZ = targetZ;
            initialized = false;
            return;
        }
        
        if (!initialized) {
            smoothX = targetX;
            smoothY = targetY;
            smoothZ = targetZ;
            initialized = true;
            return;
        }
        
        double factor = smoothness.getValue();
        
        smoothX += (targetX - smoothX) * factor;
        smoothY += (targetY - smoothY) * factor;
        smoothZ += (targetZ - smoothZ) * factor;
    }
    
    public double getSmoothedX() {
        return smoothX;
    }
    
    public double getSmoothedY() {
        return smoothY;
    }
    
    public double getSmoothedZ() {
        return smoothZ;
    }
    
    @Override
    public void onDisabled() {
        smoothX = 0.0;
        smoothY = 0.0;
        smoothZ = 0.0;
        initialized = false;
    }
}