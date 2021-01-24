import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.util.Duration;

import java.util.function.Consumer;

public class MoveChara {
    public static final int TYPE_DOWN = 0;
    public static final int TYPE_LEFT = 1;
    public static final int TYPE_RIGHT = 2;
    public static final int TYPE_UP = 3;

    private static final String[] directionStrings = {"d", "l", "r", "u"};
    private static final String[] animationNumbers = {"1", "2", "3"};
    private static final String pngPathPrefix = "png/cat/neko1";
    private static final String pngPathSuffix = ".png";

    private double drawnX;
    private double drawnY;
    private Transition moveTransition;

    private int posCol;
    private int posRow;
    private int charaDirection;

    private final MapData mapData;

    // TODO MapView ではなく interface にする
    private final MapView mapView;

    private final ImageFrameAnimation[] charaAnimations;

    private Consumer<MoveChara> onSelfMove = null;

    public MoveChara(int startCol, int startRow, MapData mapData, MapView mapView) {
        this.posCol = startCol;
        this.posRow = startRow;
        this.mapData = mapData;
        this.mapView = mapView;
        System.out.println("new MoveChara: col=" + posCol + ", row=" + posRow);

        this.drawnX = mapView.getCellDrawX(this.posCol);
        this.drawnY = mapView.getCellDrawY(this.posRow);
        this.moveTransition = new Transition() {
            @Override
            protected void interpolate(double frac) {
            }
        };

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
        final int cellSize = mapView.getCellSize();
        this.charaAnimations[this.charaDirection].draw(gc, this.drawnX, this.drawnY, cellSize, cellSize);
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

    public Consumer<MoveChara> getOnSelfMove() {
        return onSelfMove;
    }

    public void setOnMoveHandler(Consumer<MoveChara> onSelfMove) {
        this.onSelfMove = onSelfMove;
    }

    public int getCharaDirection() {
        return this.charaDirection;
    }

    // check the place where the cat will go
    public boolean isMovableBy(int dx, int dy) {
        return mapData.getCellType(posCol + dx, posRow + dy).isMovable();
    }

    // move the cat
    public boolean moveBy(int dx, int dy, Duration duration) {
        if (isMovableBy(dx, dy)) {
            final int toCol = this.posCol + dx;
            final int toRow = this.posRow + dy;
            final double fromX = this.mapView.getCellDrawX(this.posCol);
            final double fromY = this.mapView.getCellDrawY(this.posRow);
            final double toX = this.mapView.getCellDrawX(toCol);
            final double toY = this.mapView.getCellDrawY(toRow);

            this.moveTransition = new Transition() {
                private boolean isFiredOnMoveEvent = false;

                {
                    setCycleDuration(duration);
                }

                @Override
                protected void interpolate(double frac) {
                    MoveChara.this.drawnX = fromX + (toX - fromX) * frac;
                    MoveChara.this.drawnY = fromY + (toY - fromY) * frac;

                    if (frac > 0.5 && !isFiredOnMoveEvent) {
                        isFiredOnMoveEvent = true;
                        MoveChara.this.posCol = toCol;
                        MoveChara.this.posRow = toRow;
                        if (onSelfMove != null) {
                            onSelfMove.accept(MoveChara.this);
                        }
                    }
                }
            };

            this.moveTransition.play();
            return true;
        } else {
            return false;
        }
    }

    public Animation.Status getMoveTransitionStatus() {
        return this.moveTransition.getStatus();
    }

    // getter: column-position of the cat
    public int getPosCol() {
        return posCol;
    }

    // getter: row-position of the cat
    public int getPosRow() {
        return posRow;
    }

    public Pos getPos() {
        return new Pos(posCol, posRow);
    }

    public double getDrawnX() {
        return drawnX;
    }

    public double getDrawnY() {
        return drawnY;
    }
}
