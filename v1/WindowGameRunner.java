package jtjudge.Boxes.v1;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.HashMap;

import javax.swing.JFrame;

public class WindowGameRunner extends Canvas implements Runnable {
	
	private Game game;
	private Move cursor;
	private boolean selected;
	
	private static final int WIDTH = 160;
	private static final int HEIGHT = WIDTH / 16 * 9;
	private static final int SCALE = 8;
	private static final String NAME = "Boxes and Lines";
	
	private int spaceSize, pointSize;
	
	private boolean running;
	private int tickCount;
	
	private JFrame frame;
	
	private BufferedImage image;
	private int[] pixels;
	
	private InputManager in;
	
	public WindowGameRunner() {
		game = new Game(8, 8);	//TODO
		Player c1 = Player.constructComputerPlayer("CPU 1", '1', 4);
		Player c2 = Player.constructComputerPlayer("CPU 2", '2', 4);
		game.add(c1); game.add(c2);
		pointSize = SCALE * 2;
		if(game.getRows() >= game.getCols()) {
			spaceSize = (HEIGHT * SCALE) / game.getRows() - (2 * pointSize);
		} else {
			spaceSize = (WIDTH * SCALE) / game.getCols() - (2 * pointSize);
		}
		in = new InputManager(this);
		running = false;
		tickCount = 0;
		setMinimumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		setMaximumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		
		frame = new JFrame(NAME);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(this, BorderLayout.CENTER);
		frame.pack();
		
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
	}
	
	public void setUp() {
		//TODO
	}
	
	public void autoSetUp() {
		//TODO
	}
	
	public synchronized void start() {
		running = true;
		new Thread(this).start();
	}
	
	public synchronized void stop() {
		running = false;
	}
	
	public void run() {
		long lastTime = System.nanoTime();
		double nsPerTick = 1000000000D / 60D;
		
		int ticks = 0;
		int frames = 0;
		
		long lastTimer = System.currentTimeMillis();
		double dt = 0;
		
		while(running) {
			long now = System.nanoTime();
			dt += (now - lastTime) / nsPerTick;
			lastTime = now;
			boolean shouldRender = true;;
			
			while(dt >= 1) {
				ticks++;
				tick();
				dt--;
				shouldRender = true;
			}
			
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(shouldRender) {
				frames++;
				render();
			}
			
			if(System.currentTimeMillis() - lastTimer > 1000) {
				lastTimer += 1000;
				System.out.println(ticks + " ticks, " + frames + " frames");
				frames = 0;
				ticks = 0;
			}
			
		}
	}
	
	public void tick() {
		tickCount++;
		for(int i = 0; i < pixels.length; i++) {
			pixels[i] = i * tickCount;
		}
		if(game.isActive()) {
			Player player = game.getTurn();
			if(player.isCPU()) {
				Move m = player.thinkOfMove(game);
				game.make(m, player);
			} else {
				cursor = getMoveAtCoords(in.getX(), in.getY());
				if(cursor != null && in.isMouseClicked("LeftClick")) {
					game.make(cursor, player);
				}
			}
		}
	}
	
	private Move getMoveAtCoords(int x, int y) {
		return null;
	}
	
	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if(bs == null) {
			createBufferStrategy(3);
			return;
		}
		
		Graphics g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
		drawGame(g);
		g.dispose();
		bs.show();
	}
	
	private void drawGame(Graphics g) {
		GameIterator iter = game.getIterator();
		g.fillRect(0, 0, pointSize, pointSize);
		int vShift = 0;
		for( ; iter.hasNextSpace(); vShift++) {
			Move left = iter.nextMove();
			g.setColor(Color.BLACK);
			if(!left.isAvailable()) {
				g.fillRect(0, vShift * (spaceSize + pointSize) + pointSize,
						pointSize, spaceSize);
			}
			int hShift = 0;
			for( ; hShift < game.getCols(); hShift++) {
				Move top = iter.nextMove();
				Move right = iter.nextMove();
				Space s = iter.nextSpace();
				g.setColor(Color.WHITE);
				g.fillRect(hShift * (spaceSize + pointSize), vShift *
						(spaceSize + pointSize), pointSize, pointSize);
				if(!top.isAvailable()) {
					g.setColor(Color.BLACK);
					g.fillRect(hShift * (spaceSize + pointSize) + pointSize, 
							vShift * (spaceSize + pointSize), spaceSize, pointSize);
				} else if(top == cursor) {
					g.setColor(Color.YELLOW);
					g.fillRect(hShift * (spaceSize + pointSize) + pointSize, 
							vShift * (spaceSize + pointSize), spaceSize, pointSize);
				}
				g.setColor(Color.BLACK);
				if(!right.isAvailable()) {
					g.fillRect((hShift + 1) * (spaceSize + pointSize), 
							vShift * (spaceSize + pointSize) + pointSize, pointSize,
							spaceSize);
				} else if(right == cursor) {
					g.setColor(Color.YELLOW);
					g.fillRect((hShift + 1) * (spaceSize + pointSize), 
							vShift * (spaceSize + pointSize) + pointSize, pointSize,
							spaceSize);
				}
				if(s.isFull()) {
					if(s.getMark() == '1') g.setColor(Color.BLUE);
					if(s.getMark() == '2') g.setColor(Color.RED);
					g.fillRect(hShift * (spaceSize + pointSize) + pointSize,
							vShift * (spaceSize + pointSize) + pointSize,
							spaceSize, spaceSize);
				}
			}
			g.setColor(Color.WHITE);
			g.fillRect(hShift * (spaceSize + pointSize), vShift *
					(spaceSize + pointSize), pointSize, pointSize);
		}
		int hShift = 0;
		for( ; iter.hasNextMove(); hShift++) {
			Move bottom = iter.nextMove();
			g.setColor(Color.WHITE);
			g.fillRect(hShift * (spaceSize + pointSize),
					vShift * (spaceSize + pointSize), pointSize, pointSize);
			g.setColor(Color.BLACK);
			if(!bottom.isAvailable()) {
				g.fillRect(hShift * (spaceSize + pointSize) + pointSize, 
						vShift * (spaceSize + pointSize), spaceSize, pointSize);
			}
		}

		g.fillRect(hShift * (spaceSize + pointSize),
				vShift * (spaceSize + pointSize), pointSize, pointSize);
	}
	
	private void drawLeft(Graphics g, int vShift) {
		g.fillRect(0, vShift * (spaceSize + pointSize) + pointSize,
				pointSize, spaceSize);
	}
	
	private void drawRight(Graphics g, int hShift, int vShift) {
		g.fillRect((hShift + 1) * (spaceSize + pointSize), 
				vShift * (spaceSize + pointSize) + pointSize, pointSize,
				spaceSize);
	}
	
	private void drawTop(Graphics g, int hShift, int vShift) {
		g.fillRect(hShift * (spaceSize + pointSize) + pointSize, 
				vShift * (spaceSize + pointSize), spaceSize, pointSize);
	}
	
	private void drawBottom(Graphics g, int hShift, int vShift) {
		g.fillRect(hShift * (spaceSize + pointSize) + pointSize, 
				vShift * (spaceSize + pointSize), spaceSize, pointSize);
	}
	
	private void drawSpace(Graphics g, int hShift, int vShift) {
		g.fillRect(hShift * (spaceSize + pointSize) + pointSize,
				vShift * (spaceSize + pointSize) + pointSize,
				spaceSize, spaceSize);
	}
	
	private void drawPoint(Graphics g, int hShift, int vShift) {
		g.fillRect(hShift * (spaceSize + pointSize), vShift *
				(spaceSize + pointSize), pointSize, pointSize);
	}

	private static class InputManager
		implements KeyListener, MouseListener, MouseMotionListener {

		private HashMap<Integer, Key> keyCodesToKeys;
		private HashMap<String, Key> namesToKeys;
		private HashMap<Integer, Click> mouseCodesToClicks;
		private HashMap<String, Click> namesToClicks;
		
		private static final Mouse MOUSE = new Mouse();

		InputManager(Canvas c) {
			c.addKeyListener(this);
			c.addMouseListener(this);
			c.addMouseMotionListener(this);
			keyCodesToKeys = new HashMap<>();
			namesToKeys = new HashMap<>();
			mouseCodesToClicks = new HashMap<>();
			namesToClicks = new HashMap<>();
			addKeyMapping("W", KeyEvent.VK_W);
			addMouseMapping("LeftClick", MouseEvent.BUTTON1);
			addMouseMapping("RightClick", MouseEvent.BUTTON3);
		}
		
		void addKeyMapping(String s, int keyCode) {
			Key k = new Key(s, keyCode);
			keyCodesToKeys.put(keyCode, k);
			namesToKeys.put(s, k);
		}
		
		void addMouseMapping(String s, int mouseCode) {
			Click c = new Click(s, mouseCode);
			mouseCodesToClicks.put(mouseCode, c);
			namesToClicks.put(s, c);
		}
		
		boolean isKeyPressed(String s) {
			Key k = namesToKeys.get(s);
			if(k == null) return false;
			return k.isPressed();
		}
		
		boolean isMouseClicked(String s) {
			Click c = namesToClicks.get(s);
			if(c == null) return false;
			if(c.isClicked()) {
				c.toggleClicked();
				return true;
			}
			return false;
		}
		
		boolean isMousePressed(String s) {
			Click c = namesToClicks.get(s);
			if(c == null) return false;
			return c.isPressed();
		}
		
		int getX() {
			return MOUSE.getX();
		}
		
		int getY() {
			return MOUSE.getY();
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			Key k = keyCodesToKeys.get(e.getKeyCode());
			if(k == null) return;
			k.togglePressed();
		}

		@Override
		public void keyReleased(KeyEvent e) {
			Key k = keyCodesToKeys.get(e.getKeyCode());
			if(k == null) return;
			k.togglePressed();
		}

		@Override
		public void keyTyped(KeyEvent e) {
			//not used	
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			Click c = mouseCodesToClicks.get(e.getButton());
			if(c == null) return;
			c.toggleClicked();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			Click c = mouseCodesToClicks.get(e.getButton());
			if(c == null) return;
			c.togglePressed();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			Click c = mouseCodesToClicks.get(e.getButton());
			if(c == null) return;
			c.togglePressed();
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
		
		Key(String name, int keyCode) {
			this.name = name;
			this.keyCode = keyCode;
			this.pressed = false;
			this.pressCount = 0;
		}
		
		boolean isPressed() {
			return pressed;
		}
		
		void togglePressed() {
			pressed = !pressed;
			if(pressed) {
				pressCount++;
			}
		}
	}
	
	private static class Click {
		
		private String name;
		private int mouseCode, clickCount, pressCount;
		private boolean clicked, pressed;
		
		Click(String name, int mouseCode) {
			this.name = name;
			this.mouseCode = mouseCode;
			this.clicked = false;
			this.clickCount = 0;
			this.pressed = false;
			this.pressCount = 0;
		}

		boolean isClicked() {
			return clicked;
		}
		
		void toggleClicked() {
			clicked = !clicked;
			if(clicked) {
				clickCount++;
			}
		}
		
		boolean isPressed() {
			return pressed;
		}
		
		void togglePressed() {
			pressed = !pressed;
			if(pressed) {
				pressCount++;
			}
		}
		
	}
	
	private static class Mouse {
		
		private int x, y;
		private boolean dragged, inScreen;
		
		Mouse() {}
		
		void setDragged(boolean setting) {
			dragged = setting;
		}
		
		void setInScreen(boolean setting) {
			inScreen = setting;
		}
		
		void setX(int x) { this.x = x; }
		
		int getX() { return x; }
		
		void setY(int y) { this.y = y; }
		
		int getY() { return y; }
	}

}