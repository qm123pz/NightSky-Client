package nightsky.powershell.shells;

import nightsky.NightSky;
import nightsky.powershell.PowerShell;
import nightsky.enums.ChatColors;
import nightsky.util.ChatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class Friend extends PowerShell {
    public Friend() {
        super(new ArrayList<>(Arrays.asList("Friend")));
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
                    String added = NightSky.friendManager.add(args.get(2));
                    if (added == null) {
                        ChatUtil.sendFormatted(String.format("%s&o%s&r is already in your friend list&r", NightSky.clientName, args.get(2)));
                        return;
                    }
                    ChatUtil.sendFormatted(String.format("%sAdded &o%s&r to your friend list&r", NightSky.clientName, added));
                    return;
                case "remove":
                    if (args.size() < 3) {
                        ChatUtil.sendFormatted(
                                String.format("%sUsage: .%s remove <&oname&r>&r", NightSky.clientName, args.get(0).toLowerCase(Locale.ROOT))
                        );
                        return;
                    }
                    String removed = NightSky.friendManager.remove(args.get(2));
                    if (removed == null) {
                        ChatUtil.sendFormatted(String.format("%s&o%s&r is not in your friend list&r", NightSky.clientName, args.get(2)));
                        return;
                    }
                    ChatUtil.sendFormatted(String.format("%sRemoved &o%s&r from your friend list&r", NightSky.clientName, removed));
                    return;
                case "list":
                    ArrayList<String> list = NightSky.friendManager.getPlayers();
                    if (list.isEmpty()) {
                        ChatUtil.sendFormatted(String.format("%sNo friends&r", NightSky.clientName));
                        return;
                    }
                    ChatUtil.sendFormatted(String.format("%sFriends:&r", NightSky.clientName));
                    for (String friend : list) {
                        ChatUtil.sendRaw(String.format(ChatColors.formatColor("   &o%s&r"), friend));
                    }
                    return;
                case "clear":
                    NightSky.friendManager.clear();
                    ChatUtil.sendFormatted(String.format("%sCleared your friend list&r", NightSky.clientName));
                    return;
            }
        }
        ChatUtil.sendFormatted(
                String.format("%sUsage: .%s <&oadd&r/&oremove&r/&olist&r/&oclear&r>&r", NightSky.clientName, args.get(0).toLowerCase(Locale.ROOT))
        );
    }
}
