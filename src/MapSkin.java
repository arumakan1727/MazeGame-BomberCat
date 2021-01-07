import javafx.scene.image.Image;

import java.util.EnumMap;

public class MapSkin {
    private final EnumMap<CellType, String> cellImageFilePaths;
    private final EnumMap<CellType, Image> cellImages;

    public MapSkin(EnumMap<CellType, String> cellImageFilePaths) {
        this.cellImageFilePaths = cellImageFilePaths;
        this.cellImages = new EnumMap<>(CellType.class);

        this.cellImageFilePaths.forEach((cellType, filePath) -> {
            Image image = new Image(filePath);
            this.cellImages.put(cellType, image);
        });
    }

    public String getCellImageFilePath(CellType cellType) {
        return this.cellImageFilePaths.get(cellType);
    }

    public Image getCellImage(CellType cellType) {
        return this.cellImages.get(cellType);
    }
}
