package nightsky.module.modules.misc;
import nightsky.module.Module;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.ModeValue;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
public class AntiOBS extends Module {
    public final ModeValue protectionLevel;
    public final BooleanValue useWindowsProtection;
    public final BooleanValue useLinuxProtection;
    public final BooleanValue useMacProtection;
    public final BooleanValue aggressiveMode;
    public final BooleanValue stealthMode;
    public final BooleanValue antiDetection;
    public final BooleanValue memoryProtection;
    public final BooleanValue visualProtection;
    private boolean originalFullscreen;
    private int interferenceFBO = -1;
    private int interferenceTexture = -1;
    private float interferenceAlpha = 0.0f;
    private long lastPulseTime = 0;
    private List<String> protectedWindows;
    private Thread protectionThread;
    private boolean isRunning;
    private Random random;
    private final String[] SCREEN_CAPTURE_SIGNATURES = {
            "obs", "obs64", "xsplit", "bandicam", "fraps",
            "shadowplay", "nvidia", "gamebar", "xboxgaming",
            "discord", "zoom", "teams", "meet", "skype",
            "ffmpeg", "gdigrab", "dshow", "avfoundation"
    };
    public AntiOBS() {
        super("AntiOBS", false);
        this.protectionLevel = new ModeValue("ProtectionLevel", 0,
                new String[]{"Maximum", "High", "Medium", "Low"});
        this.useWindowsProtection = new BooleanValue("WindowsProtection", true);
        this.useLinuxProtection = new BooleanValue("LinuxProtection", false);
        this.useMacProtection = new BooleanValue("MacProtection", true);
        this.aggressiveMode = new BooleanValue("AggressiveMode", false);
        this.stealthMode = new BooleanValue("StealthMode", true);
        this.antiDetection = new BooleanValue("AntiDetection", true);
        this.memoryProtection = new BooleanValue("MemoryProtection", true);
        this.visualProtection = new BooleanValue("VisualProtection", true);
        this.protectedWindows = new ArrayList<>();
        this.random = new SecureRandom();
    }
    @Override
    public void onEnabled() {
        originalFullscreen = Display.isFullscreen();
        isRunning = true;
        initGraphicsProtection();
        startContinuousProtection();
        applyMultiLayerProtection();
        if (memoryProtection.getValue()) {
            applyMemoryProtection();
        }
        if (antiDetection.getValue()) {
            applyAntiDetection();
        }
    }
    @Override
    public void onDisabled() {
        isRunning = false;
        if (protectionThread != null && protectionThread.isAlive()) {
            protectionThread.interrupt();
        }
        cleanupGraphicsProtection();
        try {
            if (originalFullscreen) {
                Display.setFullscreen(true);
            }
        } catch (Exception ignored) {}
    }
    private void initGraphicsProtection() {
        try {
            interferenceFBO = GL30.glGenFramebuffers();
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, interferenceFBO);
            interferenceTexture = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, interferenceTexture);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, Display.getWidth(), Display.getHeight(),
                    0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
                    GL11.GL_TEXTURE_2D, interferenceTexture, 0);
            if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
                System.err.println("FrameBuffer incomplete");
            }
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        } catch (Exception e) {
            System.err.println("Failed to initialize graphics protection: " + e.getMessage());
        }
    }
    private void cleanupGraphicsProtection() {
        try {
            if (interferenceFBO != -1) {
                GL30.glDeleteFramebuffers(interferenceFBO);
                interferenceFBO = -1;
            }
            if (interferenceTexture != -1) {
                GL11.glDeleteTextures(interferenceTexture);
                interferenceTexture = -1;
            }
        } catch (Exception ignored) {}
    }
    private void startContinuousProtection() {
        protectionThread = new Thread(() -> {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    continuousMonitoring();
                    int sleepTime;
                    switch (protectionLevel.getValue()) {
                        case 0:
                            sleepTime = 100;
                            break;
                        case 1:
                            sleepTime = 500;
                            break;
                        case 2:
                            sleepTime = 1000;
                            break;
                        default:
                            sleepTime = 2000;
                    }
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                }
            }
        });
        protectionThread.setDaemon(true);
        protectionThread.setName("AntiOBS-Protection");
        protectionThread.start();
    }
    private void continuousMonitoring() {
        boolean captureDetected = detectScreenCapture();
        if (visualProtection.getValue() && captureDetected) {
            applyVisualInterference();
        }
        monitorWindowState();
        dynamicProtectionAdjustment();
    }
    private boolean detectScreenCapture() {
        String os = System.getProperty("os.name").toLowerCase();
        boolean detected = false;
        if (os.contains("win")) {
            detected = checkWindowsProcesses();
        } else if (os.contains("mac")) {
            detected = checkMacProcesses();
        }
        detected |= checkCaptureWindows();
        return detected;
    }
    private boolean checkWindowsProcesses() {
        try {
            Process process = Runtime.getRuntime().exec("tasklist /fo csv /nh");
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                for (String signature : SCREEN_CAPTURE_SIGNATURES) {
                    if (line.toLowerCase().contains(signature.toLowerCase())) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }
    private boolean checkMacProcesses() {
        try {
            Process process = Runtime.getRuntime().exec("ps aux");
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                for (String signature : SCREEN_CAPTURE_SIGNATURES) {
                    if (line.toLowerCase().contains(signature.toLowerCase())) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }
    private boolean checkCaptureWindows() {
        try {
            Frame[] frames = Frame.getFrames();
            for (Frame frame : frames) {
                String title = frame.getTitle().toLowerCase();
                for (String signature : SCREEN_CAPTURE_SIGNATURES) {
                    if (title.contains(signature)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }
    private void applyVisualInterference() {
        if (interferenceFBO == -1 || interferenceTexture == -1) return;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPulseTime > 2000) {
            lastPulseTime = currentTime;
            interferenceAlpha = 0.0f;
        }
        float pulse = (float) Math.sin((currentTime - lastPulseTime) / 2000.0 * Math.PI);
        interferenceAlpha = Math.min(0.4f, 0.15f + 0.25f * pulse);
        try {
            int originalFBO = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
            int originalProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
            boolean depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
            boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, interferenceFBO);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            if (depthTest) GL11.glDisable(GL11.GL_DEPTH_TEST);
            if (!blend) GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GL11.glOrtho(0, Display.getWidth(), Display.getHeight(), 0, -1, 1);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            int pointCount = 20 + (int)(Math.random() * 30);
            GL11.glBegin(GL11.GL_POINTS);
            for (int i = 0; i < pointCount; i++) {
                float x = (float)(Math.random() * Display.getWidth());
                float y = (float)(Math.random() * Display.getHeight());
                float r = (float)Math.random();
                float g = (float)Math.random();
                float b = (float)Math.random();
                GL11.glColor4f(r, g, b, interferenceAlpha * (0.5f + (float)Math.random() * 0.5f));
                GL11.glVertex2f(x, y);
            }
            GL11.glEnd();
            GL11.glLineWidth(1.0f + (float)Math.random() * 2.0f);
            GL11.glBegin(GL11.GL_LINES);
            for (int i = 0; i < 10; i++) {
                float x1 = (float)(Math.random() * Display.getWidth());
                float y1 = (float)(Math.random() * Display.getHeight());
                float x2 = (float)(Math.random() * Display.getWidth());
                float y2 = (float)(Math.random() * Display.getHeight());
                GL11.glColor4f(1.0f, 0.0f, 0.0f, interferenceAlpha * 0.7f);
                GL11.glVertex2f(x1, y1);
                GL11.glVertex2f(x2, y2);
            }
            GL11.glEnd();
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPopMatrix();
            if (depthTest) GL11.glEnable(GL11.GL_DEPTH_TEST);
            if (!blend) GL11.glDisable(GL11.GL_BLEND);
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, originalFBO);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, interferenceTexture);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0, 0); GL11.glVertex2f(0, 0);
            GL11.glTexCoord2f(1, 0); GL11.glVertex2f(Display.getWidth(), 0);
            GL11.glTexCoord2f(1, 1); GL11.glVertex2f(Display.getWidth(), Display.getHeight());
            GL11.glTexCoord2f(0, 1); GL11.glVertex2f(0, Display.getHeight());
            GL11.glEnd();
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            if (originalProgram != 0) {
                GL20.glUseProgram(originalProgram);
            }
        } catch (Exception e) {
        }
    }
    private void applyMultiLayerProtection() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win") && useWindowsProtection.getValue()) {
            applyWindowsProtection();
        } else if (os.contains("mac") && useMacProtection.getValue()) {
            applyMacProtection();
        }
        applyCrossPlatformProtection();
    }
    private void applyWindowsProtection() {
        try {
            trySetWindowDisplayAffinity();
            disableGameFeatures();
        } catch (Exception e) {
            applyFallbackProtection();
        }
    }
    private void trySetWindowDisplayAffinity() {
        try {
            Class<?> user32Class = Class.forName("com.sun.jna.platform.win32.User32");
            Class<?> winDefClass = Class.forName("com.sun.jna.platform.win32.WinDef");
            Method getForegroundWindow = user32Class.getMethod("GetForegroundWindow");
            Method setWindowDisplayAffinity = user32Class.getMethod("SetWindowDisplayAffinity",
                    winDefClass.getDeclaredClasses()[0], int.class);
            Object hwnd = getForegroundWindow.invoke(null);
            setWindowDisplayAffinity.invoke(null, hwnd, 0x11);
        } catch (Exception e) {
            System.out.println("JNA not available, skipping SetWindowDisplayAffinity");
        }
    }
    private void disableGameFeatures() {
        try {
            String disableGameBar = "reg add \"HKEY_CURRENT_USER\\Software\\Microsoft\\GameBar\" /v \"AutoGameModeEnabled\" /t REG_DWORD /d 0 /f";
            Runtime.getRuntime().exec("cmd.exe /c " + disableGameBar);
            String disableGameMode = "reg add \"HKEY_CURRENT_USER\\Software\\Microsoft\\GameBar\" /v \"AllowAutoGameMode\" /t REG_DWORD /d 0 /f";
            Runtime.getRuntime().exec("cmd.exe /c " + disableGameMode);
        } catch (Exception e) {
        }
    }
    private void applyMacProtection() {
        try {
            checkScreenRecordingPermission();
        } catch (Exception e) {
        }
    }
    private void checkScreenRecordingPermission() {
        try {
            String checkCmd = "sqlite3 ~/Library/Application\\ Support/com.apple.TCC/TCC.db " +
                    "\"SELECT service, client FROM access WHERE service='kTCCServiceScreenCapture' AND auth_value=2;\"";
            Process process = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", checkCmd});
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                for (String signature : SCREEN_CAPTURE_SIGNATURES) {
                    if (line.toLowerCase().contains(signature)) {
                        System.out.println("Capture software detected: " + line);
                    }
                }
            }
        } catch (Exception e) {
        }
    }
    private void applyCrossPlatformProtection() {
        if (!stealthMode.getValue()) {
            try {
                Frame[] frames = Frame.getFrames();
                for (Frame frame : frames) {
                    if (frame.isVisible()) {
                        frame.setAlwaysOnTop(true);
                    }
                }
            } catch (Exception e) {
            }
        }
        applyBasicMemoryProtection();
    }
    private void applyMemoryProtection() {
        applyBasicMemoryProtection();
        if (aggressiveMode.getValue()) {
            encryptSensitiveData();
            interfereWithMemoryScanners();
        }
    }
    private void applyBasicMemoryProtection() {
    }
    private void encryptSensitiveData() {
        try {
            Field[] fields = getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getType() == String.class) {
                    field.setAccessible(true);
                    String value = (String) field.get(this);
                    if (value != null && !value.isEmpty()) {
                        field.set(this, encryptString(value));
                    }
                }
            }
        } catch (Exception e) {
        }
    }
    private void applyAntiDetection() {
        try {
            obfuscateProcessName();
        } catch (Exception e) {
        }
    }
    private void obfuscateProcessName() {
        try {
            String fakeName = generateRandomName();
            java.lang.management.RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            Field jvmField = runtime.getClass().getDeclaredField("jvm");
            jvmField.setAccessible(true);
            Object vm = jvmField.get(runtime);
            Method setProcessName = vm.getClass().getDeclaredMethod("setProcessName", String.class);
            setProcessName.setAccessible(true);
            setProcessName.invoke(vm, fakeName);
        } catch (Exception e) {
        }
    }
    private String generateRandomName() {
        String[] prefixes = {"system_", "svchost_", "runtime_", "service_", "update_"};
        String[] suffixes = {"_host", "_mgr", "_svc", "_daemon", "_agent"};
        return prefixes[random.nextInt(prefixes.length)] +
                Integer.toHexString(random.nextInt(0xFFFF)) +
                suffixes[random.nextInt(suffixes.length)];
    }
    private String encryptString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return input;
        }
    }
    private void applyFallbackProtection() {
        try {
            Frame[] frames = Frame.getFrames();
            for (Frame frame : frames) {
                if (frame instanceof javax.swing.JFrame) {
                    ((javax.swing.JFrame)frame).setType(java.awt.Window.Type.UTILITY);
                }
            }
        } catch (Exception e) {
        }
    }
    private void monitorWindowState() {
    }
    private void dynamicProtectionAdjustment() {
    }
    private void interfereWithMemoryScanners() {
    }
    @Override
    public String[] getSuffix() {
        String level;
        switch (protectionLevel.getValue()) {
            case 0: level = "MAX"; break;
            case 1: level = "HIGH"; break;
            case 2: level = "MED"; break;
            default: level = "LOW";
        }
        return new String[]{level + (visualProtection.getValue() ? "+VIS" : "")};
    }
}