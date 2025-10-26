package me.liuli.elixir.account;

import me.liuli.elixir.compat.Session;

public class MojangAccount extends MinecraftAccount {
    private String email = "";
    private String password = "";
    private String name = "UNKNOWN";
    
    public MojangAccount() {
        super("Mojang");
    }
    
    public MojangAccount(String email, String password) {
        super("Mojang");
        this.email = email;
        this.password = password;
    }
    
    @Override
    public String getName() {
        return name.isEmpty() ? email : name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    @Override
    public Session getSession() {
        return new Session(getName(), "", "", "mojang");
    }
    
    @Override
    public void update() {
        // Simplified - in real implementation would authenticate with Mojang
    }
}
