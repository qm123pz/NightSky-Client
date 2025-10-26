package me.liuli.elixir.account;

import me.liuli.elixir.compat.Session;

public class CrackedAccount extends MinecraftAccount {
    private String name = "UNKNOWN";
    
    public CrackedAccount() {
        super("Cracked");
    }
    
    public CrackedAccount(String name) {
        super("Cracked");
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public Session getSession() {
        return new Session(name, "", "", "legacy");
    }
    
    @Override
    public void update() {
        // Nothing to update for cracked accounts
    }
}
