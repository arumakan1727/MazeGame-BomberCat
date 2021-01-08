import javafx.event.ActionEvent;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.EnumMap;

public class MazePhase implements Phase {
    private MapGameScene scene;

    private MapData mapData;
    private MapView mapView;
    private MoveChara player;

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

    }

    @Override
    public void update(long now) {

    }

    @Override
    public void draw(GraphicsContext gc) {
        mapView.draw(gc);

        // Todo draw item

        player.draw(gc, mapView);

        // Todo draw Effect
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
        System.out.println("keycode:" + key);
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

    // Operations for going the cat down
    public void upButtonAction() {
        printAction("UP");
        player.setCharaDirection(MoveChara.TYPE_UP);
        player.moveBy(0, -1);
    }

    // Operations for going the cat down
    public void downButtonAction() {
        printAction("DOWN");
        player.setCharaDirection(MoveChara.TYPE_DOWN);
        player.moveBy(0, 1);
    }

    // Operations for going the cat right
    public void leftButtonAction() {
        printAction("LEFT");
        player.setCharaDirection(MoveChara.TYPE_LEFT);
        player.moveBy(-1, 0);
    }

    // Operations for going the cat right
    public void rightButtonAction() {
        printAction("RIGHT");
        player.setCharaDirection(MoveChara.TYPE_RIGHT);
        player.moveBy(1, 0);
    }

    public void func1ButtonAction(ActionEvent event) {
        System.out.println("func1: Nothing to do");
    }

    // Print actions of user inputs
    public void printAction(String actionString) {
        System.out.println("Action: " + actionString);
    }

}
