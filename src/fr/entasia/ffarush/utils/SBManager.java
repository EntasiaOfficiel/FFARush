package fr.entasia.ffarush.utils;

import fr.entasia.apis.other.ScoreBoardHelper;
import fr.entasia.egtools.utils.MoneyUtils;

public class SBManager extends ScoreBoardHelper {

	public FFAPlayer ffp;

	public SBManager(FFAPlayer ffp){
		super(ffp.p, "ffa", "§cFFA§6Rush");
		this.ffp = ffp;
	}

	@Override
	public void setSlots(){
		staticLine(19, "§b§m-----------");
		refreshMoney();
		staticLine(17," ");
		refreshKills();
		refreshDeaths();
		refreshRatio();
		staticLine(13, "  ");
		refreshKs();
		staticLine(11, "§b§m----------- ");
	}

	public void refreshMoney(){
		int a = MoneyUtils.getMoney(ffp.p.getUniqueId());
		dynamicLine(18, "§7Monnaie : §b"+a);
	}

	public void refreshKills(){
		dynamicLine(16, "§7Kills : §b"+ffp.kills);
	}

	public void refreshDeaths(){
		dynamicLine(15,"§7Morts : §b"+ffp.deaths);
	}

	public void refreshRatio(){
		String ratio;
		if(ffp.deaths==0) ratio = "§7Ratio : §bNon.";
		else{
			float r = Math.round((float)ffp.kills/ffp.deaths*100)/100f;
			ratio = Float.toString(r);
			if(r%1==0) ratio = ratio.substring(0, ratio.length()-2);
			ratio = "§7Ratio : §b"+ratio;
		}
		dynamicLine(14, ratio);
	}

	public void refreshKs(){
		dynamicLine(12, "§7KillStreak : §b"+ffp.ks);
	}



}
