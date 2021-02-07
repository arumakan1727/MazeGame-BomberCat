import javafx.scene.canvas.GraphicsContext;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class BombExecutor {
    private final List<AbstractBomb> bombList;
    private final DrawableExecutor explosionAnimExecutor;
    private final CellDrawnPositionResolver positionResolver;

    public BombExecutor(CellDrawnPositionResolver positionResolver) {
        this.bombList = new LinkedList<>();
        this.explosionAnimExecutor = new DrawableExecutor();
        this.positionResolver = positionResolver;
    }

    // ボムを登録してカウントダウンを開始する。
    public void registerAndStartCountDown(AbstractBomb bomb) {
        this.bombList.add(bomb);
        bomb.startCountDown();
    }

    // マップ上に存在する未爆発ボムのうち、そのボムインスタンスのクラスが引数 clazz と等しいボムの個数を返す。
    public int countBombByClass(Class<? extends AbstractBomb> clazz) {
        return (int) this.bombList.stream()
                .filter(b -> b.getClass() == clazz)
                .count();
    }

    // マップ上に存在する、全ての未爆発のボムの個数を返す。
    public int countBomb() {
        return this.bombList.size();
    }

    // 指定マスにボムが存在するなら true を返す。
    public boolean isExistsBombAt(int col, int row) {
        return this.bombList.stream().anyMatch(bomb -> bomb.col == col && bomb.row == row);
    }

    // ボムのリストを更新し、必要に応じて爆破処理を実行する。
    public void update(MapData mapData) {
        ListIterator<AbstractBomb> itr = bombList.listIterator();
        while (itr.hasNext()) {
            final AbstractBomb bomb = itr.next();
            if (bomb.hasExploded()) {
                bomb.explode(mapData, this.positionResolver, this.explosionAnimExecutor);
                bomb.playExplosionSE();
                itr.remove();
            }
        }
    }

    // ボムおよび爆発アニメーションを描画する。
    public void draw(GraphicsContext gc) {
        for (final var bomb : this.bombList) {
            bomb.draw(gc, this.positionResolver);
        }
        explosionAnimExecutor.draw(gc);
    }
}
