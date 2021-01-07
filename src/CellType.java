public enum CellType {
    SPACE(true),
    WALL(false),
    ILLEGAL(false);

    private boolean isMovable;

    CellType(boolean isMovable) {
        this.isMovable = isMovable;
    }

    public boolean isMovable() {
        return this.isMovable;
    }
}
