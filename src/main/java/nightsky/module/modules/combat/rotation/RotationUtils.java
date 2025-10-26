package nightsky.module.modules.combat.rotation;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.Random;

public class RotationUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Random random = new Random();

    public static float getAngleDiff(float startAngle, float endAngle) {
        return wrapAngleTo180(endAngle - startAngle);
    }

    public static float wrapAngleTo180(float angle) {
        angle %= 360.0f;
        if (angle >= 180.0f) {
            angle -= 360.0f;
        }
        if (angle < -180.0f) {
            angle += 360.0f;
        }
        return angle;
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float randomFloat(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    public static int randomInt(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    public static float abs(float value) {
        return Math.abs(value);
    }

    public static float negTo0(float value) {
        return Math.max(0f, value);
    }

    public static float pow(float base, float exponent) {
        return (float) Math.pow(base, exponent);
    }

    public static float sin(float value) {
        return (float) Math.sin(value);
    }

    public static float cos(float value) {
        return (float) Math.cos(value);
    }

    public static float log10(float value) {
        return (float) Math.log10(value);
    }

    public static float log2(float value) {
        return (float) (Math.log(value) / Math.log(2));
    }

    public static double getDistanceToEntity(Entity entity) {
        return mc.thePlayer.getDistanceToEntity(entity);
    }

    public static Rotation getRotationToEntity(Entity entity) {
        Vec3 eyePos = mc.thePlayer.getPositionEyes(1.0f);
        Vec3 targetPos = entity.getPositionVector().addVector(0, entity.getEyeHeight() / 2.0, 0);
        
        double deltaX = targetPos.xCoord - eyePos.xCoord;
        double deltaY = targetPos.yCoord - eyePos.yCoord;
        double deltaZ = targetPos.zCoord - eyePos.zCoord;
        
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        
        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, distance));
        
        return new Rotation(yaw, pitch);
    }

    public static Rotation getRotationToBox(AxisAlignedBB box) {
        Vec3 eyePos = mc.thePlayer.getPositionEyes(1.0f);
        
        double centerX = (box.minX + box.maxX) / 2.0;
        double centerY = (box.minY + box.maxY) / 2.0;
        double centerZ = (box.minZ + box.maxZ) / 2.0;
        
        double deltaX = centerX - eyePos.xCoord;
        double deltaY = centerY - eyePos.yCoord;
        double deltaZ = centerZ - eyePos.zCoord;
        
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        
        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, distance));
        
        return new Rotation(yaw, pitch);
    }

    public static Rotation getCurrentRotation() {
        return new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
    }

    public static void setRotation(Rotation rotation) {
        mc.thePlayer.rotationYaw = rotation.yaw;
        mc.thePlayer.rotationPitch = rotation.pitch;
    }

    public static Rotation getDiffRotation(Rotation startRotation, Rotation endRotation) {
        Rotation correctedSource = startRotation.getCorrectRotation();
        Rotation correctedDestination = endRotation.getCorrectRotation();
        return correctedDestination.minus(correctedSource).getCorrectRotation();
    }
}
