import javafx.scene.canvas.GraphicsContext;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class BombExecutor {
    private final List<NormalBomb> bombList;
    private final DrawableExecutor explosionAnimExecutor;
    private final CellDrawnPositionResolver positionResolver;

    public BombExecutor(CellDrawnPositionResolver positionResolver) {
        this.bombList = new LinkedList<>();
        this.explosionAnimExecutor = new DrawableExecutor();
        this.positionResolver = positionResolver;
    }

    public void register(NormalBomb bomb) {
        this.bombList.add(bomb);
        bomb.startCountDown();
    }

    public int countBomb() {
        return this.bombList.size();
    }

    public boolean isExistsBombAt(int col, int row) {
        return this.bombList.stream().anyMatch(bomb -> bomb.col == col && bomb.row == row);
    }

    public void update(MapData mapData) {
        ListIterator<NormalBomb> itr = bombList.listIterator();
        while (itr.hasNext()) {
            final NormalBomb bomb = itr.next();
            if (bomb.hasExploded()) {
                bomb.explode(mapData, this.positionResolver, this.explosionAnimExecutor);
                bomb.playExplosionSE();
                itr.remove();
            }
        }
    }

    public void draw(GraphicsContext gc) {
        for (final var bomb : this.bombList) {
            bomb.draw(gc, this.positionResolver);
        }
        explosionAnimExecutor.draw(gc);
    }
}
