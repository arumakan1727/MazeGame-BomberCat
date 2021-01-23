import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;

public class ImageFrameAnimation extends AnimationTimer {

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

    private final long duration;
    private long startTime = 0;

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
        this.imageFrames = imageFrames;
        this.duration = durationMilliSec * 1000000L;  // ms -> ns
        this.animationDirection = animationDirection;

        if (this.animationDirection == Direction.REVERSE) {
            this.currentFrameIndex = this.imageFrames.countFrame() - 1;
            isPlus = false;
        } else {
            this.currentFrameIndex = 0;
            isPlus = true;
        }
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
    public void handle(long now) {
        if (this.startTime == 0) {
            this.startTime = now;
        }

        final long preCount = currentFrameCount;
        this.currentFrameCount = (now - startTime) / duration;

        // 次のフレームへ切り替えるタイミングにまだ到達していない場合は何もしない
        if (preCount == this.currentFrameCount) {
            return;
        }

        if (isPlus) {
            this.currentFrameIndex++;
        } else {
            this.currentFrameIndex--;
        }

        final int n = this.imageFrames.countFrame();
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
