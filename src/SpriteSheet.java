import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class SpriteSheet implements ImageFrames {
    private final Image image;
    private final int ncol;
    private final int frameCount;

    private final double width;
    private final double height;

    public SpriteSheet(Image image, int ncol, int nrow) {
        this.image = image;
        this.ncol = ncol;
        this.frameCount = ncol * nrow;

        this.width = image.getWidth() / ncol;
        this.height = image.getHeight() / nrow;
    }

    @Override
    public int countFrame() {
        return this.frameCount;
    }

    @Override
    public void drawFrame(GraphicsContext gc, int frameIndex, double x, double y, double w, double h) {
        if (frameIndex >= this.frameCount) {
            throw new IllegalArgumentException("`frameIndex` is out of `frameCount`");
        }
        final int row = frameIndex / ncol;
        final int col = frameIndex % ncol;
        gc.drawImage(image, width * col, height * row, width, height, x, y, w, h);
    }

    @Override
    public void drawFrame(GraphicsContext gc, int frameIndex, double x, double y) {
        if (frameIndex >= this.frameCount) {
            throw new IllegalArgumentException("`frameIndex` is out of `frameCount`");
        }
        final int row = frameIndex / ncol;
        final int col = frameIndex % ncol;
        gc.drawImage(image, width * col, height * row, width, height, x, y, width, height);
    }
}
