package nightsky.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;

public class SoundUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void playSound(String soundName) {
        SoundHandler soundHandler = mc.getSoundHandler();
        if (soundHandler != null) {
            PositionedSoundRecord positionedSoundRecord = PositionedSoundRecord.create(new ResourceLocation(soundName));
            soundHandler.playSound(positionedSoundRecord);
        }
    }
    
    public static void playSound(ResourceLocation location, float volume) {
        new Thread(() -> {
            try {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(
                    mc.getResourceManager().getResource(location).getInputStream()
                );
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(bufferedInputStream);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float range = gainControl.getMaximum() - gainControl.getMinimum();
                float gain = (range * volume) + gainControl.getMinimum();
                gainControl.setValue(gain);
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}