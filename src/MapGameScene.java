import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

public class MapGameScene extends Scene {
    private static final int WIDTH = 860;
    private static final int HEIGHT = 640;

    private final Canvas canvas;
    private final Group otherComponents;
    private Phase phase;

    private MapGameScene(Pane root) {
        super(root);
        this.canvas = new Canvas(WIDTH, HEIGHT);
        this.otherComponents = new Group();
        root.getChildren().add(this.canvas);
        root.getChildren().add(this.otherComponents);
    }

    public static MapGameScene create() {
        Pane pane = new Pane();
        MapGameScene scene = new MapGameScene(pane);
        return scene;
    }

    public void startGame(Phase initialPhase) {
        final GraphicsContext gc = this.canvas.getGraphicsContext2D();
        this.phase = initialPhase;

        AnimationTimer gameLoopTimer = new AnimationTimer() {
            @Override
            public void handle(long curTime) {
                phase.update(curTime);
                phase.draw(gc);
            }
        };

        this.phase.setup(this);
        gameLoopTimer.start();
    }

    public void changePhase(Phase nextPhase) {
        this.phase.tearDown();
        nextPhase.setup(this);
        this.phase = nextPhase;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public Group getOtherComponents() {
        return otherComponents;
    }
}
