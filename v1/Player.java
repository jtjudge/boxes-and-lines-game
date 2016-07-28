package jtjudge.Boxes.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

/**
 * @author jtjudge
 * 
 * A Player object represents a single player in a single game. It holds 
 * a reference to the game in which this player is participating as well 
 * as this player's user-determined name in String form, this player's 
 * user-determined character with which the game marks the spaces filled 
 * by this player, and this player's score in the game. Also in a Player 
 * object is a boolean value that indicates if this player is controlled 
 * by the CPU algorithm, and the difficulty level of this Player if it is 
 * indeed a CPU.
 *
 */
public class Player {
	
	
	
	//FIELDS
	
	
	
	/**
	 * Name of this player.
	 */
	private String name;
	
	/**
	 * Game session in which this player is participating.
	 */
	private Game game;

	/**
	 * Mark to be displayed on spaces filled by this player.
	 */
	private char mark;
	
	/**
	 * This player's current score.
	 */
	private int score;
	
	/**
	 * This player's status as CPU controlled.
	 */
	private boolean isCPU;
	
	/**
	 * The difficulty level of this player if CPU controlled.
	 */
	private int diff;
	
	//used by Lv 4-5 CPUs
	private ArrayList<Move> strategy;
	
	public static ArrayList<Player> activePlayers = new ArrayList<>();
	
	private static int playerTurnIndex = 0;
	
	
	
	//CONSTRUCTORS / FACTORIES
	
	
	
	private Player() {
		//suppress default constructor
	}
	
	public static Player constructHumanPlayer(Game game, String name, char mark)
			throws IllegalArgumentException {
		if(game == null || name == null || !game.isActive()) 
			throw new IllegalArgumentException();
		Player p = new Player();
		p.name = name;
		p.game = game;
		p.mark = mark;
		p.score = 0;
		p.isCPU = false;
		if(activePlayers.contains(p)) {
			throw new IllegalArgumentException();
		}
		activePlayers.add(p);
		if(activePlayers.size() == 1) {
			game.setFirstPlayer(changeTurn());
		}
		return p;
	}
	
	public static Player constructComputerPlayer(Game game, String name, char mark, Integer diff)
			throws IllegalArgumentException {
		if(game == null || name == null || diff == null || !game.isActive())
			throw new IllegalArgumentException();
		Player p = new Player();
		p.name = name;
		p.game = game;
		p.mark = mark;
		p.score = 0;
		p.isCPU = true;
		p.diff = diff;
		p.strategy = new ArrayList<>();
		if(activePlayers.contains(p)) {
			throw new IllegalArgumentException();
		}
		activePlayers.add(p);
		if(activePlayers.size() == 1) {
			game.setFirstPlayer(changeTurn());
		}
		return p;
	}
	
	
	
	//KEY METHODS
	
	
	
	public static Player changeTurn() {
		if(activePlayers.isEmpty()) throw new IllegalStateException();
		playerTurnIndex++;
		if(playerTurnIndex == activePlayers.size()) playerTurnIndex = 0;
		Player p = activePlayers.get(playerTurnIndex);
		return p;
	}
	
	public static boolean removePlayer(Player p) {
		if(activePlayers.isEmpty()) throw new IllegalArgumentException();
		return activePlayers.remove(p);
	}
	
	/**
	 * Runs the controlling algorithm for a CPU player. Returns highest priority move
	 * as determined by the CPU analysis of every possible move for the current turn.
	 * A higher difficulty rating results in a more thorough analysis.
	 * @return
	 *  CPU's best move
	 */
	public Move thinkOfMove() {
		if(strategy.size() != 0) {
			Move m = strategy.remove(0);
			m.setPlayer(this);
			return m;
		}
		if(diff == 1 || diff == 2) {
			ArrayList<Move> best = new ArrayList<>();
			int mCost, minCost = Integer.MAX_VALUE;
			for(Move m : Move.movesLeft) {
				m.calculateBasicCost(diff);
				mCost = m.getCost();
				if(mCost < minCost) {
					minCost = mCost;
					best.clear();
					best.add(m);
				} else if(mCost == minCost) {
					best.add(m);
				}
			}
			if(best.size() > 1) {
				Move m = best.get(new Random().nextInt(best.size()));
				m.setPlayer(this);
				return m;
			}
			Move m = best.get(0);
			m.setPlayer(this);
			return m;
		} else if(diff == 3 || diff == 4) {
			ArrayList<Chain> openChains = new ArrayList<>();
			//if every move is in chains
			if(this.game.isEndGame()) {
				//start the end game
				//find all the open chains and the smallest closed chain
				Chain bestClosed = null;
				int cSize, minSize = Integer.MAX_VALUE;
				for(Chain c : Chain.activeChains) {
					cSize = c.getNumMoves();
					if(!c.isClosed()) {
						openChains.add(c);
					} else {
						if(cSize < minSize) {
							minSize = cSize;
							bestClosed = c;
						}
					}
				}
				//DEBUG
				if(bestClosed == null) {
					System.out.println("Smallest closed: none");
				} else {
					System.out.println("Smallest closed: " + bestClosed.getID());
				}
				//if all chains are closed, open the smallest chain
				if(openChains.isEmpty()) {
					if(bestClosed == null) throw new IllegalStateException();
					Move m = bestClosed.getMembers().get(0);
					m.setPlayer(this);
					return m;
				//if any chains are open
				} else {
					if(diff == 3) {
						//if Lv 3, take every open chain
						for(Chain c : openChains)
							for(Move m : c.getMembers()) {
								this.strategy.add(m);
							}
						Move m = this.strategy.remove(0);
						m.setPlayer(this);
						return m;
					} else {
						//If the open chains only have one space, or if the smallest closed 
						//chain (the one this player will be left to capture) has fewer spaces than
						//the number of spaces this player will leave the next player, it is not 
						//worth it to leave the next player any spaces.
						int numOpen = 0;
						for(Chain c: openChains) {
							numOpen += c.getNumSpaces();
						}
						if(numOpen == openChains.size() || bestClosed == null ||
								bestClosed.getNumSpaces() < 2 || Player.activePlayers.size() > 2) {
							//take everything
							for(Chain c1: openChains) {
								for(Move m : c1.getMembers()) {
									this.strategy.add(m);
								}
							}
							Move m = this.strategy.remove(0);
							m.setPlayer(this);
							return m;
						} else {
							//sort by chain size in ascending order
							Comparator<Chain> comp = new Comparator<Chain>() {
								@Override
								public int compare(Chain c1, Chain c2) {
									return c1.getNumMoves() - c2.getNumMoves();
								}
							};
							Collections.sort(openChains, comp);
							//make all moves but the second to last (leaving two spaces)
							ArrayList<Move> moves = new ArrayList<>();
							for(Chain c1: openChains) {
								for(Move m : c1.getMembers()) {
									moves.add(m);
								}
							}
							for(int i = 0; i < moves.size(); i++) {
								if(i != moves.size() - 2) {
									this.strategy.add(moves.get(i));
								}
							}
							Move m = this.strategy.remove(0);
							m.setPlayer(this);
							return m;
						}
					}
				}
			} else {
				//find the open chains
				for(Chain c : Chain.activeChains) {
					if(!c.isClosed()) {
						openChains.add(c);
					}
				}
				//if there are any chains open, take every one of them
				if(!openChains.isEmpty()) {
					for(Chain open : openChains) {
						for(Move m : open.getMembers()) {
							this.strategy.add(m);
						}
					}
					Move m = this.strategy.remove(0);
					m.setPlayer(this);
					return m;
				} else {
					//return a random nonchain move
					int i = new Random().nextInt(Move.nonChains.size());
					int j = 0;
					for(Move m : Move.nonChains) {
						if(i == j++) {
							m.setPlayer(this);
							return m;
						}
					}
				}
				return null;
			}
		}
		return null;
	}
	
	/**
	 * Increments this player's score.
	 */
	public void scoreUp() {
		this.score++;
	}
	
	
	
	//GETTERS
	
	
	
	public Game getGame() { return this.game; }
	
	public String getName() { return this.name; }
	
	public char getMark() { return this.mark; }
	
	public int getScore() { return this.score; }
	
	public boolean isCPU() { return isCPU; }
	
	public int getDiff() { return this.diff; }
	
	
	
	//OVERRIDDEN
	
	
	
	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof Player)) return false;
		Player p = (Player) o;
		//there cannot be multiple players with the same name
		return this.name.equals(p.name);
	}
	
	@Override
	public int hashCode() {
		int prime = 19;
		int result = 1;
		for(int i = 0; i < this.name.length(); i++) {
				result = prime * result + (int) this.name.charAt(i);
		}
		return result;
	}
	
	@Override
	public String toString() {
		return "Name: " + this.name +
				"\nMark: " + Character.toString(this.mark) +
				"\nScore: " + this.score;
	}
	
	
	
}
