import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.EnumMap;

public class MazePhase implements Phase {
    private MapGameScene scene;

    private final MapData mapData;
    private final MapView mapView;
    private final MoveChara player;

    public MazePhase() {
        this.mapData = new MapData(21, 15);
        this.mapView = new MapView(mapData, 32, createDefaultMapSkin());
        this.player = new MoveChara(mapData.getPlayerStartX(), mapData.getPlayerStartY(), mapData);
    }

    @Override
    public void setup(MapGameScene scene) {
        this.scene = scene;
        this.scene.setOnKeyPressed(this::keyPressedAction);
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
    }

    private static MapSkin createDefaultMapSkin() {
        EnumMap<CellType, String> cellImagePaths = new EnumMap<>(CellType.class);
        cellImagePaths.put(CellType.SPACE, "png/SPACE.png");
        cellImagePaths.put(CellType.WALL, "png/WALL.png");
        return new MapSkin(cellImagePaths);
    }

    // Get users key actions
    public void keyPressedAction(KeyEvent event) {
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

    public void itemGetAction(int col, int row) {
        this.mapData.setItemType(col, row, ItemType.NONE);

        if (mapData.countExistingKeys() <= 0) {
            this.mapData.setGoalOpen(true);
        }
    }

    public void goalAction() {
        System.out.println("Goal!!!!!!!!!!!!!!!!!");
        createAndGotoNextMaze();
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
