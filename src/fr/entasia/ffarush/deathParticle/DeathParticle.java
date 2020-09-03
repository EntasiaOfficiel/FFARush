package fr.entasia.ffarush.deathParticle;

import fr.entasia.apis.other.InstantFirework;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum DeathParticle {


    GREEN_FIREWORK(101, new ItemStack(Material.FIREWORK_ROCKET), "§7Feu d'artifice vert et rouge", "§7Imaginez les différents morceaux de votre armure exploser, au moins ça impressione", 612,
            new ParticleStruct() {
                @Override
                public void update(Location loc) {
                    FireworkEffect effects = FireworkEffect.builder().withColor(Color.RED,Color.GREEN).with(FireworkEffect.Type.BALL).flicker(true).build();
                    InstantFirework.explode(loc.add(0,2.5,0),effects);
                }
            }
    ),


    BLOOD(102, new ItemStack(Material.REDSTONE), "§7Traces de sang", "§7C'est un peu sanguinaire, mais ça a le mérite d'être clair", 300,

            new ParticleStruct() {
                @Override
                public void update(Location loce) {
                    Location loc = loce.add(0,-1,0);
                    loc.getWorld().spawnParticle(Particle.REDSTONE,loc,10,1,1,1,0);
                }
            }

    ),
    RAINBOW(103, new ItemStack(Material.LIGHT_BLUE_DYE),"§7Feu d'artifice multicolore", "§7Alors là ya pas à dire , c'est beau la couleur !", 1300,

            new ParticleStruct() {
                @Override
                public void update(Location loce) {
                    FireworkEffect effects = FireworkEffect.builder().withColor(Color.RED,Color.GREEN,Color.AQUA,Color.GRAY,Color.ORANGE,Color.YELLOW,Color.BLUE,Color.FUCHSIA,Color.LIME,Color.PURPLE,Color.TEAL).withFade(Color.RED,Color.GREEN,Color.AQUA,Color.GRAY,Color.ORANGE,Color.YELLOW,Color.BLUE,Color.FUCHSIA,Color.LIME,Color.PURPLE,Color.TEAL).with(FireworkEffect.Type.BALL_LARGE).flicker(true).trail(false).build();
                    InstantFirework.explode(loce.add(0,2.5,0),effects);
                }
            }
    );



    public void update(Location loc){
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
