package nightsky.module;

public enum ModuleCategory {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    RENDER("Render"),
    MISC("Misc");

    public final String name;

    ModuleCategory(String name) {
        this.name = name;
    }
}