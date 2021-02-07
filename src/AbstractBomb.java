import javafx.scene.canvas.GraphicsContext;

public abstract class AbstractBomb {
    public final int col;
    public final int row;
    public final ImageFrameAnimation bombAnimation;

    public abstract void playExplosionSE();

    public abstract void explode(MapData mapData, CellDrawnPositionResolver positionResolver, DrawableExecutor explosionAnimExecutor);

    public AbstractBomb(int col, int row, SpriteSheet bombSpriteSheet) {
        this.col = col;
        this.row = row;
        this.bombAnimation = new ImageFrameAnimation(bombSpriteSheet, 1000, ImageFrameAnimation.Direction.REVERSE);
    }

    public void startCountDown() {
        this.bombAnimation.start();
    }

    public boolean hasExploded() {
        return this.bombAnimation.isDead();
    }

    public void draw(GraphicsContext gc, CellDrawnPositionResolver positionResolver) {
        if (this.bombAnimation.getCurrentFrameIndex() <= 0) {
            this.bombAnimation.setDead(true);
            this.bombAnimation.stop();
            return;
        }

        final int x = positionResolver.getCellDrawnX(this.col);
        final int y = positionResolver.getCellDrawnY(this.row);
        final int s = positionResolver.getCellSize();
        bombAnimation.draw(gc, x, y, s, s);
    }
}
