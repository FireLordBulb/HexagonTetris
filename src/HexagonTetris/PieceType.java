package HexagonTetris;
import java.awt.Color;

public class PieceType {
    private static final Coordinate leftSpawnPosition = new Coordinate(4, 4), rightSpawnPosition = new Coordinate(5, 5);
    // Using a matrix to rotate without doubling isn't possible, because that would require non-integer values in the matrix.
    private static final Coordinate rotateAndDoubleMatrixX = new Coordinate(+1, +3), rotateAndDoubleMatrixY = new Coordinate(-1, +1);
    public final Color color;
    public final Coordinate spawnPosition;
    private final Coordinate[][] rotations;
    public PieceType(int rgb, boolean useRightSpawn, Coordinate... defaultRotation){
        this(new Color(rgb), useRightSpawn, defaultRotation);
    }
    public PieceType(Color color, boolean useRightSpawn, Coordinate... defaultRotation){
        this.color = color;
        spawnPosition = useRightSpawn ? rightSpawnPosition : leftSpawnPosition;
        rotations = generateRotations(defaultRotation);
    }

    public Coordinate[] getRotation(int i){
        return rotations[i];
    }

    private static Coordinate[][] generateRotations(Coordinate[] defaultRotation){
        Coordinate[][] rotations = new Coordinate[Hexagon.NUM_POINTS][defaultRotation.length];
        rotations[0] = defaultRotation;
        for (int i = 1; i < Hexagon.NUM_POINTS; i++){
            Coordinate[] currentRotation = rotations[i];
            Coordinate[] previousRotation = rotations[i-1];
            for (int j = 0; j < defaultRotation.length; j++){
                currentRotation[j] = previousRotation[j].applyMatrix(rotateAndDoubleMatrixX, rotateAndDoubleMatrixY);
                currentRotation[j].divide(2);
            }
        }
        return rotations;
    }
}
