package nightsky.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public class RaytraceUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static List<EntityLivingBase> getEntitiesInRay(double range) {
        List<EntityLivingBase> entities = new ArrayList<>();
        
        if (mc.thePlayer == null || mc.theWorld == null) {
            return entities;
        }

        Vec3 eyePos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
        
        float yaw = mc.thePlayer.rotationYaw;
        float pitch = mc.thePlayer.rotationPitch;
        
        float yawRad = yaw * (float) Math.PI / 180.0F;
        float pitchRad = pitch * (float) Math.PI / 180.0F;
        
        Vec3 lookVec = new Vec3(
            -MathHelper.sin(yawRad) * MathHelper.cos(pitchRad),
            -MathHelper.sin(pitchRad),
            MathHelper.cos(yawRad) * MathHelper.cos(pitchRad)
        );
        
        Vec3 endPos = eyePos.addVector(lookVec.xCoord * range, lookVec.yCoord * range, lookVec.zCoord * range);

        AxisAlignedBB rayBB = new AxisAlignedBB(
            Math.min(eyePos.xCoord, endPos.xCoord) - 1.0,
            Math.min(eyePos.yCoord, endPos.yCoord) - 1.0,
            Math.min(eyePos.zCoord, endPos.zCoord) - 1.0,
            Math.max(eyePos.xCoord, endPos.xCoord) + 1.0,
            Math.max(eyePos.yCoord, endPos.yCoord) + 1.0,
            Math.max(eyePos.zCoord, endPos.zCoord) + 1.0
        );
        
        List<Entity> nearbyEntities = mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.thePlayer, rayBB);
        
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof EntityLivingBase)) {
                continue;
            }
            
            EntityLivingBase livingEntity = (EntityLivingBase) entity;

            double distance = mc.thePlayer.getDistanceToEntity(entity);
            if (distance > range) {
                continue;
            }

            AxisAlignedBB entityBB = entity.getEntityBoundingBox().expand(0.3, 0.3, 0.3);
            if (entityBB.calculateIntercept(eyePos, endPos) != null) {
                entities.add(livingEntity);
            }
        }

        entities.sort((e1, e2) -> Double.compare(mc.thePlayer.getDistanceToEntity(e1), mc.thePlayer.getDistanceToEntity(e2)));
        
        return entities;
    }
}
