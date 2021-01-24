import javafx.animation.Transition;
import javafx.scene.canvas.GraphicsContext;

public interface DrawableTransition {

    void draw(GraphicsContext gc);

    boolean hasFinished();

    void play();
}
