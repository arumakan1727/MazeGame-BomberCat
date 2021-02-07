import javafx.scene.canvas.GraphicsContext;

public class ImageFrameAnimation extends MyTimer {

    public enum Direction {
        /**
         * 順方向。
         * 0 1 2 3, 0 1 2 3, 0 1 2 3, ...
         */
        NORMAL,

        /**
         * 逆方向。
         * 3 2 1 0, 3 2 1 0, 3 2 1 0, ...
         */
        REVERSE,

        /**
         * アニメーション終了毎に方向がトグルする。
         * 0 1 2 3, 2 1 0, 1 2 3, 2 1 0, ...
         */
        ALTERNATE
    }

    private final ImageFrames imageFrames;
    private final Direction animationDirection;
    private int currentFrameIndex;
    private boolean isPlus;
    private boolean isInfinite = true;

    private final long durationNano;

    private long currentFrameCount = 0;

    /**
     * コマ撮りのアニメーションタイマーを生成する。
     * アニメーションは無限再生される。
     *
     * @param imageFrames        コマ撮りの画像群
     * @param durationMilliSec   次のフレームに切り替わるまでのミリ秒
     * @param animationDirection アニメーションの再生方向
     */
    public ImageFrameAnimation(
            ImageFrames imageFrames,
            long durationMilliSec,
            Direction animationDirection
    ) {
        super();
        this.imageFrames = imageFrames;
        this.durationNano = durationMilliSec * 1000000L;  // ms -> ns
        this.animationDirection = animationDirection;

        if (this.animationDirection == Direction.REVERSE) {
            this.currentFrameIndex = this.imageFrames.getFrameCount() - 1;
            isPlus = false;
        } else {
            this.currentFrameIndex = 0;
            isPlus = true;
        }
    }

    public boolean isInfinite() {
        return isInfinite;
    }

    public void setInfinite(boolean infinite) {
        isInfinite = infinite;
    }

    public void draw(GraphicsContext gc, double x, double y, double w, double h) {
        this.imageFrames.drawFrame(gc, this.currentFrameIndex, x, y, w, h);
    }

    public void draw(GraphicsContext gc, double x, double y) {
        this.imageFrames.drawFrame(gc, this.currentFrameIndex, x, y);
    }

    public int getCurrentFrameIndex() {
        return currentFrameIndex;
    }

    @Override
    public void update(long elapsedTimeNano) {
        if (!this.isInfinite && elapsedTimeNano > durationNano * this.imageFrames.getFrameCount()) {
            this.setDead(true);
            return;
        }

        final long preCount = currentFrameCount;
        this.currentFrameCount = elapsedTimeNano / durationNano;

        // 次のフレームへ切り替えるタイミングにまだ到達していない場合は何もしない
        if (preCount == this.currentFrameCount) {
            return;
        }

        if (isPlus) {
            this.currentFrameIndex++;
        } else {
            this.currentFrameIndex--;
        }

        final int n = this.imageFrames.getFrameCount();
        // frameIndex が終端または始端を超えたときの処理
        if (this.currentFrameIndex >= n) {
            if (this.animationDirection == Direction.ALTERNATE) {
                this.currentFrameIndex = n - 2;
                isPlus = false;
            } else {
                this.currentFrameIndex = 0;
            }
        } else if (this.currentFrameIndex < 0) {
            if (this.animationDirection == Direction.ALTERNATE) {
                this.currentFrameIndex = 1;
                isPlus = true;
            } else {
                this.currentFrameIndex = n - 1;
            }
        }
    }
}
