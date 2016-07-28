package jtjudge.Boxes.v1;

import java.util.ArrayList;

class Player {

	private String name;
	private char mark;
	private int score;
	private int wins;
	private int hashcode;	//cached after first call to hashCode()
	
	private boolean isCPU;
	private int diff;
	
	private static final int MIN_DIFF = 1;
	private static final int MAX_DIFF = 5;
	
	private ArrayList<Move> strategy;	//used by Lv 4-5 CPUs

	private Player() {
		//suppress default constructor
	}
	
	static Player constructHumanPlayer(String name, char mark) {
		if(name.isEmpty()) throw new IllegalArgumentException();
		Player p = new Player();
		p.name = name;
		p.mark = mark;
		p.score = 0;
		p.wins = 0;
		p.hashcode = 0;
		p.isCPU = false;
		p.strategy = new ArrayList<>();	//used only in windowed form
		return p;
	}
	
	static Player constructComputerPlayer(String name, char mark, int diff) {
		if(name.isEmpty() || diff > MAX_DIFF || diff < MIN_DIFF)
			throw new IllegalArgumentException();
		Player p = new Player();
		p.name = name;
		p.mark = mark;
		p.score = 0;
		p.wins = 0;
		p.hashcode = 0;
		p.isCPU = true;
		p.diff = diff;
		p.strategy = new ArrayList<>();
		return p;
	}

	Move thinkOfMove(Game game, int diff) {
		if(strategy.isEmpty()) {
			strategy = game.analyze(diff);
			for(Move m : strategy) {
				m.setStrategized(true);
			}
		}
		Move m = strategy.remove(0);
		return m;
	}
	
	void addToStrategy(Move m) {
		if(!strategy.contains(m)) {
			strategy.add(m);
			m.setStrategized(true);
		}
	}
	
	String getName() { return this.name; }
	
	char getMark() { return this.mark; }
	
	boolean isCPU() { return isCPU; }
	
	int getDiff() { return this.diff; }
	
	void scoreUp() { this.score++; }
	
	int getScore() { return this.score; }
	
	void winsUp() { this.wins++; }
	
	int getWins() { return this.wins; }
	
	boolean hasStrategy() { return !this.strategy.isEmpty(); }
	
	Move doStrategy() { return this.strategy.remove(0); }
	
	@Override
	public boolean equals(Object o) {
		if(o != null && this == o) return true;
		if(!(o instanceof Player)) return false;
		Player p = (Player) o;
		return this.name.equals(p.name);
	}
	
	@Override
	public int hashCode() {
		if(hashcode == 0) {
			int prime = 19;
			int result = 1;
			for(int i = 0; i < this.name.length(); i++) {
					result = prime * result +
							(int) this.name.charAt(i);
			}
			hashcode = result;
		}
		return hashcode;
	}
	
	@Override
	public String toString() {
		return "Name: " + this.name +
				"\nMark: " + Character.toString(this.mark) +
				"\nScore: " + this.score;
	}
	
}
