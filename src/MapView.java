import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class MapView implements CellDrawnPositionResolver {
    private final MapData mapData;
    private final MapSkin mapSkin;
    private int mapLeftX = 0;
    private int mapTopY = 0;
    private int cellSize;

    private final DoorKeyView doorKeyView = new DoorKeyView();
    private final CoinView coinView = new CoinView();
    private final Image closedGoalImage = new Image("png/door-close-32x32.png");
    private final Image openedGoalImage = new Image("png/door-open-32x32.png");

    public MapView(MapData mapData, int cellSize, MapSkin mapSkin) {
        this.mapData = mapData;
        this.cellSize = cellSize;
        this.mapSkin = mapSkin;
    }

    public void draw(GraphicsContext gc) {
        this.drawCells(gc);
        this.drawGoal(gc);
        this.drawItems(gc);
    }

    /**
     * マップの横の幅のピクセル数を返す。
     *
     * @return マップの横の幅のピクセル数
     */
    public int getMapWidth() {
        return this.cellSize * this.mapData.getWidth();
    }

    /**
     * マップの縦の高さのピクセル数を返す。
     *
     * @return マップの縦の高さのピクセル数
     */
    public int getMapHeight() {
        return this.cellSize * this.mapData.getHeight();
    }

    public int getMapBottomY() {
        return this.getMapTopY() + this.getMapHeight();
    }

    private void drawCells(GraphicsContext gc) {
        final int nrow = this.mapData.getHeight();
        final int ncol = this.mapData.getWidth();

        // 全セルの描画
        for (int row = 0; row < nrow; ++row) {
            for (int col = 0; col < ncol; ++col) {
                final var cellType = this.mapData.getCellType(col, row);
                final var cellImage = this.mapSkin.getCellImage(cellType);
                final int x = this.getCellDrawnX(col);
                final int y = this.getCellDrawnY(row);
                gc.drawImage(cellImage, x, y, this.cellSize, this.cellSize);
            }
        }
    }

    private void drawGoal(GraphicsContext gc) {
        final int goalCol = mapData.getGoalX();
        final int goalRow = mapData.getGoalY();
        final int x = this.getCellDrawnX(goalCol);
        final int y = this.getCellDrawnY(goalRow);

        if (mapData.isGoalOpen()) {
            gc.drawImage(openedGoalImage, x, y, this.cellSize, this.cellSize);
        } else {
            gc.drawImage(closedGoalImage, x, y, this.cellSize, this.cellSize);
        }
    }

    private void drawItems(GraphicsContext gc) {
        final int nrow = this.mapData.getHeight();
        final int ncol = this.mapData.getWidth();

        for (int row = 0; row < nrow; ++row) {
            for (int col = 0; col < ncol; ++col) {
                final var itemType = this.mapData.getItemType(col, row);

                if (itemType == ItemType.KEY) {
                    final int x = this.getCellDrawnX(col);
                    final int y = this.getCellDrawnY(row);
                    this.doorKeyView.draw(gc, x, y, this.cellSize, this.cellSize);
                } else if (itemType == ItemType.COIN) {
                    final int coinSize = (int) (this.cellSize * 0.7);
                    final int offset = (this.cellSize - coinSize) / 2;
                    final int x = this.getCellDrawnX(col) + offset;
                    final int y = this.getCellDrawnY(row) + offset;
                    this.coinView.draw(gc, x, y, coinSize, coinSize);
                }
            }
        }
    }

    @Override
    public int getCellDrawnX(int col) {
        return this.mapLeftX + (this.cellSize * col);
    }

    @Override
    public int getCellDrawnY(int row) {
        return this.mapTopY + (this.cellSize * row);
    }

    @Override
    public int getCellSize() {
        return cellSize;
    }

    @Override
    public int getMapTopY() {
        return mapTopY;
    }

    @Override
    public int getMapLeftX() {
        return mapLeftX;
    }

    public void setCellSize(int cellSize) {
        this.cellSize = cellSize;
    }

    public void setMapLeftX(int mapLeftX) {
        this.mapLeftX = mapLeftX;
    }

    public void setMapTopY(int mapTopY) {
        this.mapTopY = mapTopY;
    }
}
