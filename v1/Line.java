package jtjudge.Boxes.v1;

/**
 * @author jtjudge
 *
 * A Line object represents both a connection between two points on the
 * board and a border to two spaces. A Line contains its two endpoints 
 * as well as the spaces to its left and right. One of the Spaces can be 
 * null--this indicates a Line is at the edge of the board. When a Line 
 * is drawn in the game, it increments the rank of its spaces.
 */
public class Line {

	/**
	 * Points that form this line.
	 */
	protected Point a, b;
	
	/**
	 * Spaces adjacent to this line.
	 */
	private Space left, right;
	
	/**
	 * Player who drew or will draw this line
	 */
	private Player player;
	
	/**
	 * Constructs a new Line between the given Points and adjacent to the 
	 * given Spaces, with the given Player as its creator. As all Lines are
	 * constructed by Moves, the parameters are assumed to be valid for their
	 * game session.
	 * @param a
	 *  first Point
	 * @param b
	 *  second Point
	 * @param left
	 *  left Space
	 * @param right
	 *  right Space
	 * @param player
	 *  associated Player
	 *  
	 */
	public Line(Point a, Point b, Space left, Space right, Player player) {
		this.a = a; this.b = b;
		this.left = left; this.right = right;
		this.player = player;
	}

	/**
	 * Returns the Space to the left of this Line. Null if the line is at the
	 * leftmost or topmost edge of the board.
	 * @return
	 *  left space
	 */
	public Space getLeft() {
		return left;
	}
	
	/**
	 * Returns the Space to the right of this Line. Null if the line is at the
	 * rightmost or topmost edge of the board.
	 * @return
	 *  right space
	 */
	public Space getRight() {
		return right;
	}
	
	/**
	 * Associates this Line with the given Player. Ensures the scoring system works.
	 * @param p
	 *  player to associate with this line
	 */
	public void setPlayer(Player p) {
		this.player = p;
	}
	
	/**
	 * Applies this Line to the game board, incrementing the ranks of its adjacent
	 * spaces. Returns the number of spaces filled by this action.
	 * @return
	 *  number of spaces filled by drawing this line
	 */
	public int draw() {
		int num = 0;
		this.a.points.add(this.b);
		this.b.points.add(this.a);
		if(left != null) {
			if(left.rankUp(player)) num++;
		}
		if(right != null) {
			if(right.rankUp(player)) num++;
		}
		return num;
	}
	
	/**
	 * Returns true if the points in this Line are the same two in a given line.
	 * @param o
	 *  Object to be compared, must be a Line
	 * @return
	 *  true if given Line is equal
	 */
	@Override
	public boolean equals(Object o) {
		Line other = (Line) o;
		return (this.a == other.a && this.b == other.b)
				|| (this.a == other.b && this.b == other.a);
	}

}
