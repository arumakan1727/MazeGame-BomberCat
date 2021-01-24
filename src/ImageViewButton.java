import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public class ImageViewButton extends ImageView {
    public ImageViewButton(Image image, Scene scene) {
        super(image);
        this.setCache(true);

        final DropShadow dropShadow = new DropShadow(3, 2, 2, Color.BLACK);

        this.setOnMouseEntered(evt -> {
            scene.setCursor(Cursor.HAND);
            this.setEffect(dropShadow);
        });
        this.setOnMouseExited(evt -> {
            scene.setCursor(Cursor.DEFAULT);
            this.setScaleX(1.0);
            this.setScaleY(1.0);
            this.setEffect(null);
        });
        this.setOnMousePressed(evt -> {
            this.setScaleX(0.95);
            this.setScaleY(0.95);
        });
        this.setOnMouseReleased(evt -> {
            this.setScaleX(1.0);
            this.setScaleY(1.0);
        });
    }
}
