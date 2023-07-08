package fr.aqua_tuor.pathfinder;

import org.bukkit.Color;
import org.bukkit.Location;

public class Path {

    private Location start;
    private Location end;
    private PathType type;
    private Color color;

    public Path(Location start, Location end, PathType type, Color color) {
        this.start = start;
        this.end = end;
        this.type = type;
        this.color = color;
    }

    public Location getStart() {
        return start;
    }

    public Location getEnd() {
        return end;
    }

    public PathType getType() {
        return type;
    }

    public Color getColor() {
        return color;
    }

}
