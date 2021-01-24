public abstract class TaskScheduleTimer extends MyTimer {
    private final long delayNano;

    public TaskScheduleTimer(long delayMilli) {
        this.delayNano = delayMilli * 1000000;
    }

    public abstract void task();

    @Override
    final void update(long elapsedTimeNano) {
        if (!this.isDead() && elapsedTimeNano > this.delayNano) {
            this.task();
            this.setDead(true);
            this.stop();
        }
    }
}
