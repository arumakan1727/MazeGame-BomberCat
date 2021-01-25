import javafx.animation.Transition;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class TimeGageImageViewButton {
    private final ImageViewButton button;
    private double gage = 0.0;
    private Transition gageTransition;

    public TimeGageImageViewButton(ImageViewButton button) {
        this.button = button;
        this.button.setButtonEnabled(false);
    }

    public ImageViewButton getButton() {
        return button;
    }

    public void gageStartFromEmpty(Duration durationUntilFull) {
        this.gage = 0.0;
        this.button.setButtonEnabled(false);

        this.gageTransition = new Transition() {
            {
                setCycleDuration(durationUntilFull);
            }

            @Override
            protected void interpolate(double frac) {
                gage = frac;
            }
        };

        this.gageTransition.setOnFinished(evt -> {
            setGage(1.0);
        });

        this.gageTransition.play();
    }

    public void pause() {
        this.gageTransition.pause();
    }

    public void draw(GraphicsContext gc) {
        if (button.isButtonEnabled()) return;

        final double w = button.getImage().getWidth();
        final double h = button.getImage().getHeight() * gage;
        final double x = button.getLayoutX();
        final double y = button.getLayoutY() + button.getImage().getHeight() - h;
        gc.setFill(Color.DARKORANGE);
        gc.fillRect(x, y, w, h);
    }

    public double getGage() {
        return gage;
    }

    public void setGage(double gage) {
        if (gage < 0 || gage > 1.0) {
            throw new IllegalArgumentException("Gage value must be between [0.0, 1.0]");
        }

        this.gage = gage;
        this.button.setButtonEnabled(this.gage == 1.0);
        this.gageTransition.stop();
    }

    public double getWidth() {
        return button.getImage().getWidth();
    }

    public double getHeight() {
        return button.getImage().getHeight();
    }
}
