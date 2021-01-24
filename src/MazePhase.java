import javafx.animation.Animation;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.*;
import java.util.function.Predicate;

public class MazePhase implements Phase {
    private MapGameScene scene;

    private final MapData mapData;
    private final MapView mapView;
    private final MoveChara player;
    private final Duration playerMoveDuration = Duration.millis(200);

    private final Circle blindHollowAtPlayer;
    private boolean isBlindEnabled = true;

    private final BombExecutor bombExecutor;

    private final AudioClip bgm;
    private final AudioClip coinSE;
    private final AudioClip keySE;
    private final AudioClip goalSE;

    private final MessageArea guideMessage;

    private int score = 0;
    private boolean isPlayerControllable = true;

    private final boolean[] isKeyPushed;

    private final DrawableExecutor topLayerDrawable;

    public MazePhase() {
        this.mapData = new MapData(21, 15);
        this.mapView = new MapView(mapData, 32, createDefaultMapSkin());
        this.mapView.setMapTopY(40);
        this.player = new MoveChara(mapData.getPlayerStartX(), mapData.getPlayerStartY(), mapData, mapView);
        this.player.setOnMoveHandler(this::onPlayerMoved);
        this.blindHollowAtPlayer = new Circle(mapView.getCellSize() * 3);
        this.bombExecutor = new BombExecutor(mapView);

        this.bgm = new AudioClip(MapGame.getResourceAsString("sound/bgm_maoudamashii_8bit18.mp3"));
        this.bgm.setVolume(0.3);
        this.bgm.setCycleCount(AudioClip.INDEFINITE);

        this.coinSE = new AudioClip(MapGame.getResourceAsString("sound/coin1.wav"));
        this.keySE = new AudioClip(MapGame.getResourceAsString("sound/se_maoudamashii_system46.mp3"));
        this.goalSE = new AudioClip(MapGame.getResourceAsString("sound/goal.wav"));

        final Font pixelFont = Font.loadFont(MapGame.getResourceAsString("font/PixelMplus12-Regular.ttf"), 16);
        this.guideMessage = new MessageArea(0, this.mapView.getMapBottomY(), this.mapView.getMapWidth(), 28, pixelFont);

        this.isKeyPushed = new boolean[KeyCode.values().length];

        this.topLayerDrawable = new DrawableExecutor();
    }

    public boolean isKeyPushed(KeyCode keyCode) {
        return this.isKeyPushed[keyCode.ordinal()];
    }

    public void hollowAtPlayer() {
        final int sHalf = mapView.getCellSize() / 2;
        this.blindHollowAtPlayer.setCenterX(player.getDrawnX() + sHalf);
        this.blindHollowAtPlayer.setCenterY(player.getDrawnY() + sHalf);
    }

    @Override
    public void setup(MapGameScene scene) {
        this.scene = scene;
        this.scene.setOnKeyPressed(this::keyPressedAction);
        this.scene.setOnKeyReleased(this::keyReleasedAction);
        this.bgm.play();

        this.guideMessage.setMessage("カギを すべて 拾って ゴールの 扉 を開けよう！");
    }

    @Override
    public void tearDown() {
        this.scene.setOnKeyPressed(null);
        this.scene.setOnKeyReleased(null);
    }

    @Override
    public void update(long now) {
        this.bombExecutor.update(mapData);
        this.hollowAtPlayer();

        if (isPlayerControllable) {
            if (isKeyPushed(KeyCode.H) || isKeyPushed(KeyCode.LEFT)) {
                leftButtonAction();
            } else if (isKeyPushed(KeyCode.J) || isKeyPushed(KeyCode.DOWN)) {
                downButtonAction();
            } else if (isKeyPushed(KeyCode.K) || isKeyPushed(KeyCode.UP)) {
                upButtonAction();
            } else if (isKeyPushed(KeyCode.L) || isKeyPushed(KeyCode.RIGHT)) {
                rightButtonAction();
            }
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();
        {
            if (this.isBlindEnabled) {
                gc.setFill(Color.BLACK);
                gc.fillRect(mapView.getMapLeftX(), mapView.getMapTopY(), mapView.getMapWidth(), mapView.getMapHeight());
                clipHoles(gc, blindHollowAtPlayer);
            }
            mapView.draw(gc);
            player.draw(gc);
            this.bombExecutor.draw(gc);
        }
        gc.restore();

        this.topLayerDrawable.draw(gc);

        this.drawScore(gc);
        this.guideMessage.draw(gc);
    }

    private void clipHoles(GraphicsContext gc, Circle... holes) {
        gc.beginPath();
        for (Circle hole : holes) {
            final double cx = hole.getCenterX();
            final double cy = hole.getCenterY();
            final double r = hole.getRadius();
            gc.moveTo(cx - r, cy);
            gc.arc(cx, cy, r, r, 180, 360);
        }
        gc.closePath();
        gc.clip();
    }

    private static MapSkin createDefaultMapSkin() {
        EnumMap<CellType, String> cellImagePaths = new EnumMap<>(CellType.class);
        cellImagePaths.put(CellType.SPACE, "png/SPACE.png");
        cellImagePaths.put(CellType.WALL, "png/stone-block.png");
        cellImagePaths.put(CellType.BREAKABLE_BLOCK, "png/ice-block.png");
        return new MapSkin(cellImagePaths);
    }

    public void keyReleasedAction(KeyEvent event) {
        this.isKeyPushed[event.getCode().ordinal()] = false;
    }

    // Get users key actions
    public void keyPressedAction(KeyEvent event) {
        this.isKeyPushed[event.getCode().ordinal()] = true;

        if (!isPlayerControllable) {
            return;
        }

        switch (event.getCode()) {
            case SPACE:
                spaceButtonAction();
                break;
            case F:
                putCoinTrailToGoal();
                break;
            case ESCAPE:
                this.isBlindEnabled = !this.isBlindEnabled;
                break;
        }
    }

    public void itemGetAction(int col, int row) {
        final ItemType itemType = this.mapData.getItemType(col, row);
        this.score += itemType.getScore();
        this.mapData.setItemType(col, row, ItemType.NONE);

        if (itemType == ItemType.COIN) {
            this.coinGetAction(col, row);
        } else if (itemType == ItemType.KEY) {
            this.keyGetAction(col, row);
        }
    }

    private void coinGetAction(int col, int row) {
        this.coinSE.play();

        registerScoreTextFloatAnimation(ItemType.COIN.getScore(),
                mapView.getCellDrawnX(col), mapView.getCellDrawnY(row),
                this.topLayerDrawable);
    }

    private void keyGetAction(int col, int row) {
        this.keySE.play();

        if (mapData.countExistingKeys() <= 0) {
            this.mapData.setGoalOpen(true);
            this.guideMessage.setMessage("カギを すべて 拾って ゴールの 扉 が開いた！");
        } else {
            final int n = mapData.countExistingKeys();
            this.guideMessage.setMessage("カギを拾った！ のこり " + n + " つ！");
        }

        registerScoreTextFloatAnimation(ItemType.KEY.getScore(),
                mapView.getCellDrawnX(col), mapView.getCellDrawnY(row),
                this.topLayerDrawable);
    }

    private static void registerScoreTextFloatAnimation(int score, int x, int y, DrawableExecutor drawableExecutor) {
        drawableExecutor.registerAndPlay(new TextFloatUpAnimation(
                "+" + score,
                x, y,
                Font.font("sans-serif", FontWeight.BLACK, 18),
                Color.WHITE,
                Color.DARKORANGE,
                Duration.millis(800),
                24
        ));
    }

    private void onPlayerMoved(MoveChara chara) {
        final int playerCol = player.getPosCol();
        final int playerRow = player.getPosRow();

        // ゴール扉が開いている状態でゴールマスに重なったらゴール処理
        if (mapData.isGoalOpen() && playerCol == mapData.getGoalX() && playerRow == mapData.getGoalY()) {
            goalAction();
        }

        // プレイヤーがアイテムマスに重なったならアイテム拾得処理
        if (mapData.getItemType(playerCol, playerRow) != ItemType.NONE) {
            itemGetAction(playerCol, playerRow);
        }
    }

    public void goalAction() {
        System.out.println("Goal!!!!!!!!!!!!!!!!!");
        this.isPlayerControllable = false;
        this.bgm.stop();
        this.goalSE.play();

        TimerTask gotoNextMazeTask = new TimerTask() {
            @Override
            public void run() {
                createAndGotoNextMaze();
            }
        };

        new Timer().schedule(gotoNextMazeTask, 2000);
    }

    private void drawScore(GraphicsContext gc) {
        gc.setFont(new Font("monospace", 16));
        gc.setFill(Color.DIMGRAY);
        final int x = 10;
        final int y = 30;
        gc.fillText("Score: " + this.score, x, y);
    }

    /**
     * TODO 必須3 次のマップへ遷移
     */
    public void createAndGotoNextMaze() {
        Phase nextPhase = new MazePhase();
        this.scene.changePhase(nextPhase);
    }

    // Operations for going the cat down
    public void upButtonAction() {
        if (player.getMoveTransitionStatus() == Animation.Status.RUNNING) return;
        player.setCharaDirection(MoveChara.TYPE_UP);
        player.moveBy(0, -1, playerMoveDuration);
    }

    // Operations for going the cat down
    public void downButtonAction() {
        if (player.getMoveTransitionStatus() == Animation.Status.RUNNING) return;
        player.setCharaDirection(MoveChara.TYPE_DOWN);
        player.moveBy(0, 1, playerMoveDuration);
    }

    // Operations for going the cat right
    public void leftButtonAction() {
        if (player.getMoveTransitionStatus() == Animation.Status.RUNNING) return;
        player.setCharaDirection(MoveChara.TYPE_LEFT);
        player.moveBy(-1, 0, playerMoveDuration);
    }

    // Operations for going the cat right
    public void rightButtonAction() {
        if (player.getMoveTransitionStatus() == Animation.Status.RUNNING) return;
        player.setCharaDirection(MoveChara.TYPE_RIGHT);
        player.moveBy(1, 0, playerMoveDuration);
    }

    public void spaceButtonAction() {
        if (bombExecutor.isExistsBombAt(player.getPosCol(), player.getPosRow())) {
            return;
        }
        if (bombExecutor.countBomb() >= 3) {
            return;
        }
        bombExecutor.registerNewBomb(player.getPosCol(), player.getPosRow());
    }

    public void putCoinTrailToGoal() {
        final List<Pos> path = calcShortestPath(
                this.player.getPos(), this.mapData.getGoalPos(),
                mapData.getWidth(), mapData.getHeight(),
                pos -> mapData.getCellType(pos.col, pos.row).isMovable());

        Timer timer = new Timer();

        final AudioClip coinPutSE = new AudioClip(MapGame.getResourceAsString("sound/coin8.wav"));

        int i = 0;
        for (Pos pos : path) {
            if (mapData.getItemType(pos.col, pos.row) != ItemType.NONE) continue;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mapData.setItemType(pos.col, pos.row, ItemType.COIN);
                    coinPutSE.play();
                }
            }, i * 80);
            ++i;
        }
    }

    /**
     * start から goal までの最短ルートを List として返す。
     * List の先頭要素は start で、末尾要素は goal である。
     * 最短ルートが複数ある場合は、そのうちのどれが選ばれるかは実装依存である。
     *
     * @param start      スタートマスの位置
     * @param goal       ゴールマスの位置
     * @param canBeTrail 指定したマスが道になれる (=プレイヤーが通過できる) なら true を返す述語関数
     * @return start から goal までの最短ルート
     */
    public static List<Pos> calcShortestPath(Pos start, Pos goal, int ncol, int nrow, Predicate<Pos> canBeTrail) {
        final int directions[] = {0, 1, -1, 0};

        Queue<Pos> que = new ArrayDeque<>();

        int[][] dist = new int[nrow][ncol];
        for (int[] a : dist) {
            Arrays.fill(a, -1);
        }

        // goal から BFS する
        que.add(goal);
        dist[goal.row][goal.col] = 0;

        while (!que.isEmpty()) {
            Pos cur = que.poll();
            if (cur.equals(start)) break;

            for (int i = 0; i < 4; ++i) {
                final int ny = cur.row + directions[i];
                final int nx = cur.col + directions[i ^ 1];
                final Pos nextPos = new Pos(nx, ny);

                if (ny < 0 || ny >= nrow || nx < 0 || nx >= ncol) continue;
                if (!canBeTrail.test(nextPos)) continue;
                if (dist[ny][nx] != -1) continue;

                dist[ny][nx] = dist[cur.row][cur.col] + 1;
                que.add(nextPos);
            }
        }

        // start から goal に向かって経路復元する

        final int distance = dist[start.row][start.col];
        final List<Pos> path = new ArrayList<>(distance);
        Pos cur = start;
        while (!cur.equals(goal)) {
            path.add(cur);
            for (int i = 0; i < 4; ++i) {
                final int ny = cur.row + directions[i];
                final int nx = cur.col + directions[i ^ 1];
                if (ny < 0 || ny >= nrow || nx < 0 || nx >= ncol) continue;
                if (dist[ny][nx] == dist[cur.row][cur.col] - 1) {
                    cur = new Pos(nx, ny);
                    break;
                }
            }
        }
        path.add(goal);
        return path;
    }
}
