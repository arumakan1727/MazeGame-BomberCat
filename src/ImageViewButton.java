import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageViewButton extends ImageView {
    public ImageViewButton(Image image, Scene scene) {
        super(image);

        this.setOnMouseEntered(evt -> {
            scene.setCursor(Cursor.HAND);
        });
        this.setOnMouseExited(evt -> {
            scene.setCursor(Cursor.DEFAULT);
            this.setScaleX(1.0);
            this.setScaleY(1.0);
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
