package nightsky.events;

import nightsky.event.events.Event;
import nightsky.event.types.EventType;

public class TickEvent implements Event {
    private final EventType type;

    public TickEvent(EventType u) {
        this.type = u;
    }

    public EventType getType() {
        return this.type;
    }
}
