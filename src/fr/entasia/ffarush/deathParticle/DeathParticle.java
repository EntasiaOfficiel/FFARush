package fr.entasia.ffarush.deathParticle;

import fr.entasia.apis.ServerUtils;
import fr.entasia.apis.other.InstantFirework;
import fr.entasia.ffarush.utils.FFAPlayer;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum DeathParticle {


    GREEN_FIREWORK(501, new ItemStack(Material.FIREWORK), "§7Feu d'artifice vert et rouge", "§7Imaginez les différents morceaux de votre armure exploser, au moins ça impressione", 1000,

            new ParticleStruct() {


                @Override
                public void update(Location loc) {
                    FireworkEffect effects = FireworkEffect.builder().withColor(Color.RED,Color.GREEN).with(FireworkEffect.Type.BALL).flicker(true).build();
                    InstantFirework.explode(loc.add(0,1,0),effects);
                }


            }

    );



    public void update(Location loc, FFAPlayer ffaPlayer){
        for(ParticleStruct s : structs){
            s.update(loc);
        }
    }


    public int id;
    public ItemStack itemStack;
    public int price;
    public String nom;
    public String description;
    public ParticleStruct[] structs;

    DeathParticle(int id, ItemStack item, String nom, String description, int price, ParticleStruct... structs){
        this.price = price;
        this.id = id;
        this.itemStack = item;
        this.nom = nom;
        this.description = description;
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(nom);
        itemStack.setItemMeta(meta);
        this.structs = structs;
    }



}
