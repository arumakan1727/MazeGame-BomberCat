import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

// ガイドメッセージ用クラス
public class MessageArea {
    private int x;
    private int y;
    private int width;
    private int height;
    private String message;
    private Font font;
    private Paint messageColor;
    private Paint backColor;

    private int paddingLeft;

    public MessageArea(int x, int y, int width, int height, Font font) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.font = font;
        this.messageColor = Color.WHITE;
        this.backColor = Color.BLACK;

        this.setPaddingLeft(5);
    }

    public void setMessage(String msg) {
        this.message = msg;
    }

    public void draw(GraphicsContext gc) {
        gc.save();

        // draw background
        gc.setFill(this.backColor);
        gc.fillRect(this.x, this.y, this.width, this.height);

        // draw message
        gc.setFill(this.messageColor);
        gc.setFont(this.font);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(this.message, this.x + this.paddingLeft, this.y + (this.height / 2));

        gc.restore();
    }

    public Paint getMessageColor() {
        return messageColor;
    }

    public void setMessageColor(Paint messageColor) {
        this.messageColor = messageColor;
    }

    public Paint getBackColor() {
        return backColor;
    }

    public void setBackColor(Paint backColor) {
        this.backColor = backColor;
    }

    public int getPaddingLeft() {
        return paddingLeft;
    }

    public void setPaddingLeft(int paddingLeft) {
        this.paddingLeft = paddingLeft;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
