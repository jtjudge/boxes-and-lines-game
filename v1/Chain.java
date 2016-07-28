package jtjudge.Boxes.v1;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author jtjudge
 * 
 * A Chain object represents a sequence of spaces that are available for capture after
 * just one of the chain members is elevated to rank 3. Chains are used in high-level
 * CPU game analysis.
 *
 */
public class Chain {
	
	
	
	//FIELDS
	
	
	
	private ArrayList<Move> members;
	
	private ArrayList<Space> sharedSpaces;
	
	private ArrayList<Space> allSpaces;
	
	private ArrayList<Space> ends;
	
	private boolean closed;
	
	private char id;
	
	public static ArrayList<Chain> activeChains = new ArrayList<>();
	
	
	
	//CONSTRUCTORS / FACTORIES
	
	
	
	public Chain() {
		//suppress default constructor
	}
	
	public static void constructChain(Move m1, Move m2) {
		Chain c = new Chain();
		c.members = new ArrayList<>();
		c.sharedSpaces = new ArrayList<>();
		c.allSpaces = new ArrayList<>();
		c.ends = new ArrayList<>();
		c.closed = true;
		c.addMove(m1);
		c.addMove(m2);
		activeChains.add(c);
		updateActiveChains(c);
	}
	
	
	
	//KEY METHODS
	
	
	
	public void addMove(Move m) {
		members.add(m);
		m.addChain(this);
		Space left = m.getLeft();
		Space right = m.getRight();
		if(left != null) {
			if(allSpaces.contains(left)) {
				if(!sharedSpaces.contains(left)) sharedSpaces.add(left);
			} else {
				allSpaces.add(left);
			}
		}
		if(right != null) {
			if(allSpaces.contains(right)) {
				if(!sharedSpaces.contains(right)) sharedSpaces.add(right);
			} else {
				allSpaces.add(right);
			}
		}
	}
	
	public void removeMove(Move m) {
		if(members.isEmpty()) throw new IllegalStateException();
		members.remove(m);
		m.removeChain(this);
		//once the chain is empty, it is removed from the game
		if(this.members.size() == 0) {
			sharedSpaces.clear();
			allSpaces.clear();
			ends.clear();
			activeChains.remove(this);
		} else {
			Space left = m.getLeft();
			Space right = m.getRight();
			if(left != null && allSpaces.contains(left)) {
				if(sharedSpaces.contains(left)) {
					if(left.isFull()) {
						sharedSpaces.remove(left);
					}
				} else {
					allSpaces.remove(left);
				}
			}
			if(right != null && allSpaces.contains(right)) {
				if(sharedSpaces.contains(right)) {
					if(right.isFull()) {
						sharedSpaces.remove(right);
					}
				} else {
					allSpaces.remove(right);
				}
			}
		}
	}
	
	//Removes a given move from the chain, reordering the chain according to the
	//move removed. This method ensures that a CPU player will always capture the
	//complete chain even when another player makes moves out of chain order.
	public void makeMove(Move m) {
		if(members.isEmpty()) throw new IllegalStateException();
		//get the index of this move within the chain
		int index = members.indexOf(m);
		//if the move is not at the start of the chain
		if(index != 0) {
			if(index == members.size() - 1) {
				//if it is at the end, invert the chain
				invert();
			} else {
				//if it is in the middle somewhere, split the moves
				//to its left and right into two lists, removing
				//each one from the chain
				ArrayList<Move> temp1 = new ArrayList<>();
				ArrayList<Move> temp2 = new ArrayList<>();
				int i = 0;
				while(members.size() > 1) {
					Move move = members.get(i);
					if(!move.equals(m)) {
						if(members.indexOf(move) < members.indexOf(m)) {
							//add left moves to temp1
							temp1.add(move);
						} else {
							//add right moves to temp2
							temp2.add(move);	
						}
						removeMove(move);
					} else {
						i++;
					}
				}
				Collections.reverse(temp1);
				//the given move m is now the first in the chain
				//add the rest back--smallest side first
				if(temp1.size() > temp2.size()) {
					for(Move right : temp2) {
						addMove(right);
					}
					for(Move left : temp1) {
						addMove(left);
					}
				} else {
					for(Move left : temp1) {
						addMove(left);
					}
					for(Move right : temp2) {
						addMove(right);
					}
				}
			}
		}
		//remove m from the chain
		removeMove(m);
		//open the chain, if not open already
		if(closed) open();
		//update the set of end spaces
		findEnds();
		Space.updateEndSpaces();
	}
	
	
	
	//GETTERS
	
	
	
	public ArrayList<Move> getMembers() { return this.members; }
	
	public ArrayList<Space> getEnds() { return this.ends; }
	
	public int getNumMoves() { return this.members.size(); }
	
	public int getNumSpaces() { return this.sharedSpaces.size(); }
	
	public char getID() { return this.id; }
	
	public boolean isEmpty() { return this.members.size() == 0; }
	
	public boolean isClosed() { return this.closed; }
	
	public boolean hasEndSpace(Space s) { return this.ends.contains(s);}
	
	
	
	//OVERRIDDEN

	
	
	@Override
	public String toString() {
		String output = id + ": ";
		if(getNumMoves() > 1) {
			for(int i = 0; i < members.size() - 1; i++) {
				Move m = members.get(i);
				output += "(" + m.getName() + ")--";
			}
		}
		Move m = members.get(members.size() - 1);
		output += "(" + m.getName() + ")";
		output += "    # moves: " + getNumMoves() + "    # spaces: " + getNumSpaces();
		if(isClosed()) {
			output += "    closed";
		} else {
			output += "    open";
		}
		return output;
	}
	
	
	
	//HELPERS
	
	
	
	private static void updateActiveChains(Chain c) {
		//It is possible for chain c to have moves that belong to other chains.
		//Because c was just created, it has only two moves, both of which can be
		//doubles.
		//find the doubles
		ArrayList<Move> doubles = new ArrayList<>();
		for(Move m : c.members) {
			if(m.hasTwoChains()) {
				doubles.add(m);
			}
		}
		if(!doubles.isEmpty()) {
			Move firstdub = doubles.get(0);
			Chain c1 = firstdub.getChains().get(0);	//the older chain
			Chain c2 = firstdub.getChains().get(1);	//the new chain, c
			//remove the duplicates from c
			for(Move dub : doubles) {
				c2.removeMove(dub);
			}
			//if the double is not the last move in the older chain, invert the
			//older chain
			int lastc1 = c1.getNumMoves() - 1;
			if(!c1.getMembers().get(lastc1).equals(firstdub)) {
				c1.invert();
			}
			//add the rest of c's members to the older chain
			while(!c2.isEmpty()) {
				Move m1 = c2.members.get(0);
				c2.removeMove(m1);
				c1.addMove(m1);
			}
			doubles.remove(0);
			//if there is a second double
			if(doubles.size() == 1) {
				Move seconddub = doubles.get(0);
				Chain c3 = seconddub.getChains().get(0); //the other old chain
				//if the last move in c3 is equal to seconddub, invert c3
				int lastc3 = c3.getNumMoves() - 1;
				if(c3.getMembers().get(lastc3).equals(seconddub)) {
					c3.invert();
				}
				//merge the two chains
				while(!c3.isEmpty()) {
					Move m1 = c3.members.get(0);
					c3.removeMove(m1);
					c1.addMove(m1);
				}
				doubles.remove(0);
				//open the completed chain if either of the others were open
				if(!c3.isClosed() && c1.isClosed()) {
					c1.open();
				}
			}
			if(!c2.isClosed() && c1.isClosed()) {
				c1.open();
			}
			if(!c1.isClosed()) {
				if(!c1.getMembers().get(0).hasRankThree()) {
					c1.invert();
				}
			}
			c1.findEnds();
		} else {
			c.findEnds();
		}
		//keep the chain names and end spaces updated
		int index = 0;
		for(Chain n : activeChains) {
			n.id = (char) (index++ + 49);
			Space.updateEndSpaces();
		}
	}
	
	private void invert() {
		Collections.reverse(this.members);
		Collections.reverse(this.allSpaces);
		Collections.reverse(this.sharedSpaces);
	}
	
	private void open() {
		closed = false;
	}
	
	private void findEnds() {
		this.ends.clear();
		if(members.isEmpty()) return;
		Move start = members.get(0);
		Move end = members.get(members.size() - 1);
		//at maximum, two of these will be true
		Space left = start.getLeft(), right = start.getRight();
		if(left != null && !sharedSpaces.contains(left)) ends.add(left);
		if(right != null && !sharedSpaces.contains(right)) ends.add(right);
		if(members.size() != 1) {
			left = end.getLeft(); right = end.getRight();
			if(left != null && !sharedSpaces.contains(left)) ends.add(left);
			if(right != null && !sharedSpaces.contains(right)) ends.add(right);
		}
	}
	

}