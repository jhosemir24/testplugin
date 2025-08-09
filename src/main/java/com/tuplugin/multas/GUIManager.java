package com.tuplugin.multas;

import com.tuplugin.multas.model.Multa;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

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
        // control slots (bottom row)
        inv.setItem(49, makeControlItem(Material.PAPER, ChatColor.GREEN + "Crear multa", List.of(ChatColor.GRAY + "Usa /multa <jugador> <dinero> <razon>")));
        inv.setItem(45, makeControlItem(Material.FILLED_MAP, ChatColor.YELLOW + "Filtrar jugador", List.of(ChatColor.GRAY + "Click para filtrar (no implementado)")));
        viewer.openInventory(inv);
    }

    public void openPlayerGUI(Player viewer, UUID target, int page, String filterType) throws SQLException {
        List<Multa> multas = db.getMultasFor(target);
        Inventory inv = Bukkit.createInventory(null, 54, "Multas de " + getName(target));

        int slot = 0;
        for (Multa m : multas){
            if (slot >= 45) break;
            ItemStack it = createMultaItem(m);
            inv.setItem(slot++, it);
        }

        // bottom controls
        inv.setItem(45, makeControlItem(Material.ARROW, ChatColor.GRAY + "Página anterior", List.of()));
        inv.setItem(53, makeControlItem(Material.ARROW, ChatColor.GRAY + "Página siguiente", List.of()));
        inv.setItem(49, makeControlItem(Material.BOOK, ChatColor.AQUA + "Presets", List.of(ChatColor.GRAY + "Usa /multa preset <nombre>")));
        viewer.openInventory(inv);
    }

    public Inventory createDetailGUI(Multa m){
        Inventory inv = Bukkit.createInventory(null, 27, "Multa #" + m.getId());
        ItemStack main = createMultaItem(m);
        inv.setItem(13, main);

        inv.setItem(11, makeControlItem(Material.GOLD_INGOT, ChatColor.GREEN + "Pagar", List.of(ChatColor.GRAY + "Click izquierdo para pagar")));
        inv.setItem(15, makeControlItem(Material.ENDER_PEARL, ChatColor.YELLOW + "Archivar", List.of(ChatColor.GRAY + "Archiva la multa")));
        inv.setItem(26, makeControlItem(Material.BARRIER, ChatColor.RED + "Eliminar", List.of(ChatColor.GRAY + "Shift-Click para eliminar")));

        return inv;
    }

    private ItemStack createMultaItem(Multa m){
        ItemStack it = new ItemStack(Material.PAPER);
        ItemMeta meta = it.getItemMeta();
        String title = ChatColor.YELLOW + "#" + m.getId() + " - " + (m.getReason().length()>20?m.getReason().substring(0,20)+"...":m.getReason());
        meta.setDisplayName(title);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Fecha: " + fmt.format(m.getDate()));
        lore.add(ChatColor.GRAY + "Motivo: " + m.getReason());
        lore.add(ChatColor.GRAY + "Multador: " + m.getIssuer());
        lore.add(ChatColor.GRAY + "Dinero: $" + m.getAmount());
        lore.add(ChatColor.GRAY + "Estado: " + m.getState());
        lore.add(ChatColor.GRAY + "Importancia: " + (m.getImportance()==null?"MEDIA":m.getImportance()));
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(meta);
        return it;
    }

    private ItemStack makeControlItem(Material mat, String name, List<String> lore){
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        List<String> l = new ArrayList<>(lore);
        meta.setLore(l);
        it.setItemMeta(meta);
        return it;
    }

    private String getName(UUID uuid){ OfflinePlayer p = Bukkit.getOfflinePlayer(uuid); return p.getName() == null ? uuid.toString() : p.getName(); }
}
