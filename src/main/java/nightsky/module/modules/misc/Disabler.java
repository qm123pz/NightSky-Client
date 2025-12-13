package nightsky.module.modules.misc;

import nightsky.NightSky;
import nightsky.event.EventTarget;
import nightsky.events.PacketEvent;
import nightsky.event.types.EventType;
import nightsky.module.Module;
import nightsky.util.ChatUtil;
import nightsky.util.PacketUtil;
import nightsky.value.values.ModeValue;
import nightsky.value.values.BooleanValue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action;

import java.util.ArrayList;
import java.util.List;

public class Disabler extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final ModeValue mode = new ModeValue("Mode", 0, new String[]{"PredictionInventory", "NewInventory", "Inventory"});
    public final BooleanValue Jump = new BooleanValue("Jump", false);
    public final BooleanValue digValue = new BooleanValue("Spoof Dig Release", true);
    public final BooleanValue debug = new BooleanValue("Debug", false);

    private final List<Packet<?>> inventoryPackets = new ArrayList<>();
    private int jumpDelayTicks = 0;
    private boolean shouldAutoJump = false;
    private boolean wasOnGround = true;
    private int groundTicks = 0;
    private int airTicks = 0;
    private double lastY = 0.0;
    private boolean lastOnGround = true;
    private int fakeGroundTicks = 0;

    public Disabler() {
        super("Disabler", false);
    }

    @Override
    public void onEnabled() {
        String currentMode = mode.getModeString();
        if (currentMode.equals("PredictionInventory")) {
            ChatUtil.sendFormatted(String.format("%s%s: Vanilla-InvWalk & Silent-InvManager ready.",
                    NightSky.clientName, this.getName()));
        } else if (currentMode.equals("NewInventory")) {
            ChatUtil.sendFormatted(String.format("%s%s: New Inventory Disabler enabled.",
                    NightSky.clientName, this.getName()));
        } else if (currentMode.equals("Inventory")) {
            ChatUtil.sendFormatted(String.format("%s%s: Inventory Disabler enabled.",
                    NightSky.clientName, this.getName()));
        }

        if (Jump.getValue()) {
            ChatUtil.sendFormatted(String.format("%s%s: Advanced Jump Bypass for Grim & Watchdog active",
                    NightSky.clientName, this.getName()));
            shouldAutoJump = true;
        }

        if (digValue.getValue()) {
            ChatUtil.sendFormatted(String.format("%s%s: Spoof Dig Release enabled",
                    NightSky.clientName, this.getName()));
        }

        if (debug.getValue()) {
            ChatUtil.sendFormatted(String.format("%s%s: Debug mode enabled",
                    NightSky.clientName, this.getName()));
        }

        resetStates();
    }

    @Override
    public void onDisabled() {
        if (!inventoryPackets.isEmpty()) {
            for (Packet<?> p : inventoryPackets) {
                PacketUtil.sendPacketNoEvent(p);
            }
            inventoryPackets.clear();
        }
        resetStates();
    }

    private void resetStates() {
        inventoryPackets.clear();
        jumpDelayTicks = 0;
        shouldAutoJump = false;
        wasOnGround = true;
        groundTicks = 0;
        airTicks = 0;
        lastY = mc.thePlayer != null ? mc.thePlayer.posY : 0.0;
        lastOnGround = true;
        fakeGroundTicks = 0;
    }

    public void onUpdate() {
        if (!this.isEnabled() || mc.thePlayer == null) return;

        if (Jump.getValue()) {

            if (shouldAutoJump && mc.thePlayer.onGround && jumpDelayTicks <= 0) {
                mc.thePlayer.jump();
                shouldAutoJump = false;
                jumpDelayTicks = 10;
                if (debug.getValue()) {
                    ChatUtil.sendFormatted("§a[Disabler] Jump executed");
                }
            }
            if (jumpDelayTicks > 0) jumpDelayTicks--;
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (!this.isEnabled()) return;

        if (event.getType() == EventType.SEND) {
            handleSpoofDigRelease(event);
            handleGRIMInventory(event);
            handleInventory(event);
            handlePredictionInventory(event);
        }

        if (Jump.getValue()) {
            if (event.getType() == EventType.SEND && event.getPacket() instanceof C03PacketPlayer) {
                handleAdvancedOnGroundJump((C03PacketPlayer) event.getPacket(), event);
            }
            if (event.getType() == EventType.RECEIVE) {
                if (event.getPacket() instanceof S08PacketPlayerPosLook) {
                    handlePlayerPosLook((S08PacketPlayerPosLook) event.getPacket());
                }
            }
        }
    }

    private void handleSpoofDigRelease(PacketEvent event) {
        if (digValue.getValue() && mc.thePlayer != null && mc.thePlayer.getCurrentEquippedItem() != null
                && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemSword
                && event.getPacket() instanceof C07PacketPlayerDigging) {

            C07PacketPlayerDigging c07 = (C07PacketPlayerDigging) event.getPacket();
            if (c07.getStatus() == Action.RELEASE_USE_ITEM) {
                event.setCancelled(true);
                int current = mc.thePlayer.inventory.currentItem;
                int next = (current + 1) % 9;

                PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(next));
                PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(current));

                if (debug.getValue()) {
                    ChatUtil.sendFormatted(NightSky.clientName + "Spoofed C07 release with C09 swap.");
                }
            }
        }
    }

    private void handleGRIMInventory(PacketEvent event) {
        if (mode.getModeString().equals("GRIM-Inv") && event.getPacket() instanceof C0EPacketClickWindow) {
            C0EPacketClickWindow c0e = (C0EPacketClickWindow) event.getPacket();
            if (c0e.getWindowId() != 0) {
                return;
            }

            event.setCancelled(true);
            PacketUtil.sendPacketNoEvent(c0e);
            PacketUtil.sendPacketNoEvent(new C0DPacketCloseWindow(0));

            if (debug.getValue()) {
                ChatUtil.sendFormatted(NightSky.clientName + "Sent C0E + C0D (GRIM-Inv) | slot=" + c0e.getSlotId());
            }
        }
    }

    private void handleInventory(PacketEvent event) {
        if (mode.getModeString().equals("Inventory") && event.getPacket() instanceof C0EPacketClickWindow) {
            C0EPacketClickWindow c0e = (C0EPacketClickWindow) event.getPacket();
            if (c0e.getWindowId() != 0) {
                return;
            }

            event.setCancelled(true);
            PacketUtil.sendPacketNoEvent(c0e);
            PacketUtil.sendPacketNoEvent(new C0DPacketCloseWindow(0));

            if (debug.getValue()) {
                ChatUtil.sendFormatted(NightSky.clientName + "Sent C0E + C0D (Inventory) | slot=" + c0e.getSlotId());
            }
        }
    }

    private void handlePredictionInventory(PacketEvent event) {
        if (!mode.getModeString().equals("PredictionInventory")) return;

        Packet<?> packet = event.getPacket();
        if (packet instanceof C16PacketClientStatus || packet instanceof C0EPacketClickWindow) {
            event.setCancelled(true);
            inventoryPackets.add(packet);
        } else if (packet instanceof C0DPacketCloseWindow) {
            for (Packet<?> p : inventoryPackets) {
                PacketUtil.sendPacketNoEvent(p);
            }
            inventoryPackets.clear();
        }
    }

    private void handleAdvancedOnGroundJump(C03PacketPlayer packet, PacketEvent event) {
        if (mc.thePlayer == null || !Jump.getValue()) return;


        if (mc.thePlayer.onGround) {
            groundTicks++;
            airTicks = 0;
            wasOnGround = true;
            lastY = mc.thePlayer.posY;
        } else {
            airTicks++;
            groundTicks = 0;
            wasOnGround = false;
        }


        boolean isActuallyOnGround = mc.thePlayer.onGround;
        boolean packetClaimsOnGround = packet.isOnGround();
        boolean shouldFakeGround = false;


        if (!isActuallyOnGround && airTicks <= 6) {
            double fallDistance = Math.abs(lastY - mc.thePlayer.posY);


            if (fallDistance < 0.03 && Math.abs(mc.thePlayer.motionY) < 0.08 && mc.thePlayer.fallDistance < 0.1) {
                shouldFakeGround = true;
            }


            if (fallDistance < 0.05 && airTicks <= 3 && mc.thePlayer.fallDistance < 0.2) {
                shouldFakeGround = true;
            }


            if (airTicks <= 2 && mc.thePlayer.fallDistance < 0.1) {
                shouldFakeGround = true;
            }
        }


        if (shouldFakeGround) {
            try {
                java.lang.reflect.Field onGroundField = C03PacketPlayer.class.getDeclaredField("onGround");
                onGroundField.setAccessible(true);
                onGroundField.set(packet, true);

                fakeGroundTicks++;

                if (debug.getValue()) {
                    ChatUtil.sendFormatted("§a[Disabler] ONGROUND FAKE: Forced ground state (ticks:" + airTicks + ", dist:" + String.format("%.3f", Math.abs(lastY - mc.thePlayer.posY)) + ")");
                }
            } catch (Exception e) {
                if (debug.getValue()) {
                    ChatUtil.sendFormatted("§c[Disabler] Error modifying onGround: " + e.getMessage());
                }
            }
        } else {
            fakeGroundTicks = 0;
        }


        if (groundTicks < 3 && !wasOnGround && !packet.isOnGround()) {
            try {
                java.lang.reflect.Field onGroundField = C03PacketPlayer.class.getDeclaredField("onGround");
                onGroundField.setAccessible(true);
                if (mc.thePlayer.fallDistance < 2.0f && groundTicks == 0) {
                    onGroundField.set(packet, true);
                    if (debug.getValue()) {
                        ChatUtil.sendFormatted("§a[Disabler] WD Legacy: Modified C03 onGround");
                    }
                }
            } catch (Exception e) {
                if (debug.getValue()) {
                    ChatUtil.sendFormatted("§c[Disabler] Error: " + e.getMessage());
                }
            }
        }


        lastOnGround = packet.isOnGround();
    }

    private void handlePlayerPosLook(S08PacketPlayerPosLook packet) {
        if (debug.getValue()) {
            ChatUtil.sendFormatted("§e[Disabler] Received S08 packet. Resetting jump states.");
        }

        shouldAutoJump = true;
        jumpDelayTicks = 0;
        groundTicks = 0;
        airTicks = 0;
        wasOnGround = true;
        lastY = mc.thePlayer.posY;
        fakeGroundTicks = 0;
        lastOnGround = true;
    }
}