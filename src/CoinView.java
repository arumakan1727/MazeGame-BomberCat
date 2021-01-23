import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class CoinView {
    private static final String imageFilePath = "png/full-coins.png";

    private final ImageFrameAnimation coinRotateAnimation;

    public CoinView() {
        Image img = new Image(imageFilePath);
        SpriteSheet coinSpriteSheet = new SpriteSheet(img, 8, 1);
        this.coinRotateAnimation = new ImageFrameAnimation(coinSpriteSheet, 100, ImageFrameAnimation.Direction.NORMAL);
        this.coinRotateAnimation.start();
    }

    public void draw(GraphicsContext gc, double x, double y, double w, double h) {
        this.coinRotateAnimation.draw(gc, x, y, w, h);
    }
}
