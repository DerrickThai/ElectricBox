import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * Main - the main frame of our program that keeps track of different game
 * states, handles the menu and menu bar, and switches panels in and out
 * 
 * @author Derrick Thai and Riddle Li
 * @version v6.2Final, Last Updated: June 16, 2014
 */
public class Main extends JFrame implements ActionListener
{
	// Serial Version ID to remove error
	private static final long serialVersionUID = 1L;

	/* Constants */
	// Game States
	private static final int MENU = 0;
	private static final int EDITOR = 1;
	private static final int LEVEL = 2;

	/* Variables */
	public static int noOfLevels;
	public static int state;

	// Menus and menu items
	private JMenuItem mainMenuOption, exitOption, instructionsMenuItem,
			aboutMenuItem;
	private JComboBox<String> levelSelect;
	private String[] levels;

	// Panels
	public static Level level;
	public static LevelEditor levelEditor;
	private Menu menu;
	private JPanel panelTop, panelBot, panelLeft, panelRight;

	/**
	 * Constructs the main program frame
	 * 
	 * @throws FileNotFoundException if a file is not found
	 */
	public Main() throws FileNotFoundException
	{
		// Title and preferences
		setTitle("Electric Box Beta");
		setResizable(false);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		// Corner icon image
		setIconImage(Toolkit.getDefaultToolkit().getImage(
				"./images/lightBulbOn.png"));

		// Put the frame in the centre of the monitor near the top
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(screen.width / 2 - 510, 10);

		// Load the menu and menu bar
		loadMenuBar();
		loadMainMenu();
	}

	/**
	 * Loads up the menu bar
	 */
	private void loadMenuBar()
	{
		// Initialize the Game Menu options and their hot keys
		mainMenuOption = new JMenuItem("Main Menu");
		mainMenuOption.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
				InputEvent.CTRL_MASK));
		mainMenuOption.addActionListener(this);

		exitOption = new JMenuItem("Exit");
		exitOption.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
				InputEvent.CTRL_MASK));
		exitOption.addActionListener(this);

		// Set up the Help Menu
		instructionsMenuItem = new JMenuItem("Instructions");
		instructionsMenuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_I, InputEvent.CTRL_MASK));
		instructionsMenuItem.addActionListener(this);

		aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				InputEvent.CTRL_MASK));
		aboutMenuItem.addActionListener(this);

		// Add each menu item to its menu
		JMenu gameMenu = new JMenu("Game");
		gameMenu.add(mainMenuOption);
		gameMenu.add(exitOption);

		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(instructionsMenuItem);
		helpMenu.add(aboutMenuItem);

		// Add the menus to the menu bar
		JMenuBar mainMenu = new JMenuBar();
		mainMenu.add(gameMenu);
		mainMenu.add(helpMenu);

		// Add the menu bar to the frame
		setJMenuBar(mainMenu);
	}

	/**
	 * Loads the main menu
	 */
	private void loadMainMenu()
	{
		// Update the state to menu
		state = MENU;

		// Add the menu panel to the frame
		menu = new Menu();
		add(menu, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	/**
	 * Loads a level chosen by the user
	 * 
	 * @throws FileNotFoundException if the file is not found
	 */
	private void loadLevel() throws FileNotFoundException
	{
		// Create a JOptionPane and combine it with a JComboBox that lists all
		// of the levels to select a level
		checkNoOfLevels();
		levels = new String[noOfLevels];

		for (int level = 0; level < levels.length; level++)
			levels[level] = "Level " + (level + 1);

		levelSelect = new JComboBox<String>(levels);
		levelSelect.setSelectedIndex(0);
		int reply = JOptionPane.showConfirmDialog(null, levelSelect,
				"Select a level", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);

		// If OK was selected
		if (reply == JOptionPane.OK_OPTION)
		{
			// Update the state
			state = LEVEL;

			// Add the level to the frame
			remove(menu);
			loadBorderPanels();
			level = new Level(levelSelect.getSelectedIndex() + 1);
			add(level, BorderLayout.CENTER);
			level.requestFocus();
			revalidate();
			repaint();
		}
	}

	/**
	 * Loads the level editor
	 */
	private void loadEditor()
	{
		// Update the state to editor
		state = EDITOR;

		// Add the editor panel to the frame
		remove(menu);
		loadBorderPanels();
		levelEditor = new LevelEditor();
		add(levelEditor, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	/**
	 * Loads the border panels needed for level editor and levels to make screen
	 * size the same as main menu
	 */
	private void loadBorderPanels()
	{
		// Light blue colour
		Color borderColour = new Color(124, 189, 221);

		// Top panel
		Dimension dimensionX = new Dimension(1020, 60);

		panelTop = new JPanel(new BorderLayout());

		// Add the inventory title
		ImageIcon inventoryTitle = new ImageIcon("./images/inventoryTitle.png");
		JLabel label = new JLabel("", inventoryTitle, JLabel.CENTER);
		panelTop.add(label, BorderLayout.EAST);

		panelTop.setPreferredSize(dimensionX);
		panelTop.setBackground(borderColour);

		add(panelTop, BorderLayout.NORTH);

		// Bottom panel
		panelBot = new JPanel();
		panelBot.setPreferredSize(dimensionX);
		panelBot.setBackground(borderColour);
		add(panelBot, BorderLayout.SOUTH);

		// Left and right panels
		Dimension dimensionY = new Dimension(30, 600);

		panelLeft = new JPanel();
		panelLeft.setPreferredSize(dimensionY);
		panelLeft.setBackground(borderColour);
		add(panelLeft, BorderLayout.WEST);

		panelRight = new JPanel();
		panelRight.setPreferredSize(dimensionY);
		panelRight.setBackground(borderColour);
		add(panelRight, BorderLayout.EAST);
	}

	/**
	 * Removes the border panel for the main menu
	 */
	private void removeBorderPanels()
	{
		remove(panelTop);
		remove(panelBot);
		remove(panelLeft);
		remove(panelRight);
	}

	/**
	 * Shows the instructions as a popup dialog
	 */
	private void showInstructions()
	{
		JOptionPane
				.showMessageDialog(
						this,
						"Welcome to ELECTRIC BOX BETA.\n\n"
								+ "The objective of this game is to conduct electricity from a source box to the target box."
								+ "\nWires placed on the board will conduct the electricity, but empty spaces will not."
								+ "\nThe inventory on the right contain items that convert electricity to other forms"
								+ "\nof energy and convert energy back to electricity."
								+ "\n\nUse these items by dragging them onto a square with wires on it. "
								+ "\nTurn on and off the source box by clicking on it or using the space bar."
								+ "\nYou pass the level when the target box turns on.",
						"Instructions",
						JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Checks the number of levels by counting ".op" files
	 */
	public static void checkNoOfLevels()
	{
		noOfLevels = 0;
		File directory = new File("./levels");
		for (File file : directory.listFiles())
			if (file.isFile() && file.getAbsolutePath().endsWith(".op"))
				noOfLevels++;
	}

	/**
	 * Responds to a menu bar events
	 * 
	 * @param event the event that triggered this method
	 */
	public void actionPerformed(ActionEvent event)
	{
		// Selected "Main Menu"
		if (event.getSource() == mainMenuOption)
		{
			// Remove the current panel and load the menu
			if (state == LEVEL)
			{
				remove(level);
				removeBorderPanels();
			}
			else if (state == EDITOR)
			{
				remove(levelEditor);
				removeBorderPanels();
			}

			loadMainMenu();
		}
		// Selected "Exit"
		else if (event.getSource() == exitOption)
			System.exit(0);

		// Selected "Rules"
		else if (event.getSource() == instructionsMenuItem)
			showInstructions();

		// Selected "About"
		else if (event.getSource() == aboutMenuItem)
			JOptionPane.showMessageDialog(this,
					"Programming and Menus: Derrick Thai\nGraphics and Tooltips: Riddle Li"
							+ "\n\u00a9 2014", "About Electric Box Beta",
					JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Starts up the main program
	 * 
	 * @param args an array of Strings
	 * @throws FileNotFoundException if a level file is not found
	 */
	public static void main(String[] args) throws FileNotFoundException
	{
		// Starts up the frame
		Main frame = new Main();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Menu - the main menu panel that works together with the Main class to
	 * alternate between different game screen and thus an inner class
	 * 
	 * @author Derrick Thai and Riddle Li
	 * @version v6.2Final, Last Updated: June 16, 2014
	 */
	private class Menu extends JPanel implements MouseListener,
			MouseMotionListener
	{
		// Serial Version ID to remove error
		private static final long serialVersionUID = 1L;

		/* Constants */
		// Dimension
		private final Dimension SIZE = new Dimension(1020, 720);

		// Button Indexes
		private final int BUTTON_PLAY = 0;
		private final int BUTTON_EDITOR = 1;
		private final int BUTTON_HELP = 2;
		private final int BUTTON_EXIT = 3;

		// Variables
		private Image menuImage;
		private Rectangle[] buttons;
		private Rectangle selectedRectangle;

		/**
		 * Constructs a new main menu
		 */
		private Menu()
		{
			// Add the listeners
			addMouseListener(this);
			addMouseMotionListener(this);

			// Panel preferences
			setPreferredSize(SIZE);
			setFocusable(true);
			requestFocusInWindow();

			loadMenu();
		}

		/**
		 * Loads the image and buttons of the menu
		 */
		private void loadMenu()
		{
			// Menu image
			menuImage = new ImageIcon("./images/mainMenu.png").getImage();

			// Buttons
			buttons = new Rectangle[4];
			buttons[BUTTON_PLAY] = new Rectangle(327, 286, 378, 139);
			buttons[BUTTON_EDITOR] = new Rectangle(88, 553, 300, 84);
			buttons[BUTTON_HELP] = new Rectangle(430, 553, 232, 84);
			buttons[BUTTON_EXIT] = new Rectangle(702, 553, 232, 84);
		}

		/**
		 * Paints the menu's drawing panel
		 * 
		 * @param g The Graphics context
		 */
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);

			// Grey colour for unselected button
			Color notSelected = new Color(162, 164, 176);
			g.setColor(notSelected);

			// Draw the buttons
			for (Rectangle button : buttons)
				g.fillRect(button.x, button.y, button.width, button.height);

			// Highlight hovered buttons blue
			if (selectedRectangle != null)
			{
				Color selected = new Color(124, 140, 222);
				g.setColor(selected);
				g.fillRect(selectedRectangle.x, selectedRectangle.y,
						selectedRectangle.width, selectedRectangle.height);
			}

			// Foreground image
			g.drawImage(menuImage, 0, 0, this);

		}

		/* Listener Events */
		/**
		 * Handles mouse movements
		 * 
		 * @param event information about the mouse moved event
		 */
		public void mouseMoved(MouseEvent event)
		{
			Point currentPoint = event.getPoint();

			// Change to hand cursor and highlight button if mouse is hovering
			for (Rectangle button : buttons)
				if (button.contains(currentPoint))
				{
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					selectedRectangle = button;
					repaint();
					return;
				}

			// If not hovering any button use default cursor and button colour
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			selectedRectangle = null;
			repaint();
		}

		/**
		 * Handles mouse presses
		 * 
		 * @param event information about the mouse pressed event
		 */
		public void mousePressed(MouseEvent event)
		{
			Point selectedPoint = event.getPoint();

			// Determine what button was pressed
			for (int button = 0; button < buttons.length; button++)
				if (buttons[button].contains(selectedPoint))
				{
					// Play: load level
					if (button == BUTTON_PLAY)
					{
						// Try to load a level
						try
						{
							loadLevel();
						}
						catch (FileNotFoundException e)
						{
							e.printStackTrace();
						}
					}
					// Editor: load editor
					else if (button == BUTTON_EDITOR)
						loadEditor();

					// Help: show instructions
					else if (button == BUTTON_HELP)
						showInstructions();

					// Exit: close window
					else if (button == BUTTON_EXIT)
						System.exit(0);

				}
		}

		// Unused Listeners
		public void mouseClicked(MouseEvent event)
		{
		}

		public void mouseEntered(MouseEvent event)
		{
		}

		public void mouseExited(MouseEvent event)
		{
		}

		public void mouseReleased(MouseEvent event)
		{
		}

		public void mouseDragged(MouseEvent event)
		{
		}

	}
}
