import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class ExplosionAnimation implements DrawableTransition {
    private static final SpriteSheet spriteSheet = new SpriteSheet(new Image("png/explosion-animation.png"), 6, 1);

    public final int x;
    public final int y;
    public final int cellSize;
    public final ImageFrameAnimation animation;

    ExplosionAnimation(int x, int y, int cellSize) {
        this.x = x;
        this.y = y;
        this.cellSize = cellSize;
        this.animation = new ImageFrameAnimation(
                spriteSheet, 70, ImageFrameAnimation.Direction.NORMAL);
        this.animation.setInfinite(false);
    }

    @Override
    public void draw(GraphicsContext gc) {
        this.animation.draw(gc, x, y, cellSize, cellSize);
    }

    @Override
    public boolean hasFinished() {
        return this.animation.isDead();
    }

    @Override
    public void play() {
        this.animation.start();
    }
}
