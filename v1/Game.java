package jtjudge.Boxes.v1;

import java.util.ArrayList;

/**
 * A Game object represents the state of a game session turn after turn. A game contains
 * the following data: 1) the grid of all spaces on the board 2) the number of rows and
 * columns in that grid 3) the grid of all corner points on the board 4) the number of 
 * unfilled spaces 5) the set of players 6) the index for whose turn it is 7) set of the 
 * lines drawn between points and 8) set of all possible moves a player could make at a 
 * given turn.
 * 
 * A game is constructed with a given amount of rows and columns, and initializes every 
 * space in the game board grid as well as the points in the corner point grid. Players
 * reference these points when drawing a line. When a line is drawn, the spaces to
 * its left and right (top and bottom if line is horizontal) increase in rank. A Line at
 * the edge of the board has only one space to either its left or right. Drawn lines--
 * lines that have been actively placed in the game--are held in a list so that no line 
 * can be drawn twice.
 * 
 * When a space increased to rank 4--when it is boxed in--it is marked with the filling
 * player's signature, the player's score is incremented, and the total size is 
 * decreased by one. When the total size is zero, the game is over.
 * 
 * The set of all possible moves is referenced by CPU players--each turn, the player
 * evaluates each one of the possible moves and makes the one that is most effective.
 * After a move is made, either by a CPU or by a human, it is removed from the set and,
 * if no space was filled, the turn index is advanced one spot to the next player.
 * 
 * The method getState() returns a String representation of the board to be printed to 
 * the console.
 */
public class Game {
	
	
	
	//FIELDS
	
	
	
		/**
		 * The dimensions of the grid of Spaces.
		 */
		private final int rows, cols;
		
		/**
		 * The grid of all spaces on the board.
		 */
		private Space[][] spaces;
		
		/**
		 * The grid of all corner points on the board.
		 */
		private Point[][] points;
		
		/**
		 * Number of unfilled spaces left in the game.
		 */
		private int spacesLeft;
		
		private Player currentTurn;
		
		//for use by high-level CPU players
		private boolean isEndGame;
		
		private Move mostRecent;
	
	
	
	//CONSTRUCTOR
	
	
	
		/**
		 * Constructs a new Game with the given amount of rows and columns
		 * of spaces.
		 * @param rows
		 *  rows of spaces
		 * @param cols
		 *  columns of spaces
		 */
		public Game(int rows, int cols) {
			if(rows < 1 || rows > 8 || cols < 1 || cols > 25) {
				throw new IllegalArgumentException();
			}
			this.rows = rows;
			this.cols = cols;
			this.spacesLeft = rows*cols;
			this.isEndGame = false;
			//initialize all spaces
			this.spaces = new Space[cols][rows];
			for(int i = 0; i < rows; i++) {
				for(int j = 0; j < cols; j++) {
					this.spaces[j][i] = new Space(i, j);
				}
			}
			//initialize all points
			this.points = new Point[cols + 1][rows + 1];
			for(int i = 0; i < rows + 1; i++) {
				for(int j = 0; j < cols + 1; j++) {
					Space[] conn = getSpaces(i, j);
					this.points[j][i] = new Point(i, j, conn);
				}
			}
			//initialize all possible moves
			for(int i = 0; i < rows; i++) {
				for(int j = 0; j < cols; j++) {
					Move.constructMove(points[j][i], points[j+1][i], this);
					Move.constructMove(points[j][i], points[j][i+1], this);
				}
				Move.constructMove(points[cols][i], points[cols][i+1], this);
			}
			for(int k = 0; k < cols; k++) {
				Move.constructMove(points[k][rows], points[k+1][rows], this);
			}
		}

	
	
	//METHODS
	
	
	
		/**
		 * Executes the given move, changing turns if no space is filled.
		 * @param m
		 *  Move to be executed
		 */
		public void makeMove(Move m) {
			//Move is already validated
			int num = m.execute();
			mostRecent = m;
			this.spacesLeft -= num;
			if(num == 0) {
				this.currentTurn = Player.changeTurn();
			}
		}
		
		public void beginEndGame() {
			isEndGame = true;
		}
		
		public void setFirstPlayer(Player p) {
			this.currentTurn = p;
		}
	
		/** 
		 * Returns a String representation of the current state of this game.
		 *  Key method for displaying graphics through the console.
		 *  @return
		 *   set of Strings representing the game board
		 */
		public ArrayList<String> getGameState() {
			
			/* 
			 * outline
			 * 
			 * for each row of spaces
			 * 		if a is connected to c
			 * 			draw line from a to c				a		b
			 * 			if a is connected to b
			 * 				draw line from a to b			c		d
			 * 			if space is full
			 * 				draw player marks in space
			 * 			if b is connected to d
			 * 				draw line from b to d
			 * 	for every space in the last row
			 * 		if c is connected to d
			 * 			draw line from c to d
			 */
			
			ArrayList<String> gameState = new ArrayList<>();
			
			//the top four lines in every row
			String topline = ""; String midline1 = ""; 
			String midline2 = ""; String midline3 = "";
			
			//the very last line on the grid
			String verybottomline = "";
			
			//make the top row of letters for the grid ( A      B      C     ...)
			char ch = 'A';
			String verytopline = "  " + Character.toString(ch++);
			for(int c = 0; c < cols; c++) {
				verytopline += "       " + Character.toString(ch++);
			}
			gameState.add(verytopline);
	
			//go through every row in the grid
			int i = 0;
			for(i = 0; i < rows; i++) {
				
				//add the numbering on the side of the grid
				topline = Character.toString((char)(i+49)) + " +";
				
				//add the far left side of the row
				Point a = points[0][i]; Point c = points[0][i+1];
				Move m = a.getConnectingMove(c);
				if(!m.isAvailable()) {
					if(m.equals(mostRecent)) {
						midline1 = "  /"; midline2 = "  /"; midline3 = "  /";	
					} else {
						midline1 = "  |"; midline2 = "  |"; midline3 = "  |";
					}
				} else {
					//can only have a single chain at this point
					if(m.getChains().size() == 1) {
						String cha = Character.toString(m.getChains().get(0).getID());
						midline1 = "   "; midline2 = "  " + cha; midline3 = "   ";
					} else {
						midline1 = "   "; midline2 = "   "; midline3 = "   ";
					}
				}
				
				//construct the rest of the row
				for(int j = 0; j < cols; j++) {
					a = points[j][i]; Point b = points[j+1][i]; Point d = points[j+1][i+1];
					m = a.getConnectingMove(b);
					if(!m.isAvailable()) {
						if(m.equals(mostRecent)) {
							topline += "///////+";
						} else {
							topline += "-------+";
						}
					} else {
						if(m.getChains().size() == 1) {
							String cha = Character.toString(m.getChains().get(0).getID());
							topline += "   " + cha + "   +";
						} else {
							topline += "       +";
						}
					}
					if(a.getBottomRight().isFull()) {
						String sch = Character.toString(a.getBottomRight().getPlayer().getMark()) + " ";
						midline1 += " " + sch + sch + sch; midline2 += " " + sch + sch + sch;
						midline3 += " " + sch + sch + sch;
					} else {
						midline1 += "       "; midline2 += "       "; midline3 += "       ";
					}
					m = b.getConnectingMove(d);
					if(!m.isAvailable()) {
						if(m.equals(mostRecent)) {
							midline1 += "/"; midline2 += "/"; midline3 += "/";
						} else {
							midline1 += "|"; midline2 += "|"; midline3 += "|";
						}
					} else {
						if(m.getChains().size() == 1) {
							String cha = Character.toString(m.getChains().get(0).getID());
							midline1 += " "; midline2 += cha; midline3 += " ";
						} else {
							midline1 += " "; midline2 += " "; midline3 += " ";
						}
					}
				}
				
				//add the new row
				gameState.add(topline); gameState.add(midline1);
				gameState.add(midline2); gameState.add(midline3);
				
			}
			
			//add the last line
			verybottomline += Character.toString((char)(i+49)) + " +";
			for(int k = 0; k < cols; k++) {
				Point c = points[k][rows];
				Point d = points[k + 1][rows];
				Move m1 = c.getConnectingMove(d);
				if(!m1.isAvailable()) {
					if(m1.equals(mostRecent)) {
						verybottomline += "///////+";	
					} else {
						verybottomline += "-------+";
					}
				} else {
					if(m1.getChains().size() == 1) {
						String cha = Character.toString(m1.getChains().get(0).getID());
						verybottomline += "   " + cha + "   +";
					} else {
						verybottomline += "       +";
					}
				}
			}
			gameState.add(verybottomline);
			
			//return the list of lines
			return gameState;
			
		}
	
	
	
	//GETTERS
	
	
	
		public int getRows() { return rows; }
		
		public int getCols() { return cols; }
		
		public Space[][] getSpaces() { return this.spaces; }
		
		public Point[][] getPoints() { return this.points; }
		
		public Player getTurn() { return this.currentTurn; }
		
		public int getSpacesLeft() { return spacesLeft; }
		
		public boolean isEndGame() { return this.isEndGame; }
		
		public boolean isActive() { return spacesLeft != 0; }
	
	
	
	//HELPERS
	
	
	
		/**
		 * Given a pair of indices representing a Point, finds the indices of all connected
		 * Spaces and returns those Spaces from the grid.
		 * @param i
		 *  row index of Point
		 * @param j
		 *  column index of Point
		 * @return
		 *  the Spaces connected to the Point
		 */
		private Space[] getSpaces(int i, int j) {
			
			//spaces connected to a point
			Space topright, topleft, bottomleft, bottomright;
			int tlr = i - 1; int tlc = j - 1;	//the indices of the top left space
			int trr = i - 1; int trc = j;		//top right space
			int blr = i;	int blc = j - 1;	//bottom left space
			int brr = i; int brc = j;			//bottom right space
			
			//check if out of bounds
			if(tlr == -1 || tlc == -1 || tlr > rows - 1 || tlc > cols - 1) {
				topleft = null;
			} else {
				topleft = this.spaces[tlc][tlr];
			}
			if(trr == -1 || trc == -1 || trr > rows - 1 || trc > cols - 1) {
				topright = null;
			} else {
				topright = this.spaces[trc][trr];
			}
			if(blr == -1 || blc == -1 || blr > rows - 1 || blc > cols - 1) {
				bottomleft = null;
			} else {
				bottomleft = this.spaces[blc][blr];
			}
			if(brr == -1 || brc == -1 || brr > rows - 1 || brc > cols - 1) {
				bottomright = null;
			} else {
				bottomright = this.spaces[brc][brr];
			}
			return new Space[] { topleft, topright, bottomleft, bottomright };
		}
	
	
	
	//OVERRIDDEN
	
	
	
		@Override
		public String toString() {
			return  "Size: " + rows + " x " + cols +
					"\nTurn: " + currentTurn.getName() +
					"\nBoxes remaining: " + spacesLeft;
		}
	
	
	
}