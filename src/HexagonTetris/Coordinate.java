package HexagonTetris;

public class Coordinate {
    public int x;
    public int y;
    public Coordinate(int x, int y){
        this.x = x;
        this.y = y;
    }
    public void add(Coordinate other){
       x += other.x;
       y += other.y;
    }
    public void subtract(Coordinate other){
        x -= other.x;
        y -= other.y;
    }
    public void divide(int denominator){
        x /= denominator;
        y /= denominator;
    }
    public Coordinate applyMatrix(Coordinate xColumn, Coordinate yColumn){
        return new Coordinate(x*xColumn.x + y*yColumn.x, x*xColumn.y + y*yColumn.y);
    }
    public static Coordinate multiply(Coordinate coordinate, int scalar){
        return new Coordinate(coordinate.x * scalar, coordinate.y * scalar);
    }
    public static Coordinate add(Coordinate a, Coordinate b){
        return new Coordinate(a.x+b.x, a.y+b.y);
    }
}
