import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Bomb {
    private static final SpriteSheet bombSpriteSheet = new SpriteSheet(new Image("png/bomb-spritesheet.png"), 4, 1);

    public final int col;
    public final int row;

    private final ImageFrameAnimation bombAnimation;

    private boolean hasExploded = false;

    public Bomb(int col, int row) {
        this.col = col;
        this.row = row;
        this.bombAnimation = new ImageFrameAnimation(bombSpriteSheet, 1000, ImageFrameAnimation.Direction.REVERSE);
    }

    public void startCountDown() {
        this.bombAnimation.start();
    }

    public boolean hasExploded() {
        return hasExploded;
    }

    public void draw(GraphicsContext gc, MapView mapView) {
        if (this.bombAnimation.getCurrentFrameIndex() <= 0) {
            this.hasExploded = true;
            this.bombAnimation.stop();
            return;
        }

        final int x = mapView.getCellDrawX(this.col);
        final int y = mapView.getCellDrawY(this.row);
        final int s = mapView.getCellSize();
        bombAnimation.draw(gc, x, y, s, s);
    }
}
