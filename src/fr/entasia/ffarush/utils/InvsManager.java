package fr.entasia.ffarush.utils;

import fr.entasia.egtools.utils.MoneyUtils;
import fr.entasia.ffarush.FFAUtils;
import fr.entasia.ffarush.deathParticle.ParticleInv;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class InvsManager {

	public static void optionsOpen(Player p){
		Inventory inv = Bukkit.createInventory(null, 45, "§9Options");

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

		LocalDateTime now = LocalDateTime.now();
		if(now.getMonthValue() >= 8 || now.getYear()>=2021 || p.getDisplayName().equalsIgnoreCase("Stargeyt") || p.getDisplayName().equalsIgnoreCase("iTrooz_")){
			item = new ItemStack(Material.FIREWORK_ROCKET);
			meta = item.getItemMeta();
			meta.setDisplayName("§7Particules de mort");
			item.setItemMeta(meta);
			inv.setItem(22,item);
		}



		p.openInventory(inv);

	}

	public static void optionsClick(Player p, ItemStack item){
		if(item.hasItemMeta()&&item.getItemMeta().hasDisplayName()){
			switch(item.getType()){
				case SANDSTONE:
					blockOpen(FFAUtils.playerCache.get(p.getUniqueId()));
					break;
				case IRON_CHESTPLATE:
					customInvOpen(FFAUtils.playerCache.get(p.getUniqueId()));
					break;
				case FIREWORK_ROCKET:
					ParticleInv.deathParticleOpenMenu(FFAUtils.playerCache.get(p.getUniqueId()));
					break;
				default:
					p.sendMessage("§cOops ! Fonction non créée ):");
			}
		}

	}

	public static int[] prices = new int[]{100,300,600,950,1300,1500,2000};

	public static void blockOpen(FFAPlayer ffp){
		Inventory inv = Bukkit.createInventory(null, 45, "§3Choix du block");

		byte index = ffp.block;
		int[] places = new int[]{10, 12, 14, 16,  28, 30, 32, 34};
		ItemStack tite = new ItemStack(Material.WRITABLE_BOOK);
		ItemMeta meta = tite.getItemMeta();
		meta.setDisplayName("§cRetour");
		tite.setItemMeta(meta);
		inv.setItem(44, tite);
		ArrayList<String> list;
		for(int i=0;i<FFAUtils.ffablocks.length;i++){
			tite = new ItemStack(FFAUtils.ffablocks[i].getType(), 1, FFAUtils.ffablocks[i].getDurability());
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

	public static void blockClick(FFAPlayer ffp, ItemStack item){
		if(item.hasItemMeta()&&item.getItemMeta().hasDisplayName()) {
			if(item.getItemMeta().getDisplayName().equalsIgnoreCase("§cRetour"))optionsOpen(ffp.p);
		}else{
			byte id;
			switch(item.getType()){
				case SANDSTONE:
					if(item.getDurability()==0){
						id = -1;
					}else{
						id = 0;
					}
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
					ffp.p.sendMessage("§cOops ! Ce block n'a pas été pris en charge ! Contacte un membre du staff");
					return;
			}
			if(id==-1||((ffp.blocks >> id) & 1) == 1){
				ffp.p.sendMessage("§aBlock choisi ! "+item.getType());
				ffp.block = (byte) (id+1);
				ffp.p.closeInventory();
				blockOpen(ffp);
			}else{
				int price = prices[id];
				if(MoneyUtils.getMoney(ffp.p.getUniqueId())<price)ffp.p.sendMessage("§cTu n'a pas assez d'argent pour débloquer ce block !");
				else{
					ffp.p.sendMessage("§aAchat fait avec succès !");
					MoneyUtils.removeMoney(ffp.p.getUniqueId(), price);
					ffp.block = (byte) (id+1);
					ffp.blocks |= 1 << id;
					ffp.p.closeInventory();
					blockOpen(ffp);
				}
			}
		}
	}


	public static boolean customInvOpen(FFAPlayer ffp){
		Inventory inv = Bukkit.createInventory(null, 18, "§7Modifier l'inventaire de départ");


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
		return false;
	}
}
