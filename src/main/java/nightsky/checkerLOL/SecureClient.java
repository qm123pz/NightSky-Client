package nightsky.checkerLOL;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import org.json.JSONException;
import org.json.JSONObject;

public class SecureClient {
    private static final String BASE_URL = "http://lifey.icu";
    private static final int SESSION_TIMEOUT = 100;

    private String sessionId;
    private String sessionKey;
    private long expireTime;
    private final String secret;
    private final String appId;

    private String token;
    private byte[] sessionHmac;
    private byte[] saltSessionId;
    private byte[] saltSessionKey;
    private byte[] saltToken;

    public SecureClient(String appId, String secret) {
        this.appId = appId;
        this.secret = secret;
    }

    private String getDeviceFingerprint() {
        List<String> deviceInfoParts = new ArrayList<>();

        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArch = System.getProperty("os.arch");
        deviceInfoParts.add(osName + "-" + osVersion + "-" + osArch);

        String cpuInfo = getCpuInfo();
        if (cpuInfo == null) {
            return "hwid失败 请联系管理员";
        }
        deviceInfoParts.add("cpu:" + cpuInfo);

        String macAddr = getMacAddress();
        if (macAddr == null) {
            return "hwid失败 请联系管理员";
        }
        deviceInfoParts.add("mac:" + macAddr);

        String diskInfo = getDiskInfo();
        if (diskInfo != null) {
            deviceInfoParts.add("disk:" + diskInfo);
        } else {
            deviceInfoParts.add("disk:unknown");
        }

        if (osName.toLowerCase().contains("win")) {
            String boardInfo = getMotherboardInfo();
            if (boardInfo != null) {
                deviceInfoParts.add("board:" + boardInfo);
            } else {
                deviceInfoParts.add("board:unknown");
            }
        }

        String deviceString = String.join("-", deviceInfoParts);
        return sha256(deviceString);
    }

    private String getCpuInfo() {
        String osName = System.getProperty("os.name").toLowerCase();

        try {
            if (osName.contains("win")) {
                try {
                    Process process = Runtime.getRuntime().exec("wmic cpu get ProcessorId,Name");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    List<String> lines = new ArrayList<>();

                    while ((line = reader.readLine()) != null) {
                        if (!line.trim().isEmpty()) {
                            lines.add(line.trim());
                        }
                    }
                    reader.close();

                    if (lines.size() > 1) {
                        return md5(lines.get(1));
                    }
                } catch (Exception e) {
                }

                try {
                    Process process = Runtime.getRuntime().exec(
                            "reg query \"HKLM\\HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0\" /v ProcessorNameString");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;

                    while ((line = reader.readLine()) != null) {
                        if (line.contains("ProcessorNameString")) {
                            String[] parts = line.split("\\s+", 3);
                            if (parts.length >= 3) {
                                return md5(parts[2]);
                            }
                        }
                    }
                    reader.close();
                } catch (Exception e) {
                }
            } else if (osName.contains("linux")) {
                try (BufferedReader reader = new BufferedReader(new FileReader("/proc/cpuinfo"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("model name") || line.startsWith("cpu family")) {
                            return md5(line.trim());
                        }
                    }
                } catch (Exception e) {
                }
            } else if (osName.contains("mac")) {
                try {
                    Process process = Runtime.getRuntime().exec("sysctl -n machdep.cpu.brand_string");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = reader.readLine();
                    reader.close();

                    if (line != null && !line.trim().isEmpty()) {
                        return md5(line.trim());
                    }
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
        }

        return null;
    }

    private String getMacAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();

                if (ni.isLoopback() || !ni.isUp()) {
                    continue;
                }

                byte[] mac = ni.getHardwareAddress();
                if (mac != null && mac.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    String macAddr = sb.toString();

                    if (!macAddr.startsWith("00-00-00") && !macAddr.startsWith("FF-FF-FF")) {
                        return macAddr;
                    }
                }
            }

            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                Process process = Runtime.getRuntime().exec("ipconfig /all");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.toLowerCase().contains("物理地址") || line.toLowerCase().contains("mac address")) {
                        String[] parts = line.split(":");
                        if (parts.length > 1) {
                            String mac = parts[1].trim().toUpperCase();
                            if (!mac.isEmpty() && !mac.startsWith("00-00-00")) {
                                return mac;
                            }
                        }
                    }
                }
                reader.close();
            } else if (osName.contains("linux") || osName.contains("mac")) {
                Process process = Runtime.getRuntime().exec("ifconfig");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.toLowerCase().contains("ether") || line.toLowerCase().contains("hwaddr")) {
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length > 1) {
                            String mac = parts[1].toUpperCase().replace(":", "-");
                            if (!mac.startsWith("00-00-00")) {
                                return mac;
                            }
                        }
                    }
                }
                reader.close();
            }
        } catch (Exception e) {
        }

        return null;
    }

    private String getDiskInfo() {
        String osName = System.getProperty("os.name").toLowerCase();

        try {
            if (osName.contains("win")) {
                Process process = Runtime.getRuntime().exec("wmic diskdrive get SerialNumber");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                List<String> lines = new ArrayList<>();

                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty() && !line.trim().equalsIgnoreCase("SerialNumber")) {
                        lines.add(line.trim());
                    }
                }
                reader.close();

                if (!lines.isEmpty()) {
                    return md5(lines.get(0));
                }
            } else if (osName.contains("linux")) {
                try (BufferedReader reader = new BufferedReader(new FileReader("/proc/diskstats"))) {
                    String line = reader.readLine();
                    if (line != null && !line.trim().isEmpty()) {
                        return md5(line.trim());
                    }
                }
            } else if (osName.contains("mac")) {
                Process process = Runtime.getRuntime().exec("diskutil info / | grep \"Volume UUID\"");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                reader.close();

                if (line != null && !line.trim().isEmpty()) {
                    return md5(line.trim());
                }
            }
        } catch (Exception e) {
        }

        return null;
    }

    private String getMotherboardInfo() {
        try {
            Process process = Runtime.getRuntime().exec("wmic baseboard get SerialNumber");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            List<String> lines = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.trim().equalsIgnoreCase("SerialNumber")) {
                    lines.add(line.trim());
                }
            }
            reader.close();

            if (!lines.isEmpty()) {
                return md5(lines.get(0));
            }
        } catch (Exception e) {
        }

        return null;
    }

    private JSONObject buildDirectActivateRequest(JSONObject payload) {
        payload.put("timestamp", System.currentTimeMillis());
        payload.put("appid", appId);
        payload.put("encryption_key", secret);
        return payload;
    }

    private JSONObject sendDirectActivateRequest(JSONObject payload) {
        try {
            JSONObject request = buildDirectActivateRequest(payload);
            URL url = new URL(BASE_URL + "/api/user/direct-activate");
            System.out.println("【明文请求】发送激活请求到: " + url.toString());
            System.out.println("【明文请求参数】" + request.toString(2));

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = request.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            System.out.println("【明文响应】状态码: " + responseCode);

            String responseStr;
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            responseCode == HttpURLConnection.HTTP_OK ? conn.getInputStream() : conn.getErrorStream(),
                            StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine);
                }
                responseStr = response.toString();
                System.out.println("【明文响应内容】" + responseStr);
            }

            return parseJsonSafely(responseStr);

        } catch (Exception e) {
            System.out.println("【请求异常】激活请求失败: " + e.getMessage());
            e.printStackTrace();

            JSONObject errorObj = new JSONObject();
            errorObj.put("success", false);
            errorObj.put("message", "激活请求处理失败: " + e.getMessage());
            return errorObj;
        }
    }

    private JSONObject parseJsonSafely(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            System.out.println("【JSON解析】JSON字符串为空");
            return null;
        }

        try {
            return new JSONObject(jsonStr);
        } catch (JSONException e) {
            System.out.println("【JSON解析】直接解析失败，尝试修复: " + e.getMessage());

            String cleaned = jsonStr.trim();
            int startIndex = cleaned.indexOf('{');
            int endIndex = cleaned.lastIndexOf('}');
            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                cleaned = cleaned.substring(startIndex, endIndex + 1);
            }
            cleaned = cleaned.replaceAll(",\\s*}", "}").replaceAll(",\\s*]", "]");

            try {
                return new JSONObject(cleaned);
            } catch (JSONException e2) {
                System.out.println("【JSON解析】修复后仍失败: " + e2.getMessage());
                System.out.println("【JSON解析】问题内容: " + cleaned);
                return null;
            }
        }
    }

    private boolean checkDebugger() {
        String osName = System.getProperty("os.name").toLowerCase();

        try {
            if (osName.contains("win")) {
                Process process = Runtime.getRuntime().exec("tasklist");
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.toLowerCase().contains("ollydbg") ||
                            line.toLowerCase().contains("ida") ||
                            line.toLowerCase().contains("ghidra") ||
                            line.toLowerCase().contains("x64dbg") ||
                            line.toLowerCase().contains("windbg")) {
                        return true;
                    }
                }
            } else if (osName.contains("linux")) {
                File statusFile = new File("/proc/self/status");
                if (statusFile.exists()) {
                    try (Scanner scanner = new Scanner(statusFile)) {
                        while (scanner.hasNextLine()) {
                            String line = scanner.nextLine();
                            if (line.startsWith("TracerPid:")) {
                                String pid = line.split("\\s+")[1];
                                if (!pid.equals("0")) {
                                    return true;
                                }
                            }
                        }
                    }
                }

                Process process = Runtime.getRuntime().exec("ps aux");
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.toLowerCase().contains("gdb") ||
                            line.toLowerCase().contains("lldb") ||
                            line.toLowerCase().contains("strace") ||
                            line.toLowerCase().contains("ida")) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public boolean directActivate(String username, String password, String kamiCode) {
        if (checkDebugger()) {
            System.out.println("安全警告：检测到调试环境");
            return false;
        }

        String deviceFingerprint = getDeviceFingerprint();
        if (deviceFingerprint.equals("hwid失败 请联系管理员")) {
            System.out.println(deviceFingerprint);
            return false;
        }
        System.out.println("设备指纹: " + deviceFingerprint);

        JSONObject payload = new JSONObject();
        payload.put("appid", appId);
        payload.put("username", username);
        payload.put("password", password);
        payload.put("kami_code", kamiCode);
        payload.put("device_id", deviceFingerprint);
        payload.put("timestamp", System.currentTimeMillis());

        JSONObject res = sendDirectActivateRequest(payload);

        if (res != null) {
            System.out.println("激活响应: " + res.toString(2));
            if (res.optBoolean("success", false)) {
                this.token = res.optString("temp_token", null);
                return true;
            } else {
                System.out.println("激活失败: " + res.optString("message", "未知错误"));
            }
        } else {
            System.out.println("未收到有效的激活响应");
        }
        return false;
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String md5(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
