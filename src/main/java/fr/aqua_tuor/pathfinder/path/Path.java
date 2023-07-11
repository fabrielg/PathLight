package fr.aqua_tuor.pathfinder.path;

import fr.aqua_tuor.pathfinder.node.Node;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

public class Path {

    private final Node start;
    private final Node end;
    private PathType type;
    private final Color color;

    public Path(Node start, Node end, PathType type, Color color) {
        this.start = start;
        this.end = end;
        this.type = type;
        this.color = color;
    }

    public void drawPath() {
        switch (type) {
            case LINE:
                drawLine();
                break;
            case ARC:
            case AROUND:
            case SNAKE:
                break;
        }
    }

    public void drawLine() {
        double distance = getDistance();
        double x = start.getX();
        double y = start.getY();
        double z = start.getZ();
        double x2 = end.getX();
        double y2 = end.getY();
        double z2 = end.getZ();
        double deltaX = (x2 - x) / distance;
        double deltaY = (y2 - y) / distance;
        double deltaZ = (z2 - z) / distance;

        for (int i = 0; i < distance; i++) {
            x += deltaX;
            y += deltaY;
            z += deltaZ;
            start.getWorld().spawnParticle(Particle.REDSTONE, x, y, z, 0, 0, 0, 0, 1, new Particle.DustOptions(color, 1));
        }

    }


    public double getDistance() {
        Location startLoc = start.getLocation();
        Location endLoc = end.getLocation();
        return startLoc.distance(endLoc);
    }


    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }

    public PathType getType() {
        return type;
    }

    public void setType(PathType type) {
        this.type = type;
    }

    public Color getColor() {
        return color;
    }

}
