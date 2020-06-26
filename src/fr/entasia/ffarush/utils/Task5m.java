package fr.entasia.ffarush.utils;

import fr.entasia.apis.utils.ServerUtils;
import fr.entasia.ffarush.FFAUtils;
import org.bukkit.scheduler.BukkitRunnable;

public class Task5m extends BukkitRunnable {


	public void run(){
		FFAUtils.clearArena();
		if(!FFAUtils.saveAllUsers()) ServerUtils.permMsg("§4§lSEVERE §cErreur lors de la sauuvegarde SQL des joueurs en FFARush !", "staff.notify");
	}
}
