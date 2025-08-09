package com.tuplugin.multas;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryListener implements Listener {
    private final MultasPlugin plugin;
    private final Database db;
    private final GUIManager gui;

    public InventoryListener(MultasPlugin plugin, Database db, GUIManager gui){ this.plugin = plugin; this.db = db; this.gui = gui; }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getView().getTitle() == null) return;
        String title = e.getView().getTitle();
        Player p = (Player) e.getWhoClicked();

        if (title.startsWith("Multas de ")){
            e.setCancelled(true);
            // Click handling: abrir detalle, pagar (click derecho/izquierdo), borrar (shift-click)
        }

        if (title.equals("Multas - Principal")){
            e.setCancelled(true);
            // manejo de filtro/paginación
        }
    }
}
