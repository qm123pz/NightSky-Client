package me.liuli.elixir.compat;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import me.liuli.elixir.account.MicrosoftAccount;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class OAuthServer {
    private final MicrosoftAccount.OAuthHandler handler;
    private final MicrosoftAccount.AuthMethod authMethod;
    private final HttpServer httpServer;
    private final String context;
    private final ThreadPoolExecutor threadPoolExecutor;

    public OAuthServer(MicrosoftAccount.OAuthHandler handler, MicrosoftAccount.AuthMethod authMethod) throws IOException {
        this.handler = handler;
        this.authMethod = authMethod;
        this.context = "/login";
        this.httpServer = HttpServer.create(new InetSocketAddress("localhost", 1919), 0);
        this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    }

    public void start() {
        httpServer.createContext(context, new OAuthHttpHandler(this, authMethod));
        httpServer.setExecutor(threadPoolExecutor);
        httpServer.start();
        
        String authUrl = MicrosoftAccount.replaceKeys(authMethod, 
            "https://login.live.com/oauth20_authorize.srf?client_id=<client_id>&redirect_uri=<redirect_uri>&response_type=code&display=touch&scope=<scope>");
        handler.openUrl(authUrl);
    }

    public void stop(boolean isInterrupt) {
        httpServer.stop(0);
        threadPoolExecutor.shutdown();
        if (isInterrupt) {
            handler.authError("Has been interrupted");
        }
    }

    public static class OAuthHttpHandler implements HttpHandler {
        private final OAuthServer server;
        private final MicrosoftAccount.AuthMethod authMethod;

        public OAuthHttpHandler(OAuthServer server, MicrosoftAccount.AuthMethod authMethod) {
            this.server = server;
            this.authMethod = authMethod;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> queryMap = new HashMap<>();
            
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=", 2);
                    if (keyValue.length == 2) {
                        queryMap.put(keyValue[0], keyValue[1]);
                    }
                }
            }

            if (queryMap.containsKey("code")) {
                try {
                    MicrosoftAccount account = MicrosoftAccount.buildFromAuthCode(queryMap.get("code"), authMethod);
                    server.handler.authResult(account);
                    response(exchange, "Login Success", 200);
                } catch (Exception e) {
                    server.handler.authError(e.toString());
                    response(exchange, "Error: " + e.getMessage(), 500);
                }
            } else {
                server.handler.authError("No code in the query");
                response(exchange, "No code in the query", 500);
            }
            server.stop(false);
        }

        private void response(HttpExchange exchange, String message, int code) throws IOException {
            byte[] bytes = message.getBytes();
            exchange.sendResponseHeaders(code, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        }
    }
}
