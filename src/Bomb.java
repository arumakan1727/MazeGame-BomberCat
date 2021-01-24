import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;

public class Bomb {
    private static final SpriteSheet bombSpriteSheet = new SpriteSheet(new Image("png/black-bomb-spritesheet.png"), 4, 1);
    private static final AudioClip explosionSE = new AudioClip(MapGame.getResourceAsString("sound/explosion.wav"));

    public final int col;
    public final int row;

    public final ImageFrameAnimation bombAnimation;
    protected Bomb(int col, int row, SpriteSheet bombSpriteSheet) {
        this.col = col;
        this.row = row;
        this.bombAnimation = new ImageFrameAnimation(bombSpriteSheet, 1000, ImageFrameAnimation.Direction.REVERSE);
    }

    public Bomb(int col, int row) {
        this(col, row, Bomb.bombSpriteSheet);
    }

    public void startCountDown() {
        this.bombAnimation.start();
    }

    public boolean hasExploded() {
        return this.bombAnimation.isDead();
    }

    public void draw(GraphicsContext gc, CellDrawnPositionResolver positionResolver) {
        if (this.bombAnimation.getCurrentFrameIndex() <= 0) {
            this.bombAnimation.setDead(true);
            this.bombAnimation.stop();
            return;
        }

        final int x = positionResolver.getCellDrawnX(this.col);
        final int y = positionResolver.getCellDrawnY(this.row);
        final int s = positionResolver.getCellSize();
        bombAnimation.draw(gc, x, y, s, s);
    }

    public void playExplosionSE() {
        explosionSE.play();
    }

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
