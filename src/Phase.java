import javafx.scene.canvas.GraphicsContext;

/**
 * タイトル画面や迷路画面、ゲームオーバー画面等アプリケーションの流れの一単位を表すインターフェース。
 * 想定される使用例:
 *
 * 1. 外部の処理で setup() が呼び出される
 * ↓
 * 2. 外部の処理から 毎フレーム(一秒あたり約60回)呼び出される (これをゲームループと呼ぶことにする)
 * {
 *     update()
 *     draw()
 * }
 *
 * 3. 外部の処理から tearDown() が呼び出される
 *
 */
public interface Phase {
    /**
     * このフェーズがゲームループに入る前に実行される処理。
     */
    void setup();

    /**
     * このフェーズがゲームループを終えた後に実行される処理。
     */
    void tearDown();

    /**
     * ゲームループで毎フレーム呼び出される処理。
     * draw() の前に呼び出される。
     * @param now 現在時刻(ナノ秒)
     */
    void update(long now);

    /**
     * ゲームループで毎フレーム呼び出される処理。
     * update() の後に呼び出される。
     * @param gc 描画用オブジェクト
     */
    void draw(GraphicsContext gc);
}
