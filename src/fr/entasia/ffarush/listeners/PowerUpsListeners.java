package fr.entasia.ffarush.listeners;

import fr.entasia.apis.regionManager.api.RegionManager;
import fr.entasia.apis.utils.ItemUtils;
import fr.entasia.apis.utils.VectorUtils;
import fr.entasia.ffarush.FFAUtils;
import fr.entasia.ffarush.Main;
import fr.entasia.ffarush.utils.FFAPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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

public class PowerUpsListeners implements Listener {

	@EventHandler
	public void a(EntityShootBowEvent e) {
		if (e.getEntity().getWorld() != FFAUtils.world) return;
		if (e.getEntityType() != EntityType.PLAYER) return;
		if (!RegionManager.getRegionsAt(e.getEntity().getLocation()).contains(FFAUtils.reg_arena)) return;
		if (ItemUtils.hasName(e.getBow(), "§7BlockBow")) {
			FFAPlayer ffp = FFAUtils.playerCache.get(e.getEntity().getUniqueId());
			if (ffp == null) return;

			ItemStack bl = FFAUtils.ffablocks[ffp.block];

			Random r = new Random();
			new BukkitRunnable() {
				int i = 0;
				Location loc;

				public void run() {
					loc = e.getProjectile().getLocation();
					if (i > 200 || e.getProjectile().isOnGround() || !RegionManager.getRegionsAt(loc).contains(FFAUtils.reg_arena)) {
						e.getProjectile().remove();
						cancel();
						return;
					}

					loc.setY(11);
					softReplace(loc.getBlock(), bl.getType());


					BlockFace bf = faces[r.nextInt(4)];
					loc.add(new Vector(bf.getModX(), bf.getModY(), bf.getModZ()));
					softReplace(loc.getBlock(), bl.getType());
					i++;
				}
			}.runTaskTimer(Main.main, 0, 1);

		}
	}

	public static BlockFace[] faces = new BlockFace[]{BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};


	public static void softReplace(Block b, Material id){
		if(b.getType()==Material.AIR){
			b.setType(id);
		}
	}


	@EventHandler
	public static void a(PlayerInteractEvent e) {
		if (e.getPlayer().getWorld() != FFAUtils.world) return;

		if (e.getAction().equals(Action.PHYSICAL)) {
			if (e.getClickedBlock().getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
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
		HashMap<Entity, Vector> vecs = new HashMap<>();
		e.setCancelled(true);
		e.getEntity().getWorld().createExplosion(e.getEntity().getLocation(), 4f, false, false);
		for(Block b : e.blockList()){
			if(FFAUtils.canbeBroken(b)){
				if(b.getType()==Material.TNT){
					e.getEntity().getWorld().createExplosion(b.getX(), b.getY(), b.getZ(), 4f, false, false);
					simuleExplosion(b.getLocation(), vecs);
				}
				b.setType(Material.AIR);
			}
		}
		e.blockList().clear();

		for(Entity e2 : e.getEntity().getWorld().getNearbyEntities(e.getEntity().getLocation(), 4.5, 4.5, 4.5)){
			if(e2 instanceof TNTPrimed){

				simuleExplosion(e2.getLocation(), vecs);
				e2.remove();
			}
		}

		for(Map.Entry<Entity, Vector> en : vecs.entrySet()){
			if(en.getValue().getY()>3)en.getValue().setY(3);

			VectorUtils.limitVector(en.getValue());
			en.getKey().setVelocity(en.getValue());
		}

	}

	public static void simuleExplosion(Location loc, HashMap<Entity, Vector> vecs) {
		double temp;
		for(Entity e : loc.getWorld().getNearbyEntities(loc, 4.5, 4.5, 4.5)){
			if(e instanceof Player){
				temp = loc.distance(e.getLocation());
				Vector vec = vectorise(loc, e.getLocation());
				vec.multiply(1/temp);

				if(vecs.size()==0){
					vec.multiply(1.90);
				}else vec.setY(vec.getY()*1.05);

				Vector v = vecs.get(e);

				if(v==null)vecs.put(e, vec);
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

}
