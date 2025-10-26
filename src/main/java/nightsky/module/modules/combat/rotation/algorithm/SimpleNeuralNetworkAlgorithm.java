package nightsky.module.modules.combat.rotation.algorithm;

import nightsky.module.modules.combat.rotation.TurnSpeed;
import nightsky.module.modules.combat.rotation.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class SimpleNeuralNetworkAlgorithm implements TurnSpeedAlgorithm {
    private static final Minecraft mc = Minecraft.getMinecraft();
    
    private TurnSpeed neuralSpeed = new TurnSpeed(0f, 0f);
    private NeuralNetworkData yawData = new NeuralNetworkData();
    private NeuralNetworkData pitchData = new NeuralNetworkData();
    private TurnSpeed totalSpeed = new TurnSpeed(0f, 0f);
    
    private SmoothRandom smoothRandomPitch1 = new SmoothRandom();
    private SmoothRandom smoothRandomPitch2 = new SmoothRandom();
    private SmoothRandom smoothRandomPitch3 = new SmoothRandom();
    private SmoothRandom smoothRandomPitch4 = new SmoothRandom();
    
    private Entity target;
    private boolean hittable = false;

    public void setTarget(Entity target) {
        this.target = target;
    }

    public void setHittable(boolean hittable) {
        this.hittable = hittable;
    }

    @Override
    public TurnSpeed calculateTurnSpeed(float diffYaw, float diffPitch, float maxDiffYaw, float maxDiffPitch) {
        updateNeuralNetworkYaw(diffYaw, maxDiffYaw);
        updateNeuralNetworkPitch(diffPitch, maxDiffPitch);
        
        totalSpeed = neuralSpeed;
        
        return totalSpeed;
    }

    private void updateNeuralNetworkYaw(float diffYaw, float maxDiffYaw) {
        float targetYaw = target != null ? RotationUtils.getRotationToEntity(target).yaw : mc.thePlayer.rotationYaw;
        float currentYaw = mc.thePlayer.rotationYaw;
        float visualYaw = RotationUtils.wrapAngleTo180(currentYaw);
        
        yawData.currDiffHistory.add(RotationUtils.abs(RotationUtils.getAngleDiff(targetYaw, visualYaw)));
        if (yawData.currDiffHistory.size() > 100) yawData.currDiffHistory.remove(0);
        
        float lastDiff = yawData.currDiffHistory.size() > 1 ? 
            yawData.currDiffHistory.get(yawData.currDiffHistory.size() - 1) - yawData.currDiffHistory.get(yawData.currDiffHistory.size() - 2) : 0f;
        yawData.currDiffDiffHistory.add(lastDiff);
        if (yawData.currDiffDiffHistory.size() > 100) yawData.currDiffDiffHistory.remove(0);
        
        yawData.maxDiffHistory.add(maxDiffYaw);
        if (yawData.maxDiffHistory.size() > 100) yawData.maxDiffHistory.remove(0);
        
        double dist = target != null ? RotationUtils.getDistanceToEntity(target) : 4.0;
        yawData.distHistory.add(dist);
        if (yawData.distHistory.size() > 100) yawData.distHistory.remove(0);
        
        yawData.hittableHistory.add(hittable);
        if (yawData.hittableHistory.size() > 100) yawData.hittableHistory.remove(0);
        
        yawData.totalSpeedHistory.add(totalSpeed.yaw);
        if (yawData.totalSpeedHistory.size() > 100) yawData.totalSpeedHistory.remove(0);
        
        yawData.neuralSpeedHistory.add(neuralSpeed.yaw);
        if (yawData.neuralSpeedHistory.size() > 100) yawData.neuralSpeedHistory.remove(0);
        
        yawData.currAngleHistory.add(visualYaw);
        if (yawData.currAngleHistory.size() > 100) yawData.currAngleHistory.remove(0);
        
        float lastAngleDiff = yawData.currAngleHistory.size() > 1 ? 
            RotationUtils.abs(RotationUtils.wrapAngleTo180(yawData.currAngleHistory.get(yawData.currAngleHistory.size() - 1) - 
            yawData.currAngleHistory.get(yawData.currAngleHistory.size() - 2))) : 0f;
        yawData.currAngleDiffHistory.add(lastAngleDiff);
        if (yawData.currAngleDiffHistory.size() > 100) yawData.currAngleDiffHistory.remove(0);
        
        processYawNeuralNodes();
    }

    private void processYawNeuralNodes() {
        List<Float> var1 = yawData.currDiffHistory;
        List<Float> var2 = yawData.currDiffDiffHistory;
        List<Float> var3 = yawData.maxDiffHistory;
        List<Double> var4 = yawData.distHistory;
        List<Boolean> var5 = yawData.hittableHistory;
        List<Float> var6 = yawData.totalSpeedHistory;
        List<Float> var7 = yawData.neuralSpeedHistory;
        List<Float> var8 = yawData.currAngleHistory;
        List<Float> var9 = yawData.currAngleDiffHistory;
        
        // NODE - 长极差追踪
        if (var5.size() >= 4) {
            boolean allFalse = true;
            int startIdx = Math.max(0, var5.size() - 3 - RotationUtils.randomInt(0, 1));
            for (int i = startIdx; i < var5.size(); i++) {
                if (var5.get(i)) {
                    allFalse = false;
                    break;
                }
            }
            
            float maxDiffChange = var3.size() >= 2 ? 
                RotationUtils.abs(var3.get(var3.size() - 1) - var3.get(var3.size() - 2)) : 0f;
            float currDiff = var1.size() > 0 ? var1.get(var1.size() - 1) : 0f;
            
            if ((allFalse || maxDiffChange >= 90f) && neuralSpeed.yaw < currDiff * 0.7f) {
                neuralSpeed.yaw += currDiff * (0.7f + RotationUtils.randomFloat(-0.05f, 0.1f));
            }
            
            boolean allTrue = true;
            startIdx = Math.max(0, var5.size() - 2 - RotationUtils.randomInt(0, 1));
            for (int i = startIdx; i < var5.size(); i++) {
                if (!var5.get(i)) {
                    allTrue = false;
                    break;
                }
            }
            
            if (allTrue && maxDiffChange < 30f && neuralSpeed.yaw > 45f) {
                neuralSpeed.yaw -= neuralSpeed.yaw * (0.85f + RotationUtils.randomFloat(-0.05f, 0.08f));
            }
        }
        
        // NODE - 短极差追踪
        if (var5.size() >= 5 && var1.size() >= 2 && var7.size() >= 2 && var2.size() >= 2) {
            boolean allTrue = true;
            for (int i = Math.max(0, var5.size() - 4); i < var5.size(); i++) {
                if (!var5.get(i)) {
                    allTrue = false;
                    break;
                }
            }
            
            float avgDiff = (var1.get(var1.size() - 1) + var1.get(var1.size() - 2)) / 2f;
            float avgNeuralSpeed = (var7.get(var7.size() - 1) + var7.get(var7.size() - 2)) / 2f;
            float avgDiffDiff = (var2.get(var2.size() - 1) + var2.get(var2.size() - 2)) / 2f;
            float currDiff = var1.get(var1.size() - 1);
            
            if (allTrue && avgDiff < 30f && avgNeuralSpeed <= currDiff * RotationUtils.randomFloat(0f, 0.3f) * 0.5f && avgDiffDiff <= 0f) {
                neuralSpeed.yaw += currDiff * (1f + RotationUtils.randomFloat(-0.2f, 0.2f));
            }
        }
        
        // NODE - 过欠调整
        if (var2.size() >= 1 && var5.size() >= 2) {
            boolean allNegative = true;
            int takeCount = Math.min(var2.size(), RotationUtils.randomInt(1, 1));
            for (int i = Math.max(0, var2.size() - takeCount); i < var2.size(); i++) {
                if (var2.get(i) >= 0f) {
                    allNegative = false;
                    break;
                }
            }
            
            boolean allTrue = true;
            int startIdx = Math.max(0, var5.size() - RotationUtils.randomInt(0, 1));
            for (int i = startIdx; i < var5.size(); i++) {
                if (!var5.get(i)) {
                    allTrue = false;
                    break;
                }
            }
            
            if (allNegative && allTrue && neuralSpeed.yaw > RotationUtils.randomFloat(8.2f, 22.1f)) {
                neuralSpeed.yaw -= RotationUtils.randomFloat(3f, 7.2f);
            }
            
            boolean allPositive = true;
            for (int i = Math.max(0, var2.size() - takeCount); i < var2.size(); i++) {
                if (var2.get(i) <= 0f) {
                    allPositive = false;
                    break;
                }
            }
            
            if (allPositive && neuralSpeed.yaw < RotationUtils.randomFloat(14.2f, 28.1f)) {
                neuralSpeed.yaw += RotationUtils.randomFloat(5f, 10f);
            }
        }
        
        // NODE - 距离调整
        if (var4.size() >= 4) {
            double avgDist = 0;
            for (int i = Math.max(0, var4.size() - 3); i < var4.size(); i++) {
                avgDist += var4.get(i);
            }
            avgDist /= Math.min(4, var4.size());
            
            if (avgDist >= 0.0 && avgDist <= 6.0) {
                double currentDist = var4.get(var4.size() - 1);
                double factor = pointedCurve(currentDist, 0.0, 1.0, 6.0, 0.7, 0.85);
                neuralSpeed.yaw *= (float) factor;
            }
        }
        
        // NODE - 必然随机调整
        if (neuralSpeed.yaw >= 2f) {
            float adjustment = RotationUtils.randomFloat(-RotationUtils.log10(neuralSpeed.yaw - 1f), RotationUtils.log10(neuralSpeed.yaw - 1f));
            adjustment /= RotationUtils.randomFloat(1f, 2f);
            if (RotationUtils.randomInt(1, 100) > 65) adjustment = 0f;
            neuralSpeed.yaw += adjustment;
        }
        
        // NODE - 极值调整
        if (var5.size() >= 5 && var1.size() >= 5) {
            boolean allTrue = true;
            for (int i = Math.max(0, var5.size() - 4); i < var5.size(); i++) {
                if (!var5.get(i)) {
                    allTrue = false;
                    break;
                }
            }
            
            float avgDiff = 0;
            for (int i = Math.max(0, var1.size() - 4); i < var1.size(); i++) {
                avgDiff += var1.get(i);
            }
            avgDiff /= Math.min(5, var1.size());
            
            if (allTrue && avgDiff <= 30f && neuralSpeed.yaw > RotationUtils.randomFloat(18f, 30f)) {
                neuralSpeed.yaw *= RotationUtils.randomFloat(0.4f, 0.65f);
            }
            
            if (neuralSpeed.yaw != 0f && neuralSpeed.yaw < RotationUtils.randomFloat(4f, 7f) && var2.size() >= 3) {
                float avgDiffDiff = 0;
                for (int i = Math.max(0, var2.size() - 2); i < var2.size(); i++) {
                    avgDiffDiff += var2.get(i);
                }
                avgDiffDiff /= Math.min(3, var2.size());
                
                if (avgDiffDiff < 0f) {
                    neuralSpeed.yaw += RotationUtils.randomFloat(3f, 5f);
                }
            }
        }
        
        // NODE - 惰性调整
        if (var5.size() >= 7 && var1.size() >= 9 && var8.size() >= 8 && var9.size() >= 8) {
            boolean allTrue = true;
            for (int i = Math.max(0, var5.size() - 6); i < var5.size(); i++) {
                if (!var5.get(i)) {
                    allTrue = false;
                    break;
                }
            }
            
            float avgDiff = 0;
            for (int i = Math.max(0, var1.size() - 8); i < var1.size(); i++) {
                avgDiff += var1.get(i);
            }
            avgDiff /= Math.min(9, var1.size());
            
            float angleDiff = var8.size() >= 8 ? 
                RotationUtils.abs(var8.get(var8.size() - 1) - var8.get(var8.size() - 8)) : 0f;
            
            float sumAngleDiff = 0;
            for (int i = Math.max(0, var9.size() - 7); i < var9.size(); i++) {
                sumAngleDiff += var9.get(i);
            }
            
            if (allTrue && avgDiff < 5f && angleDiff < 3f && sumAngleDiff < RotationUtils.randomFloat(3f, 5f)) {
                neuralSpeed.yaw = 0f;
            }
        }
        
        // NODE - 边界调整
        neuralSpeed.yaw = RotationUtils.clamp(neuralSpeed.yaw, 0f, 180f);
    }

    private void updateNeuralNetworkPitch(float diffPitch, float maxDiffPitch) {
        pitchData.currDiffHistory.add(diffPitch);
        if (pitchData.currDiffHistory.size() > 100) pitchData.currDiffHistory.remove(0);
        
        float lastDiff = pitchData.currDiffHistory.size() > 1 ? 
            pitchData.currDiffHistory.get(pitchData.currDiffHistory.size() - 1) - pitchData.currDiffHistory.get(pitchData.currDiffHistory.size() - 2) : 0f;
        pitchData.currDiffDiffHistory.add(lastDiff);
        if (pitchData.currDiffDiffHistory.size() > 100) pitchData.currDiffDiffHistory.remove(0);
        
        pitchData.maxDiffHistory.add(maxDiffPitch);
        if (pitchData.maxDiffHistory.size() > 100) pitchData.maxDiffHistory.remove(0);
        
        double dist = target != null ? RotationUtils.getDistanceToEntity(target) : 4.0;
        pitchData.distHistory.add(dist);
        if (pitchData.distHistory.size() > 100) pitchData.distHistory.remove(0);
        
        pitchData.hittableHistory.add(hittable);
        if (pitchData.hittableHistory.size() > 100) pitchData.hittableHistory.remove(0);
        
        pitchData.totalSpeedHistory.add(totalSpeed.pitch);
        if (pitchData.totalSpeedHistory.size() > 100) pitchData.totalSpeedHistory.remove(0);
        
        pitchData.neuralSpeedHistory.add(neuralSpeed.pitch);
        if (pitchData.neuralSpeedHistory.size() > 100) pitchData.neuralSpeedHistory.remove(0);
        
        float visualPitch = RotationUtils.wrapAngleTo180(mc.thePlayer.rotationPitch);
        pitchData.currAngleHistory.add(visualPitch);
        if (pitchData.currAngleHistory.size() > 100) pitchData.currAngleHistory.remove(0);
        
        float lastAngleDiff = pitchData.currAngleHistory.size() > 1 ? 
            RotationUtils.abs(RotationUtils.wrapAngleTo180(pitchData.currAngleHistory.get(pitchData.currAngleHistory.size() - 1) - 
            pitchData.currAngleHistory.get(pitchData.currAngleHistory.size() - 2))) : 0f;
        pitchData.currAngleDiffHistory.add(lastAngleDiff);
        if (pitchData.currAngleDiffHistory.size() > 100) pitchData.currAngleDiffHistory.remove(0);
        
        processPitchNeuralNodes();
    }

    private void processPitchNeuralNodes() {
        List<Float> var1 = pitchData.currDiffHistory;
        List<Float> var2 = pitchData.currDiffDiffHistory;
        List<Float> var3 = pitchData.maxDiffHistory;
        List<Double> var4 = pitchData.distHistory;
        List<Boolean> var5 = pitchData.hittableHistory;
        List<Float> var6 = pitchData.totalSpeedHistory;
        List<Float> var7 = pitchData.neuralSpeedHistory;
        List<Float> var8 = pitchData.currAngleHistory;
        List<Float> var9 = pitchData.currAngleDiffHistory;
        
        // NODE-1 - 长极差追踪
        if (var5.size() >= 4 && var3.size() >= 2 && var1.size() >= 1) {
            boolean allFalse = true;
            int startIdx = Math.max(0, var5.size() - 3 - RotationUtils.randomInt(0, 1));
            for (int i = startIdx; i < var5.size(); i++) {
                if (var5.get(i)) {
                    allFalse = false;
                    break;
                }
            }
            
            float maxDiffChange = RotationUtils.abs(var3.get(var3.size() - 1) - var3.get(var3.size() - 2));
            float currDiff = var1.get(var1.size() - 1);
            
            if ((allFalse || maxDiffChange >= 90f) && neuralSpeed.pitch < currDiff * 0.7f) {
                neuralSpeed.pitch += currDiff * RotationUtils.randomFloat(0.65f, 0.75f);
            }
            
            boolean allTrue = true;
            startIdx = Math.max(0, var5.size() - 2 - RotationUtils.randomInt(0, 1));
            for (int i = startIdx; i < var5.size(); i++) {
                if (!var5.get(i)) {
                    allTrue = false;
                    break;
                }
            }
            
            if (allTrue && maxDiffChange < 30f && neuralSpeed.pitch > 45f) {
                float lastNeuralSpeed = var7.size() > 0 ? var7.get(var7.size() - 1) : 0f;
                neuralSpeed.pitch -= lastNeuralSpeed * RotationUtils.randomFloat(0.8f, 0.97f);
            }
        }
        
        // NODE-2 - 短极差追踪
        if (var5.size() >= 5 && var1.size() >= 2 && var7.size() >= 2 && var2.size() >= 2) {
            boolean allTrue = true;
            for (int i = Math.max(0, var5.size() - 4); i < var5.size(); i++) {
                if (!var5.get(i)) {
                    allTrue = false;
                    break;
                }
            }
            
            float avgDiff = (var1.get(var1.size() - 1) + var1.get(var1.size() - 2)) / 2f;
            float avgNeuralSpeed = (var7.get(var7.size() - 1) + var7.get(var7.size() - 2)) / 2f;
            float avgDiffDiff = (var2.get(var2.size() - 1) + var2.get(var2.size() - 2)) / 2f;
            float currDiff = var1.get(var1.size() - 1);
            
            if (allTrue && avgDiff < 30f && avgNeuralSpeed <= currDiff * RotationUtils.randomFloat(0f, 0.3f) * 0.5f && avgDiffDiff <= 0f) {
                neuralSpeed.pitch += currDiff * RotationUtils.randomFloat(1f, 1.4f);
            }
        }
        
        // NODE-3 - 过欠调整
        if (var2.size() >= 2 && var7.size() >= 3) {
            float lastDiffDiff = var2.get(var2.size() - 1);
            float secondLastDiffDiff = var2.get(var2.size() - 2);
            
            float avgNeuralSpeed = 0;
            int count = Math.min(3, var7.size());
            for (int i = var7.size() - count; i < var7.size(); i++) {
                avgNeuralSpeed += var7.get(i);
            }
            avgNeuralSpeed /= count;
            
            if (lastDiffDiff > 0f && secondLastDiffDiff > 0f && avgNeuralSpeed < 8f) {
                int tick = RotationUtils.randomInt(0, 3);
                neuralSpeed.pitch += smoothRandomPitch1.randomFrom(RotationUtils.randomFloat(0f, 2f), RotationUtils.randomFloat(6f, 10f), tick);
            }
            
            if (lastDiffDiff < 0f && secondLastDiffDiff < 0f && avgNeuralSpeed > 8f) {
                int tick = RotationUtils.randomInt(0, 3);
                neuralSpeed.pitch -= smoothRandomPitch2.randomFrom(RotationUtils.randomFloat(0f, 3f), RotationUtils.randomFloat(5f, 8f), tick);
            }
        }
        
        // NODE-4 - 距离调整
        if (var4.size() >= 2 && var6.size() >= 7 && var2.size() >= 2) {
            double lastDistDiff = var4.get(var4.size() - 1) - var4.get(var4.size() - 2);
            
            float avgTotalSpeed = 0;
            for (int i = Math.max(0, var6.size() - 6); i < var6.size(); i++) {
                avgTotalSpeed += var6.get(i);
            }
            avgTotalSpeed /= Math.min(7, var6.size());
            
            float lastDiffDiff = var2.get(var2.size() - 1);
            float secondLastDiffDiff = var2.get(var2.size() - 2);
            float diffDiffChange = lastDiffDiff - secondLastDiffDiff;
            
            if (lastDistDiff < 0.0 && avgTotalSpeed < 15f && diffDiffChange > 0f) {
                int tick = RotationUtils.randomInt(0, 3);
                neuralSpeed.pitch += smoothRandomPitch3.randomFrom(RotationUtils.randomFloat(1f, 4f), RotationUtils.randomFloat(4f, 6f), tick);
            }
            
            if (lastDistDiff > 0.0 && avgTotalSpeed > 15f && diffDiffChange < 0f) {
                int tick = RotationUtils.randomInt(0, 3);
                neuralSpeed.pitch -= smoothRandomPitch4.randomFrom(RotationUtils.randomFloat(1f, 3f), RotationUtils.randomFloat(3f, 6f), tick);
            }
        }
        
        // NODE-5 - 必然随机调整
        if (neuralSpeed.pitch > 0f) {
            float adjustment = RotationUtils.randomFloat(0f, RotationUtils.log2(neuralSpeed.pitch + 1f));
            if (RotationUtils.randomInt(1, 100) <= 50) adjustment = -adjustment;
            neuralSpeed.pitch += adjustment;
        }
        
        // NODE-6 - 短零值调整
        if (neuralSpeed.pitch < RotationUtils.randomFloat(0f, 2f) && var4.size() >= 4 && var7.size() >= 21) {
            double avgDist = 0;
            for (int i = Math.max(0, var4.size() - 3); i < var4.size(); i++) {
                avgDist += var4.get(i);
            }
            avgDist /= Math.min(4, var4.size());
            
            if (avgDist < 3.5 + RotationUtils.randomFloat(-1.0f, 1.0f)) {
                float avgNeuralSpeed = 0;
                for (int i = Math.max(0, var7.size() - 20); i < var7.size(); i++) {
                    avgNeuralSpeed += var7.get(i);
                }
                avgNeuralSpeed /= Math.min(21, var7.size());
                
                float baseValue = avgNeuralSpeed * RotationUtils.randomFloat(0.25f, 0.75f);
                neuralSpeed.pitch += RotationUtils.clamp(baseValue, RotationUtils.randomFloat(1.5f, 3.5f), RotationUtils.randomFloat(7f, 9f));
            }
        }
        
        neuralSpeed.pitch = RotationUtils.clamp(neuralSpeed.pitch, 0f, 180f);
    }

    private double pointedCurve(double x, double x1, double y1, double x2, double y2, double exp) {
        double t = (x - x1) / (x2 - x1);
        t = Math.pow(t, exp);
        return y1 + t * (y2 - y1);
    }

    private static class SmoothRandom {
        private float lastValue = 0f;
        private int lastTick = -1;

        public float randomFrom(float min, float max, int tick) {
            if (tick != lastTick) {
                lastValue = RotationUtils.randomFloat(min, max);
                lastTick = tick;
            }
            return lastValue;
        }
    }

    private static class NeuralNetworkData {
        public List<Float> currDiffHistory = new ArrayList<>();
        public List<Float> currDiffDiffHistory = new ArrayList<>();
        public List<Float> maxDiffHistory = new ArrayList<>();
        public List<Double> distHistory = new ArrayList<>();
        public List<Boolean> hittableHistory = new ArrayList<>();
        public List<Float> totalSpeedHistory = new ArrayList<>();
        public List<Float> neuralSpeedHistory = new ArrayList<>();
        public List<Float> currAngleHistory = new ArrayList<>();
        public List<Float> currAngleDiffHistory = new ArrayList<>();

        public NeuralNetworkData() {
            currDiffHistory.add(0f);
            currDiffDiffHistory.add(0f);
            maxDiffHistory.add(0f);
            distHistory.add(Double.MAX_VALUE);
            hittableHistory.add(false);
            totalSpeedHistory.add(0f);
            neuralSpeedHistory.add(0f);
            currAngleHistory.add(0f);
            currAngleDiffHistory.add(0f);
        }
    }
}
