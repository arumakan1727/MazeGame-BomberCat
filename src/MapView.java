import javafx.scene.canvas.GraphicsContext;

public class MapView {
    private MapData mapData;
    private MapSkin mapSkin;
    private int mapLeftX = 0;
    private int mapTopY = 0;
    private int cellSize;

    private final DoorKeyView doorKeyView = new DoorKeyView();
    private final CoinView coinView = new CoinView();

    public MapView(MapData mapData, int cellSize, MapSkin mapSkin) {
        this.mapData = mapData;
        this.cellSize = cellSize;
        this.mapSkin = mapSkin;
    }

    public void draw(GraphicsContext gc) {
        this.drawCells(gc);
        this.drawItems(gc);
    }

    private void drawCells(GraphicsContext gc) {
        final int nrow = this.mapData.getHeight();
        final int ncol = this.mapData.getWidth();

        // 全セルの描画
        for (int row = 0; row < nrow; ++row) {
            for (int col = 0; col < ncol; ++col) {
                final var cellType = this.mapData.getCellType(col, row);
                final var cellImage = this.mapSkin.getCellImage(cellType);
                final int x = this.getCellDrawX(col);
                final int y = this.getCellDrawY(row);
                gc.drawImage(cellImage, x, y, this.cellSize, this.cellSize);
            }
        }
    }

    private void drawItems(GraphicsContext gc) {
        final int nrow = this.mapData.getHeight();
        final int ncol = this.mapData.getWidth();

        for (int row = 0; row < nrow; ++row) {
            for (int col = 0; col < ncol; ++col) {
                final var itemType = this.mapData.getItemType(col, row);

                if (itemType == ItemType.KEY) {
                    final int x = this.getCellDrawX(col);
                    final int y = this.getCellDrawY(row);
                    this.doorKeyView.draw(gc, x, y, this.cellSize, this.cellSize);
                } else if (itemType == ItemType.COIN) {
                    final int coinSize = (int)(this.cellSize * 0.7);
                    final int offset = (this.cellSize - coinSize) / 2;
                    final int x = this.getCellDrawX(col) + offset;
                    final int y = this.getCellDrawY(row) + offset;
                    this.coinView.draw(gc, x, y, coinSize, coinSize);
                }
            }
        }
    }

    public int getCellDrawX(int col) {
        return this.mapLeftX + (this.cellSize * col);
    }

    public int getCellDrawY(int row) {
        return this.mapTopY + (this.cellSize * row);
    }

    public MapData getMapData() {
        return mapData;
    }

    public void setMapData(MapData mapData) {
        this.mapData = mapData;
    }

    public MapSkin getMapSkin() {
        return mapSkin;
    }

    public void setMapSkin(MapSkin mapSkin) {
        this.mapSkin = mapSkin;
    }

    public int getCellSize() {
        return cellSize;
    }

    public void setCellSize(int cellSize) {
        this.cellSize = cellSize;
    }

    public int getMapLeftX() {
        return mapLeftX;
    }

    public void setMapLeftX(int mapLeftX) {
        this.mapLeftX = mapLeftX;
    }

    public int getMapTopY() {
        return mapTopY;
    }

    public void setMapTopY(int mapTopY) {
        this.mapTopY = mapTopY;
    }
}
