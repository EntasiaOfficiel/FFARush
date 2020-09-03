package fr.entasia.ffarush.commands;

import fr.entasia.ffarush.FFAUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FFARushCmd implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] arg){
		Player p = (Player) sender;
		if (arg.length == 0) p.sendMessage("§cMet un argument !");
		else{
			if (arg[0].equalsIgnoreCase("join")) {
				FFAUtils.tpSpawnFFA(p, true);
			}else if (arg[0].equalsIgnoreCase("start")) {
				FFAUtils.joinFFA(p);
			}else{
				p.sendMessage("§cCet argument est inccorect");
			}
		}
		return true;
	}
}