package fr.entasia.ffarush.deathParticle;

import org.bukkit.Location;

public abstract class ParticleStruct {

    public abstract void update(Location loc);

    public String name;

    public void setName(String name){
        this.name=name;
    }
}
