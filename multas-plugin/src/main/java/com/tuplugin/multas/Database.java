package com.tuplugin.multas;

import com.tuplugin.multas.model.Multa;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Database {
    private final JavaPlugin plugin;
    private Connection conn;

    public Database(JavaPlugin plugin){ this.plugin = plugin; }

    public void connect() throws SQLException {
        String path = plugin.getDataFolder().getAbsolutePath() + "/multas.db";
        conn = DriverManager.getConnection("jdbc:sqlite:" + path);
        try (Statement s = conn.createStatement()){
            s.executeUpdate("CREATE TABLE IF NOT EXISTS multas (id INTEGER PRIMARY KEY AUTOINCREMENT, target TEXT, issuer TEXT, amount REAL, reason TEXT, date TEXT, type TEXT, importance TEXT, state TEXT);");
        }
    }

    public void disconnect(){
        try { if (conn != null) conn.close(); } catch (SQLException e){ e.printStackTrace(); }
    }

    public int createMulta(UUID target, String issuer, double amount, String reason, String type, String importance) throws SQLException {
        String sql = "INSERT INTO multas (target, issuer, amount, reason, date, type, importance, state) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1, target.toString());
            ps.setString(2, issuer);
            ps.setDouble(3, amount);
            ps.setString(4, reason);
            ps.setString(5, Instant.now().toString());
            ps.setString(6, type);
            ps.setString(7, importance);
            ps.setString(8, "PENDIENTE");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()){ if (rs.next()) return rs.getInt(1); }
        }
        return -1;
    }

    public List<Multa> getMultasFor(UUID target) throws SQLException {
        List<Multa> out = new ArrayList<>();
        String sql = "SELECT * FROM multas WHERE target = ? ORDER BY date DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, target.toString());
            try (ResultSet rs = ps.executeQuery()){
                while (rs.next()){
                    Multa m = new Multa(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("target")),
                        rs.getString("issuer"),
                        rs.getDouble("amount"),
                        rs.getString("reason"),
                        Instant.parse(rs.getString("date")),
                        rs.getString("type"),
                        rs.getString("importance"),
                        rs.getString("state")
                    );
                    out.add(m);
                }
            }
        }
        return out;
    }

    public void updateState(int id, String state) throws SQLException{
        try (PreparedStatement ps = conn.prepareStatement("UPDATE multas SET state = ? WHERE id = ?")){
            ps.setString(1, state);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void deleteMulta(int id) throws SQLException{
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM multas WHERE id = ?")){
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
