import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class BombExecutor {
    private final List<Bomb> bombList;
    private final List<Explosion> explosionList;

    public BombExecutor() {
        this.bombList = new LinkedList<>();
        this.explosionList = new LinkedList<>();
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
                breakFourNeighborBlock(mapData, bomb.col, bomb.row, this.explosionList);
                this.explosionList.add(new Explosion(bomb.col, bomb.row));
                itr.remove();
            }
        }

        explosionList.removeIf(Explosion::hasFinished);
    }

    public void draw(GraphicsContext gc, MapView mapView) {
        for (final var bomb : this.bombList) {
            bomb.draw(gc, mapView);
        }

        for (final var explosion : this.explosionList) {
            explosion.draw(gc, mapView);
        }
    }

    private static void breakFourNeighborBlock(MapData mapData, int col, int row, List<Explosion> explosionList) {
        final int[] directions = {0, 1, 0, -1};

        for (int i = 0; i < 4; ++i) {
            final int dy = directions[i];
            final int dx = directions[i ^ 1];
            if (mapData.getCellType(col + dx, row + dy) == CellType.BREAKABLE_BLOCK) {
                mapData.setCellType(col + dx, row + dy, CellType.SPACE);
                explosionList.add(new Explosion(col + dx, row + dy));
            }
        }
    }
}

class Explosion {
    private static final SpriteSheet spriteSheet = new SpriteSheet(new Image("png/explosion-animation.png"), 6, 1);

    public final int row;
    public final int col;
    public final ImageFrameAnimation animation;

    Explosion(int col, int row) {
        this.row = row;
        this.col = col;
        this.animation = new ImageFrameAnimation(
                spriteSheet, 70, ImageFrameAnimation.Direction.NORMAL);
        this.animation.setInfinite(false);
        this.animation.start();
    }

    public boolean hasFinished() {
        return this.animation.isDead();
    }

    public void draw(GraphicsContext gc, MapView mapView) {
        final int x = mapView.getCellDrawX(this.col);
        final int y = mapView.getCellDrawY(this.row);
        final int s = mapView.getCellSize();
        this.animation.draw(gc, x, y, s, s);
    }
}