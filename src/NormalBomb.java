import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;

public final class NormalBomb extends AbstractBomb {
    private static final SpriteSheet bombSpriteSheet = new SpriteSheet(new Image("png/black-bomb-spritesheet.png"), 4, 1);
    private static final AudioClip explosionSE = new AudioClip(MapGame.getResourceAsString("sound/explosion.wav"));

    public NormalBomb(int col, int row) {
        super(col, row, NormalBomb.bombSpriteSheet);
    }

    @Override
    public void playExplosionSE() {
        NormalBomb.explosionSE.play();
    }

    @Override
    public void explode(MapData mapData, CellDrawnPositionResolver positionResolver, DrawableExecutor explosionAnimExecutor) {
        registerExplosion(this.col, this.row, positionResolver, explosionAnimExecutor);
        final int[] directions = {0, 1, 0, -1};

        for (int i = 0; i < 4; ++i) {
            final int y = row + directions[i];
            final int x = col + directions[i ^ 1];
            if (mapData.getCellType(x, y) == CellType.BREAKABLE_BLOCK) {
                mapData.setCellType(x, y, CellType.SPACE);
            }
            if (mapData.getCellType(x, y) != CellType.WALL) {
                registerExplosion(x, y, positionResolver, explosionAnimExecutor);
            }
        }
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
