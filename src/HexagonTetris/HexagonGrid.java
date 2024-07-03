package HexagonTetris;

import java.awt.*;
import java.util.Arrays;

public class HexagonGrid {
	private static final double SQRT3 = Math.sqrt(3);
	private final Hexagon[][] grid;
	private final int xDrawOffset, yDrawOffset;
	public HexagonGrid(int columns, int rows, int hexagonSize, int columnOffset, int rowOffset){
		grid = new Hexagon[columns][rows];
		xDrawOffset = columnOffset*hexagonSize;
		yDrawOffset = (int)(rowOffset*hexagonSize*SQRT3);
		int xOffset = hexagonSize*3/2;
		double yOffset = hexagonSize*SQRT3;
		double yHalfOffset = yOffset/2;
		for (int x = 0; x < grid.length; x++){
			int xPosition = x*xOffset;
			double extraYOffset = yHalfOffset*(x%2);
			for (int y = 0; y < grid[x].length; y++){
				grid[x][y] = new Hexagon(xPosition, y*yOffset + extraYOffset, hexagonSize);
			}
		}
	}
	public void draw(Graphics g, int skipRows, boolean doSkipFirstHalfRow, boolean doHideHexagons){
		g.translate(xDrawOffset, yDrawOffset);
		for (int i = 0; i < grid.length; i++){
			int startAtRow = skipRows;
			if (doSkipFirstHalfRow && i%2 == 0){
				startAtRow++;
			}
			Arrays.stream(grid[i], startAtRow, grid[i].length).forEach(hexagon -> hexagon.draw(g, doHideHexagons));
		}
		g.translate(-xDrawOffset, -yDrawOffset);
	}
	public void clear(){
		Arrays.stream(grid).forEach(hexagons -> Arrays.stream(hexagons).forEach(hexagon -> hexagon.color = null));
	}
	public void removeHexagon(int column, int halfRow){
		for (int y = halfRow; y > halfRow % 2; y -= 2){
			setHexagonColor(column, y, getHexagonColor(column, y - 2));
		}
		setHexagonColor(column, halfRow % 2, null);
	}
	public void setHexagonColor(Coordinate coordinate, Color color){
		setHexagonColor(coordinate.x , coordinate.y, color);
	}
	public void setHexagonColor(int x, int y, Color color){
		getHexagonAt(x, y).color = color;
	}
	public Color getHexagonColor(Coordinate coordinate){
		return getHexagonColor(coordinate.x , coordinate.y);
	}
	public Color getHexagonColor(int x, int y){
		return getHexagonAt(x, y).color;
	}
	public Hexagon getHexagonAt(Coordinate coordinate){
		return getHexagonAt(coordinate.x, coordinate.y);
	}
	public Hexagon getHexagonAt(int x, int y){
		int doubleY = (y-x%2);
		if (doubleY % 2 != 0){
			System.out.printf("Column %d does not have hexagon at height %d!%n", x, y);
			return null;
		}
		try {
			return grid[x][doubleY/2];
		} catch (ArrayIndexOutOfBoundsException e){
			return null;
		}
	}
}
