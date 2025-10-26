package nightsky.powershell.shells;

import nightsky.NightSky;
import nightsky.powershell.PowerShell;
import nightsky.enums.ChatColors;
import nightsky.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;

public class Item extends PowerShell {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public Item() {
        super(new ArrayList<>(Arrays.asList("Item")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        ItemStack stack = mc.thePlayer.inventory.getCurrentItem();
        if (stack != null) {
            String display = stack.getDisplayName().replace('§', '&');
            String registryName = stack.getItem().getRegistryName();
            String compound = stack.hasTagCompound() ? stack.getTagCompound().toString().replace('§', '&') : "";
            ChatUtil.sendRaw(String.format("%s%s (%s) %s", ChatColors.formatColor(NightSky.clientName), display, registryName, compound));
        }
    }
}
