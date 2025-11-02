package nightsky.webgui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.minecraft.client.Minecraft;
import nightsky.NightSky;
import nightsky.module.Module;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ModuleToggleHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            WebServer.sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)
        );
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }

        try {
            JsonObject json = new JsonParser().parse(body.toString()).getAsJsonObject();
            String moduleName = json.get("module").getAsString();
            boolean enabled = json.get("enabled").getAsBoolean();

            Module module = NightSky.moduleManager.getModule(moduleName);
            if (module == null) {
                WebServer.sendResponse(exchange, 404, "{\"error\":\"Module not found\"}");
                return;
            }

            final Module finalModule = module;
            final boolean finalEnabled = enabled;
            
            Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    finalModule.setEnabled(finalEnabled);
                }
            });

            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("module", moduleName);
            response.addProperty("enabled", enabled);
            WebServer.sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            WebServer.sendResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
        }
    }
}