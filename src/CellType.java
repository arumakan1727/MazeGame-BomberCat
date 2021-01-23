public enum CellType {
    SPACE(true),
    WALL(false),
    BREAKABLE_BLOCK(false),
    ILLEGAL(false);

    private boolean isMovable;

    CellType(boolean isMovable) {
        this.isMovable = isMovable;
    }

    public boolean isMovable() {
        return this.isMovable;
    }
}
