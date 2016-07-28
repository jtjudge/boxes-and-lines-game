package jtjudge.Boxes.v1;

import java.util.ArrayList;

/**
 * @author jtjudge
 * 
 * A Point object represents a corner of a space on the board--thus, if there are
 * n rows and m columns of Spaces, there are n+1 rows and m+1 columns of Points.
 * Points are a significant part of the algorithms that turn user input into changes
 * to the game state, as well as those that write the game state to the cosole. A
 * Point contains row and column indices, a set of surrounding Spaces, a set of 
 * Points to which it is joined by drawn lines, and a name, which is what the user
 * inputs into the console to create a move.
 */
public class Point {
	
	/**
	 * Name of this Point, in form 'A1' where A is column index and 1 is row index
	 */
	protected String name;
	
	/**
	 * Position of this Point in the grid of all Points.
	 */
	protected int row, col;
	
	/**
	 * Spaces that surround this Point.
	 */
	protected Space topleft, topright, bottomleft, bottomright;
	
	/**
	 * Set of Points that this Point is joined to through drawn lines.
	 */
	protected ArrayList<Point> points;

	/**
	 * Constructs a new Point at the given row and column of the grid of all Points
	 * and with the set of four surrounding spaces.
	 * @param row
	 *  row index in the Point grid
	 * @param col
	 *  column index in the Point grid
	 * @param spaces
	 *  the set of four spaces surrounding this Point
	 */
	public Point(int row, int col, Space[] spaces) {
		if(spaces.length != 4) throw new IllegalArgumentException();
		this.row = row;
		this.col = col;
		this.topleft = spaces[0];
		this.topright = spaces[1];
		this.bottomleft = spaces[2];
		this.bottomright = spaces[3];
		this.points = new ArrayList<>();
		this.name = Character.toString((char)(col + 65)) + Character.toString((char)(row + 49));
	}
	
	/**
	 * Returns true if the given Point is joined to this Point through a drawn line.
	 * @param other
	 *  Point to test
	 * @return
	 *  true if connected
	 */
	public boolean isConnectedTo(Point other) {
		return points.contains(other);
	}
	
	/**
	 * Returns true if a given Point has the same row and column index as this Point.
	 * @param o
	 *  Object to compare, must be a Point
	 * @return
	 *  true if equal
	 */
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		Point other = (Point) o;
		return other.row == this.row && other.col == this.col;
	}
}
