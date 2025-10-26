package nightsky.powershell.shells;

import nightsky.NightSky;
import nightsky.powershell.PowerShell;
import nightsky.enums.ChatColors;
import nightsky.util.ChatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class Target extends PowerShell {
    public Target() {
        super(new ArrayList<>(Arrays.asList("Target")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        if (args.size() >= 2) {
            String subCommand = args.get(1).toLowerCase(Locale.ROOT);
            switch (subCommand) {
                case "add":
                    if (args.size() < 3) {
                        ChatUtil.sendFormatted(
                                String.format("%sUsage: .%s add <&oname&r>&r", NightSky.clientName, args.get(0).toLowerCase(Locale.ROOT))
                        );
                        return;
                    }
                    String added = NightSky.targetManager.add(args.get(2));
                    if (added == null) {
                        ChatUtil.sendFormatted(String.format("%s&o%s&r is already in your enemy list&r", NightSky.clientName, args.get(2)));
                        return;
                    }
                    ChatUtil.sendFormatted(String.format("%sAdded &o%s&r to your enemy list&r", NightSky.clientName, added));
                    return;
                case "remove":
                    if (args.size() < 3) {
                        ChatUtil.sendFormatted(
                                String.format("%sUsage: .%s remove <&oname&r>&r", NightSky.clientName, args.get(0).toLowerCase(Locale.ROOT))
                        );
                        return;
                    }
                    String removed = NightSky.targetManager.remove(args.get(2));
                    if (removed == null) {
                        ChatUtil.sendFormatted(String.format("%s&o%s&r is not in your enemy list&r", NightSky.clientName, args.get(2)));
                        return;
                    }
                    ChatUtil.sendFormatted(String.format("%sRemoved &o%s&r from your enemy list&r", NightSky.clientName, removed));
                    return;
                case "list":
                    ArrayList<String> list = NightSky.targetManager.getPlayers();
                    if (list.isEmpty()) {
                        ChatUtil.sendFormatted(String.format("%sNo enemies&r", NightSky.clientName));
                        return;
                    }
                    ChatUtil.sendFormatted(String.format("%sEnemies:&r", NightSky.clientName));
                    for (String player : list) {
                        ChatUtil.sendRaw(String.format(ChatColors.formatColor("   &o%s&r"), player));
                    }
                    return;
                case "clear":
                    NightSky.targetManager.clear();
                    ChatUtil.sendFormatted(String.format("%sCleared your enemy list&r", NightSky.clientName));
                    return;
            }
        }
        ChatUtil.sendFormatted(
                String.format("%sUsage: .%s <&oadd&r/&oremove&r/&olist&r/&oclear&r>&r", NightSky.clientName, args.get(0).toLowerCase(Locale.ROOT))
        );
    }
}
