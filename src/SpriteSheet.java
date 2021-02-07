import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * コマ撮りの画像が連続して配置された、一枚の画像を表すクラス。
 */
public class SpriteSheet implements ImageFrames {
    private final Image image;

    // 横方向のフレーム列数
    private final int ncol;

    // 全フレーム枚数
    private final int frameCount;

    // フレームひとつあたりの横幅
    private final double frameWidth;

    // フレームひとつあたりの高さ
    private final double frameHeight;

    public SpriteSheet(Image image, int ncol, int nrow) {
        this.image = image;
        this.ncol = ncol;
        this.frameCount = ncol * nrow;

        this.frameWidth = image.getWidth() / ncol;
        this.frameHeight = image.getHeight() / nrow;
    }

    @Override
    public int getFrameCount() {
        return this.frameCount;
    }

    @Override
    public void drawFrame(GraphicsContext gc, int frameIndex, double x, double y, double w, double h) {
        if (frameIndex >= this.frameCount) {
            throw new IndexOutOfBoundsException("`frameIndex` is out of `frameCount`");
        }
        final int row = frameIndex / ncol;
        final int col = frameIndex % ncol;
        gc.drawImage(image, frameWidth * col, frameHeight * row, frameWidth, frameHeight, x, y, w, h);
    }

    @Override
    public void drawFrame(GraphicsContext gc, int frameIndex, double x, double y) {
        if (frameIndex >= this.frameCount) {
            throw new IllegalArgumentException("`frameIndex` is out of `frameCount`");
        }
        final int row = frameIndex / ncol;
        final int col = frameIndex % ncol;
        gc.drawImage(image, frameWidth * col, frameHeight * row, frameWidth, frameHeight, x, y, frameWidth, frameHeight);
    }
}
