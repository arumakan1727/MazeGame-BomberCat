import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

public class TitlePhase implements Phase {
    private final MapGameScene scene;
    private final ImageView titleLogo;
    private final ImageViewButton btnPlay;
    private final ImageViewButton btnExit;
    private final AudioClip bgm;

    public TitlePhase(MapGameScene scene) {
        this.scene = scene;
        this.titleLogo = new ImageView("png/title-logo.png");
        this.btnPlay = new ImageViewButton(new Image("png/button-play.png"), scene);
        this.btnExit = new ImageViewButton(new Image("png/button-exit.png"), scene);
        this.bgm = new AudioClip(MapGame.getResourceAsString("sound/distantfuture.mp3"));
        this.bgm.setCycleCount(AudioClip.INDEFINITE);
    }

    @Override
    public void setup() {
        setButtonsClickHandler();
        alignTitleLogo();
        alignButtons();
        scene.getOtherComponents().getChildren().addAll(titleLogo, btnPlay, btnExit);
        bgm.play();
        setAnimationToTitleLogo();
    }

    @Override
    public void tearDown() {
        this.scene.getOtherComponents().getChildren().clear();
    }

    @Override
    public void update(long now) {

    }

    @Override
    public void draw(GraphicsContext gc) {

    }

    private void setButtonsClickHandler() {
        btnPlay.setOnMouseClicked(evt -> {
            bgm.stop();
            final Phase nextPhase = new MazePhase(scene);
            scene.changePhase(nextPhase);
        });

        btnExit.setOnMouseClicked(evt -> {
            bgm.stop();
            Platform.exit();
        });
    }

    private void alignTitleLogo() {
        titleLogo.setLayoutX((scene.getWidth() - titleLogo.getImage().getWidth()) / 2);
        titleLogo.setLayoutY(scene.getHeight() * 0.2);
    }

    private void alignButtons() {
        btnPlay.setLayoutX((scene.getWidth() - btnPlay.getImage().getWidth()) / 2);
        btnPlay.setLayoutY(scene.getHeight() / 2);
        btnExit.setLayoutX((scene.getWidth() - btnPlay.getImage().getWidth()) / 2);
        btnExit.setLayoutY(btnPlay.getLayoutY() + btnPlay.getImage().getHeight() * 2);
    }

    private void setAnimationToTitleLogo() {
        final double fromSize = 0.05;
        final double toSize = 0.8;

        // titleLogo のサイズを 小 → 大へ、弾力を持った変化でアニメーションする
        final Transition elasticTransition = new Transition() {
            {
                setCycleDuration(Duration.millis(2500));
            }

            @Override
            protected void interpolate(double frac) {
                final double k = Easing.easeOutElastic(frac);
                final double scale = fromSize + (toSize - fromSize) * k;
                titleLogo.setScaleX(scale);
                titleLogo.setScaleY(scale);
            }
        };

        // titleLogo が上下に往復するアニメーション
        final Transition roundTripY = new Transition() {
            final double normalY = titleLogo.getLayoutY();

            {
                setCycleDuration(Duration.millis(2000));
                setCycleCount(INDEFINITE);
                setAutoReverse(true);
                setDelay(Duration.millis(500));
            }

            @Override
            protected void interpolate(double frac) {
                titleLogo.setLayoutY(normalY - frac * 15);
            }
        };

        elasticTransition.setOnFinished(evt -> { roundTripY.play(); });
        elasticTransition.play();
    }
}
