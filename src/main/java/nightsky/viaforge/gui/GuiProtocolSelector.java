package nightsky.viaforge.gui;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiProtocolSelector extends GuiScreen {
  private final GuiScreen parent;

  private GuiProtocolList list;

  public GuiProtocolSelector(GuiScreen parent) {
    this.parent = parent;
  }

  @Override
  public void initGui() {
    super.initGui();
    buttonList.add(new GuiButton(1, 4, height - 24, 68, 20, "Done"));
    list = new GuiProtocolList(mc, width, height, 0, height, 16);
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    list.actionPerformed(button);
    if (button.id == 1) {
      mc.displayGuiScreen(parent);
    }
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) {
    if (keyCode == Keyboard.KEY_ESCAPE) {
      mc.displayGuiScreen(parent);
    }
  }

  @Override
  public void handleMouseInput() throws IOException {
    list.handleMouseInput();
    super.handleMouseInput();
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    list.drawScreen(mouseX, mouseY, partialTicks);
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  class GuiProtocolList extends GuiSlot {
    private int getIndex(int index) {
      return getSize() - 1 - index;
    }

    public GuiProtocolList(Minecraft minecraft, int width, int height, int top, int bottom, int slotHeight) {
      super(minecraft, width, height, top, bottom, slotHeight);
    }

    @Override
    protected int getSize() {
      return ViaLoadingBase.PROTOCOLS.size();
    }

    @Override
    protected void elementClicked(int index, boolean b, int i1, int i2) {
      ViaLoadingBase.getInstance().reload(ViaLoadingBase.PROTOCOLS.get(getIndex(index)));
    }

    @Override
    protected boolean isSelected(int index) {
      ProtocolVersion targetVersion = ViaLoadingBase.getInstance().getTargetVersion();
      ProtocolVersion version = ViaLoadingBase.PROTOCOLS.get(getIndex(index));
      return targetVersion == version;
    }

    @Override
    protected void drawBackground() {
      drawDefaultBackground();
    }

    @Override
    protected void drawSlot(int index, int x, int y, int slotHeight, int mouseX, int mouseY) {
      ProtocolVersion targetVersion = ViaLoadingBase.getInstance().getTargetVersion();
      ProtocolVersion version = ViaLoadingBase.PROTOCOLS.get(getIndex(index));
      String text = String.format("%s%s§r", targetVersion == version ? "§a§l" : "§f", version.getName());
      drawCenteredString(mc.fontRendererObj, text, width / 2, y + 2, -1);
    }
  }
}
