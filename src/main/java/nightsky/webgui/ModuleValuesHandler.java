package nightsky.webgui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import nightsky.NightSky;
import nightsky.module.Module;
import nightsky.value.Value;
import nightsky.value.values.*;

import java.io.IOException;
import java.util.List;

public class ModuleValuesHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            WebServer.sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.startsWith("module=")) {
            WebServer.sendResponse(exchange, 400, "{\"error\":\"Missing module parameter\"}");
            return;
        }

        String moduleName = query.substring(7);
        Module module = NightSky.moduleManager.getModule(moduleName);
        if (module == null) {
            WebServer.sendResponse(exchange, 404, "{\"error\":\"Module not found\"}");
            return;
        }

        List<Value<?>> values = NightSky.valueHandler.properties.get(module.getClass());
        JsonObject response = new JsonObject();
        JsonArray valuesArray = new JsonArray();

        if (values != null) {
            for (Value<?> value : values) {
                JsonObject valueObj = new JsonObject();
                valueObj.addProperty("name", value.getName());
                valueObj.addProperty("visible", value.isVisible());
                
                if (value instanceof FloatValue) {
                    FloatValue fv = (FloatValue) value;
                    valueObj.addProperty("type", "FloatValue");
                    valueObj.addProperty("value", fv.getValue());
                    valueObj.addProperty("min", fv.getMinimum());
                    valueObj.addProperty("max", fv.getMaximum());
                } else if (value instanceof IntValue) {
                    IntValue iv = (IntValue) value;
                    valueObj.addProperty("type", "IntValue");
                    valueObj.addProperty("value", iv.getValue());
                    valueObj.addProperty("min", iv.getMinimum());
                    valueObj.addProperty("max", iv.getMaximum());
                } else if (value instanceof PercentValue) {
                    PercentValue pv = (PercentValue) value;
                    valueObj.addProperty("type", "PercentValue");
                    valueObj.addProperty("value", pv.getValue());
                    valueObj.addProperty("min", pv.getMinimum());
                    valueObj.addProperty("max", pv.getMaximum());
                } else if (value instanceof BooleanValue) {
                    BooleanValue bv = (BooleanValue) value;
                    valueObj.addProperty("type", "BooleanValue");
                    valueObj.addProperty("value", bv.getValue());
                } else if (value instanceof ModeValue) {
                    ModeValue mv = (ModeValue) value;
                    valueObj.addProperty("type", "ModeValue");
                    valueObj.addProperty("value", mv.getModeString());
                    JsonArray modesArray = new JsonArray();
                    for (String mode : mv.getModes()) {
                        com.google.gson.JsonPrimitive modePrimitive = new com.google.gson.JsonPrimitive(mode);
                        modesArray.add(modePrimitive);
                    }
                    valueObj.add("modes", modesArray);
                } else if (value instanceof TextValue) {
                    TextValue tv = (TextValue) value;
                    valueObj.addProperty("type", "TextValue");
                    valueObj.addProperty("value", tv.getValue());
                } else if (value instanceof ColorValue) {
                    ColorValue cv = (ColorValue) value;
                    valueObj.addProperty("type", "ColorValue");
                    valueObj.addProperty("value", String.format("#%06X", cv.getValue() & 0xFFFFFF));
                }
                
                valuesArray.add(valueObj);
            }
        }

        response.add("values", valuesArray);
        WebServer.sendResponse(exchange, 200, response.toString());
    }
}