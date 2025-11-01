package nightsky.module.modules.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.ScorePlayerTeam;
import nightsky.event.EventTarget;
import nightsky.events.Render2DEvent;
import nightsky.module.Module;
import nightsky.module.modules.player.ChestStealer;
import nightsky.module.modules.misc.NickHider;
import nightsky.notification.*;
import nightsky.util.IpProtected;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.IntValue;
import nightsky.value.values.FloatValue;
import nightsky.font.FontTransformer;
import nightsky.font.CustomFontRenderer;
import nightsky.util.render.BlurUtil;
import nightsky.ui.command.CommandInterface;
import nightsky.events.KeyEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DynamicIsland extends Module {

    private final FloatValue animationSpeed = new FloatValue("AnimationSpeed", 12.0f, 1.0f, 20.0f);
    private final BooleanValue blur = new BooleanValue("Blur", true);
    private final IntValue blurStrength = new IntValue("BlurStrength", 100, 1, 200);
    private final BooleanValue dropShadow = new BooleanValue("Shadow", true);
    private final IntValue bgAlpha = new IntValue("BackgroundAlpha", 40, 1, 255);
    private final BooleanValue showNotifications = new BooleanValue("ShowNotifications", true);

    private final Minecraft mc = Minecraft.getMinecraft();

    private final float alpha = bgAlpha.getValue() * 0.0003f;
    private final float shadowRadius = 8f;
    private final float shadowSpread = 0.07f;
    private final float shadowAlpha = 0.03f + alpha;

    private double currentWidth = 0;
    private double currentHeight = 0;
    private double targetWidth = 0;
    private double targetHeight = 0;
    private long animationStartTime = 0;
    private double lastTargetWidth = 0;
    private double lastTargetHeight = 0;
    private boolean isExpanding = false;
    private int bounceCount = 0;
    private long bounceStartTime = 0;
    private boolean wasInNoticeState = false;

    private final float textSpacing = 10f;

    private boolean showingCommandResult = false;
    private String commandResultType = "";
    private String commandResultTitle = "";
    private String commandResultContent = "";
    private long commandResultStartTime = 0;
    private static final long COMMAND_RESULT_DURATION = 3000;
    
    private final long breathingStartTime = System.currentTimeMillis();


    //这个东西爆炸了。所以被注释掉了。
    private boolean chestExpanded = false;


    private final CommandInterface commandInterface = new CommandInterface();
    
    private boolean tabPressed = false;
    private boolean showingPlayerList = false;
    private float playerListAlpha = 0.0f;

    private long lastUpdateTime = System.currentTimeMillis();
    
    private boolean wasShowingNotifications = false;
    private float mainInterfaceSlideOffset = 0.0f;
    private long mainInterfaceSlideStartTime = 0;
    private static final long MAIN_INTERFACE_SLIDE_DURATION = 400L;
    
    private final List<Integer> availableTextIndices = new ArrayList<>();
    private int currentTextIndex = 0;
    private long lastTextChangeTime = System.currentTimeMillis();
    private static final long TEXT_CHANGE_INTERVAL = 5000;
    private float textAlpha = 1.0f;
    private boolean textFadingOut = false;
    private int nextTextIndex = 0;
    
    private float minLine2Width = 0.0f;
    private float currentDisplayWidth = 0.0f;

    //神秘。
    private final String[] islandText = new String[]{
            "       赛博上帝会梦到AI天堂吗      ",
            "好想用一行/kill @e结束这名为生活的Minecraft",
            "在人生的自动售货机中堆满了一个又一个的emoji",
            "灵魂在404界面找到了他的归宿   ",
            "这里没有加载中的圆圈，只有永恒的静默",
            "你是新版本，不再兼容我的旧依赖",
            "梦想.zip (确定要永久删除这一项吗)",
            "人生是一场没有热更新的游戏     ",
            "Hello, World!                       ",
            "孤独.dll（系统文件，无法删除） ",
            "情绪.css（该文件已损坏）      ",
            "我们都在制造一场前所未有的混乱...",
            "  希望.ini（0kb）                  ",
            "人生协议未读即签，灵魂权限让渡未知",
            "回忆.zip.7z.tar.gz.null         ",
            "我设置了早上6点自动关机，逃避那迟迟加载不出的明天",
            "信仰在在虚拟机上完美运行，却忘了连接真实世界的接口",
            "热爱在启动时占用所有资源，崩溃后却找不到一丝痕迹",
            "我们都在参加一场没有终点的开发  ",
            "群体智慧.hivemind                "
    };

    //别问我这一坨是啥。
    //我不知道。ds写的。不是我写的。
    //糖丸了。

//    private String islandPoemText1 = "荧屏闪烁乱真假，数据洪流没年华。\n" +
//            "热键空按心已麻，表情虚掷笑渐沙。\n" +
//            "旧梦沉入代码海，新魂困于云端枷。\n" +
//            "报错界面觅归舟，长夜独坐待曦霞。";
//    private String islandPoemText2 = "指尖划过冷光屏，万千消息乱心庭\n" +
//            "点赞评论如潮涌，真实自我渐飘零\n" +
//            "旧日书信尘封柜，今朝言语化数据\n" +
//            "欲向虚空觅知音，只听机器复回音";
//    private String islandPoemText3 = "朝朝暮暮对荧屏，世事皆成二维码\n" +
//            "美食先让手机尝，美景终归滤镜下\n" +
//            "忽忆年少奔跑时，风拂脸颊是真切\n" +
//            "今陷数字迷宫中，何处寻回当初我";
//    private String islandPoemText4 = "区块链上说永恒，智能合约写誓言\n" +
//            "代码或可保长久，难护真心易变迁\n" +
//            "昨夜更新系统后，旧情竟成兼容难\n" +
//            "爱情若作应用程序，终将面临卸载时";
//    private String islandPoemText5 = "一生记忆传云端，却怕某天服务器崩\n" +
//            "电子相册存笑貌，难载当时温暖风\n" +
//            "欲将心事托云储，又恐黑客破隐私\n" +
//            "虚实之间徘徊久，不知何处安魂灵";
//    private String islandPoemText6 = "短视频里度春秋，注意力经济中游\n" +
//            "十五秒换一世界，三分钟忘旧时忧\n" +
//            "信息爆炸填满脑，知识碎片难成舟\n" +
//            "夜深人静扪心问，可还存有沉思力";
//    private String islandPoemText7 = "虚拟现实头盔戴，进入完美新世界\n" +
//            "触感反馈虽逼真，难比清风拂面切\n" +
//            "游戏角色已满级，现实人生却卡关\n" +
//            "摘下设备茫然顾，哪个才是真实我";
//    private String islandPoemText8 = "推荐内容精准投，困在信息茧房中\n" +
//            "万千世界虽广阔，只见相似观点浓\n" +
//            "欲破此牢寻异见，算法笑我太天真\n" +
//            "你之选择你造就，何怪代码太精通";
//    private String islandPoemText9 = "区域突然断网络，始觉已陷数字依赖\n" +
//            "坐立难安无所事，方知除却屏幕外\n" +
//            "尚有书本可阅读，尚有亲人可交谈\n" +
//            "此夜过后深思量，生活当有线下版";
//    private String islandPoemText0 = "生命若似电子设备，终有关机那一天\n" +
//            "所有数据成遗产，所有账号变纪念\n" +
//            "但愿回首这一生，不全是虚拟交互\n" +
//            "曾真实爱过痛过，方不负人间此行";
//    private String islandPoemTitle1 = "《网逝》";
//    private String islandPoemTitle2 = "《芯囚》";
//    private String islandPoemTitle3 = "《网境》";
//    private String islandPoemTitle4 = "《链情》";
//    private String islandPoemTitle5 = "《云踪》";
//    private String islandPoemTitle6 = "《流年》";
//    private String islandPoemTitle7 = "《拟真》";
//    private String islandPoemTitle8 = "《算法》";
//    private String islandPoemTitle9 = "《寂夜》";
//    private String islandPoemTitle0 = "《终局》";

    public DynamicIsland() {
        super("DynamicIsland", false);
        initializeTextIndices();
    }
    
    private void initializeTextIndices() {
        availableTextIndices.clear();
        for (int i = 0; i < islandText.length; i++) {
            availableTextIndices.add(i);
        }
        java.util.Collections.shuffle(availableTextIndices);
        currentTextIndex = availableTextIndices.remove(0);
    }

    public void showCommandResult(String type, String title, String content) {
        this.commandResultType = type;
        this.commandResultTitle = title;
        this.commandResultContent = content;
        this.showingCommandResult = true;
        this.commandResultStartTime = System.currentTimeMillis();
    }

    public static DynamicIsland getInstance() {
        return (DynamicIsland) nightsky.NightSky.moduleManager.getModule("DynamicIsland");
    }
    
    @EventTarget
    public void onKey(KeyEvent event) {
        if (event.getKey() == Keyboard.KEY_PERIOD && !commandInterface.isActive() && mc.currentScreen == null) {
            commandInterface.activate();
        }
        
        if (event.getKey() == Keyboard.KEY_TAB && mc.currentScreen == null) {
            if (!tabPressed) {
                tabPressed = true;
                showingPlayerList = true;
            }
        }
    }
    public boolean isCommandActive() {
        return commandInterface.isActive();
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if(!this.isEnabled()) {
            return;
        }
        
        NotificationRenderer.toggleBgRadius = 18f;
        NotificationRenderer.toggleButtonRadius = 10f;
        
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
        lastUpdateTime = currentTime;
        
        updateTextRotation(deltaTime);
        updateTabState(deltaTime);
        
        
        ChestStealer chestStealer = (ChestStealer) nightsky.NightSky.moduleManager.modules.get(ChestStealer.class);
        chestExpanded = chestStealer != null && chestStealer.isEnabled() && ChestData.getInstance().isChestOpen() && !commandInterface.isActive();

        ScaledResolution sr = new ScaledResolution(mc);
        buildDisplayText();

        List<Notification> activeNotifications = NotificationManager.getInstance().getActiveNotifications();
        boolean hasNotifications = !activeNotifications.isEmpty() && showNotifications.getValue();
        
        if (wasShowingNotifications && !hasNotifications) {
            mainInterfaceSlideStartTime = currentTime;
            mainInterfaceSlideOffset = (float)currentWidth;
        }
        wasShowingNotifications = hasNotifications;
        
        if (!hasNotifications && mainInterfaceSlideOffset > 0) {
            long elapsed = currentTime - mainInterfaceSlideStartTime;
            float progress = Math.min(1.0f, elapsed / (float)MAIN_INTERFACE_SLIDE_DURATION);
            float eased = easeOutCubic(progress);
            mainInterfaceSlideOffset = (float)currentWidth * (1 - eased);
            
            if (progress >= 1.0f) {
                mainInterfaceSlideOffset = 0.0f;
            }
        }

        double padding = 12;
        
//        if (chestExpanded) {
//            ChestData chestData = ChestData.getInstance();
//            int chestSize = chestData.getChestSize();
//            int rows = Math.max(3, chestSize / 9);
//            targetWidth = 9 * 20 + padding * 3;
//            targetHeight = rows * 20 + padding * 3;
//        } else
            if (commandInterface.isActive()) {
            targetWidth = commandInterface.getExpandedWidth();
            targetHeight = commandInterface.getExpandedHeight();

            if (mc.currentScreen == null) {
                mc.displayGuiScreen(new net.minecraft.client.gui.GuiScreen() {
                    @Override
                    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
                        commandInterface.handleMouseMove(mouseX, mouseY);
                    }
                    
                    @Override
                    public boolean doesGuiPauseGame() {
                        return true;
                    }
                    
                    @Override
                    protected void keyTyped(char typedChar, int keyCode) {
                        commandInterface.handleKeyInput(keyCode, typedChar);
                    }
                    
                    @Override
                    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
                        commandInterface.handleMouseClick(mouseX, mouseY, mouseButton);
                    }
                    
                    @Override
                    public void handleMouseInput() throws java.io.IOException {
                        super.handleMouseInput();
                        commandInterface.handleMouseScroll();
                    }
                });
            }
        } else {
            if (mc.currentScreen != null && mc.currentScreen.getClass().isAnonymousClass() && 
                mc.currentScreen.getClass().getEnclosingClass() == DynamicIsland.class) {
                mc.displayGuiScreen(null);
            }
        }
            
        if (commandInterface.isActive()) {
        } else if (showingPlayerList) {
            Collection<NetworkPlayerInfo> playerInfos = mc.getNetHandler().getPlayerInfoMap();
            double playerListWidth = calculatePlayerListWidth(playerInfos);
            targetWidth = playerListWidth + padding * 2;
            targetHeight = calculatePlayerListHeight(playerInfos) + padding * 2;
        } else if (showingCommandResult) {
            double commandResultSize = calculateCommandResultSize();
            targetWidth = commandResultSize + padding * 2;
            targetHeight = calculateCommandResultHeight() + padding * 2;
        } else if (hasNotifications) {
            double notificationContentWidth = calculateNotificationContentWidth(activeNotifications);
            targetWidth = notificationContentWidth + padding;
            
            float scale = 0.8f;
            float totalHeight = 0;
            for (Notification notification : activeNotifications) {
                totalHeight += calculateNotificationItemHeight(notification.getType()) * scale;
            }
            targetHeight = totalHeight + padding * 2;
        } else {
            String serverIP = getServerIP();
            int ping = mc.getCurrentServerData() != null ? (int)mc.getCurrentServerData().pingToServer : 0;
            int fps = net.minecraft.client.Minecraft.getDebugFPS();

            float scale = 0.58f;
            
            FontTransformer transformer = FontTransformer.getInstance();
            Font mainFont = transformer.getFont("MicrosoftYaHei", 50);
            Font islandTextFont = transformer.getFont("MicrosoftYaHei", 38);
            Font contentFont = transformer.getFont("MicrosoftYaHei", 40);

            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("M/d");
            java.text.SimpleDateFormat weekFormat = new java.text.SimpleDateFormat("EEE", java.util.Locale.ENGLISH);
            java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm");
            java.util.Date now = new java.util.Date();

            String dateStr = dateFormat.format(now);
            String weekStr = weekFormat.format(now);
            String timeStr = timeFormat.format(now);

            String line2Base = dateStr + " " + weekStr + " " + timeStr + " " + ping + "ms to " + serverIP + " " + fps + " FPS";
            float line2BaseWidth = CustomFontRenderer.getStringWidth(line2Base, contentFont) * scale;
            
            if (minLine2Width == 0.0f) {
                minLine2Width = line2BaseWidth;
            }
            
            String currentText = islandText[currentTextIndex];
            
            float nightskyWidth = CustomFontRenderer.getStringWidth("NightSky", mainFont);
            float currentTextWidth = CustomFontRenderer.getStringWidth(currentText, islandTextFont);
            
            float line1BaseWidth = nightskyWidth + 10;
            float line1TotalWidth = (line1BaseWidth + currentTextWidth) * scale;
            
            float requiredWidth = line1TotalWidth + 80 * scale;

            if (currentDisplayWidth == 0.0f) {
                currentDisplayWidth = requiredWidth;
            } else {
                float widthDelta = requiredWidth - currentDisplayWidth;
                float widthSpeed = 3.5f;
                if (Math.abs(widthDelta) > 0.5f) {
                    currentDisplayWidth += widthDelta * widthSpeed * deltaTime;
                } else {
                    currentDisplayWidth = requiredWidth;
                }
            }
            
            int mainFontHeight = CustomFontRenderer.getFontHeight(mainFont);
            int contentFontHeight = CustomFontRenderer.getFontHeight(contentFont);
            float lineSpacing = 4 * scale;
            
            targetWidth = currentDisplayWidth + padding * 3.5;
            targetHeight = (mainFontHeight * scale + contentFontHeight * scale + lineSpacing) + padding * 1.2;
        }

        updateAnimations();

        double finalWidth = getBreathingWidth();
        double finalHeight = getBreathingHeight();
        
        if (animationStartTime > 0) {
            finalWidth = Math.max(finalWidth, getBounceWidth());
            finalHeight = Math.max(finalHeight, getBounceHeight());
        }
        
        drawDynamicIsland(sr, finalWidth, finalHeight);
    }


    public boolean shouldHideTabList() {
        return this.isEnabled() && showingPlayerList;
    }
    
    private void updateTextRotation(float deltaTime) {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastTextChangeTime >= TEXT_CHANGE_INTERVAL) {
            if (!textFadingOut) {
                textFadingOut = true;
                if (availableTextIndices.isEmpty()) {
                    initializeTextIndices();
                }
                nextTextIndex = availableTextIndices.remove(0);
            }
        }
        
        float fadeSpeed = 3.0f;
        
        if (textFadingOut) {
            textAlpha -= fadeSpeed * deltaTime;
            if (textAlpha <= 0.0f) {
                textAlpha = 0.0f;
                currentTextIndex = nextTextIndex;
                textFadingOut = false;
                lastTextChangeTime = currentTime;
            }
        } else {
            if (textAlpha < 1.0f) {
                textAlpha += fadeSpeed * deltaTime;
                if (textAlpha > 1.0f) {
                    textAlpha = 1.0f;
                }
            }
        }
    }
    
    private void updateTabState(float deltaTime) {
        if (tabPressed && !Keyboard.isKeyDown(Keyboard.KEY_TAB)) {
            tabPressed = false;
            showingPlayerList = false;
            playerListAlpha = 0.0f;
        }

        float PLAYER_LIST_FADE_SPEED = 8.0f;
        if (showingPlayerList) {
            playerListAlpha = Math.min(1.0f, playerListAlpha + PLAYER_LIST_FADE_SPEED * deltaTime);
        } else {
            playerListAlpha = Math.max(0.0f, playerListAlpha - PLAYER_LIST_FADE_SPEED * deltaTime);
        }
    }
    
    private float easeOutCubic(float t) {
        return 1 - (float)Math.pow(1 - t, 3);
    }

    private void buildDisplayText() {
        if (showNotifications.getValue()) {
            NotificationManager.getInstance().getCurrentNotification();
        }
    }

    private void updateAnimations() {
        long currentTime = System.currentTimeMillis();
        
        List<Notification> activeNotifications = NotificationManager.getInstance().getActiveNotifications();
        boolean hasNotifications = !activeNotifications.isEmpty() && showNotifications.getValue();
        
        if (targetWidth != lastTargetWidth || targetHeight != lastTargetHeight) {
            animationStartTime = currentTime;
            bounceStartTime = currentTime;
            lastTargetWidth = targetWidth;
            lastTargetHeight = targetHeight;
            isExpanding = targetWidth > currentWidth || targetHeight > currentHeight;
            bounceCount = 0;
        }
        
        wasInNoticeState = hasNotifications;
        
        double widthDiff = targetWidth - currentWidth;
        double heightDiff = targetHeight - currentHeight;
        
        float speed = animationSpeed.getValue() * 0.01f;
        currentWidth += widthDiff * speed;
        currentHeight += heightDiff * speed;
        
        if (Math.abs(widthDiff) < 0.5) {
            currentWidth = targetWidth;
        }
        if (Math.abs(heightDiff) < 0.5) {
            currentHeight = targetHeight;
        }
    }
    
    private double getBounceWidth() {
        long currentTime = System.currentTimeMillis();
        
        if (targetWidth != lastTargetWidth || targetHeight != lastTargetHeight) {
            return currentWidth;
        }
        
        List<Notification> activeNotifications = NotificationManager.getInstance().getActiveNotifications();
        boolean hasNotifications = !activeNotifications.isEmpty() && showNotifications.getValue();
        boolean isEnteringNoticeState = hasNotifications && !wasInNoticeState;
        
        float animationDuration = isExpanding ? 450.0f : 500.0f;
        int maxBounces = (isExpanding && isEnteringNoticeState) ? 2 : 0;
        float bounceDuration = isExpanding ? (bounceCount == 0 ? 0.0f : 450.0f) : 0.0f;
        
        float totalProgress = Math.min((currentTime - animationStartTime) / animationDuration, 1.0f);
        float bounceProgress = Math.min((currentTime - bounceStartTime) / bounceDuration, 1.0f);
        
        if (totalProgress < 1.0f && bounceCount < maxBounces) {
            if (bounceProgress >= 1.0f) {
                bounceCount++;
                bounceStartTime = currentTime;
                bounceProgress = 0;
            }
            
            float bounceIntensity;
            if (isExpanding) {
                bounceIntensity = bounceCount == 0 ? 0.0f : 0.09f;
            } else {
                bounceIntensity = 0.0f;
            }
            
            if (bounceProgress < 0.5f) {
                float expandProgress = bounceProgress * 2;
                float overshoot = (float)Math.sin(expandProgress * Math.PI) * bounceIntensity;
                return currentWidth * (1.0f + overshoot);
            } else {
                float contractProgress = (bounceProgress - 0.5f) * 2;
                float undershoot = (float)Math.sin(contractProgress * Math.PI) * bounceIntensity * 0.5f;
                return currentWidth * (1.0f - undershoot);
            }
        }
        
        return currentWidth;
    }
    
    private double getBounceHeight() {
        long currentTime = System.currentTimeMillis();
        
        if (targetWidth != lastTargetWidth || targetHeight != lastTargetHeight) {
            return currentHeight;
        }
        
        List<Notification> activeNotifications = NotificationManager.getInstance().getActiveNotifications();
        boolean hasNotifications = !activeNotifications.isEmpty() && showNotifications.getValue();
        boolean isEnteringNoticeState = hasNotifications && !wasInNoticeState;
        
        float animationDuration = isExpanding ? 450.0f : 500.0f;
        int maxBounces = (isExpanding && isEnteringNoticeState) ? 2 : 0;
        float bounceDuration = isExpanding ? (bounceCount == 0 ? 0.0f : 450.0f) : 0.0f;
        
        float totalProgress = Math.min((currentTime - animationStartTime) / animationDuration, 1.0f);
        float bounceProgress = Math.min((currentTime - bounceStartTime) / bounceDuration, 1.0f);
        
        if (totalProgress < 1.0f && bounceCount < maxBounces) {
            if (bounceProgress >= 1.0f) {
                bounceCount++;
                bounceStartTime = currentTime;
                bounceProgress = 0;
            }
            
            float bounceIntensity;
            if (isExpanding) {
                bounceIntensity = bounceCount == 0 ? 0.0f : 0.09f;
            } else {
                bounceIntensity = 0.0f;
            }
            
            if (bounceProgress < 0.5f) {
                float expandProgress = bounceProgress * 2;
                float overshoot = (float)Math.sin(expandProgress * Math.PI) * bounceIntensity;
                return currentHeight * (1.0f + overshoot);
            } else {
                float contractProgress = (bounceProgress - 0.5f) * 2;
                float undershoot = (float)Math.sin(contractProgress * Math.PI) * bounceIntensity * 0.5f;
                return currentHeight * (1.0f - undershoot);
            }
        }
        
        return currentHeight;
    }
    
    private double getBreathingWidth() {
        List<Notification> activeNotifications = NotificationManager.getInstance().getActiveNotifications();
        boolean hasNotifications = !activeNotifications.isEmpty() && showNotifications.getValue();

        if (!hasNotifications && !chestExpanded && !commandInterface.isActive()) {
            long currentTime = System.currentTimeMillis();
            float breathingCycle = (currentTime - breathingStartTime) / 3000.0f;
            float breathingIntensity = (float)(Math.sin(breathingCycle * Math.PI * 2) * 0.5 + 0.5);
            float breathingScale = 1.0f + breathingIntensity * 0.03f;
            return currentWidth * breathingScale;
        }
        else if (commandInterface.isActive() && commandInterface.isShowingKeyboard()) {
            long currentTime = System.currentTimeMillis();
            float breathingCycle = (currentTime - breathingStartTime) / 3000.0f;
            float breathingIntensity = (float)(Math.sin(breathingCycle * Math.PI * 2) * 0.5 + 0.5);
            float breathingScale = 1.0f + breathingIntensity * 0.01f;
            return currentWidth * breathingScale;
        }
        return currentWidth;
    }
    
    private double getBreathingHeight() {
        List<Notification> activeNotifications = NotificationManager.getInstance().getActiveNotifications();
        boolean hasNotifications = !activeNotifications.isEmpty() && showNotifications.getValue();

        if (!hasNotifications && !chestExpanded && !commandInterface.isActive()) {
            long currentTime = System.currentTimeMillis();
            float breathingCycle = (currentTime - breathingStartTime) / 3000.0f;
            float breathingIntensity = (float)(Math.sin(breathingCycle * Math.PI * 2) * 0.5 + 0.5);
            float breathingScale = 1.0f + breathingIntensity * 0.03f;
            return currentHeight * breathingScale;
        }
        else if (commandInterface.isActive() && commandInterface.isShowingKeyboard()) {
            long currentTime = System.currentTimeMillis();
            float breathingCycle = (currentTime - breathingStartTime) / 3000.0f;
            float breathingIntensity = (float)(Math.sin(breathingCycle * Math.PI * 2) * 0.5 + 0.5);
            float breathingScale = 1.0f + breathingIntensity * 0.01f;
            return currentHeight * breathingScale;
        }
        return currentHeight;
    }
    

    private void drawDynamicIsland(ScaledResolution sr, double width, double height) {

        double x = (sr.getScaledWidth() - width) / 2.0;
        double y;
        y = 8;

        List<Notification> activeNotifications = NotificationManager.getInstance().getActiveNotifications();
        boolean hasNotifications = !activeNotifications.isEmpty() && showNotifications.getValue();

        float radius;
//        if (chestExpanded) {
//            radius = 12.0f;
//        } else
                if(hasNotifications){
            radius = 39.0f;
        } else {
            radius = 32.0f;
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        
        if (dropShadow.getValue()) {
            drawDropShadowBackground((float)x, (float)y, (float)width, (float)height, radius);
        }
        
        if (blur.getValue()) {
            BlurUtil.blurAreaRounded((float)x, (float)y, (float)(x + width), (float)(y + height), radius, blurStrength.getValue().floatValue());
        }
        
        nightsky.util.render.RenderUtil.drawRoundedRect((float)x, (float)y, (float)width, (float)height, radius, new Color(0, 0, 0, bgAlpha.getValue()));

        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        double contentWidth = currentWidth;
        double contentHeight = currentHeight;
        double contentX = (sr.getScaledWidth() - contentWidth) / 2.0;

        if (showingCommandResult && System.currentTimeMillis() - commandResultStartTime > COMMAND_RESULT_DURATION) {
            showingCommandResult = false;
        }

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        int scaleFactor = sr.getScaleFactor();
        int scissorX = (int) (x * scaleFactor);
        int scissorY = (int) (mc.displayHeight - (y + height) * scaleFactor);
        int scissorWidth = (int) (width * scaleFactor);
        int scissorHeight = (int) (height * scaleFactor);
        GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight);

//        if (chestExpanded) {
//            drawChestInterface((float)contentX, (float)y, (float)contentWidth, (float)contentHeight);
//        } else
            if (commandInterface.isActive()) {
            commandInterface.render((float)contentX, (float)y, (float)contentWidth, (float)contentHeight);
        } else if (showingPlayerList) {
            drawPlayerList((float)contentX, (float)y, (float)contentWidth, (float)contentHeight);
        } else if (showingCommandResult) {
            drawCommandResult((float)contentX, (float)y);
        } else if (hasNotifications) {
            NotificationRenderer.drawMultipleNotifications(activeNotifications, (float)contentX, (float)y, (float)contentWidth, (float)contentHeight, 0.8f);
        } else {
            float slideX = (float)contentX - mainInterfaceSlideOffset;
            float slideAlpha = mainInterfaceSlideOffset > 0 ? 1.0f - (mainInterfaceSlideOffset / (float)contentWidth) : 1.0f;
            
            GlStateManager.pushMatrix();
            GlStateManager.color(1.0f, 1.0f, 1.0f, slideAlpha);
            drawMainInterface(slideX, (float)y, (float)contentWidth);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.popMatrix();
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private double calculateNotificationContentWidth(List<Notification> notifications) {
        double maxWidth = 0;
        double padding = 4;
        float scale = 0.8f;
        
        for (Notification notification : notifications) {
            double contentWidth;
            
            switch (notification.getType()) {
                case SCAFFOLDING:
                    contentWidth = calculateScaffoldingWidth(padding);
                    break;
                case BED_NUKER:
                    contentWidth = calculateBedNukerWidth(padding);
                    break;
                case MODULE_ENABLED:
                case MODULE_DISABLED:
                    contentWidth = calculateModuleNotificationWidth(notification, padding);
                    break;
                default:
                    contentWidth = calculateRegularNotificationWidth(notification, padding);
                    break;
            }
            
            maxWidth = Math.max(maxWidth, contentWidth * scale);
        }
        
        return maxWidth;
    }
    
    private double calculateScaffoldingWidth(double padding) {
        FontTransformer transformer = FontTransformer.getInstance();
        Font otherFont = transformer.getFont("OpenSansSemiBold", 35);
        
        String title = "Scaffold Toggled";
        double titleWidth = CustomFontRenderer.getStringWidth(title, otherFont);
        
        ScaffoldData scaffoldData = ScaffoldData.getInstance();
        String status = scaffoldData.getBlocksLeft() + " blocks left · " + 
                       String.format("%.2f", scaffoldData.getBlocksPerSecond()) + " block/s";
        double statusWidth = CustomFontRenderer.getStringWidth(status, otherFont);
        
        double iconWidth = 12;
        double iconPadding = 18;
        
        double maxTextWidth = Math.max(titleWidth, statusWidth);
        return padding + iconWidth + iconPadding + maxTextWidth;
    }
    
    private double calculateBedNukerWidth(double padding) {
        FontTransformer transformer = FontTransformer.getInstance();
        Font otherFont = transformer.getFont("OpenSansSemiBold", 35);
        
        nightsky.notification.BedNukerData bedNukerData = nightsky.notification.BedNukerData.getInstance();
        
        String breakingText = "Breaking " + bedNukerData.getTargetBlockName();
        double breakingWidth = CustomFontRenderer.getStringWidth(breakingText, otherFont);
        
        String progressText = "Break Progress: 100%";
        double progressWidth = CustomFontRenderer.getStringWidth(progressText, otherFont);
        
        double iconWidth = 12;
        double iconPadding = 18;
        
        double maxTextWidth = Math.max(breakingWidth, progressWidth);
        return padding + iconWidth + iconPadding + maxTextWidth;
    }
    
    private double calculateModuleNotificationWidth(Notification notification, double padding) {
        FontTransformer transformer = FontTransformer.getInstance();
        Font otherFont = transformer.getFont("OpenSansSemiBold", 37);
        
        String title = "ModuleToggled";
        double titleWidth = CustomFontRenderer.getStringWidth(title, otherFont);
        
        String moduleName = notification.getTitle();
        String hasBeenText = " Has Been ";
        String statusText = notification.getType() == Notification.NotificationType.MODULE_ENABLED ? "Enabled" : "Disabled";
        String exclamation = "!";
        
        double statusLineWidth = CustomFontRenderer.getStringWidth(moduleName + hasBeenText + statusText + exclamation, otherFont);
        
        double switchWidth = 32;
        double switchPadding = 8;
        
        double maxTextWidth = Math.max(titleWidth, statusLineWidth);
        return padding + switchWidth + switchPadding + maxTextWidth;
    }
    
    private double calculateRegularNotificationWidth(Notification notification, double padding) {
        FontTransformer transformer = FontTransformer.getInstance();
        Font otherFont = transformer.getFont("OpenSansSemiBold", 35);
        
        String typeText = getNotificationTypeDisplayName(notification.getType());
        double typeWidth = CustomFontRenderer.getStringWidth(typeText, otherFont);
        
        String displayText = notification.getTitle();
        if (!notification.getMessage().isEmpty()) {
            displayText += " " + notification.getMessage();
        }
        double contentWidth = CustomFontRenderer.getStringWidth(displayText, otherFont);
        
        double maxTextWidth = Math.max(typeWidth, contentWidth);
        return padding + maxTextWidth;
    }
    
    private String getNotificationTypeDisplayName(Notification.NotificationType type) {
        switch (type) {
            case ERROR:
                return "Error";
            case WARNING:
                return "Warning";
            case INFO:
                return "Info";
            case SCAFFOLDING:
                return "Scaffolding";
            case BED_NUKER:
                return "BedNuker";
            default:
                return "Notification";
        }
    }
    
    private double calculatePlayerListWidth(Collection<NetworkPlayerInfo> playerInfos) {
        FontTransformer transformer = FontTransformer.getInstance();
        Font otherFont = transformer.getFont("OpenSansSemiBold", 35);
        
        double maxWidth = 200;
        for (NetworkPlayerInfo playerInfo : playerInfos) {
            String playerName = getPlayerName(playerInfo);
            String pingText = playerInfo.getResponseTime() + "ms";
            String cleanName = net.minecraft.util.EnumChatFormatting.getTextWithoutFormattingCodes(playerName);
            String fullText = cleanName + " " + pingText;
            double textWidth = CustomFontRenderer.getStringWidth(fullText, otherFont);
            maxWidth = Math.max(maxWidth, textWidth + 40);
        }
        
        String serverInfo = getServerIP();
        String clientInfo = "NightSky";
        String pingInfo = "Ping: " + (mc.getCurrentServerData() != null ? mc.getCurrentServerData().pingToServer + "ms" : "0ms");
        
        NickHider nickHider = (NickHider) nightsky.NightSky.moduleManager.modules.get(NickHider.class);
        if (nickHider != null && nickHider.isEnabled()) {
            serverInfo = nickHider.replaceNick(serverInfo);
        }
        
        maxWidth = Math.max(maxWidth, CustomFontRenderer.getStringWidth(serverInfo, otherFont) + 40);
        maxWidth = Math.max(maxWidth, CustomFontRenderer.getStringWidth(clientInfo, otherFont) + 40);
        maxWidth = Math.max(maxWidth, CustomFontRenderer.getStringWidth(pingInfo, otherFont) + 40);
        
        return Math.min(maxWidth, 400);
    }
    
    private double calculatePlayerListHeight(Collection<NetworkPlayerInfo> playerInfos) {
        FontTransformer transformer = FontTransformer.getInstance();
        Font otherFont = transformer.getFont("OpenSansSemiBold", 35);
        int fontHeight = CustomFontRenderer.getFontHeight(otherFont);
        
        int playerCount = Math.min(playerInfos.size(), 16);
        double playerListHeight = playerCount * (fontHeight + 6);
        double headerHeight = fontHeight + 8;
        double footerHeight = fontHeight * 3 + 16;
        return Math.max(playerListHeight + headerHeight + footerHeight, 120);
    }
    
    private void drawPlayerList(float x, float y, float width, float height) {
        FontTransformer transformer = FontTransformer.getInstance();
        Font otherFont = transformer.getFont("OpenSansSemiBold", 35);
        int fontHeight = CustomFontRenderer.getFontHeight(otherFont);
        
        Collection<NetworkPlayerInfo> playerInfos = mc.getNetHandler().getPlayerInfoMap();
        float padding = 12;
        float currentY = y + padding;
        
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        
        String headerText = "Players (" + playerInfos.size() + ")";
        float headerX = x + (width - CustomFontRenderer.getStringWidth(headerText, otherFont)) / 2;
        CustomFontRenderer.drawStringWithShadow(headerText, headerX, currentY, 0x55AAFF, otherFont);
        currentY += fontHeight + 8;
        
        nightsky.util.render.RenderUtil.drawRect(x + padding, currentY - 2, width - padding * 2, 1, 0x33FFFFFF);
        currentY += 6;
        
        int displayCount = 0;
        int maxPlayers = Math.min(16, playerInfos.size());
        
        for (NetworkPlayerInfo playerInfo : playerInfos) {
            if (displayCount >= maxPlayers) break;
            
            String playerName = getPlayerName(playerInfo);
            String pingText = playerInfo.getResponseTime() + "ms";
            
            Color pingColor = getPingColor(playerInfo.getResponseTime());
            
            float playerY = currentY + displayCount * (fontHeight + 6);
            
            float alpha = Math.min(1.0f, playerListAlpha * (1.0f + displayCount * 0.1f));
            int nameColor = (int)(255 * alpha) << 24 | 0xFFFFFF;
            int pingColorWithAlpha = (int)(255 * alpha) << 24 | (pingColor.getRGB() & 0xFFFFFF);
            
            drawPlayerNameWithColors(playerName, x + padding, playerY, nameColor, alpha, otherFont);
            
            float pingX = x + width - padding - CustomFontRenderer.getStringWidth(pingText, otherFont);
            CustomFontRenderer.drawStringWithShadow(pingText, pingX, playerY, pingColorWithAlpha, otherFont);
            
            displayCount++;
        }
        
        float footerY = y + height - fontHeight * 3 - padding;
        nightsky.util.render.RenderUtil.drawRect(x + padding, footerY - 4, width - padding * 2, 1, 0x33FFFFFF);
        
        drawPlayerListFooter(x, footerY, width, otherFont);
        
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
    
    private void drawPlayerNameWithColors(String name, float x, float y, int defaultColor, float alpha, Font font) {
        if (name.contains("§")) {
            float currentX = x;
            String[] parts = name.split("§");
            
            CustomFontRenderer.drawStringWithShadow(parts[0], currentX, y, defaultColor, font);
            currentX += CustomFontRenderer.getStringWidth(parts[0], font);
            
            for (int i = 1; i < parts.length; i++) {
                if (!parts[i].isEmpty()) {
                    char colorCode = parts[i].charAt(0);
                    String text = parts[i].substring(1);
                    
                    int color = getMinecraftColor(colorCode, alpha);
                    CustomFontRenderer.drawStringWithShadow(text, currentX, y, color, font);
                    currentX += CustomFontRenderer.getStringWidth(text, font);
                }
            }
        } else {
            CustomFontRenderer.drawStringWithShadow(name, x, y, defaultColor, font);
        }
    }
    
    private int getMinecraftColor(char colorCode, float alpha) {
        int baseColor;
        switch (colorCode) {
            case '0': baseColor = 0x000000; break;
            case '1': baseColor = 0x0000AA; break;
            case '2': baseColor = 0x00AA00; break;
            case '3': baseColor = 0x00AAAA; break;
            case '4': baseColor = 0xAA0000; break;
            case '5': baseColor = 0xAA00AA; break;
            case '6': baseColor = 0xFFAA00; break;
            case '7': baseColor = 0xAAAAAA; break;
            case '8': baseColor = 0x555555; break;
            case '9': baseColor = 0x5555FF; break;
            case 'a': baseColor = 0x55FF55; break;
            case 'b': baseColor = 0x55FFFF; break;
            case 'c': baseColor = 0xFF5555; break;
            case 'd': baseColor = 0xFF55FF; break;
            case 'e': baseColor = 0xFFFF55; break;
            default: baseColor = 0xFFFFFF; break;
        }
        return (int)(255 * alpha) << 24 | baseColor;
    }
    
    private void drawPlayerListFooter(float x, float y, float width, Font font) {
        int fontHeight = CustomFontRenderer.getFontHeight(font);
        float padding = 12;
        float currentY = y;
        
        String serverInfo = getServerIP();
        String clientName = nightsky.NightSky.clientName;
        String clientVersion = nightsky.NightSky.clientVersion;
        String pingInfo = "Ping: " + (mc.getCurrentServerData() != null ? mc.getCurrentServerData().pingToServer + "ms" : "0ms");
        
        NickHider nickHider = (NickHider) nightsky.NightSky.moduleManager.modules.get(NickHider.class);
        if (nickHider != null && nickHider.isEnabled()) {
            serverInfo = nickHider.replaceNick(serverInfo);
        }
        
        CustomFontRenderer.drawStringWithShadow(serverInfo, x + padding, currentY, 0xAAAAAA, font);
        currentY += fontHeight + 4;
        
        CustomFontRenderer.drawStringWithShadow(clientName, x + padding, currentY, 0x55AAFF, font);
        float versionX = x + width - padding - CustomFontRenderer.getStringWidth(clientVersion, font);
        CustomFontRenderer.drawStringWithShadow(clientVersion, versionX, currentY, 0x55AAFF, font);
        currentY += fontHeight + 4;
        
        CustomFontRenderer.drawStringWithShadow(pingInfo, x + padding, currentY, getPingColor().getRGB(), font);
    }
    
    private String getPlayerName(NetworkPlayerInfo playerInfo) {
        String name = playerInfo.getDisplayName() != null ? 
            playerInfo.getDisplayName().getFormattedText() : 
            ScorePlayerTeam.formatPlayerName(playerInfo.getPlayerTeam(), playerInfo.getGameProfile().getName());
        
        NickHider nickHider = (NickHider) nightsky.NightSky.moduleManager.modules.get(NickHider.class);
        if (nickHider != null && nickHider.isEnabled()) {
            name = nickHider.replaceNick(name);
        }
        
        return name;
    }
    
    private Color getPingColor(int ping) {
        if (ping < 50) {
            return new Color(85, 255, 85);
        } else if (ping < 100) {
            return new Color(255, 255, 85);
        } else if (ping < 200) {
            return new Color(255, 170, 85);
        } else {
            return new Color(255, 85, 85);
        }
    }
    
    
    private String getServerIP() {
        if (mc.getCurrentServerData() != null) {
            if (IpProtected.containsPattern("us-test")) {
                return "hypixel.net";
            } else {
                return mc.getCurrentServerData().serverIP;
            }
        }
        return "Singleplayer";
    }
//
//    private void drawChestInterface(float x, float y, float width, float height) {
//        FontTransformer transformer = FontTransformer.getInstance();
//        Font otherFont = transformer.getFont("OpenSansSemiBold", 35);
//
//        ChestData chestData = ChestData.getInstance();
//        if (!chestData.isChestOpen()) return;
//
//        float padding = 8;
//        float slotSize = 18;
//        int chestSize = chestData.getChestSize();
//
//        if (chestData.isChestEmpty()) {
//            String emptyText = "Empty Chest";
//            float textX = x + (width - CustomFontRenderer.getStringWidth(emptyText, otherFont)) / 2.0f;
//            float textY = y + (height - CustomFontRenderer.getFontHeight(otherFont)) / 2.0f;
//            CustomFontRenderer.drawStringWithShadow(emptyText, textX, textY, 0xAAAAAA, otherFont);
//            return;
//        }
//
//        float startX = x + padding;
//        float startY = y + padding;
//
//        GlStateManager.enableRescaleNormal();
//        RenderHelper.enableGUIStandardItemLighting();
//
//        for (int slot = 0; slot < chestSize; slot++) {
//            int row = slot / 9;
//            int col = slot % 9;
//
//            float slotX = startX + col * 20;
//            float slotY = startY + row * 20;
//
//            ItemStack itemStack = chestData.getItemInSlot(slot);
//            if (itemStack != null) {
//                mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, (int)slotX + 1, (int)slotY + 1);
//                mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, itemStack, (int)slotX + 1, (int)slotY + 1, null);
//            }
//
//            ChestData.ClickAnimation animation = chestData.getClickAnimations().get(slot);
//            if (animation != null) {
//                float alpha = animation.getAlpha();
//                if (alpha > 0) {
//                    float centerX = slotX + slotSize / 2;
//                    float centerY = slotY + slotSize / 2;
//                    float radius = slotSize / 2;
//
//                    GlStateManager.pushMatrix();
//                    GlStateManager.enableBlend();
//                    GlStateManager.disableTexture2D();
//
//                    nightsky.util.render.RenderUtil.drawRoundedRect(centerX - radius, centerY - radius, radius * 2, radius * 2, radius, new Color(255, 255, 255, (int)(alpha * 255)));
//
//                    GlStateManager.enableTexture2D();
//                    GlStateManager.disableBlend();
//                    GlStateManager.popMatrix();
//                }
//            }
//        }
//
//        RenderHelper.disableStandardItemLighting();
//        GlStateManager.disableRescaleNormal();
//
//        String stealingText = "Stealing Chest...";
//        float textX = x + (width - CustomFontRenderer.getStringWidth(stealingText, otherFont)) / 2.0f;
//        float textY = y + height - CustomFontRenderer.getFontHeight(otherFont) - 4;
//        CustomFontRenderer.drawStringWithShadow(stealingText, textX, textY, 0x888888, otherFont);
//    }

    private void drawMainInterface(float x, float y, float width) {
        String serverIP = getServerIP();
        int ping = mc.getCurrentServerData() != null ? (int)mc.getCurrentServerData().pingToServer : 0;
        int fps = net.minecraft.client.Minecraft.getDebugFPS();

        float scale = 0.7f;
        float padding = 8;

        nightsky.module.modules.render.Interface interfaceModule = 
            (nightsky.module.modules.render.Interface) nightsky.NightSky.moduleManager.getModule("Interface");
        
        FontTransformer transformer = FontTransformer.getInstance();
        Font mainFont = transformer.getFont("MicrosoftYaHei", 60);
        Font islandTextFont = transformer.getFont("MicrosoftYaHei", 46);
        Font contentFont = transformer.getFont("MicrosoftYaHei", 40);

        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("M/d");
        java.text.SimpleDateFormat weekFormat = new java.text.SimpleDateFormat("EEEE", java.util.Locale.ENGLISH);
        java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm");
        java.util.Date now = new java.util.Date();
        
        String dateStr = dateFormat.format(now);
        String weekStr = weekFormat.format(now);
        String timeStr = timeFormat.format(now);

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);

        float scaledX = (x + padding) / scale;
        float scaledY = (y + padding) / scale;
        
        int mainFontHeight = CustomFontRenderer.getFontHeight(mainFont);
        float lineSpacing = 4;

        FontTransformer cheriTransformer = FontTransformer.getInstance();
        Font WaterMarkFont = cheriTransformer.getFont("SuperJoyful", 60);
        drawNightSkyTitle(scaledX, scaledY, WaterMarkFont);
        float textX = scaledX + CustomFontRenderer.getStringWidth("NightSky", WaterMarkFont) + 10;
        
        String displayText = islandText[currentTextIndex];
        int textColor = (int)(255 * textAlpha) << 24 | 0xFFFFFF;
        CustomFontRenderer.drawStringWithShadow(displayText, textX, scaledY, textColor, islandTextFont);
        
        scaledY += mainFontHeight + lineSpacing;
        
        float availableWidth = (float)((width / scale) - padding * 3.5);

        String[] line2Parts = {dateStr, weekStr, timeStr, ping + "ms to " + serverIP, fps + " FPS"};
        float[] partWidths = new float[line2Parts.length];
        float totalBaseWidth = 0;
        for (int i = 0; i < line2Parts.length; i++) {
            partWidths[i] = CustomFontRenderer.getStringWidth(line2Parts[i], contentFont);
            totalBaseWidth += partWidths[i];
        }
        
        float minSpacing = textSpacing;
        float totalSpacing = availableWidth - totalBaseWidth;
        float dynamicSpacing = Math.max(minSpacing, totalSpacing / (line2Parts.length - 1));
        
        float currentX = scaledX;
        for (int i = 0; i < line2Parts.length; i++) {
            int color = 0xFFFFFF;
            if (i == 3) {
                color = getPingColor().getRGB();
            }
            CustomFontRenderer.drawStringWithShadow(line2Parts[i], currentX, scaledY, color, contentFont);
            if (i < line2Parts.length - 1) {
                currentX += partWidths[i] + dynamicSpacing;
            }
        }

        GlStateManager.popMatrix();
    }

    private void drawNightSkyTitle(float x, float y, Font cheriFont) {
        CustomFontRenderer.drawStringWithShadow("NightSky", x, y, 0xFFFFFF, cheriFont);
    }
    
    private void drawDropShadowBackground(float x, float y, float width, float height, float cornerRadius) {
        float radius = shadowRadius;
        float spread = shadowSpread;
        float alphaMultiplier = shadowAlpha;
        
        if (radius <= 0 || alphaMultiplier <= 0) return;
        
        
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        
        float spreadDistance = radius * spread;
        float blurDistance = radius - spreadDistance;
        
        int samples = Math.max(8, Math.min(20, (int)(radius * 1.5f)));
        
        for (int i = 0; i < samples; i++) {
            float t = (float)i / (float)(samples - 1);
            
            float currentSpread = t * spreadDistance;
            float currentBlur = t * blurDistance;
            float totalOffset = currentSpread + currentBlur;
            
            float alpha;
            if (t <= spread) {
                alpha = 0.5f * alphaMultiplier;
            } else {
                float blurProgress = (t - spread) / (1.0f - spread);
                alpha = 0.5f * alphaMultiplier * (1.0f - blurProgress);
            }
            
            float shadowX = x - totalOffset;
            float shadowY = y - totalOffset;
            float shadowWidth = width + totalOffset * 2.0f;
            float shadowHeight = height + totalOffset * 2.0f;
            
            drawGaussianBlurredRect(shadowX, shadowY, shadowWidth, shadowHeight, cornerRadius, alpha);
        }
        
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
    
    private void drawGaussianBlurredRect(float x, float y, float width, float height, float cornerRadius, float alpha) {
        int blurLayers = 3;
        float blurSpread = 2.0f;
        
        for (int layer = 0; layer < blurLayers; layer++) {
            float layerProgress = (float)layer / (float)blurLayers;
            float layerAlpha = alpha * (1.0f - layerProgress * 0.3f);
            float layerExpand = layerProgress * blurSpread;
            
            float layerX = x - layerExpand;
            float layerY = y - layerExpand;
            float layerWidth = width + layerExpand * 2.0f;
            float layerHeight = height + layerExpand * 2.0f;
            
            nightsky.util.render.RenderUtil.drawRoundedRect(
                layerX, layerY, layerWidth, layerHeight,
                cornerRadius,
                new Color(0, 0, 0, (int)(layerAlpha * 255))
            );
        }
    }

    private double calculateCommandResultSize() {
        if (!showingCommandResult) return 200;
        
        FontTransformer transformer = FontTransformer.getInstance();
        Font otherFont = transformer.getFont("OpenSansSemiBold", 35);
        
        double maxWidth = 200;

        String typeText = commandResultType + " " + commandResultTitle;
        double titleWidth = CustomFontRenderer.getStringWidth(typeText, otherFont);
        maxWidth = Math.max(maxWidth, titleWidth);

        String[] lines = commandResultContent.split("\n");
        for (String line : lines) {
            double lineWidth = CustomFontRenderer.getStringWidth(line, otherFont);
            maxWidth = Math.max(maxWidth, lineWidth);
        }
        
        return Math.min(maxWidth + 16, 600);
    }
    
    private double calculateCommandResultHeight() {
        if (!showingCommandResult) return 60;
        
        FontTransformer transformer = FontTransformer.getInstance();
        Font otherFont = transformer.getFont("OpenSansSemiBold", 35);
        int fontHeight = CustomFontRenderer.getFontHeight(otherFont);
        
        double height = 16;

        height += fontHeight + 4;

        String[] lines = commandResultContent.split("\n");
        for (int i = 0; i < lines.length; i++) {
            height += fontHeight;
            if (i < lines.length - 1) {
                height += 2;
            }
        }
        
        return Math.min(height, 400);
    }
    
    private float calculateNotificationItemHeight(Notification.NotificationType type) {
        switch (type) {
            case MODULE_ENABLED:
            case MODULE_DISABLED:
            case INFO:
            case ERROR:
            case WARNING:
            case COMMAND_RESULT:
                return 30f;
            default:
                return 40f;
        }
    }
    
    private void drawCommandResult(float x, float y) {
        FontTransformer transformer = FontTransformer.getInstance();
        Font otherFont = transformer.getFont("OpenSansSemiBold", 35);
        int fontHeight = CustomFontRenderer.getFontHeight(otherFont);
        
        float padding = 8;
        float currentY = y + padding;

        Color typeColor;
        switch (commandResultType.toLowerCase()) {
            case "info":
                typeColor = new Color(100, 200, 255);
                break;
            case "warning":
                typeColor = new Color(255, 200, 100);
                break;
            case "error":
                typeColor = new Color(255, 100, 100);
                break;
            default:
                typeColor = new Color(255, 255, 255);
                break;
        }

        String typeText = commandResultType + " " + commandResultTitle;
        CustomFontRenderer.drawStringWithShadow(typeText, x + padding, currentY, typeColor.getRGB(), otherFont);
        currentY += fontHeight + 4;

        String[] lines = commandResultContent.split("\n");
        for (String line : lines) {
            CustomFontRenderer.drawStringWithShadow(line, x + padding, currentY, 0xFFFFFF, otherFont);
            currentY += fontHeight + 2;
        }
    }
    private Color getPingColor() {
        if (mc.getCurrentServerData() != null) {
            int ping = (int)mc.getCurrentServerData().pingToServer;
            if (ping < 130) return new Color(76, 175, 80);
            if (ping < 270) return new Color(255, 193, 7);
            if (ping < 500) return new Color(255, 152, 0);
            return new Color(244, 67, 54);
        }
        return new Color(160, 160, 160);
    }
}