package fr.entasia.ffarush.listeners;

import fr.entasia.apis.other.ChatComponent;
import fr.entasia.apis.regionManager.api.RegionManager;
import fr.entasia.egtools.utils.MoneyUtils;
import fr.entasia.ffarush.FFAUtils;
import fr.entasia.ffarush.utils.FFAPlayer;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Date;

public class FightListeners implements Listener {

	@EventHandler
	public static void damage(EntityDamageEvent e) {
		if(e.getEntity().getWorld()!=FFAUtils.world)return;
		if(e.getEntity() instanceof Player){
			if(RegionManager.getRegionsAtLocation(e.getEntity().getLocation()).contains(FFAUtils.reg_spawn)) {
				e.setCancelled(true);
				return;
			}
			if(e.getCause()==EntityDamageEvent.DamageCause.ENTITY_ATTACK)return;
			if(e.getCause()==EntityDamageEvent.DamageCause.PROJECTILE)e.setCancelled(true);
			else if(e.getCause()==EntityDamageEvent.DamageCause.BLOCK_EXPLOSION||e.getCause()==EntityDamageEvent.DamageCause.ENTITY_EXPLOSION||
					e.getCause()== EntityDamageEvent.DamageCause.FALL){
				e.setDamage(0);
			}else{
				Player p = (Player)e.getEntity();
				if(e.getFinalDamage()<p.getHealth())return;
				e.setCancelled(true);
				p.sendMessage("§7Tu es mort !");
				kill(p);
			}
		}else e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public static void damage(EntityDamageByEntityEvent e) {
		if(e.getEntity().getWorld()!=FFAUtils.world)return;
		if(e.getEntity() instanceof Player){
			Player p = (Player)e.getEntity();
			if(e.getCause()==EntityDamageEvent.DamageCause.ENTITY_EXPLOSION||e.getCause()==EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)return;

			if(p.getInventory().getChestplate()!=null&&p.getInventory().getChestplate().getType()==Material.ELYTRA){
				p.getInventory().setChestplate(FFAUtils.ffaarmor[1]);
			}

			Player dam =null;
			if(e.getDamager() instanceof Player){
				dam = (Player) e.getDamager();
				FFAPlayer ffp = FFAUtils.playerCache.get(p.getUniqueId());
				ffp.lastDamager = dam;
				ffp.lastDamage = new Date().getTime();

				if(dam.getInventory().getChestplate()!=null&&dam.getInventory().getChestplate().getType()==Material.ELYTRA){
					dam.getInventory().setChestplate(FFAUtils.ffaarmor[1]);
					e.setCancelled(true);
					return;
				}
			}
			if(e.getFinalDamage()<p.getHealth()){
				if(dam!=null) dam.sendMessage(ChatMessageType.ACTION_BAR, ChatComponent.create("§4"+(int)(p.getHealth()-e.getFinalDamage())+"§6/§420 §c❤"));

			}else{
				e.setCancelled(true);
				kill(p);
			}
		}
	}


	public static void kill(Player p){
		kill(FFAUtils.playerCache.get(p.getUniqueId()));
	}

	public static void kill(FFAPlayer ffp){
		ffp.deaths++;
		ffp.ks = 0;
		if(ffp.deathParticle == null){
			ffp.p.getWorld().spawnParticle(Particle.LAVA, ffp.p.getLocation(), 50, 0.4, 0.7, 0.4, 0.08);
		} else{
			ffp.deathParticle.update(ffp.p.getLocation(),ffp);
		}

		FFAUtils.tpSpawnFFA(ffp.p, false);
		ffp.sb.refreshDeaths();
		ffp.sb.refreshRatio();
		ffp.sb.refreshKs();
		if(ffp.lastDamager!=null&&new Date().getTime()-ffp.lastDamage<15000){
			FFAPlayer damager = FFAUtils.playerCache.get(ffp.lastDamager.getUniqueId());
			if(damager!=null){
				damager.kills++;
				damager.ks++;
				MoneyUtils.addMoney(damager.p.getUniqueId(), (int)(Math.random()*1.8)+1);
				damager.sb.refreshKills();
				damager.sb.refreshRatio();
				damager.sb.refreshKs();
				damager.sb.refreshMoney();
				ffp.p.sendMessage("§7Tu as été tué par §c"+damager.p.getName()+" ! §9( §4"+(int)damager.p.getHealth()+"§6/§420 §c❤ restants §9)");
				ffp.p.sendMessage(ChatMessageType.ACTION_BAR, ChatComponent.create("§cTué par "+damager.p.getDisplayName()+" !"));
				damager.p.sendMessage("§7Tu as tué §c"+ffp.p.getName()+" ! §9( §4"+(int)damager.p.getHealth()+"§6/§420 §c❤ restants §9)");
				damager.p.sendMessage(ChatMessageType.ACTION_BAR, ChatComponent.create("§4"+ffp.p.getDisplayName()+" §ctué !"));
				damager.p.setHealth(20);

				ffp.lastDamage = 0;
				ffp.lastDamager = null;
				return;
			}
		}
		ffp.p.sendMessage("§7Tu es mort !");
	}
}
