package fr.entasia.ffarush.commands;

import fr.entasia.ffarush.FFAUtils;
import fr.entasia.ffarush.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class FFARushPlCmd implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] arg){
		Player p = (Player) sender;
		if(p.hasPermission("plugin.ffarush")) {
			if (arg.length == 0){
				p.sendMessage("§cMet un argument !");
				showOptions(p);
			}else{
				if (arg[0].equalsIgnoreCase("setspawn")) {
					FFAUtils.spawn = p.getLocation().getBlock().getLocation();
					Main.main.getConfig().set("spawn", FFAUtils.spawn.getX() + ";" + FFAUtils.spawn.getY() + ";" + FFAUtils.spawn.getZ());
					Main.main.saveConfig();
					p.sendMessage("§bSpawn défini !");
				} else if (arg[0].equalsIgnoreCase("reload")) {
					Main.main.reloadConfig();
					Main.loadConfig();
					p.sendMessage("§bConfiguration rechargée !");
				} else if (arg[0].equalsIgnoreCase("listpoints")) {
					p.sendMessage("§6Liste des points : ");
					for (int i = 0; i < 12; i++) {
						p.sendMessage("§6" + (i + 1) + "§e- x:" + FFAUtils.spawnsloc[i].getX() + " y:" + FFAUtils.spawnsloc[i].getY() + " z:" + FFAUtils.spawnsloc[i].getZ());
					}
				} else if (arg[0].equalsIgnoreCase("setpoint")) {
					if (arg.length ==2) {
						try {
							int a = Integer.parseInt(arg[1]);
							if (a < 1 || a > 12)throw new NumberFormatException();
							Location b = p.getLocation().getBlock().getLocation();
							b.add(new Vector(0.5, 0.5, 0.5));
							b.setYaw(Math.round(b.getYaw()/90)*90);
							b.setPitch(0);
							FFAUtils.spawnsloc[a - 1] = b;
							List<String> c = Main.main.getConfig().getStringList("ffalocs");
							c.set(a - 1, b.getBlockX() + ";" + b.getBlockY() + ";" + b.getBlockZ() + ";" + b.getYaw());
							Main.main.getConfig().set("ffalocs", c);
							Main.main.saveConfig();
							p.sendMessage("§bPoint n°" + a + " §bde l'arène FFARush défini !");

						} catch (NumberFormatException e) {
							p.sendMessage("§cPoint invalide ! Met un point de 1 à 12 !");
						}
					} else p.sendMessage("§cMet un point de 1 à 12 !");

				} else if (arg[0].equalsIgnoreCase("clearblocks")) {
					FFAUtils.clearArena();
					p.sendMessage("§aSuccès");
				} else if (arg[0].equalsIgnoreCase("saveplayers")) {
					if(FFAUtils.saveAllUsers())p.sendMessage("§aSuccès");
					else p.sendMessage("§cEchec");
				} else if (arg[0].equalsIgnoreCase("damageticks")) {
					if (arg.length ==2) {
						if(arg[1].equals("default")){
							FFAUtils.damageticks = Main.main.getConfig().getInt("damageticks");
							for (Player lp : FFAUtils.world.getPlayers()) {
								lp.setMaximumNoDamageTicks(FFAUtils.damageticks);
							}
							p.sendMessage("§cTicks d'invincibilité réinitialisés !");
						}else{
							try {
								int t = Integer.parseInt(arg[1]);
								if (t < 1 || t > 40) throw new NumberFormatException();
								FFAUtils.damageticks = t;
								p.sendMessage("§cTicks d'invicibilité définis à " + t + " !");
								for (Player lp : FFAUtils.world.getPlayers()) {
									lp.setMaximumNoDamageTicks(t);
								}
							} catch (NumberFormatException e) {
								p.sendMessage("§cCe nombre est invalide ! Met un nombre entre 0 et 40 / default !");
							}
						}
					}else p.sendMessage("§cMet un nombre !");
				}else{
					p.sendMessage("§cCet argument est incorrect ! Arguments :");
					showOptions(p);
				}
			}
		}
		return true;
	}

	public static void showOptions(Player p){
		p.sendMessage("§c- setspawn");
		p.sendMessage("§c- reload");
		p.sendMessage("§c- listpoints");
		p.sendMessage("§c- setpoint");
		p.sendMessage("§c- clearblocks");
		p.sendMessage("§c- saveplayers");
		p.sendMessage("§c- damageticks");
	}
}