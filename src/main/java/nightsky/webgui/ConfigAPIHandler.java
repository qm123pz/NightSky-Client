package nightsky.webgui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.minecraft.client.Minecraft;
import nightsky.config.Config;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigAPIHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        
        if (method.equals("GET")) {
            handleGetConfigs(exchange);
        } else if (method.equals("POST")) {
            handleConfigAction(exchange);
        } else {
            WebServer.sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
        }
    }
    
    private void handleGetConfigs(HttpExchange exchange) throws IOException {
        File configDir = new File("./NightSky/");
        JsonArray configs = new JsonArray();
        
        if (configDir.exists() && configDir.isDirectory()) {
            File[] files = configDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    JsonObject configObj = new JsonObject();
                    String name = file.getName().replace(".json", "");
                    configObj.addProperty("name", name);
                    configObj.addProperty("size", file.length());
                    configObj.addProperty("lastModified", file.lastModified());
                    configs.add(configObj);
                }
            }
        }
        
        JsonObject response = new JsonObject();
        response.add("configs", configs);
        WebServer.sendResponse(exchange, 200, response.toString());
    }
    
    private void handleConfigAction(HttpExchange exchange) throws IOException {
        try {
            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            JsonObject request = new com.google.gson.JsonParser().parse(reader).getAsJsonObject();
            reader.close();
            
            String action = request.get("action").getAsString();
            String configName = request.has("name") ? request.get("name").getAsString() : null;
            
            JsonObject response = new JsonObject();
            
            switch (action) {
                case "load":
                    if (configName != null) {
                        final String finalConfigName = configName;
                        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                            @Override
                            public void run() {
                                Config config = new Config(finalConfigName, false);
                                config.load();
                            }
                        });
                        response.addProperty("success", true);
                        response.addProperty("message", "Config loaded: " + configName);
                    }
                    break;
                    
                case "save":
                    if (configName != null) {
                        final String finalConfigName = configName;
                        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                            @Override
                            public void run() {
                                Config config = new Config(finalConfigName, true);
                                config.save();
                            }
                        });
                        response.addProperty("success", true);
                        response.addProperty("message", "Config saved: " + configName);
                    }
                    break;
                    
                case "delete":
                    if (configName != null) {
                        File file = new File("./NightSky/", configName + ".json");
                        boolean deleted = file.delete();
                        response.addProperty("success", deleted);
                        response.addProperty("message", deleted ? "Config deleted: " + configName : "Failed to delete config");
                    }
                    break;
                    
                case "openFolder":
                    File configDir = new File("./NightSky/");
                    if (!configDir.exists()) {
                        configDir.mkdirs();
                    }
                    try {
                        java.awt.Desktop.getDesktop().open(configDir);
                        response.addProperty("success", true);
                        response.addProperty("message", "Folder opened");
                    } catch (Exception e) {
                        response.addProperty("success", false);
                        response.addProperty("message", "Failed to open folder");
                    }
                    break;
                    
                default:
                    response.addProperty("success", false);
                    response.addProperty("message", "Unknown action");
            }
            
            WebServer.sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("message", e.getMessage());
            WebServer.sendResponse(exchange, 500, error.toString());
        }
    }
}
