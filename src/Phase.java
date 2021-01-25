import javafx.scene.canvas.GraphicsContext;

public interface Phase {
    void setup();

    void tearDown();

    void update(long now);

    void draw(GraphicsContext gc);
}
