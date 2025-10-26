package me.liuli.elixir.account;

import me.liuli.elixir.compat.Session;

public abstract class MinecraftAccount {
    protected String type;
    
    public MinecraftAccount(String type) {
        this.type = type;
    }
    
    public abstract String getName();
    public abstract Session getSession();
    public abstract void update();
    
    public String getType() {
        return type;
    }
}
