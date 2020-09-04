package fr.entasia.ffarush;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.FaweCache;
import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.math.BlockVector3Imp;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
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
import org.bukkit.util.BlockVector;

import java.sql.SQLException;
import java.util.*;

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

			ffp.sb.set();
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

	public static HashSet<BaseBlock> blocks = new HashSet<>();
	public static BaseBlock redBlock = new BaseBlock(BlockTypes.RED_TERRACOTTA.getDefaultState());
	public static BaseBlock airBlock = new BaseBlock(BlockTypes.AIR.getDefaultState());
	public static com.sk89q.worldedit.regions.Region reg;

	static{
		blocks.add(new BaseBlock(BlockTypes.TNT.getDefaultState())); // tnt
		blocks.add(new BaseBlock(BlockTypes.SANDSTONE.getDefaultState())); // sandstone
		blocks.add(new BaseBlock(BlockTypes.SMOOTH_SANDSTONE.getDefaultState())); // sandstone 2
		blocks.add(new BaseBlock(BlockTypes.QUARTZ_BLOCK.getDefaultState())); // quartz
		blocks.add(new BaseBlock(BlockTypes.NETHER_BRICKS.getDefaultState())); // nether bricks
		blocks.add(new BaseBlock(BlockTypes.PURPUR_BLOCK.getDefaultState())); // purpur
		blocks.add(new BaseBlock(BlockTypes.END_STONE_BRICKS.getDefaultState())); // end bricks
		blocks.add(new BaseBlock(BlockTypes.BRICKS.getDefaultState())); // bricks
		blocks.add(new BaseBlock(BlockTypes.PRISMARINE.getDefaultState())); // bricks

		reg = new CuboidRegion(
				BlockVector3Imp.at(reg_arena.getLowerBound().x, reg_arena.getLowerBound().y, reg_arena.getLowerBound().z),
				BlockVector3Imp.at(reg_arena.getUpperBound().x, reg_arena.getUpperBound().y, reg_arena.getUpperBound().z)
		);
	}

	public static void clearArena() {
		EditSession editSession = new EditSessionBuilder(FaweAPI.getWorld(world.getName())).fastmode(true).build();


		editSession.replaceBlocks(reg, blocks, redBlock);
		editSession.flushQueue();
		FFAUtils.sendMessage("§6Les blocks rouges vont être supprimés dans 30 secondes !");

		new BukkitRunnable() {
			public void run() {
				editSession.replaceBlocks(reg, Collections.singleton(redBlock), airBlock);
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
