import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class ImageArray implements ImageFrames {
    private final Image[] images;

    public ImageArray(Image[] images) {
        this.images = images;
    }

    public Image[] getImages() {
        return images;
    }

    @Override
    public int countFrame() {
        return this.images.length;
    }

    @Override
    public void drawFrame(GraphicsContext gc, int frameIndex, double x, double y, double w, double h) {
        gc.drawImage(images[frameIndex], x, y, w, h);
    }

    @Override
    public void drawFrame(GraphicsContext gc, int frameIndex, double x, double y) {
        gc.drawImage(images[frameIndex], x, y);
    }
}
