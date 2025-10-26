package nightsky.powershell.shells;

import nightsky.NightSky;
import nightsky.powershell.PowerShell;
import nightsky.util.ChatUtil;

import java.util.ArrayList;
import java.util.Arrays;

public class Help extends PowerShell {
    public Help() {
        super(new ArrayList<>(Arrays.asList("Help")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        if (!NightSky.moduleManager.modules.isEmpty()) {
            ChatUtil.sendFormatted(String.format("%sCommands:&r", NightSky.clientName));
            for (PowerShell powerShell : NightSky.handler.powerShells) {
                if (!(powerShell instanceof Module)) {
                    ChatUtil.sendFormatted(String.format("&7»&r .%s&r", String.join(" &7/&r .", powerShell.names)));
                }
            }
        }
    }
}
