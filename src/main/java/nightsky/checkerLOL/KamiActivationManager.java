package nightsky.checkerLOL;

import java.util.concurrent.CompletableFuture;

public class KamiActivationManager {
    private static KamiActivationManager instance;
    private final SecureClient secureClient;
    private String lastActivationError = "";

    private KamiActivationManager() {
        this.secureClient = new SecureClient("appid", "key");
    }

    public static KamiActivationManager getInstance() {
        if (instance == null) {
            instance = new KamiActivationManager();
        }
        return instance;
    }

    public CompletableFuture<Boolean> activateWithLoginAsync(String username, String password, String kamiCode) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean activationSuccess = secureClient.directActivate(username, password, kamiCode);

                if (activationSuccess) {
                    lastActivationError = "";
                    return true;
                } else {
                    lastActivationError = "Key activation failed";
                    return false;
                }
            } catch (Exception e) {
                lastActivationError = "Activation failed: " + e.getMessage();
                return false;
            }
        });
    }

    public boolean activateWithLogin(String username, String password, String kamiCode) {
        try {
            boolean activationSuccess = secureClient.directActivate(username, password, kamiCode);

            if (activationSuccess) {
                lastActivationError = "";
                return true;
            } else {
                lastActivationError = "Key activation failed";
                return false;
            }
        } catch (Exception e) {
            lastActivationError = "Activation failed: " + e.getMessage();
            return false;
        }
    }

    public CompletableFuture<Boolean> activateAsync(String username, String password, String kamiCode) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean activationSuccess = secureClient.directActivate(username, password, kamiCode);
                if (activationSuccess) {
                    lastActivationError = "";
                    return true;
                } else {
                    lastActivationError = "Key activation failed";
                    return false;
                }
            } catch (Exception e) {
                lastActivationError = "Activation request error: " + e.getMessage();
                return false;
            }
        });
    }

    public boolean activate(String username, String password, String kamiCode) {
        try {
            boolean activationSuccess = secureClient.directActivate(username, password, kamiCode);
            if (activationSuccess) {
                lastActivationError = "";
                return true;
            } else {
                lastActivationError = "Key activation failed";
                return false;
            }
        } catch (Exception e) {
            lastActivationError = "Activation request error: " + e.getMessage();
            return false;
        }
    }

    public String getLastActivationError() {
        return lastActivationError;
    }

    public void clearError() {
        lastActivationError = "";
    }
}
