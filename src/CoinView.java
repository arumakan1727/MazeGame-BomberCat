import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class CoinView {
    private static final String imageFilePath = "png/coin_01.png";

    private final Image image;

    public CoinView() {
        this.image = new Image(imageFilePath);
    }

    public void draw(GraphicsContext gc, double x, double y, double w, double h) {
        gc.drawImage(this.image, x, y, w, h);
    }
}
