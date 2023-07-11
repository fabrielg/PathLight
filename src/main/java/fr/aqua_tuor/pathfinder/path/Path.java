package fr.aqua_tuor.pathfinder.path;

import fr.aqua_tuor.pathfinder.node.Node;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class Path {

    private final Node start;
    private Node end;
    private PathType type;
    private final Color color;
    private double distanceBetweenParticles = 0.5;

    public Path(Node start, Node end, PathType type, Color color) {
        this.start = start;
        this.end = end;
        this.type = type;
        this.color = color;
    }

    public Path(Node start, Node end, PathType type, Color color, double distanceBetweenParticles) {
        this.start = start;
        this.end = end;
        this.type = type;
        this.color = color;
        this.distanceBetweenParticles = distanceBetweenParticles;
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
        Vector vector  = getDirectionBetweenLocations();
        for (double i = 1; i <= start.getLocation().distance(end.getLocation()); i += distanceBetweenParticles) {
            vector.multiply(i);
            start.getLocation().add(vector);
            start.getLocation().getWorld().spawnParticle(Particle.REDSTONE, start.getLocation(), 1, 0, 0, 0, 0, new Particle.DustOptions(color, 1));
            start.getLocation().subtract(vector);
            vector.normalize();
        }
    }


    public Vector getDirectionBetweenLocations() {
        Vector from = start.getLocation().toVector();
        Vector to = end.getLocation().toVector();
        return to.subtract(from);
    }


    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }

    public void setEnd(Node end) {
        this.end = end;
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

    public double getDistanceBetweenParticles() {
        return distanceBetweenParticles;
    }

    public void setDistanceBetweenParticles(double distanceBetweenParticles) {
        this.distanceBetweenParticles = distanceBetweenParticles;
    }

}
