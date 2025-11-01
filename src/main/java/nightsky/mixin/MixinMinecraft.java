package nightsky.mixin;

import nightsky.NightSky;
import nightsky.event.EventManager;
import nightsky.init.Initializer;
import nightsky.event.types.EventType;
import nightsky.events.*;
import nightsky.module.modules.combat.NoHitDelay;
import nightsky.ui.mainmenu.GuiMainMenu;
import nightsky.ui.auth.GuiAuthMain;
import nightsky.ui.auth.GuiAuth;
import nightsky.checkerLOL.AuthManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nightsky.viaforge.ViaForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;

@SideOnly(Side.CLIENT)
@Mixin({Minecraft.class})
public abstract class MixinMinecraft {
    @Shadow
    private int leftClickCounter;
    @Shadow
    public PlayerControllerMP playerController;
    @Shadow
    public WorldClient theWorld;
    @Shadow
    public EntityPlayerSP thePlayer;
    @Shadow
    public GuiScreen currentScreen;

    @Inject(
            method = {"startGame"},
            at = {@At("HEAD")}
    )
    private void startGame(CallbackInfo callbackInfo) {
        new Initializer();
    }

    @Inject(
            method = {"startGame"},
            at = {@At("RETURN")}
    )
    private void postStartGame(CallbackInfo callbackInfo) {
        new NightSky();
    }

    @Inject(
            method = {"startGame"},
            at = {@At("RETURN")}
    )
    private void viaforge$init(CallbackInfo ci) {
        ViaForge.init();
    }

    @Inject(
            method = {"runTick"},
            at = {@At("HEAD")}
    )
    private void runTick(CallbackInfo callbackInfo) {
        if (this.theWorld != null && this.thePlayer != null) {
            EventManager.call(new TickEvent(EventType.PRE));
        }
    }

    @Inject(
            method = {"runTick"},
            at = {@At("RETURN")}
    )
    private void postRunTick(CallbackInfo callbackInfo) {
        if (this.theWorld != null && this.thePlayer != null) {
            EventManager.call(new TickEvent(EventType.POST));
        }
    }

    @Inject(
            method = {"loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V"},
            at = {@At("HEAD")}
    )
    private void loadWorld(WorldClient worldClient, String string, CallbackInfo callbackInfo) {
        EventManager.call(new LoadWorldEvent());
    }

    @Inject(
            method = {"updateFramebufferSize"},
            at = {@At("RETURN")}
    )
    private void updateFramebufferSize(CallbackInfo callbackInfo) {
        EventManager.call(new ResizeEvent());
    }

    @Inject(
            method = {"clickMouse"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void clickMouse(CallbackInfo callbackInfo) {
        if (NightSky.moduleManager != null && NightSky.moduleManager.modules.get(NoHitDelay.class).isEnabled()) {
            this.leftClickCounter = 0;
        }
        LeftClickMouseEvent event = new LeftClickMouseEvent();
        EventManager.call(event);
        if (event.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(
            method = {"rightClickMouse"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void rightClickMouse(CallbackInfo callbackInfo) {
        RightClickMouseEvent event = new RightClickMouseEvent();
        EventManager.call(event);
        if (event.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(
            method = {"sendClickBlockToController"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void sendClickBlockToController(CallbackInfo callbackInfo) {
        HitBlockEvent event = new HitBlockEvent();
        EventManager.call(event);
        if (event.isCancelled()) {
            callbackInfo.cancel();
            this.playerController.resetBlockRemoving();
        }
    }

    @Redirect(
            method = {"runTick"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/settings/KeyBinding;setKeyBindState(IZ)V"
            )
    )
    private void setKeyBindState(int integer, boolean boolean2) {
        KeyBinding.setKeyBindState(integer, boolean2);
        if (boolean2 && this.currentScreen == null) {
            EventManager.call(new KeyEvent(integer));
        }
    }

    @Redirect(
            method = {"runTick"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/InventoryPlayer;changeCurrentItem(I)V"
            )
    )
    private void changeCurrentItem(InventoryPlayer inventoryPlayer, int slot) {
        SwapItemEvent event = new SwapItemEvent(-1, slot);
        EventManager.call(event);
        if (!event.isCancelled()) {
            inventoryPlayer.changeCurrentItem(slot);
        }
    }

    @Inject(
            method = {"shutdown"},
            at = {@At("HEAD")}
    )
    private void shutdown(CallbackInfo callbackInfo) {
        try {
            GuiMainMenu.VideoPlayer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "displayGuiScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;", shift = At.Shift.AFTER))
    private void replaceMainMenu(GuiScreen guiScreenIn, CallbackInfo ci) {
        if (this.currentScreen instanceof net.minecraft.client.gui.GuiMainMenu) {
            //dev模式
            boolean devtest = GuiAuth.isDevTestMode();
            if (devtest) {
                AuthManager.getInstance().setDevMode(true);
                this.currentScreen = new GuiMainMenu();
            } else
            if (!AuthManager.getInstance().isAuthenticated()) {
                this.currentScreen = new GuiAuthMain();
            } else {
                this.currentScreen = new GuiMainMenu();
            }
            net.minecraft.client.gui.ScaledResolution scaledResolution = new net.minecraft.client.gui.ScaledResolution((Minecraft)(Object)this);
            this.currentScreen.setWorldAndResolution((Minecraft)(Object)this, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
        }
    }

    @Inject(method = "createDisplay", at = @At("RETURN"))
    private void onCreateDisplay(CallbackInfo ci) {
        org.lwjgl.opengl.Display.setTitle(NightSky.clientName + " " + NightSky.clientVersion);
        setWindowIcon();
    }
    
    private void setWindowIcon() {
        try {
            InputStream iconStream = this.getClass().getResourceAsStream("/assets/minecraft/nightsky/texture/title/nightsky.png");
            if (iconStream == null) {
                iconStream = Minecraft.class.getResourceAsStream("/assets/minecraft/nightsky/texture/title/nightsky.png");
            }
            if (iconStream == null) {
                return;
            }
            BufferedImage originalImage = ImageIO.read(iconStream);
            iconStream.close();
            if (originalImage == null) {
                return;
            }
            BufferedImage icon16 = resizeImage(originalImage, 16, 16);
            BufferedImage icon32 = resizeImage(originalImage, 32, 32);
            ByteBuffer buffer16 = convertImageToByteBuffer(icon16);
            ByteBuffer buffer32 = convertImageToByteBuffer(icon32);
            org.lwjgl.opengl.Display.setIcon(new ByteBuffer[]{buffer16, buffer32});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        int currentWidth = originalImage.getWidth();
        int currentHeight = originalImage.getHeight();
        BufferedImage currentImage = originalImage;
        while (currentWidth > targetWidth * 2 || currentHeight > targetHeight * 2) {
            currentWidth = Math.max(targetWidth, currentWidth / 2);
            currentHeight = Math.max(targetHeight, currentHeight / 2);
            BufferedImage tempImage = new BufferedImage(currentWidth, currentHeight, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = tempImage.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g.drawImage(currentImage, 0, 0, currentWidth, currentHeight, null);
            g.dispose();
            currentImage = tempImage;
        }
        if (currentWidth != targetWidth || currentHeight != targetHeight) {
            BufferedImage finalImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = finalImage.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g.drawImage(currentImage, 0, 0, targetWidth, targetHeight, null);
            g.dispose();
            return finalImage;
        }
        return currentImage;
    }
    
    private ByteBuffer convertImageToByteBuffer(BufferedImage image) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        ByteBuffer buffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        buffer.flip();
        return buffer;
    }
}