import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.EnumMap;
import java.util.Timer;
import java.util.TimerTask;

public class MazePhase implements Phase {
    private MapGameScene scene;

    private final MapData mapData;
    private final MapView mapView;
    private final MoveChara player;

    private final AudioClip bgm;
    private final AudioClip coinSE;
    private final AudioClip keySE;
    private final AudioClip goalSE;

    private final MessageArea guideMessage;

    private int score = 0;
    private boolean isPlayerControllable = true;

    public MazePhase() {
        this.mapData = new MapData(21, 15);
        this.mapView = new MapView(mapData, 32, createDefaultMapSkin());
        this.player = new MoveChara(mapData.getPlayerStartX(), mapData.getPlayerStartY(), mapData);

        this.bgm = new AudioClip(MapGame.getResourceAsString("sound/bgm_maoudamashii_8bit18.mp3"));
        this.bgm.setVolume(0.3);
        this.bgm.setCycleCount(AudioClip.INDEFINITE);

        this.coinSE = new AudioClip(MapGame.getResourceAsString("sound/coin1.wav"));
        this.keySE = new AudioClip(MapGame.getResourceAsString("sound/se_maoudamashii_system46.mp3"));
        this.goalSE = new AudioClip(MapGame.getResourceAsString("sound/goal.wav"));

        this.mapView.setMapTopY(40);

        final Font pixelFont = Font.loadFont(MapGame.getResourceAsString("font/PixelMplus12-Regular.ttf"), 16);
        this.guideMessage = new MessageArea(0, this.mapView.getMapBottomY(), this.mapView.getMapWidth(), 28, pixelFont);
    }

    @Override
    public void setup(MapGameScene scene) {
        this.scene = scene;
        this.scene.setOnKeyPressed(this::keyPressedAction);
        this.bgm.play();

        this.guideMessage.setMessage("カギを すべて 拾って ゴールの 扉 を開けよう！");
    }

    @Override
    public void tearDown() {
        this.scene.setOnKeyPressed(null);
    }

    @Override
    public void update(long now) {

    }

    @Override
    public void draw(GraphicsContext gc) {
        mapView.draw(gc);
        player.draw(gc, mapView);
        this.drawScore(gc);
        this.guideMessage.draw(gc);
    }

    private static MapSkin createDefaultMapSkin() {
        EnumMap<CellType, String> cellImagePaths = new EnumMap<>(CellType.class);
        cellImagePaths.put(CellType.SPACE, "png/SPACE.png");
        cellImagePaths.put(CellType.WALL, "png/stone-block.png");
        cellImagePaths.put(CellType.BREAKABLE_BLOCK, "png/ice-block.png");
        return new MapSkin(cellImagePaths);
    }

    // Get users key actions
    public void keyPressedAction(KeyEvent event) {
        if (!isPlayerControllable) {
            return;
        }

        KeyCode key = event.getCode();
        if (key == KeyCode.H || key == KeyCode.LEFT) {
            leftButtonAction();
        } else if (key == KeyCode.J || key == KeyCode.DOWN) {
            downButtonAction();
        } else if (key == KeyCode.K || key == KeyCode.UP) {
            upButtonAction();
        } else if (key == KeyCode.L || key == KeyCode.RIGHT) {
            rightButtonAction();
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
    }

    private void actionAfterPlayerMove() {
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
        player.setCharaDirection(MoveChara.TYPE_UP);
        player.moveBy(0, -1);
    }

    // Operations for going the cat down
    public void downButtonAction() {
        player.setCharaDirection(MoveChara.TYPE_DOWN);
        player.moveBy(0, 1);
    }

    // Operations for going the cat right
    public void leftButtonAction() {
        player.setCharaDirection(MoveChara.TYPE_LEFT);
        player.moveBy(-1, 0);
    }

    // Operations for going the cat right
    public void rightButtonAction() {
        player.setCharaDirection(MoveChara.TYPE_RIGHT);
        player.moveBy(1, 0);
    }
}
