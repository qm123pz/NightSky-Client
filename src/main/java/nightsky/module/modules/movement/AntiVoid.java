package nightsky.module.modules.movement;

import com.google.common.base.CaseFormat;
import nightsky.NightSky;
import nightsky.enums.BlinkModules;
import nightsky.event.EventTarget;
import nightsky.event.types.Priority;
import nightsky.events.KeyEvent;
import nightsky.events.PlayerUpdateEvent;
import nightsky.module.Module;
import nightsky.util.PlayerUtil;
import nightsky.util.RandomUtil;
import nightsky.value.values.FloatValue;
import nightsky.value.values.ModeValue;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;
import net.minecraft.util.AxisAlignedBB;

public class AntiVoid extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private boolean isInVoid = false;
    private boolean wasInVoid = false;
    private double[] lastSafePosition = null;
    public final ModeValue mode = new ModeValue("mode", 0, new String[]{"BLINK"});
    public final FloatValue distance = new FloatValue("distance", 5.0F, 0.0F, 16.0F);

    private void resetBlink() {
        NightSky.blinkManager.setBlinkState(false, BlinkModules.ANTI_VOID);
        this.lastSafePosition = null;
    }

    private boolean canUseAntiVoid() {
        LongJump longJump = (LongJump) NightSky.moduleManager.modules.get(LongJump.class);
        return !longJump.isJumping();
    }

    public AntiVoid() {
        super("AntiVoid", false);
    }

    @EventTarget(Priority.LOWEST)
    public void onUpdate(PlayerUpdateEvent event) {
        if (this.isEnabled()) {
            this.isInVoid = !mc.thePlayer.capabilities.allowFlying && PlayerUtil.isInWater();
            if (this.mode.getValue() == 0) {
                if (!this.isInVoid) {
                    this.resetBlink();
                }
                if (this.lastSafePosition != null) {
                    float subWidth = mc.thePlayer.width / 2.0F;
                    float height = mc.thePlayer.height;
                    if (PlayerUtil.checkInWater(
                            new AxisAlignedBB(
                                    this.lastSafePosition[0] - (double) subWidth,
                                    this.lastSafePosition[1],
                                    this.lastSafePosition[2] - (double) subWidth,
                                    this.lastSafePosition[0] + (double) subWidth,
                                    this.lastSafePosition[1] + (double) height,
                                    this.lastSafePosition[2] + (double) subWidth
                            )
                    )) {
                        this.resetBlink();
                    }
                }
                if (!this.wasInVoid && this.isInVoid && this.canUseAntiVoid()) {
                    NightSky.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                    if (NightSky.blinkManager.setBlinkState(true, BlinkModules.ANTI_VOID)) {
                        this.lastSafePosition = new double[]{mc.thePlayer.prevPosX, mc.thePlayer.prevPosY, mc.thePlayer.prevPosZ};
                    }
                }
                if (NightSky.blinkManager.getBlinkingModule() == BlinkModules.ANTI_VOID
                        && this.lastSafePosition != null
                        && this.lastSafePosition[1] - (double) this.distance.getValue().floatValue() > mc.thePlayer.posY) {
                    NightSky.blinkManager
                            .blinkedPackets
                            .offerFirst(
                                    new C04PacketPlayerPosition(
                                            this.lastSafePosition[0], this.lastSafePosition[1] - RandomUtil.nextDouble(10.0, 20.0), this.lastSafePosition[2], false
                                    )
                            );
                    this.resetBlink();
                }
            }
            this.wasInVoid = this.isInVoid;
        }
    }

    @EventTarget
    public void onKey(KeyEvent event) {
        if (event.getKey() == mc.gameSettings.keyBindUseItem.getKeyCode()) {
            ItemStack currentItem = mc.thePlayer.inventory.getCurrentItem();
            if (currentItem != null && currentItem.getItem() instanceof ItemEnderPearl) {
                this.resetBlink();
            }
        }
    }

    @Override
    public void onEnabled() {
        this.isInVoid = false;
        this.wasInVoid = false;
        this.resetBlink();
    }

    @Override
    public void onDisabled() {
        NightSky.blinkManager.setBlinkState(false, BlinkModules.ANTI_VOID);
    }

    @Override
    public void verifyValue(String mode) {
        if (this.isEnabled()) {
            this.onDisabled();
        }
    }

    @Override
    public String[] getSuffix() {
        return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeString())};
    }
}