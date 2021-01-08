import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class DoorKeyView {
    private static final String imageFilePath = "png/key-32x32.png";

    private final Image image;

    public DoorKeyView() {
        this.image = new Image(imageFilePath);
    }

    public void draw(GraphicsContext gc, double x, double y, double w, double h) {
        gc.drawImage(this.image, x, y, w, h);
    }
}
