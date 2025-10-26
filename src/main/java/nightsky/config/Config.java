package nightsky.config;

import com.google.gson.*;
import nightsky.NightSky;
import nightsky.mixin.IAccessorMinecraft;
import nightsky.module.Module;
import nightsky.util.ChatUtil;
import nightsky.value.Value;
import nightsky.checkerLOL.AuthManager;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.ArrayList;

public class Config {
    public static Minecraft mc = Minecraft.getMinecraft();
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public String name;
    public File file;

    public Config(String name, boolean newConfig) {
        this.name = name;
        this.file = new File("./NightSky/", String.format("%s.json", this.name));
        try {
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    ((IAccessorMinecraft) mc).getLogger().error("Failed to create config directory: " + parentDir.getAbsolutePath());
                }
            }
            if (newConfig) {
                ((IAccessorMinecraft) mc).getLogger().info(String.format("Config initialized: %s", this.file.getName()));
            }
        } catch (Exception e) {
            ((IAccessorMinecraft) mc).getLogger().error("Error initializing config: " + e.getMessage());
        }
    }

    public void load() {
        if (!file.exists()) {
            ChatUtil.sendFormatted(String.format("%sConfig file not found (&c&o%s&r)&r", NightSky.clientName, file.getName()));
            return;
        }
        
        try (FileReader reader = new FileReader(file)) {
            JsonElement parsed = new JsonParser().parse(reader);
            if (!parsed.isJsonObject()) {
                throw new JsonSyntaxException("Config file is not a valid JSON object");
            }
            
            JsonObject jsonObject = parsed.getAsJsonObject();
            
            // 加载开发者模式设置
            JsonElement devModeObj = jsonObject.get("DeveloperMode");
            if (devModeObj != null && devModeObj.isJsonPrimitive()) {
                AuthManager.getInstance().setDevMode(devModeObj.getAsBoolean());
            }
            
            JsonElement guiObj = jsonObject.get("ClickGUI");
            if (guiObj != null && guiObj.isJsonObject()) {
                JsonObject guiObject = guiObj.getAsJsonObject();
                JsonElement posX = guiObject.get("posX");
                JsonElement posY = guiObject.get("posY");
                JsonElement width = guiObject.get("width");
                JsonElement height = guiObject.get("height");
                
                if (posX != null && posX.isJsonPrimitive()) {
                    nightsky.ui.clickgui.augustus.AugustusClickGui.lastPosX = posX.getAsFloat();
                }
                if (posY != null && posY.isJsonPrimitive()) {
                    nightsky.ui.clickgui.augustus.AugustusClickGui.lastPosY = posY.getAsFloat();
                }
                if (width != null && width.isJsonPrimitive()) {
                    nightsky.ui.clickgui.augustus.AugustusClickGui.lastWidth = width.getAsFloat();
                }
                if (height != null && height.isJsonPrimitive()) {
                    nightsky.ui.clickgui.augustus.AugustusClickGui.lastHeight = height.getAsFloat();
                }
            }
            
            for (Module module : NightSky.moduleManager.modules.values()) {
                JsonElement moduleObj = jsonObject.get(module.getName());
                if (moduleObj != null && moduleObj.isJsonObject()) {
                    JsonObject object = moduleObj.getAsJsonObject();
                    
                    JsonElement toggled = object.get("toggled");
                    if (toggled != null && toggled.isJsonPrimitive()) {
                        module.setEnabled(toggled.getAsBoolean());
                    }
                    
                    JsonElement key = object.get("key");
                    if (key != null && key.isJsonPrimitive()) {
                        module.setKey(key.getAsInt());
                    }
                    
                    JsonElement hidden = object.get("hidden");
                    if (hidden != null && hidden.isJsonPrimitive()) {
                        module.setHidden(hidden.getAsBoolean());
                    }
                    
                    ArrayList<Value<?>> list = NightSky.valueHandler.properties.get(module.getClass());
                    if (list != null) {
                        for (Value<?> value : list) {
                            if (object.has(value.getName())) {
                                try {
                                    value.read(object);
                                } catch (Exception e) {
                                    ((IAccessorMinecraft) mc).getLogger().warn("Failed to load value: " + value.getName());
                                }
                            }
                        }
                    }
                }
            }
            ChatUtil.sendFormatted(String.format("%sConfig has been loaded (&a&o%s&r)&r", NightSky.clientName, file.getName()));
        } catch (FileNotFoundException e) {
            ChatUtil.sendFormatted(String.format("%sConfig file not found (&c&o%s&r)&r", NightSky.clientName, file.getName()));
        } catch (JsonSyntaxException e) {
            ((IAccessorMinecraft) mc).getLogger().error("Invalid JSON syntax in config: " + e.getMessage());
            ChatUtil.sendFormatted(String.format("%sInvalid config format (&c&o%s&r)&r", NightSky.clientName, file.getName()));
        } catch (IOException e) {
            ((IAccessorMinecraft) mc).getLogger().error("IO Error loading config: " + e.getMessage());
            ChatUtil.sendFormatted(String.format("%sConfig couldn't be loaded (&c&o%s&r)&r", NightSky.clientName, file.getName()));
        } catch (Exception e) {
            ((IAccessorMinecraft) mc).getLogger().error("Unexpected error loading config: " + e.getMessage());
            ChatUtil.sendFormatted(String.format("%sConfig couldn't be loaded (&c&o%s&r)&r", NightSky.clientName, file.getName()));
        }
    }

    public void save() {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            
            JsonObject object = new JsonObject();
            
            // 保存开发者模式设置
            object.addProperty("DeveloperMode", AuthManager.getInstance().isDevMode());
            
            JsonObject guiObject = new JsonObject();
            guiObject.addProperty("posX", nightsky.ui.clickgui.augustus.AugustusClickGui.lastPosX);
            guiObject.addProperty("posY", nightsky.ui.clickgui.augustus.AugustusClickGui.lastPosY);
            guiObject.addProperty("width", nightsky.ui.clickgui.augustus.AugustusClickGui.lastWidth);
            guiObject.addProperty("height", nightsky.ui.clickgui.augustus.AugustusClickGui.lastHeight);
            object.add("ClickGUI", guiObject);
            
            for (Module module : NightSky.moduleManager.modules.values()) {
                JsonObject moduleObject = new JsonObject();
                moduleObject.addProperty("toggled", module.isEnabled());
                moduleObject.addProperty("key", module.getKey());
                moduleObject.addProperty("hidden", module.isHidden());
                
                ArrayList<Value<?>> list = NightSky.valueHandler.properties.get(module.getClass());
                if (list != null) {
                    for (Value<?> value : list) {
                        try {
                            value.write(moduleObject);
                        } catch (Exception e) {
                            ((IAccessorMinecraft) mc).getLogger().warn("Failed to save value: " + value.getName());
                        }
                    }
                }
                object.add(module.getName(), moduleObject);
            }
            
            try (PrintWriter printWriter = new PrintWriter(new FileWriter(file))) {
                printWriter.println(gson.toJson(object));
            }
            
            ChatUtil.sendFormatted(String.format("%sConfig has been saved (&a&o%s&r)&r", NightSky.clientName, file.getName()));
        } catch (IOException e) {
            ((IAccessorMinecraft) mc).getLogger().error("IO Error saving config: " + e.getMessage());
            ChatUtil.sendFormatted(String.format("%sConfig couldn't be saved (&c&o%s&r)&r", NightSky.clientName, file.getName()));
        } catch (Exception e) {
            ((IAccessorMinecraft) mc).getLogger().error("Unexpected error saving config: " + e.getMessage());
            ChatUtil.sendFormatted(String.format("%sConfig couldn't be saved (&c&o%s&r)&r", NightSky.clientName, file.getName()));
        }
    }
}
