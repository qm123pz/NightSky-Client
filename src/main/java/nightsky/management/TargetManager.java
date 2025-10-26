package nightsky.management;

import nightsky.enums.ChatColors;

import java.awt.*;
import java.io.File;

public class TargetManager extends PlayerFileManager {
    public TargetManager() {
        super(new File("./NightSky/", "enemies.txt"), new Color(ChatColors.DARK_RED.toAwtColor()));
    }
}
