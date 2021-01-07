import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class MoveChara {
    public static final int TYPE_DOWN = 0;
    public static final int TYPE_LEFT = 1;
    public static final int TYPE_RIGHT = 2;
    public static final int TYPE_UP = 3;

    private static final String[] directionStrings = {"Down", "Left", "Right", "Up"};
    private static final String[] animationNumbers = {"1", "2", "3"};
    private static final String pngPathPrefix = "png/cat";
    private static final String pngPathSuffix = ".png";

    private int posCol;
    private int posRow;
    private int charaDirection;

    private final MapData mapData;

    private final ImageFrameAnimation[] charaAnimations;

    public MoveChara(int startCol, int startRow, MapData mapData) {
        this.posCol = startCol;
        this.posRow = startRow;
        this.mapData = mapData;

        this.charaAnimations = new ImageFrameAnimation[4];

        for (int dir = 0; dir < 4; dir++) {
            Image[] charaImages = new Image[3];
            for (int j = 0; j < 3; j++) {
                charaImages[j] = new Image(pngPathPrefix + directionStrings[dir] + animationNumbers[j] + pngPathSuffix);
            }
            this.charaAnimations[dir] = new ImageFrameAnimation(
                    new ImageArray(charaImages),
                    500,
                    ImageFrameAnimation.Direction.ALTERNATE
            );
        }

        this.setCharaDirection(TYPE_RIGHT);
    }

    public void draw(GraphicsContext gc, MapView mapView) {
        final int x = mapView.getCellDrawX(this.posCol);
        final int y = mapView.getCellDrawY(this.posRow);
        final int cellSize = mapView.getCellSize();
        this.charaAnimations[this.charaDirection].draw(gc, x, y, cellSize, cellSize);
    }

    // set the cat's image of a direction
    public void setCharaDirection(int cd) {
        this.charaDirection = cd;
        for (int dir = 0; dir < 4; dir++) {
            if (dir == this.charaDirection) {
                this.charaAnimations[dir].start();
            } else {
                this.charaAnimations[dir].stop();
            }
        }
    }

    public int getCharaDirection() {
        return this.charaDirection;
    }

    // check the place where the cat will go
    public boolean isMovableBy(int dx, int dy) {
        return mapData.getCellType(posCol + dx, posRow + dy).isMovable();
    }

    // move the cat
    public boolean moveBy(int dx, int dy) {
        if (isMovableBy(dx, dy)) {
            posCol += dx;
            posRow += dy;
            return true;
        } else {
            return false;
        }
    }

    // getter: column-position of the cat
    public int getPosCol() {
        return posCol;
    }

    // getter: row-position of the cat
    public int getPosRow() {
        return posRow;
    }
}
