package nightsky.events;

import nightsky.event.events.Event;

public class Shader2DEvent implements Event {
    private final ShaderType shaderType;
    
    public enum ShaderType {
        GLOW,
        OUTLINE,
        BLUR,
        SHADOW
    }
    
    public Shader2DEvent(ShaderType shaderType) {
        this.shaderType = shaderType;
    }
    
    public ShaderType getShaderType() {
        return shaderType;
    }
}
