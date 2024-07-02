package HexagonTetris;

import java.awt.*;
import java.awt.geom.AffineTransform;

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
				grid[x][y] = new Hexagon(xPosition,  y*yOffset + extraYOffset, hexagonSize);
			}
		}
	}
	public void draw(Graphics g, int skipRows, boolean doSkipFirstHalfRow){
		g.translate(xDrawOffset, yDrawOffset);
		for (int i = 0; i < grid.length; i++){
			Hexagon[] column = grid[i];
			for (int j = skipRows; j < column.length; j++){
				if (doSkipFirstHalfRow && j == skipRows && i%2 == 0){
					continue;
				}
				Hexagon hexagon = column[j];
				hexagon.draw(g);
			}
		}
		g.translate(-xDrawOffset, -yDrawOffset);
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
