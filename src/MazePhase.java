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
    private static final int HEADER_PANEL_HEIGHT = 56;
    private static final int GUIDE_MESSAGE_AREA_HEIGHT = 28;
    private static final int MAZE_AREA_WIDTH = 32 * 21;
    private static final int MAZE_AREA_HEIGHT = 32 * 15;

    public static int getScreenHeight() {
        return HEADER_PANEL_HEIGHT + MAZE_AREA_HEIGHT + GUIDE_MESSAGE_AREA_HEIGHT;
    }

    public static int getScreenWidth() {
        return MAZE_AREA_WIDTH;
    }

    private static final Duration playerNormalMoveDuration = Duration.millis(250);
    private static final Duration playerFeverMoveDuration = Duration.millis(120);
    private static final long FEVER_KEEP_TIME_MILLI = 15 * 1000;
    private static final Duration gageDuration_coinTrail = Duration.seconds(10);
    private static final Duration gageDuration_fever = Duration.seconds(15);
    private static final Duration gageDuration_collectCoins = Duration.seconds(5);

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
    private final TimeGageImageViewButton btnCollectNeighborCoin;
    private final TimeGageImageViewButton btnFever;

    /**
     * コンストラクタ。
     * 迷路ゲームの実行に必要なオブジェクトを構築する。
     * ゲームはまだ始めない。setup() 関数を呼び出すことでゲームが始まる。
     *
     * @param scene ゲームを描画する対象のシーン
     */
    public MazePhase(MapGameScene scene) {
        this.scene = scene;
        this.goalStopwatch = new Stopwatch(Duration.millis(500));
        this.mapData = new MapData(21, 15);
        this.mapView = new MapView(mapData, 32, createDefaultMapSkin());
        this.mapView.setMapTopY(HEADER_PANEL_HEIGHT);

        // 上部のパネルの設定
        {
            this.headerPanel = new HeaderPanel(0, 0, mapView.getMapWidth(), HEADER_PANEL_HEIGHT);

            this.btnCoinTrail = new TimeGageImageViewButton(new ImageViewButton(new Image("png/button-coin-to-goal.png"), scene));
            this.btnCollectNeighborCoin = new TimeGageImageViewButton(new ImageViewButton(new Image("png/button-coin-rod.png"), scene));
            this.btnFever = new TimeGageImageViewButton(new ImageViewButton(new Image("png/button-magic-circle.png"), scene));

            this.headerPanel.addCenterButton(this.btnCoinTrail);
            this.headerPanel.addCenterButton(this.btnCollectNeighborCoin);
            this.headerPanel.addCenterButton(this.btnFever);
            this.goalStopwatch.setTickHandler(elapsedTime -> headerPanel.setElapsedTime(((int) elapsedTime.toSeconds())));
        }

        // プレイヤー
        this.player = new MoveChara(mapData.getPlayerStartX(), mapData.getPlayerStartY(), mapData, mapView);
        this.player.setOnMoveHandler(this::onPlayerMoved);

        // 暗闇をくり抜く穴
        this.blindHollowAtPlayer = new Circle(mapView.getCellSize() * 3);

        // 爆弾・爆発を管理するもの
        this.bombExecutor = new BombExecutor(mapView);

        // BGM, SE の設定
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

        // ガイドメッセージの設定
        final Font pixelFont = Font.loadFont(MapGame.getResourceAsString("font/PixelMplus12-Regular.ttf"), 16);
        this.guideMessage = new MessageArea(0, this.mapView.getMapBottomY(), this.mapView.getMapWidth(), GUIDE_MESSAGE_AREA_HEIGHT, pixelFont);

        // キーが押されているなら 対応する列挙体の ord が true になる配列
        this.isKeyPushed = new boolean[KeyCode.values().length];

        // トップレイヤーに描画されるオブジェクトを管理するもの
        this.topLayerDrawable = new DrawableExecutor();
    }

    /**
     * keyCode のキーが押されているなら true
     */
    public boolean isKeyPushed(KeyCode keyCode) {
        return this.isKeyPushed[keyCode.ordinal()];
    }


    /**
     * 暗闇をくり抜く穴の中心座標をプレイヤーの中心座標に追従させる
     */
    public void updateHollowPosition() {
        final int sHalf = mapView.getCellSize() / 2;
        this.blindHollowAtPlayer.setCenterX(player.getDrawnX() + sHalf);
        this.blindHollowAtPlayer.setCenterY(player.getDrawnY() + sHalf);
    }

    @Override
    public void setup() {
        this.scene.setOnKeyPressed(this::keyPressedAction);
        this.scene.setOnKeyReleased(this::keyReleasedAction);

        // シーンにボタンを追加
        scene.getOtherComponents().getChildren().addAll(this.btnCoinTrail.getButton(), this.btnCollectNeighborCoin.getButton(), this.btnFever.getButton());

        // コインの小道のボタン
        this.btnCoinTrail.getButton().setOnMouseClicked(evt -> {
            this.putCoinTrailToGoal();
        });
        this.btnCoinTrail.setOnGageFilled(btn -> {
            guideMessage.setMessage("コインの小道 が使えるようになった！ [1]キーを押すとコインがゴールへ導いてくれる！");
        });

        // コインの杖のボタン
        this.btnCollectNeighborCoin.getButton().setOnMouseClicked(evt -> {
            this.collectCoinsAroundPlayer(2);
        });
        this.btnCollectNeighborCoin.setOnGageFilled(btn -> {
            guideMessage.setMessage("コインの杖 が使えるようになった！ [2]キー を押して周囲 2 マスのコインを一気にゲット！");
        });

        // フィーバースターのボタン
        this.btnFever.getButton().setOnMouseClicked(evt -> {
            this.enterFeverMode();
        });
        this.btnFever.setOnGageFilled(btn -> {
            guideMessage.setMessage("フィーバースター が使えるようになった！ [3]キー を押して フィーバー だ！");
        });

        // ガイドメッセージの設定
        this.guideMessage.setMessage("カギをすべて拾ってゴールの扉を開けよう！ スペースキーで爆弾を置けるぞ！");

        // ゲージボタンのゲージ貯めを開始
        this.btnCoinTrail.gageStartFromEmpty(gageDuration_coinTrail);
        this.btnCollectNeighborCoin.gageStartFromEmpty(gageDuration_collectCoins);
        this.btnFever.gageStartFromEmpty(gageDuration_fever);

        // BGMの再生
        this.normalBGM.play();

        // ゴールまでの時間計測開始
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
        this.updateHollowPosition();

        if (isPlayerControllable) {
            if (isKeyPushed(KeyCode.H) || isKeyPushed(KeyCode.LEFT)) {
                moveLeftAction();
            } else if (isKeyPushed(KeyCode.J) || isKeyPushed(KeyCode.DOWN)) {
                moveDownAction();
            } else if (isKeyPushed(KeyCode.K) || isKeyPushed(KeyCode.UP)) {
                moveUpAction();
            } else if (isKeyPushed(KeyCode.L) || isKeyPushed(KeyCode.RIGHT)) {
                moveRightAction();
            }
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();
        {
            // 暗闇が有効 かつ 暗闇の半径が画面幅よりも小さければ、暗闇を描画する
            // まず全て黒く塗りつぶした後に、プレイヤー周囲の正円くり抜き でクリッピングする。
            // クリッピングするとクリッピングされた領域のみが描画される。
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

    /**
     * 円領域群 (holes) のみを描画するよう gc を設定する。
     */
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

    /**
     * セルタイプ からデフォルトの セルの画像 を求める写像を生成して返す。
     */
    private static MapSkin createDefaultMapSkin() {
        EnumMap<CellType, String> cellImagePaths = new EnumMap<>(CellType.class);
        cellImagePaths.put(CellType.SPACE, "png/SPACE.png");
        cellImagePaths.put(CellType.WALL, "png/stone-block.png");
        cellImagePaths.put(CellType.BREAKABLE_BLOCK, "png/ice-block.png");
        return new MapSkin(cellImagePaths);
    }

    /**
     * 任意のキーが離されたときの処理
     */
    public void keyReleasedAction(KeyEvent event) {
        this.isKeyPushed[event.getCode().ordinal()] = false;
    }

    /**
     * 任意のキーが押されたときの処理
     */
    public void keyPressedAction(KeyEvent event) {
        // フラグ配列の更新
        this.isKeyPushed[event.getCode().ordinal()] = true;

        // 以下、プレイヤーの操作 (アイテムの使用も含める)
        if (!isPlayerControllable) {
            return;
        }

        switch (event.getCode()) {
            case SPACE:
                spaceButtonAction();
                break;

            case DIGIT1: // 1 キー
                // シフトキーが同時に押されていれば強制でコインの小道発動, そうでなければゲージボタンの状態などをチェックして発動
                if (isKeyPushed(KeyCode.SHIFT)) {
                    putCoinTrailToGoal();
                } else {
                    requirePutCoinTrail();
                }
                break;

            case DIGIT2: // 2 キー
                // シフトキーが同時に押されていれば強制でコインの杖発動, そうでなければゲージボタンの状態などをチェックして発動
                if (isKeyPushed(KeyCode.SHIFT)) {
                    collectCoinsAroundPlayer(2);
                } else {
                    requireCollectCoinsAroundPlayer(2);
                }
                break;

            case DIGIT3: // 3 キー
                // シフトキーが同時に押されていれば強制でフィーバーモード発動, そうでなければゲージボタンの状態などをチェックして発動
                if (isKeyPushed(KeyCode.SHIFT)) {
                    enterFeverMode();
                } else {
                    requireEnterFeverMode();
                }
                break;

            case ESCAPE: // デバッグ用。暗闇の有効/無効をトグルする。
                this.isBlindEnabled = !this.isBlindEnabled;
                break;

            case F1: // デバッグ用。強制的にゴールしたことにする。
                this.goalAction();
        }
    }

    /**
     * スコアを取得したときに乗算する値。
     * フィーバーモードなら x1.5倍, そうでなければ 1.0 倍
     */
    private double getScoreCoefficient() {
        if (isFeverMode) {
            return 1.5;
        } else {
            return 1.0;
        }
    }

    /**
     * 任意のアイテムを取得したときにこのメソッドを呼び出す必要がある。
     *
     * @param col 取得したアイテムの列番号
     * @param row 取得したアイテムの行番号
     */
    public void itemGetAction(int col, int row) {
        final boolean shouldPlaySE = true;

        switch (this.mapData.getItemType(col, row)) {
            case COIN:
                coinGetAction(col, row, shouldPlaySE);
                break;
            case KEY:
                keyGetAction(col, row, shouldPlaySE);
                break;
            default:
                break;
        }
    }

    /**
     * コインを取得したときに呼び出す。
     * マップの更新、スコアの更新、スコアテキストのエフェクトの登録を行う。
     *
     * @param col    コインの列番号
     * @param row    コインの行番号
     * @param playSE true なら効果音を鳴らす
     */
    private void coinGetAction(int col, int row, boolean playSE) {
        this.mapData.setItemType(col, row, ItemType.NONE);

        final int score = (int) (ItemType.COIN.getScore() * getScoreCoefficient());
        this.headerPanel.incrementScore(score);

        registerScoreTextFloatAnimation(score,
                mapView.getCellDrawnX(col), mapView.getCellDrawnY(row),
                this.topLayerDrawable, isFeverMode);

        if (playSE) {
            this.coinSE.play();
        }
    }

    /**
     * キーを取得したときに呼び出す。
     * マップの更新、スコアの更新、スコアテキストのエフェクトの登録、ガイドメッセージの更新を行う。
     *
     * @param col    キーの列番号
     * @param row    キーの行番号
     * @param playSE true なら効果音を鳴らす
     */
    private void keyGetAction(int col, int row, boolean playSE) {
        this.mapData.setItemType(col, row, ItemType.NONE);

        headerPanel.setGotKeyCount(mapData.countRemovedKeys());

        if (mapData.countExistingKeys() <= 0) {
            this.mapData.setGoalOpen(true);
            this.guideMessage.setMessage("カギを すべて 拾って ゴールの 扉 が開いた！");
        } else {
            final int n = mapData.countExistingKeys();
            this.guideMessage.setMessage("カギを拾った！ のこり " + n + " つ！");
        }

        final int score = (int) (ItemType.KEY.getScore() * getScoreCoefficient());
        this.headerPanel.incrementScore(score);

        registerScoreTextFloatAnimation(score,
                mapView.getCellDrawnX(col), mapView.getCellDrawnY(row),
                this.topLayerDrawable, isFeverMode);

        if (playSE) {
            this.keySE.play();
        }
    }

    /**
     * フェードアウトしながら上昇するスコアテキストのエフェクトを登録する。
     * エフェクトは引数の drawableExecutor に登録される。
     */
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

    /**
     * プレイヤーのマス位置が変化した直後に呼び出される処理。
     * (マス位置であり、描画座標ではない)
     */
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

    /**
     * ゴール処理を行う。
     * 時間計測の停止やBGMの停止、アニメーションの停止、ゴールパネルの登場処理などを行う。
     */
    public void goalAction() {
        this.goalStopwatch.stop();
        this.isPlayerControllable = false;
        this.hasGoaled = true;
        this.normalBGM.stop();
        this.feverBGM.stop();
        this.goalSE.play();

        this.btnCoinTrail.getButton().setButtonEnabled(false);
        this.btnCoinTrail.pause();
        this.btnCollectNeighborCoin.getButton().setButtonEnabled(false);
        this.btnCollectNeighborCoin.pause();
        this.btnFever.getButton().setButtonEnabled(false);
        this.btnFever.pause();

        new TaskScheduleTimer(2000) {
            @Override
            public void task() {
                goalResultPanel = new GoalResultPanel("GOAL!", ((int) goalStopwatch.getCurTime().toSeconds()), headerPanel.getScore(), scene);
                goalResultPanel.getBtnNewMap().setOnMouseClicked(event -> createAndGotoNextMaze());
                goalResultPanel.getBtnToTitle().setOnMouseClicked(event -> {
                    final Phase nextPhase = new TitlePhase(scene);
                    scene.changePhase(nextPhase);
                });

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
     * 次のマップへ遷移する
     */
    public void createAndGotoNextMaze() {
        Phase nextPhase = new MazePhase(this.scene);
        this.scene.changePhase(nextPhase);
    }

    // Operations for going the cat up
    public void moveUpAction() {
        if (player.getMoveTransitionStatus() == Animation.Status.RUNNING) return;
        player.setCharaDirection(MoveChara.TYPE_UP);
        player.moveBy(0, -1, playerMoveDuration);
    }

    // Operations for going the cat down
    public void moveDownAction() {
        if (player.getMoveTransitionStatus() == Animation.Status.RUNNING) return;
        player.setCharaDirection(MoveChara.TYPE_DOWN);
        player.moveBy(0, 1, playerMoveDuration);
    }

    // Operations for going the cat right
    public void moveLeftAction() {
        if (player.getMoveTransitionStatus() == Animation.Status.RUNNING) return;
        player.setCharaDirection(MoveChara.TYPE_LEFT);
        player.moveBy(-1, 0, playerMoveDuration);
    }

    // Operations for going the cat right
    public void moveRightAction() {
        if (player.getMoveTransitionStatus() == Animation.Status.RUNNING) return;
        player.setCharaDirection(MoveChara.TYPE_RIGHT);
        player.moveBy(1, 0, playerMoveDuration);
    }

    /**
     * スペースキーが押されたときの処理。ボムを設置する。
     */
    public void spaceButtonAction() {
        if (bombExecutor.isExistsBombAt(player.getPosCol(), player.getPosRow())) {
            return;
        }

        if (this.isFeverMode) {
            if (bombExecutor.countBombByClass(GoldBomb.class) >= 5) return;
            bombExecutor.registerAndStartCountDown(new GoldBomb(player.getPosCol(), player.getPosRow()));
        } else {
            if (bombExecutor.countBombByClass(NormalBomb.class) >= 3) return;
            bombExecutor.registerAndStartCountDown(new NormalBomb(player.getPosCol(), player.getPosRow()));
        }
    }

    private void requirePutCoinTrail() {
        if (!btnCoinTrail.getButton().isButtonEnabled()) return;
        if (hasGoaled) return;
        putCoinTrailToGoal();
    }

    private void requireCollectCoinsAroundPlayer(int dist) {
        if (!btnCollectNeighborCoin.getButton().isButtonEnabled()) return;
        if (hasGoaled) return;
        collectCoinsAroundPlayer(dist);
    }

    private void requireEnterFeverMode() {
        if (!btnFever.getButton().isButtonEnabled()) return;
        if (hasGoaled) return;
        enterFeverMode();
    }

    /**
    * コインの小道を発動する。
    */
    public void putCoinTrailToGoal() {
        this.btnCoinTrail.gageStartFromEmpty(gageDuration_coinTrail);
        guideMessage.setMessage("コインの小道 発動！ コインの道が作られていく！");

        final List<Pos> path = calcShortestPath(
                this.player.getPos(), this.mapData.getGoalPos(),
                mapData.getWidth(), mapData.getHeight(),
                pos -> mapData.getCellType(pos.col, pos.row).isMovable());

        final AudioClip coinPutSE = new AudioClip(MapGame.getResourceAsString("sound/coin8.wav"));

        for (int i = 0; i < path.size(); i++) {
            final Pos pos = path.get(i);
            if (mapData.getItemType(pos.col, pos.row) != ItemType.NONE) continue;
            if (i == 0 || i == path.size() - 1) continue;

            new TaskScheduleTimer(i * 80L) {
                @Override
                public void task() {
                    if (hasGoaled) return;
                    mapData.setItemType(pos.col, pos.row, ItemType.COIN);
                    coinPutSE.play();
                }
            }.start();
        }
    }

    /**
     * コインの杖を発動する。
     * プレイヤーのマスを (px, py) として、
     * (x, y) s.t. (px - dist <= x <= dx + dist) && (py - dist <= y <= py + dist)
     * であるようなマス (x, y) にあるコイン全てを取得する。
     * 効果音も鳴らす。
     */
    public void collectCoinsAroundPlayer(int dist) {
        this.btnCollectNeighborCoin.gageStartFromEmpty(gageDuration_collectCoins);

        int collectedCoinCount = 0;

        final int centerRow = player.getPosRow();
        final int centerCol = player.getPosCol();
        for (int y = centerRow - dist; y <= centerRow + dist; ++y) {
            for (int x = centerCol - dist; x <= centerCol + dist; ++x) {
                if (y < 0 || x < 0 || y >= mapData.getHeight() || x >= mapData.getWidth()) continue;
                if (mapData.getItemType(x, y) != ItemType.COIN) continue;

                coinGetAction(x, y, false);
                ++collectedCoinCount;

                new TaskScheduleTimer(30L * collectedCoinCount) {
                    @Override
                    public void task() {
                        coinSE.play();
                    }
                }.start();
            }
        }

        if (collectedCoinCount <= 0) {
            guideMessage.setMessage("コインの杖 発動！ しかし周囲 2 マスにコインが無かった...");
        } else {
            guideMessage.setMessage("コインの杖 発動！ " + collectedCoinCount + " 枚のコインをゲットした！");
        }
    }

    /**
     * フィーバースターを発動する。
     * FEVER_KEEP_TIME_MILLI ミリ秒後にフィーバーモードは解除されて通常モードになる。
     */
    public void enterFeverMode() {
        if (this.isFeverMode) return;
        this.btnFever.setGage(0.0);
        guideMessage.setMessage("フィーバーモード！ スペースキーでゴールドボムが使えるぞ！");

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
                btnFever.gageStartFromEmpty(gageDuration_fever);

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
