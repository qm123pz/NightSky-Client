package nightsky.ui.mainmenu.altmanager;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.lang.reflect.Field;

public class SessionUtils {
    
    public static void setSession(Session session) {
        try {
            Field sessionField = Minecraft.class.getDeclaredField("session");
            sessionField.setAccessible(true);
            sessionField.set(Minecraft.getMinecraft(), session);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
