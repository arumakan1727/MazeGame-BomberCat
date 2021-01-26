import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.function.Consumer;

public class Stopwatch {
    private final Timeline ticker;
    private Duration curTime = Duration.ZERO;
    private Consumer<Duration> tickHandler = null;

    public Stopwatch(Duration tickResolution) {
        this.ticker = new Timeline(
                new KeyFrame(tickResolution,
                        evt -> {
                            curTime = curTime.add(tickResolution);
                            if (tickHandler != null) tickHandler.accept(curTime);
                        })
        );
        this.ticker.setCycleCount(Animation.INDEFINITE);
    }

    public Duration getCurTime() {
        return curTime;
    }

    public void setTickHandler(Consumer<Duration> tickHandler) {
        this.tickHandler = tickHandler;
    }

    public void start() {
        this.ticker.play();
    }

    public void stop() {
        this.ticker.stop();
    }

    public void pause() {
        this.ticker.pause();
    }
}
