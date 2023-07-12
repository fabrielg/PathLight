package fr.aqua_tuor.pathfinder.path;

import fr.aqua_tuor.pathfinder.node.Node;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class Path {

    private int id;
    private final Node start;
    private Node end;
    private PathType type;
    private final Color color;
    private double distanceBetweenParticles = 0.2;

    public Path(int id, Node start, Node end, PathType type, Color color) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.type = type;
        this.color = color;
    }

    public Path(int id, Node start, Node end, PathType type, Color color, double distanceBetweenParticles) {
        this.id = id;
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
        System.out.println(start.getLocation().distance(end.getLocation()));
        System.out.println("start: " + start.getLocation());
        System.out.println("end: " + end.getLocation());
        System.out.println("\n");
        Vector vector  = getDirectionBetweenLocations();
        for (double i = 1; i <= start.getLocation().distance(end.getLocation()); i += distanceBetweenParticles) {
            vector.normalize().multiply(i);
            Location location = start.getLocation().clone().add(vector);
            location.getWorld().spawnParticle(Particle.REDSTONE, location, 3, 0, 0, 0, 500, new Particle.DustOptions(color, 1));
        }
    }


    public Vector getDirectionBetweenLocations() {
        Vector from = start.getLocation().toVector();
        Vector to = end.getLocation().toVector();
        return to.subtract(from);
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
