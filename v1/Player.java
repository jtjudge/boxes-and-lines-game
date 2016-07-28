package jtjudge.Boxes.v1;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.Stack;

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
	
	/**
	 * Game session in which this player is participating.
	 */
	private Game game;

	/**
	 * Name of this player.
	 */
	private String name;
	
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
	
	/**
	 * Constructs a human player in a given game with a given name and mark.
	 * @param game
	 *  game session in which this player is participating
	 * @param name
	 *  name of this player
	 * @param mark
	 *  signature to be displayed on spaces filled by this player
	 */
	public Player(Game game, String name, char mark) {
		this.game = game;
		this.name = name;
		this.mark = mark;
		this.score = 0;
		this.isCPU = false;
	}
	
	/**
	 * Makes a CPU player with a given name, mark, and difficulty value 1 - 3.
	 * @param game
	 *  game session in which this CPU player is participating
	 * @param name
	 *  name of this CPU player
	 * @param mark
	 *  mark to be displayed on spaces filled by this CPU player
	 * @param diff
	 *  difficulty level of this CPU
	 */
	public Player(Game game, String name, char mark, int diff) {
		this.game = game;
		this.name = name;
		this.mark = mark;
		this.diff = diff;
		this.score = 0;
		this.isCPU = true;
		this.strategy = new ArrayList<>();
	}
	
	/**
	 * Runs the controlling algorithm for a CPU player. Returns highest priority move
	 * as determined by the CPU analysis of every possible move for the current turn.
	 * A higher difficulty rating results in a more thorough analysis.
	 * @return
	 *  CPU's best move
	 */
	public Move thinkOfMove() {
		if(!strategy.isEmpty()) return strategy.remove(0);
		ArrayList<Move> moves = this.game.getMoves();
		//analyze every move in the game
		for(Move move : moves) {
			move.analyze(diff);
		}
		//ensures moves with greater cost are later in the list
		Comparator<Move> c = new Comparator<Move>() {
			@Override
			public int compare(Move m, Move n) {
				return m.cost - n.cost;
			}
		};
		moves.sort(c);
		//if there are no moves that can be made without cost, the late game has begun
		if(moves.get(0).cost > 0) this.game.isLateGame = true;
		//if player is at least a level 3 CPU in the late game, do a predictive analysis
		//on moves with costs
//		if(diff >= 3 && this.game.isLateGame) {
//			for(Move move : moves) {
//				if(move.cost > 0) {
//					predictCost(move);
//				}
//			}	
//			//sort the moves again
//			moves.sort(c);
//			//create the chains currently on the board
//			ArrayList<Chain> chains = getChains(moves);
//		}
//		//if player is a level 4 CPU in the late game, and there are captures available, form the
//		//sequence of captures
//		if(diff == 4 && this.game.isLateGame) {	
//			if(moves.get(0).cost < 0) {
//				strategize(moves);
//				return strategy.remove(0);
//			}
//		}
		//check for multiple most efficient moves
		int count = 0;
		for(int i = 0, j = 1; j < moves.size(); i++, j++) {
			if(moves.get(i).cost != moves.get(j).cost) {
				break;
			}
			count++;
		}
		if(count == 0) {	//there is only one most efficient move
			Move m = moves.get(0);
			m.setPlayer(this);
			return m;
		} else {			//pick one at random
			Random gen = new Random();
			int index = gen.nextInt(count);
			Move m = moves.get(index);
			m.setPlayer(this);
			return m;
		}
	}
	
	/*
	 * Cost-analysis algorithms and high-level strategies
	 * 
	 * After a certain point in the game, making any move can result in a sequence
	 * of captures by the opponent. Any sequence of spaces that is vulnerable after
	 * the player's move must be included in a true cost analysis of that move.
	 * 
	 * High-level CPU players must shift focus from the analysis of single moves
	 * to the analysis of move sequences during the later parts of the game.
	 * 
	 * Furthermore, a high-level CPU must account for sequences that are vulnerable
	 * after a sequence of captures by the CPU itself, and weigh the benefits of its
	 * captures against those that will be made by the next player.
	 * 
	 * The goal of a level 3 CPU is to capture available sequences in their entirety
	 * while limiting the next player to the shortest sequences on the board.
	 * 
	 * The goal of a level 4 CPU is to control the game by capturing a sequence until
	 * it is reduced to two rank 3 spaces--meaning the next player only has the option of
	 * capturing the two spaces before setting up the next sequence.
	 * 
	 * 
	 * 
	 * For each of these algorithms, make a "fake" copy of the spaces in this game, 
	 * and use it to create a "fake" representation of the moves left in this game.
	 * 
	 * cost analysis algorithm (lv 3+ players)
	 * 
	 * execute this move's fake version
	 * make a stack of all the fakemoves that now have rank 3 spaces
	 * while the stack has fakemoves
	 * 	 	cost++
	 *   	execute each fakemove, remove fakemove from stack
	 *   	add fakemoves that now have rank 3 fakespaces to the stack
	 * return cost
	 * 
	 * strategy algorithm (lv 4+ players)
	 * 
	 * 
	 */
	
	
	/**
	 * Increments this player's score.
	 */
	public void scoreUp() {
		this.score++;
	}
	
	/**
	 * Returns the game session of this player.
	 * @return
	 *  player's game session
	 */
	public Game getGame() {
		return this.game;
	}
	
	/**
	 * Returns this player's name.
	 * @return
	 *  player's name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Returns the character the game draws in spaces filled by this player.
	 * @return
	 *  player's mark
	 */
	public char getMark() {
		return this.mark;
	}
	
	/**
	 * Returns this player's score in the game session
	 * @return
	 *  player's score
	 */
	public int getScore() {
		return this.score;
	}
	
	/**
	 * Returns true if the player is a CPU.
	 * @return
	 *   player's status as a CPU
	 */
	public boolean isCPU() {
		return isCPU;
	}
	
	/**
	 * Returns this CPU player's difficulty level. Returns null if player is human.
	 * @return
	 *  CPU player's difficulty
	 */
	public int getDiff() {
		return this.diff;
	}
	
}
