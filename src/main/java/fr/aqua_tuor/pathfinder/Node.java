package fr.aqua_tuor.pathfinder;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.World;

public class Node {
    private final int id;
    private final int x;
    private final int y;
    private final int z;
    private final World world;
    private boolean isShow;
    private boolean isActive;

    public Node(int id, int x, int y, int z, World world) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.isShow = false;
        this.isActive = false;
    }

    public void show(boolean toShow) {
        if (toShow != isShow) isShow = toShow;

        if (isShow) {
            world.spawnParticle(Particle.REDSTONE, x, y, z, 10, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, 1));
        }
    }

    public void active(boolean toActive) {
        if (!isShow) {
            isActive = false;
            return;
        }

        if (toActive != isActive) isActive = toActive;

        if (isActive) {
            world.spawnParticle(Particle.REDSTONE, x, y, z, 3, 0, 0, 0, 0, new Particle.DustOptions(Color.GREEN, 2));
        }
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public World getWorld() {
        return world;
    }

    public boolean isShow() {
        return isShow;
    }

    public boolean isActive() {
        return isActive;
    }
}
