package jtjudge.Boxes.v1;

class Move {

	private final String name;
	private final int index;
	private boolean isAvailable;
	private boolean isStrategized;
	private int hashcode;	//cached after first call to hashCode()

	private Space left, right;
	private Chain chain;

	private Move next;
	
	Move(String name, int index, Move next) {
		this.name = name;
		this.index = index;
		this.isAvailable = true;
		this.isStrategized = false;
		this.hashcode = 0;
		this.next = next;
	}
	
	int calculateBasicCost(int diff) {
		int cost = 0, lrank, rrank;
		if(this.left == null) {
			lrank = 0;
		} else {
			lrank = this.left.getRank();
		}
		if(this.right == null) {
			rrank = 0;
		} else {
			rrank = this.right.getRank();
		}
		if(diff == 1) {
			if(lrank == 3 || rrank == 3) cost--;
		}
		if(diff >= 2) {
			if(lrank == 3) {
				cost--;
				if(rrank == 3) cost--;
				if(rrank == 2) cost-=2;
			} else if(rrank == 3) {
				cost--;
				if(lrank == 3) cost--;
				if(lrank == 2) cost-=2;
			}
			if(lrank == 2) {
				cost++;
				if(rrank != 3) cost++;
			} else if(rrank == 2) {
				cost++;
				if(lrank != 3) cost++;
			}
		}
		return cost;
	}

	int getIndex() { return this.index; }
	
	Move getNext() { return this.next; }
	
	void setNext(Move m) { this.next = m; }
	
	Space getLeft() { return this.left; }
	
	void setLeft(Space s) { this.left = s; }
	
	Space getRight() { return this.right; }
	
	void setRight(Space s) { this.right = s; }

	boolean isAvailable() { return this.isAvailable; }
	
	void makeUnavailable() {
		this.isAvailable = false;
		this.chain = null;
	}
	
	boolean isStrategized() { return this.isStrategized; }
	
	void setStrategized(boolean set) { this.isStrategized = set; }
	
	boolean hasChain() { return this.chain != null; }
	
	Chain getChain() { return this.chain; }
	
	void setChain(Chain c) { this.chain = c; }

	boolean hasRankThree() {
		return (this.left != null && this.left.getRank() == 3) ||
				(this.right != null && this.right.getRank() == 3);
	}

	@Override
	public boolean equals(Object o) {
		if(o != null && this == o) return true;
		if(!(o instanceof Move)) return false;
		Move m = (Move) o;
		return this.index == m.index;
	}
	
	@Override
	public int hashCode() {
		if(this.hashcode == 0) {
			int prime = 31, result = 1;
			result = prime * result + this.index;
			this.hashcode = result;
		}
		return this.hashcode;
	}
	
	@Override
	public String toString() {
		return this.name;
	}

}
