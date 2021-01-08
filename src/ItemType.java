public enum ItemType {
    COIN(100),
    KEY(500),
    NONE(0);

    private final int score;

    ItemType(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }
}
