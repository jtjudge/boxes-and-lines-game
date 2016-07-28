package jtjudge.Boxes.v1;

import java.util.ArrayList;

/**
 * @author jtjudge
 * 
 * A Chain object represents a sequence of spaces that are available for capture after
 * just one of the chain members is elevated to rank 3. Chains are used in high-level
 * CPU game analysis.
 *
 */
public class Chain {
	
	private ArrayList<Move> members;
	
	//the end spaces of this chain, used for merge operations
	private Space start;
	private Space end;
	private ArrayList<Space> lefts;
	private ArrayList<Space> rights;
	
	private boolean closed;
	
	/**
	 * Constructs a chain based on the first rank 2 move
	 * @param m
	 */
	public Chain(Move m) {
		members = new ArrayList<>();
		lefts = new ArrayList<>();
		rights = new ArrayList<>();
		addMove(m);
		closed = true;
	}
	
	public void addMove(Move m) {
		members.add(m);
		m.chain = this;
		Space left = m.getLine().getLeft();
		Space right = m.getLine().getRight();
		if(left != null) lefts.add(left);
		if(right != null) rights.add(right);
		findEnds();
	}
	
	private void findEnds() {
		//for any move in the chain, the right and left spaces will
		//overlap with another member's right or left space, unless
		//that move is on the end.
		for(Space left : lefts) {
			if(!rights.contains(left)) {
				start = left;
			}
		}
		for(Space right : rights) {
			if(!lefts.contains(right)) {
				end = right;
			}
		}
	}
	
	public void removeMove(Move m) {
		members.remove(m);
		m.chain = null;
		Space left = m.getLine().getLeft();
		Space right = m.getLine().getRight();
		if(left != null) lefts.remove(left);
		if(right != null) rights.remove(right);
		findEnds();
	}
	
	public ArrayList<Move> getMembers() {
		return members;
	}
	
	public boolean hasMove(Move m) {
		return members.contains(m);
	}
	
	public boolean hasEndSpace(Space s) {
		return (start == s || end == s);
	}
	
	public boolean isEmpty() {
		return members.size() == 0;
	}
	
	public boolean isClosed() {
		return closed;
	}
	
	public int getNumMoves() {
		return members.size();
	}
	
	public int getNumSpaces() {
		return lefts.size();
	}
	
	public void open() {
		closed = false;
	}
	
	public String toString() {
		String output = "";
		if(getNumMoves() > 1) {
			for(int i = 0; i < members.size() - 2; i++) {
				Move m = members.get(i);
				output += "(" + m.getName() + ")--";
			}
		}
		Move m = members.get(members.size() - 1);
		output += "(" + m.getName() + ")";
		output += "    # moves: " + getNumMoves() + "    # spaces: " + getNumSpaces();
		return output;
	}
	
}