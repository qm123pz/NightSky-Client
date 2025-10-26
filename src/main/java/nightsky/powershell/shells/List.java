package nightsky.powershell.shells;

import nightsky.NightSky;
import nightsky.powershell.PowerShell;
import nightsky.module.Module;
import nightsky.util.ChatUtil;

import java.util.ArrayList;
import java.util.Arrays;

public class List extends PowerShell {
    public List() {
        super(new ArrayList<>(Arrays.asList("List")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        if (!NightSky.moduleManager.modules.isEmpty()) {
            ChatUtil.sendFormatted(String.format("%sModules:&r", NightSky.clientName));
            for (Module module : NightSky.moduleManager.modules.values()) {
                ChatUtil.sendFormatted(String.format("%s»&r %s&r", module.isHidden() ? "&8" : "&7", module.formatModule()));
            }
        }
    }
}
