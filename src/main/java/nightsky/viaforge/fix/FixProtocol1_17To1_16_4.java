package nightsky.viaforge.fix;

import com.viaversion.viabackwards.protocol.v1_17to1_16_4.Protocol1_17To1_16_4;
import com.viaversion.viabackwards.protocol.v1_17to1_16_4.storage.PlayerLastCursorItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ServerboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ClientboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ServerboundPackets1_17;

public class FixProtocol1_17To1_16_4 {
    public static void fix(Protocol1_17To1_16_4 protocol) {
        if (protocol == null) {
            return;
        }

        protocol.registerClientbound(
                ClientboundPackets1_17.PING,
                ClientboundPackets1_16_2.CONTAINER_ACK,
                wrapper -> {
                    wrapper.cancel();
                    int id = wrapper.read(Types.INT);
                    PacketWrapper t = wrapper.create(ClientboundPackets1_16_2.CONTAINER_ACK);
                    t.write(Types.INT, id);
                    t.send(Protocol1_17To1_16_4.class);
                },
                true
        );

        protocol.registerServerbound(
                ServerboundPackets1_16_2.CONTAINER_ACK,
                ServerboundPackets1_17.PONG,
                wrapper -> {
                    wrapper.cancel();
                    int id = wrapper.read(Types.INT);
                    PacketWrapper p = wrapper.create(ServerboundPackets1_17.PONG);
                    p.write(Types.INT, id);
                    p.sendToServer(Protocol1_17To1_16_4.class);
                },
                true
        );

        protocol.registerServerbound(
                ServerboundPackets1_16_2.CONTAINER_CLICK,
                ServerboundPackets1_17.CONTAINER_CLICK,
                new PacketHandlers() {
                    @Override
                    public void register() {
                        map(Types.BYTE);
                        handler(wrapper -> {
                            short slot = wrapper.passthrough(Types.SHORT);
                            byte button = wrapper.passthrough(Types.BYTE);
                            wrapper.read(Types.SHORT);
                            int mode = wrapper.passthrough(Types.VAR_INT);
                            Item clicked = protocol.getItemRewriter().handleItemToServer(
                                    wrapper.user(), wrapper.read(Types.ITEM1_13_2)
                            );

                            wrapper.write(Types.VAR_INT, 0);

                            PlayerLastCursorItem state = wrapper.user().get(PlayerLastCursorItem.class);
                            if (state == null) {
                                wrapper.write(Types.ITEM1_13_2, clicked);
                                return;
                            }

                            if (mode == 0 && button == 0 && clicked != null) {
                                state.setLastCursorItem(clicked);
                            } else if (mode == 0 && button == 1 && clicked != null) {
                                if (state.isSet()) {
                                    state.setLastCursorItem(clicked);
                                } else {
                                    state.setLastCursorItem(clicked, (clicked.amount() + 1) / 2);
                                }
                            } else if (!(mode == 5 && (slot == -999 && (button == 0 || button == 4) || (button == 1 || button == 5)))) {
                                state.setLastCursorItem(null);
                            }

                            Item carried = state.getLastCursorItem();
                            if (carried == null) {
                                wrapper.write(Types.ITEM1_13_2, clicked);
                            } else {
                                wrapper.write(Types.ITEM1_13_2, carried);
                            }
                        });
                    }
                },
                true
        );
    }
}
