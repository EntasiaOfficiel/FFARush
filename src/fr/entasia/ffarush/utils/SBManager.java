package fr.entasia.ffarush.utils;

import fr.entasia.egtools.utils.MoneyUtils;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class SBManager {

	public FFAPlayer ffp;
	public Scoreboard scoreboard;
	public Objective objective;
	public String ratio = "";
	public String ks = "";
	public String kills = "";
	public String deaths = "";
	public String money = "";

	public SBManager(FFAPlayer ffp){
		this.ffp = ffp;
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		objective = scoreboard.registerNewObjective("ffa", "dummy");
	}
	public void softSet(){
		if(ffp.p.getScoreboard()!=scoreboard)refresh();
	}

	public void clear(){
		scoreboard.getEntries().forEach(a -> scoreboard.resetScores(a));
	}

	public void refresh(){
		ffp.p.setScoreboard(scoreboard);
		clear();
		objective.setDisplayName("§cFFA§6Rush");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.getScore("§b§m-----------").setScore(50);
		refreshMoney();
		objective.getScore(" ").setScore(47);
		refreshKills();
		refreshDeaths();
		refreshRatio();
		objective.getScore("  ").setScore(43);
		refreshKs();
		objective.getScore("§b§m----------- ").setScore(41);
		objective.getScore("§bplay.enta§7sia.fr").setScore(10);
	}

	public void refreshMoney(){
		scoreboard.resetScores(money);
		int a = MoneyUtils.getMoney(ffp.p.getUniqueId());
		objective.getScore("§7Monnaie : §b"+a).setScore(48);
		money = "§7Monnaie : §b"+a;
	}

	public void refreshRatio(){
		scoreboard.resetScores(ratio);
		if(ffp.deaths==0) ratio = "§7Ratio : §bNon.";
		else{
			float r = Math.round((float)ffp.kills/ffp.deaths*100)/100f;
			ratio = Float.toString(r);
			if(r%1==0){
				ratio = ratio.substring(0, ratio.length()-2);
			}
			ratio = "§7Ratio : §b"+ratio;
		}
		objective.getScore(ratio).setScore(44);
	}

	public void refreshKs(){
		scoreboard.resetScores(ks);
		objective.getScore("§7KillStreak : §b"+ffp.ks).setScore(42);
		ks = "§7KillStreak : §b"+ffp.ks;
	}

	public void refreshKills(){
		scoreboard.resetScores(kills);
		objective.getScore("§7Kills : §b"+ffp.kills).setScore(45);
		kills = "§7Kills : §b"+ffp.kills;
	}

	public void refreshDeaths(){
		scoreboard.resetScores(deaths);
		objective.getScore("§7Morts : §b"+ffp.deaths).setScore(46);
		deaths = "§7Morts : §b"+ffp.deaths;
	}

}
