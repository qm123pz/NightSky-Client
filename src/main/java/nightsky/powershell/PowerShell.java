package nightsky.powershell;

import java.util.ArrayList;

public abstract class PowerShell {
    public final ArrayList<String> names;

    public PowerShell(ArrayList<String> arrayList) {
        this.names = arrayList;
    }

    public abstract void runCommand(ArrayList<String> args);
}
