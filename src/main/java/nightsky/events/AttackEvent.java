package nightsky.events;

import net.minecraft.entity.Entity;
import nightsky.event.events.Event;

public class AttackEvent implements Event {
    private final Entity targetEntity;
    
    public AttackEvent(Entity targetEntity) {
        this.targetEntity = targetEntity;
    }

    public Entity getTargetEntity() {
        return targetEntity;
    }
}
