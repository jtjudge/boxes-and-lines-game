package jtjudge.Boxes.v1;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;

public final class WindowGameRunner extends Canvas implements Runnable {

	private static final long serialVersionUID = 1L;
	private static final String NAME = "BOXES AND LINES";
	
	private static final int
	W_RATIO = 16,
	H_RATIO = 9,
	F_WIDTH = 200,
	F_HEIGHT = H_RATIO * F_WIDTH / W_RATIO,
	FONT_SIZE = 6,
	SCALE = 6;
	
	private static final Color
	LINE = Color.WHITE,
	CURSOR = Color.GREEN,
	POINT = Color.GRAY,
	CHAIN = Color.RED, 
	STRATEGY = Color.CYAN,
	TEXT = Color.WHITE;
	
	private Game game;
	private Move cursor;
	private JFrame frame;
	private InputManager in;
	private BufferedImage image;
	
	private int spaceSize, pointSize;
	private boolean
	running,
	portraitMode,
	mouseOnExit, 
	mouseOnReset;
	
	public WindowGameRunner() {
		running = false;
		
		setMinimumSize(new Dimension(F_WIDTH * SCALE, F_HEIGHT * SCALE));
		setMaximumSize(new Dimension(F_WIDTH * SCALE, F_HEIGHT * SCALE));
		setPreferredSize(new Dimension(F_WIDTH * SCALE, F_HEIGHT * SCALE));
		
		frame = new JFrame(NAME);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(this, BorderLayout.CENTER);
		frame.pack();
		
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		in = new InputManager(this);
		
		image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
	}
	
	public void setUp(int rows, int cols, ArrayList<Player> players) {
		game = new Game(rows, cols);
		for(Player p : players) {
			game.add(p);
		}
		autoScale();
	}
	
	public synchronized void start() {
		running = true;
		new Thread(this).start();
	}
	
	public synchronized void stop() {
		running = false;
	}
	
	@Override
	public void run() {
		final long targetFrameTime = 1_000_000_000 / 60;
		long frameTime = 0, thisSecond = 0, now, dt;
		int ticks = 0, frames = 0;
		while(running) {
			now = System.nanoTime();
			tick();
			ticks++;
			dt = System.nanoTime() - now;
			frameTime += dt;
			thisSecond += dt;
			if(frameTime >= targetFrameTime) {
				now = System.nanoTime();
				render();
				frames++;
				dt = System.nanoTime() - now;
				thisSecond += dt;
				frameTime = 0;
			}
			if(thisSecond >= 1_000_000_000) {
				System.out.println(ticks + ", " + frames);
				ticks = 0;
				frames = 0;
				thisSecond = 0;
			}
		}
	}
	
	private void tick() {
		if(!game.isFinished()) {
			Player player = game.getTurn();
			if(player.isCPU()) {
				Move m = player.thinkOfMove(game, player.getDiff());
				game.make(m, player);
			} else if(in.isKeyPressed("ENTER")) {
				Move m = player.thinkOfMove(game, 4);
				game.make(m, player);
			} else {
				cursor = getMoveAtCoords(in.getX() - spaceSize, in.getY() - spaceSize);
				if(cursor == null) {
					if(!in.isMousePressed("RightClick") && player.hasStrategy()) {
						Move m = player.doStrategy();
						if(m.isAvailable()) game.make(m, player);
					}
					in.isMouseClicked("LeftClick"); //de-clicks the mouse if clicked
				} else {
					if(in.isMouseClicked("LeftClick")) {
						if(in.isMousePressed("RightClick")) {
							player.addToStrategy(cursor);
						} else {
							game.make(cursor, player);
						}
					}
					if(in.isMousePressed("LeftClick") &&
						in.isMousePressed("RightClick")) {
						player.addToStrategy(cursor);
					}
				}
			}
		}
		mouseOnExit();
		mouseOnReset();
		if(mouseOnExit && in.isMousePressed("LeftClick")) {
			System.exit(0);
		}
		if(mouseOnReset && in.isMousePressed("LeftClick")) {
			ArrayList<Player> players = game.getPlayers();
			int rows = game.getRows(), cols = game.getCols();
			game = new Game(rows, cols);
			for(Player p: players) {
				p.resetScore();
				game.add(p);
			}
		}
	}
	
	private void mouseOnExit() {
		int x = in.getX(), y = in.getY();
		if(portraitMode) {
			int left = 2 * F_WIDTH * SCALE / 3, right = left + (12 * SCALE),
					top = F_HEIGHT * SCALE / 3, bottom = top - (FONT_SIZE * SCALE);
			mouseOnExit = (x > left && x < right && y < top && y > bottom);
		} else {
			int left = F_WIDTH * SCALE / 6, right = left + (12 * SCALE),
					top = 5 * F_HEIGHT * SCALE / 6, bottom = top - (FONT_SIZE * SCALE);
			mouseOnExit = (x > left && x < right && y < top && y > bottom);
		}
	}
	
	private void mouseOnReset() {
		int x = in.getX(), y = in.getY();
		if(portraitMode) {
			int left = 3 * F_WIDTH * SCALE / 4, right = left + (15 * SCALE),
					top = F_HEIGHT * SCALE / 3, bottom = top - (FONT_SIZE * SCALE);
			mouseOnReset = (x > left && x < right && y < top && y > bottom);
		} else {
			int left = F_WIDTH * SCALE / 4, right = left + (15 * SCALE),
					top = 5 * F_HEIGHT * SCALE / 6, bottom = top - (FONT_SIZE * SCALE);
			mouseOnReset = (x > left && x < right && y < top && y > bottom);
		}
	}
	
	private void autoScale() {
		int height, width;
		if(game.getCols() / game.getRows() > W_RATIO / H_RATIO) {
			height = (5 * (F_HEIGHT * SCALE) / 2) / (6 * (game.getRows() + 2));
			width = (5 * F_WIDTH * SCALE) / (6 * (game.getCols() + 1));
			portraitMode = false;
		} else {
			height = (5 * F_HEIGHT * SCALE) / (6 * (game.getRows() + 2));
			width = (5 * (F_WIDTH * SCALE) / 2) / (6 * (game.getCols() + 1));
			portraitMode = true;
		}
		spaceSize = width <= height ? width : height;
		pointSize = spaceSize / 5 == 0 ? 1 : spaceSize / 5;
	}
	
	private Move getMoveAtCoords(int x, int y) {
		if(x < 0 || y < 0) return null;
		int row = y / (spaceSize + pointSize);
		int col = x / (spaceSize + pointSize);
		int innerX = x % (spaceSize + pointSize);
		int innerY = y % (spaceSize + pointSize);
		if(innerX > pointSize && innerY < pointSize) {
			String p1 = Character.toString((char)(col + 65)) +
					Character.toString((char)(row + 48 + 1));
			String p2 = Character.toString((char)(col + 65 + 1)) + 
					Character.toString((char)(row + 48 + 1));
			return game.retrieve(p1 + ", " + p2);
		} else if(innerX < pointSize && innerY > pointSize) {
			String p1 = Character.toString((char)(col + 65)) +
					Character.toString((char)(row + 48 + 1));
			String p2 = Character.toString((char)(col + 65)) + 
					Character.toString((char)(row + 48 + 2));
			return game.retrieve(p1 + ", " + p2);
		} else {
			return null;
		}
	}
	
	private void render() {
		BufferStrategy bs = getBufferStrategy();
		if(bs == null) {
			createBufferStrategy(3);
			return;
		}
		
		Graphics g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, null);
		drawGame(g);
		drawText(g);
		g.dispose();
		bs.show();
	}
	
	private void drawGame(Graphics g) {
		GameIterator iter = game.getIterator();
		int vShift = 0;
		for( ; iter.hasNextSpace(); vShift++) {
			Move left = iter.nextMove();
			if(!left.isAvailable()) {
				g.setColor(LINE);
				drawLeft(g, vShift);
			} else if(left == cursor) {
				g.setColor(CURSOR);
				drawLeft(g, vShift);
			} else if(left.isStrategized()) {
				g.setColor(STRATEGY);
				drawLeft(g, vShift);
			} else if(left.hasChain()) {
				if(cursor != null && left.getChain() == cursor.getChain()) {
					g.setColor(CHAIN);
					drawLeft(g, vShift);
				}
			}
			int hShift = 0;
			for( ; hShift < game.getCols(); hShift++) {
				Move top = iter.nextMove();
				Move right = iter.nextMove();
				Space s = iter.nextSpace();
				g.setColor(POINT);
				drawPoint(g, hShift, vShift);
				if(!top.isAvailable()) {
					g.setColor(LINE);
					drawTop(g, hShift, vShift);
				} else if(top == cursor) {
					g.setColor(CURSOR);
					drawTop(g, hShift, vShift);
				} else if(top.isStrategized()) {
					g.setColor(STRATEGY);
					drawTop(g, hShift, vShift);		
				} else if(top.hasChain()) {
					if(cursor != null && top.getChain() == cursor.getChain()) {
						g.setColor(CHAIN);
						drawTop(g, hShift, vShift);
					}
				}
				if(!right.isAvailable()) {
					g.setColor(LINE);
					drawRight(g, hShift, vShift);
				} else if(right == cursor) {
					g.setColor(CURSOR);
					drawRight(g, hShift, vShift);
				} else if(right.isStrategized()) {
					g.setColor(STRATEGY);
					drawRight(g, hShift, vShift);
				} else if(right.hasChain()) {
					if(cursor != null && right.getChain() == cursor.getChain()) {
						g.setColor(CHAIN);
						drawRight(g, hShift, vShift);
					}
				}
				if(s.isFull()) {
					g.setColor(s.getColor());
					drawSpace(g, hShift, vShift);
				}
			}
			g.setColor(POINT);
			drawPoint(g, hShift, vShift);
		}
		int hShift = 0;
		for( ; iter.hasNextMove(); hShift++) {
			Move bottom = iter.nextMove();
			g.setColor(POINT);
			drawPoint(g, hShift, vShift);
			if(!bottom.isAvailable()) {
				g.setColor(LINE);
				drawBottom(g, hShift, vShift);
			} else if(bottom == cursor) {
				g.setColor(CURSOR);
				drawBottom(g, hShift, vShift);
			} else if(bottom.isStrategized()) {
				g.setColor(STRATEGY);
				drawBottom(g, hShift, vShift);
			} else if(bottom.hasChain()) {
				if(cursor != null && bottom.getChain() == cursor.getChain()) {
					g.setColor(CHAIN);
					drawBottom(g, hShift, vShift);
				}
			}
		}
		g.setColor(POINT);
		drawPoint(g, hShift, vShift);
	}
	
	private void drawLeft(Graphics g, int vShift) {
		g.fillRect(spaceSize, (vShift * (spaceSize + pointSize)) +
				spaceSize + pointSize,
				pointSize, spaceSize);
	}
	
	private void drawRight(Graphics g, int hShift, int vShift) {
		g.fillRect(spaceSize + ((hShift + 1) * (spaceSize + pointSize)), 
				(vShift * (spaceSize + pointSize)) + spaceSize + pointSize,
				pointSize, spaceSize);
	}
	
	private void drawTop(Graphics g, int hShift, int vShift) {
		g.fillRect((hShift * (spaceSize + pointSize)) + spaceSize + pointSize, 
				(vShift * (spaceSize + pointSize)) + spaceSize,
				spaceSize, pointSize);
	}
	
	private void drawBottom(Graphics g, int hShift, int vShift) {
		g.fillRect((hShift * (spaceSize + pointSize)) + spaceSize + pointSize, 
				(vShift * (spaceSize + pointSize)) + spaceSize, spaceSize, pointSize);
	}
	
	private void drawSpace(Graphics g, int hShift, int vShift) {
		g.fillRect((hShift * (spaceSize + pointSize)) + spaceSize + pointSize,
				(vShift * (spaceSize + pointSize)) + spaceSize + pointSize,
				spaceSize, spaceSize);
	}
	
	private void drawPoint(Graphics g, int hShift, int vShift) {
		g.fillRect((hShift * (spaceSize + pointSize)) + spaceSize,
				(vShift * (spaceSize + pointSize)) + spaceSize,
				pointSize, pointSize);
	}
	
	private void drawText(Graphics g) {
		g.setFont(new Font("Trebuchet MS", Font.BOLD, FONT_SIZE * SCALE));
		if(portraitMode) {
			g.setColor(TEXT);
			if(mouseOnExit) g.setColor(CURSOR);
			g.drawString("Exit", 2 * F_WIDTH * SCALE / 3, F_HEIGHT * SCALE / 3);
			g.setColor(TEXT);
			if(mouseOnReset) g.setColor(CURSOR);
			g.drawString("Reset", 3 * F_WIDTH * SCALE / 4, F_HEIGHT * SCALE / 3);
			g.setColor(game.getTurn().getColor());
			g.drawString("Turn: " + game.getTurn().getName(),
					2 * F_WIDTH * SCALE / 3, F_HEIGHT * SCALE / 4);
			int i = 0;
			g.setColor(Color.WHITE);
			g.drawString("Scores: ", 2 * F_WIDTH * SCALE / 3,
					(F_HEIGHT * SCALE / 2) + ++i * (FONT_SIZE * SCALE));
			for(Player p : game.getPlayers()) {
				g.setColor(p.getColor());
				g.drawString(p.getName() + ": " + p.getScore() + " (" + p.getWins() + " wins)",
						2 * F_WIDTH * SCALE / 3, (F_HEIGHT * SCALE / 2) +
							++i * (FONT_SIZE * SCALE));
			}
		} else {
			g.setColor(TEXT);
			if(mouseOnExit) g.setColor(CURSOR);
			g.drawString("Exit", F_WIDTH * SCALE / 6, 5 * F_HEIGHT * SCALE / 6);
			g.setColor(TEXT);
			if(mouseOnReset) g.setColor(CURSOR);
			g.drawString("Reset", F_WIDTH * SCALE / 4, 5 * F_HEIGHT * SCALE / 6);
			g.setColor(game.getTurn().getColor());
			g.drawString("Turn: " + game.getTurn().getName(), F_WIDTH * SCALE / 6,
					3 * F_HEIGHT * SCALE / 4);
			g.setColor(Color.WHITE);
			g.drawString("Scores: ", F_WIDTH * SCALE / 2,
					(3 * F_HEIGHT * SCALE / 4));
			int i = -1;
			for(Player p : game.getPlayers()) {
				g.setColor(p.getColor());
				g.drawString(p.getName() + ": " + p.getScore() + " (" + p.getWins() + " wins)",
						2 * F_WIDTH * SCALE / 3, (2 * F_HEIGHT * SCALE / 3) +
							++i * (FONT_SIZE * SCALE));
			}
		}
	}

	private static class InputManager
		implements KeyListener, MouseListener, MouseMotionListener {
		
		private HashMap<Integer, Key> keyCodesToKeys;
		private HashMap<String, Key> namesToKeys;
		private HashMap<Integer, Click> mouseCodesToClicks;
		private HashMap<String, Click> namesToClicks;
		
		private static final Mouse MOUSE = new Mouse();
		
		private InputManager(Canvas c) {
			c.addKeyListener(this);
			c.addMouseListener(this);
			c.addMouseMotionListener(this);
			keyCodesToKeys = new HashMap<>();
			namesToKeys = new HashMap<>();
			mouseCodesToClicks = new HashMap<>();
			namesToClicks = new HashMap<>();
			addKeyMapping("ENTER", KeyEvent.VK_ENTER);
			addMouseMapping("LeftClick", MouseEvent.BUTTON1);
			addMouseMapping("RightClick", MouseEvent.BUTTON3);
		}
		
		private void addKeyMapping(String s, int keyCode) {
			Key k = new Key(s, keyCode);
			keyCodesToKeys.put(keyCode, k);
			namesToKeys.put(s, k);
		}
		
		private void addMouseMapping(String s, int mouseCode) {
			Click c = new Click(s, mouseCode);
			mouseCodesToClicks.put(mouseCode, c);
			namesToClicks.put(s, c);
		}
		
		private boolean isKeyPressed(String s) {
			Key k = namesToKeys.get(s);
			if(k == null) return false;
			return k.isPressed();
		}
		
		private boolean isMouseClicked(String s) {
			Click c = namesToClicks.get(s);
			if(c == null) return false;
			if(c.isClicked()) {
				c.setClicked(false);
				return true;
			}
			return false;
		}
		
		private boolean isMousePressed(String s) {
			Click c = namesToClicks.get(s);
			if(c == null) return false;
			return c.isPressed();
		}
		
		private int getX() {
			return MOUSE.getX();
		}
		
		private int getY() {
			return MOUSE.getY();
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			Key k = keyCodesToKeys.get(e.getKeyCode());
			if(k == null) return;
			k.setPressed(true);
		}
		
		@Override
		public void keyReleased(KeyEvent e) {
			Key k = keyCodesToKeys.get(e.getKeyCode());
			if(k == null) return;
			k.setPressed(false);
		}
		
		@Override
		public void keyTyped(KeyEvent e) {
			//not used	
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			Click c = mouseCodesToClicks.get(e.getButton());
			if(c == null) return;
			c.setClicked(true);
		}
	
		@Override
		public void mousePressed(MouseEvent e) {
			Click c = mouseCodesToClicks.get(e.getButton());
			if(c == null) return;
			c.setPressed(true);
		}
	
		@Override
		public void mouseReleased(MouseEvent e) {
			Click c = mouseCodesToClicks.get(e.getButton());
			if(c == null) return;
			c.setPressed(false);
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
			MOUSE.setInScreen(true);
		}
	
		@Override
		public void mouseExited(MouseEvent e) {
			MOUSE.setInScreen(false);
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			MOUSE.setDragged(true);
			MOUSE.setX(e.getX());
			MOUSE.setY(e.getY());
		}
	
		@Override
		public void mouseMoved(MouseEvent e) {
			MOUSE.setDragged(false);
			MOUSE.setX(e.getX());
			MOUSE.setY(e.getY());
		}
	
	}
	
	private static class Key {
		
		private String name;
		private int keyCode, pressCount;
		private boolean pressed;
		
		private Key(String name, int keyCode) {
			this.name = name;
			this.keyCode = keyCode;
			this.pressed = false;
			this.pressCount = 0;
		}
		
		private boolean isPressed() {
			return pressed;
		}
		
		private void setPressed(boolean set) {
			pressed = set;
			if(pressed) {
				pressCount++;
			}
		}
	}
	
	private static class Click {
		
		private String name;
		private int mouseCode, clickCount, pressCount;
		private boolean clicked, pressed;
		
		private Click(String name, int mouseCode) {
			this.name = name;
			this.mouseCode = mouseCode;
			this.clicked = false;
			this.clickCount = 0;
			this.pressed = false;
			this.pressCount = 0;
		}
	
		private boolean isClicked() {
			return clicked;
		}
		
		private void setClicked(boolean set) {
			clicked = set;
			if(clicked) {
				clickCount++;
			}
		}
		
		private boolean isPressed() {
			return pressed;
		}
		
		private void setPressed(boolean set) {
			pressed = set;
			if(pressed) {
				pressCount++;
			}
		}
		
	}
	
	private static class Mouse {
		
		private int x, y;
		private boolean dragged, inScreen;
		
		private Mouse() {}
		
		private void setDragged(boolean setting) {
			dragged = setting;
		}
		
		private void setInScreen(boolean setting) {
			inScreen = setting;
		}
		
		private void setX(int x) { this.x = x; }
		
		private int getX() { return x; }
		
		private void setY(int y) { this.y = y; }
		
		private int getY() { return y; }
	}

}