package nightsky.powershell.shells;

import nightsky.NightSky;
import nightsky.powershell.PowerShell;
import nightsky.module.Module;
import nightsky.util.ChatUtil;
import nightsky.util.KeyBindUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class Bind extends PowerShell {
    public Bind() {
        super(new ArrayList<>(Arrays.asList("Bind")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        if (args.size() < 3) {
            if (args.size() == 2 && (args.get(1).equalsIgnoreCase("l") || args.get(1).equalsIgnoreCase("list"))) {
                List<Module> modules = NightSky.moduleManager.modules.values().stream().filter(module -> module.getKey() != 0).collect(Collectors.toList());
                if (modules.isEmpty()) {
                    ChatUtil.sendFormatted(String.format("%sNo binds&r", NightSky.clientName));
                } else {
                    ChatUtil.sendFormatted(String.format("%sBinds:&r", NightSky.clientName));
                    for (Module module : modules) {
                        ChatUtil.sendFormatted(String.format("%s»&r %s&r", module.isHidden() ? "&8" : "&7", module.formatModule()));
                    }
                }
            } else {
                ChatUtil.sendFormatted(
                        String.format(
                                "%sUsage: .%s <&omodule&r> <&okey&r>&r | .%s <&omodule&r> &onone&r | .%s &olist&r",
                                NightSky.clientName,
                                args.get(0).toLowerCase(Locale.ROOT),
                                args.get(0).toLowerCase(Locale.ROOT),
                                args.get(0).toLowerCase(Locale.ROOT)
                        )
                );
            }
        } else {
            String keyInput = args.get(2).toUpperCase();
            int keyIndex = 0;

            if (keyInput.equalsIgnoreCase("NONE") || keyInput.equalsIgnoreCase("NULL") || keyInput.equalsIgnoreCase("0")) {
                keyIndex = 0;
            } else {
                keyIndex = Keyboard.getKeyIndex(keyInput);

                if (keyIndex == 0) {
                    int buttonIndex = Mouse.getButtonIndex(keyInput);
                    if (buttonIndex != -1) {
                        keyIndex = buttonIndex - 100;
                    }
                }
            }
                if (!args.get(1).equals("*")) {
                    Module module = NightSky.moduleManager.getModule(args.get(1));
                    if (module == null) {
                        ChatUtil.sendFormatted(String.format("%sModule not found (&o%s&r)&r", NightSky.clientName, args.get(1)));
                    } else {
                        module.setKey(keyIndex);
                        if (keyIndex == 0) {
                            ChatUtil.sendFormatted(
                                    String.format("%sUnbind &o%s&r", NightSky.clientName, module.getName())
                            );
                        } else {
                            ChatUtil.sendFormatted(
                                    String.format("%sBound &o%s&r to &l[%s]&r", NightSky.clientName, module.getName(), KeyBindUtil.getKeyName(keyIndex))
                            );
                        }
                    }
                } else {
                    for (Module module : NightSky.moduleManager.modules.values()) {
                        module.setKey(keyIndex);
                    }
                    if (keyIndex == 0) {
                        ChatUtil.sendFormatted(
                                String.format("%sUnbind all modules&r", NightSky.clientName)
                        );
                    } else {
                        ChatUtil.sendFormatted(
                                String.format("%sBind all modules to &l[%s]&r", NightSky.clientName, KeyBindUtil.getKeyName(keyIndex))
                    );
                }
            }
        }
    }
}