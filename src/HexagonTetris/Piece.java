package HexagonTetris;

import java.util.Random;

public class Piece {
	private static final Random RANDOM = new Random();
	private static final PieceType[] pieceTypes = {
		new PieceType(0xEE5510, 1, false, new Coordinate(-0, -4), new Coordinate(-0, -2), new Coordinate(0, 0), new Coordinate(0, 2)), // I
		new PieceType(0xDDAA10, 0, true , new Coordinate(-1, -1), new Coordinate(+1, -1), new Coordinate(0, 0), new Coordinate(0, 2)), // Y
		new PieceType(0xBB1010, 0, false, new Coordinate(-1, -1), new Coordinate(+1, -1), new Coordinate(0, 0), new Coordinate(0,-2)), // O
		new PieceType(0x108877, 0, true , new Coordinate(-1, -1), new Coordinate(-1, +1), new Coordinate(1, 1), new Coordinate(0, 2)), // U
		new PieceType(0x995510, 2, false, new Coordinate(-1, -3), new Coordinate(-1, -1), new Coordinate(0, 0), new Coordinate(0, 2)), // S
		new PieceType(0xEE4466, 4, true , new Coordinate(+1, -3), new Coordinate(+1, -1), new Coordinate(0, 0), new Coordinate(0, 2)), // Z
		new PieceType(0x10AA10, 2, false, new Coordinate(-0, -2), new Coordinate(-1, -1), new Coordinate(0, 0), new Coordinate(0, 2)), // d
		new PieceType(0x1055CC, 4, true , new Coordinate(-0, -2), new Coordinate(+1, -1), new Coordinate(0, 0), new Coordinate(0, 2)), // b
		new PieceType(0x6610BB, 1, false, new Coordinate(+1, -3), new Coordinate(-0, -2), new Coordinate(0, 0), new Coordinate(0, 2)), // J
		new PieceType(0x2020BB, 5, true , new Coordinate(-1, -3), new Coordinate(-0, -2), new Coordinate(0, 0), new Coordinate(0, 2))  // L
	};
	private final PieceType type;
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
		tryAddToGrid(gridIsPlayBoard);
	}

	public boolean tryMove(Coordinate moveStep){
		for (Coordinate coordinate : hexagonCoordinates){
			grid.setHexagonColor(coordinate, null);
			coordinate.add(moveStep);
		}
		boolean moveWasBlocked = isBlocked();
		if (moveWasBlocked){
			for (Coordinate coordinate : hexagonCoordinates){
				coordinate.subtract(moveStep);
				grid.setHexagonColor(coordinate, type.color);
			}
		} else {
			for (Coordinate coordinate : hexagonCoordinates){
				grid.setHexagonColor(coordinate, type.color);
			}
		}
		return moveWasBlocked;
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

	public boolean tryChangeGrid(HexagonGrid hexagonGrid, boolean gridIsPlayBoard){
		for (Coordinate coordinate : hexagonCoordinates){

			grid.setHexagonColor(coordinate, null);
		}
		grid = hexagonGrid;
		return tryAddToGrid(gridIsPlayBoard);
	}

	private boolean tryAddToGrid(boolean gridIsPlayBoard){
		if (gridIsPlayBoard){
			rotationIndex = type.spawnRotation;
		}
		Coordinate[] pieceRelativeCoordinates = type.getRotation(rotationIndex);
		Coordinate pivotAbsolutePosition = gridIsPlayBoard ? type.spawnPosition : type.nextPieceGridPosition;
		boolean addFailed = false;
		for (int i = 0; i < hexagonCoordinates.length; i++){
			hexagonCoordinates[i] = Coordinate.add(pieceRelativeCoordinates[i], pivotAbsolutePosition);
			if (grid.getHexagonColor(hexagonCoordinates[i]) != null){
				addFailed = true;
			}
			grid.setHexagonColor(hexagonCoordinates[i], type.color);
		}
		return !addFailed;
	}

	private boolean isBlocked(){
		for (Coordinate coordinate : hexagonCoordinates){
			Hexagon hexagon = grid.getHexagonAt(coordinate);
			if (hexagon == null || grid.getHexagonColor(coordinate) != null){
				return true;
			}
		}
		return false;
	}
}
