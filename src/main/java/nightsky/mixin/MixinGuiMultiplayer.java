package nightsky.mixin;

import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import nightsky.viaforge.gui.GuiProtocolSelector;

@Mixin(GuiMultiplayer.class)
public abstract class MixinGuiMultiplayer extends GuiScreen {
  @Inject(method = "initGui", at = @At("RETURN"))
  private void viaforge$addButton(CallbackInfo callbackInfo) {
    buttonList.add(new GuiButton(997, 4, height - 24, 68, 20, "Protocol"));
  }

  @Inject(method = "drawScreen", at = @At("RETURN"))
  private void viaforge$drawVersion(CallbackInfo callbackInfo) {
    fontRendererObj.drawString(
            String.format("§7Version: §d%s§r", ViaLoadingBase.getInstance().getTargetVersion().getName()),
            6, height - 35, -1, true
    );
  }

  @Inject(method = "actionPerformed", at = @At("HEAD"))
  private void viaforge$handleButton(GuiButton button, CallbackInfo callbackInfo) {
    if (button.id == 997) {
      mc.displayGuiScreen(new GuiProtocolSelector(this));
    }
  }
}
