package nightsky.ui.mainmenu.altmanager;

import com.google.gson.*;
import me.liuli.elixir.account.CrackedAccount;
import me.liuli.elixir.account.MicrosoftAccount;
import me.liuli.elixir.account.MinecraftAccount;
import me.liuli.elixir.account.MojangAccount;

import java.lang.reflect.Type;

public class AccountTypeAdapter implements JsonSerializer<MinecraftAccount>, JsonDeserializer<MinecraftAccount> {

    @Override
    public JsonElement serialize(MinecraftAccount src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        
        if (src instanceof CrackedAccount) {
            jsonObject.addProperty("type", "cracked");
            jsonObject.addProperty("name", src.getName());
        } else if (src instanceof MojangAccount) {
            MojangAccount mojang = (MojangAccount) src;
            jsonObject.addProperty("type", "mojang");
            jsonObject.addProperty("email", mojang.getEmail());
            jsonObject.addProperty("password", mojang.getPassword());
        } else if (src instanceof MicrosoftAccount) {
            MicrosoftAccount microsoft = (MicrosoftAccount) src;
            jsonObject.addProperty("type", "microsoft");
            jsonObject.addProperty("name", microsoft.getName());
            if (microsoft.getSession() != null) {
                jsonObject.addProperty("token", microsoft.getSession().getToken());
                jsonObject.addProperty("uuid", microsoft.getSession().getUuid());
            }
        }
        
        return jsonObject;
    }

    @Override
    public MinecraftAccount deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("type").getAsString();
        
        switch (type) {
            case "cracked":
                CrackedAccount crackedAccount = new CrackedAccount();
                try {
                    java.lang.reflect.Field nameField = CrackedAccount.class.getDeclaredField("name");
                    nameField.setAccessible(true);
                    nameField.set(crackedAccount, jsonObject.get("name").getAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return crackedAccount;
            case "mojang":
                MojangAccount mojangAccount = new MojangAccount();
                try {
                    java.lang.reflect.Field emailField = MojangAccount.class.getDeclaredField("email");
                    emailField.setAccessible(true);
                    emailField.set(mojangAccount, jsonObject.get("email").getAsString());
                    
                    java.lang.reflect.Field passwordField = MojangAccount.class.getDeclaredField("password");
                    passwordField.setAccessible(true);
                    passwordField.set(mojangAccount, jsonObject.get("password").getAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return mojangAccount;
            case "microsoft":
                String name = jsonObject.get("name").getAsString();
                MicrosoftAccount account = new MicrosoftAccount();
                try {
                    java.lang.reflect.Field nameField = MicrosoftAccount.class.getDeclaredField("name");
                    nameField.setAccessible(true);
                    nameField.set(account, name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return account;
            default:
                throw new JsonParseException("Unknown account type: " + type);
        }
    }
}
