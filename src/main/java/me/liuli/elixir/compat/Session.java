package me.liuli.elixir.compat;

public class Session {
    private String username;
    private String uuid;
    private String token;
    private String type;
    
    public Session(String username, String uuid, String token, String type) {
        this.username = username;
        this.uuid = uuid;
        this.token = token;
        this.type = type;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getUuid() {
        return uuid;
    }
    
    public String getToken() {
        return token;
    }
    
    public String getType() {
        return type;
    }
}
