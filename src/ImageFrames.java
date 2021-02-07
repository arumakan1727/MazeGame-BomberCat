import javafx.scene.canvas.GraphicsContext;

public interface ImageFrames {
    int getFrameCount();

    void drawFrame(GraphicsContext gc, int frameIndex, double x, double y, double w, double h);

    void drawFrame(GraphicsContext gc, int frameIndex, double x, double y);
}
