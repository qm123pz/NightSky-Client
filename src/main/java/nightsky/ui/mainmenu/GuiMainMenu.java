package nightsky.ui.mainmenu;

import net.minecraft.client.Minecraft;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


import static org.lwjgl.opengl.GL11.*;

public class GuiMainMenu extends GuiScreen {
    private static boolean videoInitialized = false;

    private MainButtonContainer buttonContainer;

    @Override
    public void initGui() {
        if (!videoInitialized) {
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

        try {
            VideoPlayer.render(0, 0, sr.getScaledWidth(), sr.getScaledHeight());
        } catch (Exception e) {
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
}
