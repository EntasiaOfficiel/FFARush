package fr.entasia.ffarush.utils;

import org.bukkit.entity.Player;

import java.util.Date;

public class FFADamager {

	public Player damager;
	public long time;

	public FFADamager(Player d){
		damager = d;
		time = new Date().getTime();
	}

}
