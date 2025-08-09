package com.tuplugin.multas;

import com.tuplugin.multas.model.Multa;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

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

        if (title.startsWith("Multas de ")) {
            e.setCancelled(true);
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            ItemMeta meta = clicked.getItemMeta();
            String name = meta.getDisplayName();
            if (name == null) return;

            // assuming title "Multas de <name>"
            String targetName = title.substring("Multas de ".length());
            UUID targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();

            // If clicking a control item, ignore for now
            if (meta.getLore() == null || meta.getLore().isEmpty()) return;

            // find id from displayname pattern "#id - ..."
            if (name.startsWith("#")) {
                String after = name.substring(1);
                String[] parts = after.split(" - ");
                try {
                    int id = Integer.parseInt(parts[0]);
                    try {
                        Multa m = db.getMultasFor(targetUUID).stream().filter(x -> x.getId() == id).findFirst().orElse(null);
                        if (m == null) { p.sendMessage(ChatColor.RED + "No se encontró la multa"); return; }
                        // open detail GUI
                        p.openInventory(gui.createDetailGUI(m));
                    } catch (SQLException ex) { ex.printStackTrace(); p.sendMessage(ChatColor.RED + "Error al leer la multa"); }
                } catch (NumberFormatException ex) { /* ignore */ }
            }
        } else if (title.startsWith("Multa #")) {
            e.setCancelled(true);
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            ItemMeta meta = clicked.getItemMeta();
            String name = meta.getDisplayName();
            if (name == null) return;
            // title "Multa #ID"
            String idStr = title.substring("Multa #".length());
            int id;
            try { id = Integer.parseInt(idStr); } catch (NumberFormatException ex) { return; }

            if (name.contains("Pagar")) {
                // attempt to pay
                try {
                    Multa m = db.getMultaById(id);
                    if (m == null) { p.sendMessage(ChatColor.RED + "Multa no encontrada"); return; }
                    if (m.getState().equalsIgnoreCase("PAGADA")) { p.sendMessage(ChatColor.YELLOW + "Esta multa ya está pagada"); return; }
                    Economy econ = MultasPlugin.getEconomy();
                    if (econ == null) { p.sendMessage(ChatColor.RED + "Economía no disponible en el servidor (Vault)!"); return; }
                    double balance = econ.getBalance(p);
                    if (balance < m.getAmount()) { p.sendMessage(ChatColor.RED + "No tienes suficiente dinero. Faltan: $" + (m.getAmount() - balance)); return; }
                    // withdraw from payer, give to issuer if online
                    Economy.Response resp = econ.withdrawPlayer(p, m.getAmount());
                    if (!resp.transactionSuccess()) { p.sendMessage(ChatColor.RED + "Error al realizar la transacción: " + resp.errorMessage); return; }
                    if (Bukkit.getPlayer(m.getIssuer()) != null) { econ.depositPlayer(Bukkit.getPlayer(m.getIssuer()), m.getAmount()); }
                    db.updateState(id, "PAGADA");
                    p.sendMessage(ChatColor.GREEN + "Multa pagada con éxito: $" + m.getAmount());
                    p.closeInventory();
                } catch (SQLException ex) { ex.printStackTrace(); p.sendMessage(ChatColor.RED + "Error al procesar pago"); }
            } else if (name.contains("Archivar")) {
                try {
                    db.updateState(id, "ARCHIVADA"); p.sendMessage(ChatColor.GREEN + "Multa archivada"); p.closeInventory();
                } catch (SQLException ex) { ex.printStackTrace(); p.sendMessage(ChatColor.RED + "Error al archivar"); }
            } else if (name.contains("Eliminar")) {
                // require shift-click as confirmation
                if (!e.isShiftClick()) { p.sendMessage(ChatColor.YELLOW + "Haz shift-click para confirmar eliminación"); return; }
                try {
                    db.deleteMulta(id); p.sendMessage(ChatColor.GREEN + "Multa eliminada"); p.closeInventory();
                } catch (SQLException ex) { ex.printStackTrace(); p.sendMessage(ChatColor.RED + "Error al eliminar"); }
            }
        } else if (title.equals("Multas - Principal")) {
            e.setCancelled(true);
            // future implementation: open player search or create GUI
        }
    }

    // removed unused findMultaById

        // simple linear search across database results: not optimal but adequate for small sets
        // get all players? Instead query DB directly for id - implement quick query
        String sql = "SELECT * FROM multas WHERE id = ?";
        try (var conn = db.conn) {
            // access to connection is package-private; use Database methods instead.
        } catch (Exception ex) { /* ignore */ }
        // fallback: search all known players in server (offline players)
        for (var op : Bukkit.getOfflinePlayers()) {
            if (op == null || op.getUniqueId() == null) continue;
            var list = db.getMultasFor(op.getUniqueId());
            for (Multa m : list) if (m.getId() == id) return m;
        }
        return null;
    }
}
