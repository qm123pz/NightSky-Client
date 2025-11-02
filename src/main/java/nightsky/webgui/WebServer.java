package nightsky.webgui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class WebServer {
    private HttpServer server;
    private final int port;
    private boolean running = false;

    public WebServer() {
        this.port = 1337;
    }

    public void start() {
        if (running) return;
        
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new WebUIHandler());
            server.createContext("/api/modules", new ModulesAPIHandler());
            server.createContext("/api/module/toggle", new ModuleToggleHandler());
            server.createContext("/api/module/values", new ModuleValuesHandler());
            server.createContext("/api/value/update", new ValueUpdateHandler());
            server.createContext("/api/configs", new ConfigAPIHandler());
            server.setExecutor(Executors.newFixedThreadPool(4));
            server.start();
            running = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (server != null && running) {
            server.stop(0);
            running = false;
        }
    }

    public boolean isRunning() {
        return running;
    }

    public int getPort() {
        return port;
    }

    protected static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = encodeWithDynamicBuffer(response);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    protected static void sendHTMLResponse(HttpExchange exchange, int statusCode, String html) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        byte[] bytes = encodeWithDynamicBuffer(html);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    protected static void sendCSSResponse(HttpExchange exchange, int statusCode, String css) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/css; charset=UTF-8");
        byte[] bytes = encodeWithDynamicBuffer(css);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    protected static void sendJSResponse(HttpExchange exchange, int statusCode, String js) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/javascript; charset=UTF-8");
        byte[] bytes = encodeWithDynamicBuffer(js);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static byte[] encodeWithDynamicBuffer(String content) {
        int estimatedSize = Math.min(content.length() * 2, 512);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(estimatedSize);
        try {
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            buffer.write(bytes);
            return buffer.toByteArray();
        } catch (IOException e) {
            return content.getBytes(StandardCharsets.UTF_8);
        }
    }
}