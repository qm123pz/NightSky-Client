package nightsky.viaforge.fix;

import com.viaversion.viabackwards.protocol.v1_17_1to1_17.Protocol1_17_1To1_17;
import com.viaversion.viabackwards.protocol.v1_17_1to1_17.storage.InventoryStateIds;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ServerboundPackets1_17;

public class FixProtocol1_17_1To1_17 {
    public static void fix(Protocol1_17_1To1_17 protocol) {
        if (protocol == null) {
            return;
        }

        protocol.registerServerbound(
                ServerboundPackets1_17.CONTAINER_CLICK,
                ServerboundPackets1_17.CONTAINER_CLICK,
                wrapper -> {
                    byte containerId = wrapper.passthrough(Types.BYTE);
                    int stateId = Integer.MAX_VALUE;
                    InventoryStateIds state = wrapper.user().get(InventoryStateIds.class);
                    if (state != null) {
                        stateId = state.removeStateId(containerId);
                        state.setStateId(containerId, stateId);
                    }
                    wrapper.write(Types.VAR_INT, stateId == Integer.MAX_VALUE ? 0 : stateId);
                },
                true
        );
    }
}
