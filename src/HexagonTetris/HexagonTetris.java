package HexagonTetris;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class HexagonTetris extends JPanel {
	// Public static constants.
	public static final Color BackgroundColor = new Color(0x202020);
	// Private static constants.
	private static final Random RANDOM = new Random();
	private static final double SQRT3 = Math.sqrt(3);
	private static final int HEXAGON_SIZE = 20;
	private static final int COLUMNS = 10, ROWS = 24, INVISIBLE_ROWS = 4;
	private static final int HALF_ROWS = ROWS*2;
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
	private final Hexagon[][] hexagonGrid = new Hexagon[COLUMNS][ROWS];
	private final List<Integer> completeHalfRows = new ArrayList<>();
	// Changeable state.
	private Timer fallTimer = null;
	private Coordinate[] currentPiece = null;
	private PieceType currentPieceType = null;
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
		{
			int xOffset = HEXAGON_SIZE*3/2;
			double yOffset = HEXAGON_SIZE*SQRT3;
			double yHalfOffset = yOffset/2;
			for (int x = 0; x < hexagonGrid.length; x++){
				int xPosition = x*xOffset;
				double extraYOffset = yHalfOffset*(x%2);
				for (int y = 0; y < hexagonGrid[x].length; y++){
					hexagonGrid[x][y] = new Hexagon(xPosition,  y*yOffset + extraYOffset, HEXAGON_SIZE);
				}
			}
		}
		spawnNewPiece();
		repaint();
		startFallTimer();
	}
	// Overridden methods. |------------------------------------------------------------------------------------------
	@Override
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		((Graphics2D)g).setStroke(new BasicStroke(1));
		g.translate(150, -100);
		for (Hexagon[] hexagons : hexagonGrid){
			for (int i = INVISIBLE_ROWS; i < hexagons.length; i++){
				Hexagon hexagon = hexagons[i];
				hexagon.draw(g);
			}
		}
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
			setHexagonColor(coordinate, null);
			coordinate.add(moveStep);
		}
		boolean moveWasBlocked = false;
		for (Coordinate coordinate : currentPiece){
			Hexagon hexagon = getHexagonAt(coordinate);
			if (hexagon == null || getHexagonColor(coordinate) != null){
				moveWasBlocked = true;
				break;
			}
		}
		if (moveWasBlocked){
			for (Coordinate coordinate : currentPiece){
				coordinate.subtract(moveStep);
				setHexagonColor(coordinate, currentPieceType.color);
			}
		} else {
			for (Coordinate coordinate : currentPiece){
				setHexagonColor(coordinate, currentPieceType.color);
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
			setHexagonColor(coordinate, null);
			coordinate.subtract(currentRotationCoordinates[i]);
			coordinate.add(newRotationCoordinates[i]);
		}
		boolean rotationWasBlocked = false;
		for (Coordinate coordinate : currentPiece){
			Hexagon hexagon = getHexagonAt(coordinate);
			if (hexagon == null || getHexagonColor(coordinate) != null){
				rotationWasBlocked = true;
				break;
			}
		}
		if (rotationWasBlocked){
			for (int i = 0; i < currentPiece.length; i++){
				Coordinate coordinate = currentPiece[i];
				coordinate.subtract(newRotationCoordinates[i]);
				coordinate.add(currentRotationCoordinates[i]);
				setHexagonColor(coordinate, currentPieceType.color);
			}
			return;
		}
		for (Coordinate coordinate : currentPiece){
			setHexagonColor(coordinate, currentPieceType.color);
		}
		currentRotation = newRotation;
		repaint();
	}

	private void spawnNewPiece(){
		currentPieceType = pieceTypes[RANDOM.nextInt(pieceTypes.length)];
		currentRotation = 0;
		Coordinate[] pieceRelativeCoordinates = currentPieceType.getRotation(currentRotation);
		currentPiece = new Coordinate[pieceRelativeCoordinates.length];
		for (int i = 0; i < currentPiece.length; i++){
			currentPiece[i] = Coordinate.add(pieceRelativeCoordinates[i], currentPieceType.spawnPosition);
			setHexagonColor(currentPiece[i], currentPieceType.color);
		}
	}

	// TODO: Clear lines that don't match the square double array grid.
	private void findCompleteLines(){
		for (int halfRow = HALF_ROWS-1; halfRow >= 0; halfRow--){
			boolean rowIsFull = true;
			for (int column = halfRow%2; column < COLUMNS; column += 2){
				if (getHexagonColor(column, halfRow) == null){
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
					setHexagonColor(column, halfRow, Color.WHITE);
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
			setHexagonColor(column, y, getHexagonColor(column, y - 2));
		}
		setHexagonColor(column, halfRow % 2, null);
	}

	// Getters and setters. |------------------------------------------------------------------------------------------
	private void setHexagonColor(Coordinate coordinate, Color color){
		setHexagonColor(coordinate.x , coordinate.y, color);
	}
	private void setHexagonColor(int x, int y, Color color){
		getHexagonAt(x, y).color = color;
	}
	private Color getHexagonColor(Coordinate coordinate){
		return getHexagonColor(coordinate.x , coordinate.y);
	}
	private Color getHexagonColor(int x, int y){
		return getHexagonAt(x, y).color;
	}
	private Hexagon getHexagonAt(Coordinate coordinate){
		return getHexagonAt(coordinate.x, coordinate.y);
	}
	private Hexagon getHexagonAt(int x, int y){
		int doubleY = (y-x%2);
		if (doubleY % 2 != 0){
			System.out.printf("Column %d does not have hexagon at height %d!%n", x, y);
			return null;
		}
		try {
			return hexagonGrid[x][doubleY/2];
		} catch (ArrayIndexOutOfBoundsException e){
			return null;
		}
	}
}