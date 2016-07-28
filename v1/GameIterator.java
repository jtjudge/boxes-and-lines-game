package jtjudge.Boxes.v1;

class GameIterator {

	private Move nonmove, nextmove, lastmove;
	private Space nonspace, nextspace, lastspace;
	private int rows, cols;
	
	GameIterator(Move nonmove, Space nonspace, int rows, int cols) {
		this.nonmove = nonmove;
		this.nonspace = nonspace;
		lastmove = nonmove;
		lastspace = nonspace;
		nextmove = lastmove.getNext();
		nextspace = lastspace.getNext();
		this.rows = rows;
		this.cols = cols;
	}

	boolean hasNextMove() {
		return nextmove != nonmove;
	}
	
	Move nextMove() {
		lastmove = nextmove;
		nextmove = nextmove.getNext();
		return lastmove;
	}
	
	boolean isOnLastRow() {
		return nextmove.getIndex() + cols >=
				(rows+1)*cols + (cols+1)*rows;
	}

	boolean hasNextSpace() {
		return nextspace != nonspace;
	}

	Space nextSpace() {
		lastspace = nextspace;
		nextspace = nextspace.getNext();
		return lastspace;
	}
	
	@Override
	public String toString() {
		return "Next Move: " + nextmove + "   Next Space: " + nextspace;
	}

}
	