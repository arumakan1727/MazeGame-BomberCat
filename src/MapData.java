/**
 * グリッド状マップのデータを保持するクラス。
 *
 * 各マスはセルとも呼ぶことにする。
 * また、左から x マス目、上から y マス目のセルを セル(x, y) と表すことにする。
 *
 * x, y は 0 から始まる (0-indexed)。
 */
public class MapData {
    private final CellType[][] cellTypeGrid;
    private final int width;
    private final int height;

    /**
     * 縦 height マス、横 width マスからなるマップデータを生成し、迷路を構築する。
     *
     * @param width マップの列数
     * @param height マップの行数
     */
    public MapData(int width, int height) {
        this.width = width;
        this.height = height;
        this.cellTypeGrid = new CellType[height][width];

        this.fillMap(CellType.WALL);
        this.digMap(1, 3);
    }

    /**
     * マップの縦のマス数 (=行数) を返す。
     *
     * @return マップの行数
     */
    public int getHeight() {
        return height;
    }

    /**
     * マップの横のマス数 (=列数) を返す。
     *
     * @return マップの列数
     */
    public int getWidth() {
        return width;
    }

    /**
     * セル(x, y) の種類を返す。
     * x, y は 0-indexed。
     * 指定したマスの位置がマップの範囲を超える場合は {@link CellType#ILLEGAL} を返す。
     *
     * @param x 取得するセルの列番号
     * @param y 取得するセルの行番号
     *
     * @return x列y行目のセルの種類
     */
    public CellType getCellType(int x, int y) {
        if (x < 0 || width <= x || y < 0 || height <= y) {
            return CellType.ILLEGAL;
        }
        return cellTypeGrid[y][x];
    }

    /**
     * セル(x, y) の種類を設定する。
     * x, y は 0-indexed。
     * 指定したマスの位置がマップの範囲を超える場合は例外を投げずに何もしない。
     *
     * @param x 設定するセルの列番号
     * @param y 設定するセルの行番号
     * @param cellType 設定するセルの種類
     */
    public void setCellType(int x, int y, CellType cellType) {
        if (x < 1 || width <= x - 1 || y < 1 || height <= y - 1) {
            return;
        }
        cellTypeGrid[y][x] = cellType;
    }

    /**
     * マップの全マスを fillValue で塗りつぶす。
     *
     * @param fillValue 塗りつぶす値
     */
    public void fillMap(CellType fillValue) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cellTypeGrid[y][x] = fillValue;
            }
        }
    }

    /**
     * 迷路の通り道を セル(x, y) から再帰的に作る。
     *
     * @param x 列番号
     * @param y 行番号
     */
    public void digMap(int x, int y) {
        setCellType(x, y, CellType.SPACE);
        final int[][] dl = {{0, 1}, {0, -1}, {-1, 0}, {1, 0}};

        // Shuffle dl[]
        for (int i = 0; i < dl.length; i++) {
            // swap (dl[i]) and (dl[k])
            final int k = (int) (Math.random() * dl.length);
            final int[] tmp = dl[i];
            dl[i] = dl[k];
            dl[k] = tmp;
        }

        // シャッフルした順で上下左右の4方向へ道を掘る
        for (int[] direction : dl) {
            final int dx = direction[0];
            final int dy = direction[1];
            if (getCellType(x + dx * 2, y + dy * 2) == CellType.WALL) {
                setCellType(x + dx, y + dy, CellType.SPACE);
                digMap(x + dx * 2, y + dy * 2);
            }
        }
    }
}
