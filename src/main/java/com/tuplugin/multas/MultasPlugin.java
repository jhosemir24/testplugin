package com.tuplugin.multas;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class MultasPlugin extends JavaPlugin {
    private static Economy econ;
    private Database db;
    private GUIManager gui;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (!setupEconomy()) {
            getLogger().warning("Vault no encontrado o no registrado. El plugin seguirá activo pero sin integración con economía.");
        }

        this.db = new Database(this);
        try { this.db.connect(); } catch (Exception e) { e.printStackTrace(); }

        this.gui = new GUIManager(this, db);

        getCommand("multa").setExecutor(new CommandMulta(this, db, gui));
        getServer().getPluginManager().registerEvents(new InventoryListener(this, db, gui), this);

        getLogger().info("MultasPlugin activado");
    }

    @Override
    public void onDisable() {
        db.disconnect();
        getLogger().info("MultasPlugin desactivado");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy(){ return econ; }
}
