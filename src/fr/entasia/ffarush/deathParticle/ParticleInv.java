package fr.entasia.ffarush.deathParticle;

import fr.entasia.apis.menus.MenuClickEvent;
import fr.entasia.apis.menus.MenuCreator;
import fr.entasia.cosmetiques.Main;
import fr.entasia.cosmetiques.utils.Utils;
import fr.entasia.egtools.utils.MoneyUtils;
import fr.entasia.ffarush.FFAUtils;
import fr.entasia.ffarush.utils.FFAPlayer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class ParticleInv {

    public static MenuCreator deathParticle = new MenuCreator() {
        @Override
        public void onMenuClick(MenuClickEvent e) {
            for(DeathParticle c : DeathParticle.values()){
                if(c.itemStack.getItemMeta().getDisplayName().equals(e.item.getItemMeta().getDisplayName())){
                    if(Utils.haveCosm(c.id,e.player.getUniqueId(), false)){
                        e.player.sendMessage("§7Vous avez activé la particule de mort "+c.nom);
                        FFAPlayer ffp = FFAUtils.playerCache.get(e.player.getUniqueId());
                        if(ffp==null){
                            e.player.sendMessage("§cTon profil FFARush est mal chargé ! Contacte un membre du staff");
                            e.player.closeInventory();
                        }
                        ffp.deathParticle = c;
                        e.player.closeInventory();
                    } else{
                        e.player.closeInventory();
                        openParticleBuyMenu(e.player,c);

                    }
                    return;
                }
            }
            if(e.item.getItemMeta().getDisplayName().equals("§cRetour")){
                e.player.closeInventory();
            }else if(e.item.getItemMeta().getDisplayName().equals("§cEnlever les particules de mort")){
                FFAPlayer ffp = FFAUtils.playerCache.get(e.player.getUniqueId());
                if(ffp==null){
                    e.player.sendMessage("§cTon profil FFARush est mal chargé ! Contacte un membre du staff");
                    e.player.closeInventory();
                }
                if(ffp.deathParticle==null)e.player.sendMessage("§7Vous n'avez pas de particule de mort activée !");
                else{
                    ffp.deathParticle = null;
                    e.player.sendMessage("§7Vous avez enlevé votre particule de mort");
                }
            }
            e.player.closeInventory();
        }
    };

    public static void deathParticleOpenMenu(Player p){
        int cosm = 0;
        for(DeathParticle c : DeathParticle.values()){
            cosm++;
        }
        int slot = cosm*2;
        while( slot%9!=0){
            slot++;
        }
        Inventory inv = deathParticle.createInv(slot/9,"§7Menu particule de mort");
        int nextSlot = 1;
        for(DeathParticle c : DeathParticle.values()){
            ItemStack item = c.itemStack.clone();
            ItemMeta meta = item.getItemMeta();
            ArrayList<String> lore = new ArrayList<>(Collections.singletonList(c.description));
            FFAPlayer cp = FFAUtils.playerCache.get(p.getUniqueId());
            if(cp==null){
                p.sendMessage("§cTon profil FFARush est mal chargé ! Contacte un membre du staff");
                p.closeInventory();
            }

            if(cp.deathParticle!=null && cp.deathParticle.equals(c)){
                lore.add("§6Cette particule de mort est déjà activée");
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.addEnchant(Enchantment.LURE, 1, false);
            }else if(Utils.haveCosm(c.id,p.getUniqueId(), false)){
                lore.add("§aVous possédez cette particule de mort");
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.addEnchant(Enchantment.LURE, 1, false);
            } else{
                lore.add("§cVous n'avez pas encore débloqué cette particule de mort");
            }

            if(item.getType().equals(Material.POTION)){
                meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            }
            meta.setDisplayName(c.nom);
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(nextSlot, item);
            nextSlot= nextSlot+2;

        }
        ItemStack item = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta meta=  item.getItemMeta();
        meta.setDisplayName("§cEnlever les particules de mort");
        item.setItemMeta(meta);
        inv.setItem(slot-1,item);

        p.openInventory(inv);
    }


    public static void openParticleBuyMenu(Player p, DeathParticle c){
        Inventory inv = buyParticleMenu.createInv(2,"§7Achat d'une particule de mort", c);

        ItemStack cosmetique=c.itemStack;
        inv.setItem(4,cosmetique);
        ItemStack achat= new ItemStack(Material.STAINED_GLASS_PANE,1, (byte) 5);
        ItemMeta achatMeta= achat.getItemMeta();
        achatMeta.setDisplayName("§2Acheter");
        achatMeta.setLore(Collections.singletonList("§2Cout : "+c.price +" coins"));
        achat.setItemMeta(achatMeta);


        ItemStack refuser= new ItemStack(Material.STAINED_GLASS_PANE,1, (byte) 14);
        ItemMeta refuserMeta= refuser.getItemMeta();
        refuserMeta.setDisplayName("§cAnnuler");
        refuserMeta.setLore(Collections.singletonList("§cAnnuler l'achat"));
        refuser.setItemMeta(refuserMeta);

        inv.setItem(11,achat);
        inv.setItem(15,refuser);

        p.openInventory(inv);
    }


    public static MenuCreator buyParticleMenu = new MenuCreator() {

        @Override
        public void onMenuClick(MenuClickEvent e){
            if(e.item.getItemMeta().getDisplayName().equalsIgnoreCase("§cAnnuler")){


                e.player.closeInventory();
                e.player.sendMessage("§cAchat annulé");
                return;
            }
            UUID uuid = e.player.getUniqueId();
            DeathParticle c = (DeathParticle) e.data;
            if(MoneyUtils.getMoney(uuid)>= c.price){
                MoneyUtils.removeMoney(uuid, c.price);
                e.player.sendMessage("§2Vous avez acheté la particule de mort "+c.nom);
                e.player.closeInventory();
                Main.sql.checkConnect();
                PreparedStatement ps;
                try {
                    ps = Main.sql.connection.prepareStatement("INSERT INTO particles(uuid,id) VALUES (?,?)");
                    ps.setInt(2, c.id);
                    ps.setString(1, e.player.getUniqueId().toString());
                    ps.execute();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                FFAPlayer cp = FFAUtils.playerCache.get(e.player.getUniqueId());
                if(cp==null){
                    e.player.sendMessage("§cTon profil FFARush est mal chargé ! Contacte un membre du staff");
                    e.player.closeInventory();
                }
                cp.deathParticle=c;
            } else {
                e.player.sendMessage("§4Vous n'avez pas assez d'argent pour acheter cette particule de mort");
                e.player.closeInventory();
            }

        }
    };
}
