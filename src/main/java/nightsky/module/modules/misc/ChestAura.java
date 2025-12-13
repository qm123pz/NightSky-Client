package nightsky.module.modules.misc;

import java.util.ArrayList;
import java.util.List;
import nightsky.event.EventTarget;
import nightsky.event.types.EventType;
import nightsky.events.LoadWorldEvent;
import nightsky.events.Render3DEvent;
import nightsky.events.UpdateEvent;
import nightsky.module.Module;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.IntValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldSettings.GameType;
import org.apache.commons.lang3.RandomUtils;

public class ChestAura extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public final IntValue range = new IntValue("range", 4, 1, 10);
   public final BooleanValue rotate = new BooleanValue("rotate", true);
   public final IntValue delay = new IntValue("delay", 200, 0, 2000);
   public final BooleanValue throughWalls = new BooleanValue("through-walls", false);
   public final BooleanValue autoClose = new BooleanValue("auto-close", false);
   public final BooleanValue debug = new BooleanValue("debug", false);
   private BlockPos targetChest = null;
   private List<BlockPos> openedChests = new ArrayList<>();
   private long lastOpenTime = 0L;
   private boolean isSearching = false;
   private boolean isRotating = false;
   private float targetYaw = 0.0F;
   private float targetPitch = 0.0F;
   private BlockPos rotatingTo = null;

   public ChestAura() {
      super("ChestAura", false);
   }

   private boolean isChest(Block block) {
      return block instanceof BlockChest;
   }

   private boolean isValidGameMode() {
      WorldSettings.GameType gameType = mc.playerController.getCurrentGameType();
      return gameType == GameType.SURVIVAL || gameType == GameType.ADVENTURE;
   }

   private boolean canOpenChest() {
      if (!this.isValidGameMode()) {
         return false;
      } else if (mc.currentScreen instanceof GuiChest) {
         if (this.autoClose.getValue()) {
            mc.thePlayer.closeScreen();
            return false;
         } else {
            return false;
         }
      } else if (System.currentTimeMillis() - this.lastOpenTime < (long)this.delay.getValue()) {
         return false;
      } else {
         return !mc.thePlayer.isUsingItem();
      }
   }

   private boolean canReach(BlockPos pos) {
      double dx = mc.thePlayer.posX - ((double)pos.getX() + 0.5);
      double dy = mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight() - ((double)pos.getY() + 0.5);
      double dz = mc.thePlayer.posZ - ((double)pos.getZ() + 0.5);
      double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      float reachDistance = mc.playerController.getBlockReachDistance() + 0.5F;
      if (distance > (double)reachDistance) {
         return false;
      } else if (!this.throughWalls.getValue()) {
         Vec3 playerPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
         Vec3 blockPos = new Vec3((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
         return mc.theWorld.rayTraceBlocks(playerPos, blockPos, false, true, false) == null;
      } else {
         return true;
      }
   }

   private void openChest(BlockPos pos) {
      if (this.debug.getValue()) {
         System.out.println("[ChestAura] Starting to rotate toward chest at " + pos);
      }

      mc.thePlayer.setSprinting(false);
      if (this.rotate.getValue()) {
         double offsetX = RandomUtils.nextDouble(-0.1, 0.1);
         double offsetZ = RandomUtils.nextDouble(-0.1, 0.1);
         double offsetY = RandomUtils.nextDouble(-0.05, 0.05);
         Vec3 targetVec = new Vec3((double)pos.getX() + 0.5 + offsetX, (double)pos.getY() + 0.5 + offsetY, (double)pos.getZ() + 0.5 + offsetZ);
         Vec3 playerEyePos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
         double dx = targetVec.xCoord - playerEyePos.xCoord;
         double dy = targetVec.yCoord - playerEyePos.yCoord;
         double dz = targetVec.zCoord - playerEyePos.zCoord;
         double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
         this.targetYaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
         this.targetPitch = (float)Math.toDegrees(-Math.atan2(dy, horizontalDistance));
         this.isRotating = true;
         this.rotatingTo = pos;
      } else {
         this.sendChestInteract(pos);
      }

   }

   private void sendChestInteract(BlockPos pos) {
      mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(pos, 1, mc.thePlayer.getCurrentEquippedItem(), 0.0F, 0.0F, 0.0F));
      mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
      this.openedChests.add(pos);
      this.lastOpenTime = System.currentTimeMillis();
      this.targetChest = null;
      if (this.debug.getValue()) {
         System.out.println("[ChestAura] Chest opened at " + pos);
      }

   }

   @EventTarget
   public void onUpdate(UpdateEvent event) {
      if (this.isEnabled() && mc.thePlayer != null && mc.theWorld != null) {
         float nearestDistance;
         if (event.getType() == EventType.PRE) {
            if (mc.thePlayer.ticksExisted % 5 != 0) {
               return;
            }

            if (!this.canOpenChest()) {
               return;
            }

            BlockPos nearestChest = null;
            nearestDistance = Float.MAX_VALUE;
            int radius = this.range.getValue();

            for(int y = radius; y >= -radius; --y) {
               for(int x = -radius; x <= radius; ++x) {
                  for(int z = -radius; z <= radius; ++z) {
                     BlockPos pos = new BlockPos((int)(mc.thePlayer.posX + (double)x), (int)(mc.thePlayer.posY + (double)y), (int)(mc.thePlayer.posZ + (double)z));
                     if (!this.openedChests.contains(pos)) {
                        Block block = mc.theWorld.getBlockState(pos).getBlock();
                        if (this.isChest(block) && this.canReach(pos)) {
                           double distance = mc.thePlayer.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
                           if (distance < (double)nearestDistance) {
                              nearestDistance = (float)distance;
                              nearestChest = pos;
                           }
                        }
                     }
                  }
               }
            }

            if (nearestChest != null) {
               this.targetChest = nearestChest;
               this.openChest(this.targetChest);
            } else if (this.debug.getValue()) {
               System.out.println("[ChestAura] No chests found in range");
            }
         }

         if (this.isRotating && this.rotatingTo != null) {
            float currentYaw = mc.thePlayer.rotationYaw;
            nearestDistance = mc.thePlayer.rotationPitch;
            float factor = 0.2F;
            float newYaw = currentYaw + (this.targetYaw - currentYaw) * factor;
            float newPitch = nearestDistance + (this.targetPitch - nearestDistance) * factor;
            mc.thePlayer.rotationYaw = newYaw;
            mc.thePlayer.rotationPitch = newPitch;
            mc.thePlayer.renderYawOffset = newYaw;
            mc.thePlayer.rotationYawHead = newYaw;
            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(newYaw, newPitch, mc.thePlayer.onGround));
            if (Math.abs(newYaw - this.targetYaw) < 0.5F && Math.abs(newPitch - this.targetPitch) < 0.5F) {
               this.isRotating = false;
               this.sendChestInteract(this.rotatingTo);
               this.rotatingTo = null;
            }
         }

      }
   }

   @EventTarget
   public void onRender3D(Render3DEvent event) {
   }

   @EventTarget
   public void onLoadWorld(LoadWorldEvent event) {
      this.openedChests.clear();
      this.targetChest = null;
      this.lastOpenTime = 0L;
      this.isRotating = false;
      this.rotatingTo = null;
   }

   public void onEnabled() {
      this.openedChests.clear();
      this.targetChest = null;
      this.lastOpenTime = 0L;
      this.isSearching = true;
      this.isRotating = false;
      this.rotatingTo = null;
      if (this.debug.getValue()) {
         System.out.println("[ChestAura] Enabled");
      }

   }

   public void onDisabled() {
      this.openedChests.clear();
      this.targetChest = null;
      this.lastOpenTime = 0L;
      this.isSearching = false;
      this.isRotating = false;
      this.rotatingTo = null;
      if (this.debug.getValue()) {
         System.out.println("[ChestAura] Disabled");
      }

   }

   public String[] getSuffix() {
      return new String[]{String.format("%d", this.range.getValue())};
   }
}