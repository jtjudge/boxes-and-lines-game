package jtjudge.Boxes.v1;

import java.util.ArrayList;
import java.util.HashSet;

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
	
	
	
	//FIELDS
	
	
	
	/**
	 * Name of this move given by the coordinates of its two points.
	 */
	private String name;	//used to print CPU moves to the console

	/**
	 * Player who is making this move.
	 */
	private Player player;
	
	/**
	 * Game session in which this move is being made.
	 */
	private Game game;
	
	private boolean isAvailable;
	
	/**
	 * Spaces this move affects
	 */
	private Space left, right;
	
	/**
	 * Points this move is drawn between
	 */
	private Point a, b;
	
	private int cost;
	
	private ArrayList<Chain> chains;
	
	public static HashSet<Move> movesMade = new HashSet<>();
	
	public static HashSet<Move> movesLeft = new HashSet<>();
	
	public static HashSet<Move> nonChains = new HashSet<>();
	
	public static HashSet<Move> mergers = new HashSet<>();
	
	
	
	//CONSTRUCTORS / FACTORIES
	
	
	
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
	private Move() {
		//Suppress default constructor
	}
	
	public static void constructMove(Point p1, Point p2, Game game) {
		if(p1 == null || p2 == null || game == null) throw new IllegalArgumentException();
		Move m = new Move();
		m.name = p1.getName() + ", " + p2.getName();
		m.game = game;
		m.a = p1; m.b = p2;
		m.a.addInactiveConnection(m, m.b);
		m.b.addInactiveConnection(m, m.a);
		m.fillSpaces(m.a, m.b);
		m.chains = new ArrayList<>();
		m.isAvailable = true;
		//this constructor is only used in game setup--should never be invalid
		if(!movesLeft.add(m)) throw new IllegalStateException();
		nonChains.add(m);
	}
	
	
	
	//KEY METHODS
	
	
	
	//Parses the user input and attempts to find the matching move. If the input is invalid,
	//throws an exception. Otherwise, returns the move.
	public static Move retrievePlayerMove(String s1, String s2, Player p) {
		if((s1 == null || s2 == null || p == null || s1.length() != 2 || s2.length() != 2 || 
			!Character.isLetter(s1.charAt(0)) || !Character.isDigit(s1.charAt(1)) ||
			!Character.isLetter(s2.charAt(0)) || !Character.isDigit(s2.charAt(1))		)) {
			return null;
		}
		int c1 = Character.getNumericValue(Character.toUpperCase(s1.charAt(0))) - 10;
		int r1 = Character.getNumericValue(s1.charAt(1)) - 1;
		int c2 = Character.getNumericValue(Character.toUpperCase(s2.charAt(0))) - 10;
		int r2 = Character.getNumericValue(s2.charAt(1) - 1);
		if(r1 != r2 && c1 != c2) return null;
		//check if the coordinates are in range
		Game game = p.getGame();
		if(r1 < 0 || r1 > game.getRows() || c1 < 0 || c1 > game.getCols()
				|| r2 < 0 || r2 > game.getRows() || c2 < 0 || c2 > game.getCols()) return null;
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
		Point[][] points = game.getPoints();
		//finds the points with the given coordinates
		Point p1 = points[c1][r1];
		Point p2 = points[c2][r2];
		//returns the existing move between the two points
		for(Move m : movesLeft) {
			if(m.getA().equals(p1) && m.getB().equals(p2)) {
				m.player = p;
				return m;
			}
		}
		return null;
	}
	
	/**
	 * "Draws" this move on the board. Connects its two points, ranks up the adjacent spaces, and
	 * returns the number of spaces filled by this move.
	 * @return
	 *  number of spaces filled
	 */
	public int execute() {
		//CPU should have claimed this move by now
		if(this.player == null) throw new IllegalStateException();
		int num = 0;
		//move is validated
		this.a.activateConnection(this.b);
		this.b.activateConnection(this.a);
		if(this.left != null && this.left.makeMove(this)) num++;
		if(this.right != null && this.right.makeMove(this)) num++;
		movesLeft.remove(this);
		nonChains.remove(this);
		if(nonChains.isEmpty() && !this.game.isEndGame()) {
			this.game.beginEndGame();
		}
		movesMade.add(this);
		this.isAvailable = false;
		int i = 0;
		while(!chains.isEmpty()) {
			Chain c = chains.get(i);
			c.makeMove(this);
		}
		return num;
	}
	
	public void addChain(Chain c) {
		//it should be impossible for one move to be part of more than two chains at a given
		//moment in the game
		if(this.chains.size() == 2) throw new IllegalStateException();
		this.chains.add(c);
		nonChains.remove(this);
		if(nonChains.isEmpty() && !this.game.isEndGame()) this.game.beginEndGame();
	}
	
	public void removeChain(Chain c) {
		this.chains.remove(c);
	}
	
	/**
	 * Sets the player who will receive points when <code>execute()</code> is called. Used by a CPU player.
	 * @param p
	 *  player to set
	 */
	public void setPlayer(Player p) {
		if(p == null) throw new IllegalArgumentException();
		//a move's player should not be modified after it is set
		if(this.player != null) throw new IllegalStateException();
		this.player = p;
	}

	/**
	 * Determines the basic cost of making this move. Used by the lower-level CPU 
	 * players to make moves in a game. CPU difficulty level determines the extent
	 * of the evaluation.
	 * @param diff
	 *  difficulty level of the CPU player evaluating this move
	 */
	public void calculateBasicCost(int diff) {
		int cost = 0;
		Space left = this.left;
		Space right = this.right;
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
		this.cost = cost;
	}

	//TODO
	public static void updateChainBuilders() {
		for(Space s : Space.sharedEndSpaces) {
			if(s.getRank() == 1) return;
		}
	}
	
	//GETTERS
	
	
	
	public Player getPlayer() { return this.player; }
	
	public String getName() { return this.name; }
	
	public Point getA() { return this.a; }
	
	public Point getB() { return this.b; }
	
	public Space getLeft() { return this.left; }
	
	public Space getRight() { return this.right; }
	
	public int getCost() { return this.cost; }
	
	public boolean isAvailable() { return this.isAvailable; }
	
	public boolean hasTwoChains() { return this.chains.size() > 1; }
	
	public ArrayList<Chain> getChains() { return this.chains; }

	public boolean hasRankThree() { return (this.left != null && this.left.getRank() == 3) ||
											(this.right != null && this.right.getRank() == 3); }
	
	
	
	//OVERRIDDEN
	
	
	
	/**
	 * Returns true  
	 * @param o
	 *  Object to be compared, must be a Move
	 * @return
	 *  true if given is equal to this move
	 */
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof Move)) return false;
		Move m = (Move) o;
		return this.name.equals(m.name);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int row1 = a.getRow(); int col1 = a.getCol();
		int row2 = b.getRow(); int col2 = b.getCol();
		int result = 1;
		result = prime * result + row1 + col1;
		result = prime * result + row2 + col2;
		return result;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	
	
	//HELPERS
	
	
	
	/**
	 * Helper method for constructing moves for both human and CPU players. If parameters are 
	 * valid points, fills this move's 'left' and 'right' fields with the spaces shared by the
	 * two points. Then, returns true if this move has not yet been made in the game.
	 * @param p1
	 *  first Point
	 * @param p2
	 *  second Point
	 * @return
	 *  true if valid
	 */
	private void fillSpaces(Point p1, Point p2) {
		//the points are adjacent and in ascending order,
		//so we can simply compare their shared spaces
		Space left = null, right = null;
		if(p1.getTopRight() == p2.getTopLeft()) {
			left = p1.getTopRight();
			right = p1.getBottomRight();
		} else if (p1.getBottomRight() == p2.getTopRight()) {
			left = p1.getBottomLeft();
			right = p1.getBottomRight();
		}
		//there is no case in which this move should be invalid
		if(left == null && right == null) throw new IllegalStateException();
		if(left != null) left.addMove(this);
		if(right != null) right.addMove(this);
		this.left = left;
		this.right = right;
	}
	
	
	
}
