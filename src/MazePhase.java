import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.*;
import java.util.function.Predicate;

public class MazePhase implements Phase {
    private static final Duration playerNormalMoveDuration = Duration.millis(250);
    private static final Duration playerFeverMoveDuration = Duration.millis(120);
    private static final long FEVER_KEEP_TIME_MILLI = 15 * 1000;
    private static final Duration coinTrailGageDuration = Duration.seconds(15);
    private static final Duration feverGageDuration = Duration.seconds(20);

    private final MapGameScene scene;
    private final Stopwatch goalStopwatch;

    private final MapData mapData;
    private final MapView mapView;
    private final MoveChara player;
    private Duration playerMoveDuration = playerNormalMoveDuration;

    private final Circle blindHollowAtPlayer;
    private boolean isBlindEnabled = true;

    private final BombExecutor bombExecutor;

    private final MediaPlayer normalBGM;
    private final MediaPlayer feverBGM;
    private final AudioClip coinSE;
    private final AudioClip keySE;
    private final AudioClip goalSE;

    private boolean isFeverMode = false;
    private boolean hasGoaled = false;

    private final MessageArea guideMessage;
    private final HeaderPanel headerPanel;

    private boolean isPlayerControllable = true;

    private final boolean[] isKeyPushed;

    private final DrawableExecutor topLayerDrawable;

    private GoalResultPanel goalResultPanel = null;
    private final TimeGageImageViewButton btnCoinTrail;
    private final TimeGageImageViewButton btnFever;

    public MazePhase(MapGameScene scene) {
        final int HEADER_PANEL_HEIGHT = 56;
        this.scene = scene;
        this.goalStopwatch = new Stopwatch(Duration.millis(500));
        this.mapData = new MapData(21, 15);
        this.mapView = new MapView(mapData, 32, createDefaultMapSkin());
        this.mapView.setMapTopY(HEADER_PANEL_HEIGHT);
        {
            this.headerPanel = new HeaderPanel(0, 0, mapView.getMapWidth(), HEADER_PANEL_HEIGHT);
            this.btnCoinTrail = new TimeGageImageViewButton(new ImageViewButton(new Image("png/button-coin-rod.png"), scene));
            this.btnFever = new TimeGageImageViewButton(new ImageViewButton(new Image("png/button-magic-circle.png"), scene));
            this.headerPanel.addCenterButton(this.btnCoinTrail);
            this.headerPanel.addCenterButton(this.btnFever);

            this.btnCoinTrail.getButton().setOnMouseClicked(evt -> {
                this.putCoinTrailToGoal();
            });
            this.btnFever.getButton().setOnMouseClicked(evt -> {
                this.enterFeverMode();
            });
            this.goalStopwatch.setTickHandler(elapsedTime -> headerPanel.setDrawnElapsedTime(((int) elapsedTime.toSeconds())));
        }

        this.player = new MoveChara(mapData.getPlayerStartX(), mapData.getPlayerStartY(), mapData, mapView);
        this.player.setOnMoveHandler(this::onPlayerMoved);
        this.blindHollowAtPlayer = new Circle(mapView.getCellSize() * 3);
        this.bombExecutor = new BombExecutor(mapView);

        {
            this.normalBGM = new MediaPlayer(new Media(MapGame.getResourceAsString("sound/digitalworld.mp3")));
            this.normalBGM.setVolume(0.4);
            this.normalBGM.setCycleCount(AudioClip.INDEFINITE);

            this.feverBGM = new MediaPlayer(new Media(MapGame.getResourceAsString("sound/bgm-fever.mp3")));
            this.feverBGM.setVolume(0.3);
            this.feverBGM.setCycleCount(AudioClip.INDEFINITE);

            this.coinSE = new AudioClip(MapGame.getResourceAsString("sound/coin1.wav"));
            this.keySE = new AudioClip(MapGame.getResourceAsString("sound/se_maoudamashii_system46.mp3"));
            this.goalSE = new AudioClip(MapGame.getResourceAsString("sound/goal.wav"));
        }

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
    public void setup() {
        this.scene.setOnKeyPressed(this::keyPressedAction);
        this.scene.setOnKeyReleased(this::keyReleasedAction);
        scene.getOtherComponents().getChildren().addAll(this.btnCoinTrail.getButton(), this.btnFever.getButton());
        this.normalBGM.play();

        this.guideMessage.setMessage("カギを すべて 拾って ゴールの 扉 を開けよう！");
        this.btnCoinTrail.gageStartFromEmpty(coinTrailGageDuration);
        this.btnFever.gageStartFromEmpty(feverGageDuration);
        this.goalStopwatch.start();
    }

    @Override
    public void tearDown() {
        this.scene.setOnKeyPressed(null);
        this.scene.setOnKeyReleased(null);
        this.scene.getOtherComponents().getChildren().clear();
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
            if (this.isBlindEnabled && this.blindHollowAtPlayer.getRadius() < this.mapView.getMapWidth()) {
                gc.setFill(Color.BLACK);
                gc.fillRect(mapView.getMapLeftX(), mapView.getMapTopY(), mapView.getMapWidth(), mapView.getMapHeight());
                clipHoles(gc, blindHollowAtPlayer);
            }
            mapView.draw(gc);
            player.draw(gc);
            this.bombExecutor.draw(gc);
        }
        gc.restore();

        this.headerPanel.draw(gc);
        this.guideMessage.draw(gc);
        this.topLayerDrawable.draw(gc);

        if (this.goalResultPanel != null) {
            this.goalResultPanel.draw(gc);
        }
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
            case DIGIT1:
                if (isKeyPushed(KeyCode.SHIFT)) {
                    putCoinTrailToGoal();
                } else {
                    checkCoinTrailTrigger();
                }
                break;
            case DIGIT2:
                if (isKeyPushed(KeyCode.SHIFT)) {
                    this.enterFeverMode();
                } else {
                    checkEnterFeverModeTrigger();
                }
                break;
            case ESCAPE:
                this.isBlindEnabled = !this.isBlindEnabled;
                break;
            case F1:
                this.goalAction();
        }
    }

    public void itemGetAction(int col, int row) {
        final ItemType itemType = this.mapData.getItemType(col, row);
        this.mapData.setItemType(col, row, ItemType.NONE);

        if (itemType == ItemType.COIN) {
            this.coinGetAction(col, row);
        } else if (itemType == ItemType.KEY) {
            this.keyGetAction(col, row);
        }
    }

    private double getFeverBonusRate() {
        if (isFeverMode) {
            return 1.5;
        } else {
            return 1.0;
        }
    }

    private void coinGetAction(int col, int row) {
        final int score = (int) (ItemType.COIN.getScore() * getFeverBonusRate());
        this.headerPanel.addDrawnScore(score);

        registerScoreTextFloatAnimation(score,
                mapView.getCellDrawnX(col), mapView.getCellDrawnY(row),
                this.topLayerDrawable, isFeverMode);

        this.coinSE.play();
    }

    private void keyGetAction(int col, int row) {
        headerPanel.setGotKeyCount(mapData.countRemovedKeys());

        if (mapData.countExistingKeys() <= 0) {
            this.mapData.setGoalOpen(true);
            this.guideMessage.setMessage("カギを すべて 拾って ゴールの 扉 が開いた！");
        } else {
            final int n = mapData.countExistingKeys();
            this.guideMessage.setMessage("カギを拾った！ のこり " + n + " つ！");
        }

        final int score = (int) (ItemType.KEY.getScore() * getFeverBonusRate());
        this.headerPanel.addDrawnScore(score);

        registerScoreTextFloatAnimation(score,
                mapView.getCellDrawnX(col), mapView.getCellDrawnY(row),
                this.topLayerDrawable, isFeverMode);

        this.keySE.play();
    }

    private static void registerScoreTextFloatAnimation(int score, int x, int y, DrawableExecutor drawableExecutor, boolean isFeverMode) {
        drawableExecutor.registerAndPlay(new TextFloatUpAnimation(
                "+" + score,
                x, y,
                Font.font("sans-serif", FontWeight.BLACK, isFeverMode ? 24 : 18),
                Color.WHITE,
                isFeverMode ? Color.BLUE : Color.DARKORANGE,
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
        this.goalStopwatch.stop();
        this.isPlayerControllable = false;
        this.hasGoaled = true;
        this.normalBGM.stop();
        this.feverBGM.stop();
        this.goalSE.play();
        this.btnCoinTrail.getButton().setButtonEnabled(false);
        this.btnCoinTrail.pause();
        this.btnFever.getButton().setButtonEnabled(false);
        this.btnFever.pause();

        new TaskScheduleTimer(2000) {
            @Override
            public void task() {
                goalResultPanel = new GoalResultPanel("GOAL!", ((int) goalStopwatch.getCurTime().toSeconds()), headerPanel.getDrawnScore(), scene);
                goalResultPanel.getBtnNewMap().setOnMouseClicked(event -> createAndGotoNextMaze());

                final int fromY = -1 * goalResultPanel.getHeight();
                final int toY = (mapView.getMapHeight() - goalResultPanel.getHeight()) / 2 + mapView.getMapTopY();

                goalResultPanel.setX((mapView.getMapWidth() - goalResultPanel.getWidth()) / 2);
                goalResultPanel.setY(fromY);

                new Transition() {
                    {
                        setCycleDuration(Duration.millis(1000));
                    }

                    @Override
                    protected void interpolate(double frac) {
                        final double k = Easing.easeOutBounce(frac);
                        goalResultPanel.setY((int) (fromY + (toY - fromY) * k));
                    }
                }.play();
            }
        }.start();
    }

    /**
     * TODO 必須3 次のマップへ遷移
     */
    public void createAndGotoNextMaze() {
        Phase nextPhase = new MazePhase(this.scene);
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

        if (this.isFeverMode) {
            if (bombExecutor.countBomb() >= 5) return;
            bombExecutor.register(new GoldBomb(player.getPosCol(), player.getPosRow()));
        } else {
            if (bombExecutor.countBomb() >= 3) return;
            bombExecutor.register(new NormalBomb(player.getPosCol(), player.getPosRow()));
        }
    }

    private void checkCoinTrailTrigger() {
        if (!btnCoinTrail.getButton().isButtonEnabled()) return;
        if (hasGoaled) return;
        putCoinTrailToGoal();
    }

    private void checkEnterFeverModeTrigger() {
        if (!btnFever.getButton().isButtonEnabled()) return;
        if (hasGoaled) return;
        enterFeverMode();
    }

    public void putCoinTrailToGoal() {
        this.btnCoinTrail.gageStartFromEmpty(coinTrailGageDuration);

        final List<Pos> path = calcShortestPath(
                this.player.getPos(), this.mapData.getGoalPos(),
                mapData.getWidth(), mapData.getHeight(),
                pos -> mapData.getCellType(pos.col, pos.row).isMovable());

        final AudioClip coinPutSE = new AudioClip(MapGame.getResourceAsString("sound/coin8.wav"));

        int i = 0;
        for (Pos pos : path) {
            if (mapData.getItemType(pos.col, pos.row) != ItemType.NONE) continue;
            new TaskScheduleTimer(i * 80L) {
                @Override
                public void task() {
                    if (hasGoaled) return;
                    mapData.setItemType(pos.col, pos.row, ItemType.COIN);
                    coinPutSE.play();
                }
            }.start();
            ++i;
        }
    }

    public void enterFeverMode() {
        if (this.isFeverMode) return;
        this.btnFever.setGage(0.0);

        this.normalBGM.pause();
        this.feverBGM.play();
        isFeverMode = true;
        this.playerMoveDuration = playerFeverMoveDuration;

        final double initRadius = this.blindHollowAtPlayer.getRadius();
        final double targetRadius = this.mapView.getMapWidth() * 2;

        new Transition() {
            {
                setCycleDuration(Duration.millis(1000));
            }

            @Override
            protected void interpolate(double frac) {
                final double r = initRadius + (targetRadius - initRadius) * frac;
                blindHollowAtPlayer.setRadius(r);
            }
        }.play();

        new TaskScheduleTimer(FEVER_KEEP_TIME_MILLI) {
            @Override
            public void task() {
                if (hasGoaled) return;
                normalBGM.play();
                feverBGM.stop();
                isFeverMode = false;
                playerMoveDuration = playerNormalMoveDuration;
                btnFever.gageStartFromEmpty(feverGageDuration);

                new Transition() {
                    {
                        setCycleDuration(Duration.millis(1000));
                    }

                    @Override
                    protected void interpolate(double frac) {
                        final double r = targetRadius + (initRadius - targetRadius) * frac;
                        blindHollowAtPlayer.setRadius(r);
                    }
                }.play();
            }
        }.start();
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
        final int[] directions = {0, 1, -1, 0};

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
