package nightsky.module.modules.misc;

import nightsky.enums.ChatColors;
import nightsky.module.Module;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.TextValue;
import net.minecraft.client.Minecraft;

import java.util.regex.Matcher;

public class NickHider extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final TextValue protectName = new TextValue("Name", "91大神");
    public final BooleanValue scoreboard = new BooleanValue("Scoreboard", true);
    public final BooleanValue level = new BooleanValue("Level", true);

    public NickHider() {
        super("NickHider", false, true);
    }

    public String replaceNick(String input) {
        if (input != null && mc.thePlayer != null) {
            if (this.scoreboard.getValue() && input.matches("§7\\d{2}/\\d{2}/\\d{2}(?:\\d{2})?  ?§8.*")) {
                input = input.replaceAll("§8", "§8§k").replaceAll("[^\\x00-\\x7F§]", "?");
            }
            return input.replaceAll(
                    mc.thePlayer.getName(), Matcher.quoteReplacement(ChatColors.formatColor(this.protectName.getValue()))
            );
        } else {
            return input;
        }
    }
}
