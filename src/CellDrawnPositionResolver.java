public interface CellDrawnPositionResolver {
    int getCellDrawnX(int col);
    int getCellDrawnY(int row);
    int getCellSize();
    int getMapHeight();
    int getMapWidth();
    int getMapTopY();
    int getMapLeftX();
}
