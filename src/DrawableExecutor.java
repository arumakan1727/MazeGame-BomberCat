import javafx.scene.canvas.GraphicsContext;

import java.util.LinkedList;
import java.util.List;

public class DrawableExecutor {
    private final List<DrawableTransition> drawableTransitionList;

    public DrawableExecutor() {
        this.drawableTransitionList = new LinkedList<>();
    }

    public void registerAndPlay(DrawableTransition drawableTransition) {
        this.drawableTransitionList.add(drawableTransition);
        drawableTransition.play();
    }

    public void draw(GraphicsContext gc) {
        this.drawableTransitionList.removeIf(DrawableTransition::hasFinished);

        for (final var e : this.drawableTransitionList) {
            e.draw(gc);
        }
    }
}
