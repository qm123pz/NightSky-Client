package nightsky.module.modules.misc;

import nightsky.module.Module;

public class AntiObfuscate extends Module {
    public AntiObfuscate() {
        super("AntiObfuscate", false, true);
    }

    public String stripObfuscated(String string) {
        return string.replaceAll("§k", "");
    }
}
