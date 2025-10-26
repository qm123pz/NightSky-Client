package nightsky.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

public class IpProtected {
    public static boolean containsPattern(String pattern) {
        String serverIP = getServerIP();
        if (serverIP == null) return false;

        return serverIP.contains(pattern);
    }
    private static String getServerIP() {
        Minecraft mc = Minecraft.getMinecraft();
        ServerData serverData = mc.getCurrentServerData();
        return serverData != null ? serverData.serverIP : null;
    }
}