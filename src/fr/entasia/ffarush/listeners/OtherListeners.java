package fr.entasia.ffarush.listeners;

import fr.entasia.apis.regionManager.api.RegionAction;
import fr.entasia.apis.regionManager.api.RegionManager;
import fr.entasia.apis.regionManager.events.RegionLeaveEvent;
import fr.entasia.ffarush.FFAUtils;
import fr.entasia.ffarush.Main;
import fr.entasia.ffarush.utils.FFAPlayer;
import fr.entasia.ffarush.utils.InvsManager;
import fr.entasia.ffarush.utils.SBManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class OtherListeners implements Listener {


	@EventHandler
	public static void onJoin(PlayerJoinEvent e) {
		FFAPlayer ffp = new FFAPlayer(e.getPlayer());
		try{ // ID = ITEM | VALUE = SLOT
			Main.sqlConnection.checkConnect();
			PreparedStatement ps = Main.sqlConnection.connection.prepareStatement("SELECT ffa_kills, ffa_deaths, ffa_blocks, ffa_cblock, ffa_inv from entagames where uuid=?");
			ps.setString(1, ffp.p.getUniqueId().toString());
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				ffp.kills = rs.getInt(1);
				ffp.deaths = rs.getInt(2);
				ffp.blocks = rs.getByte(3);
				ffp.block = rs.getByte(4);
				if(rs.getInt(5)!=0){
					ffp.inv = new byte[FFAUtils.ffaitems.length];
					int a=0;
					for(byte i : rs.getBytes(5)){
						a=(a<<8)+i;
//						System.out.println(Integer.toBinaryString(a));
					}
					for(int i=0;i<ffp.inv.length;i++){
						ffp.inv[i] = (byte) ((15<<i*4 & a)>>i*4); // 15 = 1111 , on récupère les bits 4 par 4
						if(ffp.inv[i]==9) ffp.inv[i] = 40;
					}
				}
				ffp.sb = new SBManager(ffp);
				FFAUtils.playerCache.put(e.getPlayer().getUniqueId(), ffp);
			}
		}catch(SQLException|NumberFormatException e2){
			e2.printStackTrace();
			e.getPlayer().sendMessage("§cErreur lors du chargement de ton profil FFARush ! Contacte un membre du staff");
			FFAUtils.playerCache.remove(e.getPlayer().getUniqueId());
			return;
		}
		FFAUtils.playerCache.put(e.getPlayer().getUniqueId(), ffp);
	}


	@EventHandler
	public static void onQuit(PlayerQuitEvent e) {
		FFAPlayer ffp = FFAUtils.playerCache.get(e.getPlayer().getUniqueId());
		if(ffp!=null){
			FFAUtils.saveUser(ffp);
			FFAUtils.playerCache.remove(e.getPlayer().getUniqueId());
		}
	}


	@EventHandler
	public static void onDrop(PlayerDropItemEvent e) {
		if(e.getPlayer().getWorld()!=FFAUtils.world)return;
		e.setCancelled(true);
	}


	@EventHandler
	public static void onInteract(PlayerInteractEvent e) {
		if(e.getPlayer().getWorld()!=FFAUtils.world||e.getHand()==EquipmentSlot.OFF_HAND||e.getItem()==null)return;

		if(e.getAction()== Action.LEFT_CLICK_AIR||e.getAction()==Action.LEFT_CLICK_BLOCK||
		e.getAction()==Action.RIGHT_CLICK_AIR||e.getAction()==Action.RIGHT_CLICK_BLOCK){

			if(ItemClick(e.getItem(), e.getPlayer()))e.setCancelled(true);

		}
	}


	@EventHandler
	public static void blockIgnite(BlockIgniteEvent e) {
		if (e.getBlock().getWorld() == FFAUtils.world){
			e.setCancelled(true);
		}
	}


	@EventHandler
	public static void blockPlace(BlockPlaceEvent e) {
		if(e.getPlayer().getWorld()!=FFAUtils.world)return;

		if(RegionManager.getRegionsAtLocation(e.getBlockPlaced().getLocation()).contains(FFAUtils.reg_arena)) { // en pvp
			boolean ok = false;
			for(ItemStack i : FFAUtils.ffablocks){
				if(i.getType()==e.getBlockPlaced().getType()&&i.getDurability()==e.getBlockPlaced().getData()){
					ok = true;
					break;
				}
			}
			if(ok){
				ItemStack item = e.getItemInHand();
				item.setAmount(64);
				if(e.getHand()==EquipmentSlot.HAND)e.getPlayer().getInventory().setItemInMainHand(item);
				if(e.getHand()==EquipmentSlot.OFF_HAND)e.getPlayer().getInventory().setItemInOffHand(item);
				e.getPlayer().updateInventory();
				e.setCancelled(false);
			}else if(e.getBlockPlaced().getType()==Material.TNT){
				e.setCancelled(false);
			}
		}
	}


	@EventHandler
	public static void blockBreak(BlockBreakEvent e) {
		if(e.getPlayer().getWorld()!=FFAUtils.world)return;

		if(RegionManager.getRegionsAtLocation(e.getBlock().getLocation()).contains(FFAUtils.reg_arena)) { // en pvp
			if(FFAUtils.canbeBroken(e.getBlock())){
				e.setCancelled(false);
				if(e.getBlock().getType()==Material.TNT){
					ItemStack tnt = FFAUtils.ffaitems[4].clone();
					tnt.setAmount(1);
					e.getPlayer().getInventory().addItem(tnt);
				}
			}
		}
	}

	@EventHandler
	public static void onInteract(PlayerInteractEntityEvent e) {
		if(e.getPlayer().getWorld()!=FFAUtils.world||e.getHand()==EquipmentSlot.OFF_HAND)return;

		if(ItemClick(e.getPlayer().getInventory().getItemInMainHand(), e.getPlayer()))e.setCancelled(true);
		else{
			if(RegionManager.getRegionsAtLocation(e.getPlayer().getLocation()).contains(FFAUtils.reg_arena)){ // en pvp
				if(e.getPlayer().getInventory().getItemInMainHand().getType()== Material.FLINT_AND_STEEL&&
						e.getRightClicked() instanceof TNTPrimed){
					TNTPrimed tnt = (TNTPrimed)e.getRightClicked();
					tnt.setFuseTicks(9);
				}
			}
			if(RegionManager.getRegionsAtLocation(e.getPlayer().getLocation()).contains(FFAUtils.reg_spawn)){ // au spawn
				e.setCancelled(true);
			}
		}
	}

	public static boolean ItemClick(ItemStack item, Player p) {
		if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
			switch (item.getItemMeta().getDisplayName()) {
				case "§6Jouer !":
					FFAUtils.joinFFA(p);
					break;
				case "§9Options":
					InvsManager.optionsOpen(p);
					break;
				default:
					return false;
			}
			return true;
		}
		return false;
	}

	@EventHandler
	public static void onInvClick2(InventoryClickEvent e) {
		if(!(e.getWhoClicked() instanceof Player)||e.getWhoClicked().getWorld()!=FFAUtils.world||
				e.getClickedInventory()==null||e.getCurrentItem()==null)return;

		if(e.getInventory().getName().equalsIgnoreCase("§7Modifier l'inventaire de départ")) {
			if (e.getClickedInventory() instanceof PlayerInventory) {
				e.setCancelled(true);
			} else if (e.getClick() != ClickType.LEFT || e.getCurrentItem().getType() == Material.STAINED_GLASS_PANE)
				e.setCancelled(true);
			else if (e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()){
				if(e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("§cRetour"))
					InvsManager.optionsOpen((Player) e.getWhoClicked());
				else if(e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("§cRemettre par défaut")){
					for(int i=0;i<10;i++){
						e.getInventory().setItem(i, null);
					}
					for(int i=0;i<FFAUtils.defaultInv.length;i++){
						if(FFAUtils.defaultInv[i]==40)e.getInventory().setItem(9, FFAUtils.ffaitems[i]);
						else e.getInventory().setItem(FFAUtils.defaultInv[i], FFAUtils.ffaitems[i]);
					}
				}else return;
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public static void onInvClick(InventoryClickEvent e) {
		if(!(e.getWhoClicked() instanceof Player)||e.getWhoClicked().getWorld()!=FFAUtils.world||e.getClickedInventory()==null)return;

		if(e.getClickedInventory().getType() == InventoryType.CHEST) {
			if(e.getAction() == InventoryAction.NOTHING||e.getCurrentItem() == null||e.getCurrentItem().getType() == Material.AIR)return;
			if(e.getClickedInventory().getName().equalsIgnoreCase("§9Options")){
				InvsManager.optionsClick((Player)e.getWhoClicked(), e.getCurrentItem());
			}else if(e.getClickedInventory().getName().equalsIgnoreCase("§3Choix du block")) {
				InvsManager.blockClick(FFAUtils.playerCache.get(e.getWhoClicked().getUniqueId()), e.getCurrentItem());
			}else return;
			e.setCancelled(true);
		}else{
			if(e.getWhoClicked().getGameMode()==GameMode.CREATIVE&&e.getSlot()<=39&&e.getSlot()>=36)e.setCancelled(true);
		}
	}

	@EventHandler
	public static void invClose(InventoryCloseEvent e) {
		if(e.getPlayer().getWorld()!=FFAUtils.world||!e.getInventory().getName().equals("§7Modifier l'inventaire de départ"))return;
		FFAPlayer ffp = FFAUtils.playerCache.get(e.getPlayer().getUniqueId());
		if(ffp==null)return;

		byte[] a = new byte[FFAUtils.ffaitems.length];

		byte v;
		for(byte i=0;i<10;i++){
			if(e.getInventory().getItem(i)==null)continue;
			if(i==9)v=40;
			else v = i;
			switch(e.getInventory().getItem(i).getType()){
				case DIAMOND_SWORD:
					a[0] = v;
					break;
				case IRON_PICKAXE:
					a[1] = v;
					break;
				case BOW:
					a[2] = v;
					break;
				case GOLDEN_APPLE:
					a[3] = v;
					break;
				case TNT:
					a[4] = v;
					break;
				case FLINT_AND_STEEL:
					a[5] = v;
					break;
				default:
					Main.warn("Item invalide detectée dans la modification d'inventaire ! "+e.getInventory().getItem(i).getType());
			}
//			for(int j=0;j<4;j++){
//				if(((i >> j) & 1) == 0){
//					bs.clear(ran*4+j);
//				}else{
//					bs.set(ran*4+j);
//				}
//			}
		}
		if(Arrays.equals(FFAUtils.defaultInv, a)) ffp.inv = null;
		else ffp.inv = a;
		ffp.p.sendMessage("§6Nouvelle configuration de l'inventaire sauvegardée !");
	}

	@EventHandler
	public static void regionQuit(RegionLeaveEvent e) {
		if(e.getTriggerType()== RegionAction.MOVE&&(e.getPlayer().getGameMode()==GameMode.SURVIVAL||e.getPlayer().getGameMode()==GameMode.ADVENTURE)){
			if(e.getRegion()==FFAUtils.reg_spawn){
				e.getPlayer().teleport(FFAUtils.spawn);
				e.getPlayer().sendMessage("§cAttention à ne pas tomber !");
			}else if(e.getRegion()==FFAUtils.reg_arena){
				if(e.getPlayer().getLocation().getY()<FFAUtils.reg_arena.getLowerBound().getY()+3){
					FightListeners.kill(FFAUtils.playerCache.get(e.getPlayer().getUniqueId()));
				}
			}
		}
	}

	@EventHandler
	public static void worldChange(PlayerChangedWorldEvent e){
		if(e.getPlayer().getWorld()==FFAUtils.world){
			e.getPlayer().setMaximumNoDamageTicks(FFAUtils.damageticks);
		}else{
			e.getPlayer().setMaximumNoDamageTicks(17);
		}

	}
}
