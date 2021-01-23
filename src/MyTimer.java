import javafx.animation.AnimationTimer;

public abstract class MyTimer extends AnimationTimer {
    private static final long UNDEFINED_TIME = -1;

    private long startedTime = UNDEFINED_TIME;
    private boolean isDead = false;

    public MyTimer() {
    }

    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean dead) {
        this.isDead = dead;
    }

    abstract void update(long elapsedTimeNano);

    @Override
    public final void handle(long now) {
        if (this.isDead) return;

        if (this.startedTime == UNDEFINED_TIME) {
            this.startedTime = now;
        }

        long elapsedTimeNano = now - this.startedTime;
        this.update(elapsedTimeNano);
    }
}
