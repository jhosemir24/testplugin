package com.tuplugin.multas;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class CommandMulta implements CommandExecutor {
    private final MultasPlugin plugin;
    private final Database db;
    private final GUIManager gui;

    public CommandMulta(MultasPlugin plugin, Database db, GUIManager gui){
        this.plugin = plugin; this.db = db; this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage("Solo jugadores"); return true; }
        Player p = (Player) sender;

        if (args.length == 0){
            gui.openMainGUI(p, 0, "");
            return true;
        }

        if (args[0].equalsIgnoreCase("ver") && args.length >= 2){
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) { p.sendMessage("Jugador no encontrado"); return true; }
            try { gui.openPlayerGUI(p, target.getUniqueId(), 0, ""); } catch (Exception e) { e.printStackTrace(); }
            return true;
        }

        if (args.length < 3){ p.sendMessage("Uso: /multa <jugador> <dinero> <razon>"); return true; }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null){ p.sendMessage("Jugador no encontrado"); return true; }

        double amount;
        try { amount = Double.parseDouble(args[1]); } catch (NumberFormatException e){ p.sendMessage("Cantidad invalida"); return true; }

        String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));

        try{
            int id = db.createMulta(target.getUniqueId(), p.getName(), amount, reason, "CUSTOM", "MEDIA");
            p.sendMessage("Multa creada (id: " + id + ") para " + target.getName());
            target.sendMessage("Has recibido una multa de " + amount + " por: " + reason);
        } catch (SQLException ex) { ex.printStackTrace(); p.sendMessage("Error al crear la multa"); }
        return true;
    }
}
