package nightsky.module.modules.player;

import nightsky.NightSky;
import nightsky.event.EventTarget;
import nightsky.event.types.EventType;
import nightsky.event.types.Priority;
import nightsky.events.*;
import nightsky.management.RotationState;
import nightsky.module.Module;
import nightsky.module.modules.movement.LongJump;
import nightsky.util.*;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.ModeValue;
import nightsky.value.values.PercentValue;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.WorldSettings.GameType;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class ScaffoldB extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final double[] placeOffsets = new double[]{
            0.03125,
            0.09375,
            0.15625,
            0.21875,
            0.28125,
            0.34375,
            0.40625,
            0.46875,
            0.53125,
            0.59375,
            0.65625,
            0.71875,
            0.78125,
            0.84375,
            0.90625,
            0.96875
    };

    private int rotationTick = 0;
    private int lastSlot = -1;
    private int blockCount = -1;
    private float yaw = -180.0F;
    private float pitch = 0.0F;
    private boolean canRotate = false;
    private int towerTick = 0;
    private int towerDelay = 0;
    private int stage = 0;
    private int startY = 256;
    private boolean shouldKeepY = false;
    private boolean towering = false;
    private EnumFacing targetFacing = null;
    private float groundedYaw = 0.0F;
    private int airTicks = 0;

    public final ModeValue rotationMode = new ModeValue("Rotations", 2, new String[]{"None", "Default", "Backwards", "Sideways"});
    public final ModeValue moveFix = new ModeValue("MoveFix", 1, new String[]{"None", "Silent"});
    public final ModeValue sprintMode = new ModeValue("Sprint", 0, new String[]{"None", "Vanilla"});
    public final PercentValue groundMotion = new PercentValue("GroundMotion", 100);
    public final PercentValue airMotion = new PercentValue("AirMotion", 100);
    public final PercentValue speedMotion = new PercentValue("SpeedMotion", 100);
    public final ModeValue tower = new ModeValue("Tower", 0, new String[]{"None", "Vanilla", "Extra", "Telly"});
    public final ModeValue keepY = new ModeValue("KeepY", 0, new String[]{"None", "Vanilla", "Extra", "Telly"});
    public final BooleanValue keepYonPress = new BooleanValue("KeepYOnPress", false, () -> keepY.getValue() != 0);
    public final BooleanValue multiplace = new BooleanValue("MultiPlace", true);
    public final BooleanValue safeWalk = new BooleanValue("SafeWalk", true);
    public final BooleanValue swing = new BooleanValue("Swing", true);
    public final BooleanValue itemSpoof = new BooleanValue("ItemSpoof", false);
    public final BooleanValue blockCounter = new BooleanValue("BlockCounter", true);

    private boolean shouldStopSprint() {
        if (isTowering()) {
            return false;
        } else {
            int keepYValue = (Integer) keepY.getValue();
            boolean isKeepingY = keepYValue == 1 || keepYValue == 2;
            if (isKeepingY && stage > 0) {
                return (Integer) sprintMode.getValue() == 0;
            }

            return false;
        }
    }

    private boolean canPlace() {
        BedNuker bedNuker = (BedNuker) NightSky.moduleManager.modules.get(BedNuker.class);
        if (bedNuker.isEnabled() && bedNuker.isReady()) {
            return false;
        } else {
            LongJump longJump = (LongJump) NightSky.moduleManager.modules.get(LongJump.class);
            return !longJump.isEnabled() || !longJump.isAutoMode() || longJump.isJumping();
        }
    }

    private EnumFacing getBestFacing(BlockPos blockPos1, BlockPos blockPos3) {
        double offset = 0.0;
        EnumFacing enumFacing = null;
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing != EnumFacing.DOWN) {
                BlockPos pos = blockPos1.offset(facing);
                if (pos.getY() <= blockPos3.getY()) {
                    double distance = pos.distanceSqToCenter(blockPos3.getX() + 0.5, blockPos3.getY() + 0.5, blockPos3.getZ() + 0.5);
                    if (enumFacing == null || distance < offset || (distance == offset && facing == EnumFacing.UP)) {
                        offset = distance;
                        enumFacing = facing;
                    }
                }
            }
        }
        return enumFacing;
    }

    private BlockData getBlockData() {
        int startY = MathHelper.floor_double(mc.thePlayer.posY);
        BlockPos targetPos = new BlockPos(
                MathHelper.floor_double(mc.thePlayer.posX),
                (stage != 0 && !shouldKeepY ? Math.min(startY, this.startY) : startY) - 1,
                MathHelper.floor_double(mc.thePlayer.posZ)
        );
        if (!BlockUtil.isReplaceable(targetPos)) {
            return null;
        }

        ArrayList<BlockPos> positions = new ArrayList<>();
        for (int x = -4; x <= 4; ++x) {
            for (int y = -4; y <= 0; ++y) {
                for (int z = -4; z <= 4; ++z) {
                    BlockPos pos = targetPos.add(x, y, z);
                    if (!BlockUtil.isReplaceable(pos)
                            && !BlockUtil.isContainer(pos)
                            && mc.thePlayer.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= mc.playerController.getBlockReachDistance()
                            && (stage == 0 || shouldKeepY || pos.getY() < this.startY)) {
                        for (EnumFacing facing : EnumFacing.VALUES) {
                            if (facing != EnumFacing.DOWN) {
                                BlockPos blockPos = pos.offset(facing);
                                if (BlockUtil.isReplaceable(blockPos)) {
                                    positions.add(pos);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (positions.isEmpty()) {
            return null;
        }

        positions.sort(Comparator.comparingDouble(o ->
                o.distanceSqToCenter(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5)
        ));

        BlockPos blockPos = positions.get(0);
        EnumFacing facing = getBestFacing(blockPos, targetPos);
        return facing == null ? null : new BlockData(blockPos, facing);
    }

    private void place(BlockPos blockPos, EnumFacing enumFacing, Vec3 vec3) {
        if (ItemUtil.isHoldingBlock() && blockCount > 0 &&
                mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem(), blockPos, enumFacing, vec3)) {
            if (mc.playerController.getCurrentGameType() != GameType.CREATIVE) {
                --blockCount;
            }
            if (swing.getValue()) {
                mc.thePlayer.swingItem();
            } else {
                PacketUtil.sendPacket(new C0APacketAnimation());
            }
        }
    }

    private EnumFacing yawToFacing(float yaw) {
        if (yaw < -135.0F || yaw > 135.0F) {
            return EnumFacing.NORTH;
        } else if (yaw < -45.0F) {
            return EnumFacing.EAST;
        } else {
            return yaw < 45.0F ? EnumFacing.SOUTH : EnumFacing.WEST;
        }
    }

    private double distanceToEdge(EnumFacing enumFacing) {
        switch (enumFacing) {
            case NORTH:
                return mc.thePlayer.posZ - Math.floor(mc.thePlayer.posZ);
            case EAST:
                return Math.ceil(mc.thePlayer.posX) - mc.thePlayer.posX;
            case SOUTH:
                return Math.ceil(mc.thePlayer.posZ) - mc.thePlayer.posZ;
            case WEST:
            default:
                return mc.thePlayer.posX - Math.floor(mc.thePlayer.posX);
        }
    }

    private float getSpeed() {
        if (!mc.thePlayer.onGround) {
            return (float) airMotion.getValue() / 100.0F;
        } else {
            return MoveUtil.getSpeedLevel() > 0 ? (float) speedMotion.getValue() / 100.0F : (float) groundMotion.getValue() / 100.0F;
        }
    }

    private double getRandomOffset() {
        return 0.2155 - RandomUtil.nextDouble(1.0E-4, 9.0E-4);
    }

    private float getCurrentYaw() {
        return MoveUtil.adjustYaw(mc.thePlayer.rotationYaw, (float) MoveUtil.getForwardValue(), (float) MoveUtil.getLeftValue());
    }

    private boolean isDiagonal(float yaw) {
        float absYaw = Math.abs(yaw % 90.0F);
        return absYaw > 20.0F && absYaw < 70.0F;
    }

    private boolean isTowering() {
        if (mc.thePlayer.onGround && MoveUtil.isForwardPressed() && !PlayerUtil.isAirAbove()) {
            boolean keepY = ((Integer) this.keepY.getValue()) == 3;
            boolean tower = ((Integer) this.tower.getValue()) == 3;
            return keepY && stage > 0 || tower && mc.gameSettings.keyBindJump.isKeyDown();
        }
        return false;
    }

    public ScaffoldB() {
        super("ScaffoldB", false);
    }

    public int getSlot() {
        return lastSlot;
    }

    @EventTarget(Priority.HIGH)
    public void onUpdate(UpdateEvent event) {
        if (!isEnabled() || event.getType() != EventType.PRE) return;

        if (mc.thePlayer.onGround) {
            airTicks = 0;
            groundedYaw = RotationUtil.wrapAngleDiff(mc.thePlayer.rotationYaw - 180.0F, 0.0F);
        } else {
            ++airTicks;
        }

        if (rotationTick > 0) {
            --rotationTick;
        }

        if (mc.thePlayer.onGround) {
            if (stage > 0) --stage;
            if (stage < 0) ++stage;

            if (stage == 0 && ((Integer) keepY.getValue()) != 0 &&
                    (!(Boolean) keepYonPress.getValue() || PlayerUtil.isUsingItem()) &&
                    !mc.gameSettings.keyBindJump.isKeyDown()) {
                stage = 1;
            }

            startY = shouldKeepY ? startY : MathHelper.floor_double(mc.thePlayer.posY);
            shouldKeepY = false;
            towering = false;
        }

        if (canPlace()) {
            ItemStack stack = mc.thePlayer.getHeldItem();
            int count = ItemUtil.isBlock(stack) ? stack.stackSize : 0;
            blockCount = Math.min(blockCount, count);

            if (blockCount <= 0) {
                int startSlot = mc.thePlayer.inventory.currentItem - 1;
                for (int i = startSlot; i > startSlot - 9; --i) {
                    int hotbarSlot = (i % 9 + 9) % 9;
                    ItemStack candidate = mc.thePlayer.inventory.getStackInSlot(hotbarSlot);
                    if (ItemUtil.isBlock(candidate)) {
                        mc.thePlayer.inventory.currentItem = hotbarSlot;
                        blockCount = candidate.stackSize;
                        break;
                    }
                }
            }

            float currentYaw = getCurrentYaw();
            float yawDiffTo180 = RotationUtil.wrapAngleDiff(currentYaw - 180.0F, event.getYaw());
            float diagonalYaw = isDiagonal(currentYaw)
                    ? yawDiffTo180
                    : RotationUtil.wrapAngleDiff(
                    currentYaw - 135.0F * (((currentYaw + 180.0F) % 90.0F) < 45.0F ? 1.0F : -1.0F),
                    event.getYaw());

            if (!canRotate) {
                switch ((Integer) rotationMode.getValue()) {
                    case 1:
                        if (yaw == -180.0F && pitch == 0.0F) {
                            yaw = RotationUtil.quantizeAngle(diagonalYaw);
                            pitch = RotationUtil.quantizeAngle(85.0F);
                        } else {
                            yaw = RotationUtil.quantizeAngle(diagonalYaw);
                        }
                        break;
                    case 2:
                        if (yaw == -180.0F && pitch == 0.0F) {
                            yaw = RotationUtil.quantizeAngle(RotationUtil.wrapAngleDiff(mc.thePlayer.rotationYaw - 180.0F, 0.0F));
                            pitch = RotationUtil.quantizeAngle(85.0F);
                        }
                        float baseYaw = groundedYaw;
                        if (airTicks > 0) {
                            boolean sba;
                            if (airTicks <= 1) {
                                sba = Math.abs(RotationUtil.wrapAngleDiff(mc.thePlayer.rotationYaw - 180.0F, baseYaw)) > 0.0F;
                                baseYaw += sba ? 60.0F : -60.0F;
                            } else if (airTicks == 2) {
                                sba = Math.abs(RotationUtil.wrapAngleDiff(mc.thePlayer.rotationYaw - 180.0F, baseYaw)) > 0.0F;
                                baseYaw += sba ? 30.0F : -30.0F;
                            } else if (airTicks >= 3) {
                                float targetYaw = Math.round(baseYaw / 15.0F) * 15.0F;
                                float diff = RotationUtil.wrapAngleDiff(targetYaw, yaw);
                                float speed = 30.0F;
                                if (Math.abs(diff) <= speed) {
                                    yaw = targetYaw;
                                } else {
                                    yaw = MathHelper.wrapAngleTo180_float(yaw + Math.copySign(speed, diff));
                                }
                                break;
                            }
                        }
                        yaw = RotationUtil.quantizeAngle(baseYaw);
                        break;
                    case 3:
                        if (yaw == -180.0F && pitch == 0.0F) {
                            yaw = RotationUtil.quantizeAngle(diagonalYaw);
                            pitch = RotationUtil.quantizeAngle(85.0F);
                        } else {
                            yaw = RotationUtil.quantizeAngle(diagonalYaw);
                        }
                        break;
                }
            }

            BlockData blockData = getBlockData();
            Vec3 hitVec = null;

            if (blockData != null) {
                double[] x = placeOffsets;
                double[] y = placeOffsets;
                double[] z = placeOffsets;

                switch (blockData.facing()) {
                    case NORTH:
                        z = new double[]{0.0};
                        break;
                    case EAST:
                        x = new double[]{1.0};
                        break;
                    case SOUTH:
                        z = new double[]{1.0};
                        break;
                    case WEST:
                        x = new double[]{0.0};
                        break;
                    case DOWN:
                        y = new double[]{0.0};
                        break;
                    case UP:
                        y = new double[]{1.0};
                        break;
                }

                float bestYaw = -180.0F;
                float bestPitch = 0.0F;
                float bestDiff = 0.0F;

                for (double dx : x) {
                    for (double dy : y) {
                        for (double dz : z) {
                            double relX = blockData.blockPos().getX() + dx - mc.thePlayer.posX;
                            double relY = blockData.blockPos().getY() + dy - mc.thePlayer.posY - mc.thePlayer.getEyeHeight();
                            double relZ = blockData.blockPos().getZ() + dz - mc.thePlayer.posZ;

                            float baseYawRot = RotationUtil.wrapAngleDiff(yaw, event.getYaw());
                            float[] rotations = RotationUtil.getRotationsTo(relX, relY, relZ, baseYawRot, pitch);
                            MovingObjectPosition mop = RotationUtil.rayTrace(rotations[0], rotations[1], mc.playerController.getBlockReachDistance(), 1.0F);

                            if (mop != null && mop.typeOfHit == MovingObjectType.BLOCK &&
                                    mop.getBlockPos().equals(blockData.blockPos()) &&
                                    mop.sideHit == blockData.facing()) {
                                float totalDiff = Math.abs(rotations[0] - baseYawRot) + Math.abs(rotations[1] - pitch);
                                if (bestYaw == -180.0F && bestPitch == 0.0F || totalDiff < bestDiff) {
                                    bestYaw = rotations[0];
                                    bestPitch = rotations[1];
                                    bestDiff = totalDiff;
                                    hitVec = mop.hitVec;
                                }
                            }
                        }
                    }
                }

                if (bestYaw != -180.0F || bestPitch != 0.0F) {
                    yaw = bestYaw;
                    pitch = bestPitch;
                    canRotate = true;
                }
            }

            if (canRotate && MoveUtil.isForwardPressed() &&
                    Math.abs(MathHelper.wrapAngleTo180_float(yawDiffTo180 - yaw)) < 90.0F) {
                switch ((Integer) rotationMode.getValue()) {
                    case 2:
                        yaw = RotationUtil.quantizeAngle(yawDiffTo180);
                        break;
                    case 3:
                        yaw = RotationUtil.quantizeAngle(diagonalYaw);
                        break;
                }
            }

            if ((Integer) rotationMode.getValue() != 0) {
                float targetYaw = yaw;
                float targetPitch = pitch;

                if (towering && (mc.thePlayer.motionY > 0.0 || mc.thePlayer.posY > startY + 1)) {
                    float yawDelta = MathHelper.wrapAngleTo180_float(yaw - event.getYaw());
                    float tolerance = rotationTick >= 2 ? RandomUtil.nextFloat(90.0F, 95.0F) : RandomUtil.nextFloat(30.0F, 35.0F);
                    if (Math.abs(yawDelta) > tolerance) {
                        float clamped = RotationUtil.clampAngle(yawDelta, tolerance);
                        targetYaw = RotationUtil.quantizeAngle(event.getYaw() + clamped);
                        rotationTick = Math.max(rotationTick, 1);
                    }
                }

                if (isTowering()) {
                    float yawDelta = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - event.getYaw());
                    targetYaw = RotationUtil.quantizeAngle(event.getYaw() + yawDelta * RandomUtil.nextFloat(0.98F, 0.99F));
                    targetPitch = RotationUtil.quantizeAngle(RandomUtil.nextFloat(30.0F, 80.0F));
                    rotationTick = 3;
                    towering = true;
                }

                event.setRotation(targetYaw, targetPitch, 3);
                if ((Integer) moveFix.getValue() == 1) {
                    event.setPervRotation(targetYaw, 3);
                }
            }

            if (blockData != null && hitVec != null && rotationTick <= 0) {
                place(blockData.blockPos(), blockData.facing(), hitVec);
                if (multiplace.getValue()) {
                    for (int i = 0; i < 3; ++i) {
                        blockData = getBlockData();
                        if (blockData == null) break;

                        MovingObjectPosition mop = RotationUtil.rayTrace(yaw, pitch, mc.playerController.getBlockReachDistance(), 1.0F);
                        if (mop != null && mop.typeOfHit == MovingObjectType.BLOCK &&
                                mop.getBlockPos().equals(blockData.blockPos()) &&
                                mop.sideHit == blockData.facing()) {
                            place(blockData.blockPos(), blockData.facing(), mop.hitVec);
                        } else {
                            hitVec = BlockUtil.getClickVec(blockData.blockPos(), blockData.facing());
                            double dx = hitVec.xCoord - mc.thePlayer.posX;
                            double dy = hitVec.yCoord - mc.thePlayer.posY - mc.thePlayer.getEyeHeight();
                            double dz = hitVec.zCoord - mc.thePlayer.posZ;
                            float[] rotations = RotationUtil.getRotationsTo(dx, dy, dz, event.getYaw(), event.getPitch());

                            if (Math.abs(rotations[0] - yaw) >= 120.0F || Math.abs(rotations[1] - pitch) >= 60.0F) break;

                            mop = RotationUtil.rayTrace(rotations[0], rotations[1], mc.playerController.getBlockReachDistance(), 1.0F);
                            if (mop == null || mop.typeOfHit != MovingObjectType.BLOCK ||
                                    !mop.getBlockPos().equals(blockData.blockPos()) ||
                                    mop.sideHit != blockData.facing()) break;

                            place(blockData.blockPos(), blockData.facing(), mop.hitVec);
                        }
                    }
                }
            }

            if (targetFacing != null && rotationTick <= 0) {
                int px = MathHelper.floor_double(mc.thePlayer.posX);
                int py = MathHelper.floor_double(mc.thePlayer.posY);
                int pz = MathHelper.floor_double(mc.thePlayer.posZ);
                BlockPos below = new BlockPos(px, py - 1, pz);
                hitVec = BlockUtil.getHitVec(below, targetFacing, yaw, pitch);
                place(below, targetFacing, hitVec);
                targetFacing = null;
            } else if (((Integer) keepY.getValue()) == 2 && stage > 0 && !mc.thePlayer.onGround) {
                int nextY = MathHelper.floor_double(mc.thePlayer.posY + mc.thePlayer.motionY);
                if (nextY <= startY && mc.thePlayer.posY > startY + 1) {
                    shouldKeepY = true;
                    blockData = getBlockData();
                    if (blockData != null && rotationTick <= 0) {
                        hitVec = BlockUtil.getHitVec(blockData.blockPos(), blockData.facing(), yaw, pitch);
                        place(blockData.blockPos(), blockData.facing(), hitVec);
                    }
                }
            }
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (!isEnabled()) return;

        if (!mc.thePlayer.isCollidedHorizontally &&
                mc.thePlayer.hurtTime <= 5 &&
                !mc.thePlayer.isPotionActive(Potion.jump) &&
                mc.gameSettings.keyBindJump.isKeyDown() &&
                ItemUtil.isHoldingBlock()) {

            int yState = (int) (mc.thePlayer.posY % 1.0 * 100.0);
            switch ((Integer) tower.getValue()) {
                case 1:
                    switch (towerTick) {
                        case 0:
                            if (mc.thePlayer.onGround) {
                                towerTick = 1;
                                mc.thePlayer.motionY = -0.0784000015258789;
                            }
                            return;
                        case 1:
                            if (yState == 0 && PlayerUtil.isAirBelow()) {
                                startY = MathHelper.floor_double(mc.thePlayer.posY);
                                towerTick = 2;
                                mc.thePlayer.motionY = 0.42F;
                                if (MoveUtil.isForwardPressed()) {
                                    MoveUtil.setSpeed(MoveUtil.getSpeed(), MoveUtil.getMoveYaw());
                                } else {
                                    MoveUtil.setSpeed(0.0);
                                    event.setForward(0.0F);
                                    event.setStrafe(0.0F);
                                }
                                return;
                            }
                            towerTick = 0;
                            return;
                        case 2:
                            towerTick = 3;
                            mc.thePlayer.motionY = 0.75 - mc.thePlayer.posY % 1.0;
                            return;
                        case 3:
                            towerTick = 1;
                            mc.thePlayer.motionY = 1.0 - mc.thePlayer.posY % 1.0;
                            return;
                        default:
                            towerTick = 0;
                    }
                    break;

                case 2:
                    switch (towerTick) {
                        case 0:
                            if (mc.thePlayer.onGround) {
                                towerTick = 1;
                                mc.thePlayer.motionY = -0.0784000015258789;
                            }
                            return;
                        case 1:
                            if (yState == 0 && PlayerUtil.isAirBelow()) {
                                startY = MathHelper.floor_double(mc.thePlayer.posY);
                                if (!MoveUtil.isForwardPressed()) {
                                    towerDelay = 2;
                                    MoveUtil.setSpeed(0.0);
                                    event.setForward(0.0F);
                                    event.setStrafe(0.0F);
                                    EnumFacing facing = yawToFacing(MathHelper.wrapAngleTo180_float(yaw - 180.0F));
                                    double distance = distanceToEdge(facing);
                                    if (distance > 0.1 && mc.thePlayer.onGround) {
                                        Vec3i dir = facing.getDirectionVec();
                                        double offset = Math.min(getRandomOffset(), distance - 0.05);
                                        double jitter = RandomUtil.nextDouble(0.02, 0.03);
                                        AxisAlignedBB next = mc.thePlayer.getEntityBoundingBox()
                                                .offset(dir.getX() * (offset - jitter), 0, dir.getZ() * (offset - jitter));
                                        if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, next).isEmpty()) {
                                            mc.thePlayer.motionY = -0.0784000015258789;
                                            mc.thePlayer.setPosition(
                                                    next.minX + (next.maxX - next.minX) / 2.0,
                                                    next.minY,
                                                    next.minZ + (next.maxZ - next.minZ) / 2.0
                                            );
                                        }
                                        return;
                                    } else {
                                        towerTick = 2;
                                        targetFacing = facing;
                                        mc.thePlayer.motionY = 0.42F;
                                    }
                                    return;
                                } else {
                                    towerTick = 2;
                                    ++towerDelay;
                                    mc.thePlayer.motionY = 0.42F;
                                    MoveUtil.setSpeed(MoveUtil.getSpeed(), MoveUtil.getMoveYaw());
                                    return;
                                }
                            }
                            towerTick = 0;
                            towerDelay = 0;
                            return;
                        case 2:
                            towerTick = 3;
                            mc.thePlayer.motionY -= RandomUtil.nextDouble(0.00101, 0.00109);
                            return;
                        case 3:
                            if (towerDelay >= 4) {
                                towerTick = 4;
                                towerDelay = 0;
                            } else {
                                towerTick = 1;
                                mc.thePlayer.motionY = 1.0 - mc.thePlayer.posY % 1.0;
                            }
                            return;
                        case 4:
                            towerTick = 5;
                            return;
                        case 5:
                            if (!PlayerUtil.isAirBelow()) {
                                towerTick = 0;
                            } else {
                                towerTick = 1;
                                EntityPlayerSP p = mc.thePlayer;
                                p.motionY -= 0.08;
                                p.motionY *= 0.98F;
                                p.motionY -= 0.08;
                                p.motionY *= 0.98F;
                            }
                            return;
                        default:
                            towerTick = 0;
                            towerDelay = 0;
                    }
                    break;
                default:
                    towerTick = 0;
                    towerDelay = 0;
            }
        } else {
            towerTick = 0;
            towerDelay = 0;
        }
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (!isEnabled()) return;

        if (moveFix.getValue() == 1 &&
                RotationState.isActived() &&
                RotationState.getPriority() == 3.0F &&
                MoveUtil.isForwardPressed()) {
            MoveUtil.fixStrafe(RotationState.getSmoothedYaw());
        }

        if (mc.thePlayer.onGround && stage > 0 && MoveUtil.isForwardPressed()) {
            mc.thePlayer.movementInput.jump = true;
        }
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (!isEnabled()) return;

        float speed = getSpeed();
        if (speed != 1.0F) {
            MovementInput input = mc.thePlayer.movementInput;
            if (input.moveForward != 0.0F && input.moveStrafe != 0.0F) {
                input.moveForward *= 1.0F / (float) Math.sqrt(2.0);
                input.moveStrafe *= 1.0F / (float) Math.sqrt(2.0);
            }
            input.moveForward *= speed;
            input.moveStrafe *= speed;
        }

        if (shouldStopSprint()) {
            mc.thePlayer.setSprinting(false);
        }
    }

    @EventTarget
    public void onSafeWalk(SafeWalkEvent event) {
        if (isEnabled() && safeWalk.getValue() &&
                mc.thePlayer.onGround && mc.thePlayer.motionY <= 0.0 &&
                PlayerUtil.canMove(mc.thePlayer.motionX, mc.thePlayer.motionZ, -1.0)) {
            event.setSafeWalk(true);
        }
    }

    @EventTarget
    public void onRender(Render2DEvent event) {
        if (!isEnabled() || !blockCounter.getValue()) return;

        int count = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.stackSize > 0 && stack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock) stack.getItem()).getBlock();
                if (!BlockUtil.isContainer(block) && BlockUtil.isSolid(block)) {
                    count += stack.stackSize;
                }
            }
        }

        float scale = 1.0F;
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1.0F);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        mc.fontRendererObj.drawString(
                String.format("%d block%s left", count, count != 1 ? "s" : ""),
                (int) ((new ScaledResolution(mc).getScaledWidth() / 2.0F + mc.fontRendererObj.FONT_HEIGHT * 1.5F) / scale),
                (int) (new ScaledResolution(mc).getScaledHeight() / 2.0F / scale - mc.fontRendererObj.FONT_HEIGHT / 2.0F + 1.0F),
                (count > 0 ? Color.WHITE.getRGB() : new Color(255, 85, 85).getRGB()) | 0xB2000000,
                true
        );

        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    @EventTarget
    public void onLeftClick(LeftClickMouseEvent event) {
        if (isEnabled()) event.setCancelled(true);
    }

    @EventTarget
    public void onRightClick(RightClickMouseEvent event) {
        if (isEnabled()) event.setCancelled(true);
    }

    @EventTarget
    public void onHitBlock(HitBlockEvent event) {
        if (isEnabled()) event.setCancelled(true);
    }

    @EventTarget
    public void onSwap(SwapItemEvent event) {
        if (isEnabled()) {
            lastSlot = event.setSlot(lastSlot);
            event.setCancelled(true);
        }
    }

    @Override
    public void onEnabled() {
        if (mc.thePlayer != null) {
            lastSlot = mc.thePlayer.inventory.currentItem;
        } else {
            lastSlot = -1;
        }
        blockCount = -1;
        rotationTick = 3;
        yaw = -180.0F;
        pitch = 0.0F;
        canRotate = false;
        towerTick = 0;
        towerDelay = 0;
        towering = false;
    }

    @Override
    public void onDisabled() {
        if (mc.thePlayer != null && lastSlot != -1) {
            mc.thePlayer.inventory.currentItem = lastSlot;
        }
    }

    public static class BlockData {
        private final BlockPos blockPos;
        private final EnumFacing facing;

        public BlockData(BlockPos blockPos, EnumFacing facing) {
            this.blockPos = blockPos;
            this.facing = facing;
        }

        public BlockPos blockPos() {
            return blockPos;
        }

        public EnumFacing facing() {
            return facing;
        }
    }
}