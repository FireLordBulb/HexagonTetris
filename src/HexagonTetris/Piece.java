package HexagonTetris;

import java.util.Random;

public class Piece {
	private static final Random RANDOM = new Random();
	private static final PieceType[] pieceTypes = {
		new PieceType(0xEE5510, false, new Coordinate(-0, -4), new Coordinate(-0, -2), new Coordinate(0, 0), new Coordinate(0, 2)), // I
		new PieceType(0xDDAA10, false, new Coordinate(-1, -1), new Coordinate(+1, -1), new Coordinate(0, 0), new Coordinate(0, 2)), // Y
		new PieceType(0x108877, false, new Coordinate(+1, -1), new Coordinate(-1, +1), new Coordinate(1, 1), new Coordinate(0, 2)), // U
		new PieceType(0xBB1010, false, new Coordinate(-1, +1), new Coordinate(+1, +1), new Coordinate(0, 0), new Coordinate(0, 2)), // O
		new PieceType(0xEE4466, false, new Coordinate(+1, -3), new Coordinate(+1, -1), new Coordinate(0, 0), new Coordinate(0, 2)), // Z
		new PieceType(0x995510, true , new Coordinate(-1, -3), new Coordinate(-1, -1), new Coordinate(0, 0), new Coordinate(0, 2)), // S
		new PieceType(0x1055CC, false, new Coordinate(-0, -2), new Coordinate(+1, -1), new Coordinate(0, 0), new Coordinate(0, 2)), // b
		new PieceType(0x10AA10, true , new Coordinate(-0, -2), new Coordinate(-1, -1), new Coordinate(0, 0), new Coordinate(0, 2)), // d
		new PieceType(0x6610BB, false, new Coordinate(+1, -3), new Coordinate(-0, -2), new Coordinate(0, 0), new Coordinate(0, 2)), // J
		new PieceType(0x2020BB, true , new Coordinate(-1, -3), new Coordinate(-0, -2), new Coordinate(0, 0), new Coordinate(0, 2))  // L
	};
	public final PieceType type;
	private final Coordinate[] hexagonCoordinates;
	public int rotationIndex;
	private HexagonGrid grid;
	public Piece(HexagonGrid grid, boolean gridIsPlayBoard){
		this(pieceTypes[RANDOM.nextInt(pieceTypes.length)], grid, gridIsPlayBoard);
	}
	public Piece(PieceType pieceType, HexagonGrid hexagonGrid, boolean gridIsPlayBoard){
		type = pieceType;
		rotationIndex = 0;
		hexagonCoordinates = new Coordinate[type.hexagonCount];
		grid = hexagonGrid;
		addToGrid(gridIsPlayBoard);
	}
	public void rotate(int rotationChange){
		int newRotationIndex = (((rotationIndex + rotationChange) % Hexagon.NUM_POINTS) + Hexagon.NUM_POINTS) % Hexagon.NUM_POINTS;
		Coordinate[] currentRotationCoordinates = type.getRotation(rotationIndex);
		Coordinate[] newRotationCoordinates = type.getRotation(newRotationIndex);
		for (int i = 0; i < type.hexagonCount; i++){
			Coordinate coordinate = hexagonCoordinates[i];
			grid.setHexagonColor(coordinate, null);
			coordinate.subtract(currentRotationCoordinates[i]);
			coordinate.add(newRotationCoordinates[i]);
		}
		if (isBlocked()){
			for (int i = 0; i < type.hexagonCount; i++){
				Coordinate coordinate = hexagonCoordinates[i];
				coordinate.subtract(newRotationCoordinates[i]);
				coordinate.add(currentRotationCoordinates[i]);
				grid.setHexagonColor(coordinate, type.color);
			}
			return;
		}
		for (Coordinate coordinate : hexagonCoordinates){
			grid.setHexagonColor(coordinate, type.color);
		}
		rotationIndex = newRotationIndex;
	}

	private void addToGrid(boolean gridIsPlayBoard){
		Coordinate[] pieceRelativeCoordinates = type.getRotation(rotationIndex);
		for (int i = 0; i < hexagonCoordinates.length; i++){
			hexagonCoordinates[i] = Coordinate.add(pieceRelativeCoordinates[i], gridIsPlayBoard ? type.spawnPosition : type.nextPieceGridPosition);
			grid.setHexagonColor(hexagonCoordinates[i], type.color);
		}
	}
	// Getters and Setters.
	public void setGrid(HexagonGrid hexagonGrid, boolean gridIsPlayBoard){
		for (Coordinate coordinate : hexagonCoordinates){
			grid.setHexagonColor(coordinate, null);
		}
		grid = hexagonGrid;
		addToGrid(gridIsPlayBoard);
	}
	public boolean isBlocked(){
		for (Coordinate coordinate : hexagonCoordinates){
			Hexagon hexagon = grid.getHexagonAt(coordinate);
			if (hexagon == null || grid.getHexagonColor(coordinate) != null){
				return true;
			}
		}
		return false;
	}
	public Coordinate getCoordinate(int index){
		return hexagonCoordinates[index];
	}
	public Coordinate[] getCoordinates(){
		return hexagonCoordinates;
	}
}
