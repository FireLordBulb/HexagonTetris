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
	// Visual static constants.
	public static final Color BACKGROUND_COLOR = new Color(0x202020);
	public static final Color PAUSE_SHADOW_COLOR = new Color(0, 0, 0, 60);
	public static final Color GAME_OVER_SHADOW_COLOR = new Color(0, 0, 0, 160);
	private static final int HEXAGON_SIZE = 20;
	private static final int TEXT_LINE_HEIGHT = 15;
	private static final int BOARD_X = 150, BOARD_Y = 40;
	private static final int INFO_PANEL_X = 340, INFO_PANEL_Y = 300;
	private static final int BOARD_CENTER_TEXT_X = HEXAGON_SIZE*27/4, BOARD_CENTER_TEXT_Y = 305;
	private static final int INVISIBLE_ROWS = 1, NEXT_WINDOW_COLUMN_OFFSET = 18, NEXT_WINDOW_ROW_OFFSET = 3;
	// Gameplay static constants.
	private static final int COLUMNS = 10, ROWS = 20+INVISIBLE_ROWS, HALF_ROWS = ROWS*2;
	private static final int NEXT_WINDOW_COLUMNS = 3, NEXT_WINDOW_ROWS = 4;
	private static final int LEFT = -1, RIGHT = +1, UP = -1, DOWN = +1;
	private static final Coordinate ONE_STEP_DOWN = new Coordinate(0, 2);

	private static final int LINE_CLEAR_ANIMATION_TIME = 1000;
	private static final int UNPAUSE_DELAY = 1000;
	// Final instance fields (collections).
	private final HexagonGrid board = new HexagonGrid(COLUMNS, ROWS, HEXAGON_SIZE, 0, -INVISIBLE_ROWS);
	private final HexagonGrid nextWindow = new HexagonGrid(NEXT_WINDOW_COLUMNS, NEXT_WINDOW_ROWS, HEXAGON_SIZE, NEXT_WINDOW_COLUMN_OFFSET, NEXT_WINDOW_ROW_OFFSET);
	private final List<Integer> completeHalfRows = new ArrayList<>();
	// Changeable state.
	private Timer fallTimer = null;
	private int timePerFall = 800;
	private long previousFallTime = 0;
	private int fallDelayLeft = 0;
	private long lastPauseTime = 0;

	private int highScore = 0;
	private int score;
	private int pushDownPoints;
	private int level;
	private int clearedHalfRows;

	private Piece currentPiece;
	private Piece nextPiece;
	private boolean pieceIsAtLowest = false;
	private boolean isInAnimation = false;
	private boolean gameIsOver = false;
	private boolean isPaused = false;
	// Constructor. |------------------------------------------------------------------------------------------
	public HexagonTetris(){
		setFocusable(true);
		setBackground(BACKGROUND_COLOR);
		addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent event){
				handleInput(event);
			}
		});
		restartGame();
	}
	// Drawing methods. |------------------------------------------------------------------------------------------
	@Override
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke(new BasicStroke(1));
		g2d.translate(BOARD_X, BOARD_Y);
		board.draw(g2d, INVISIBLE_ROWS, false, isPaused);
		nextWindow.draw(g2d, 0, true, isPaused);
		g2d.translate(INFO_PANEL_X, INFO_PANEL_Y);
		g2d.setColor(Color.WHITE);
		drawLeftAlignedText(g2d, "High Score: "+highScore, "Score: "+score, "Level: "+level, "Lines: "+(clearedHalfRows/2.0));
		if (isPaused){
			drawFullScreenRectWithText(g2d, PAUSE_SHADOW_COLOR, "Paused");
		} else if (gameIsOver){
			drawFullScreenRectWithText(g2d, GAME_OVER_SHADOW_COLOR, "Game Over!", "Press R to restart");
		}
	}

	private void drawLeftAlignedText(Graphics2D g, String... textLines){
		for (int i = 0; i < textLines.length; i++){
			g.drawString(textLines[i], 0, i*TEXT_LINE_HEIGHT);
		}
	}
	
	private void drawFullScreenRectWithText(Graphics2D g, Color color, String... textLines){
		g.setTransform(new AffineTransform());
		g.setColor(color);
		g.fillRect(0, 0, getParent().getWidth(), getParent().getHeight());
		g.translate(BOARD_X, BOARD_Y);
		g.translate(BOARD_CENTER_TEXT_X, BOARD_CENTER_TEXT_Y);
		g.setColor(Color.WHITE);
		FontMetrics fontMetrics = g.getFontMetrics(g.getFont());
		for (int i = 0; i < textLines.length; i++){
			int textWidth = fontMetrics.stringWidth(textLines[i]);
			g.drawString(textLines[i], -textWidth/2, i*TEXT_LINE_HEIGHT);
		}
	}
	// Input methods. |------------------------------------------------------------------------------------------
	private void handleInput(KeyEvent event){
		if (isInAnimation){
			return;
		}
		if (gameIsOver){
			if (event.getKeyCode() == KeyEvent.VK_R){
				restartGame();
			}
			return;
		}
		if (event.getKeyCode() == KeyEvent.VK_P){
			togglePause();
		}
		if (isPaused){
			return;
		}
		// TODO: Add pausing.
		// Gameplay inputs
		switch(event.getKeyCode()){
			case KeyEvent.VK_LEFT, KeyEvent.VK_A -> movePieceToSide(LEFT);
			case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> movePieceToSide(RIGHT);
			case KeyEvent.VK_Z -> rotatePiece(LEFT);
			case KeyEvent.VK_UP, KeyEvent.VK_W, KeyEvent.VK_X -> rotatePiece(RIGHT);
			case KeyEvent.VK_DOWN, KeyEvent.VK_S -> movePieceDownManually();
			case KeyEvent.VK_SPACE -> hardDrop();
		}
	}

	private void restartGame(){
		board.clear();
		nextWindow.clear();
		gameIsOver = false;
		score = 0;
		level = 0;
		clearedHalfRows = 0;
		timePerFall = 800;
		nextPiece = new Piece(nextWindow, false);
		spawnNextPiece();
		startFallTimer();
	}

	private void togglePause(){
		if (isPaused && System.currentTimeMillis()-lastPauseTime < UNPAUSE_DELAY){
			return;
		}
		isPaused = !isPaused;
		repaint();
		if (isPaused){
			lastPauseTime = System.currentTimeMillis();
			fallTimer.cancel();
			fallDelayLeft = timePerFall-(int)(System.currentTimeMillis()-previousFallTime);
		} else {
			startFallTimer(fallDelayLeft);
		}
	}

	private void movePieceToSide(int side){
		pushDownPoints = 0;
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
		tryMovePieceDown(true);
	}

	private void hardDrop(){
		// Stop and start the fall timer to ensure it takes the full period before then next move down.
		fallTimer.cancel();
		startFallTimer();
		// The while loop is empty because tryMovePieceDown has side effects.
		// noinspection StatementWithEmptyBody
		while (!tryMovePieceDown(true));
	}
	// Movement methods. |------------------------------------------------------------------------------------------
	private boolean tryMovePieceDown(boolean isManual){
		boolean fallWasBlocked = tryMovePiece(ONE_STEP_DOWN);
		if (isManual && !fallWasBlocked){
			pushDownPoints++;
		}
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
		boolean spawnWasSuccessful = nextPiece.tryChangeGrid(board, true);
		gameIsOver = !spawnWasSuccessful;
		currentPiece = nextPiece;
		nextPiece = new Piece(nextWindow, false);
		pushDownPoints = 0;
		repaint();
	}

	private void startFallTimer(){
		startFallTimer(timePerFall);
	}

	private void startFallTimer(int delay){
		previousFallTime = System.currentTimeMillis()+delay-timePerFall;
		fallTimer = new Timer();
		fallTimer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run(){
				previousFallTime = System.currentTimeMillis();
				tryMovePieceDown(false);
			}
		}, delay, timePerFall);
	}

	private void findCompleteLines(){
		score += pushDownPoints;
		findFullHalfRows();
		removeIsolatedHalfRows();
		if (completeHalfRows.isEmpty()){
			spawnNextPiece();
			if (gameIsOver){
				fallTimer.cancel();
			}
			return;
		}
		startLineClearAnimation();
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

	private void clearCompleteLines(){
		for (int i = completeHalfRows.size()-1; i >= 0; i--){
			int halfRow = completeHalfRows.get(i);
			for (int column = halfRow % 2; column < COLUMNS; column += 2){
				board.removeHexagon(column, halfRow);
			}
		}
		int newHalfRows = completeHalfRows.size();
		clearedHalfRows += newHalfRows;
		score += newHalfRows*100*(level+1);
		if (highScore < score){
			highScore = score;
		}
		level = clearedHalfRows/20;

		completeHalfRows.clear();
		spawnNextPiece();
		if (!gameIsOver){
			startFallTimer();
		}
		isInAnimation = false;
	}
}