package nightsky.events;

import nightsky.event.events.Cancellable;
import nightsky.event.events.Event;
import net.minecraft.network.Packet;

public class SendPacketEvent implements Event, Cancellable {
    private Packet<?> packet;
    private boolean cancelled;

    public SendPacketEvent(Packet<?> packet) {
        this.packet = packet;
        this.cancelled = false;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
