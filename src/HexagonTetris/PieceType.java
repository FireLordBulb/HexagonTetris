package HexagonTetris;
import java.awt.Color;

public class PieceType {
    private static final Coordinate LEFT_SPAWN_POSITION = new Coordinate(4, 4), RIGHT_SPAWN_POSITION = new Coordinate(5, 5);
    private static final Coordinate NEXT_PIECE_GRID_POSITION = new Coordinate(1, 5);

    // Using a matrix to rotate without doubling isn't possible, because that would require non-integer values in the matrix.
    private static final Coordinate ROTATE_AND_DOUBLE_MATRIX_X = new Coordinate(+1, +3), ROTATE_AND_DOUBLE_MATRIX_Y = new Coordinate(-1, +1);
    public final Color color;
    public final Coordinate spawnPosition;
    public final Coordinate nextPieceGridPosition;
    private final Coordinate[][] rotations;
    public PieceType(int rgb, boolean useRightSpawn, Coordinate... defaultRotation){
        this(new Color(rgb), useRightSpawn, defaultRotation);
    }
    public PieceType(Color color, boolean useRightSpawn, Coordinate... defaultRotation){
        this.color = color;
		spawnPosition = useRightSpawn ? RIGHT_SPAWN_POSITION : LEFT_SPAWN_POSITION;
        nextPieceGridPosition = NEXT_PIECE_GRID_POSITION;
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
                currentRotation[j] = previousRotation[j].applyMatrix(ROTATE_AND_DOUBLE_MATRIX_X, ROTATE_AND_DOUBLE_MATRIX_Y);
                currentRotation[j].divide(2);
            }
        }
        return rotations;
    }
}
