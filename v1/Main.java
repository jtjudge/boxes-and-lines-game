package jtjudge.Boxes.v1;

import java.awt.Color;
import java.util.ArrayList;

public class Main {

	public static void main (String[] args) {
	
//		Scanner in = new Scanner(System.in);
//		
////		ConsoleGameRunner runner = new ConsoleGameRunner(in, 50, 100);
////		runner.setUp();
////		runner.runGame();
//		
//		ConsoleGameRunner runner = new ConsoleGameRunner(in, 100, 100);
//		Player c1 = Player.constructComputerPlayer("CPU 1", '1', 4);
//		Player c2 = Player.constructComputerPlayer("CPU 2", '2', 1);
//		Player p1 = Player.constructHumanPlayer("Player 1", 'P');
//		runner.setUpDebug(4, 4, c1, p1, 15);
//		runner.runGame();
//		
////		Game game = new Game(5, 5);
////		game.add(c1); game.add(c2);
////		ConsoleGameRunner runner2 = new ConsoleGameRunner(game, in, 50, 100);
////		runner2.runGameUntilEnd();
////		game.remove(c2);
////		game.add(Player.constructHumanPlayer("Player 1", 'P'));
////	//	game.add(Player.constructHumanPlayer("Player 2", 'L'));
////		runner2.runGame();
//		
//		in.close();
		
		Player c1 = Player.constructComputerPlayer("CPU 1", '1', Color.RED, 1);
		Player c2 = Player.constructComputerPlayer("CPU 2", '2', Color.BLUE, 1);
		Player c3 = Player.constructComputerPlayer("CPU 3", '3', Color.GREEN, 1);
		Player c4 = Player.constructComputerPlayer("CPU 4", '4', Color.YELLOW, 1);
		
		Player p1 = Player.constructHumanPlayer("Player 2", '2', null);
		
		ArrayList<Player> players = new ArrayList<>();
		
		players.add(c1);
		
//		players.add(c2);
//		players.add(c3);
//		players.add(c4);
		
		players.add(p1);
		
		WindowGameRunner runner = new WindowGameRunner();
		runner.setUp(3, 3, players);
		runner.start();
	}
	
}
