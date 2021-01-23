import javafx.application.Application;
import javafx.stage.Stage;

public class MapGame extends Application {
    Stage stage;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        primaryStage.setTitle("MAP GAME");
        MapGameScene myScene = MapGameScene.create();
        primaryStage.setScene(myScene);
        primaryStage.show();

        myScene.startGame(new MazePhase());
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static String getResourceAsString(String path) {
        return MapGame.class.getResource(path).toString();
    }
}
