package com.songoda.ultimatekits.database;

import com.songoda.core.database.DataManagerAbstract;
import com.songoda.core.database.DatabaseConnector;
import com.songoda.ultimatekits.UltimateKits;
import com.songoda.ultimatekits.kit.Kit;
import com.songoda.ultimatekits.kit.KitBlockData;
import com.songoda.ultimatekits.kit.KitType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DataManager extends DataManagerAbstract {

    public DataManager(DatabaseConnector databaseConnector, Plugin plugin) {
        super(databaseConnector, plugin);
    }

    public void bulkUpdateBlockData(Map<Location, KitBlockData> blockData) {
        try (Connection connection = this.databaseConnector.getConnection()) {
            String updateData = "UPDATE " + this.getTablePrefix() + "blockdata SET type = ?, kit = ?, holograms = ?, " +
                    "displayItems = ?, particles = ?, itemOverride = ? " +
                    "WHERE world = ? AND x = ? AND y = ? AND z = ?";
            PreparedStatement statement = connection.prepareStatement(updateData);
            for (KitBlockData data : blockData.values()) {
                if (data == null || data.getWorld() == null) continue;
                statement.setString(1, data.getType().toString());
                statement.setString(2, data.getKit().getKey());
                statement.setBoolean(3, data.showHologram());
                statement.setBoolean(4, data.isDisplayingItems());
                statement.setBoolean(5, data.hasParticles());
                statement.setBoolean(6, data.isItemOverride());
                statement.setString(7, data.getWorld().getName());
                statement.setInt(8, data.getX());
                statement.setInt(9, data.getY());
                statement.setInt(10, data.getZ());
                statement.addBatch();
            }

            statement.executeBatch();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateBlockData(KitBlockData blockData) {
        if (blockData.getWorld() == null) return;
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String updateData = "UPDATE " + this.getTablePrefix() + "blockdata SET type = ?, kit = ?, holograms = ?, " +
                        "displayItems = ?, particles = ?, itemOverride = ? " +
                        "WHERE world = ? AND x = ? AND y = ? AND z = ?";
                PreparedStatement statement = connection.prepareStatement(updateData);
                statement.setString(1, blockData.getType().toString());
                statement.setString(2, blockData.getKit().getKey());
                statement.setBoolean(3, blockData.showHologram());
                statement.setBoolean(4, blockData.isDisplayingItems());
                statement.setBoolean(5, blockData.hasParticles());
                statement.setBoolean(6, blockData.isItemOverride());
                statement.setString(7, blockData.getWorld().getName());
                statement.setInt(8, blockData.getX());
                statement.setInt(9, blockData.getY());
                statement.setInt(10, blockData.getZ());
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void createBlockData(KitBlockData blockData) {
        if (blockData.getWorld() == null) return;
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String createData = "INSERT INTO " + this.getTablePrefix() + "blockdata (" +
                        "type, kit, holograms, displayItems, particles, itemOverride, world, x, y, z)" +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(createData);
                statement.setString(1, blockData.getType().toString());
                statement.setString(2, blockData.getKit().getKey());
                statement.setBoolean(3, blockData.showHologram());
                statement.setBoolean(4, blockData.isDisplayingItems());
                statement.setBoolean(5, blockData.hasParticles());
                statement.setBoolean(6, blockData.isItemOverride());
                statement.setString(7, blockData.getWorld().getName());
                statement.setInt(8, blockData.getX());
                statement.setInt(9, blockData.getY());
                statement.setInt(10, blockData.getZ());
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void deleteBlockData(KitBlockData blockData) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String deleteData = "DELETE FROM " + this.getTablePrefix() + "blockdata WHERE world = ? " +
                        "AND x = ? AND y = ? AND z = ?";
                PreparedStatement statement = connection.prepareStatement(deleteData);
                statement.setString(1, blockData.getWorld().getName());
                statement.setInt(2, blockData.getX());
                statement.setInt(3, blockData.getY());
                statement.setInt(4, blockData.getZ());
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void getBlockData(Consumer<Map<Location, KitBlockData>> callback) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String selectData = "SELECT * FROM " + this.getTablePrefix() + "blockdata";
                Map<Location, KitBlockData> blockData = new HashMap<>();
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(selectData);
                while (result.next()) {

                    World world = Bukkit.getWorld(result.getString("world"));
                    if (world == null) continue;

                    Kit kit = UltimateKits.getInstance().getKitManager().getKit(result.getString("kit"));
                    KitType type = KitType.getKitType(result.getString("type"));
                    if (kit == null || type == null) continue;

                    boolean holograms = result.getBoolean("holograms");
                    boolean displayItems = result.getBoolean("displayItems");
                    boolean particles = result.getBoolean("particles");
                    boolean itemOverride = result.getBoolean("itemOverride");
                    int x = result.getInt("x");
                    int y = result.getInt("y");
                    int z = result.getInt("z");
                    Location location = new Location(world, x, y, z);

                    blockData.put(location, new KitBlockData(kit, location, type, holograms, particles, displayItems, itemOverride));
                }

                this.sync(() -> callback.accept(blockData));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}
