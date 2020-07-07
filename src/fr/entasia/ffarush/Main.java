package fr.entasia.ffarush;


import fr.entasia.apis.nbt.ItemNBT;
import fr.entasia.apis.nbt.NBTComponent;
import fr.entasia.apis.regionManager.api.RegionManager;
import fr.entasia.apis.sql.SQLConnection;
import fr.entasia.apis.utils.ServerUtils;
import fr.entasia.errors.EntasiaException;
import fr.entasia.ffarush.commands.FFARushCmd;
import fr.entasia.ffarush.commands.FFARushPlCmd;
import fr.entasia.ffarush.listeners.FightListeners;
import fr.entasia.ffarush.listeners.OtherListeners;
import fr.entasia.ffarush.listeners.PowerUpsListeners;
import fr.entasia.ffarush.utils.Task5m;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	public static Main main;
	public static SQLConnection sqlConnection;

	public static void warn(String msg){
		new EntasiaException(msg).printStackTrace();
		main.getLogger().warning(msg);
		ServerUtils.permMsg("logs.warn", "§6Warning FFARush : "+msg);
	}

	public static void loadConfig() {
		FFAUtils.world = Bukkit.getWorld(main.getConfig().getString("world"));
		int j=0;
		for(String i : main.getConfig().getStringList("ffalocs")){
			String[] a = i.split(";");
			Location b = new Location(FFAUtils.world, Double.parseDouble(a[0])+0.5,Double.parseDouble(a[1])+0.5,Double.parseDouble(a[2])+0.5);
			b.setYaw(Float.parseFloat(a[3]));
			FFAUtils.spawnsloc[j] = b;
			j++;
		}
		String[] a = main.getConfig().getString("spawn").split(";");
		FFAUtils.spawn = new Location(FFAUtils.world, Double.parseDouble(a[0])+0.5,Double.parseDouble(a[1])+0.5, Double.parseDouble(a[2])+0.5);
		FFAUtils.damageticks = main.getConfig().getInt(("damageticks"));


	}

	@Override
	public void onEnable() {
		try{
			main = this;

			saveDefaultConfig();
			loadConfig();

			if(getConfig().getBoolean("dev", false)) sqlConnection = new SQLConnection("root");
			else sqlConnection = new SQLConnection("entagames", "playerdata");


			Bukkit.getConsoleSender().sendMessage("Plugin activé !");
			getServer().getPluginManager().registerEvents(new OtherListeners(), this);
			getServer().getPluginManager().registerEvents(new FightListeners(), this);
			getServer().getPluginManager().registerEvents(new PowerUpsListeners(), this);
			getCommand("ffarushpl").setExecutor(new FFARushPlCmd());
			getCommand("ffarush").setExecutor(new FFARushCmd());
			new Task5m().runTaskTimerAsynchronously(this, 1150, 6000); // 5 minutes = 300 secondes = 6000 ticks

			FFAUtils.reg_arena = RegionManager.getRegionByName("ffa_arena");
			FFAUtils.reg_spawn = RegionManager.getRegionByName("ffa_spawn");
			if(FFAUtils.reg_arena==null)throw new Exception("Arena region not found");
			if(FFAUtils.reg_spawn==null)throw new Exception("Spawn region not found");

			FFAUtils.ffaitems[0] = ItemNBT.setNBT(new ItemStack(Material.DIAMOND_SWORD),
					new NBTComponent("{HideFlags:6,AttributeModifiers:[{AttributeName:\"generic.attackDamage\",Name:\"generic.attackDamage\",Amount:5,Operation:0,UUIDLeast:918586,UUIDMost:329936},{AttributeName:\"generic.attackSpeed\",Name:\"generic.attackSpeed\",Amount:10000,Operation:0,UUIDLeast:524170,UUIDMost:178348}],Unbreakable:1,ench:[{id:16,lvl:5}]}"));

			FFAUtils.ffaitems[1] = new ItemStack(Material.IRON_PICKAXE);
			ItemMeta meta = FFAUtils.ffaitems[1].getItemMeta();
			meta.addEnchant(Enchantment.DIG_SPEED, 4, true);
			meta.spigot().setUnbreakable(true);
			meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
			FFAUtils.ffaitems[1].setItemMeta(meta);

			FFAUtils.ffaitems[2] = new ItemStack(Material.BOW);
			meta = FFAUtils.ffaitems[2].getItemMeta();
			meta.setDisplayName("§7BlockBow");
			meta.spigot().setUnbreakable(true);
			meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
			FFAUtils.ffaitems[2].setItemMeta(meta);

			FFAUtils.ffaitems[3] = new ItemStack(Material.GOLDEN_APPLE, 16);

			FFAUtils.ffaitems[4] = new ItemStack(Material.TNT, 16);
			meta = FFAUtils.ffaitems[4].getItemMeta();
			meta.addEnchant(Enchantment.LURE, 1, true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			FFAUtils.ffaitems[4].setItemMeta(meta);

			FFAUtils.ffaitems[5] = new ItemStack(Material.FLINT_AND_STEEL);
			meta = FFAUtils.ffaitems[5].getItemMeta();
			meta.addEnchant(Enchantment.LURE, 1, true);
			meta.spigot().setUnbreakable(true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
			FFAUtils.ffaitems[5].setItemMeta(meta);

			FFAUtils.defaultInv[0] = 0;
			FFAUtils.defaultInv[1] = 1;
			FFAUtils.defaultInv[2] = 2;
			FFAUtils.defaultInv[3] = 3;
			FFAUtils.defaultInv[4] = 4;
			FFAUtils.defaultInv[5] = 5;

			FFAUtils.ffaarmor[0] = new ItemStack(Material.LEATHER_HELMET);
			meta = FFAUtils.ffaarmor[0].getItemMeta();
			meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, false);
			meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
			meta.spigot().setUnbreakable(true);
			FFAUtils.ffaarmor[0].setItemMeta(meta);

			FFAUtils.ffaarmor[2] = new ItemStack(Material.LEATHER_LEGGINGS);
			FFAUtils.ffaarmor[2].setItemMeta(meta);

			FFAUtils.ffaarmor[3] = new ItemStack(Material.LEATHER_BOOTS);
			FFAUtils.ffaarmor[3].setItemMeta(meta);

			FFAUtils.ffaarmor[1] = new ItemStack(Material.IRON_CHESTPLATE);
			meta = FFAUtils.ffaarmor[1].getItemMeta();
			meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, false);
			meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
			meta.spigot().setUnbreakable(true);
			FFAUtils.ffaarmor[1].setItemMeta(meta);

			FFAUtils.ffablocks[0] = new ItemStack(Material.SANDSTONE, 64);
			FFAUtils.ffablocks[1] = new ItemStack(Material.SANDSTONE, 64, (short) 2);
			FFAUtils.ffablocks[2] = new ItemStack(Material.QUARTZ_BLOCK, 64);
			FFAUtils.ffablocks[3] = new ItemStack(Material.NETHER_BRICK, 64);
			FFAUtils.ffablocks[4] = new ItemStack(Material.PURPUR_BLOCK, 64);
			FFAUtils.ffablocks[5] = new ItemStack(Material.END_BRICKS, 64);
			FFAUtils.ffablocks[6] = new ItemStack(Material.BRICK, 64);
			FFAUtils.ffablocks[7] = new ItemStack(Material.PRISMARINE, 64);

			for(int i=0;i<FFAUtils.ffablocks.length;i++){
				meta = FFAUtils.ffablocks[i].getItemMeta();
				meta.addEnchant(Enchantment.LURE, 1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				FFAUtils.ffablocks[i].setItemMeta(meta);
			}

		}catch(Throwable e){
			e.printStackTrace();
			getLogger().severe("LE SERVEUR VA S'ETEINDRE !");
			Bukkit.getServer().shutdown();
		}
	}

}
