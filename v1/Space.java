package jtjudge.Boxes.v1;

import java.awt.Color;
import java.util.ArrayList;

class Space {
	
	private final int index;
	private int rank;
	private boolean full;
	private char mark;		//stored when space is filled
	private Color color;
	private int hashcode;	//cached after first call to hashCode()
	
	private Space next;		//for game iterator
	
	private ArrayList<Move> unmadeMoves;
	
	Space(int index, Space next) {
		this.index = index;
		this.rank = 0;
		this.full = false;
		this.hashcode = 0;
		this.next = next;
		this.unmadeMoves = new ArrayList<>();
	}

	boolean add(Move m) {
		if(m == null || unmadeMoves.size() == 4) return false;
		return this.unmadeMoves.add(m);
	}
	
	boolean remove(Move m) {
		if(m == null || this.full) return false;
		return this.unmadeMoves.remove(m);
	}
	
	boolean rankUp(char ch, Color color) {
		if(full) return false;
		if(++rank == 4) {
			this.full = true;
			this.mark = ch;
			this.color = color;
			return true;
		}
		return false;
	}
	
	int getIndex() { return this.index; }
	
	int getRank() { return this.rank; }
	
	boolean isFull() { return this.full; }
	
	char getMark() { return this.mark; }
	
	Color getColor() { return this.color; }
	
	Space getNext() { return this.next; }
	
	void setNext(Space s) { this.next = s; }
	
	ArrayList<Move> getUnmadeMoves() { return this.unmadeMoves; }
	
	@Override
	public boolean equals(Object o) {
		if(o != null && this == o) return true;
		if(!(o instanceof Space)) return false;
		Space s = (Space) o;
		return this.index == s.index;
	}
	
	@Override
	public int hashCode() {
		if(hashcode == 0) {
			int prime = 23, result = 1;
			result = prime * result + this.index;
			hashcode = result;
		}
		return hashcode;
	}
	
	@Override
	public String toString() {
		return "" + this.index;
	}

}
