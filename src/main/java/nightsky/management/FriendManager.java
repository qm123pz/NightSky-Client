package nightsky.management;

import nightsky.enums.ChatColors;

import java.awt.*;
import java.io.File;

public class FriendManager extends PlayerFileManager {
    public FriendManager() {
        super(new File("./NightSky/", "friends.txt"), new Color(ChatColors.DARK_GREEN.toAwtColor()));
    }
}
