import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class TextFloatUpAnimation implements DrawableTransition {

    private final String text;
    private double opacity;
    private double x;
    private double y;
    private final Font font;
    private final Paint fillColor;
    private final Paint strokeColor;

    private final Transition transition;
    private boolean hasPlayStarted = false;

    public TextFloatUpAnimation(String text, double sx, double sy, Font font, Paint fillColor, Paint strokeColor, Duration duration, double upDistance) {
        this.text = text;
        this.opacity = 1.0;
        this.x = sx;
        this.y = sy;
        this.font = font;
        this.fillColor = fillColor;
        this.strokeColor = strokeColor;

        this.transition = new Transition() {
            {
                setCycleDuration(duration);
            }

            @Override
            protected void interpolate(double frac) {
                TextFloatUpAnimation.this.opacity = 1.0 - frac;
                TextFloatUpAnimation.this.y = sy - upDistance * frac;
            }
        };
    }

    @Override
    public void play() {
        this.transition.play();
        this.hasPlayStarted = true;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();
        gc.setGlobalAlpha(this.opacity);
        gc.setFont(this.font);
        gc.setFill(this.fillColor);
        gc.setStroke(this.strokeColor);
        gc.setLineWidth(2);
        gc.fillText(this.text, this.x, this.y);
        gc.strokeText(this.text, this.x, this.y);
        gc.restore();
    }

    @Override
    public boolean hasFinished() {
        return this.hasPlayStarted && this.transition.getStatus() == Animation.Status.STOPPED;
    }
}
