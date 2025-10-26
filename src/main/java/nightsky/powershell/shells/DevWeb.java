package nightsky.powershell.shells;

import nightsky.NightSky;
import nightsky.powershell.PowerShell;
import nightsky.util.ChatUtil;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class DevWeb extends PowerShell {
    private static final String HTML_CONTENT =
            "<!DOCTYPE html>\n" +
                    "<html lang=\"zh-CN\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>NightSky Devlog</title>\n" +
                    "    <style>\n" +
                    "        * {\n" +
                    "            margin: 0;\n" +
                    "            padding: 0;\n" +
                    "            box-sizing: border-box;\n" +
                    "            font-family: 'Consolas', 'Microsoft YaHei', monospace;\n" +
                    "        }\n" +
                    "        \n" +
                    "        body {\n" +
                    "            background-color: #0a0a1a;\n" +
                    "            color: #e0e0e0;\n" +
                    "            line-height: 1.6;\n" +
                    "            background-image: linear-gradient(rgba(10, 10, 26, 0.95), rgba(10, 10, 26, 0.95)), \n" +
                    "                              url('data:image/svg+xml;utf8,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\" viewBox=\"0 0 100 100\"><rect width=\"100\" height=\"100\" fill=\"none\"/><circle cx=\"20\" cy=\"20\" r=\"1\" fill=\"%23333\"/><circle cx=\"50\" cy=\"30\" r=\"1\" fill=\"%23333\"/><circle cx=\"80\" cy=\"20\" r=\"1\" fill=\"%23333\"/><circle cx=\"40\" cy=\"60\" r=\"1\" fill=\"%23333\"/><circle cx=\"70\" cy=\"70\" r=\"1\" fill=\"%23333\"/><circle cx=\"10\" cy=\"80\" r=\"1\" fill=\"%23333\"/></svg>');\n" +
                    "        }\n" +
                    "        \n" +
                    "        .container {\n" +
                    "            max-width: 1000px;\n" +
                    "            margin: 0 auto;\n" +
                    "            padding: 20px;\n" +
                    "        }\n" +
                    "        \n" +
                    "        header {\n" +
                    "            text-align: center;\n" +
                    "            padding: 40px 0;\n" +
                    "            border-bottom: 1px solid #333366;\n" +
                    "            margin-bottom: 40px;\n" +
                    "        }\n" +
                    "        \n" +
                    "        h1 {\n" +
                    "            font-size: 2.5rem;\n" +
                    "            color: #4a76ee;\n" +
                    "            margin-bottom: 10px;\n" +
                    "            text-shadow: 0 0 10px rgba(74, 118, 238, 0.5);\n" +
                    "        }\n" +
                    "        \n" +
                    "        .subtitle {\n" +
                    "            font-size: 1.1rem;\n" +
                    "            color: #aaaacc;\n" +
                    "            margin-bottom: 20px;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .devlog-container {\n" +
                    "            position: relative;\n" +
                    "            margin: 40px 0;\n" +
                    "        }\n" +
                    "        \n" +
                    "        /* 时间线样式 */\n" +
                    "        .timeline {\n" +
                    "            position: relative;\n" +
                    "            max-width: 800px;\n" +
                    "            margin: 0 auto;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .timeline::after {\n" +
                    "            content: '';\n" +
                    "            position: absolute;\n" +
                    "            width: 4px;\n" +
                    "            background-color: #333366;\n" +
                    "            top: 0;\n" +
                    "            bottom: 0;\n" +
                    "            left: 50%;\n" +
                    "            margin-left: -2px;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .log-entry {\n" +
                    "            padding: 10px 40px;\n" +
                    "            position: relative;\n" +
                    "            width: 50%;\n" +
                    "            box-sizing: border-box;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .log-entry:nth-child(odd) {\n" +
                    "            left: 0;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .log-entry:nth-child(even) {\n" +
                    "            left: 50%;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .log-content {\n" +
                    "            padding: 20px;\n" +
                    "            background-color: rgba(20, 20, 40, 0.7);\n" +
                    "            border-radius: 8px;\n" +
                    "            border: 1px solid #333366;\n" +
                    "            position: relative;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .log-entry:nth-child(odd) .log-content::after {\n" +
                    "            content: '';\n" +
                    "            position: absolute;\n" +
                    "            width: 20px;\n" +
                    "            height: 20px;\n" +
                    "            right: -10px;\n" +
                    "            background-color: #4a76ee;\n" +
                    "            top: 25px;\n" +
                    "            border-radius: 50%;\n" +
                    "            z-index: 1;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .log-entry:nth-child(even) .log-content::after {\n" +
                    "            content: '';\n" +
                    "            position: absolute;\n" +
                    "            width: 20px;\n" +
                    "            height: 20px;\n" +
                    "            left: -10px;\n" +
                    "            background-color: #4a76ee;\n" +
                    "            top: 25px;\n" +
                    "            border-radius: 50%;\n" +
                    "            z-index: 1;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .log-date {\n" +
                    "            font-weight: bold;\n" +
                    "            color: #6a9eff;\n" +
                    "            margin-bottom: 10px;\n" +
                    "            font-size: 1.1rem;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .log-title {\n" +
                    "            font-size: 1.3rem;\n" +
                    "            color: #ffffff;\n" +
                    "            margin-bottom: 10px;\n" +
                    "            border-bottom: 1px solid #333366;\n" +
                    "            padding-bottom: 5px;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .log-details {\n" +
                    "            margin-top: 15px;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .log-details ul {\n" +
                    "            margin-left: 20px;\n" +
                    "            margin-bottom: 10px;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .log-details li {\n" +
                    "            margin-bottom: 5px;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .status {\n" +
                    "            display: inline-block;\n" +
                    "            padding: 3px 8px;\n" +
                    "            border-radius: 4px;\n" +
                    "            font-size: 0.8rem;\n" +
                    "            margin-left: 10px;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .status-completed {\n" +
                    "            background-color: #2a5a2a;\n" +
                    "            color: #90ee90;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .status-in-progress {\n" +
                    "            background-color: #5a5a2a;\n" +
                    "            color: #ffff90;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .status-planned {\n" +
                    "            background-color: #5a2a2a;\n" +
                    "            color: #ff9090;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .version-badge {\n" +
                    "            display: inline-block;\n" +
                    "            background: linear-gradient(135deg, #4a76ee, #6a11cb);\n" +
                    "            color: white;\n" +
                    "            padding: 5px 10px;\n" +
                    "            border-radius: 15px;\n" +
                    "            font-size: 0.9rem;\n" +
                    "            margin-left: 10px;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .warning {\n" +
                    "            background: rgba(120, 20, 20, 0.3);\n" +
                    "            border-left: 4px solid #cc3333;\n" +
                    "            padding: 15px;\n" +
                    "            margin: 30px 0;\n" +
                    "            border-radius: 0 8px 8px 0;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .warning h3 {\n" +
                    "            color: #ff6666;\n" +
                    "            margin-bottom: 10px;\n" +
                    "        }\n" +
                    "        \n" +
                    "        footer {\n" +
                    "            text-align: center;\n" +
                    "            padding: 20px;\n" +
                    "            margin-top: 40px;\n" +
                    "            border-top: 1px solid #333366;\n" +
                    "            color: #8888aa;\n" +
                    "            font-size: 0.9rem;\n" +
                    "        }\n" +
                    "        \n" +
                    "        @media (max-width: 768px) {\n" +
                    "            .timeline::after {\n" +
                    "                left: 31px;\n" +
                    "            }\n" +
                    "            \n" +
                    "            .log-entry {\n" +
                    "                width: 100%;\n" +
                    "                padding-left: 70px;\n" +
                    "                padding-right: 25px;\n" +
                    "            }\n" +
                    "            \n" +
                    "            .log-entry:nth-child(even) {\n" +
                    "                left: 0;\n" +
                    "            }\n" +
                    "            \n" +
                    "            .log-content::after {\n" +
                    "                left: 21px !important;\n" +
                    "                right: auto !important;\n" +
                    "            }\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"container\">\n" +
                    "        <header>\n" +
                    "            <h1>NightSky Devlog</h1>\n" +
                    "            <div class=\"subtitle\">记录每一次傻逼更新</div>\n" +
                    "        </header>\n" +
                    "        \n" +
                    "        <div class=\"devlog-container\">\n" +
                    "            <div class=\"timeline\">\n" +
                    "                <div class=\"log-entry\">\n" +
                    "                    <div class=\"log-content\">\n" +
                    "                        <div class=\"log-date\">2025年10月26日 <span class=\"version-badge\">Release b1</span></div>\n" +
                    "                        <div class=\"log-title\">ChangeLog v5<span class=\"status status-completed\">已完成</span></div>\n" +
                    "                        <div class=\"log-details\">\n" +
                    "                            <p>PartySpammer</p>\n" +
                    "                            <ul>\n" +
                    "                                <li>[Important]OpenSrc</li>\n" +
                    "                                <li>[Refactor]DynamicIsland</li>\n" +
                    "                                <li>[Improved]ChestStealer</li>\n" +
                    "                                <li>[Added]PartySpammer</li>\n" +
                    "                                <li>[Added]AugustusClickGui</li>\n" +
                    "                                <li>[Added]SimulationRotation</li>\n" +
                    "                                <li>[Added]Camera</li>\n" +
                    "                                <li>[Added]ExhibitionTargetHUD</li>\n" +
                    "                                <li>[Added]ExhibitionWaterMark</li>\n" +
                    "                                <li>[Added]NotificationDisplay</li>\n" +
                    "                                <li>[Added]ChestView</li>\n" +
                    "                                <li>[Added]Rotate-JumpReset</li>\n" +
                    "                                <li>[Added]DropShadow</li>\n" +
                    "                                <li>[Added]PlaySounds</li>\n" +
                    "                                <li>[Added]ProtectedNyaIP</li>\n" +
                    "                            </ul>\n" +
                    "                        </div>\n" +
                    "                    </div>\n" +
                    "                </div>\n" +
                    "                \n" +
                    "                <div class=\"log-entry\">\n" +
                    "                    <div class=\"log-content\">\n" +
                    "                        <div class=\"log-date\">2025年10月12日 <span class=\"version-badge\">Private v2</span></div>\n" +
                    "                        <div class=\"log-title\">ChangeLog v4<span class=\"status status-completed\">已完成</span></div>\n" +
                    "                        <div class=\"log-details\">\n" +
                    "                            <p>更新日志</p>\n" +
                    "                            <ul>\n" +
                    "                                <li>[Added]DynamicIsland</li>\n" +
                    "                            </ul>\n" +
                    "                        </div>\n" +
                    "                    </div>\n" +
                    "                </div>\n" +
                    "                \n" +
                    "                <div class=\"log-entry\">\n" +
                    "                    <div class=\"log-content\">\n" +
                    "                        <div class=\"log-date\">2025年10月7日 <span class=\"version-badge\">Private v1</span></div>\n" +
                    "                        <div class=\"log-title\">ChangeLog v3<span class=\"status status-completed\">已完成</span></div>\n" +
                    "                        <div class=\"log-details\">\n" +
                    "                            <p>更新日志</p>\n" +
                    "                            <ul>\n" +
                    "                                <li>[Improved]AutoProjectile-Prediction</li>\n" +
                    "                                <li>[Fixed]ClickGuiBug</li>\n" +
                    "                                <li>[Added]BlurShader</li>\n" +
                    "                                <li>[Added]Velocity-Prediction</li>\n" +
                    "                                <li>[Added]GuiMainMenu</li>\n" +
                    "                                <li>[Added]验证？</li>\n" +
                    "                            </ul>\n" +
                    "                        </div>\n" +
                    "                    </div>\n" +
                    "                </div>\n" +
                    "                \n" +
                    "                <div class=\"log-entry\">\n" +
                    "                    <div class=\"log-content\">\n" +
                    "                        <div class=\"log-date\">2025年10月6日 <span class=\"version-badge\">test2</span></div>\n" +
                    "                        <div class=\"log-title\">ChangeLog v2<span class=\"status status-completed\">已完成</span></div>\n" +
                    "                        <div class=\"log-details\">\n" +
                    "                            <p>更新日志</p>\n" +
                    "                            <ul>\n" +
                    "                                <li>[Added]FontRenderer</li>\n" +
                    "                                <li>[Added]AutoProjectile</li>\n" +
                    "                                <li>[Added]FullAutoBlock</li>\n" +
                    "                                <li>[Added]AutoBlockNoSlow</li>\n" +
                    "                            </ul>\n" +
                    "                        </div>\n" +
                    "                    </div>\n" +
                    "                </div>\n" +
                    "                \n" +
                    "                <div class=\"log-entry\">\n" +
                    "                    <div class=\"log-content\">\n" +
                    "                        <div class=\"log-date\">2025年10月5日 <span class=\"version-badge\">test1</span></div>\n" +
                    "                        <div class=\"log-title\">ChangeLog v1<span class=\"status status-completed\">已完成</span></div>\n" +
                    "                        <div class=\"log-details\">\n" +
                    "                            <p>更新日志</p>\n" +
                    "                            <ul>\n" +
                    "                                <li>[Refactor]ClickGui</li>\n" +
                    "                                <li>[Refactor]HUD</li>\n" +
                    "                                <li>[Fixed]BlinkManager</li>\n" +
                    "                                <li>[Fixed]AutoBlock</li>\n" +
                    "                                <li>[Added]ArrayList</li>\n" +
                    "                                <li>[Added]JumpCircle</li>\n" +
                    "                            </ul>\n" +
                    "                        </div>\n" +
                    "                    </div>\n" +
                    "                </div>\n" +
                    "                \n" +
                    "                <div class=\"log-entry\">\n" +
                    "                    <div class=\"log-content\">\n" +
                    "                        <div class=\"log-date\">2025年10月3日 <span class=\"version-badge\">Test</span></div>\n" +
                    "                        <div class=\"log-title\">更换base <span class=\"status status-completed\">已完成</span></div>\n" +
                    "                        <div class=\"log-details\">\n" +
                    "                            <p>更换3次base</p>\n" +
                    "                            <ul>\n" +
                    "                                <li>我们的大神dev先把主base从moonlight更换为myau</li>\n" +
                    "                                <li>之后发现渲染难写换回moonlight</li>\n" +
                    "                                <li>之后发现绕过难滑所以换回myau</li>\n" +
                    "                                <li>(实际上NightSky在没选定好方向前已经换过10余次base了)</li>\n" +
                    "                            </ul>\n" +
                    "                        </div>\n" +
                    "                    </div>\n" +
                    "                </div>\n" +
                    "            </div>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <footer>\n" +
                    "            <p>NightSky DevLog 2025</p>\n" +
                    "        </footer>\n" +
                    "    </div>\n" +
                    "</body>\n" +
                    "</html>";

    public DevWeb() {
        super(new ArrayList<>(Arrays.asList("ChangeLog")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        if (args.size() > 1 && "help".equalsIgnoreCase(args.get(1))) {
            sendUsage(args);
            return;
        }

        try {
            openHTMLFile();
            ChatUtil.sendFormatted(String.format("%s正在打开NightSky开发日志...", NightSky.clientName));
        } catch (Exception e) {
            ChatUtil.sendFormatted(String.format("%s打开网页时出错: %s", NightSky.clientName, e.getMessage()));
        }
    }

    private void openHTMLFile() {
        try {
            File tempFile = File.createTempFile("nightsky_devlog", ".html");
            tempFile.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(HTML_CONTENT.getBytes(StandardCharsets.UTF_8));
            }
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(tempFile.toURI());
            } else {
                throw new Exception("无法打开浏览器，桌面操作不被支持");
            }
        } catch (IOException e) {
            throw new RuntimeException("创建HTML文件失败: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("打开浏览器失败: " + e.getMessage());
        }
    }

    private void sendUsage(ArrayList<String> args) {
        String baseCommand = args.get(0).toLowerCase();
        ChatUtil.sendFormatted(
                String.format("%sUsage: .%s - 打开NightSky开发日志页面",
                        NightSky.clientName,
                        baseCommand
                )
        );
    }
}