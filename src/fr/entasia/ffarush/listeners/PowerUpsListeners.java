package fr.entasia.ffarush.listeners;

import fr.entasia.apis.regionManager.api.RegionManager;
import fr.entasia.ffarush.FFAUtils;
import fr.entasia.ffarush.Main;
import fr.entasia.ffarush.utils.FFAPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

public class PowerUpsListeners implements Listener {

	@EventHandler
	public void aaa(EntityShootBowEvent e) {
		if (e.getEntity().getWorld() != FFAUtils.world) return;

		if(RegionManager.getRegionsAtLocation(e.getEntity().getLocation()).contains(FFAUtils.reg_arena)&&e.getEntityType()== EntityType.PLAYER&&
			e.getBow().hasItemMeta()&&e.getBow().getItemMeta().hasDisplayName()&&e.getBow().getItemMeta().getDisplayName().equals("§7BlockBow")){
				FFAPlayer ffp = FFAUtils.playerCache.get(e.getEntity().getUniqueId());
				if(ffp==null)return;

				ItemStack bl = FFAUtils.ffablocks[ffp.block];
				byte b = (byte)bl.getDurability();

				Random r = new Random();
				new BukkitRunnable() {
					int i = 0;
					Location loc;
					public void run() {
						loc = e.getProjectile().getLocation();
						if(i>200||e.getProjectile().isOnGround()||!RegionManager.getRegionsAtLocation(loc).contains(FFAUtils.reg_arena)){
							e.getProjectile().remove();
							cancel();
							return;
						}

						loc.setY(11);
						softReplace(loc.getBlock(), bl.getType(), b);

						if(r.nextInt(4)==1){
							loc.setX(loc.getX()+1);
							softReplace(loc.getBlock(), bl.getType(), b);
							loc.setX(loc.getX()-1);
						}else if(r.nextInt(4)==1){
							loc.setX(loc.getX()-1);
							softReplace(loc.getBlock(), bl.getType(), b);
							loc.setX(loc.getX()+1);
						}

						loc = e.getProjectile().getLocation();
						loc.setY(11);

						if(r.nextInt(4)==1){
							loc.setZ(loc.getZ()+1);
							softReplace(loc.getBlock(), bl.getType(), b);
							loc.setZ(loc.getZ()-1);
						}else if(r.nextInt(4)==1){
							loc.setZ(loc.getZ()-1);
							softReplace(loc.getBlock(), bl.getType(), b);
							loc.setZ(loc.getZ()+1);
						}
						i++;
					}
				}.runTaskTimer(Main.main, 0, 1);
			}
	}


	public static void softReplace(Block b, Material id, byte dura){
		if(b.getType()==Material.AIR){
			b.setType(id);
			b.setData(dura);
		}
	}


	@EventHandler
	public static void a(PlayerInteractEvent e) {
		if (e.getPlayer().getWorld() != FFAUtils.world) return;

		if (e.getAction().equals(Action.PHYSICAL)) {
			if (e.getClickedBlock().getType() == Material.GOLD_PLATE) {
				e.getPlayer().getInventory().setChestplate(new ItemStack(Material.ELYTRA));
				new BukkitRunnable() {
					public void run() {
						e.getPlayer().setVelocity(new Vector(0, 0.9, 0));
					}
				}.runTask(Main.main);
				new BukkitRunnable() {
					public void run() {
						e.getPlayer().setGliding(true);
					}
				}.runTaskLater(Main.main, 14);
			}
		}
	}

	@EventHandler
	public static void a(EntityToggleGlideEvent e) {
		if(e.getEntity().getWorld() == FFAUtils.world&&e.getEntity() instanceof Player){
			Player p = (Player)e.getEntity();
			if(!e.isGliding())p.getInventory().setChestplate(FFAUtils.ffaarmor[1]);
		}
	}

	@EventHandler
	public static void a(EntityExplodeEvent e) {
		if (e.getEntity().getWorld() != FFAUtils.world) return;
		HashMap<UUID, Vector> vecs = new HashMap<>();
		for(Block b : e.blockList()){
			if(FFAUtils.canbeBroken(b)){
				if(b.getType()==Material.TNT){
					e.getEntity().getWorld().createExplosion(b.getX(), b.getY(), b.getZ(), 4f, false, false);
					simuleExplosion(b.getLocation(), vecs);
				}
				b.setType(Material.AIR);
			}
		}
		e.setCancelled(true);
		e.getEntity().getWorld().createExplosion(e.getEntity().getLocation().getX(), e.getEntity().getLocation().getY(), e.getEntity().getLocation().getZ(), 4f, false, false);
		e.blockList().clear();
		int radius = 4;
		for(Entity e2 : e.getEntity().getWorld().getNearbyEntities(e.getEntity().getLocation(), radius, radius, radius)){
			if(e2 instanceof TNTPrimed){
				simuleExplosion(e2.getLocation(), vecs);
				e2.remove();
			}
		}

		for(Map.Entry<UUID, Vector> en : vecs.entrySet()){
			if(en.getValue().getY()>3)en.getValue().setY(3);

			if(en.getValue().getX()>4)en.getValue().setX(4);
			else if(en.getValue().getX()<-4)en.getValue().setX(-4);

			if(en.getValue().getZ()>4)en.getValue().setZ(4);
			else if(en.getValue().getZ()<-4)en.getValue().setZ(-4);
			Bukkit.getPlayer(en.getKey()).setVelocity(en.getValue());
		}

	}

	public static void simuleExplosion(Location loc, HashMap<UUID, Vector> vecs) {
		double temp;
		for(Entity e : loc.getWorld().getNearbyEntities(loc, 4.5, 4.5, 4.5)){
			if(e instanceof Player){
				temp = loc.distance(e.getLocation());
				Vector vec = vectorise(loc, e.getLocation());
				vec.multiply(1/temp);

				if(vecs.size()==0){
					vec.multiply(1.90);
				}else vec.setY(vec.getY()*1.05);

				Vector v = vecs.get(e.getUniqueId());

				if(v==null)vecs.put(e.getUniqueId(), vec);
				else v.add(vec);
			}
		}
	}

	public static Vector vectorise(Location vec1, Location vec2) {
		Vector result = new Vector();
		result.setX(vec2.getX()-vec1.getX());
		result.setY(vec2.getY()-vec1.getY());
		result.setZ(vec2.getZ()-vec1.getZ());
		return result;
	}

	private static <T> Predicate<T> nop(Predicate<T> p) {
		return p.negate();
	}

}
