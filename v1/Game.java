package jtjudge.Boxes.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

class Game {

	private final int rows;
	private final int cols;
	private static final int ROW_LIMIT = Integer.MAX_VALUE;
	private static final int COL_LIMIT = Integer.MAX_VALUE;
	
	private int spacesLeft;
	
	private ArrayList<Player> players;
	private Player currentTurn;
	
	Move nonmove;	//for iterator
	private Move mostRecent;
	private HashMap<String, Move> legalMoves;
	private HashSet<Move> nonChains;

	Space nonspace;	//for iterator
	
	//used by high-level CPU players
	private boolean isEndGame;
	private HashSet<Chain> activeChains;
	
	private HashSet<Space> endSpaces;
	private HashSet<Space> sharedEndSpaces;
	
	private HashSet<Move> mergers;
	private HashSet<Move> blockers;
	
	//DEBUG MODE
	private int seed;
	private static final int NO_SEED = 0;
	
	Game(int rows, int cols) {
		if(rows < 1 || rows > ROW_LIMIT || 
				cols < 1 || cols > COL_LIMIT) {
			throw new IllegalArgumentException();
		}
		this.rows = rows;
		this.cols = cols;
		spacesLeft = rows * cols;
		players = new ArrayList<>();
		legalMoves = new HashMap<>();
		nonChains = new HashSet<>();
		this.seed = NO_SEED;
		endSpaces = new HashSet<>();
		sharedEndSpaces = new HashSet<>();
		mergers = new HashSet<>();
		blockers = new HashSet<>();
		isEndGame = false;
		activeChains = new HashSet<>();
		constructSpaces();
		constructMoves();
		connectMovesToSpaces();
	}
	
	//DEBUG MODE
	Game(int rows, int cols, int seed) {
		this(rows, cols);
		this.seed = seed;
	}

	GameIterator getIterator() {
		return new GameIterator(nonmove, nonspace, rows, cols);
	}
	
	void make(Move m, Player p) {
		if(players.isEmpty()) throw new IllegalStateException();
		if(m == null || p == null) throw new NullPointerException();
		if(!m.isAvailable()) throw new IllegalStateException();
		int num = 0;
		Space left = m.getLeft(), right = m.getRight();
		if(left != null) left.remove(m);
		if(right != null) right.remove(m);
		if(m.hasChain()) {
			Chain c = m.getChain();
			if(c.takeOut(m)) {
				activeChains.remove(c);
			}
		}
		legalMoves.remove(m.toString());
		nonChains.remove(m);
		m.makeUnavailable();
		m.setStrategized(false);
		mostRecent = m;
		if(left != null && left.rankUp(p.getMark())) {
			num++;
			p.scoreUp();
		} else if(left != null && left.getRank() == 2) {
			buildChain(left);
		}
		if(right != null && right.rankUp(p.getMark())) {
			num++;
			p.scoreUp();
		} else if(right != null && right.getRank() == 2) {
			buildChain(right);
		}
		updateEndSpaces();
		spacesLeft -= num;
		if(num == 0) changeTurn();
		if(nonChains.isEmpty() && !isEndGame) isEndGame = true;
	}
	
	//Parses the user input and attempts to find the matching move. Returns null if the
	//input string is invalid, otherwise returns the move.
	Move retrieve(String name) {
		Move m = legalMoves.get(name);
		return m;
	}
	
	boolean add(Player p) {
		if(players.contains(p)) return false;
		if(players.isEmpty()) currentTurn = p;
		return players.add(p);
	}
	
	boolean remove(Player p) {
		if(currentTurn == p) changeTurn();
		return players.remove(p);
	}
	
	ArrayList<Move> analyze(int diff) {
		ArrayList<Move> strategy = new ArrayList<>();
		if(diff == 1 || diff == 2) {
			ArrayList<Move> best = new ArrayList<>();
			int mCost, minCost = Integer.MAX_VALUE;
			for(Move m : legalMoves.values()) {
				mCost = m.calculateBasicCost(diff);
				if(mCost < minCost) {
					minCost = mCost;
					best.clear();
					best.add(m);
				} else if(mCost == minCost) {
					best.add(m);
				}
			}
			int b = best.size();
			if(b > 1) {
				if(seed == NO_SEED) {
					strategy.add(best.get(new Random().nextInt(b)));
				} else {
					strategy.add(best.get(new Random(seed).nextInt(b)));
				}
			} else {
				strategy.add(best.get(0));
			}
			if(minCost > 0 && !isEndGame) isEndGame = true;
		} else if(diff == 3 || diff == 4) {
			ArrayList<Chain> openChains = new ArrayList<>();
			//if every move is in chains
			if(isEndGame) {
				//divide the chains into two lists--open and closed
				ArrayList<Chain> closedChains = new ArrayList<>();
				for(Chain c : activeChains) {
					if(c.isOpen()) {
						openChains.add(c);
					} else {
						closedChains.add(c);
					}
				}
				//sort the lists by chain size in ascending order
				Comparator<Chain> comp = new Comparator<Chain>() {
					@Override
					public int compare(Chain c1, Chain c2) {
						return c1.getNumMoves() - c2.getNumMoves();
					}
				};
				Collections.sort(openChains, comp);
				Collections.sort(closedChains, comp);
				Chain bestClosed = null;
				if(!closedChains.isEmpty()) {
					bestClosed = closedChains.remove(0);
				}
				//if all chains are closed
				if(openChains.isEmpty()) {
					if(bestClosed == null) throw new IllegalStateException();
					//open the smallest chain that is not a cycle
					while(bestClosed.isCycle()) {
						if(closedChains.isEmpty()) break;
						bestClosed = closedChains.remove(0);
					}
					strategy.add(bestClosed.getMembers().get(0));
				//if any chains are open
				} else {
					if(diff == 3) {
						//if Lv 3, take every open chain
						for(Chain c : openChains) {
							for(Move m : c.getMembers()) {
								strategy.add(m);
							}
						}
					} else if(diff == 4) {
						int numOpen = 0;
						for(Chain c: openChains) {
							numOpen += c.getNumSpaces();
						}
						//if there are only open chains left, or if there are more than two players,
						//or if the open chains only have one space each, or if the smallest closed
						//chain will only leave the player with 2 or fewer spaces, it is not worth it
						//to let the next player have anything
						if(bestClosed == null || players.size() > 2 ||
							numOpen == openChains.size() || bestClosed.getNumSpaces() < 2) {
							//take everything
							for(Chain c1: openChains) {
								for(Move m : c1.getMembers()) {
									strategy.add(m);
								}
							}
						//if the smallest closed is a cycle chain, test if the chain has a
						//chainBuilder--if it does not, take everything
						} else if(bestClosed.isCycle()) {
							boolean take = true;
							for(Move m : bestClosed.getMembers()) {
								if(mergers.contains(m)) {
									take = false;
									break;
								}
							}
							if(take) {
								for(Chain c1: openChains) {
									for(Move m : c1.getMembers()) {
										strategy.add(m);
									}
								}
							} else {
								//make all moves but the second to last (leaving two spaces)
								ArrayList<Move> moves = new ArrayList<>();
								for(Chain c1: openChains) {
									for(Move m : c1.getMembers()) {
										moves.add(m);
									}
								}
								for(int i = 0; i < moves.size(); i++) {
									if(i != moves.size() - 2) {
										strategy.add(moves.get(i));
									}
								}
							}
						} else {
							//sort by chain size
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
									strategy.add(moves.get(i));
								}
							}
						}
					}
				}
			} else {
				//find the open chains
				for(Chain c : activeChains) {
					if(c.isOpen()) {
						openChains.add(c);
					}
				}
				//if there are any chains open, take every one of them
				if(!openChains.isEmpty()) {
					for(Chain open : openChains) {
						for(Move m : open.getMembers()) {
							strategy.add(m);
						}
					}
				} else {
					//return a random nonchain move
					int i;
					if(seed == NO_SEED) {
						i = new Random().nextInt(nonChains.size());
					} else {
						i = new Random(seed).nextInt(nonChains.size());
					}
					int j = 0;
					for(Move m : nonChains) {
						if(i == j++) {
							strategy.add(m);
						}
					}
				}
			}
		}
		return strategy;
	}

	int getRows() { return rows; }
	
	int getCols() { return cols; }
	
	int getSpacesLeft() { return spacesLeft; }
	
	Player getTurn() { return this.currentTurn; }
	
	ArrayList<Player> getPlayers() { return this.players; }
	
	boolean isEndGame() { return this.isEndGame; }
	
	void beginEndGame() { isEndGame = true; }
	
	boolean isActive() { return spacesLeft != 0; }
	
	HashSet<Chain> getActiveChains() { return activeChains; }
	
	HashSet<Space> getEndSpaces() { return endSpaces; }
	
	HashSet<Move> getMergers() { return mergers; }
	
	HashSet<Move> getBlockers() { return blockers; }
	
	@Override
	public String toString() {
		boolean debug = seed != NO_SEED;
		ArrayList<String> gamestate = new ArrayList<>();
		String topline, midline1, midline2, midline3;
		GameIterator iter = getIterator();
		int index, width = cols;
		char ch = 'A';
		topline = "  " + Character.toString(ch++);
		for(int c = 0; c < cols; c++) {
			topline += "       " + Character.toString(ch++);
		}
		gamestate.add(topline);
		int i = 0;
		while(iter.hasNextSpace()) {
			index = 0;
			//far left side
			topline = Character.toString((char)(++i+48)) + " +";
			Move left = iter.nextMove();
			if(left.isAvailable()) {
				if(debug && left.hasChain()) {
					String cha = Character.toString(left.getChain().getIndex());
					midline1 = "   "; midline2 = "  " + cha; midline3 = "   ";
				} else {
					midline1 = "   "; midline2 = "   "; midline3 = "   ";
				}
			} else {
				if(left.equals(mostRecent)) {
					midline1 = "  /"; midline2 = "  /"; midline3 = "  /";	
				} else {
					midline1 = "  |"; midline2 = "  |"; midline3 = "  |";
				}
			}
			//rest of row
			while(index++ < width) {
				Space s = iter.nextSpace();
				Move top = iter.nextMove();
				Move right = iter.nextMove();
				if(!s.isFull()) {
					midline1 += "       ";
					midline2 += "       ";
					midline3 += "       ";
				} else {
					String sch = Character.toString(s.getMark()) + " ";
					midline1 += " " + sch + sch + sch; midline2 += " " + sch + sch + sch;
					midline3 += " " + sch + sch + sch;
				}
				if(top.isAvailable()) {
					if(debug && top.hasChain()) {
						String cha = Character.toString(top.getChain().getIndex());
						topline += "   " + cha + "   +";
					} else {
						topline += "       +";
					}
				} else {
					if(top == mostRecent) {
						topline += "///////+";
					} else {
						topline += "-------+";
					}
				}
				if(right.isAvailable()) {
					if(debug && right.hasChain()) {
						String cha = Character.toString(right.getChain().getIndex());
						midline1 += " "; midline2 += cha; midline3 += " ";
					} else {
						midline1 += " "; midline2 += " "; midline3 += " ";
					}
				} else {
					if(right == mostRecent) {
						midline1 += "/"; midline2 += "/"; midline3 += "/";
					} else {
						midline1 += "|"; midline2 += "|"; midline3 += "|";
					}
				}
			}
			gamestate.add(topline); gamestate.add(midline1); 
			gamestate.add(midline2); gamestate.add(midline3);
		}
		String bottomline = Character.toString((char)(i+49)) + " +";
		while(iter.hasNextMove()) {
			Move bottom = iter.nextMove();
			if(bottom.isAvailable()) {
				if(debug && bottom.hasChain()) {
					String cha = Character.toString(bottom.getChain().getIndex());
					bottomline += "   " + cha + "   +";
				} else {
					bottomline += "       +";
				}
			} else {
				if(bottom == mostRecent) {
					bottomline += "///////+";	
				} else {
					bottomline += "-------+";
				}
			}
		}
		gamestate.add(bottomline);
		String out = "\n";
		for(String line : gamestate) {
			out += line + "\n";
		}
		return out;
	}

	private void constructMoves() {
		int index = rows*(cols+1) + cols*(rows+1) - 1;
		String name = parse(rows, cols - 1, rows, cols);
		nonmove = new Move("NONMOVE", -1, null);
		Move next = new Move(name, index, nonmove);
		legalMoves.put(name, next);
		nonChains.add(next);
		Move last = null;
		while(next != null) {
			last = next;
			index--;
			int p1r = name.charAt(1) - 49, p1c = name.charAt(0) - 65,
					p2r = name.charAt(5) - 49, p2c = name.charAt(4) - 65;
			if(index == -1) {
				//have arrived at very first move, method is done
				break;
			}
			//middle move
			//if on bottom edge
			if(p1r == rows && p2r == rows) {
				//if at start of bottom edge
				if(p1c == 0) {
					//move to end of next row
					//with last vertical move
					name = parse(rows - 1, cols, rows, cols);
				//if not at the end
				} else {
					//shift along
					name = parse(rows, p1c - 1, rows, p2c - 1);
				}
			//if not on the bottom edge
			} else {
				//if vertical move
				if(p1r != p2r) {
					//if first move in the row
					if(p1c == 0) {
						//start the next row up
						//with its last vertical move
						name = parse(p1r - 1, cols, p2r - 1, cols);
					//if not the first
					} else {
						//make the previous horizontal move
						name = parse(p1r, p1c - 1, p2r - 1, p2c);
					}
				//if horizontal move
				} else {
					//shift to the previous vertical move
					name = parse(p1r, p1c, p2r + 1, p2c - 1);
				}
			}
			next = new Move(name, index, next);
			legalMoves.put(name, next);
			nonChains.add(next);
		}
		nonmove.setNext(last);
	}
	
	private void constructSpaces() {
		int index = rows * cols - 1;
		String name = (rows - 1) + ", " + (cols - 1);
		nonspace = new Space(-1, null);
		Space next = new Space(index, nonspace);
		Space last = null;
		while(next != null) {
			last = next;
			index--;
			int row = name.charAt(0) - 48,
				col = name.charAt(3) - 48;
			if(index == -1) {
				//first space, method is done
				break;
			}
			//middle spaces
			col--;
			if(col == -1) {
				row--;
				col = cols - 1;
			}
			if(row == -1) row = 0;
			name = "" + row + ", " + col;
			next = new Space(index, next);
		}
		nonspace.setNext(last);
	}
	
	private String parse(int p1r, int p1c, int p2r, int p2c) {
		char r1 = (char)(p1r + 48 + 1), r2 = (char)(p2r + 48 + 1);
		char c1 = (char)(p1c + 65), c2 = (char)(p2c + 65);
		return  Character.toString(c1) + Character.toString(r1) + ", " +
				Character.toString(c2) + Character.toString(r2);
	}
	
	private void connectMovesToSpaces() {
		GameIterator iter = getIterator();
		GameIterator shift = getIterator();
		for(int i = 0; i < cols; i++) {
			shift.nextMove();
			shift.nextMove();
		}
		shift.nextMove();
		//next move on shift is bottom of first space
		while(iter.hasNextSpace()) {
			Space s = iter.nextSpace();
			Move left = iter.nextMove();
			Move top = iter.nextMove();
			Move right = iter.nextMove();
			if(!shift.isOnLastRow()) shift.nextMove();
			Move bottom = shift.nextMove();
			//move to the next bottom
			if(!shift.isOnLastRow()) shift.nextMove();
			left.setRight(s); s.add(left);
			top.setRight(s); s.add(top);
			right.setLeft(s); s.add(right);
			bottom.setLeft(s); s.add(bottom);
			for(int i = 0; i < cols - 1; i++) {
				s = iter.nextSpace();
				left = right;
				top = iter.nextMove();
				right = iter.nextMove();
				bottom = shift.nextMove();
				if(!shift.isOnLastRow()) shift.nextMove();
				left.setRight(s); s.add(left);
				top.setRight(s); s.add(top);
				right.setLeft(s); s.add(right);
				bottom.setLeft(s); s.add(bottom);
			}
		}
	}
	
	private void changeTurn() {
		int index = players.indexOf(currentTurn) + 1;
		if(index == players.size()) index = 0;
		currentTurn = players.get(index);
	}
	
	private void buildChain(Space s) {
		Chain c;
		ArrayList<Move> moves = s.getUnmadeMoves();
		Move m1 = moves.get(0), m2 = moves.get(1);
		if(!m1.hasChain() && !m2.hasChain()) {
			//if both moves are nonChains, construct a new chain
			c = new Chain(m1, m2);
			m1.setChain(c); m2.setChain(c);
			nonChains.remove(m1); nonChains.remove(m2);
			activeChains.add(c);
		} else if(m1.hasChain() && !m2.hasChain()) {
			//if only m2 is nonChain, add m2 to m1's chain
			c = m1.getChain();
			if(m1 == c.getLastMove()) {
				c.putAtEnd(m2);
			} else {
				c.putAtBeginning(m2);
			}
			m2.setChain(c);
			nonChains.remove(m2);
		} else if(!m1.hasChain() && m2.hasChain()) {
			//if only m1 is nonChain, add m1 to m2's chain
			c = m2.getChain();
			if(m2 == c.getLastMove()) {
				c.putAtEnd(m1);
			} else {
				c.putAtBeginning(m1);
			}
			m1.setChain(c);
			nonChains.remove(m1);
		} else {
			//if both are chains
			c = m1.getChain();
			Chain d = m2.getChain();
			if(c == d) return;	//chains are already one
			if(c.getLastMove() == m1 && d.getLastMove() != m2) {	
				//if c is facing forward and d is facing forward,
				//let c absorb d
				c.absorb(d);
				for(Move m : d.getMembers()) {
					m.setChain(c);
				}
				activeChains.remove(d);
			} else if(c.getLastMove() != m1 && d.getLastMove() == m2) {
				//if c is facing backward and d is facing backward,
				//let d absorb c
				d.absorb(c);
				for(Move m : c.getMembers()) {
					m.setChain(d);
				}
				activeChains.remove(c);
			} else if(c.getLastMove() == m1 && d.getLastMove() == m2){
				//if they are facing toward each other, invert d,
				//and let c absorb d
				d.invert();
				c.absorb(d);
				for(Move m : d.getMembers()) {
					m.setChain(c);
				}
				activeChains.remove(d);
			} else {
				//if they are facing away from each other, invert c and
				//let c absorb d
				c.invert();
				c.absorb(d);
				for(Move m : d.getMembers()) {
					m.setChain(c);
				}
				activeChains.remove(d);
			}
		}
		//re-index the chains starting from 1
		char index = 48;
		for(Chain e : activeChains) {
			e.setIndex(++index);
		}
	}
	
	private void updateEndSpaces() {
		endSpaces.clear();
		sharedEndSpaces.clear();
		ArrayList<Space> ends;
		for(Chain c : activeChains) {
			ends = c.getEnds();
			for(Space s : ends) {
				if(!endSpaces.add(s)) {
					sharedEndSpaces.add(s);
				}
			}
		}
		updateMetaMoves();
	}
	
	private void updateMetaMoves() {
		mergers.clear();
		blockers.clear();
		for(Space s : endSpaces) {
			if(s.getRank() == 1) {
				ArrayList<Move> moves = s.getUnmadeMoves();
				Move m1 = moves.get(0); Chain c1 = m1.getChain();
				Move m2 = moves.get(1); Chain c2 = m2.getChain();
				Move m3 = moves.get(2); Chain c3 = m3.getChain();
				if(c2 != null && c3 != null) {
					if(m2.getChain() != m3.getChain()) {
						mergers.add(m1);
					} else {
						blockers.add(m1);
					}
				}
				if(c1 != null && c3 != null) {
					if(m1.getChain() != m3.getChain()) {
						mergers.add(m2);
					} else {
						blockers.add(m2);
					}
				}
				if(c1 != null && c2 != null) {
					if(m1.getChain() != m2.getChain()) {
						mergers.add(m3);
					} else {
						blockers.add(m3);
					}
				}
			}
		}
	}
	
}
