import javafx.animation.AnimationTimer;

public abstract class MyTimer extends AnimationTimer {
    private static final long UNDEFINED_TIME = -1;

    private long startedTime = UNDEFINED_TIME;

    public MyTimer() {
    }

    abstract void update(long elapsedTimeNano);

    @Override
    public final void handle(long now) {
        if (this.startedTime == UNDEFINED_TIME) {
            this.startedTime = now;
        }

        long elapsedTimeNano = now - this.startedTime;
        this.update(elapsedTimeNano);
    }
}
