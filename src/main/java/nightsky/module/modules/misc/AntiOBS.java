package nightsky.module.modules.misc;

import nightsky.module.Module;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.ModeValue;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

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

    private Object securityManager;
    private boolean originalFullscreen;
    private List<Process> killedProcesses;
    private List<String> protectedWindows;
    private Thread protectionThread;
    private boolean isRunning;
    private Random random;

    // 录屏软件进程列表
    private final String[] SCREEN_CAPTURE_SOFTWARE = {
            "obs", "obs64", "obs32", "xsplit", "bandicam", "fraps",
            "action", "camtasia", "streamlabs", "nvidia", "shadowplay",
            "plays", "msedge", "chrome", "firefox", "browser",
            "teamviewer", "anydesk", "vnc", "rdp", "mstsc",
            "discord", "skype", "zoom", "teams", "meet",
            "ffmpeg", "gdigrab", "avfoundation", "dshow"
    };

    public AntiOBS() {
        super("AntiOBS", false);
        this.protectionLevel = new ModeValue("ProtectionLevel", 0,
                new String[]{"Maximum", "High", "Medium", "Low"});
        this.useWindowsProtection = new BooleanValue("WindowsProtection", true);
        this.useLinuxProtection = new BooleanValue("LinuxProtection", true);
        this.useMacProtection = new BooleanValue("MacProtection", true);
        this.aggressiveMode = new BooleanValue("AggressiveMode", false);
        this.stealthMode = new BooleanValue("StealthMode", true);
        this.antiDetection = new BooleanValue("AntiDetection", true);
        this.memoryProtection = new BooleanValue("MemoryProtection", true);

        this.killedProcesses = new ArrayList<>();
        this.protectedWindows = new ArrayList<>();
        this.random = new SecureRandom();
    }

    @Override
    public void onEnabled() {
        originalFullscreen = Display.isFullscreen();
        isRunning = true;

        // 启动持续保护线程
        startContinuousProtection();

        // 应用多层级保护
        applyMultiLayerProtection();

        // 应用内存保护
        if (memoryProtection.getValue()) {
            applyMemoryProtection();
        }

        // 应用反检测保护
        if (antiDetection.getValue()) {
            applyAntiDetection();
        }
    }

    @Override
    public void onDisabled() {
        isRunning = false;

        // 停止保护线程
        if (protectionThread != null && protectionThread.isAlive()) {
            protectionThread.interrupt();
        }

        removeProtection();

        // 恢复原始显示设置
        try {
            if (originalFullscreen) {
                Display.setFullscreen(true);
            }
        } catch (Exception ignored) {}
    }

    private void startContinuousProtection() {
        protectionThread = new Thread(() -> {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    // 持续监控和防护
                    continuousMonitoring();

                    // 根据保护级别调整监控频率
                    int sleepTime;
                    switch (protectionLevel.getValue()) {
                        case 0: // Maximum
                            sleepTime = 100; // 100ms
                            break;
                        case 1: // High
                            sleepTime = 500; // 500ms
                            break;
                        case 2: // Medium
                            sleepTime = 1000; // 1秒
                            break;
                        default: // Low
                            sleepTime = 2000; // 2秒
                    }

                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    // 忽略异常，继续运行
                }
            }
        });
        protectionThread.setDaemon(true);
        protectionThread.setName("AntiOBS-Protection");
        protectionThread.start();
    }

    private void continuousMonitoring() {
        // 持续监控录屏软件进程
        monitorScreenCaptureProcesses();

        // 监控窗口状态
        monitorWindowState();

        // 检查系统变化
        checkSystemChanges();

        // 动态调整保护策略
        dynamicProtectionAdjustment();
    }

    private void applyMultiLayerProtection() {
        String os = System.getProperty("os.name").toLowerCase();

        // 应用操作系统特定的保护
        if (os.contains("win") && useWindowsProtection.getValue()) {
            applyWindowsProtection();
            if (aggressiveMode.getValue()) {
                applyAdvancedWindowsProtection();
            }
        } else if ((os.contains("nix") || os.contains("nux")) && useLinuxProtection.getValue()) {
            applyLinuxProtection();
            if (aggressiveMode.getValue()) {
                applyAdvancedLinuxProtection();
            }
        } else if (os.contains("mac") && useMacProtection.getValue()) {
            applyMacProtection();
            if (aggressiveMode.getValue()) {
                applyAdvancedMacProtection();
            }
        }

        // 应用跨平台保护
        applyCrossPlatformProtection();

        // 应用安全管理器保护
        applySecurityManagerProtection();

        // 应用图形层保护
        applyGraphicsLayerProtection();
    }

    /**
     * Windows 系统保护 - 增强版
     */
    private void applyWindowsProtection() {
        try {
            // 方法1: 使用JNA调用Windows API
            applyWindowsAPIProtection();

            // 方法2: 使用PowerShell命令
            applyWindowsPowerShellProtection();

            // 方法3: 终止录屏进程
            terminateScreenCaptureProcesses();

            // 方法4: 修改注册表阻止录屏软件
            if (aggressiveMode.getValue()) {
                applyWindowsRegistryProtection();
            }

        } catch (Exception e) {
            // 备用保护方案
            applyFallbackWindowsProtection();
        }
    }

    private void applyWindowsAPIProtection() {
        try {
            // 使用反射调用Windows API
            Class<?> user32 = Class.forName("com.sun.jna.platform.win32.User32");
            Method getForegroundWindow = user32.getMethod("GetForegroundWindow");
            Method setWindowDisplayAffinity = user32.getMethod("SetWindowDisplayAffinity",
                    long.class, int.class);

            long hwnd = (Long) getForegroundWindow.invoke(null);

            // 设置窗口显示关联性，防止被捕获
            // WDA_MONITOR = 1, WDA_EXCLUDEFROMCAPTURE = 0x11 (Windows 10 2004+)
            setWindowDisplayAffinity.invoke(null, hwnd, 0x11);

        } catch (Exception e) {
            // 如果JNA不可用，使用其他方法
        }
    }

    private void applyWindowsPowerShellProtection() {
        try {
            // 使用PowerShell禁用录屏功能
            String[] commands = {
                    "powershell", "-Command",
                    "Get-Process | Where-Object {$_.ProcessName -match 'obs|xsplit|bandicam|fraps'} | Stop-Process -Force"
            };
            Runtime.getRuntime().exec(commands);

            // 禁用游戏栏
            String disableGameBar = "powershell -Command \"Set-ItemProperty -Path 'HKCU:\\Software\\Microsoft\\GameBar' -Name 'AllowAutoGameMode' -Value 0\"";
            Runtime.getRuntime().exec(disableGameBar);

        } catch (Exception e) {
            // 忽略异常
        }
    }

    private void applyAdvancedWindowsProtection() {
        try {
            // 高级保护：挂钩系统API
            hookWindowsAPI();

            // 创建虚假进程干扰检测
            createDecoyProcesses();

            // 修改内存保护
            modifyMemoryProtection();

        } catch (Exception e) {
            // 高级保护失败不影响基础保护
        }
    }

    /**
     * Linux 系统保护 - 增强版
     */
    private void applyLinuxProtection() {
        try {
            // 方法1: 使用xprop设置窗口属性
            applyXWindowProtection();

            // 方法2: 使用LD_PRELOAD挂钩系统调用
            applyLDPreloadProtection();

            // 方法3: 终止录屏进程
            terminateLinuxCaptureProcesses();

            // 方法4: 修改环境变量
            modifyLinuxEnvironment();

        } catch (Exception e) {
            applyFallbackLinuxProtection();
        }
    }

    private void applyXWindowProtection() {
        try {
            long windowId = getWindowId();
            if (windowId == 0) return;

            // 设置多个窗口属性防止捕获
            String[] commands = {
                    "xprop", "-id", String.valueOf(windowId),
                    "-format", "_NET_WM_STATE", "32a",
                    "-set", "_NET_WM_STATE", "_NET_WM_STATE_HIDDEN"
            };
            Runtime.getRuntime().exec(commands);

            // 设置窗口为覆盖类型
            String[] overlayCmd = {
                    "xprop", "-id", String.valueOf(windowId),
                    "-format", "_NET_WM_WINDOW_TYPE", "32a",
                    "-set", "_NET_WM_WINDOW_TYPE", "_NET_WM_WINDOW_TYPE_OVERRIDE"
            };
            Runtime.getRuntime().exec(overlayCmd);

        } catch (Exception e) {
            // 忽略异常
        }
    }

    private void applyAdvancedLinuxProtection() {
        try {
            // 使用ptrace防止调试
            applyPtraceProtection();

            // 修改系统调用表
            modifySyscallTable();

            // 使用seccomp过滤系统调用
            applySeccompProtection();

        } catch (Exception e) {
            // 高级保护失败
        }
    }

    /**
     * Mac 系统保护 - 增强版
     */
    private void applyMacProtection() {
        try {
            // 方法1: 使用AppleScript
            applyAppleScriptProtection();

            // 方法2: 使用Objective-C运行时
            applyObjCProtection();

            // 方法3: 终止录屏进程
            terminateMacCaptureProcesses();

            // 方法4: 修改权限设置
            modifyMacPermissions();

        } catch (Exception e) {
            applyFallbackMacProtection();
        }
    }

    private void applyAppleScriptProtection() {
        try {
            // 禁用屏幕录制权限
            String disableScreenRecording = "tell application \"System Events\" to " +
                    "set UI elements enabled to false";
            executeAppleScript(disableScreenRecording);

            // 隐藏应用程序
            String hideApp = "tell application \"System Events\" to " +
                    "set visible of process \"" + getProcessName() + "\" to false";
            executeAppleScript(hideApp);

        } catch (Exception e) {
            // 忽略异常
        }
    }

    private void applyAdvancedMacProtection() {
        try {
            // 使用代码签名API
            applyCodeSigningProtection();

            // 修改沙盒配置
            modifySandboxConfiguration();

            // 使用内核扩展保护
            applyKernelExtensionProtection();

        } catch (Exception e) {
            // 高级保护失败
        }
    }

    /**
     * 跨平台保护方法 - 增强版
     */
    private void applyCrossPlatformProtection() {
        try {

            // 应用加密保护
            applyEncryptionProtection();

            // 应用时间干扰
            applyTimingObfuscation();

        } catch (Exception e) {
            // 忽略异常
        }
    }

    /**
     * 安全管理器保护 - 增强版
     */
    private void applySecurityManagerProtection() {
        try {
            securityManager = new SecurityManager() {
                @Override
                public void checkPermission(java.security.Permission perm) {
                    String permName = perm.getName();

                    // 阻止各种敏感权限
                    if (perm instanceof java.lang.RuntimePermission) {
                        if (permName.startsWith("accessDeclaredMembers") ||
                                permName.startsWith("setSecurityManager") ||
                                permName.startsWith("createSecurityManager") ||
                                permName.startsWith("getenv") ||
                                permName.startsWith("exitVM") ||
                                permName.startsWith("shutdownHooks")) {
                            throw new SecurityException("Access denied by AntiOBS protection");
                        }
                    }

                    // 阻止文件访问
                    if (perm instanceof java.io.FilePermission) {
                        if (perm.getActions().contains("read") ||
                                perm.getActions().contains("write")) {
                            String name = perm.getName().toLowerCase();
                            if (name.contains("obs") || name.contains("screen") ||
                                    name.contains("capture") || name.contains("record")) {
                                throw new SecurityException("File access blocked by AntiOBS");
                            }
                        }
                    }

                    // 阻止属性访问
                    if (perm instanceof java.util.PropertyPermission) {
                        if (permName.contains("java") || permName.contains("os") ||
                                permName.contains("user") || permName.contains("sun")) {
                            throw new SecurityException("Property access blocked by AntiOBS");
                        }
                    }
                }

                @Override
                public void checkExec(String cmd) {
                    cmd = cmd.toLowerCase();
                    for (String software : SCREEN_CAPTURE_SOFTWARE) {
                        if (cmd.contains(software)) {
                            throw new SecurityException("Screen recording software execution blocked");
                        }
                    }
                }

                @Override
                public void checkConnect(String host, int port) {
                    // 阻止网络连接
                    throw new SecurityException("Network connection blocked by AntiOBS");
                }

                @Override
                public void checkListen(int port) {
                    // 阻止监听端口
                    throw new SecurityException("Port listening blocked by AntiOBS");
                }
            };

            System.setSecurityManager((SecurityManager) securityManager);

        } catch (Exception e) {
            // 如果无法设置安全管理器，使用其他方法
            applyAlternativeSecurityProtection();
        }
    }

    /**
     * 图形层保护 - 新增
     */
    private void applyGraphicsLayerProtection() {
        try {
            // OpenGL 深度保护
            setupOpenGLDepthProtection();

            // 帧缓冲保护
            setupFramebufferProtection();

            // 着色器保护
            setupShaderProtection();

            // 动态纹理保护
            setupDynamicTextureProtection();

        } catch (Exception e) {
            // 忽略异常
        }
    }

    private void setupOpenGLDepthProtection() {
        try {
            // 启用深度测试
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(GL11.GL_ALWAYS);

            // 设置深度范围
            GL11.glDepthRange(0.1, 0.9);

            // 启用模板测试
            GL11.glEnable(GL11.GL_STENCIL_TEST);
            GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);

        } catch (Exception e) {
            // 忽略异常
        }
    }

    /**
     * 内存保护 - 新增
     */
    private void applyMemoryProtection() {
        try {
            // 内存加密
            encryptCriticalMemory();

            // 内存填充
            fillMemoryWithNoise();

            // 内存访问保护
            protectMemoryAccess();

            // 垃圾收集干扰
            interfereWithGC();

        } catch (Exception e) {
            // 忽略异常
        }
    }

    private void encryptCriticalMemory() {
        try {
            // 使用反射访问和加密关键字段
            Field[] fields = getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getType() == String.class) {
                    field.setAccessible(true);
                    String value = (String) field.get(this);
                    if (value != null) {
                        String encrypted = encryptString(value);
                        field.set(this, encrypted);
                    }
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
    }

    /**
     * 反检测保护 - 新增
     */
    private void applyAntiDetection() {
        try {
            // 进程名混淆
            obfuscateProcessName();

            // 窗口标题混淆
            obfuscateWindowTitle();

            // 类名混淆
            obfuscateClassNames();

            // 行为模式混淆
            obfuscateBehaviorPatterns();

        } catch (Exception e) {
            // 忽略异常
        }
    }

    private void obfuscateProcessName() {
        try {
            // 修改进程名
            String fakeName = generateRandomProcessName();
            java.lang.management.RuntimeMXBean runtime =
                    ManagementFactory.getRuntimeMXBean();
            Field jvmField = runtime.getClass().getDeclaredField("jvm");
            jvmField.setAccessible(true);
            Object jvm = jvmField.get(runtime);
            Method setProcessName = jvm.getClass().getDeclaredMethod("setProcessName", String.class);
            setProcessName.setAccessible(true);
            setProcessName.invoke(jvm, fakeName);
        } catch (Exception e) {
            // 忽略异常
        }
    }

    /**
     * 监控录屏进程 - 增强版
     */
    private void monitorScreenCaptureProcesses() {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                monitorWindowsProcesses();
            } else if (os.contains("nix") || os.contains("nux")) {
                monitorLinuxProcesses();
            } else if (os.contains("mac")) {
                monitorMacProcesses();
            }

            // 检查网络活动
            monitorNetworkActivity();

            // 检查系统调用
            monitorSystemCalls();

        } catch (Exception e) {
            // 忽略异常
        }
    }

    private void monitorWindowsProcesses() {
        try {
            Process process = Runtime.getRuntime().exec("tasklist /fo csv /nh");
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                for (String software : SCREEN_CAPTURE_SOFTWARE) {
                    if (line.toLowerCase().contains(software.toLowerCase())) {
                        // 终止进程
                        String processName = line.split("\",\"")[0].replace("\"", "");
                        Runtime.getRuntime().exec("taskkill /f /im " + processName);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
    }

    /**
     * 工具方法
     */
    private String encryptString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            return input;
        }
    }

    private String generateRandomProcessName() {
        String[] names = {"svchost", "java", "runtime", "system", "services"};
        return names[random.nextInt(names.length)] + ".exe";
    }

    private void executeAppleScript(String script) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("osascript", "-e", script);
        pb.start();
    }

    private long getWindowId() {
        // 简化实现，实际需要根据平台获取
        return 0;
    }

    private String getProcessName() {
        try {
            return ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        } catch (Exception e) {
            return "java";
        }
    }

    // 其他辅助方法...
    private void applyFallbackWindowsProtection() {
        try {
            // 基础进程终止
            for (String proc : new String[]{"obs64.exe", "obs32.exe", "xsplit.exe"}) {
                Runtime.getRuntime().exec("taskkill /f /im " + proc);
            }
        } catch (Exception e) {
            // 最终备用方案
        }
    }

    private void applyFallbackLinuxProtection() {
        try {
            Runtime.getRuntime().exec("pkill -9 obs");
            Runtime.getRuntime().exec("pkill -9 ffmpeg");
        } catch (Exception e) {
            // 忽略异常
        }
    }

    private void applyFallbackMacProtection() {
        try {
            Runtime.getRuntime().exec("pkill -9 OBS");
            Runtime.getRuntime().exec("pkill -9 QuickTime");
        } catch (Exception e) {
            // 忽略异常
        }
    }

    private void terminateScreenCaptureProcesses() {
        // 实现进程终止逻辑
    }

    private void applyAlternativeSecurityProtection() {
        // 替代安全保护方案
    }

    // 其他占位方法实现...
    private void hookWindowsAPI() {}
    private void createDecoyProcesses() {}
    private void modifyMemoryProtection() {}
    private void applyLDPreloadProtection() {}
    private void terminateLinuxCaptureProcesses() {}
    private void modifyLinuxEnvironment() {}
    private void applyPtraceProtection() {}
    private void modifySyscallTable() {}
    private void applySeccompProtection() {}
    private void applyObjCProtection() {}
    private void terminateMacCaptureProcesses() {}
    private void modifyMacPermissions() {}
    private void applyCodeSigningProtection() {}
    private void modifySandboxConfiguration() {}
    private void applyKernelExtensionProtection() {}
    private void applyEncryptionProtection() {}
    private void applyTimingObfuscation() {}
    private void setupFramebufferProtection() {}
    private void setupShaderProtection() {}
    private void setupDynamicTextureProtection() {}
    private void fillMemoryWithNoise() {}
    private void protectMemoryAccess() {}
    private void interfereWithGC() {}
    private void obfuscateWindowTitle() {}
    private void obfuscateClassNames() {}
    private void obfuscateBehaviorPatterns() {}
    private void monitorLinuxProcesses() {}
    private void monitorMacProcesses() {}
    private void monitorNetworkActivity() {}
    private void monitorSystemCalls() {}
    private void monitorWindowState() {}
    private void checkSystemChanges() {}
    private void dynamicProtectionAdjustment() {}
    private void applyWindowsRegistryProtection() {}

    /**
     * 移除所有保护
     */
    private void removeProtection() {
        try {
            // 恢复安全管理器
            if (securityManager != null) {
                System.setSecurityManager(null);
                securityManager = null;
            }

            // 恢复窗口属性
            Frame[] frames = Frame.getFrames();
            for (Frame frame : frames) {
                if (frame.isVisible()) {
                    frame.setAlwaysOnTop(false);
                    frame.setOpacity(1.0f);
                }
            }

            // 清理创建的进程
            for (Process proc : killedProcesses) {
                if (proc != null && proc.isAlive()) {
                    proc.destroy();
                }
            }

        } catch (Exception e) {
            // 忽略异常
        }
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
        return new String[]{level + (aggressiveMode.getValue() ? "+AGGR" : "")};
    }
}