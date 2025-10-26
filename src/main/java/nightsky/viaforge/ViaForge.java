package nightsky.viaforge;

import com.viaversion.viabackwards.protocol.v1_17_1to1_17.Protocol1_17_1To1_17;
import com.viaversion.viabackwards.protocol.v1_17to1_16_4.Protocol1_17To1_16_4;
import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.ProtocolManager;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9;
import net.minecraft.item.ItemSword;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import nightsky.viaforge.fix.FixProtocol1_17To1_16_4;
import nightsky.viaforge.fix.FixProtocol1_17_1To1_17;
import nightsky.viaforge.fix.FixProtocol1_9To1_8;
import nightsky.mixin.AccessorC02PacketUseEntity;
import nightsky.mixin.AccessorC17PacketCustomPayload;

import java.io.File;

public class ViaForge {
  private static final Minecraft mc = Minecraft.getMinecraft();

  private static boolean swing = false;

  public static void init() {
    ViaLoadingBase.ViaLoadingBaseBuilder.create()
            .runDirectory(new File(Loader.instance().getConfigDir(), "ViaForge"))
            .nativeVersion(ProtocolVersion.v1_8.getVersion())
            .forceNativeVersionCondition(mc::isSingleplayer)
            .onProtocolReload(targetVersion -> {
              Block blockLilyPad = Block.getBlockById(111 /* waterlily */);

              if (!targetVersion.newerThanOrEqualTo(ProtocolVersion.v1_9)) {
                blockLilyPad.setBlockBounds(
                        0.0F, 0.0F, 0.0F,
                        1.0F, 0.015625F, 1.0F
                );
                return;
              }

              blockLilyPad.setBlockBounds(
                      0.0625F, 0.0F, 0.0625F,
                      0.9375F, 0.09375F, 0.9375F
              );
            })
            .build();
    ProtocolManager protocolManager = Via.getManager().getProtocolManager();
    FixProtocol1_9To1_8.fix(protocolManager.getProtocol(Protocol1_9To1_8.class));
    FixProtocol1_17To1_16_4.fix(protocolManager.getProtocol(Protocol1_17To1_16_4.class));
    FixProtocol1_17_1To1_17.fix(protocolManager.getProtocol(Protocol1_17_1To1_17.class));
  }

  public static boolean handle(Packet<?> packet) {
    ProtocolVersion targetVersion = ViaLoadingBase.getInstance().getTargetVersion();
    if (!targetVersion.newerThanOrEqualTo(ProtocolVersion.v1_9)) {
      return false;
    }

    if (packet instanceof FMLProxyPacket) {
      return true;
    }

    if (packet instanceof C17PacketCustomPayload) {
      C17PacketCustomPayload c17 = (C17PacketCustomPayload) packet;
      if (!c17.getChannelName().startsWith("MC|")) {
        return true;
      }

      if (c17.getChannelName().equals("MC|Brand")) {
        ((AccessorC17PacketCustomPayload) c17).setData(
                new PacketBuffer(Unpooled.buffer()).writeString("vanilla")
        );
      }
    }

    if (packet instanceof C02PacketUseEntity) {
      C02PacketUseEntity c02 = (C02PacketUseEntity) packet;
      switch (((C02PacketUseEntity) packet).getAction()) {
        case ATTACK: {
          return false;
        }
        case INTERACT_AT: {
          Vec3 hitVec = c02.getHitVec();
          Entity entity = c02.getEntityFromWorld(mc.theWorld);

          if (hitVec == null || entity == null) {
            break;
          }

          if (
                  entity instanceof EntityItemFrame ||
                          entity instanceof EntityFireball
          ) {
            break;
          }

          float w = entity.width;
          float h = entity.height;
          ((AccessorC02PacketUseEntity) packet).setHitVec(new Vec3(
                  Math.max(-(w / 2.0D), Math.min(w / 2.0D, hitVec.xCoord)),
                  Math.max(0.0D, Math.min(h, hitVec.yCoord)),
                  Math.max(-(w / 2.0D), Math.min(w / 2.0D, hitVec.zCoord))
          ));
          break;
        }
      }
    }

    if (packet instanceof C08PacketPlayerBlockPlacement) {
      C08PacketPlayerBlockPlacement c08 = (C08PacketPlayerBlockPlacement) packet;
      // Send shield packet for 1.9+ compatibility when blocking with sword
      if (c08.getStack() != null && c08.getStack().getItem() instanceof ItemSword) {
        try {
          UserConnection uc = Via.getManager().getConnectionManager().getConnections().iterator().next();
          // Always send USE_ITEM for sword blocking to ensure compatibility
          PacketWrapper useItem = PacketWrapper.create(ServerboundPackets1_9.USE_ITEM, uc);
          useItem.write(Types.VAR_INT, 1); // Main hand
          useItem.sendToServer(Protocol1_9To1_8.class);
        } catch (Exception e) {
          // Ignore if Via connection is not available
        }
      }
      return false;
    }

    if (packet instanceof C09PacketHeldItemChange) {
      return false;
    }

    if (swing) {
      UserConnection uc = Via.getManager().getConnectionManager().getConnections().iterator().next();
      PacketWrapper s = PacketWrapper.create(ServerboundPackets1_9.SWING, uc);
      s.write(Types.VAR_INT, 0);
      s.sendToServer(Protocol1_9To1_8.class);
      swing = false;
    }

    if (packet instanceof C0APacketAnimation) {
      swing = true;
      return true;
    }

    return false;
  }
}