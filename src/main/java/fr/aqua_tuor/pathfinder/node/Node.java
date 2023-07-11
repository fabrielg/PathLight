package fr.aqua_tuor.pathfinder.node;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Node {
    private final int id;
    private final double x;
    private final double y;
    private final double z;
    private final World world;
    private boolean isShow;
    private NodeType type;

    public Node(int id, double x, double y, double z, World world) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.isShow = false;
        this.type = NodeType.NORMAL;
    }

    public void show(boolean toShow) {
        if (toShow != isShow) isShow = toShow;

        if (isShow) {
            world.spawnParticle(Particle.REDSTONE, x, y, z, 10, 0, 0, 0, 500, type.getDustOptions());
        }
    }

    public static void showCurrent(Player player) {
        Block block = player.getTargetBlock(null, 5);
        if (block == null || block.getType().name().contains("AIR")) return;

        double x = block.getX() + 0.5;
        double y = block.getY() + 1.5;
        double z = block.getZ() + 0.5;
        World world = block.getWorld();
        world.spawnParticle(Particle.REDSTONE, x, y, z, 10, 0, 0, 0, 0, new Particle.DustOptions(Color.BLACK, 1));
    }

    public Location getLocation() {
        return new Location(world, x, y, z);
    }

    public void setType(NodeType type) {
        this.type = type;
    }
    public NodeType getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public World getWorld() {
        return world;
    }

    public boolean isShow() {
        return isShow;
    }

}
