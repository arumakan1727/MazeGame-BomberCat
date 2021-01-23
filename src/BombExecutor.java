import javafx.scene.canvas.GraphicsContext;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class BombExecutor {
    private final List<Bomb> bombList;

    public BombExecutor() {
        this.bombList = new LinkedList<>();
    }

    public void register(Bomb bomb) {
        this.bombList.add(bomb);
        bomb.startCountDown();
    }

    public void registerNewBomb(int col, int row) {
        this.register(new Bomb(col, row));
    }

    public int countBomb() {
        return this.bombList.size();
    }

    public boolean isExistsBombAt(int col, int row) {
        return this.bombList.stream().anyMatch(bomb -> bomb.col == col && bomb.row == row);
    }

    public void update(MapData mapData) {
        ListIterator<Bomb> itr = bombList.listIterator();
        while (itr.hasNext()) {
            final Bomb bomb = itr.next();
            if (bomb.hasExploded()) {
                breakFourNeighborBlock(mapData, bomb.col, bomb.row);
                itr.remove();
            }
        }
    }

    public void draw(GraphicsContext gc, MapView mapView) {
        for (final Bomb bomb : this.bombList) {
            bomb.draw(gc, mapView);
        }
    }

    private static void breakFourNeighborBlock(MapData mapData, int col, int row) {
        final int[] directions = {0, 1, 0, -1};

        for (int i = 0; i < 4; ++i) {
            final int dy = directions[i];
            final int dx = directions[i ^ 1];
            if (mapData.getCellType(col + dx, row + dy) == CellType.BREAKABLE_BLOCK) {
                mapData.setCellType(col + dx, row + dy, CellType.SPACE);
            }
        }
    }
}
