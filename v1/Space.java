package jtjudge.Boxes.v1;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author jtjudge
 * 
 * A Space object represents a space on the game board. To win the game, players
 * fill these spaces by surrounding them with lines. A Space contains a rank that
 * represents its progress toward being filled, its status as filled or unfilled,
 * and a reference to the player who filled them, if any.
 */
public class Space {

	
	
	//FIELDS
	
	
	
	/**
	 * Name of this space in form "x, y" where x is row number and y is column number.
	 */
	private String name;	//for debug
	
	/**
	 * The number of lines surrounding this space. Represents the space's progress towards
	 * being filled. Space if full if rank is equal to 4.
	 */
	private int rank;
	
	/**
	 * Whether or not the space is filled yet.
	 */
	private boolean full;
	
	/**
	 * The player associated with this space--the one to draw its last line and fill it.
	 */
	private Player player;
	
	private ArrayList<Move> availableMoves;
	
	public static HashSet<Space> endSpaces = new HashSet<>();
	
	public static HashSet<Space> sharedEndSpaces = new HashSet<>();
	
	
	
	//CONSTRUCTOR
	
	
	
	/**
	 * Constructs a new Space on the game board. The position of the space is actually
	 * managed by the Game object, but the name of the Space is created given the two
	 * coordinates.
	 * @param i
	 *  row number of the space
	 * @param j
	 *  column number of the space
	 */
	public Space(int i, int j) {
		this.name = Character.toString((char)(i + 49)) + ", " + Character.toString((char)(j + 49));
		rank = 0;
		full = false;
		availableMoves = new ArrayList<>();
	}
	
	
	
	//KEY METHODS
	
	
	
	public boolean addMove(Move m) {
		if(m == null || availableMoves.size() == 4) return false;
		availableMoves.add(m);
		return true;
	}
	
	/**
	 * Called when an adjacent line is drawn on the game board. Returns true if the space was
	 * just elevated to rank 4 and filled, and associates the given player with this Space. 
	 * Returns false if the space is already full or if the rank was not elevated to 4.
	 * @param player
	 *  player to associate if this call fills the space
	 * @return
	 *  true if filled by this call
	 */
	public boolean makeMove(Move m) {
		if(full) return false;
		//move is verified to be a member of availableMoves
		this.availableMoves.remove(m);
		if(++rank == 2) {
			//there are only two moves available if rank == 2
			Move m1 = this.availableMoves.get(0);
			Move m2 = this.availableMoves.get(1);
			Point m1a = m1.getA(); Point m1b = m1.getB();
			Point m2a = m2.getA(); Point m2b = m2.getB();
			//order the moves to the default chain order
			if(m1a.equals(m2a) || m1b.equals(m2b)) {
				Move temp = m1;
				m1 = m2;
				m2 = temp;
			}
			Chain.constructChain(m1, m2);
		}
		Player p = m.getPlayer();
		if(rank == 4) {
			full = true;
			this.player = p;
			this.player.scoreUp();
			return true;
		}
		return false;
	}
	
	public static void updateEndSpaces() {
		endSpaces.clear();
		sharedEndSpaces.clear();
		ArrayList<Space> ends;
		for(Chain c : Chain.activeChains) {
			ends = c.getEnds();
			for(Space s : ends) {
				if(!endSpaces.add(s)) {
					sharedEndSpaces.add(s);
				}
			}
		}
		Move.updateChainBuilders();
//		Move.updateChainBreakers();
	}
	
	//GETTERS
	
	
	
	public String getName() { return this.name; }
	
	public int getRank() { return this.rank; }
	
	public boolean isFull() { return this.full; }
	
	public Player getPlayer() { return this.player; }
	
	public ArrayList<Move> getAvailableMoves() { return this.availableMoves; }
	
	
	
	//OVERRIDDEN
	
	
	
	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof Space)) return false;
		Space s = (Space) o;
		//there cannot be multiple spaces with the same name
		return this.name.equals(s.name);
	}
	
	@Override
	public int hashCode() {
		int prime = 23;
		int result = 1;
		result = prime * result + (this.name.charAt(0) - 49);
		result = prime * result + (this.name.charAt(3) - 49);
		return result;
	}
	
	@Override
	public String toString() {
		return this.name + "  Rank: " + this.rank + "    ";
	}
	
	
	
}
