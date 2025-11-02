package nightsky.webgui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.minecraft.client.Minecraft;
import nightsky.NightSky;
import nightsky.module.Module;
import nightsky.value.Value;
import nightsky.value.values.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ValueUpdateHandler implements HttpHandler {
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
            String valueName = json.get("value").getAsString();

            Module module = NightSky.moduleManager.getModule(moduleName);
            if (module == null) {
                WebServer.sendResponse(exchange, 404, "{\"error\":\"Module not found\"}");
                return;
            }

            List<Value<?>> values = NightSky.valueHandler.properties.get(module.getClass());
            if (values == null) {
                WebServer.sendResponse(exchange, 404, "{\"error\":\"No values found\"}");
                return;
            }

            Value<?> targetValue = null;
            for (Value<?> value : values) {
                if (value.getName().equals(valueName)) {
                    targetValue = value;
                    break;
                }
            }

            if (targetValue == null) {
                WebServer.sendResponse(exchange, 404, "{\"error\":\"Value not found\"}");
                return;
            }

            final Value<?> finalTargetValue = targetValue;
            final JsonObject finalJson = json;
            
            Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (finalTargetValue instanceof FloatValue) {
                            finalTargetValue.setValue(finalJson.get("newValue").getAsFloat());
                        } else if (finalTargetValue instanceof IntValue || finalTargetValue instanceof PercentValue) {
                            finalTargetValue.setValue(finalJson.get("newValue").getAsInt());
                        } else if (finalTargetValue instanceof BooleanValue) {
                            finalTargetValue.setValue(finalJson.get("newValue").getAsBoolean());
                        } else if (finalTargetValue instanceof ModeValue) {
                            finalTargetValue.parseString(finalJson.get("newValue").getAsString());
                        } else if (finalTargetValue instanceof TextValue) {
                            finalTargetValue.setValue(finalJson.get("newValue").getAsString());
                        } else if (finalTargetValue instanceof ColorValue) {
                            finalTargetValue.parseString(finalJson.get("newValue").getAsString());
                        } else {
                            if (finalJson.get("newValue").isJsonPrimitive()) {
                                if (finalJson.get("newValue").getAsJsonPrimitive().isNumber()) {
                                    if (finalJson.get("newValue").getAsJsonPrimitive().getAsString().contains(".")) {
                                        finalTargetValue.setValue(finalJson.get("newValue").getAsFloat());
                                    } else {
                                        finalTargetValue.setValue(finalJson.get("newValue").getAsInt());
                                    }
                                } else if (finalJson.get("newValue").getAsJsonPrimitive().isBoolean()) {
                                    finalTargetValue.setValue(finalJson.get("newValue").getAsBoolean());
                                } else {
                                    finalTargetValue.parseString(finalJson.get("newValue").getAsString());
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("module", moduleName);
            response.addProperty("value", valueName);
            WebServer.sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            WebServer.sendResponse(exchange, 400, "{\"error\":\"Invalid request: " + e.getMessage() + "\"}");
        }
    }
}