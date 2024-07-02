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
	public Piece(boolean doSpawnImmediately, HexagonGrid grid){
		this(pieceTypes[RANDOM.nextInt(pieceTypes.length)], doSpawnImmediately, grid);
	}
	public Piece(PieceType pieceType, boolean doSpawnImmediately, HexagonGrid grid){
		type = pieceType;
		rotationIndex = 0;
		Coordinate[] pieceRelativeCoordinates = type.getRotation(rotationIndex);
		hexagonCoordinates = new Coordinate[pieceRelativeCoordinates.length];
		for (int i = 0; i < hexagonCoordinates.length; i++){
			hexagonCoordinates[i] = Coordinate.add(pieceRelativeCoordinates[i], doSpawnImmediately ? type.spawnPosition : type.nextPieceGridPosition);
			grid.setHexagonColor(hexagonCoordinates[i], type.color);
		}
	}
	public void removeFromGrid(HexagonGrid grid){
		for (Coordinate coordinate : hexagonCoordinates){
			grid.setHexagonColor(coordinate, null);
		}
	}
	public Coordinate getCoordinate(int index){
		return hexagonCoordinates[index];
	}
	public Coordinate[] getCoordinates(){
		return hexagonCoordinates;
	}
}
