import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class GoalResultPanel {
    private static final int WIDTH = 500;
    private static final int HEIGHT = 300;
    private static final int MARGIN_BUTTON_SIDE = 50;
    private static final int MARGIN_BUTTON_BOTTOM = 20;
    private static final int COL1_LEFT_X = 90;
    private static final int COL2_LEFT_X = (int) (WIDTH * 0.55);
    private static final int ROW1_BOTTOM_Y = 120;
    private static final int ROW2_BOTTOM_Y = 180;

    private final String title;
    private final int goalTimeSec;
    private final int score;
    private int x;
    private int y;
    private final ImageViewButton btnToTitle;
    private final ImageViewButton btnNewMap;

    public GoalResultPanel(String title, int goalTimeSec, int score, MapGameScene mapGameScene) {
        this.title = title;
        this.goalTimeSec = goalTimeSec;
        this.score = score;
        this.btnToTitle = new ImageViewButton(new Image("png/button-to-title.png"), mapGameScene);
        this.btnNewMap = new ImageViewButton(new Image("png/button-new-map.png"), mapGameScene);

        mapGameScene.getOtherComponents().getChildren().add(this.btnToTitle);
        mapGameScene.getOtherComponents().getChildren().add(this.btnNewMap);
    }

    public void draw(GraphicsContext gc) {
        gc.save();
        gc.translate(this.x, this.y);
        this.drawPanel(gc);
        this.drawTitle(gc);
        this.drawGoalTime(gc);
        this.drawScore(gc);
        gc.restore();
    }

    private void drawPanel(GraphicsContext gc) {
        gc.setLineWidth(16);
        gc.setStroke(Color.BROWN);
        gc.strokeRoundRect(0, 0, WIDTH, HEIGHT, 16, 16);
        gc.setFill(Color.WHEAT);
        gc.fillRoundRect(0, 0, WIDTH, HEIGHT, 16, 16);
    }

    private void drawTitle(GraphicsContext gc) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.setFill(Color.MAROON);

        gc.setFont(Font.font("sans-serif", FontWeight.BLACK, 36));
        gc.fillText(this.title, WIDTH / 2, 10);
    }

    private void drawGoalTime(GraphicsContext gc) {
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.BOTTOM);
        gc.setFill(Color.MAROON);

        gc.setFont(Font.font("sans-serif", FontWeight.BOLD, 24));
        gc.fillText("  Goal time:", COL1_LEFT_X, ROW1_BOTTOM_Y);
        gc.fillText(this.goalTimeSec + " sec", COL2_LEFT_X, ROW1_BOTTOM_Y);

        gc.setLineWidth(2);
        final int lineY = ROW1_BOTTOM_Y + 5;
        gc.strokeLine(COL1_LEFT_X, lineY, WIDTH - COL1_LEFT_X, lineY);
    }

    private void drawScore(GraphicsContext gc) {
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.BOTTOM);
        gc.setFill(Color.MAROON);

        gc.setFont(Font.font("sans-serif", FontWeight.BOLD, 24));
        gc.fillText("  Score:", COL1_LEFT_X, ROW2_BOTTOM_Y);
        gc.fillText(this.score + "", COL2_LEFT_X, ROW2_BOTTOM_Y);

        gc.setLineWidth(2);
        final int lineY = ROW2_BOTTOM_Y + 5;
        gc.strokeLine(COL1_LEFT_X, lineY, WIDTH - COL1_LEFT_X, lineY);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
        this.btnToTitle.setLayoutX(x + MARGIN_BUTTON_SIDE);
        this.btnNewMap.setLayoutX(x + WIDTH - MARGIN_BUTTON_SIDE - this.btnNewMap.getImage().getWidth());
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
        this.btnToTitle.setLayoutY(y + HEIGHT - MARGIN_BUTTON_BOTTOM - this.btnToTitle.getImage().getHeight());
        this.btnNewMap.setLayoutY(y + HEIGHT - MARGIN_BUTTON_BOTTOM - this.btnNewMap.getImage().getHeight());
    }
}
