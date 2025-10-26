package nightsky.powershell.shells;

import nightsky.NightSky;
import nightsky.powershell.PowerShell;
import nightsky.util.ChatUtil;
import nightsky.value.Value;
import nightsky.value.values.BooleanValue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Module extends PowerShell {
    public Module() {
        super(new ArrayList<>(NightSky.moduleManager.modules.values().stream().<String>map(nightsky.module.Module::getName).collect(Collectors.<String>toList())));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        nightsky.module.Module module = NightSky.moduleManager.getModule(args.get(0));
        if (args.size() >= 2) {
            Value<?> value = NightSky.valueHandler.getProperty(module, args.get(1));
            if (value == null) {
                ChatUtil.sendFormatted(String.format("%s%s has no value &o%s&r", NightSky.clientName, module.getName(), args.get(1)));
            } else if (args.size() < 3 && !(value instanceof BooleanValue)) {
                ChatUtil.sendFormatted(
                        String.format(
                                "%s%s: &o%s&r is set to %s&r (%s)&r",
                                NightSky.clientName,
                                module.getName(),
                                value.getName(),
                                value.formatValue(),
                                value.getValuePrompt()
                        )
                );
            } else {
                String newValue = args.size() < 3 ? null : String.join(" ", args.subList(2, args.size()));
                try {
                    if (value.parseString(newValue)) {
                        ChatUtil.sendFormatted(
                                String.format("%s%s: &o%s&r has been set to %s&r", NightSky.clientName, module.getName(), value.getName(), value.formatValue())
                        );
                        return;
                    }
                } catch (Exception e) {
                }
                ChatUtil.sendFormatted(
                        String.format("%sInvalid value for value &o%s&r (%s)&r", NightSky.clientName, value.getName(), value.getValuePrompt())
                );
            }
        } else {
            List<Value<?>> properties = NightSky.valueHandler.properties.get(module.getClass());
            if (properties != null) {
                List<Value<?>> visible = properties.stream().filter(Value::isVisible).collect(Collectors.toList());
                if (!visible.isEmpty()) {
                    ChatUtil.sendFormatted(String.format("%s%s:&r", NightSky.clientName, module.formatModule()));
                    for (Value<?> value : visible) {
                        ChatUtil.sendFormatted(String.format("&7»&r %s: %s&r", value.getName(), value.formatValue()));
                    }
                    return;
                }
            }
            ChatUtil.sendFormatted(String.format("%s%s has no values&r", NightSky.clientName, module.formatModule()));
        }
    }
}
