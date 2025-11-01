package nightsky;

import nightsky.powershell.Handler;
import nightsky.powershell.shells.*;
import nightsky.event.EventManager;
import nightsky.management.*;
import nightsky.module.modules.combat.*;
import nightsky.module.modules.misc.*;
import nightsky.module.modules.movement.*;
import nightsky.module.modules.player.*;
import nightsky.module.modules.render.*;
import nightsky.module.modules.render.DynamicIsland;
import nightsky.module.ModuleManager;
import nightsky.value.Value;
import nightsky.value.ValueHandler;

import java.lang.reflect.Field;
public class NightSky {
    public static String clientName = "NightSky";
    public static String clientVersion = "Release B1" ;
    public static RotationManager rotationManager;
    public static FloatManager floatManager;
    public static BlinkManager blinkManager;
    public static DelayManager delayManager;
    public static LagManager lagManager;
    public static PlayerStateManager playerStateManager;
    public static FriendManager friendManager;
    public static TargetManager targetManager;
    public static ValueHandler valueHandler;
    public static ModuleManager moduleManager;
    public static Handler handler;
    public static GuiManager guiManager;

    public NightSky() {
        this.init();
    }

    public void init() {
        rotationManager = new RotationManager();
        floatManager = new FloatManager();
        blinkManager = new BlinkManager();
        delayManager = new DelayManager();
        lagManager = new LagManager();
        playerStateManager = new PlayerStateManager();
        friendManager = new FriendManager();
        targetManager = new TargetManager();
        valueHandler = new ValueHandler();
        moduleManager = new ModuleManager();
        handler = new Handler();
        guiManager = new GuiManager();
        EventManager.register(rotationManager);
        EventManager.register(floatManager);
        EventManager.register(blinkManager);
        EventManager.register(delayManager);
        EventManager.register(lagManager);
        EventManager.register(moduleManager);
        EventManager.register(handler);
        moduleManager.modules.put(AimAssist.class, new AimAssist());
        moduleManager.modules.put(Animations.class, new Animations());
        moduleManager.modules.put(AntiDebuff.class, new AntiDebuff());
        moduleManager.modules.put(AntiFireball.class, new AntiFireball());
        moduleManager.modules.put(AntiObbyTrap.class, new AntiObbyTrap());
        moduleManager.modules.put(AntiObfuscate.class, new AntiObfuscate());
        moduleManager.modules.put(AntiVoid.class, new AntiVoid());
        moduleManager.modules.put(ArrayList.class, new ArrayList());
        moduleManager.modules.put(AutoClicker.class, new AutoClicker());
        moduleManager.modules.put(AutoHeal.class, new AutoHeal());
        moduleManager.modules.put(AutoProjectile.class, new AutoProjectile());
        moduleManager.modules.put(AutoTool.class, new AutoTool());
        moduleManager.modules.put(BedNuker.class, new BedNuker());
        moduleManager.modules.put(BedESP.class, new BedESP());
        moduleManager.modules.put(BedTracker.class, new BedTracker());
        moduleManager.modules.put(Blink.class, new Blink());
        moduleManager.modules.put(Camera.class, new Camera());
        moduleManager.modules.put(Chams.class, new Chams());
        moduleManager.modules.put(ChestESP.class, new ChestESP());
        moduleManager.modules.put(ChestStealer.class, new ChestStealer());
        moduleManager.modules.put(ChestView.class, new ChestView());
        moduleManager.modules.put(ClickGUI.class, new ClickGUI());
        moduleManager.modules.put(DynamicIsland.class, new DynamicIsland());
        moduleManager.modules.put(Eagle.class, new Eagle());
        moduleManager.modules.put(ESP.class, new ESP());
        moduleManager.modules.put(FastPlace.class, new FastPlace());
        moduleManager.modules.put(Fly.class, new Fly());
        moduleManager.modules.put(FullBright.class, new FullBright());
        moduleManager.modules.put(GlobalFont.class, new GlobalFont());
        moduleManager.modules.put(GhostHand.class, new GhostHand());
        moduleManager.modules.put(Indicators.class, new Indicators());
        moduleManager.modules.put(Interface.class, new Interface());
        moduleManager.modules.put(JumpCircles.class, new JumpCircles());
        moduleManager.modules.put(InvManager.class, new InvManager());
        moduleManager.modules.put(InvWalk.class, new InvWalk());
        moduleManager.modules.put(ItemESP.class, new ItemESP());
        moduleManager.modules.put(Jesus.class, new Jesus());
        moduleManager.modules.put(KeepSprint.class, new KeepSprint());
        moduleManager.modules.put(KillAura.class, new KillAura());
        moduleManager.modules.put(LagRange.class, new LagRange());
        moduleManager.modules.put(LightningTracker.class, new LightningTracker());
        moduleManager.modules.put(LongJump.class, new LongJump());
        moduleManager.modules.put(MCF.class, new MCF());
        moduleManager.modules.put(NameTags.class, new NameTags());
        moduleManager.modules.put(NickHider.class, new NickHider());
        moduleManager.modules.put(NoFall.class, new NoFall());
        moduleManager.modules.put(NoHitDelay.class, new NoHitDelay());
        moduleManager.modules.put(NoHurtCam.class, new NoHurtCam());
        moduleManager.modules.put(NoJumpDelay.class, new NoJumpDelay());
        moduleManager.modules.put(NoRotate.class, new NoRotate());
        moduleManager.modules.put(NoSlow.class, new NoSlow());
        moduleManager.modules.put(NotificationDisplay.class, new NotificationDisplay());
        moduleManager.modules.put(PartySpammer.class, new PartySpammer());
        moduleManager.modules.put(Reach.class, new Reach());
        moduleManager.modules.put(SafeWalk.class, new SafeWalk());
        moduleManager.modules.put(Scaffold.class, new Scaffold());
        moduleManager.modules.put(Scoreboard.class, new Scoreboard());
        moduleManager.modules.put(Spammer.class, new Spammer());
        moduleManager.modules.put(Speed.class, new Speed());
        moduleManager.modules.put(SpeedMine.class, new SpeedMine());
        moduleManager.modules.put(Sprint.class, new Sprint());
        moduleManager.modules.put(TargetESP.class, new TargetESP());
        moduleManager.modules.put(TargetHUD.class, new TargetHUD());
        moduleManager.modules.put(TargetStrafe.class, new TargetStrafe());
        moduleManager.modules.put(Tracers.class, new Tracers());
        moduleManager.modules.put(Trajectories.class, new Trajectories());
        moduleManager.modules.put(Velocity.class, new Velocity());
        moduleManager.modules.put(ViewClip.class, new ViewClip());
        moduleManager.modules.put(WaterMark.class, new WaterMark());
        moduleManager.modules.put(Wtap.class, new Wtap());
        moduleManager.modules.put(Xray.class, new Xray());
        handler.powerShells.add(new Bind());
        handler.powerShells.add(new Config());
        handler.powerShells.add(new Denick());
        handler.powerShells.add(new Friend());
        handler.powerShells.add(new Help());
        handler.powerShells.add(new Name());
        handler.powerShells.add(new Item());
        handler.powerShells.add(new Player());
        handler.powerShells.add(new Show());
        handler.powerShells.add(new Target());
        handler.powerShells.add(new Toggle());
        handler.powerShells.add(new VerticalClip());
        handler.powerShells.add(new DevWeb());
        handler.powerShells.add(new Binds());

        for (nightsky.module.Module module : moduleManager.modules.values()) {
            java.util.ArrayList<Value<?>> properties = new java.util.ArrayList<>();
            for (final Field field : module.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                final Object obj;
                try {
                    obj = field.get(module);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                if (obj instanceof Value<?>) {
                    ((Value<?>) obj).setOwner(module);
                    properties.add((Value<?>) obj);
                }
            }
            valueHandler.properties.put(module.getClass(), properties);
            EventManager.register(module);
        }
        nightsky.config.Config config = new nightsky.config.Config("default", true);
        if (config.file.exists()) {
            config.load();
        }
        if (friendManager.file.exists()) {
            friendManager.load();
        }
        if (targetManager.file.exists()) {
            targetManager.load();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(config::save));
    }
}
