import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

public class HeaderPanel {
    private int x;
    private int y;
    private int width;
    private int height;
    private int score = 0;
    private int elapsedTime = 0;
    private int gotKeyCount = 0;
    private int maxKeyCount = 3;
    private final List<TimeGageImageViewButton> centerButtons;

    private final Image keyImageActive;
    private final Image keyImageInactive;

    public HeaderPanel(int x, int y, int width, int height) {
        this.centerButtons = new ArrayList<>();
        this.keyImageActive = new Image("png/key-32x32.png");
        this.keyImageInactive = new Image("png/key-icon-heart-gray.png");

        this.width = width;
        this.height = height;
        this.setX(x);
        this.setY(y);
    }

    public void draw(GraphicsContext gc) {
        gc.translate(this.x, this.y);
        this.drawPanel(gc);
        centerButtons.forEach(button -> button.draw(gc));
        this.drawKeys(gc);
        this.drawScoreAndTime(gc);
    }

    private void drawPanel(GraphicsContext gc) {
        final int lw = 2;
        gc.setFill(Color.PAPAYAWHIP);
        gc.setStroke(Color.LIGHTPINK);
        gc.setLineWidth(lw);
        gc.fillRect(0, 0, width, height);
        gc.strokeRect(lw, lw, width - lw, height - lw);
    }

    private void drawKeys(GraphicsContext gc) {
        final int marginLeft = 10;
        final int marginSide = 5;
        final int keyBoxSize = 32;
        final int y = (this.height - keyBoxSize) / 2;

        for (int i = 0; i < maxKeyCount; ++i) {
            final int x = marginLeft + (keyBoxSize + marginSide) * i;
            gc.setFill(Color.SILVER);
            gc.setStroke(Color.PERU);
            gc.setLineWidth(3);
            gc.strokeRoundRect(x, y, keyBoxSize, keyBoxSize, 4, 4);
            gc.fillRoundRect(x, y, keyBoxSize, keyBoxSize, 4, 4);

            if (i < gotKeyCount) {
                gc.drawImage(keyImageActive, x, y, keyBoxSize, keyBoxSize);
            } else {
                final double tmp = gc.getGlobalAlpha();
                gc.setGlobalAlpha(0.2);
                gc.drawImage(keyImageInactive, x, y, keyBoxSize, keyBoxSize);
                gc.setGlobalAlpha(tmp);
            }
        }
    }

    private void drawScoreAndTime(GraphicsContext gc) {
        gc.setFont(Font.font("sans-serif", FontWeight.NORMAL, height * 0.30));
        gc.setFill(Color.MAROON);
        final int middleY = height / 2;
        final int col1X = width - 150;
        final int col2X = col1X + 60;

        gc.setTextBaseline(VPos.BOTTOM);
        gc.fillText("Score:", col1X, middleY - 1);
        gc.fillText(score + "", col2X, middleY - 1);

        gc.setTextBaseline(VPos.TOP);
        gc.fillText("Time:", col1X, middleY + 1);
        gc.fillText(formatTime(elapsedTime) + "", col2X, middleY + 1);
    }

    public void addCenterButton(TimeGageImageViewButton button) {
        this.centerButtons.add(button);
        realignCenterButtons();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
        realignCenterButtons();
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
        realignCenterButtons();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void incrementScore(int incValue) {
        this.score += incValue;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public int getGotKeyCount() {
        return gotKeyCount;
    }

    public void setGotKeyCount(int gotKeyCount) {
        this.gotKeyCount = gotKeyCount;
    }

    public int getMaxKeyCount() {
        return maxKeyCount;
    }

    public void setMaxKeyCount(int maxKeyCount) {
        this.maxKeyCount = maxKeyCount;
    }

    private void realignCenterButtons() {
        final int sideMargin = 2;
        final int totalButtonWidth = centerButtons.stream()
                .mapToInt(btn -> (int) btn.getWidth())
                .sum()
                + (sideMargin * (centerButtons.size() - 1));

        final int marginLeft = (this.width - totalButtonWidth) / 2;
        int widthSum = 0;
        for (int i = 0; i < centerButtons.size(); ++i) {
            final var btn = centerButtons.get(i);
            btn.getButton().setLayoutX(this.x + marginLeft + widthSum + i * sideMargin);
            btn.getButton().setLayoutY(this.y + (this.height - btn.getHeight()) / 2);
            widthSum += btn.getWidth();
        }
    }

    private static String formatTime(int sec) {
        return String.format("%01dm %02ds", sec / 60, sec % 60);
    }
}
