package nightsky.ui.mainmenu.altmanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.liuli.elixir.account.CrackedAccount;
import me.liuli.elixir.account.MicrosoftAccount;
import me.liuli.elixir.account.MinecraftAccount;
import me.liuli.elixir.account.MojangAccount;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AccountsConfig {
    private static AccountsConfig instance;
    private List<MinecraftAccount> accounts = new ArrayList<>();
    private File configFile;
    private Gson gson;

    private AccountsConfig() {
        configFile = new File("accounts.json");
        gson = new GsonBuilder()
            .registerTypeAdapter(MinecraftAccount.class, new AccountTypeAdapter())
            .setPrettyPrinting()
            .create();
        loadConfig();
    }

    public static AccountsConfig getInstance() {
        if (instance == null) {
            instance = new AccountsConfig();
        }
        return instance;
    }

    public List<MinecraftAccount> getAccounts() {
        return accounts;
    }

    public void addAccount(MinecraftAccount account) {
        if (!accountExists(account)) {
            accounts.add(account);
            saveConfig();
        }
    }

    public void addCrackedAccount(String name) {
        CrackedAccount account = new CrackedAccount();
        try {
            java.lang.reflect.Field nameField = CrackedAccount.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(account, name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        addAccount(account);
    }

    public void addMojangAccount(String email, String password) {
        MojangAccount account = new MojangAccount();
        try {
            java.lang.reflect.Field emailField = MojangAccount.class.getDeclaredField("email");
            emailField.setAccessible(true);
            emailField.set(account, email);
            
            java.lang.reflect.Field passwordField = MojangAccount.class.getDeclaredField("password");
            passwordField.setAccessible(true);
            passwordField.set(account, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        addAccount(account);
    }

    public void removeAccount(MinecraftAccount account) {
        accounts.remove(account);
        saveConfig();
    }

    public boolean accountExists(MinecraftAccount account) {
        return accounts.stream().anyMatch(acc -> acc.getName().equals(account.getName()));
    }

    public void saveConfig() {
        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(accounts, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            Type listType = new TypeToken<List<MinecraftAccount>>(){}.getType();
            List<MinecraftAccount> loadedAccounts = gson.fromJson(reader, listType);
            if (loadedAccounts != null) {
                accounts = loadedAccounts;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
