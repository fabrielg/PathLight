package fr.aqua_tuor.pathfinder;

public class Node {
    private final int id;
    private final int x;
    private final int y;
    private final int z;
    private boolean isShow;
    private boolean isActive;

    public Node(int id, int x, int y, int z) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.isShow = false;
        this.isActive = false;
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

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean isShow) {
        this.isShow = isShow;
    }
}
