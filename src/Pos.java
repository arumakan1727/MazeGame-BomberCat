import java.util.Objects;

public final class Pos {
    public final int row;
    public final int col;

    public Pos(int col, int row) {
        this.col = col;
        this.row = row;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pos pos = (Pos) o;
        return row == pos.row && col == pos.col;
    }

    @Override
    public String toString() {
        return "Pos(row=" + row + ", col=" + col + ")";
    }
}
