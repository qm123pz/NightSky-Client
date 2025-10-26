package nightsky.checkerLOL;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;

public class GuiLoginScreen {
    private static final String BASE_URL = "http://lifey.icu";
    private static final int BLOCK_SIZE = 16;
    private static final int SESSION_TIMEOUT = 100;
    private static final byte[] DEFAULT_NEVER_KEY = "32字节的密钥".getBytes(StandardCharsets.UTF_8);

    private String sessionId;
    private String sessionKey;
    private long expireTime;
    private final String secret;
    private final String appId;

    private String token;
    private byte[] sessionHmac;

    public GuiLoginScreen(String appId, String secret) {
        this.appId = appId;
        this.secret = secret;
    }
    
    private byte[] customEncrypt(byte[] data, byte[] key) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("加密数据不能为空");
        }
        byte[] keyPadded = new byte[32];
        Arrays.fill(keyPadded, (byte) 0x00);
        System.arraycopy(key, 0, keyPadded, 0, Math.min(key.length, 32));

        byte[] encrypted = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            byte keyByte = keyPadded[i % 32];
            byte xorByte = (byte) (data[i] ^ keyByte);
            encrypted[i] = (byte) (((xorByte << 1) & 0xFE) | ((xorByte >> 7) & 0x01));
        }
        return encrypted;
    }
    
    private byte[] customDecrypt(byte[] data, byte[] key) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("解密数据不能为空");
        }
        byte[] keyPadded = new byte[32];
        Arrays.fill(keyPadded, (byte) 0x00);
        System.arraycopy(key, 0, keyPadded, 0, Math.min(key.length, 32));

        byte[] decrypted = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            byte keyByte = keyPadded[i % 32];
            byte shiftedByte = data[i];
            byte xorByte = (byte) (((shiftedByte >> 1) & 0x7F) | ((shiftedByte << 7) & 0x80));
            decrypted[i] = (byte) (xorByte ^ keyByte);
        }
        return decrypted;
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

    private String cipher(String data, String key, boolean encrypt) throws Exception {
        byte[] keyBytes;
        if (key == null) {
            keyBytes = Base64.decodeBase64(secret);
        } else {
            keyBytes = key.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            keyBytes = Arrays.copyOf(keyBytes, 32);
        } else {
            keyBytes = Arrays.copyOf(keyBytes, 32);
        }

        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        if (encrypt) {
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] ivBytes = new byte[BLOCK_SIZE];
            SecureRandom random = new SecureRandom();
            random.nextBytes(ivBytes);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(ivBytes));

            byte[] encrypted = cipher.doFinal(dataBytes);
            byte[] combined = new byte[ivBytes.length + encrypted.length];
            System.arraycopy(ivBytes, 0, combined, 0, ivBytes.length);
            System.arraycopy(encrypted, 0, combined, ivBytes.length, encrypted.length);

            return Base64.encodeBase64String(combined);
        } else {
            byte[] combined = Base64.decodeBase64(data);
            byte[] ivBytes = Arrays.copyOfRange(combined, 0, BLOCK_SIZE);
            byte[] encrypted = Arrays.copyOfRange(combined, BLOCK_SIZE, combined.length);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(ivBytes));

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        }
    }

    private JSONObject buildNeverLoginRequest(JSONObject payload) throws Exception {
        payload.put("timestamp", System.currentTimeMillis());
        payload.put("nonce", UUID.randomUUID().toString());
        payload.put("appid", appId);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyHash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
        byte[] dataBytes = payload.toString().getBytes(StandardCharsets.UTF_8);
        byte[] encryptedData = customEncrypt(dataBytes, keyHash);
        byte[] appidBytes = appId.getBytes(StandardCharsets.UTF_8);
        if (appidBytes.length > 255) {
            throw new IllegalArgumentException("appid过长，不能超过255字节");
        }
        byte[] prefix = new byte[1 + appidBytes.length];
        prefix[0] = (byte) appidBytes.length;
        System.arraycopy(appidBytes, 0, prefix, 1, appidBytes.length);
        byte[] combined = new byte[prefix.length + encryptedData.length];
        System.arraycopy(prefix, 0, combined, 0, prefix.length);
        System.arraycopy(encryptedData, 0, combined, prefix.length, encryptedData.length);
        String encrypted = Base64.encodeBase64String(combined);

        JSONObject request = new JSONObject();
        request.put("encrypted_data", encrypted);
        return request;
    }

    private JSONObject sendNeverLoginRequest(JSONObject payload) {
        try {
            JSONObject request = buildNeverLoginRequest(payload);
            URL url = new URL(BASE_URL + "/api/user/never-login");
            System.out.println("发送请求到: " + url.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = request.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("响应码: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    String responseStr = response.toString();

                    JSONObject jsonResponse = new JSONObject(responseStr);
                    if (jsonResponse.has("encrypted_response")) {
                        String encryptedResp = jsonResponse.getString("encrypted_response");
                        byte[] encryptedBytes = Base64.decodeBase64(encryptedResp);
                        System.out.println("解密后字节长度: " + encryptedBytes.length);
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        byte[] keyHash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
                        byte[] decryptedBytes = customDecrypt(encryptedBytes, keyHash);
                        String decrypted = new String(decryptedBytes, StandardCharsets.UTF_8)
                                .replaceAll("[^\\x20-\\x7E\\x0A\\x0D]", "");
                        decrypted = decrypted.trim();
                        if (decrypted.isEmpty()) {
                            System.out.println("解密结果为空");
                            return null;
                        }
                        decrypted = decrypted.replaceAll(",\\s*}", "}");
                        decrypted = decrypted.replaceAll(",\\s*]", "]");
                        decrypted = decrypted.replaceAll("\\s+", " ");
                        if (!decrypted.startsWith("{")) {
                            int startIndex = decrypted.indexOf("{");
                            int endIndex = decrypted.lastIndexOf("}");
                            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                                decrypted = decrypted.substring(startIndex, endIndex + 1);
                            } else {
                                return null;
                            }
                        }

                        return new JSONObject(decrypted);
                    } else {
                        System.out.println("响应中不包含encrypted_response字段");
                        return jsonResponse;
                    }

                }
            } else {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder error = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        error.append(line);
                    }
                    try {
                        JSONObject errorJson = new JSONObject(error.toString());
                        if (errorJson.has("encrypted_response")) {
                            String encryptedErr = errorJson.getString("encrypted_response");
                            byte[] encryptedBytes = Base64.decodeBase64(encryptedErr);
                            byte[] decryptedBytes = customDecrypt(encryptedBytes, DEFAULT_NEVER_KEY);
                            String decryptedErr = new String(decryptedBytes, StandardCharsets.UTF_8)
                                    .replaceAll("[^\\x20-\\x7E\\x0A\\x0D]", "");
                            decryptedErr = decryptedErr.trim();
                            decryptedErr = decryptedErr.replaceAll(",\\s*}", "}");
                            decryptedErr = decryptedErr.replaceAll(",\\s*]", "]");

                            return new JSONObject(decryptedErr);
                        }
                    } catch (Exception e) {
                        JSONObject errorObj = new JSONObject();
                        errorObj.put("success", false);
                        errorObj.put("message", "服务器返回错误: " + e.getMessage());
                        return errorObj;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject errorObj = new JSONObject();
            errorObj.put("success", false);
            errorObj.put("message", "请求处理失败: " + e.getMessage());
            return errorObj;
        }
        return null;
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

    private byte[] createSessionHmac() {
        if (sessionId == null || sessionKey == null) return null;

        String data = sessionId + sessionKey + expireTime;
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(
                    Base64.decodeBase64(secret), "HmacSHA256"));
            return hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }

    private boolean verifySessionHmac() {
        if (sessionHmac == null) return false;
        byte[] currentHmac = createSessionHmac();
        return Arrays.equals(sessionHmac, currentHmac);
    }
    
    public boolean neverLogin(String user, String pwd) {
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
        payload.put("username", user);
        payload.put("password", pwd);
        payload.put("device_id", deviceFingerprint);

        JSONObject res = sendNeverLoginRequest(payload);

        if (res != null) {
            if (res.optBoolean("success", false)) {
                sessionId = res.optString("session_id", null);
                sessionKey = res.optString("session_key", null);
                token = res.optString("encrypted_token", null);
                expireTime = res.optLong("expire_time", 0);

                if (sessionId == null || sessionKey == null || token == null) {
                    System.out.println("会话信息不完整");
                    return false;
                }

                sessionHmac = createSessionHmac();
                return true;
            } else {
                System.out.println("never-login失败: " + res.optString("message", "未知错误"));
            }
        } else {
            System.out.println("未收到有效的响应");
        }
        return false;
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String md5(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
