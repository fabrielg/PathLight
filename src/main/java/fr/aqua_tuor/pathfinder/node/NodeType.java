package fr.aqua_tuor.pathfinder.node;

import org.bukkit.Color;
import org.bukkit.Particle;

public enum NodeType {

    NORMAL(new Particle.DustOptions(Color.RED, 1)),
    SELECTED(new Particle.DustOptions(Color.LIME, 1.5f)),
    PREVIEW(new Particle.DustOptions(Color.BLACK, 1));

    private Particle.DustOptions dustOptions;

    NodeType(Particle.DustOptions dustOptions) {
        this.dustOptions = dustOptions;
    }

    public Particle.DustOptions getDustOptions() {
        return dustOptions;
    }
}
