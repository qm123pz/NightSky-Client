package nightsky.powershell.shells;

import nightsky.NightSky;
import nightsky.powershell.PowerShell;
import nightsky.module.Module;
import nightsky.util.ChatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class Binds extends PowerShell {
    public Binds() {
        super(new ArrayList<>(Arrays.asList("BindList")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        if (args.size() == 1 || (args.size() == 2 && (args.get(1).equalsIgnoreCase("l") || args.get(1).equalsIgnoreCase("list")))) {
            showAllBinds();
        } else if (args.size() == 2) {
            showModuleBind(args.get(1));
        } else {
            showUsage(args.get(0));
        }
    }

    private void showAllBinds() {
        List<Module> boundModules = NightSky.moduleManager.modules.values()
                .stream()
                .filter(module -> module.getKey() != 0)
                .collect(Collectors.toList());

        List<Module> unboundModules = NightSky.moduleManager.modules.values()
                .stream()
                .filter(module -> module.getKey() == 0)
                .collect(Collectors.toList());

        if (boundModules.isEmpty() && unboundModules.isEmpty()) {
            ChatUtil.sendFormatted(String.format("%sNo modules found&r", NightSky.clientName));
            return;
        }

        if (!boundModules.isEmpty()) {
            ChatUtil.sendFormatted(String.format("%s&aBound Modules (&o%d&r&a):&r", NightSky.clientName, boundModules.size()));
            for (Module module : boundModules) {
                String keyName = nightsky.util.KeyBindUtil.getKeyName(module.getKey());
                ChatUtil.sendFormatted(String.format("  %s»&r %s &7→ &e[%s]&r", 
                    module.isHidden() ? "&8" : "&7", 
                    module.formatModule(), 
                    keyName));
            }
        }

        if (!unboundModules.isEmpty()) {
            ChatUtil.sendFormatted(String.format("%s&cUnbound Modules (&o%d&r&c):&r", NightSky.clientName, unboundModules.size()));
            for (Module module : unboundModules) {
                ChatUtil.sendFormatted(String.format("  %s»&r %s &7→ &c[None]&r", 
                    module.isHidden() ? "&8" : "&7", 
                    module.formatModule()));
            }
        }
        int totalModules = boundModules.size() + unboundModules.size();
        ChatUtil.sendFormatted(String.format("%s&7Total: &o%d&r&7 modules, &a%d&r&7 bound, &c%d&r&7 unbound&r", 
            NightSky.clientName, totalModules, boundModules.size(), unboundModules.size()));
    }

    private void showModuleBind(String moduleName) {
        Module module = NightSky.moduleManager.getModule(moduleName);
        if (module == null) {
            ChatUtil.sendFormatted(String.format("%sModule not found (&o%s&r)&r", NightSky.clientName, moduleName));
            return;
        }

        if (module.getKey() == 0) {
            ChatUtil.sendFormatted(String.format("%s&o%s&r is not bound to any key&r", NightSky.clientName, module.getName()));
        } else {
            String keyName = nightsky.util.KeyBindUtil.getKeyName(module.getKey());
            ChatUtil.sendFormatted(String.format("%s&o%s&r is bound to &e[%s]&r", NightSky.clientName, module.getName(), keyName));
        }
    }

    private void showUsage(String command) {
        ChatUtil.sendFormatted(
                String.format(
                        "%sUsage: .%s &o[list]&r | .%s <&omodule&r>&r",
                        NightSky.clientName,
                        command.toLowerCase(Locale.ROOT),
                        command.toLowerCase(Locale.ROOT)
                )
        );
        ChatUtil.sendFormatted(
                String.format(
                        "%sShows all keybinds or specific module bind&r",
                        NightSky.clientName
                )
        );
    }
}