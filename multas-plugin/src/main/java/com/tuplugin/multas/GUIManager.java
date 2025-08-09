package com.tuplugin.multas;

import com.tuplugin.multas.model.Multa;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GUIManager {
    private final MultasPlugin plugin;
    private final Database db;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    public GUIManager(MultasPlugin plugin, Database db){ this.plugin = plugin; this.db = db; }

    public void openMainGUI(Player viewer, int page, String filterPlayer){
        Inventory inv = Bukkit.createInventory(null, 54, "Multas - Principal");
        viewer.openInventory(inv);
    }

    public void openPlayerGUI(Player viewer, UUID target, int page, String filterType) throws SQLException {
        List<Multa> multas = db.getMultasFor(target);
        Inventory inv = Bukkit.createInventory(null, 54, "Multas de " + getName(target));

        int slot = 0;
        for (Multa m : multas){
            if (slot >= 45) break;
            ItemStack it = new ItemStack(org.bukkit.Material.PAPER);
            ItemMeta meta = it.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "#" + m.getId() + " - " + (m.getReason().length()>20?m.getReason().substring(0,20)+"...":m.getReason()));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Fecha: " + fmt.format(m.getDate()));
            lore.add(ChatColor.GRAY + "Motivo: " + m.getReason());
            lore.add(ChatColor.GRAY + "Multador: " + m.getIssuer());
            lore.add(ChatColor.GRAY + "Dinero: $" + m.getAmount());
            lore.add(ChatColor.GRAY + "Estado: " + m.getState());
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(meta);
            inv.setItem(slot++, it);
        }

        viewer.openInventory(inv);
    }

    private String getName(UUID uuid){ OfflinePlayer p = Bukkit.getOfflinePlayer(uuid); return p.getName() == null ? uuid.toString() : p.getName(); }
}
