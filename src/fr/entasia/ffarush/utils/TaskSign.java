package fr.entasia.ffarush.utils;

import fr.entasia.ffarush.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TaskSign extends BukkitRunnable {
    @Override
    public void run() {
        Main.sqlConnection.checkConnect();


        ConfigurationSection csKill = Main.main.getConfig().getConfigurationSection("hallofFame.kill");
        ConfigurationSection csDeath = Main.main.getConfig().getConfigurationSection("hallofFame.death");
        ConfigurationSection csRatio = Main.main.getConfig().getConfigurationSection("hallofFame.ratio");
        try {
            PreparedStatement ps= Main.sqlConnection.connection.prepareStatement("SELECT global.name, entagames.ffa_kills FROM global INNER JOIN entagames ON global.uuid = entagames.uuid ORDER BY entagames.ffa_kills DESC LIMIT 3");
            PreparedStatement ps2= Main.sqlConnection.connection.prepareStatement("SELECT global.name, entagames.ffa_deaths FROM global INNER JOIN entagames ON global.uuid = entagames.uuid ORDER BY entagames.ffa_deaths DESC LIMIT 3");
            PreparedStatement ps3= Main.sqlConnection.connection.prepareStatement("SELECT global.name, entagames.ffa_kills/entagames.ffa_deaths FROM global INNER JOIN entagames ON global.uuid = entagames.uuid ORDER BY entagames.ffa_kills/entagames.ffa_deaths DESC LIMIT 3");
            ResultSet rs = ps.executeQuery();
            int i=1;
            while(rs.next()){
                double x=csKill.getDouble(i+".x");
                double y=csKill.getDouble(i+".y");
                double z=csKill.getDouble(i+".z");
                Location loc = new Location(Bukkit.getWorld(Main.main.getConfig().getString("world")),x,y,z);
                Sign sign = (Sign) loc.getBlock().getState();
                String line1;
                switch(i){
                    case 1:
                        line1="§1§eTop 1";
                        break;
                    case 2:
                        line1="§1§6Top 2";
                        break;
                    case 3:
                        line1="§1§4Top 3";
                        break;
                    default: line1="§4Erreur";
                }
                sign.setLine(0,line1);
                sign.setLine(1,"§1§7"+rs.getString(1));
                sign.setLine(2,"§1§7"+rs.getInt(2)+" kills");
                sign.update(true);

                i++;
            }
            rs = ps2.executeQuery();
            i=1;
            while(rs.next()){

                double x=csDeath.getDouble(i+".x");
                double y=csDeath.getDouble(i+".y");
                double z=csDeath.getDouble(i+".z");
                Location loc = new Location(Bukkit.getWorld(Main.main.getConfig().getString("world")),x,y,z);
                Sign sign = (Sign) loc.getBlock().getState();
                String line1;
                switch(i){
                    case 1:
                        line1="§1§eTop 1";
                        break;
                    case 2:
                        line1="§1§6Top 2";
                        break;
                    case 3:
                        line1="§1§4Top 3";
                        break;
                    default: line1="§4Erreur";
                }
                sign.setLine(0,line1);
                sign.setLine(1,"§1§7"+rs.getString(1));
                sign.setLine(2,"§1§7"+rs.getInt(2)+" morts");
                i++;
                sign.update(true);
            }
            rs = ps3.executeQuery();
            i=1;
            while(rs.next()){

                double x=csRatio.getDouble(i+".x");
                double y=csRatio.getDouble(i+".y");
                double z=csRatio.getDouble(i+".z");
                Location loc = new Location(Bukkit.getWorld(Main.main.getConfig().getString("world")),x,y,z);
                Sign sign = (Sign) loc.getBlock().getState();
                String line1;
                switch(i){
                    case 1:
                        line1="§1§eTop 1";
                        break;
                    case 2:
                        line1="§1§6Top 2";
                        break;
                    case 3:
                        line1="§1§4Top 3";
                        break;
                    default: line1="§4Erreur";
                }
                sign.setLine(0,line1);
                sign.setLine(1,"§1§7"+rs.getString(1));
                sign.setLine(2,"§1§7"+rs.getInt(2)+" de ratio");
                i++;
                sign.update(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
