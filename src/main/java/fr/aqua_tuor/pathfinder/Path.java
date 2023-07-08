package fr.aqua_tuor.pathfinder;

import org.bukkit.Location;

public class Path {

    private Location start;
    private Location end;

    public Path(Location start, Location end) {
        this.start = start;
        this.end = end;
    }

    public Location getStart() {
        return start;
    }

    public Location getEnd() {
        return end;
    }

}
