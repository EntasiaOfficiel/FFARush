package fr.entasia.ffarush.utils;

import fr.entasia.ffarush.deathParticle.DeathParticle;
import org.bukkit.entity.Player;

public class FFAPlayer {

	public Player p;
	public int kills = 0;
	public int deaths = 0;
	public int ks = 0;
	public byte blocks = 0;
	public byte block = 0;
	public DeathParticle deathParticle;

	public byte[] inv;
	public SBManager sb;

	public FFAPlayer(Player p){
		this.p = p;
	}

}
