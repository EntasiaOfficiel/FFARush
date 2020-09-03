package fr.entasia.ffarush.utils;

import fr.entasia.apis.menus.MenuClickEvent;
import fr.entasia.apis.menus.MenuCloseEvent;
import fr.entasia.apis.menus.MenuCreator;
import fr.entasia.apis.menus.MenuFlag;
import fr.entasia.egtools.utils.MoneyUtils;
import fr.entasia.ffarush.FFAUtils;
import fr.entasia.ffarush.Main;
import fr.entasia.ffarush.deathParticle.ParticleInv;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

public class InvsManager {

	public static MenuCreator options = new MenuCreator(){
		@Override
		public void onMenuClick(MenuClickEvent e) {
			switch(e.item.getType()) {
				case SANDSTONE:
					blockOpen(FFAUtils.playerCache.get(e.player.getUniqueId()));
					break;
				case IRON_CHESTPLATE: {
					customInvOpen(FFAUtils.playerCache.get(e.player.getUniqueId()));
					break;
				}
				case FIREWORK_ROCKET: {
					ParticleInv.deathParticlesOpen(FFAUtils.playerCache.get(e.player.getUniqueId()));
					break;
				}
				default: {
					e.player.sendMessage("§cOops ! Fonction non créée ):");
				}
			}
		}
	};

	public static void optionsOpen(Player p){
		Inventory inv = options.createInv(5, "§9Options");

		ItemStack item = new ItemStack(Material.SANDSTONE);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§6Blocks !");
		item.setItemMeta(meta);
		inv.setItem(11, item);

		item = new ItemStack(Material.IRON_CHESTPLATE);
		meta = item.getItemMeta();
		meta.setDisplayName("§7Modifier la disposition de l'inventaire");
		item.setItemMeta(meta);
		inv.setItem(15, item);

		item = new ItemStack(Material.FIREWORK_ROCKET);
		meta = item.getItemMeta();
		meta.setDisplayName("§7Particules de mort");
		item.setItemMeta(meta);
		inv.setItem(22, item);

		p.openInventory(inv);
	}

	private static final int[] prices = new int[]{100,300,600,950,1300,1500,2000};

	public static MenuCreator blocks = new MenuCreator() {
		@Override
		public void onMenuClick(MenuClickEvent e) {
			if (e.item.getType() == Material.WRITABLE_BOOK) {
				optionsOpen(e.player);
				return;
			}
			FFAPlayer ffp = (FFAPlayer) e.data;
			if (ffp == null) return;
			byte id;
			switch (e.item.getType()) {
				case SANDSTONE:
					id = -1;
					break;
				case SMOOTH_SANDSTONE:
					id = 0;
					break;
				case QUARTZ_BLOCK:
					id = 1;
					break;
				case NETHER_BRICK:
					id = 2;
					break;
				case PURPUR_BLOCK:
					id = 3;
					break;
				case END_STONE_BRICKS:
					id = 4;
					break;
				case BRICK:
					id = 5;
					break;
				case PRISMARINE:
					id = 6;
					break;
				default:
					e.player.sendMessage("§cOops ! Ce block n'a pas été pris en charge ! Contacte un membre du staff");
					return;
			}

			if (id == -1 || ((ffp.blocks >> id) & 1) == 1) {
				ffp.p.sendMessage("§aBlock choisi ! " + e.item.getType());
				ffp.block = (byte) (id + 1);
				ffp.p.closeInventory();
				blockOpen(ffp);
			} else {
				int price = prices[id];
				if (MoneyUtils.getMoney(ffp.p.getUniqueId()) < price)
					ffp.p.sendMessage("§cTu n'a pas assez d'argent pour débloquer ce block !");
				else {
					ffp.p.sendMessage("§aAchat fait avec succès !");
					MoneyUtils.removeMoney(ffp.p.getUniqueId(), price);
					ffp.block = (byte) (id + 1);
					ffp.blocks |= 1 << id;
					ffp.p.closeInventory();
					blockOpen(ffp);
				}
			}
		}
	};


	public static void blockOpen(FFAPlayer ffp){
		Inventory inv = blocks.createInv(5, "§3Choix du block", ffp);


		ItemStack tite = new ItemStack(Material.WRITABLE_BOOK);
		ItemMeta meta = tite.getItemMeta();
		meta.setDisplayName("§cRetour");
		tite.setItemMeta(meta);
		inv.setItem(44, tite);

		int[] places = new int[]{10, 12, 14, 16,  28, 30, 32, 34};
		byte index = ffp.block;
		ArrayList<String> list;

		for(int i=0;i<FFAUtils.ffablocks.length;i++){
			tite = new ItemStack(FFAUtils.ffablocks[i]);
			list = new ArrayList<>();
			if(i!=0){
				if(((ffp.blocks >> i-1) & 1) == 0)
					list.add("§ePrix : "+prices[i-1]+"$");
				else
					list.add("§aAcheté !");
			}
			meta = tite.getItemMeta();
			if(index==i){
				meta.addEnchant(Enchantment.LURE, 1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				list.add("");
				list.add("§bBlock actuellement choisi");
			}
			meta.setLore(list);
			tite.setItemMeta(meta);
			inv.setItem(places[i], tite);
		}
		ffp.p.openInventory(inv);
	}

	public static MenuCreator customInv = new MenuCreator(){
		@Override
		public void onMenuClick(MenuClickEvent e) {
			if (e.item.getType()==Material.WRITABLE_BOOK) {
				InvsManager.optionsOpen(e.player);
			}else if (e.item.getType()==Material.BARRIER) {
				for (int i = 0; i < 10; i++) {
					e.inv.setItem(i, null);
				}
				for (int i = 0; i < FFAUtils.defaultInv.length; i++) {
					if (FFAUtils.defaultInv[i] == 40) e.inv.setItem(9, FFAUtils.ffaitems[i]);
					else e.inv.setItem(FFAUtils.defaultInv[i], FFAUtils.ffaitems[i]);
				}
			}
		}

		@Override
		public void onMenuClose(MenuCloseEvent e) {
			FFAPlayer ffp = (FFAPlayer)e.data;
			if(ffp==null)return;

			byte[] a = new byte[FFAUtils.ffaitems.length];

			byte v;
			for(byte i=0;i<10;i++){
				if(e.inv.getItem(i)==null)continue;
				if(i==9)v=40;
				else v = i;
				switch(e.inv.getItem(i).getType()){
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
						Main.warn("Item invalide detectée dans la modification d'inventaire ! "+e.inv.getItem(i).getType());
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
	}.setFlags(MenuFlag.NoMoveLocalInv, MenuFlag.NoReturnUnlockedItems).setFreeSlots(IntStream.range(0, 10).toArray());


	public static void customInvOpen(FFAPlayer ffp){
		Inventory inv = customInv.createInv(2, "§7Modifier l'inventaire de départ");


		ItemStack item = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
		for(int i=10;i<17;i++){
			inv.setItem(i, item);
		}

		item = new ItemStack(Material.WRITABLE_BOOK);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§cRetour");
		item.setItemMeta(meta);
		inv.setItem(17, item);

		item = new ItemStack(Material.BARRIER);
		meta = item.getItemMeta();
		meta.setDisplayName("§cRemettre par défaut");
		item.setItemMeta(meta);
		inv.setItem(16, item);

		byte[] inve;
		if(ffp.inv==null)inve = FFAUtils.defaultInv;
		else inve = ffp.inv;

		for(int i=0;i<inve.length;i++){
			if(inve[i]==40)inv.setItem(9, FFAUtils.ffaitems[i]);
			else inv.setItem(inve[i], FFAUtils.ffaitems[i]);
		}

		ffp.p.openInventory(inv);
	}
}
