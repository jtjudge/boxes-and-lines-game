package jtjudge.Boxes.v1;

/**
 * @author jtjudge
 * 
 * A Move object represent a line that can be drawn in the current game
 * session. All Moves contain the line and other information such as the
 * String coordinates input by the user and the player responsible for 
 * making the move. Moves made by CPU players also include a cost value, 
 * calculated by the analyze() method, that represents the effect this 
 * move would have on its score.
 */
public class Move {

	/**
	 * Game session in which this move is being made.
	 */
	private Game game;
	
	/**
	 * Player who is making this move.
	 */
	private Player player;
	
	/**
	 * Line object that this move will create.
	 */
	private Line line;
	
	/**
	 * The String coordinates given by the user.
	 */
	protected String v1, v2;	//used to print CPU moves to the console
	
	/**
	 * The magnitude of the negative effect this move will have on the
	 * score of the player.
	 */
	protected int cost;
	
	/**
	 * Constructs a Move to be made by a human player. Coordinates and
	 * the player making the move are stored. The coordinates are then 
	 * verified and formatted, then a Line object is stored. If the line
	 * or the coordinates are somehow invalid, the constructor throws an
	 * IllegalArgumentException.
	 * @param v1
	 *  first coordinate String
	 * @param v2
	 *  second coordinate String
	 * @param player
	 *  player making the move
	 */
	public Move(String v1, String v2, Player player) {
		this.v1 = v1; this.v2 = v2;
		this.player = player;
		this.game = this.player.getGame();
		this.line = extract(v1, v2);
		if(this.line == null) throw new IllegalArgumentException();
	}
	
	/**
	 * Constructs a Move to be made by a CPU player. The game session
	 * begins by using this constructor to construct (without players)
	 * a list of all possible moves between the different points on the
	 * board. If chosen by the CPU algorithm, this move has its 'player'
	 * field filled by the CPU player.
	 * @param p1
	 *  initial Point
	 * @param p2
	 *  final Point
	 * @param game
	 *  game session this move is possible in
	 */
	public Move(Point p1, Point p2, Game game) {
		this.v1 = p1.name; this.v2 = p2.name;
		this.game = game;
		this.line = extractLine(p1, p2);
	}
	
	/**
	 * Key helper method for turning user input into Move objects. Takes
	 * two points in form A1 where A is the column index and 1 is the row
	 * index. First turns the points into four ints in formatted order R1, 
	 * C1, R2, C2, counted from 0. Finds two Points p1 and p2 that correspond
	 * to these coordinates. Finds two spaces left and right that correspond
	 * to these points. Finally, constructs and returns the new Line that
	 * is then stored in this Move object. Returns null if the line or
	 * coordinate pairs are somehow invalid.
	 * @param s1
	 *  first coordinate String
	 * @param s2
	 *  second coordinate String
	 * @return
	 *  line between the two coordinates
	 */
	private Line extract(String s1, String s2) {
		//check for proper formatting
		if((s1 == null || s2 == null || s1.length() != 2 || s2.length() != 2
				|| !Character.isLetter(s1.charAt(0)) || !Character.isDigit(s1.charAt(1))
				|| !Character.isLetter(s2.charAt(0)) || !Character.isDigit(s2.charAt(1))	)) {
			return null;
		}
		int c1 = Character.getNumericValue(Character.toUpperCase(s1.charAt(0))) - 10;
		int r1 = Character.getNumericValue(s1.charAt(1)) - 1;
		int c2 = Character.getNumericValue(Character.toUpperCase(s2.charAt(0))) - 10;
		int r2 = Character.getNumericValue(s2.charAt(1) - 1);
		if(r1 != r2 && c1 != c2) return null;
		//check if the coordinates are in range
		if(r1 < 0 || r1 > game.rows || c1 < 0 || c1 > game.cols
				|| r2 < 0 || r2 > game.rows || c2 < 0 || c2 > game.cols) return null;
		//check that the move is between two different points
		if(r1 == r2 && c1 == c2) return null;
		//ensure that coordinates are in ascending order
		int tmp;
		if(r1 > r2) {
			tmp = r1; r1 = r2; r2 = tmp;
		}
		if(c1 > c2) {
			tmp = c1; c1 = c2; c2 = tmp;
		}
		Point[][] points = this.game.getPoints();
		//finds the points with the given coordinates
		Point p1 = points[c1][r1];
		Point p2 = points[c2][r2];
		//returns the line between the two points
		return extractLine(p1, p2);
	}
	
	/**
	 * Helper method for constructing Moves. Finds the spaces left and
	 * right of the Line created by two given points, then uses them to
	 * construct and return the new Line.
	 * @param p1
	 *  first Point
	 * @param p2
	 *  second Point
	 * @return
	 *  Line bewteen the Points
	 */
	private Line extractLine(Point p1, Point p2) {
		//previous checks have ensured that the points are adjacent and in ascending order,
		//so we can simply compare their shared spaces
		Space left = null, right = null;
		if(p1.topright == p2.topleft) {
			left = p1.topright;
			right = p1.bottomright;
		} else if (p1.bottomright == p2.topright) {
			left = p1.bottomleft;
			right = p1.bottomright;
		}
		if(left == null && right == null) return null;
		Line line = new Line(p1, p2, left, right, player);
		//check if the line is already drawn
		if(this.game.hasLine(line)) {
			return null;
		}
		return line;
	}
	
	/**
	 * Sets the player who will make this move. Used by a CPU player.
	 * @param p
	 *  player to set
	 */
	public void setPlayer(Player p) {
		this.player = p;
		this.line.setPlayer(p);
	}
	
	/**
	 * Returns the player who will make this move.
	 * @return
	 *  this move's player
	 */
	public Player getPlayer() {
		return this.player;
	}
	
	/**
	 * Returns the line to be drawn by this move.
	 * @return
	 *  this move's line
	 */
	public Line getLine() {
		return this.line;
	}
	
	/**
	 * Determines the basic cost of making this move. Used by the lower-level CPU 
	 * players to make moves in a game. CPU difficulty level determines the extent
	 * of the evaluation.
	 * @param diff
	 *  difficulty level of the CPU player evaluating this move
	 */
	public void analyze(int diff) {
		cost = 0;
		Space left = this.line.getLeft();
		Space right = this.line.getRight();
		if(diff == 1) {
			if((left != null && left.getRank() == 3) 
					|| (right != null && right.getRank() == 3)) cost--;
		}
		if(diff >= 2) {
			if(left != null && left.getRank() == 3) {
				cost--;
				if(right != null && right.getRank() == 3) cost--;
				if(right != null && right.getRank() == 2) cost-=2;
			} else if(right != null && right.getRank() == 3) {
				cost--;
				if(left != null && left.getRank() == 3) cost--;
				if(left != null && left.getRank() == 2) cost-=2;
			}
			if(left != null && left.getRank() == 2) {
				cost++;
				if(right != null && right.getRank() != 3) cost++;
			} else if(right != null && right.getRank() == 2) {
				cost++;
				if(left != null && left.getRank() != 3) cost++;
			}
		}
	}

	/**
	 * Returns true if the Line contained in this Move is the same as that
	 * in a given Move.
	 * @param o
	 *  Object to be compared, must be a Move
	 * @return
	 *  true if given is equal to this move
	 */
	@Override
	public boolean equals(Object o) {
		Move m = (Move) o;
		return this.line.equals(m.line);
	}
	
}
