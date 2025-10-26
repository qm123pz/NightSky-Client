package nightsky.powershell;

import nightsky.NightSky;
import nightsky.event.EventTarget;
import nightsky.event.types.EventType;
import nightsky.event.types.Priority;
import nightsky.events.PacketEvent;
import nightsky.module.Module;
import nightsky.util.ChatUtil;
import net.minecraft.network.play.client.C01PacketChatMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Handler {
    public ArrayList<PowerShell> powerShells;

    public Handler() {
        this.powerShells = new ArrayList<>();
    }

    public void handleCommand(String string) {
        List<String> params = Arrays.asList(string.substring(1).trim().split("\\s+"));
        ArrayList<String> arrayList = new ArrayList<>(params);
        if (params.get(0).isEmpty()) {
            ChatUtil.sendFormatted(String.format("%sUnknown powershell&r", NightSky.clientName).replace("&", "§"));
        } else {
            for (PowerShell powerShell : NightSky.handler.powerShells) {
                for (String name : powerShell.names) {
                    if (params.get(0).equalsIgnoreCase(name)) {
                        powerShell.runCommand(arrayList);
                        return;
                    }
                }
            }
            
            // 移除模块直接命令处理逻辑
            // 不再支持 .killaura、.aimassist 等模块直接命令
            
            ChatUtil.sendFormatted(String.format("%sUnknown powershell (&o%s&r)&r", NightSky.clientName, params.get(0)).replace("&", "§"));
        }
    }
    
    // 新增方法：执行命令并返回结果
    public CommandResult handleCommandWithResult(String string) {
        List<String> params = Arrays.asList(string.substring(1).trim().split("\\s+"));
        ArrayList<String> arrayList = new ArrayList<>(params);
        
        if (params.get(0).isEmpty()) {
            return new CommandResult(false, "Unknown powershell", "Empty command");
        }
        
        String command = params.get(0).toLowerCase();
        
        // 预验证命令参数
        CommandResult validation = validateCommandUsage(command, arrayList);
        if (!validation.success) {
            return validation;
        }
        
        // 查找匹配的命令
        for (PowerShell powerShell : NightSky.handler.powerShells) {
            for (String name : powerShell.names) {
                if (params.get(0).equalsIgnoreCase(name)) {
                    try {
                        // 特殊命令处理
                        CommandResult specialResult = handleSpecialCommands(command, arrayList, powerShell);
                        if (specialResult != null) {
                            return specialResult;
                        }
                        
                        // 执行命令
                        powerShell.runCommand(arrayList);
                        // 生成成功消息
                        String successMessage = generateSuccessMessage(params.get(0), arrayList);
                        return new CommandResult(true, "Command Success", successMessage);
                    } catch (Exception e) {
                        return new CommandResult(false, "Command Failed", e.getMessage());
                    }
                }
            }
        }
        
        return new CommandResult(false, "Unknown Command", "Unknown powershell: " + params.get(0) + "\nType .help for available commands");
    }
    
    private CommandResult validateCommandUsage(String command, ArrayList<String> args) {
        switch (command) {
            case "toggle":
                if (args.size() < 2) {
                    return new CommandResult(false, "Invalid Usage", 
                        "Usage: .toggle <module> [on/off]\nToggles the specified module on/off");
                }
                // 检查模块是否存在
                Module module = NightSky.moduleManager.getModule(args.get(1));
                if (module == null) {
                    return new CommandResult(false, "Module Not Found", 
                        "Module '" + args.get(1) + "' not found\nUse .help to see available commands");
                }
                break;
                
            case "config":
                if (args.size() < 2) {
                    return new CommandResult(false, "Invalid Usage", 
                        "Usage: .config <load/save/list> [name]\nManage configuration files");
                }
                break;
                
            case "friend":
                if (args.size() < 2) {
                    return new CommandResult(false, "Invalid Usage", 
                        "Usage: .friend <add/remove/list> [name]\nManage friend list");
                }
                break;
                
            case "target":
                if (args.size() < 2) {
                    return new CommandResult(false, "Invalid Usage", 
                        "Usage: .target <add/remove/clear> [name]\nManage target list");
                }
                break;
                
            case "vclip":
            case "verticalclip":
                if (args.size() < 2) {
                    return new CommandResult(false, "Invalid Usage", 
                        "Usage: .vclip <distance>\nVertically teleport by specified distance");
                }
                break;
        }
        
        return new CommandResult(true, "", ""); // 验证通过
    }
    
    private CommandResult handleSpecialCommands(String command, ArrayList<String> args, PowerShell powerShell) {
        switch (command) {
            case "help":
                // 执行help命令并获取输出内容
                powerShell.runCommand(args);
                return new CommandResult(true, "Help", generateHelpContent());
                
            case "config":
                if (args.size() >= 2 && args.get(1).equalsIgnoreCase("list")) {
                    // 不执行原命令，直接生成配置列表内容
                    return new CommandResult(true, "Configuration List", generateConfigListContent());
                }
                break;
                
            case "bind":
                if (args.size() >= 2 && (args.get(1).equalsIgnoreCase("list") || args.get(1).equalsIgnoreCase("l"))) {
                    // 不执行原命令，直接生成绑定列表内容
                    return new CommandResult(true, "Bind List", generateBindListContent());
                }
                break;
                
            case "binds":
                // 不执行原命令，直接生成绑定列表内容
                return new CommandResult(true, "Bind List", generateBindListContent());
        }
        
        return null; // 不是特殊命令，继续正常处理
    }
    
    private String generateConfigListContent() {
        try {
            java.io.FileFilter fileFilter = new org.apache.commons.io.filefilter.WildcardFileFilter("*.json", org.apache.commons.io.IOCase.INSENSITIVE);
            java.io.File[] configs = new java.io.File("./NightSky/").listFiles(fileFilter);
            
            if (configs == null || configs.length == 0) {
                return "No configurations found\nCreate configs using .config save <name>";
            }
            
            java.util.Arrays.sort(configs, org.apache.commons.io.comparator.LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            
            StringBuilder content = new StringBuilder();
            content.append("Available Configurations:\n\n");
            
            for (java.io.File file : configs) {
                String configName = org.apache.commons.io.FilenameUtils.removeExtension(file.getName());
                content.append("• ").append(configName).append("\n");
            }
            
            content.append("\nUse .config load <name> to load");
            return content.toString();
            
        } catch (Exception e) {
            return "Failed to read configuration directory\nPath: ./NightSky/";
        }
    }
    
    private String generateBindListContent() {
        java.util.List<nightsky.module.Module> boundModules = new java.util.ArrayList<>();
        
        for (nightsky.module.Module module : NightSky.moduleManager.modules.values()) {
            if (module.getKey() != 0) {
                boundModules.add(module);
            }
        }
        
        if (boundModules.isEmpty()) {
            return "No key bindings found\nUse .bind <module> to create bindings";
        }
        
        StringBuilder content = new StringBuilder();
        content.append("Current Key Bindings:\n\n");
        
        for (nightsky.module.Module module : boundModules) {
            String keyName = nightsky.util.KeyBindUtil.getKeyName(module.getKey());
            content.append("• ").append(module.getName()).append(" → ").append(keyName).append("\n");
        }
        
        content.append("\nUse .bind <module> to change bindings");
        return content.toString();
    }
    
    private String generateHelpContent() {
        StringBuilder helpContent = new StringBuilder();
        helpContent.append("Available Commands:\n\n");
        
        // 添加主要命令
        helpContent.append("Core Commands:\n");
        helpContent.append(".bind <module> - Bind key to module\n");
        helpContent.append(".bind list - Show current bindings\n");
        helpContent.append(".toggle <module> - Toggle module on/off\n");
        helpContent.append(".config <load/save/list> [name] - Manage configs\n");
        helpContent.append(".help - Show this help\n\n");
        
        // 添加其他常用命令
        helpContent.append("Other Commands:\n");
        helpContent.append(".friend <add/remove/list> [name] - Manage friends\n");
        helpContent.append(".target <add/remove/clear> [name] - Manage targets\n");
        helpContent.append(".vclip <distance> - Vertical clip\n");
        helpContent.append(".player <command> - Player operations\n");
        helpContent.append(".item <command> - Item operations\n");
        helpContent.append(".show/.hide <module> - Show/hide modules\n");
        
        return helpContent.toString();
    }
    
    private String generateSuccessMessage(String command, ArrayList<String> args) {
        String cmd = command.toLowerCase();
        
        switch (cmd) {
            case "help":
                return "Available commands displayed";
            case "bind":
                if (args.size() >= 2) {
                    return "Key binding for module: " + args.get(1);
                }
                return "Bind command executed";
            case "toggle":
                if (args.size() >= 2) {
                    return "Toggled module: " + args.get(1);
                }
                return "Toggle command executed";
            case "config":
                return "Configuration command executed";
            case "friend":
                return "Friend command executed";
            case "target":
                return "Target command executed";
            case "player":
                return "Player command executed";
            case "name":
                return "Name command executed";
            case "item":
                return "Item command executed";
            case "list":
                return "List command executed";
            case "show":
                return "Show command executed";
            case "hide":
                return "Hide command executed";
            case "denick":
                return "Denick command executed";
            case "devweb":
                return "DevWeb command executed";
            case "binds":
                return "Binds list displayed";
            case "verticalclip":
            case "vclip":
                if (args.size() >= 2) {
                    return "Vertical clip: " + args.get(1) + " blocks";
                }
                return "Vertical clip executed";
            case "module":
                return "Module command executed";
            default:
                return "Command '" + cmd + "' executed successfully";
        }
    }
    
    // 命令结果类
    public static class CommandResult {
        public final boolean success;
        public final String title;
        public final String message;
        
        public CommandResult(boolean success, String title, String message) {
            this.success = success;
            this.title = title;
            this.message = message;
        }
    }

    public boolean isTypingCommand(String string) {
        if (string == null || string.length() < 2) {
            return false;
        } else {
            return string.charAt(0) == '.' && Character.isLetterOrDigit(string.charAt(1));
        }
    }

    @EventTarget(Priority.HIGHEST)
    public void onPacket(PacketEvent event) {
        if (event.getType() == EventType.SEND && event.getPacket() instanceof C01PacketChatMessage) {
            String msg = ((C01PacketChatMessage) event.getPacket()).getMessage();
            if (this.isTypingCommand(msg)) {
                event.setCancelled(true);
                this.handleCommand(msg);
            }
        }
    }
}
