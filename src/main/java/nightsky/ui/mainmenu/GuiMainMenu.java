package nightsky.ui.mainmenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.apache.commons.io.IOUtils;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.lwjgl.opengl.GL11;
import java.io.IOException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


import static org.lwjgl.opengl.GL11.*;

public class GuiMainMenu extends GuiScreen {
    private static boolean videoInitialized = false;
    private static boolean videoPreferenceChecked = false;
    private static boolean videoEnabled = false;
    private static boolean preferenceLoaded = false;

    private MainButtonContainer buttonContainer;

    @Override
    public void initGui() {
        ensurePreferenceLoaded();
        if (!videoPreferenceChecked) {
            this.mc.displayGuiScreen(new VideoPrompt(this));
            return;
        }
        if (videoEnabled && !videoInitialized) {
            try {
                VideoComponent.ensureVideoExists();
                VideoComponent.startVideoPlayback();
            } catch (Exception e) {
            }
            videoInitialized = true;
        }

        this.buttonContainer = new MainButtonContainer(this.width / 2, this.height - 80, this.mc);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground();
        
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);

        if (buttonContainer != null) {
            buttonContainer.drawContainer(mouseX, mouseY);
        }
    }

    private void drawBackground() {
        ScaledResolution sr = new ScaledResolution(this.mc);
        boolean renderedVideo = false;
        try {
            if (videoEnabled && videoInitialized) {
                VideoPlayer.render(0, 0, sr.getScaledWidth(), sr.getScaledHeight());
                renderedVideo = true;
            }
        } catch (Exception e) {
            renderedVideo = false;
        }
        if (!renderedVideo) {
            this.drawDefaultBackground();
        }

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(0, this.height, 0).color(0, 0, 0, 100).endVertex();
        worldRenderer.pos(this.width, this.height, 0).color(0, 0, 0, 100).endVertex();
        worldRenderer.pos(this.width, 0, 0).color(0, 0, 0, 50).endVertex();
        worldRenderer.pos(0, 0, 0).color(0, 0, 0, 50).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.disableAlpha();
        GlStateManager.enableAlpha();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (buttonContainer != null) {
            buttonContainer.handleClick(mouseX, mouseY);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onGuiClosed() {
    }

    public static class VideoPlayer {
        private static FFmpegFrameGrabber frameGrabber;
        private static TextureBinder textureBinder;
        private static int frameLength;
        private static int count;
        private static ScheduledExecutorService scheduler;
        private static ScheduledFuture<?> scheduledFuture;
        public static final AtomicBoolean paused = new AtomicBoolean(false);
        private static final AtomicBoolean stopped = new AtomicBoolean(false);

        public static void init(File videoFile) throws FFmpegFrameGrabber.Exception {        
            frameGrabber = FFmpegFrameGrabber.createDefault(videoFile);
            frameGrabber.setPixelFormat(avutil.AV_PIX_FMT_RGB24);
            avutil.av_log_set_level(avutil.AV_LOG_QUIET);

            textureBinder = new TextureBinder();
            count = 0;
            stopped.set(false);
            
            frameGrabber.start();
            frameLength = frameGrabber.getLengthInFrames();

            double frameRate = frameGrabber.getFrameRate();
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduledFuture = scheduler.scheduleAtFixedRate(VideoPlayer::doGetBuffer, 0, (long) (1000 / frameRate), TimeUnit.MILLISECONDS);
        }

        private static void doGetBuffer() {
            if (paused.get() || stopped.get()) return;

            try {
                if (count < frameLength - 1) {
                    Frame frame = frameGrabber.grabImage();
                    if (frame != null) {
                        if (frame.image != null) {
                            textureBinder.setBuffer((ByteBuffer) frame.image[0], frame.imageWidth, frame.imageHeight);
                            count++;
                        }
                    }
                } else {
                    count = 0;
                    frameGrabber.setFrameNumber(0);
                }
            } catch (FFmpegFrameGrabber.Exception e) {
                e.printStackTrace();
            }
        }

        public static void render(int left, int top, int right, int bottom) throws FrameGrabber.Exception {
            if (stopped.get() || paused.get() || textureBinder == null) return;

            try {
                textureBinder.bindTexture();

                GL11.glPushMatrix();
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glDepthMask(false);
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

                GL11.glBegin(GL11.GL_QUADS);
                GL11.glTexCoord2f(0.0f, 1.0f);
                GL11.glVertex2f(left, bottom);
                GL11.glTexCoord2f(1.0f, 1.0f);
                GL11.glVertex2f(right, bottom);
                GL11.glTexCoord2f(1.0f, 0.0f);
                GL11.glVertex2f(right, top);
                GL11.glTexCoord2f(0.0f, 0.0f);
                GL11.glVertex2f(left, top);
                GL11.glEnd();

                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(true);
                GL11.glPopMatrix();
            } catch (Exception e) {
            }
        }

        public static void stop() throws FFmpegFrameGrabber.Exception {
            if (stopped.get()) return;

            stopped.set(true);
            paused.set(false);

            if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
                scheduledFuture.cancel(true);
            }

            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdownNow();
            }

            textureBinder = null;
            count = 0;

            if (frameGrabber != null) {
                frameGrabber.stop();
                frameGrabber.release();
                frameGrabber = null;
            }
        }
    }

    public static class VideoComponent {
        private static File currentVideoFile;

        public static void ensureVideoExists() {
            currentVideoFile = new File(Minecraft.getMinecraft().mcDataDir, "background.mp4");
            if (!currentVideoFile.exists()) {
                unpackFile(currentVideoFile);
            }
        }

        public static void startVideoPlayback() {
            try {
                if (currentVideoFile != null && currentVideoFile.exists()) {
                    VideoPlayer.init(currentVideoFile);
                }
            } catch (Exception e) {
            }
        }

        private static void unpackFile(File file) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                InputStream is = VideoComponent.class.getClassLoader().getResourceAsStream("assets/minecraft/nightsky/background/background.mp4");
                if (is != null) {
                    IOUtils.copy(is, fos);
                }
            } catch (Exception e) {
            }
        }
    }

    public static class TextureBinder {
        private int imageWidth;
        private int imageHeight;
        private int textureID;
        private ByteBuffer imageBuffer;

        public void setBuffer(ByteBuffer buffer, int width, int height) {
            this.setBuffer(buffer, width, height, GL_RGB);
        }

        public void setBuffer(ByteBuffer buffer, int width, int height, int ignoredInternalformat) {
            this.imageWidth = width;
            this.imageHeight = height;
            this.imageBuffer = buffer;
        }

        public void bindTexture() {
            if (this.imageBuffer == null) return;
            
            if (this.textureID != 0) {
                glDeleteTextures(this.textureID);
            }

            this.textureID = glGenTextures();

            glBindTexture(GL_TEXTURE_2D, textureID);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, this.imageWidth, this.imageHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, this.imageBuffer);
        }
    }

    private static void ensurePreferenceLoaded() {
        if (preferenceLoaded) return;
        preferenceLoaded = true;
        try {
            Path path = new File(Minecraft.getMinecraft().mcDataDir, "nightsky_android.pref").toPath();
            if (Files.exists(path)) {
                String value = new String(Files.readAllBytes(path), StandardCharsets.UTF_8).trim();
                boolean pref = Boolean.parseBoolean(value);
                nightsky.NightSky.android = pref;
                videoEnabled = !pref;
                videoPreferenceChecked = true;
            } else if (nightsky.NightSky.android != null) {
                videoEnabled = !nightsky.NightSky.android;
                videoPreferenceChecked = true;
            }
        } catch (Exception e) {
        }
    }

    private static void savePreference() {
        try {
            Path path = new File(Minecraft.getMinecraft().mcDataDir, "nightsky_android.pref").toPath();
            Files.write(path, String.valueOf(nightsky.NightSky.android).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
        }
    }

    private static class VideoPrompt extends GuiScreen {
        private final GuiScreen parent;
        private int enableX1;
        private int enableY1;
        private int enableX2;
        private int enableY2;
        private int disableX1;
        private int disableY1;
        private int disableX2;
        private int disableY2;

        public VideoPrompt(GuiScreen parent) {
            this.parent = parent;
        }

        @Override
        public void initGui() {
            int centerX = this.width / 2;
            int centerY = this.height / 2;
            this.enableX1 = centerX - 110;
            this.enableX2 = centerX + 110;
            this.enableY1 = centerY - 10;
            this.enableY2 = centerY + 20;
            this.disableX1 = centerX - 110;
            this.disableX2 = centerX + 110;
            this.disableY1 = centerY + 30;
            this.disableY2 = centerY + 60;
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            this.drawDefaultBackground();
            this.drawCenteredString(this.fontRendererObj, "Load animated video background?", this.width / 2, this.height / 2 - 40, 0xFFFFFF);
            boolean enableHover = isHovering(mouseX, mouseY, enableX1, enableY1, enableX2, enableY2);
            boolean disableHover = isHovering(mouseX, mouseY, disableX1, disableY1, disableX2, disableY2);
            drawOption(enableX1, enableY1, enableX2, enableY2, enableHover, "Enable(老安卓请点我)");
            drawOption(disableX1, disableY1, disableX2, disableY2, disableHover, "Disable(老安卓别碰我)");
        }

        private void drawOption(int x1, int y1, int x2, int y2, boolean hover, String text) {
            int base = hover ? 0xAA1E90FF : 0xAA000000;
            int border = hover ? 0xFFFFFFFF : 0x55FFFFFF;
            drawRect(x1, y1, x2, y2, base);
            drawHorizontalLine(x1, x2, y1, border);
            drawHorizontalLine(x1, x2, y2, border);
            drawVerticalLine(x1, y1, y2, border);
            drawVerticalLine(x2, y1, y2, border);
            this.drawCenteredString(this.fontRendererObj, text, (x1 + x2) / 2, y1 + 6, 0xFFFFFF);
        }

        private boolean isHovering(int mouseX, int mouseY, int x1, int y1, int x2, int y2) {
            return mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2;
        }

        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            if (isHovering(mouseX, mouseY, enableX1, enableY1, enableX2, enableY2)) {
                select(true);
            } else if (isHovering(mouseX, mouseY, disableX1, disableY1, disableX2, disableY2)) {
                select(false);
            }
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        private void select(boolean enableVideo) {
            videoEnabled = enableVideo;
            nightsky.NightSky.android = !enableVideo;
            videoPreferenceChecked = true;
            savePreference();
            if (!videoEnabled) {
                try {
                    VideoPlayer.stop();
                } catch (Exception e) {
                }
                videoInitialized = false;
            }
            this.mc.displayGuiScreen(parent);
        }
    }
}
