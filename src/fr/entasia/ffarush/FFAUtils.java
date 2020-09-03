package fr.entasia.ffarush;

import com.boydti.fawe.FaweCache;
import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import fr.entasia.apis.regionManager.api.Region;
import fr.entasia.egtools.Utils;
import fr.entasia.ffarush.utils.FFAPlayer;
import fr.entasia.ffarush.utils.SQLUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class FFAUtils {

	public static World world;

	public static Region reg_arena, reg_spawn;

	public static ItemStack[] ffaitems = new ItemStack[6];
	public static ItemStack[] ffaarmor = new ItemStack[4];
	public static ItemStack[] ffablocks = new ItemStack[8];

	public static int damageticks;
	public static Location[] spawnsloc = new Location[12];
	public static Location spawn;
	public static byte[] defaultInv = new byte[ffaitems.length];

	public static HashMap<UUID, FFAPlayer> playerCache = new HashMap<>();


	public static void joinFFA(Player p) {
		FFAPlayer ffp = playerCache.get(p.getUniqueId());
		if(ffp==null)p.sendMessage("§cTon profil FFARush est mal chargé ! Contacte un membre du staff");
		else {
			Location loc = null;
			boolean no=false;
			for(int i=0;i<50;i++){
				loc = spawnsloc[(int) (Math.random() * 12)];
				for (Player lp : world.getPlayers()){
					if(lp.getLocation().distance(loc)<6){
						no = true;
						break;
					}
				}
				if(no)no = false;
				else break;
			}
			p.teleport(loc);
			p.getInventory().clear();
			p.getActivePotionEffects().clear();
			p.setGameMode(GameMode.SURVIVAL);
			p.setHealth(20);

			byte[] inv;
			if (ffp.inv == null) inv = defaultInv;
			else inv = ffp.inv;

			for(int i=0;i<inv.length;i++){
				p.getInventory().setItem(inv[i], FFAUtils.ffaitems[i]);
			}


			p.getInventory().setHelmet(ffaarmor[0]);
			p.getInventory().setChestplate(ffaarmor[1]);
			p.getInventory().setLeggings(ffaarmor[2]);
			p.getInventory().setBoots(ffaarmor[3]);

			if (p.getInventory().getItem(40) == null) p.getInventory().setItem(40, FFAUtils.ffablocks[ffp.block]);
			for (int i = 0; i < 9; i++) {
				if (p.getInventory().getItem(i) == null) p.getInventory().setItem(i, FFAUtils.ffablocks[ffp.block]);
			}
			p.getInventory().setItem(9, new ItemStack(Material.ARROW, 2));

			ffp.sb.softSet();
		}
	}

	public static void tpSpawnFFA(Player p, boolean first) {
		FFAPlayer ffp = playerCache.get(p.getUniqueId());
		if(ffp==null)p.sendMessage("§cTon profil FFARush est mal chargé ! Contacte un membre du staff");
		else{
			p.teleport(spawn);
			if(first){
				p.sendMessage("§6Tu as été téléporté au §cFFARush§6 !");
			}

//			Bukkit.broadcastMessage("DEBUG 1");
			Utils.reset(p);
//			Bukkit.broadcastMessage("DEBUG 3");
			p.setGameMode(GameMode.SURVIVAL);

			ffp.sb.refresh();

			p.getInventory().setHeldItemSlot(4);

			ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("§9Options");
			item.setItemMeta(meta);
			p.getInventory().setItem(0, item);

			item = new ItemStack(Material.IRON_AXE);
			meta = item.getItemMeta();
			meta.setDisplayName("§6Jouer !");
			item.setItemMeta(meta);
			p.getInventory().setItem(4, item);

			item = new ItemStack(Material.ORANGE_BED);
			meta = item.getItemMeta();
			meta.setDisplayName("§cRetour au spawn EntaGames");
			item.setItemMeta(meta);
			p.getInventory().setItem(8, item);

		}
	}

	public static void clearArena() {
		EditSession editSession = new EditSessionBuilder(world.getName()).fastmode(true).build();
		HashSet<BaseBlock> blocks = new HashSet<>();
		blocks.add(FaweCache.getBlock(46, 0)); // tnt
		blocks.add(FaweCache.getBlock(24, 0)); // sandstone
		blocks.add(FaweCache.getBlock(24, 2)); // sandstone 2
		blocks.add(FaweCache.getBlock(155, 0)); // quartz
		blocks.add(FaweCache.getBlock(112, 0)); // nether bricks
		blocks.add(FaweCache.getBlock(201, 0)); // purpur
		blocks.add(FaweCache.getBlock(206, 0)); // end bricks
		blocks.add(FaweCache.getBlock(45, 0)); // bricks
		blocks.add(FaweCache.getBlock(168, 0)); // bricks
		com.sk89q.worldedit.regions.Region r = new CuboidRegion(
				new Vector(reg_arena.getLowerBound().x, reg_arena.getLowerBound().y, reg_arena.getLowerBound().z),
				new Vector(reg_arena.getUpperBound().x, reg_arena.getUpperBound().y, reg_arena.getUpperBound().z));
		editSession.replaceBlocks(r, blocks, FaweCache.getBlock(159, 14));
		editSession.flushQueue();
		FFAUtils.sendMessage("§6Les blocks rouges vont être supprimés dans 30 secondes !");

		new BukkitRunnable() {
			public void run() {
				blocks.clear();
				blocks.add(FaweCache.getBlock(159, 14));
				editSession.replaceBlocks(r, blocks, FaweCache.getBlock(0, 0));
				editSession.flushQueue();
				FFAUtils.sendMessage("§6Les blocks rouges ont été supprimés !");
			}
		}.runTaskLaterAsynchronously(Main.main, 600); // 30sec * 20 = 600
	}

	public static boolean saveAllUsers() {
		try{
			Main.sql.checkConnect();
			for(FFAPlayer ffp : playerCache.values()){
				SQLUtils.getSQLSaveObj(ffp).execute();
				if(!ffp.p.isOnline()) playerCache.remove(ffp.p.getUniqueId());
			}
			return true;
		}catch(SQLException e){
			e.printStackTrace();
		}
		return false;

	}

	public static boolean saveUser(FFAPlayer ffp) {
		try{
			Main.sql.checkConnect();
			SQLUtils.getSQLSaveObj(ffp).execute();
			return true;
		}catch(SQLException e){
			e.printStackTrace();
		}
		return false;
	}

	public static void sendMessage(String msg) {
		for(Player p : world.getPlayers()){
			if(p.isOnline())p.sendMessage(msg);
		}
	}

	public static boolean canbeBroken(Block block) {
		for(ItemStack i : FFAUtils.ffablocks){
			if(block.getType()==i.getType())return true;
		}
		return (block.getType()==Material.TNT||block.getType()==Material.FIRE||
				(block.getType()==Material.RED_TERRACOTTA));
	}
}
