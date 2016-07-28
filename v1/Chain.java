package jtjudge.Boxes.v1;

import java.util.ArrayList;
import java.util.Collections;

class Chain {

	private char index;
	
	private ArrayList<Move> members;
	
	private ArrayList<Space> allSpaces;
	
	private ArrayList<Space> sharedSpaces;
	
	private ArrayList<Space> ends;
	
	private boolean open;
	
	private boolean isCycle;
	
	Chain(Move m1, Move m2) {
		this.members = new ArrayList<>();
		this.sharedSpaces = new ArrayList<>();
		this.allSpaces = new ArrayList<>();
		this.ends = new ArrayList<>();
		this.open = false;
		addAtEnd(m1);
		addAtEnd(m2);
		findEnds();
	}

	void putAtEnd(Move m) {
		if(m == null) throw new NullPointerException();
		addAtEnd(m);
		findEnds();
	}
	
	void putAtBeginning(Move m) {
		if(m == null) throw new NullPointerException();
		addAtZero(m);
		findEnds();
	}
	
	//Removes a given move from the chain, reordering the chain according to the
	//move removed. This method ensures that a CPU player will always capture the
	//complete chain even when another player makes moves out of chain order.
	boolean takeOut(Move m) {
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
						remove(move);
					} else {
						i++;
					}
				}
				Collections.reverse(temp1);
				//the given move m is now the first in the chain
				//add the rest back--smallest side first
				if(temp1.size() > temp2.size()) {
					for(Move right : temp2) {
						addAtEnd(right);
					}
					for(Move left : temp1) {
						addAtEnd(left);
					}
				} else {
					for(Move left : temp1) {
						addAtEnd(left);
					}
					for(Move right : temp2) {
						addAtEnd(right);
					}
				}
			}
		}
		//remove m from the chain
		remove(m);
		//open the chain, if not open already
		if(!open) open = true;
		//update the set of end spaces
		findEnds();
		if(members.isEmpty()) return true;
		return false;
	}
	
	void absorb(Chain c) {
		for(Move m : c.members) {
			addAtEnd(m);
		}
		findEnds();
	}
	
	void invert() {
		Collections.reverse(members);
		Collections.reverse(allSpaces);
		Collections.reverse(sharedSpaces);
	}

	char getIndex() { return this.index; }
	
	void setIndex(char c) { this.index = c; }
	
	ArrayList<Move> getMembers() { return this.members; }
	
	Move getLastMove() { return this.members.get(members.size() - 1); }
	
	ArrayList<Space> getEnds() { return this.ends; }
	
	int getNumMoves() { return this.members.size(); }
	
	int getNumSpaces() { return this.sharedSpaces.size(); }
	
	boolean isEmpty() { return this.members.size() == 0; }
	
	boolean isOpen() { return this.open; }
	
	void setOpen() { this.open = true; }
	
	boolean isCycle() { return this.isCycle; }
	
	boolean hasEndSpace(Space s) { return this.ends.contains(s);}
	
	@Override
	public String toString() {
		String output = Character.toString(index) + ": ";
		if(getNumMoves() > 1) {
			for(int i = 0; i < members.size() - 1; i++) {
				Move m = members.get(i);
				output += "(" + m.toString() + ")--";
			}
		}
		if(!members.isEmpty()) {
			Move m = members.get(members.size() - 1);
			output += "(" + m + ")";
		}
		output += "    # moves: " + getNumMoves() + "    # spaces: " + getNumSpaces();
		if(open) {
			output += "    open";
		} else {
			output += "    closed";
		}
		if(isCycle) {
			output += " cycle";
		}
		return output;
	}
	
	private void addAtEnd(Move m) {
		members.add(m);
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
	
	private void addAtZero(Move m) {
		members.add(0, m);
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
	
	private void remove(Move m) {
		if(m == null) throw new NullPointerException();
		if(members.isEmpty()) throw new IllegalStateException();
		members.remove(m);
		if(this.members.size() == 0) {
			sharedSpaces.clear();
			allSpaces.clear();
			ends.clear();
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
	
	private void findEnds() {
		ends.clear();
		if(members.size() < 4) {
			this.isCycle = false;
		} else {
			this.isCycle = true;
		}
		if(members.isEmpty()) return;
		Move start = members.get(0);
		Move end = members.get(members.size() - 1);
		//at maximum, two of these will be true
		Space left = start.getLeft(), right = start.getRight();
		if(left == null || right == null) {
			this.isCycle = false;
		}
		if(left != null && !sharedSpaces.contains(left)) ends.add(left);
		if(right != null && !sharedSpaces.contains(right)) ends.add(right);
		if(members.size() != 1) {
			left = end.getLeft(); right = end.getRight();
			if(left == null || right == null) {
				this.isCycle = false;
			}
			if(left != null && !sharedSpaces.contains(left)) ends.add(left);
			if(right != null && !sharedSpaces.contains(right)) ends.add(right);
		}
		if(!ends.isEmpty()) {
			this.isCycle = false;
		}
	}
		
}
