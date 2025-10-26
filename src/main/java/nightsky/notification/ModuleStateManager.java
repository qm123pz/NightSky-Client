package nightsky.notification;

import java.util.HashMap;
import java.util.Map;

public class ModuleStateManager {
    private static ModuleStateManager instance;
    private final Map<String, Boolean> moduleStates = new HashMap<>();
    
    private ModuleStateManager() {}
    
    public static ModuleStateManager getInstance() {
        if (instance == null) {
            instance = new ModuleStateManager();
        }
        return instance;
    }
    
    public void setModuleState(String moduleName, boolean enabled) {
        moduleStates.put(moduleName, enabled);
        
        if ("Scaffold".equalsIgnoreCase(moduleName)) {
            ScaffoldData scaffoldData = ScaffoldData.getInstance();
            if (enabled) {
                scaffoldData.setActive(true);
                scaffoldData.setBlocksLeft(64);
                scaffoldData.setBlocksPerSecond(0.0f);
            } else {
                scaffoldData.setActive(false);
            }
        } else if ("BedNuker".equalsIgnoreCase(moduleName)) {
            BedNukerData bedNukerData = BedNukerData.getInstance();
            if (!enabled) {
                bedNukerData.reset();
            }
        }
    }
    
    public boolean isModuleEnabled(String moduleName) {
        return moduleStates.getOrDefault(moduleName, false);
    }
    
    public boolean isScaffoldActive() {
        return isModuleEnabled("Scaffold");
    }
    
    public boolean isBedNukerActive() {
        return isModuleEnabled("BedNuker");
    }
}
