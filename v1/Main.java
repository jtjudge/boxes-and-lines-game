package jtjudge.Boxes.v1;

import java.util.Scanner;
import java.util.Random;

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
		
		Player c1 = Player.constructComputerPlayer("CPU 1", '1', 4);
		Player c2 = Player.constructComputerPlayer("CPU 2", '2', 4);
		Player p1 = Player.constructHumanPlayer("Player 2", '2');
		WindowGameRunner runner = new WindowGameRunner();
		runner.setUp(4, 4, c1, p1);
		runner.start();
	}
	
}
