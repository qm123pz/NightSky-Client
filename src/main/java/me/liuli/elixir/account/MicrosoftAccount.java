package me.liuli.elixir.account;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.liuli.elixir.compat.OAuthServer;
import me.liuli.elixir.compat.Session;
import me.liuli.elixir.exception.LoginException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MicrosoftAccount extends MinecraftAccount {
    private String name = "UNKNOWN";
    private String uuid = "";
    private String accessToken = "";
    private String refreshToken = "";
    private AuthMethod authMethod = AuthMethod.MICROSOFT;

    public MicrosoftAccount() {
        super("Microsoft");
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Session getSession() {
        if (uuid.isEmpty() || accessToken.isEmpty()) {
            update();
        }
        return new Session(name, uuid, accessToken, "mojang");
    }

    @Override
    public void update() {
        try {
            Map<String, String> jsonPostHeader = new HashMap<>();
            jsonPostHeader.put("Content-Type", "application/json");
            jsonPostHeader.put("Accept", "application/json");

            // get the microsoft access token
            String msRefreshData = replaceKeys(authMethod, XBOX_REFRESH_DATA) + refreshToken;
            Map<String, String> formHeader = new HashMap<>();
            formHeader.put("Content-Type", "application/x-www-form-urlencoded");
            String msRefreshResponse = makeHttpRequest(XBOX_AUTH_URL, "POST", msRefreshData, formHeader);
            
            JsonObject msRefreshJson = new JsonParser().parse(msRefreshResponse).getAsJsonObject();
            String msAccessToken = getJsonString(msRefreshJson, "access_token");
            if (msAccessToken == null) throw new LoginException("Microsoft access token is null");
            
            // refresh token is changed after refresh
            String newRefreshToken = getJsonString(msRefreshJson, "refresh_token");
            if (newRefreshToken != null) {
                refreshToken = newRefreshToken;
            }

            // authenticate with XBL
            String xblData = XBOX_XBL_DATA.replace("<rps_ticket>", 
                authMethod.rpsTicketRule.replace("<access_token>", msAccessToken));
            String xblResponse = makeHttpRequest(XBOX_XBL_URL, "POST", xblData, jsonPostHeader);
            
            JsonObject xblJson = new JsonParser().parse(xblResponse).getAsJsonObject();
            String xblToken = getJsonString(xblJson, "Token");
            if (xblToken == null) throw new LoginException("Microsoft XBL token is null");
            
            JsonObject displayClaims = xblJson.getAsJsonObject("DisplayClaims");
            String userhash = displayClaims.getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString();
            if (userhash == null) throw new LoginException("Microsoft XBL userhash is null");

            // authenticate with XSTS
            String xstsData = XBOX_XSTS_DATA.replace("<xbl_token>", xblToken);
            String xstsResponse = makeHttpRequest(XBOX_XSTS_URL, "POST", xstsData, jsonPostHeader);
            
            JsonObject xstsJson = new JsonParser().parse(xstsResponse).getAsJsonObject();
            String xstsToken = getJsonString(xstsJson, "Token");
            if (xstsToken == null) throw new LoginException("Microsoft XSTS token is null");

            // get the minecraft access token
            String mcData = MC_AUTH_DATA.replace("<userhash>", userhash).replace("<xsts_token>", xstsToken);
            String mcResponse = makeHttpRequest(MC_AUTH_URL, "POST", mcData, jsonPostHeader);
            
            JsonObject mcJson = new JsonParser().parse(mcResponse).getAsJsonObject();
            accessToken = getJsonString(mcJson, "access_token");
            if (accessToken == null) throw new LoginException("Minecraft access token is null");

            // get the minecraft profile
            Map<String, String> authHeader = new HashMap<>();
            authHeader.put("Authorization", "Bearer " + accessToken);
            String profileResponse = makeHttpRequest(MC_PROFILE_URL, "GET", "", authHeader);
            
            JsonObject profileJson = new JsonParser().parse(profileResponse).getAsJsonObject();
            name = getJsonString(profileJson, "name");
            uuid = getJsonString(profileJson, "id");
            
            if (name == null || uuid == null) {
                throw new LoginException("Minecraft profile is null");
            }

        } catch (Exception e) {
            throw new LoginException("Failed to update Microsoft account: " + e.getMessage());
        }
    }

    public static MicrosoftAccount buildFromAuthCode(String code, AuthMethod authMethod) {
        try {
            String tokenData = replaceKeys(authMethod, XBOX_AUTH_DATA) + code;
            Map<String, String> tokenHeaders = new HashMap<>();
            tokenHeaders.put("Content-Type", "application/x-www-form-urlencoded");
            String tokenResponse = makeHttpRequest(XBOX_AUTH_URL, "POST", tokenData, tokenHeaders);

            JsonObject data = new JsonParser().parse(tokenResponse).getAsJsonObject();
            if (data.has("refresh_token")) {
                MicrosoftAccount account = new MicrosoftAccount();
                account.refreshToken = getJsonString(data, "refresh_token");
                account.authMethod = authMethod;
                account.update();
                return account;
            } else {
                throw new LoginException("Failed to get refresh token");
            }

        } catch (Exception e) {
            throw new LoginException("Failed to build Microsoft account from auth code: " + e.getMessage());
        }
    }

    public static OAuthServer buildFromOpenBrowser(OAuthHandler handler) throws IOException {
        return buildFromOpenBrowser(handler, AuthMethod.AZURE_APP);
    }
    
    public static OAuthServer buildFromOpenBrowser(OAuthHandler handler, AuthMethod authMethod) throws IOException {
        return new OAuthServer(handler, authMethod);
    }

    public static String replaceKeys(AuthMethod method, String string) {
        return string.replace("<client_id>", method.clientId)
                .replace("<client_secret>", method.clientSecret)
                .replace("<redirect_uri>", method.redirectUri)
                .replace("<scope>", method.scope);
    }

    private static String getJsonString(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private static String makeHttpRequest(String url, String method, String data, Map<String, String> headers) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        
        for (Map.Entry<String, String> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }
        
        if (!data.isEmpty()) {
            connection.setDoOutput(true);
            connection.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
        }
        
        Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8");
        StringBuilder response = new StringBuilder();
        while (scanner.hasNext()) {
            response.append(scanner.nextLine());
        }
        scanner.close();
        
        return response.toString();
    }

    public static class AuthMethod {
        public final String clientId;
        public final String clientSecret;
        public final String redirectUri;
        public final String scope;
        public final String rpsTicketRule;

        public AuthMethod(String clientId, String clientSecret, String redirectUri, String scope, String rpsTicketRule) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.redirectUri = redirectUri;
            this.scope = scope;
            this.rpsTicketRule = rpsTicketRule;
        }

        public static final AuthMethod MICROSOFT = new AuthMethod(
            "00000000441cc96b", "", 
            "https://login.live.com/oauth20_desktop.srf", 
            "service::user.auth.xboxlive.com::MBI_SSL", 
            "<access_token>"
        );
        
        public static final AuthMethod AZURE_APP = new AuthMethod(
            "c6cd7b0f-077d-4fcf-ab5c-9659576e38cb", 
            "vI87Q~GkhVHJSLN5WKBbEKbK0TJc9YRDyOYc5", 
            "http://localhost:1919/login", 
            "XboxLive.signin%20offline_access", 
            "d=<access_token>"
        );
    }

    public interface OAuthHandler {
        void openUrl(String url);
        void authResult(MicrosoftAccount account);
        void authError(String error);
    }

    // Constants from Elixir
    private static final String XBOX_PRE_AUTH_URL = "https://login.live.com/oauth20_authorize.srf?client_id=<client_id>&redirect_uri=<redirect_uri>&response_type=code&display=touch&scope=<scope>";
    private static final String XBOX_AUTH_URL = "https://login.live.com/oauth20_token.srf";
    private static final String XBOX_XBL_URL = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String XBOX_XSTS_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String MC_AUTH_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String MC_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";

    private static final String XBOX_AUTH_DATA = "client_id=<client_id>&client_secret=<client_secret>&redirect_uri=<redirect_uri>&grant_type=authorization_code&code=";
    private static final String XBOX_REFRESH_DATA = "client_id=<client_id>&client_secret=<client_secret>&scope=<scope>&grant_type=refresh_token&redirect_uri=<redirect_uri>&refresh_token=";
    private static final String XBOX_XBL_DATA = "{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"<rps_ticket>\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}";
    private static final String XBOX_XSTS_DATA = "{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\"<xbl_token>\"]},\"RelyingParty\":\"rp://api.minecraftservices.com/\",\"TokenType\":\"JWT\"}";
    private static final String MC_AUTH_DATA = "{\"identityToken\":\"XBL3.0 x=<userhash>;<xsts_token>\"}";
}
