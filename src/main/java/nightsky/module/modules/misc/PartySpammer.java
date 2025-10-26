package nightsky.module.modules.misc;

import nightsky.event.EventTarget;
import nightsky.events.Render2DEvent;
import nightsky.module.Module;
import nightsky.util.TimerUtil;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.FloatValue;
import nightsky.value.values.IntValue;
import nightsky.value.values.TextValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PartySpammer extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final TimerUtil timer = new TimerUtil();
    private final TimerUtil cooldownTimer = new TimerUtil();
    private final Set<String> invitedPlayers = new HashSet<>();
    private int currentIndex = 0;

    public final TextValue command = new TextValue("Command", "/party invite {player}");
    public final FloatValue delay = new FloatValue("Delay", 5.0F, 1.0F, 60.0F);
    public final IntValue cooldown = new IntValue("Cooldown", 60, 10, 600);
    public final BooleanValue ignoreSelf = new BooleanValue("IgnoreSelf", true);
    public final BooleanValue caseSensitive = new BooleanValue("CaseSensitive", false);
    public final BooleanValue loopMode = new BooleanValue("LoopMode", false);
    public final TextValue filter = new TextValue("Filter", "");
    public final BooleanValue useWhitelist = new BooleanValue("UseWhitelist", false);
    public final TextValue whitelist = new TextValue("Whitelist", "player1,player2,player3");
    public final BooleanValue debug = new BooleanValue("Debug", false);

    public PartySpammer() {
        super("PartySpammer", false);
    }

    @EventTarget
    public void onRender(Render2DEvent event) {
        if (!this.isEnabled() || mc.thePlayer == null || mc.theWorld == null) return;

        if (this.timer.hasTimeElapsed((long) (this.delay.getValue() * 1000.0F))) {
            this.timer.reset();

            List<String> playerList = getPlayerList();
            if (playerList.isEmpty()) return;

            if (cooldownTimer.hasTimeElapsed((long) (cooldown.getValue() * 1000.0F))) {
                invitedPlayers.clear();
                cooldownTimer.reset();
                currentIndex = 0;
            }

            sendNextInvite(playerList);
        }
    }

    private List<String> getPlayerList() {
        List<String> players = new ArrayList<>();

        try {
            NetHandlerPlayClient netHandler = mc.getNetHandler();
            if (netHandler == null) return players;

            for (NetworkPlayerInfo playerInfo : netHandler.getPlayerInfoMap()) {
                if (playerInfo != null && playerInfo.getGameProfile() != null) {
                    String playerName = playerInfo.getGameProfile().getName();
                    if (playerName != null && !playerName.trim().isEmpty()) {
                        players.add(playerName);
                    }
                }
            }
        } catch (Exception e) {
            if (debug.getValue()) e.printStackTrace();
        }

        return players;
    }

    private void sendNextInvite(List<String> playerList) {
        if (playerList.isEmpty()) return;

        if (!loopMode.getValue() && invitedPlayers.size() >= getAvailablePlayersCount(playerList)) {
            return;
        }

        int attempts = 0;
        while (attempts < playerList.size()) {
            String playerName = playerList.get(currentIndex);
            currentIndex = (currentIndex + 1) % playerList.size();
            attempts++;

            if (shouldInvitePlayer(playerName)) {
                sendInvite(playerName);
                break;
            }
        }
    }

    private boolean shouldInvitePlayer(String playerName) {
        if (ignoreSelf.getValue() && playerName.equalsIgnoreCase(mc.thePlayer.getName())) {
            return false;
        }

        String key = caseSensitive.getValue() ? playerName : playerName.toLowerCase();
        if (invitedPlayers.contains(key)) {
            return false;
        }

        if (useWhitelist.getValue()) {
            if (whitelist.getValue().isEmpty()) return true;

            String[] whitelistPlayers = whitelist.getValue().split(",");
            for (String whitelistPlayer : whitelistPlayers) {
                String wp = whitelistPlayer.trim();
                if (caseSensitive.getValue()) {
                    if (playerName.equals(wp)) {
                        return true;
                    }
                } else {
                    if (playerName.equalsIgnoreCase(wp)) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            if (filter.getValue().isEmpty()) return true;

            String filterText = filter.getValue();
            String checkName = caseSensitive.getValue() ? playerName : playerName.toLowerCase();
            return !checkName.contains(caseSensitive.getValue() ? filterText : filterText.toLowerCase());
        }
    }

    private void sendInvite(String playerName) {
        try {
            String finalCommand = command.getValue().replace("{player}", playerName);
            mc.thePlayer.sendChatMessage(finalCommand);

            String key = caseSensitive.getValue() ? playerName : playerName.toLowerCase();
            invitedPlayers.add(key);

        } catch (Exception e) {
            if (debug.getValue()) e.printStackTrace();
        }
    }

    private int getAvailablePlayersCount(List<String> playerList) {
        int count = 0;
        for (String playerName : playerList) {
            if (shouldInvitePlayer(playerName)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
        invitedPlayers.clear();
        timer.reset();
        cooldownTimer.reset();
        currentIndex = 0;
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        invitedPlayers.clear();
        currentIndex = 0;
    }
}