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
	private static final int LEFT = -1, RIGHT = +1, UP = -1, DOWN = +1;
	private static final int BOARD_X = 150, BOARD_Y = 40;
	private static final int HEXAGON_SIZE = 20;
	private static final int COLUMNS = 10, ROWS = 21, INVISIBLE_ROWS = 1, HALF_ROWS = ROWS*2;
	private static final int NEXT_WINDOW_COLUMNS = 3, NEXT_ROWS = 4, NEXT_COLUMN_OFFSET = 18, NEXT_ROW_OFFSET = 6;
	private static final Coordinate oneStepDown = new Coordinate(0, 2);
	private static final int LINE_CLEAR_ANIMATION_TIME = 1000;
	// Final instance fields (collections).
	private final HexagonGrid board = new HexagonGrid(COLUMNS, ROWS, HEXAGON_SIZE, 0, -INVISIBLE_ROWS);
	private final HexagonGrid nextWindow = new HexagonGrid(NEXT_WINDOW_COLUMNS, NEXT_ROWS, HEXAGON_SIZE, NEXT_COLUMN_OFFSET, NEXT_ROW_OFFSET-INVISIBLE_ROWS);
	private final List<Integer> completeHalfRows = new ArrayList<>();
	// Changeable state.
	private Timer fallTimer = null;
	private Piece currentPiece;
	private Piece nextPiece;
	private boolean pieceIsAtLowest = false;
	private boolean isInAnimation = false;
	private int timePerFall = 800;
	// Constructor. |------------------------------------------------------------------------------------------
	public HexagonTetris(){
		setFocusable(true);
		setBackground(BackgroundColor);
		addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent event){
				handleInput(event);
			}
		});
		nextPiece = new Piece(nextWindow, false);
		spawnNextPiece();
		startFallTimer();
	}
	// Overridden methods. |------------------------------------------------------------------------------------------
	@Override
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke(new BasicStroke(1));
		g2d.translate(BOARD_X, BOARD_Y);
		board.draw(g2d, INVISIBLE_ROWS, false);
		nextWindow.draw(g2d, 0, true);
	}
	// Input methods. |------------------------------------------------------------------------------------------
	private void handleInput(KeyEvent event){
		if (isInAnimation){
			return;
		}
		switch(event.getKeyCode()){
			case KeyEvent.VK_LEFT, KeyEvent.VK_A -> movePieceToSide(LEFT);
			case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> movePieceToSide(RIGHT);
			case KeyEvent.VK_Z -> rotatePiece(LEFT);
			case KeyEvent.VK_UP, KeyEvent.VK_W, KeyEvent.VK_X -> rotatePiece(RIGHT);
			case KeyEvent.VK_DOWN, KeyEvent.VK_S -> movePieceDownManually();
			case KeyEvent.VK_SPACE -> hardDrop();
		}
	}
	// Movement methods. |------------------------------------------------------------------------------------------
	private void movePieceToSide(int side){
		Coordinate moveStep = new Coordinate(side, pieceIsAtLowest ? UP : DOWN);
		boolean moveWasBlocked = tryMovePiece(moveStep);
		if (moveWasBlocked){
			// If the move was slightly upwards and was blocked, try again but slightly downwards.
			if (moveStep.y == UP){
				moveStep.y = DOWN;
				tryMovePiece(moveStep);
			}
			return;
		}
		// Toggle this boolean to make the piece alternate between moving to the side and slightly up and moving to the side and slightly down.
		pieceIsAtLowest = !pieceIsAtLowest;
	}
	private void movePieceDownManually(){
		// Stop and start the fall timer to ensure it takes the full period before then next move down.
		fallTimer.cancel();
		startFallTimer();
		tryMovePieceDown();
	}
	private void hardDrop(){
		// Stop and start the fall timer to ensure it takes the full period before then next move down.
		fallTimer.cancel();
		startFallTimer();
		// The while loop is empty because tryMovePieceDown has side effects.
		// noinspection StatementWithEmptyBody
		while (!tryMovePieceDown());
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
		boolean moveWasBlocked = currentPiece.tryMove(moveStep);
		if (!moveWasBlocked){
			repaint();
		}
		return moveWasBlocked;
	}

	private void rotatePiece(int rotationChange){
		currentPiece.rotate(rotationChange);
		repaint();
	}
	// Other methods. |------------------------------------------------------------------------------------------
	private void spawnNextPiece(){
		nextPiece.setGrid(board, true);
		currentPiece = nextPiece;
		nextPiece = new Piece(nextWindow, false);
		repaint();
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

	private void findCompleteLines(){
		findFullHalfRows();
		removeIsolatedHalfRows();
		if (completeHalfRows.isEmpty()){
			spawnNextPiece();
			return;
		}
		startLineClearAnimation();
	}

	private void startLineClearAnimation(){
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
	}

	private void findFullHalfRows(){
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
	}

	private void removeIsolatedHalfRows(){
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
	}

	private void clearCompleteLines(){
		for (int i = completeHalfRows.size()-1; i >= 0; i--){
			int halfRow = completeHalfRows.get(i);
			for (int column = halfRow % 2; column < COLUMNS; column += 2){
				board.removeHexagon(column, halfRow);
			}
		}
		completeHalfRows.clear();
		spawnNextPiece();
		startFallTimer();
		isInAnimation = false;
	}
}