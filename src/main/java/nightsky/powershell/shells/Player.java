package nightsky.powershell.shells;

import nightsky.NightSky;
import nightsky.powershell.PowerShell;
import nightsky.enums.ChatColors;
import nightsky.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.ArrayList;
import java.util.Arrays;

public class Player extends PowerShell {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public Player() {
        super(new ArrayList<>(Arrays.asList("PlayerList")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        ArrayList<String> players = new ArrayList<>();
        for (NetworkPlayerInfo playerInfo : mc.getNetHandler().getPlayerInfoMap()) {
            players.add(playerInfo.getGameProfile().getName().replace("§", "&"));
        }
        if (players.isEmpty()) {
            ChatUtil.sendFormatted(String.format("%sNo players&r", NightSky.clientName));
        } else {
            ChatUtil.sendRaw(
                    String.format(
                            ChatColors.formatColor("%sPlayers:&r %s"),
                            ChatColors.formatColor(NightSky.clientName),
                            String.join(", ", players)
                    )
            );
        }
    }
}
