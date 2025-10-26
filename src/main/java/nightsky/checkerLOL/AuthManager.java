package nightsky.checkerLOL;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;

public class AuthManager {
    private static final String REGISTER_API_URL = "http://lifey.icu/api/user/register";
    private static AuthManager instance;
    private String currentUser = "";
    private boolean isAuthenticated = false;
    private GuiLoginScreen loginClient;
    private boolean devMode = false;

    private AuthManager() {
        this.loginClient = new GuiLoginScreen("appid", "key");
    }

    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    private String lastLoginError = "";

    public boolean login(String user, String pwd) {
        try {
            lastLoginError = "";
            
            boolean success = loginClient.neverLogin(user, pwd);
            if (success) {
                currentUser = user;
                isAuthenticated = true;
                System.out.println("Login successful, user: " + user);
                return true;
            } else {
                lastLoginError = getLoginErrorFromAPI(user, pwd);
                return false;
            }
        } catch (Exception e) {
            lastLoginError = "Network connection error: " + e.getMessage();
            return false;
        }
    }

    public String getLastLoginError() {
        return lastLoginError.isEmpty() ? "Login failed" : lastLoginError;
    }

    private String getLoginErrorFromAPI(String user, String pwd) {
        try {
            String jsonBody = "{\"username\":\"" + user + "\",\"password\":\"" + pwd + "\",\"device_id\":\"test\"}";

            URL url = new URL("http://lifey.icu/api/auth/never-login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    
                    Gson gson = new Gson();
                    LoginErrorResponse errorResponse = gson.fromJson(response.toString(), LoginErrorResponse.class);

                    if (!errorResponse.success && errorResponse.message != null) {
                        String message = errorResponse.message;

                        if (message.contains("用户不存在") || message.contains("用户名不存在") || message.contains("User not found")) {
                            return "User not found, please register first";
                        } else if (message.contains("密码错误") || message.contains("密码不正确") || message.contains("Password incorrect")) {
                            return "Password incorrect";
                        } else if (message.contains("未激活") || message.contains("激活") || message.contains("卡密") || message.contains("not activated")) {
                            return "Please activate with key first";
                        } else {
                            return message;
                        }
                    }
                }
            }

            return "Login failed";
        } catch (Exception e) {
            return "Network connection error";
        }
    }

    private static class LoginErrorResponse {
        boolean success;
        String message;
    }

    public boolean register(String username, String password, String email) {
        try {
            String jsonBody = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\",\"email\":\"" + email + "\"}";

            URL url = new URL(REGISTER_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    
                    Gson gson = new Gson();
                    RegisterResponse registerResponse = gson.fromJson(response.toString(), RegisterResponse.class);
                    return registerResponse.success;
                }
            }
            
            return false;
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static class RegisterResponse {
        boolean success;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void logout() {
        currentUser = "";
        isAuthenticated = false;
    }
    
    /**
     * 设置开发者模式
     * @param enabled true 启用开发者模式，false 禁用开发者模式
     */
    public void setDevMode(boolean enabled) {
        this.devMode = enabled;
        System.out.println("Developer mode " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * 获取开发者模式状态
     * @return true 如果开发者模式已启用，false 否则
     */
    public boolean isDevMode() {
        return devMode;
    }
}
