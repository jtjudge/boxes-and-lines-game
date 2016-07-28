package jtjudge.Boxes.v1;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ConsoleGameRunner {

	private Game game;
	private Scanner in;
	private int delay, lateDelay;
	private boolean debugMode;

	public ConsoleGameRunner(Scanner in, int delay, int lateDelay) {
		this.in = in;
		this.delay = delay;
		this.lateDelay = lateDelay;
		this.debugMode = false;
	}	
	
	public ConsoleGameRunner(Game game, Scanner in, int delay, int lateDelay) {
		this.game = game;
		this.in = in;
		this.delay = delay;
		this.lateDelay = lateDelay;
		this.debugMode = true;
	}
	
	//set up the game with user input
	public void setUp() {
		if(this.game != null) throw new IllegalStateException();
		//GAME SETUP
		System.out.println("Game setup.");
		System.out.println("Enter rows and columns: ");
		String rows = in.next();
		String cols = in.next();
		while(true) {
			while( rows.length() != 1 || (cols.length() != 1  && cols.length() != 2)
					|| !Character.isDigit(rows.charAt(0))
					|| !Character.isDigit(cols.charAt(0))   ) {
				System.out.println("Invalid format. Try again: ");
				rows = in.next();
				cols = in.next();
			}
			int r = Integer.parseInt(rows);
			int c = Integer.parseInt(cols);
			try {
				game = new Game(r, c);
				break;
			} catch (IllegalArgumentException e) {
				System.out.println("Dimensions out of bounds. Try again: ");
				rows = in.next();
				cols = in.next();
			}
		}
		//PLAYER SETUP
		int i = 1;
		System.out.println("Player setup.");
		while(true) {
			while(true) {
				System.out.println("[Player " + i + "] Name: ");
				in.nextLine();
				String name = in.nextLine();
				System.out.println("[Player " + i + "] Mark: ");
				String resp = in.next();
				while(resp.length() != 1) {
					System.out.println("[Player " + i + "] Invalid. Enter a single ASCII character: ");
					resp = in.next();
				}
				char mark = resp.charAt(0);
				System.out.println("[Player " + i + "] CPU? (Y / N): ");
				resp = in.next();
				while(resp.length() != 1 || (resp.charAt(0) != 'y' && resp.charAt(0) != 'Y'
						&& resp.charAt(0) != 'n' && resp.charAt(0) != 'N')) {
					System.out.println("[Player " + i + "] Invalid. Enter 'Y' for yes or 'N' for no: ");
					resp = in.next();
				}
				resp = resp.toUpperCase();
				if(resp.charAt(0) == 'Y') {
					System.out.println("[Player " + i + "] Level (1 - 5): ");
					resp = in.next();
					while(resp.length() != 1 || (resp.charAt(0) != '1' && resp.charAt(0) != '2'
							&& resp.charAt(0) != '3' && resp.charAt(0) != '4' && resp.charAt(0) != '5')) {
						System.out.println("[Player " + i + "] Invalid. Enter '1', '2', '3', '4', or '5': ");
						resp = in.next();
					}
					int level = Integer.parseInt(resp);
					try {
						if(!game.add(Player.constructComputerPlayer(name, mark, level)))
							throw new IllegalStateException();
						System.out.println("Added " + name + ".");
						break;
					} catch (IllegalArgumentException e) {
						System.out.println("Name must contain at least one character.");
					} catch (IllegalStateException e) {
						System.out.println(name + " already exists. Enter a different name.");
					}
				} else {
					try {
						if(!game.add(Player.constructHumanPlayer(name, mark)))
							throw new IllegalStateException();
						System.out.println("Added " + name + ".");
						break;
					} catch (IllegalArgumentException e) {
						System.out.println("Name must contain at least one character.");
					} catch (IllegalStateException e) {
						System.out.println(name + " already exists. Enter a different name.");
					}
				}
			}
			i++;
			System.out.println("Player " + i + "? (Y / N): ");
			String resp = in.next();
			while(resp.length() != 1 || (resp.charAt(0) != 'y' && resp.charAt(0) != 'Y'
					&& resp.charAt(0) != 'n' && resp.charAt(0) != 'N')) {
				System.out.println("Invalid. Enter 'Y' for yes or 'N' for no: ");
				resp = in.next();
			}
			resp = resp.toUpperCase();
			if(resp.charAt(0) == 'N') break;
		}
		System.out.println("Game constructed.");
		try {
			TimeUnit.MILLISECONDS.sleep(1500);
		} catch(InterruptedException e) {
		}
	}
	
	//Creates a game with r rows, c cols, with two given players. Assumes all
	//parameters are valid.
	public void setUpDebug(int r, int c, Player p1, Player p2, int seed) {
		if(this.game != null) throw new IllegalStateException();
		debugMode = true;
		game = new Game(r, c, seed);
		game.add(p1);
		game.add(p2);
		System.out.println("Game constructed.");
		try {
			TimeUnit.MILLISECONDS.sleep(1500);
		} catch(InterruptedException e) {
		}
	}
	
	//run the game
	public void runGame() {
		while(game.isActive()) {
			System.out.print(game);
			if(debugMode) {
				System.out.println("CHAINS:");
				for(Chain c: game.getActiveChains()) {
					System.out.println(c);
				}
				System.out.println("END SPACES:");
				for(Space s: game.getEndSpaces()) {
					System.out.println(s);
				}
				System.out.println("MERGERS:");
				for(Move m: game.getMergers()) {
					System.out.println(m);
				}
				System.out.println("BLOCKERS:");
				for(Move m: game.getBlockers()) {
					System.out.println(m);
				}
			}
			System.out.println();
			System.out.println();
			System.out.println();
			runTurn();
		}
		System.out.print(game);
		System.out.println("[GAME OVER]");
		ArrayList<Player> winners = new ArrayList<>();
		int maxScore = 0;
		for(Player p : game.getPlayers()) {
			int score = p.getScore();
			System.out.println(p.getName() + ": " + score);
			if(score >= maxScore) {
				if(score == maxScore) {
					winners.add(p);
				} else {
					winners.clear();
					winners.add(p);
					maxScore = p.getScore();
				}
			}
		}
		if(winners.size() > 1) {
			System.out.print("Tied:");
			for(Player p : winners) {
				System.out.print(" " + p.getName());
			}
			System.out.println();
		} else {
			System.out.println(winners.get(0).getName() + " wins.");
		}
	}
	
	//runs the game until the end with no console output
	public void runGameUntilEnd() {	
		while(!game.isEndGame()) {
			runTurn();
		}
	}
	
	private void runTurn() {
		Player player = game.getTurn();
		if(debugMode && game.isEndGame()) {
			System.out.println("[LATE GAME]");
		}
		System.out.println("[Turn] " + player.getName());
		System.out.println("Enter pair of coordinates: ");
		if(player.isCPU()) {
			try {
				Move m = player.thinkOfMove(game);
				System.out.println(m);
				TimeUnit.MILLISECONDS.sleep(delay);
				if(game.isEndGame()) TimeUnit.MILLISECONDS.sleep(lateDelay);
				game.make(m, player);
			} catch (InterruptedException e) {
			}
		} else {
			while(true) {
				String a = in.next();
				String b = in.next();
				try {
					if( a.length() != 2 || b.length() != 2 ||
						!Character.isLetter(a.charAt(0)) || !Character.isDigit(a.charAt(1)) ||
						!Character.isLetter(b.charAt(0)) || !Character.isDigit(b.charAt(1))) {
						throw new IllegalStateException();
					}
					String name = a.substring(0, 1).toUpperCase() + a.substring(1,2) + ", " +
									b.substring(0, 1).toUpperCase() + b.substring(1, 2);
					Move m = game.retrieve(name);
					if(m == null) {
						name = b.substring(0, 1).toUpperCase() + b.substring(1, 2) + ", " +
								a.substring(0, 1).toUpperCase() + a.substring(1, 2);
						m = game.retrieve(name);
					}
					if(m == null) throw new IllegalArgumentException();
					game.make(m, player);
					break;
				} catch (IllegalArgumentException e) {
					System.out.println("Invalid move.");
					System.out.println("[Turn] " + game.getTurn().getName());
					System.out.println("Enter pair of coordinates: ");
				} catch (IllegalStateException e) {
					System.out.println("Invalid format. Example: 'A1' ENTER 'B1' ENTER."
							+ " Letters are case insensitive.");
					System.out.println("[Turn] " + game.getTurn().getName());
					System.out.println("Enter pair of coordinates: ");
				}
			}
		}
	}
	
}

