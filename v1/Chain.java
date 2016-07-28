package jtjudge.Boxes.v1;

import java.util.ArrayList;

/**
 * @author jtjudge
 * 
 * A Chain object represents a sequence of spaces that will be captured after just one
 * of the chain members is elevated to rank 3. Chains are used in high-level CPU game
 * analysis.
 *
 */
public class Chain {
	
	private ArrayList<Move> members;
	
	/**
	 * Constructs a chain based on the first rank 2 move
	 * @param m
	 */
	public Chain(Move m) {
		members = new ArrayList<>();
		members.add(m);
	}
	
	public void addMove(Move m) {
		members.add(m);
	}
	
	public ArrayList<Move> getMembers() {
		return members;
	}
	
	public boolean hasMove(Move m) {
		return members.contains(m);
	}
	
}