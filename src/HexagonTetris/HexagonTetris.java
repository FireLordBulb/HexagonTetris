package HexagonTetris;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class HexagonTetris extends JPanel {
	// Public static constants.
	public static final Color BackgroundColor = new Color(0x202020);
	// Private static constants.
	private static final Random RANDOM = new Random();
	private static final int HEXAGON_SIZE = 20;
	private static final int COLUMNS = 10, ROWS = 24, INVISIBLE_ROWS = 4, HALF_ROWS = ROWS*2;
	private static final int NEXT_PIECE_COLUMNS = 3, NEXT_PIECE_ROWS = 4;
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
	private static final Coordinate oneStepDown = new Coordinate(0, 2);
	private static final int LINE_CLEAR_ANIMATION_TIME = 1000;
	// Final instance fields (collections).
	private final HexagonGrid board = new HexagonGrid(COLUMNS, ROWS, HEXAGON_SIZE, 0, 0);
	private final HexagonGrid nextPieceWindow = new HexagonGrid(NEXT_PIECE_COLUMNS, NEXT_PIECE_ROWS, HEXAGON_SIZE, 18, 6);
	private final List<Integer> completeHalfRows = new ArrayList<>();
	// Changeable state.
	private Timer fallTimer = null;
	private Coordinate[] currentPiece = null;
	private Coordinate[] nextPiece = null;
	private PieceType currentPieceType = null;
	private PieceType nextPieceType = null;
	private int currentRotation = 0;
	private boolean pieceIsAtLowest = false;
	private boolean isInAnimation = false;
	private int timePerFall = 800;

	// Constructor. |------------------------------------------------------------------------------------------
	public HexagonTetris(){
		setFocusable(true);
		setBackground(BackgroundColor);
		addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent evt){
				if (isInAnimation){
					return;
				}
				switch(evt.getKeyCode()){
					case KeyEvent.VK_LEFT, KeyEvent.VK_A -> movePieceToSide(false);
					case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> movePieceToSide(true);
					case KeyEvent.VK_UP, KeyEvent.VK_W, KeyEvent.VK_X -> rotatePiece(+1);
					case KeyEvent.VK_Z -> rotatePiece(-1);
					case KeyEvent.VK_DOWN, KeyEvent.VK_S -> tryMovePieceDown();
					case KeyEvent.VK_SPACE -> {
						// The while loop is empty because tryMovePieceDown has side effects.
						// noinspection StatementWithEmptyBody
						while (!tryMovePieceDown());
					}
				}
			}
		});
		nextPieceType = pieceTypes[RANDOM.nextInt(pieceTypes.length)];
		nextPiece = new Coordinate[0];
		spawnNewPiece();
		repaint();
		startFallTimer();
	}
	// Drawing methods. |------------------------------------------------------------------------------------------
	@Override
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke(new BasicStroke(1));
		g2d.translate(150, -100);
		board.draw(g2d, INVISIBLE_ROWS, false);
		nextPieceWindow.draw(g2d, 0, true);
	}

	// Meat and bones methods. |------------------------------------------------------------------------------------------
	private void movePieceToSide(boolean sideIsRight){
		Coordinate moveStep = new Coordinate(sideIsRight ? +1 : -1, pieceIsAtLowest ? -1 : +1);
		boolean moveWasBlocked = tryMovePiece(moveStep);
		if (moveWasBlocked){
			// If the move was slightly upwards and was blocked, try again but slightly downwards.
			if (moveStep.y == -1){
				moveStep.y = +1;
				tryMovePiece(moveStep);
			}
			return;
		}
		// Toggle this boolean to make the piece alternate between moving to the side and slightly up and moving to the side and slightly down.
		pieceIsAtLowest = !pieceIsAtLowest;
	}
	private boolean tryMovePieceDown(){
		boolean fallWasBlocked = tryMovePiece(oneStepDown);
		if (fallWasBlocked){
			findCompleteLines();
		} else {
			pieceIsAtLowest = false;
		}
		return fallWasBlocked;
	}
	private boolean tryMovePiece(Coordinate moveStep){
		for (Coordinate coordinate : currentPiece){
			board.setHexagonColor(coordinate, null);
			coordinate.add(moveStep);
		}
		boolean moveWasBlocked = false;
		for (Coordinate coordinate : currentPiece){
			Hexagon hexagon = board.getHexagonAt(coordinate);
			if (hexagon == null || board.getHexagonColor(coordinate) != null){
				moveWasBlocked = true;
				break;
			}
		}
		if (moveWasBlocked){
			for (Coordinate coordinate : currentPiece){
				coordinate.subtract(moveStep);
				board.setHexagonColor(coordinate, currentPieceType.color);
			}
		} else {
			for (Coordinate coordinate : currentPiece){
				board.setHexagonColor(coordinate, currentPieceType.color);
			}
			repaint();
		}
		return moveWasBlocked;
	}

	private void rotatePiece(int rotationChange){
		int newRotation = (((currentRotation +  rotationChange) % Hexagon.NUM_POINTS) + Hexagon.NUM_POINTS) % Hexagon.NUM_POINTS;
		Coordinate[] currentRotationCoordinates = currentPieceType.getRotation(currentRotation);
		Coordinate[] newRotationCoordinates = currentPieceType.getRotation(newRotation);
		for (int i = 0; i < currentPiece.length; i++){
			Coordinate coordinate = currentPiece[i];
			board.setHexagonColor(coordinate, null);
			coordinate.subtract(currentRotationCoordinates[i]);
			coordinate.add(newRotationCoordinates[i]);
		}
		boolean rotationWasBlocked = false;
		for (Coordinate coordinate : currentPiece){
			Hexagon hexagon = board.getHexagonAt(coordinate);
			if (hexagon == null || board.getHexagonColor(coordinate) != null){
				rotationWasBlocked = true;
				break;
			}
		}
		if (rotationWasBlocked){
			for (int i = 0; i < currentPiece.length; i++){
				Coordinate coordinate = currentPiece[i];
				coordinate.subtract(newRotationCoordinates[i]);
				coordinate.add(currentRotationCoordinates[i]);
				board.setHexagonColor(coordinate, currentPieceType.color);
			}
			return;
		}
		for (Coordinate coordinate : currentPiece){
			board.setHexagonColor(coordinate, currentPieceType.color);
		}
		currentRotation = newRotation;
		repaint();
	}

	private void spawnNewPiece(){
		currentPieceType = nextPieceType;
		currentRotation = 0;
		Coordinate[] pieceRelativeCoordinates = currentPieceType.getRotation(currentRotation);
		currentPiece = new Coordinate[pieceRelativeCoordinates.length];
		for (int i = 0; i < currentPiece.length; i++){
			currentPiece[i] = Coordinate.add(pieceRelativeCoordinates[i], currentPieceType.spawnPosition);
			board.setHexagonColor(currentPiece[i], currentPieceType.color);
		}
		//Next piece handling
		for (Coordinate coordinate : nextPiece){
			nextPieceWindow.setHexagonColor(coordinate, null);
		}
		nextPieceType = pieceTypes[RANDOM.nextInt(pieceTypes.length)];
		Coordinate[] nextPieceRelativeCoordinates = nextPieceType.getRotation(0);
		nextPiece = new Coordinate[nextPieceRelativeCoordinates.length];
		for (int i = 0; i < nextPiece.length; i++){
			nextPiece[i] = Coordinate.add(nextPieceRelativeCoordinates[i], nextPieceType.nextPieceGridPosition);
			nextPieceWindow.setHexagonColor(nextPiece[i], nextPieceType.color);
		}
	}

	private void findCompleteLines(){
		for (int halfRow = HALF_ROWS-1; halfRow >= 0; halfRow--){
			boolean rowIsFull = true;
			for (int column = halfRow%2; column < COLUMNS; column += 2){
				if (board.getHexagonColor(column, halfRow) == null){
					rowIsFull = false;
					break;
				}
			}
			if (rowIsFull){
				completeHalfRows.add(halfRow);
			}
		}
		for (int i = completeHalfRows.size()-1; i >= 0; i--){
			boolean hasAdjacentHalfRow = false;
			if (i < completeHalfRows.size()-1){
				hasAdjacentHalfRow |= completeHalfRows.get(i) == completeHalfRows.get(i+1)+1;
			}
			if (i > 0){
				hasAdjacentHalfRow |= completeHalfRows.get(i) == completeHalfRows.get(i-1)-1;
			}
			if (!hasAdjacentHalfRow){
				completeHalfRows.remove(i);
			}
		}
		if (!completeHalfRows.isEmpty()){
			isInAnimation = true;
			completeHalfRows.forEach(halfRow -> {
				for (int column = halfRow%2; column < COLUMNS; column += 2){
					board.setHexagonColor(column, halfRow, Color.WHITE);
				}
			});
			repaint();
			fallTimer.cancel();
			new Timer().schedule(new TimerTask(){
				@Override
				public void run(){
					clearCompleteLines();
				}
			}, LINE_CLEAR_ANIMATION_TIME);
		} else {
			spawnNewPiece();
		}
	}
	private void clearCompleteLines(){
		for (int i = completeHalfRows.size()-1; i >= 0; i--){
			int halfRow = completeHalfRows.get(i);
			for (int column = halfRow % 2; column < COLUMNS; column += 2){
				removeHexagon(column, halfRow);
			}
		}
		completeHalfRows.clear();
		repaint();
		spawnNewPiece();
		startFallTimer();
		isInAnimation = false;
	}
	private void startFallTimer(){
		fallTimer = new Timer();
		fallTimer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run(){
				tryMovePieceDown();
			}
		}, timePerFall, timePerFall);
	}

	private void removeHexagon(int column, int halfRow){
		for (int y = halfRow; y > halfRow % 2; y -= 2){
			board.setHexagonColor(column, y, board.getHexagonColor(column, y - 2));
		}
		board.setHexagonColor(column, halfRow % 2, null);
	}
}