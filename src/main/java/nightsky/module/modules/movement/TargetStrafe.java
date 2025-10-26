package nightsky.module.modules.movement;

import nightsky.NightSky;
import nightsky.event.EventTarget;
import nightsky.event.types.EventType;
import nightsky.event.types.Priority;
import nightsky.events.Render3DEvent;
import nightsky.events.StrafeEvent;
import nightsky.events.UpdateEvent;
import nightsky.module.Module;
import nightsky.module.modules.combat.KillAura;
import nightsky.util.*;
import nightsky.value.values.*;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.ModeValue;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;

import java.awt.*;
import java.util.ArrayList;

public class TargetStrafe extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private EntityLivingBase target = null;
    private float targetYaw = Float.NaN;
    private int direction = 1;
    public final FloatValue radius = new FloatValue("Radius", 1.0F, 0.0F, 6.0F);
    public final IntValue points = new IntValue("Points", 6, 3, 24);
    public final BooleanValue requirePress = new BooleanValue("RequirePress", true);
    public final BooleanValue speedOnly = new BooleanValue("SpeedOnly", true);
    public final ModeValue showTarget = new ModeValue("ShowTarget", 1, new String[]{"None", "Default"});

    private boolean canStrafe() {
        if (this.speedOnly.getValue()) {
            Speed speed = (Speed) NightSky.moduleManager.modules.get(Speed.class);
            Fly fly = (Fly) NightSky.moduleManager.modules.get(Fly.class);
            LongJump longJump = (LongJump) NightSky.moduleManager.modules.get(LongJump.class);
            if (!speed.isEnabled() && !fly.isEnabled() && (!longJump.isEnabled() || !longJump.isJumping())) {
                return false;
            }
        }
        return !this.requirePress.getValue() || PlayerUtil.isJumping();
    }

    private EntityLivingBase getKillAuraTarget() {
        KillAura killAura = (KillAura) NightSky.moduleManager.modules.get(KillAura.class);
        if (killAura.isEnabled() && killAura.isAttackAllowed()) {
            EntityLivingBase entityLivingBase = killAura.getTarget();
            return !TeamUtil.isEntityLoaded(entityLivingBase) ? null : entityLivingBase;
        } else {
            return null;
        }
    }

    private Color getTargetColor(EntityLivingBase entityLivingBase) {
        if (entityLivingBase instanceof EntityPlayer) {
            if (TeamUtil.isFriend((EntityPlayer) entityLivingBase)) {
                return NightSky.friendManager.getColor();
            }
            if (TeamUtil.isTarget((EntityPlayer) entityLivingBase)) {
                return NightSky.targetManager.getColor();
            }
        }
        switch (this.showTarget.getValue()) {
            case 1:
                if (!(entityLivingBase instanceof EntityPlayer)) {
                    return Color.WHITE;
                }
                return TeamUtil.getTeamColor((EntityPlayer) entityLivingBase, 1.0F);
            default:
                return new Color(-1);
        }
    }

    private boolean isInWater(double x, double z) {
        return PlayerUtil.checkInWater(
                new AxisAlignedBB(x - 0.015, mc.thePlayer.posY, z - 0.015, x + 0.015, mc.thePlayer.posY + (double) mc.thePlayer.height, z + 0.015)
        );
    }

    private int wrapIndex(int integer1, int integer2) {
        if (integer1 < 0) {
            return integer2 - 1;
        } else {
            return integer1 >= integer2 ? 0 : integer1;
        }
    }

    public TargetStrafe() {
        super("TargetStrafe", false);
    }

    public float getTargetYaw() {
        return this.targetYaw;
    }

    @EventTarget(Priority.HIGHEST)
    public void onUpdate(UpdateEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            boolean left = PlayerUtil.isMovingLeft();
            boolean right = PlayerUtil.isMovingRight();
            if (left ^ right) {
                this.direction = left ? 1 : -1;
            }
            if (!this.canStrafe()) {
                this.target = null;
                this.targetYaw = Float.NaN;
            } else {
                this.target = this.getKillAuraTarget();
                if (this.target == null) {
                    this.targetYaw = Float.NaN;
                } else {
                    ArrayList<Vec2d> vpositions = new ArrayList<>();
                    for (int i = 0; i < this.points.getValue(); i++) {
                        vpositions.add(
                                new Vec2d(
                                        (double) this.radius.getValue()
                                                * Math.cos((double) i * ((Math.PI * 2) / (double) this.points.getValue())),
                                        (double) this.radius.getValue()
                                                * Math.sin((double) i * ((Math.PI * 2) / (double) this.points.getValue()))
                                )
                        );
                    }
                    if (vpositions.isEmpty()) {
                        this.target = null;
                        this.targetYaw = Float.NaN;
                    } else {
                        double closestDistance = 0.0;
                        int closestIndex = -1;
                        for (int i = 0; i < vpositions.size(); i++) {
                            double distance = mc.thePlayer
                                    .getDistance(
                                            this.target.posX + (vpositions.get(i)).getX(), mc.thePlayer.posY, this.target.posZ + (vpositions.get(i)).getY()
                                    );
                            if (closestIndex == -1 || distance < closestDistance) {
                                closestDistance = distance;
                                closestIndex = i;
                            }
                        }
                        if (mc.thePlayer.isCollidedHorizontally) {
                            this.direction *= -1;
                        }
                        int nextIndex = closestIndex + this.direction;
                        nextIndex = this.wrapIndex(nextIndex, vpositions.size());
                        double nextX = this.target.posX + (vpositions.get(nextIndex)).getX();
                        double nextZ = this.target.posZ + (vpositions.get(nextIndex)).getY();
                        if (this.isInWater(nextX, nextZ)) {
                            this.direction *= -1;
                            nextIndex = closestIndex + this.direction;
                            nextIndex = this.wrapIndex(nextIndex, vpositions.size());
                            nextX = this.target.posX + (vpositions.get(nextIndex)).getX();
                            nextZ = this.target.posZ + (vpositions.get(nextIndex)).getY();
                        }
                        double deltaX = nextX - mc.thePlayer.posX;
                        double deltaZ = nextZ - mc.thePlayer.posZ;
                        float currentPitch = event.getPitch();
                        float currentYaw = event.getYaw();
                        double deltaY = 0.0;
                        this.targetYaw = RotationUtil.getRotationsTo(deltaX, deltaY, deltaZ, currentYaw, currentPitch)[0];
                        event.setPervRotation(this.targetYaw, 10);
                    }
                }
            }
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (this.isEnabled()) {
            if (!Float.isNaN(this.targetYaw) && MoveUtil.isForwardPressed()) {
                event.setStrafe(0.0F);
                event.setForward(1.0F);
            }
        }
    }

    @EventTarget
    public void onRender(Render3DEvent event) {
        if (this.isEnabled() && TeamUtil.isEntityLoaded(this.target)) {
            if (this.showTarget.getValue() != 0) {
                Color color = this.getTargetColor(this.target);
                RenderUtil.enableRenderState();
                RenderUtil.drawEntityCircle(
                        this.target, this.radius.getValue(), this.points.getValue(), ColorUtil.darker(color, 0.2F).getRGB()
                );
                RenderUtil.drawEntityCircle(this.target, this.radius.getValue(), this.points.getValue(), color.getRGB());
                RenderUtil.disableRenderState();
            }
        }
    }

    @Override
    public void onDisabled() {
        this.target = null;
        this.targetYaw = Float.NaN;
    }

    public static class Vec2d {
        private final double x;
        private final double y;

        public Vec2d(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }
    }
}
