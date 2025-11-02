package nightsky.webgui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import nightsky.NightSky;
import nightsky.module.Module;

import java.io.IOException;

public class ModulesAPIHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            WebServer.sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        JsonObject response = new JsonObject();
        JsonArray modulesArray = new JsonArray();

        for (Module module : NightSky.moduleManager.modules.values()) {
            JsonObject moduleObj = new JsonObject();
            moduleObj.addProperty("name", module.getName());
            moduleObj.addProperty("enabled", module.isEnabled());
            moduleObj.addProperty("category", module.getCategory().toString());
            moduleObj.addProperty("hidden", module.isHidden());
            moduleObj.addProperty("key", module.getKey());
            modulesArray.add(moduleObj);
        }

        response.add("modules", modulesArray);
        WebServer.sendResponse(exchange, 200, response.toString());
    }
}