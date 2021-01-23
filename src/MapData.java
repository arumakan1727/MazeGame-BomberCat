import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * グリッド状マップのデータを保持するクラス。
 * <p>
 * 各マスはセルとも呼ぶことにする。
 * また、左から x マス目、上から y マス目のセルを セル(x, y) と表すことにする。
 * <p>
 * x, y は 0 から始まる (0-indexed)。
 */
public class MapData {
    private final CellType[][] cellTypeGrid;
    private final ItemType[][] itemTypeGrid;
    private final int width;
    private final int height;
    private final int playerStartX = 1;
    private final int playerStartY = 1;

    private int goalX;
    private int goalY;
    private boolean isGoalOpen = false;

    /**
     * 縦 height マス、横 width マスからなるマップデータを生成し、迷路を構築する。
     *
     * @param width  マップの列数
     * @param height マップの行数
     */
    public MapData(int width, int height) {
        this.width = width;
        this.height = height;
        this.cellTypeGrid = new CellType[height][width];
        this.itemTypeGrid = new ItemType[height][width];

        this.fillMap(CellType.WALL);
        this.digMap(1, 3);

        this.placeGoal();

        this.clearItem();
        this.placeKeys();
        this.placeCoins(12);
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

    public int getPlayerStartX() {
        return playerStartX;
    }

    public int getPlayerStartY() {
        return playerStartY;
    }

    public int getGoalX() {
        return goalX;
    }

    public int getGoalY() {
        return goalY;
    }

    public boolean isGoalOpen() {
        return isGoalOpen;
    }

    public void setGoalOpen(boolean goalOpen) {
        isGoalOpen = goalOpen;
    }

    /**
     * セル(x, y) の種類を返す。
     * x, y は 0-indexed。
     * 指定したマスの位置がマップの範囲を超える場合は {@link CellType#ILLEGAL} を返す。
     *
     * @param x 取得するセルの列番号
     * @param y 取得するセルの行番号
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
     * @param x        設定するセルの列番号
     * @param y        設定するセルの行番号
     * @param cellType 設定するセルの種類
     */
    public void setCellType(int x, int y, CellType cellType) {
        if (x < 1 || width <= x - 1 || y < 1 || height <= y - 1) {
            return;
        }
        cellTypeGrid[y][x] = cellType;
    }

    /**
     * セル(x, y) のアイテムの種類を返す。
     * x, y は 0-indexed。
     * (x, y) にアイテムがない場合は {@link ItemType#NONE} を返す。
     *
     * @param x 取得するアイテムの列番号
     * @param y 取得するアイテムの行番号
     * @return x列y行目のアイテムの種類
     */
    public ItemType getItemType(int x, int y) {
        return itemTypeGrid[y][x];
    }

    /**
     * セル(x, y) のアイテムの種類を設定する。
     * x, y は 0-indexed。
     *
     * @param x        設定するアイテムの列番号
     * @param y        設定するアイテムの行番号
     * @param itemType 設定するアイテムの種類
     */
    public void setItemType(int x, int y, ItemType itemType) {
        itemTypeGrid[y][x] = itemType;
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
     * マップ上のアイテムを空にする。
     */
    public void clearItem() {
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                itemTypeGrid[y][x] = ItemType.NONE;
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

    /**
     * セル(col, row) が、空白マスかつスタート位置でもなくゴール位置でもなくアイテムも配置されていないなら true を返す。
     * そうでなければ false を返す。
     *
     * @param col 列番号
     * @param row 行番号
     * @return セル(col, row) が、スタート位置でもなくゴール位置でもなくアイテムが配置されていないなら true を返す。
     */
    private boolean isEmptyPos(int col, int row) {
        return getCellType(col, row) == CellType.SPACE
                && (col != getGoalX() && row != getGoalY())
                && (col != getPlayerStartX() && row != getPlayerStartY())
                && getItemType(col, row) == ItemType.NONE;
    }

    private boolean isEmptyPos(Pos pos) {
        return isEmptyPos(pos.col, pos.row);
    }

    public void placeKeys() {
        final int midX = this.width / 2;
        final int midY = this.height / 2;

        // 右上のエリアにキーを一つランダムに配置
        {
            final List<Pos> poses =
                    this.listUpCellPositions(midX + 1, width, 0, midY - 1, this::isEmptyPos);
            final Pos p = randomChoice(poses);
            this.setItemType(p.col, p.row, ItemType.KEY);
        }

        // 右下のエリアにキーを一つランダムに配置
        {
            final List<Pos> poses =
                    this.listUpCellPositions(midX + 1, width, midY + 1, height, this::isEmptyPos);
            final Pos p = randomChoice(poses);
            this.setItemType(p.col, p.row, ItemType.KEY);
        }

        // 左のエリアにキーを一つランダムに配置
        {
            final List<Pos> poses =
                    this.listUpCellPositions(0, midX - 1, midY + 1, height, this::isEmptyPos);
            final Pos p = randomChoice(poses);
            this.setItemType(p.col, p.row, ItemType.KEY);
        }
    }

    public void placeCoins(int coinNum) {
        final List<Pos> freeCells = this.listUpCellPositions(this::isEmptyPos);
        Collections.shuffle(freeCells);

        for (int i = 0; i < coinNum; ++i) {
            final Pos p = freeCells.get(i);
            this.setItemType(p.col, p.row, ItemType.COIN);
        }
    }

    public int countExistingKeys() {
        int count = 0;
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                if (getItemType(x, y) == ItemType.KEY) {
                    ++count;
                }
            }
        }
        return count;
    }

    /**
     * TODO 必須2 ゴールの配置
     */
    public void placeGoal() {
        // ゴール位置の候補。要素を追加して長さを自由に変えられる配列。
        List<Pos> goalCandidates = new ArrayList<>();

        // 外周は候補の探索に含めないので 0 <= y < height ではなく 1 <= y < height - 1。 x も同様。
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {

                // 今見ているマスが壁ならば、ゴールを配置できないのでスキップ
                if (getCellType(x, y) != CellType.SPACE) continue;

                // プレイヤーのスタートマスにゴールは配置できないのでスキップ
                if (y == getPlayerStartY() && x == getPlayerStartX()) continue;

                // 上下左右の壁の数を数える。
                int wallCount = 0;
                if (getCellType(x + 1, y) == CellType.WALL) wallCount++;
                if (getCellType(x - 1, y) == CellType.WALL) wallCount++;
                if (getCellType(x, y + 1) == CellType.WALL) wallCount++;
                if (getCellType(x, y - 1) == CellType.WALL) wallCount++;

                // 周囲がコの字に壁で囲まれているならゴール候補に追加
                if (wallCount == 3) {
                    goalCandidates.add(new Pos(x, y));
                }
            }
        }

        // ゴール候補リストの中からランダムに一つ選んでゴール位置として設定
        final Pos goalPos = randomChoice(goalCandidates);
        this.goalX = goalPos.col;
        this.goalY = goalPos.row;
    }

    /**
     * 列番号が x が (minX <= x < maxX) かつ 行番号 y が (minY <= y < maxY)
     * の範囲内のセルのうち、posPredicate(new Pos(x, y)) が true になるマスをリストアップして返す。
     * 下限は範囲内に含むが、上限は範囲内に含めない (すなわち、範囲は半開区間)。
     *
     * @param minX         列番号の下限
     * @param maxX         列番号の上限
     * @param minY         行番号の下限
     * @param maxY         行番号の上限
     * @param posPredicate リストアップするセル位置の条件を表す述語関数オブジェクト。
     * @return x ∈ [minX, maxX) かつ
     * y ∈ [minY, maxY) かつ
     * (x,y) が空白マス
     * であるような全てのセルの位置 (x, y) を集めたリスト。
     */
    public List<Pos> listUpCellPositions(int minX, int maxX, int minY, int maxY, Predicate<Pos> posPredicate) {
        ArrayList<Pos> spaceCells = new ArrayList<>(height * width);
        for (int y = minY; y < maxY; ++y) {
            for (int x = minX; x < maxX; ++x) {
                if (posPredicate.test(new Pos(x, y))) {
                    spaceCells.add(new Pos(x, y));
                }
            }
        }
        return spaceCells;
    }

    /**
     * エリア内の全てのマスのうち条件を満たすセル位置をリストアップして返す。
     *
     * @param posPredicate リストアップするセル位置の条件を表す述語関数オブジェクト。
     * @return (x, y) が空白マスであるような全てのセルの位置 (x, y) を集めたリスト。
     */
    public List<Pos> listUpCellPositions(Predicate<Pos> posPredicate) {
        return this.listUpCellPositions(0, width, 0, height, posPredicate);
    }

    /**
     * リストの要素をランダムに一つ選んで返す。
     *
     * @param list リスト
     * @param <T>  リストの要素の型
     * @return ランダムに抽出されたリストの要素
     */
    private static <T> T randomChoice(List<T> list) {
        final int i = (int) (Math.random() * list.size());
        return list.get(i);
    }
}

final class Pos {
    public final int row;
    public final int col;

    public Pos(int col, int row) {
        this.col = col;
        this.row = row;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pos pos = (Pos) o;
        return row == pos.row && col == pos.col;
    }

    @Override
    public String toString() {
        return "Pos(row=" + row + ", col=" + col + ")";
    }
}
