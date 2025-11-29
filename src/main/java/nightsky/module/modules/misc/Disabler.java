package nightsky.module.modules.misc;

import nightsky.NightSky;
import nightsky.event.EventTarget;
import nightsky.events.PacketEvent;
import nightsky.event.types.EventType;
import nightsky.module.Module;
import nightsky.util.ChatUtil;
import nightsky.util.PacketUtil;
import nightsky.value.values.ModeValue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;

import java.util.ArrayList;
import java.util.List;

public class Disabler extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final ModeValue mode = new ModeValue("Mode", 0, new String[]{"PredictionInventory"});
    private final List<Packet> packetsQueue = new ArrayList<>();

    public Disabler() {
        super("Disabler", false);
    }

    @Override
    public void onEnabled() {
        if (this.mode.getModeString().equals("PredictionInventory")) {
            ChatUtil.sendFormatted(String.format("%s%s: You can use Vanilla-InvWalk and Silent-InvManager now", NightSky.clientName, this.getName()));
        }
        packetsQueue.clear();
    }

    @Override
    public void onDisabled() {
        if (!packetsQueue.isEmpty()) {
            for (Packet packet : packetsQueue) {
                PacketUtil.sendPacketNoEvent(packet);
            }
            packetsQueue.clear();
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if(!this.isEnabled()) return;

        if (this.mode.getModeString().equals("PredictionInventory") && event.getType() == EventType.SEND) {
            Packet packet = event.getPacket();
            
            if (packet instanceof C16PacketClientStatus) {
                event.setCancelled(true);
                packetsQueue.add(packet);
            } else if (packet instanceof C0EPacketClickWindow) {
                packetsQueue.add(packet);
            } else if (packet instanceof C0DPacketCloseWindow) {
                if (!packetsQueue.isEmpty()) {
                    for (Packet queuedPacket : packetsQueue) {
                        PacketUtil.sendPacketNoEvent(queuedPacket);
                    }
                    packetsQueue.clear();
                }
            }
        }
    }
}
