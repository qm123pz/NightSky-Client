package nightsky.powershell.shells;

import nightsky.NightSky;
import nightsky.powershell.PowerShell;
import nightsky.util.ChatUtil;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class AIChat extends PowerShell {

    private static final Map<String, ModelConfig> MODEL_CONFIGS = new HashMap<>();
    private static final Map<String, String> MODEL_DISPLAY_NAMES = new HashMap<>();

    private static String currentModelId = "siliconflow1";
    private static final List<Conversation> conversationHistory = new ArrayList<>();
    private static boolean showThinking = true;
    private static final AtomicBoolean isGenerating = new AtomicBoolean(false);

    private static class ModelConfig {
        String id;
        String name;
        String apiKey;
        String apiUrl;
        String modelName;
        boolean supportsThinking;

        public ModelConfig(String id, String name, String apiKey, String apiUrl, String modelName, boolean supportsThinking) {
            this.id = id;
            this.name = name;
            this.apiKey = apiKey;
            this.apiUrl = apiUrl;
            this.modelName = modelName;
            this.supportsThinking = supportsThinking;
        }
    }

    // 对话记录类
    private static class Conversation {
        String userMessage;
        String aiResponse;
        String thinking;
        String modelName;
        long timestamp;

        public Conversation(String user, String ai, String thinking, String model) {
            this.userMessage = user;
            this.aiResponse = ai;
            this.thinking = thinking;
            this.modelName = model;
            this.timestamp = System.currentTimeMillis();
        }

        public String formatTime() {
            return String.format("%tH:%tM:%tS", timestamp, timestamp, timestamp);
        }
    }

    static {
        String apiKey = "sk-movrcedzngjzsybleueckfciudvjtlvuthuwobldgttbfscv";

        MODEL_CONFIGS.put("siliconflow1", new ModelConfig(
            "siliconflow1",
            "DeepSeek-R1 (支持思考)",
            apiKey,
            "https://api.siliconflow.cn/v1/chat/completions",
            "deepseek-ai/DeepSeek-R1-0528-Qwen3-8B",
            true
        ));

        MODEL_CONFIGS.put("siliconflow4", new ModelConfig(
            "siliconflow4",
            "GLM-4-9B",
            apiKey,
            "https://api.siliconflow.cn/v1/chat/completions",
            "THUDM/glm-4-9b-chat",
            false
        ));

        MODEL_CONFIGS.put("siliconflow8", new ModelConfig(
            "siliconflow8",
            "Kolors",
            apiKey,
            "https://api.siliconflow.cn/v1/chat/completions",
            "Kwai-Kolors/Kolors",
            false
        ));

        MODEL_CONFIGS.put("siliconflow9", new ModelConfig(
            "siliconflow9",
            "DeepSeek-R1-Distill-Qwen-7B",
            apiKey,
            "https://api.siliconflow.cn/v1/chat/completions",
            "deepseek-ai/DeepSeek-R1-Distill-Qwen-7B",
            true
        ));
        MODEL_DISPLAY_NAMES.put("1", "siliconflow1");
        MODEL_DISPLAY_NAMES.put("2", "siliconflow4");
        MODEL_DISPLAY_NAMES.put("3", "siliconflow8");
        MODEL_DISPLAY_NAMES.put("4", "siliconflow9");
    }

    public AIChat() {
        super(new ArrayList<>(Arrays.asList("AI")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        if (args.size() < 2) {
            displayUsage();
            return;
        }

        String subCommand = args.get(1).toLowerCase(Locale.ROOT);

        switch (subCommand) {
            case "ask":
            case "q":
            case "question":
                if (args.size() >= 3) {
                    String question = String.join(" ", args.subList(2, args.size()));
                    askQuestion(question);
                } else {
                    ChatUtil.sendFormatted(String.format("%s请输入问题&r", NightSky.clientName));
                }
                break;

            case "switch":
            case "change":
            case "model":
                if (args.size() >= 3) {
                    switchModel(args.get(2));
                } else {
                    listModels();
                }
                break;

            case "models":
            case "list":
                listModels();
                break;

            case "thinking":
            case "think":
                toggleThinking();
                break;

            case "clear":
            case "reset":
                clearHistory();
                break;

            case "history":
            case "hist":
                showHistory();
                break;

            case "help":
                displayHelp();
                break;

            case "info":
                displayInfo();
                break;

            case "stop":
            case "cancel":
                stopGeneration();
                break;

            default:
                String question = String.join(" ", args.subList(1, args.size()));
                askQuestion(question);
                break;
        }
    }

    private void displayUsage() {
        ChatUtil.sendFormatted(String.format(
            "%sAI对话命令使用说明:&r\n" +
            "%s» &7.ai <&o问题&r>&7 - 向AI提问&r\n" +
            "%s» &7.ai ask <&o问题&r>&7 - 向AI提问&r\n" +
            "%s» &7.ai switch <&o1-4&r>&7 - 切换AI模型&r\n" +
            "%s» &7.ai models&7 - 查看可用模型列表&r\n" +
            "%s» &7.ai thinking&7 - 切换思考过程显示&r\n" +
            "%s» &7.ai clear&7 - 清空对话历史&r\n" +
            "%s» &7.ai history&7 - 查看对话历史&r\n" +
            "%s» &7.ai info&7 - 查看当前配置信息&r\n" +
            "%s» &7.ai stop&7 - 停止当前生成&r\n" +
            "%s» &7.ai help&7 - 显示帮助信息&r",
            NightSky.clientName,
            "&8", "&8", "&8", "&8", "&8", "&8", "&8", "&8", "&8", "&8"
        ));
    }

    private void displayHelp() {
        ModelConfig currentConfig = MODEL_CONFIGS.get(currentModelId);
        ChatUtil.sendFormatted(String.format(
            "%sAI对话帮助:&r\n" +
            "%s支持4个AI模型，包含DeepSeek-R1、GLM-4-9B、Kolors等&r\n" +
            "%s支持思考过程显示（DeepSeek-R1系列模型）&r\n" +
            "%s使用 .ai models 查看所有可用模型&r\n" +
            "%s当前模型: &o%s&r\n" +
            "%s当前模型是否支持思考: &o%s&r",
            NightSky.clientName,
            "&7", "&7", "&7", "&7", currentConfig.name,
            "&7", currentConfig.supportsThinking ? "是" : "否"
        ));
    }

    private void listModels() {
        ChatUtil.sendFormatted(String.format("%s可用AI模型 (&o4个&r):&r", NightSky.clientName));

        for (Map.Entry<String, String> entry : MODEL_DISPLAY_NAMES.entrySet()) {
            String modelId = entry.getValue();
            ModelConfig config = MODEL_CONFIGS.get(modelId);
            String currentMark = modelId.equals(currentModelId) ? " &l[当前]&r" : "";
            String thinkingMark = config.supportsThinking ? " &8(支持思考)&r" : "";

            ChatUtil.sendFormatted(String.format("%s%s. &7%s%s%s&r",
                "&8", entry.getKey(), config.name, currentMark, thinkingMark));
        }

        ChatUtil.sendFormatted(String.format("%s使用 &o.ai switch <1-4>&r 切换模型&r", "&7"));
    }

    private void switchModel(String modelId) {
        if (MODEL_DISPLAY_NAMES.containsKey(modelId)) {
            String newModelId = MODEL_DISPLAY_NAMES.get(modelId);
            ModelConfig config = MODEL_CONFIGS.get(newModelId);
            currentModelId = newModelId;
            ChatUtil.sendFormatted(String.format("%s已切换到模型: &o%s&r", NightSky.clientName, config.name));

            if (!config.supportsThinking && showThinking) {
                showThinking = false;
                ChatUtil.sendFormatted(String.format("%s当前模型不支持思考，已自动关闭思考显示&r", "&7"));
            }
        } else {
            ChatUtil.sendFormatted(String.format("%s无效的模型ID，请输入1-4，使用 &o.ai models&r 查看列表&r", NightSky.clientName));
        }
    }

    private void toggleThinking() {
        ModelConfig config = MODEL_CONFIGS.get(currentModelId);

        if (!config.supportsThinking) {
            ChatUtil.sendFormatted(String.format("%s当前模型 &o%s&r 不支持思考过程显示&r", NightSky.clientName, config.name));
            return;
        }

        showThinking = !showThinking;
        String status = showThinking ? "&a开启&r" : "&c关闭&r";
        ChatUtil.sendFormatted(String.format("%s思考过程显示: %s&r", NightSky.clientName, status));
    }

    private void clearHistory() {
        conversationHistory.clear();
        ChatUtil.sendFormatted(String.format("%s已清空对话历史&r", NightSky.clientName));
    }

    private void showHistory() {
        if (conversationHistory.isEmpty()) {
            ChatUtil.sendFormatted(String.format("%s暂无对话历史&r", NightSky.clientName));
            return;
        }

        ChatUtil.sendFormatted(String.format("%s对话历史 (&o%d&r条):&r", NightSky.clientName, conversationHistory.size()));

        for (int i = 0; i < conversationHistory.size(); i++) {
            Conversation conv = conversationHistory.get(i);
            ChatUtil.sendFormatted(String.format("%s%d. [&o%s&r][&o%s&r] &7%s&r",
                "&8", i + 1, conv.formatTime(), conv.modelName, shortenMessage(conv.userMessage)));
        }
    }

    private void stopGeneration() {
        if (isGenerating.get()) {
            isGenerating.set(false);
            ChatUtil.sendFormatted(String.format("%s正在停止生成...&r", NightSky.clientName));
        } else {
            ChatUtil.sendFormatted(String.format("%s当前没有正在进行的生成&r", NightSky.clientName));
        }
    }

    private void displayInfo() {
        ModelConfig config = MODEL_CONFIGS.get(currentModelId);
        String thinkingStatus = showThinking ? "&a开启&r" : "&c关闭&r";//试试行不行 ai写的

        ChatUtil.sendFormatted(String.format(
            "%sAI对话配置信息:&r\n" +
            "%s» &7当前模型: &o%s&r\n" +
            "%s» &7模型ID: &o%s&r\n" +
            "%s» &7思考支持: &o%s&r\n" +
            "%s» &7思考显示: %s&r\n" +
            "%s» &7对话历史: &o%d&r条&r\n" +
            "%s» &7API端点: &o%s&r",
            NightSky.clientName,
            "&8", config.name,
            "&8", config.modelName,
            "&8", config.supportsThinking ? "是" : "否",
            "&8", config.supportsThinking ? thinkingStatus : "不支持",
            "&8", conversationHistory.size(),
            "&8", config.apiUrl
        ));
    }

    private void askQuestion(String question) {
        if (isGenerating.get()) {
            ChatUtil.sendFormatted(String.format("%s当前正在生成回答，请等待或使用 &o.ai stop&r 停止&r", NightSky.clientName));
            return;
        }

        ChatUtil.sendFormatted(String.format("%s正在思考...&r", NightSky.clientName));

        isGenerating.set(true);

        CompletableFuture.runAsync(() -> {
            try {
                long startTime = System.currentTimeMillis();
                ModelConfig config = MODEL_CONFIGS.get(currentModelId);
                AIChatResponse response = callSiliconFlowAPI(config, question);
                if (!isGenerating.get()) {
                    Minecraft.getMinecraft().addScheduledTask(() -> {
                        ChatUtil.sendFormatted(String.format("%s生成已停止&r", NightSky.clientName));
                    });
                    return;
                }
                long elapsedTime = System.currentTimeMillis() - startTime;
                String timeInfo = String.format("[&o%.1fs&r]", elapsedTime / 1000.0);
                conversationHistory.add(new Conversation(question, response.content, response.thinking, config.name));
                String finalResponse = String.format("%sAI %s: &r%s",
                    NightSky.clientName, timeInfo, formatResponse(response.content));
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    ChatUtil.sendFormatted(finalResponse);
                    if (config.supportsThinking && showThinking && response.thinking != null && !response.thinking.isEmpty()) {
                        displayThinkingProcess(response.thinking);
                    }
                });

            } catch (Exception e) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    ChatUtil.sendFormatted(String.format("%s请求失败: &o%s&r",
                        NightSky.clientName, e.getMessage()));
                });
                e.printStackTrace();
            } finally {
                isGenerating.set(false);
            }
        });
    }

    private static class AIChatResponse {
        String content;
        String thinking;

        public AIChatResponse(String content, String thinking) {
            this.content = content;
            this.thinking = thinking;
        }
    }

    private AIChatResponse callSiliconFlowAPI(ModelConfig config, String message) throws Exception {
        String requestBody;

        if (config.supportsThinking) {
            requestBody = String.format(
                "{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}],\"temperature\":0.7,\"max_tokens\":1024,\"stream\":false,\"reasoning_effort\":\"medium\"}",
                config.modelName, escapeJson(message)
            );
        } else {
            requestBody = String.format(
                "{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}],\"temperature\":0.7,\"max_tokens\":1024,\"stream\":false}",
                config.modelName, escapeJson(message)
            );
        }
        java.net.URL url = new java.net.URL(config.apiUrl);
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + config.apiKey);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        try (java.io.OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("HTTP错误: " + responseCode + " - " + connection.getResponseMessage());
        }
        StringBuilder response = new StringBuilder();
        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(connection.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        String jsonResponse = response.toString();
        return parseAIResponse(jsonResponse, config.supportsThinking);
    }

    private AIChatResponse parseAIResponse(String jsonResponse, boolean supportsThinking) {
        try {
            String content = "";
            String thinking = "";

            int contentIndex = jsonResponse.indexOf("\"content\":\"");
            if (contentIndex != -1) {
                int start = contentIndex + 11;
                int end = jsonResponse.indexOf("\"", start);
                if (end != -1) {
                    content = jsonResponse.substring(start, end);
                    content = unescapeJson(content);
                }
            }

            if (supportsThinking) {
                String[] thinkingFields = {"\"reasoning_content\":\"", "\"reasoning\":\""};
                for (String field : thinkingFields) {
                    int thinkingIndex = jsonResponse.indexOf(field);
                    if (thinkingIndex != -1) {
                        int start = thinkingIndex + field.length();
                        int end = jsonResponse.indexOf("\"", start);
                        if (end != -1) {
                            thinking = jsonResponse.substring(start, end);
                            thinking = unescapeJson(thinking);
                            break;
                        }
                    }
                }
            }

            return new AIChatResponse(content, thinking);

        } catch (Exception e) {
            throw new RuntimeException("解析AI响应失败: " + e.getMessage());
        }
    }

    private void displayThinkingProcess(String thinking) {
        String cleanThinking = thinking
            .replace("<think>", "")
            .replace("</think>", "")
            .replace("\n\n", "\n")
            .trim();

        ChatUtil.sendFormatted(String.format("%s🤔 AI思考过程:&r", "&8"));
        String[] lines = splitIntoLines(cleanThinking, 80);
        for (String line : lines) {
            ChatUtil.sendFormatted(String.format("%s  %s&r", "&7", line));
        }

        ChatUtil.sendFormatted(""); // 空行
    }

    private String formatResponse(String response) {
        return response.replace("\n\n", "\n").trim();
    }

    private String shortenMessage(String message) {
        if (message.length() > 50) {
            return message.substring(0, 50) + "...";
        }
        return message;
    }

    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    private String unescapeJson(String str) {
        return str.replace("\\n", "\n")
                  .replace("\\r", "\r")
                  .replace("\\t", "\t")
                  .replace("\\\"", "\"")
                  .replace("\\\\", "\\");
    }

    private String[] splitIntoLines(String text, int maxLength) {
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        for (String word : text.split(" ")) {
            if (currentLine.length() + word.length() + 1 > maxLength) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines.toArray(new String[0]);
    }
}