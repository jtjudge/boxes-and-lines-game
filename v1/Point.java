package jtjudge.Boxes.v1;

import java.util.HashSet;

/**
 * @author jtjudge
 * 
 * A Point object represents a corner of a space on the board--thus, if there are
 * n rows and m columns of Spaces, there are n+1 rows and m+1 columns of Points.
 * Points are a significant part of the algorithms that turn user input into changes
 * to the game state, as well as those that write the game state to the console. A
 * Point contains row and column indices, a set of surrounding Spaces, a set of 
 * Points to which it is joined by drawn lines, and a name, which is what the user
 * inputs into the console to create a move.
 */
public class Point {
	
	
	
	//FIELDS
	
	
	
	/**
	 * Name of this Point, in form 'A1' where A is column index and 1 is row index
	 */
	private final String name;
	
	/**
	 * Position of this Point in the grid of all Points.
	 */
	private final int row, col;
	
	/**
	 * Spaces that surround this Point.
	 */
	private final Space topleft, topright, bottomleft, bottomright;
	
	/**
	 * Set of Points that this Point is joined to through drawn lines.
	 */
	private HashSet<Pair> connections;
	
	
	
	//CONSTRUCTOR
	
	
	
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
		this.connections = new HashSet<>();
		this.name = Character.toString((char)(col + 65)) + Character.toString((char)(row + 49));
	}
	
	
	
	//KEY METHODS
	
	
	
	public void addInactiveConnection(Move m, Point p) {
		connections.add(new Pair(m, p));
	}
	
	public void activateConnection(Point p) {
		for(Pair pair : connections) {
			Point pp = pair.point;
			if(pp.equals(p)) {
				if(pair.isActive) throw new IllegalStateException();
				pair.isActive = true;
			}
		}
	}
	
	/**
	 * Returns true if the given Point is joined to this Point through a drawn line.
	 * @param other
	 *  Point to test
	 * @return
	 *  true if connected
	 */
	public Move getConnectingMove(Point p) {
		for(Pair pair : connections) {
			if(pair.point.equals(p)) return pair.move;
		}
		return null;
	}
	
	
	
	//GETTERS
	
	
	
	public String getName() { return this.name; }
	public int getRow() { return this.row; }
	public int getCol() { return this.col; }
	public Space getTopLeft() { return this.topleft; }
	public Space getTopRight() { return this.topright; }
	public Space getBottomLeft() { return this.bottomleft; }
	public Space getBottomRight() { return this.bottomright; }
	
	
	
	//OVERRIDDEN
	
	
	
	/**
	 * Returns true if a given Point has the same row and column index as this Point.
	 * @param o
	 *  Object to compare, must be a Point
	 * @return
	 *  true if equal
	 */
	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof Point)) return false;
		Point p = (Point) o;
		//there cannot be multiple points with the same coordinates
		return this.row == p.row && this.col == p.col;
	}
	
	@Override
	public int hashCode() {
		int prime = 43;
		int result = 1;
		result = prime * result + this.row;
		result = prime * result + this.col;
		return result;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	
	
	//INNER CLASS
	
	
	
	private class Pair {
		
		protected Move move;
		protected Point point;
		protected boolean isActive;
		
		protected Pair(Move m, Point p) {
			this.move = m;
			this.point = p;
			this.isActive = false;
		}

		@Override
		public boolean equals(Object o) {
			if(o == null || !(o instanceof Pair)) return false;
			Pair p = (Pair) o;
			//there cannot be multiple points with the same coordinates
			return  (this.point == p.point || this.point.equals(p.point)) &&
					(this.move == p.move || this.move.equals(p.move));
		}
		
		@Override
		public int hashCode() {
			int prime = 29;
			int result = 1;
			result = prime * result + this.move.hashCode();
			result = prime * result + this.point.hashCode();
			return result;
		}
		
		@Override
		public String toString() {
			return this.move.getName();
		}
		
	}
	
	
	
}
