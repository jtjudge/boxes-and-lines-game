package jtjudge.Boxes.v1;

/**
 * @author jtjudge
 * 
 * A Space object represents a space on the game board. To win the game, players
 * fill these spaces by surrounding them with lines. A Space contains a rank that
 * represents its progress toward being filled, its status as filled or unfilled,
 * and a reference to the player who filled them, if any.
 */
public class Space {

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
	}
	
	//copy constructor
	public Space(Space s) {
		this.name = s.name;
		this.rank = s.rank;
		this.full = s.full;
		this.player = s.player;
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
	public boolean rankUp(Player player) {
		if(full) return false;
		if(++rank == 4) {
			full = true;
			if(player != null) {
				this.player = player;
				this.player.scoreUp();
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the name of this space
	 * @return
	 *  this space's name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Returns the current rank of this space
	 * @return
	 *  this space's rank
	 */
	public int getRank() {
		return this.rank;
	}
	
	/**
	 * Returns true if the space is filled.
	 * @return
	 *  true if full
	 */
	public boolean isFull() {
		return this.full;
	}
	
	/**
	 * Returns the player that filled the space, or null if space is unfilled.
	 * @return
	 *  associated player
	 */
	public Player getPlayer() {
		return this.player;
	}
	
}

