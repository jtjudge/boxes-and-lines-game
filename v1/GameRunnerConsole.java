package jtjudge.Boxes.v1;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class GameRunnerConsole {
	
	
	public static void main (String[] args) {
		GameRunnerConsole runner = new GameRunnerConsole();
		in = new Scanner(System.in);
		runner.quickSetUp(5,5,2,2);
//		runner.setUp();
		runner.runGame();
		in.close();
	}
	
	
	private Game game;
	private static Scanner in;
	
	
	public GameRunnerConsole() {
		//empty constructor
	}	
		
	
	//set up the game with user input
	public void setUp() {
		//GAME SETUP
		System.out.println("Game setup.");
		System.out.println("Enter rows and columns: ");
		String rows = in.next();
		String cols = in.next();
		while(true) {
			while( rows.length() != 1 || (cols.length() != 1  && cols.length() != 2)
					|| !Character.isDigit(rows.charAt(0))
					|| !Character.isDigit(cols.charAt(0))   ) {
				System.out.println("Invalid rows and columns. Try again: ");
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
				System.out.println("[Player " + i + "] Level (1 - 4): ");
				resp = in.next();
				while(resp.length() != 1 || (resp.charAt(0) != '1' && resp.charAt(0) != '2'
						&& resp.charAt(0) != '3' && resp.charAt(0) != '4')) {
					System.out.println("[Player " + i + "] Invalid. Enter '1', '2', '3', or '4': ");
					resp = in.next();
				}
				int level = Integer.parseInt(resp);
				game.addPlayer(new Player(game, name, mark, level));
				System.out.println("Added " + name + ".");
			} else {
				game.addPlayer(new Player(game, name, mark));
				System.out.println("Added " + name + ".");
			}
			i++;
			System.out.println("Player " + i + "? (Y / N): ");
			resp = in.next();
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
	
	
	//creates a game with r rows, c cols, p players, where n players are CPU
	//assumes all values are valid
	public void quickSetUp(int r, int c, int p, int n) {
		game = new Game(r, c);
		for(int i = 0; i < n; i++) {
			game.addPlayer(new Player(game, "CPU " + (i+1), (char)(i+49), 4));			//  <<-------------------- SET CPU LEVEL
		}
		for(int i = 0; i < p - n; i++) {
			game.addPlayer(new Player(game, "Player " + (char)(i+65), (char)(i+65)));
		}
		System.out.println("Game constructed.");
		try {
			TimeUnit.MILLISECONDS.sleep(1500);
		} catch(InterruptedException e) {
		}
	}
	
	
	//run the game
	public void runGame() {	
		ArrayList<String> lines;
		while(game.isActive()) {
			lines = game.getGameState();
			for(String s: lines) {
				System.out.println(s);
			}
			for(Chain c: game.getChains()) {
				System.out.println(c.toString());
			}
			System.out.println();
			System.out.println();
			System.out.println();
			Player player = game.getTurn();
			System.out.println("[Turn] " + player.getName());
			System.out.println("Enter pair of vertices: ");
			if(player.isCPU()) {
				try {
					Move m = player.thinkOfMove();
					System.out.println(m.v1);
					System.out.println(m.v2);
					if(game.isLateGame) {
						TimeUnit.MILLISECONDS.sleep(1500);		//  <<<-------------------- SET TIME DELAY HERE
					}
					game.makeMove(m);
				} catch (InterruptedException e) {
					System.out.println("CPU failure");
					break;
				}
			} else {
				while(true) {
					String a = in.next();
					String b = in.next();
					try {
						Move m = new Move(a, b, game.getTurn());
						game.makeMove(m);
						break;
					} catch (IllegalArgumentException e) {
						System.out.println("Invalid move.");
						System.out.println("[Turn] " + game.getTurn().getName());
						System.out.println("Enter pair of coordinates: ");
					}
				}
			}
		}
		lines = game.getGameState();
		for(String s: lines) {
			System.out.println(s);
		}
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
	

}
