package nightsky.viaforge.fix;

import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viarewind.protocol.v1_9to1_8.storage.BossBarStorage;
import com.viaversion.viarewind.protocol.v1_9to1_8.storage.PlayerPositionTracker;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FixProtocol1_9To1_8 {
    private static final Queue<PacketWrapper> confirmations = new ConcurrentLinkedQueue<>();

    public static void fix(Protocol1_9To1_8 protocol) {
        if (protocol == null) {
            return;
        }

        protocol.registerClientbound(
                ClientboundPackets1_9.PLAYER_POSITION,
                ClientboundPackets1_8.PLAYER_POSITION,
                new PacketHandlers() {
                    @Override
                    public void register() {
                        map(Types.DOUBLE);
                        map(Types.DOUBLE);
                        map(Types.DOUBLE);
                        map(Types.FLOAT);
                        map(Types.FLOAT);
                        map(Types.BYTE);
                        handler(wrapper -> {
                            int id = wrapper.read(Types.VAR_INT);
                            PacketWrapper c = PacketWrapper.create(ServerboundPackets1_9.ACCEPT_TELEPORTATION, wrapper.user());
                            c.write(Types.VAR_INT, id);
                            confirmations.offer(c);

                            PlayerPositionTracker tracker = wrapper.user().get(PlayerPositionTracker.class);
                            if (tracker == null) {
                                return;
                            }

                            tracker.setConfirmId(id);

                            byte flags = wrapper.get(Types.BYTE, 0);
                            double x = wrapper.get(Types.DOUBLE, 0);
                            double y = wrapper.get(Types.DOUBLE, 1);
                            double z = wrapper.get(Types.DOUBLE, 2);
                            float yaw = wrapper.get(Types.FLOAT, 0);
                            float pitch = wrapper.get(Types.FLOAT, 1);

                            wrapper.set(Types.BYTE, 0, (byte) 0);

                            if (flags != 0) {
                                if ((flags & 0x01) != 0) {
                                    x += tracker.getPosX();
                                    wrapper.set(Types.DOUBLE, 0, x);
                                }
                                if ((flags & 0x02) != 0) {
                                    y += tracker.getPosY();
                                    wrapper.set(Types.DOUBLE, 1, y);
                                }
                                if ((flags & 0x04) != 0) {
                                    z += tracker.getPosZ();
                                    wrapper.set(Types.DOUBLE, 2, z);
                                }
                                if ((flags & 0x08) != 0) {
                                    yaw += tracker.getYaw();
                                    wrapper.set(Types.FLOAT, 0, yaw);
                                }
                                if ((flags & 0x10) != 0) {
                                    pitch += tracker.getPitch();
                                    wrapper.set(Types.FLOAT, 1, pitch);
                                }
                            }

                            tracker.setPos(x, y, z);
                            tracker.setYaw(yaw);
                            tracker.setPitch(pitch);
                        });
                    }
                },
                true
        );

        protocol.registerServerbound(
                ServerboundPackets1_8.MOVE_PLAYER_POS_ROT,
                ServerboundPackets1_9.MOVE_PLAYER_POS_ROT,
                wrapper -> {
                    PacketWrapper c = confirmations.poll();
                    if (c != null) {
                        c.sendToServer(Protocol1_9To1_8.class);
                    }

                    PlayerPositionTracker tracker = wrapper.user().get(PlayerPositionTracker.class);
                    BossBarStorage storage = wrapper.user().get(BossBarStorage.class);
                    if (tracker == null || storage == null) {
                        return;
                    }

                    tracker.sendAnimations();

                    double x = wrapper.passthrough(Types.DOUBLE);
                    double y = wrapper.passthrough(Types.DOUBLE);
                    double z = wrapper.passthrough(Types.DOUBLE);
                    float yaw = wrapper.passthrough(Types.FLOAT);
                    float pitch = wrapper.passthrough(Types.FLOAT);
                    boolean onGround = wrapper.passthrough(Types.BOOLEAN);
                    if (tracker.getConfirmId() != -1) {
                        if (
                                tracker.getPosX() == x &&
                                        tracker.getPosY() == y &&
                                        tracker.getPosZ() == z &&
                                        tracker.getYaw() == yaw &&
                                        tracker.getPitch() == pitch
                        ) {
                            tracker.setConfirmId(-1);
                        }
                    } else {
                        tracker.setPos(x, y, z);
                        tracker.setYaw(yaw);
                        tracker.setPitch(pitch);
                        tracker.setOnGround(onGround);
                        storage.updateLocation();
                    }
                },
                true
        );
    }
}
