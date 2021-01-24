import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;

public class GoldBomb extends NormalBomb {
    private static final SpriteSheet bombSpriteSheet = new SpriteSheet(new Image("png/gold-bomb-spritesheet.png"), 4, 1);
    private static final AudioClip explosionSE = new AudioClip(MapGame.getResourceAsString("sound/explosion.wav"));

    public GoldBomb(int col, int row) {
        super(col, row, GoldBomb.bombSpriteSheet);
    }

    @Override
    public void explode(MapData mapData, CellDrawnPositionResolver positionResolver, DrawableExecutor explosionAnimExecutor) {
        for (int y = this.row - 2; y <= this.row + 2; ++y) {
            for (int x = this.col - 2; x <= this.col + 2; ++x) {
                if (y < 0 || x < 0 || y >= mapData.getHeight() || x >= mapData.getWidth()) continue;
                if (Math.abs(y - this.row) + Math.abs(x - this.col) > 2) continue;
                if (mapData.getCellType(x, y) == CellType.WALL) continue;

                registerExplosion(x, y, positionResolver, explosionAnimExecutor);

                if (mapData.getCellType(x, y) == CellType.BREAKABLE_BLOCK) {
                    mapData.setCellType(x, y, CellType.SPACE);
                    mapData.setItemType(x, y, ItemType.COIN);
                }
            }
        }
    }

    @Override
    public void playExplosionSE() {
        explosionSE.play();
    }

    private static void registerExplosion(
            int col, int row,
            CellDrawnPositionResolver positionResolver,
            DrawableExecutor explosionAnimExecutor
    ) {
        explosionAnimExecutor.registerAndPlay(new ExplosionAnimation(
                positionResolver.getCellDrawnX(col),
                positionResolver.getCellDrawnY(row),
                positionResolver.getCellSize()));
    }
}
