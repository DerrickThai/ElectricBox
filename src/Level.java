import java.awt.*;

import javax.swing.*;

import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Level - the level panel that loads a level to be played from a ".op" file.
 * Also handles the manipulation of electricity and energies
 * 
 * @author Derrick Thai and Riddle Li
 * @version v6.2Final, Last Updated: June 16, 2014
 */
public class Level extends JPanel implements MouseListener,
		MouseMotionListener, KeyListener
{
	// Serial Version ID to remove error
	private static final long serialVersionUID = 1L;

	/* Constants */
	// Board Size
	private final int SQUARE_SIZE = 60;
	private final int NO_OF_ROWS = 10;
	private final int NO_OF_COLS = 16;
	private final int INVENTORY_COLS = 6;

	public final Dimension BOARD_SIZE = new Dimension(NO_OF_COLS * SQUARE_SIZE,
			NO_OF_ROWS * SQUARE_SIZE);

	// Timer Intervals
	private final int TIME_INTERVAL_LONG = 1000;
	private final int TIME_INTERVAL_SHORT = 250;

	/* Variables */
	private int levelNo;
	private boolean powerOn;

	// Board and items
	private Square[][] board;
	private int noOfItems;
	private int noOfWires;
	private Item[] items;
	private Wire[] wires;

	// Hovered and selected item
	Point lastPoint, firstPoint;

	private Item hoveredItem;
	private Item selectedItem;
	private Item sourceItem;
	private int selectedItemIndex;

	// Timers
	private Timer timerLong;
	private Timer timerShort;

	// Images
	Image imageBackground;
	Image[] imagePrompt;

	/**
	 * Constructs a new Level object
	 * 
	 * @throws FileNotFoundException if a file is not found
	 */
	public Level(int levelNo) throws FileNotFoundException
	{
		// Add the listeners
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);

		// Panel preferences
		setPreferredSize(BOARD_SIZE);
		setFocusable(true);
		requestFocusInWindow();

		// Load the level
		this.levelNo = levelNo;

		// Load squares and objects
		loadSquares();
		loadObjects();

		// Initialize timers
		timerLong = new Timer(TIME_INTERVAL_LONG, new TimerEventHandler());
		timerShort = new Timer(TIME_INTERVAL_SHORT, new TimerEventHandler());

		// Initialize images
		imageBackground = new ImageIcon("./images/levelBoard.png").getImage();
		imagePrompt = new Image[2];
		imagePrompt[0] = new ImageIcon("./images/startInfo.png").getImage();
		imagePrompt[1] = new ImageIcon("./images/offInfo.png").getImage();

		turnOffPower();
	}

	/**
	 * Initializes the square objects
	 */
	private void loadSquares()
	{
		board = new Square[NO_OF_ROWS][NO_OF_COLS];

		for (int row = 0; row < board.length; row++)
			for (int col = 0; col < board[row].length; col++)
				board[row][col] = new Square(col * SQUARE_SIZE, row
						* SQUARE_SIZE, SQUARE_SIZE);
	}

	/**
	 * Loads the level from the file
	 * 
	 * @throws FileNotFoundException if the file is not found
	 */
	private void loadObjects() throws FileNotFoundException
	{
		// Load the file and read the number of items and wires
		Scanner inFile = new Scanner(new File("./levels/level" + levelNo
				+ ".op"));
		noOfItems = inFile.nextInt();
		inFile.nextLine();
		noOfWires = inFile.nextInt();
		inFile.nextLine();

		// Add the item objects
		items = new Item[noOfItems];
		wires = new Wire[noOfWires];
		int itemNo = 0;
		int wireNo = 0;

		// Add each item to the board
		for (int row = 0; row < NO_OF_ROWS; row++)
			for (int col = 0; col < NO_OF_COLS; col++)
			{
				// Store the next String
				String readStr = inFile.next();

				// If the string is not a letter there must be an item
				if (!Level.isLetter(readStr))
				{
					// If the number is negative, there is a wire (check
					// the character because -0 is a possibility)
					if (readStr.charAt(0) == '-')
					{
						// Add the wire
						wires[wireNo++] = new Wire(col * SQUARE_SIZE, row
								* SQUARE_SIZE, this);
						board[row][col].addWire();
					}

					// Use the positive value of the integer to decide
					// the item type and add that item
					int readInt = Math.abs(Integer.parseInt(readStr));

					items[itemNo] = new Item(col * SQUARE_SIZE, row
							* SQUARE_SIZE, readInt, this);

					// Lock the item if it is not on the inventory
					if (items[itemNo].x < (NO_OF_COLS - INVENTORY_COLS)
							* SQUARE_SIZE)
						items[itemNo].lock();

					// Remember the source item
					if (readInt == Item.SOURCE)
						sourceItem = items[itemNo];

					board[row][col].addItem(itemNo++);

				}
				// If the string is a letter then there is no item
				else
				{
					char readCh = readStr.charAt(0);

					// 'X' is an disabled square
					if (readCh == 'X')
						board[row][col].disable();
					// 'W' is a wire by itself
					else if (readCh == 'W')
					{
						wires[wireNo++] = new Wire(col * SQUARE_SIZE, row
								* SQUARE_SIZE, this);
						board[row][col].addWire();
					}
					// 'O' is for a completely empty square but no code
				}

			}
		inFile.close();
	}

	/**
	 * Determines if the given String from the file is a letter
	 * 
	 * @param str the string to be checked
	 * @return Whether or not the given String is a letter
	 */
	public static boolean isLetter(String str)
	{
		// Look at all positions since we may be looking at a negative number
		// that has more than one character (because of the negative sign)
		for (int pos = 0; pos < str.length(); pos++)
			if (Character.isLetter(str.charAt(pos)))
				return true;
		return false;
	}

	/**
	 * Takes a x coordinate and converts it to a column number
	 * 
	 * @param x the x coordinate to be converted
	 * @return the column number that best fits the x coordinate
	 */
	private int xCoordToCol(int x)
	{
		return x / SQUARE_SIZE;
	}

	/**
	 * Takes a y coordinate and converts it to a row number
	 * 
	 * @param y the y coordinate to be converted
	 * @return the row number that best fits the y coordinate
	 */
	private int yCoordToRow(int y)
	{
		return y / SQUARE_SIZE;
	}

	/**
	 * Turns on the different types of energies such as light and radio
	 * according to the energy providing items Also turns the items on if there
	 * is electricity
	 */
	private void turnOnEnergy()
	{
		// Checks every square object on the game board for placed items
		for (int row = 0; row < NO_OF_ROWS; row++)
			for (int col = 0; col < NO_OF_COLS - INVENTORY_COLS; col++)
				if (board[row][col].getItemIndex() > Square.EMPTY)
					// Checks whether or not each square has electricity
					// then checks the type of item occupying each square
					if (board[row][col].returnEnergyState(Square.ELECTRICITY) == true)
					{
						// For light bulbs, turn it on and transmit light
						if (items[board[row][col].getItemIndex()].getItemType() == Item.LIGHT_BULB)
						{
							if (items[board[row][col].getItemIndex()]
									.getState() == Item.STATE_OFF)
								items[board[row][col].getItemIndex()]
										.switchState();
							transmitEnergy(Square.LIGHT, row, col);
						}
						// For solar panels, turn them on
						else if (items[board[row][col].getItemIndex()]
								.getItemType() == Item.SOLAR_PANEL)
						{
							if (items[board[row][col].getItemIndex()]
									.getState() == Item.STATE_OFF)
								items[board[row][col].getItemIndex()]
										.switchState();
						}
						// For Radio Senders, turn them on and transmit the
						// radio waves
						else if (items[board[row][col].getItemIndex()]
								.getItemType() == Item.RADIO)
						{
							if (items[board[row][col].getItemIndex()]
									.getState() == Item.STATE_OFF)
								items[board[row][col].getItemIndex()]
										.switchState();
							transmitEnergy(Square.RADIO, row, col);
						}
						// For Radio Dishes, turn them on
						else if (items[board[row][col].getItemIndex()]
								.getItemType() == Item.DISH)
						{
							if (items[board[row][col].getItemIndex()]
									.getState() == Item.STATE_OFF)
								items[board[row][col].getItemIndex()]
										.switchState();
						}
						// For Teleport Senders, turn them on and teleport any
						// items above it
						else if (items[board[row][col].getItemIndex()]
								.getItemType() == Item.TSENDER)
						{
							if (items[board[row][col].getItemIndex()]
									.getState() == Item.STATE_OFF)

								items[board[row][col].getItemIndex()]
										.switchState();
							teleport(row, col);
						}
						// For Teleport Receiver, turn them on
						else if (items[board[row][col].getItemIndex()]
								.getItemType() == Item.TRECEIVER)
						{
							if (items[board[row][col].getItemIndex()]
									.getState() == Item.STATE_OFF)
								items[board[row][col].getItemIndex()]
										.switchState();
						}
						// For Kettles, turn them on and allow water to flow
						// from them
						else if (items[board[row][col].getItemIndex()]
								.getItemType() == Item.KETTLE)
						{
							if (items[board[row][col].getItemIndex()]
									.getState() == Item.STATE_OFF)
								items[board[row][col].getItemIndex()]
										.switchState();
							transmitEnergy(Square.WATER, row, col);
						}
						// For Turbines, turn them on
						else if (items[board[row][col].getItemIndex()]
								.getItemType() == Item.TURBINE)
						{
							if (items[board[row][col].getItemIndex()]
									.getState() == Item.STATE_OFF)
								items[board[row][col].getItemIndex()]
										.switchState();
						}
						// For the Target, turn it on
						else if (items[board[row][col].getItemIndex()]
								.getItemType() == Item.TARGET)
						{
							if (items[board[row][col].getItemIndex()]
									.getState() == Item.STATE_OFF)
								items[board[row][col].getItemIndex()]
										.switchState();
						}
					}
		repaint();
	}

	/**
	 * Transform the energies occupying squares back to electricity if the right
	 * item and its corresponding energy are present
	 */
	private void energyToElectricity()
	{
		// Checks each square on the game board for items and energies
		for (int row = 0; row < NO_OF_ROWS; row++)
			for (int col = 0; col < NO_OF_COLS - INVENTORY_COLS; col++)
				if (board[row][col].getItemIndex() > Square.EMPTY)
				{
					// Solar Panel: converts light into electricity
					if (items[board[row][col].getItemIndex()].getItemType() == Item.SOLAR_PANEL
							&& board[row][col].returnEnergyState(Square.LIGHT) == true)
						board[row][col].turnOnEnergy(Square.ELECTRICITY);

					// Dish: converts radio into electricity
					else if (items[board[row][col].getItemIndex()]
							.getItemType() == Item.DISH
							&& board[row][col].returnEnergyState(Square.RADIO) == true)
						board[row][col].turnOnEnergy(Square.ELECTRICITY);

					// Turbines: converts water into electricity
					else if (items[board[row][col].getItemIndex()]
							.getItemType() == Item.TURBINE
							&& board[row][col].returnEnergyState(Square.WATER) == true)
						board[row][col].turnOnEnergy(Square.ELECTRICITY);

				}
	}

	/**
	 * Transmits a certain energy across squares according to the given energy
	 * to be transmitted Each energy type has a different transmission behaviour
	 * (i.e. water transmits downwards only)
	 * 
	 * @param energyType the type of energy to be transmitted
	 * @param row the row at which the transmission starts
	 * @param col the column at which the transmission starts
	 */
	private void transmitEnergy(int energyType, int row, int col)
	{
		// LIGHT
		if (energyType == Square.LIGHT)
		{
			// All items block light except for solar panels. If light is
			// blocked, stop the transmission
			boolean blocked = false;
			// Transmit light upwards from the light bulb
			for (int rowUp = row - 1; rowUp >= 0; rowUp--)
			{
				if (blocked == false
						&& board[rowUp][col].getItemIndex() > Square.EMPTY
						&& items[board[rowUp][col].getItemIndex()]
								.getItemType() != Item.SOLAR_PANEL)
					blocked = true;

				if (!blocked)
					board[rowUp][col].turnOnEnergy(energyType);
			}
			// Transmit light downwards from the light bulb
			blocked = false;
			for (int rowDown = row + 1; rowDown < NO_OF_ROWS; rowDown++)
			{
				if (blocked == false
						&& board[rowDown][col].getItemIndex() > Square.EMPTY
						&& items[board[rowDown][col].getItemIndex()]
								.getItemType() != Item.SOLAR_PANEL)

					blocked = true;

				if (!blocked)
					board[rowDown][col].turnOnEnergy(energyType);
			}
			// Transmit light left from the light bulb
			blocked = false;
			for (int colLeft = col - 1; colLeft >= 0; colLeft--)
			{
				if (blocked == false
						&& board[row][colLeft].getItemIndex() > Square.EMPTY
						&& items[board[row][colLeft].getItemIndex()]
								.getItemType() != Item.SOLAR_PANEL)
					blocked = true;

				if (!blocked)
					board[row][colLeft].turnOnEnergy(energyType);
			}
			// Transmit light right from the light bulb
			blocked = false;
			for (int colRight = col + 1; colRight < NO_OF_COLS - INVENTORY_COLS; colRight++)
			{
				if (blocked == false
						&& board[row][colRight].getItemIndex() > Square.EMPTY
						&& items[board[row][colRight].getItemIndex()]
								.getItemType() != Item.SOLAR_PANEL)
					blocked = true;

				if (!blocked)
					board[row][colRight].turnOnEnergy(energyType);
			}
		}
		// RADIO WAVES
		else if (energyType == Square.RADIO)
		{
			// Turn on the radio energy for all squares in a 5x5 range centered
			// at the radio transmitter
			for (int rowIndex = row - 2; rowIndex <= row + 2; rowIndex++)
				for (int colIndex = col - 2; colIndex <= col + 2; colIndex++)
					if (rowIndex < NO_OF_ROWS && rowIndex >= 0
							&& colIndex < NO_OF_COLS - INVENTORY_COLS
							&& colIndex >= 0)
						board[rowIndex][colIndex].turnOnEnergy(Square.RADIO);
		}
		// WATER
		else if (energyType == Square.WATER)
		{
			// All items except the turbine blocks water. Stop the transmission
			// if water is blocked
			boolean blocked = false;
			// Transmit water downwards from the kettle
			for (int rowIndex = row + 1; rowIndex < NO_OF_ROWS; rowIndex++)
			{
				if (blocked == false && board[rowIndex][col].getItemIndex() > Square.EMPTY
						&& items[board[rowIndex][col].getItemIndex()]
								.getItemType() != Item.TURBINE
						)
					blocked = true;
				if (!blocked)
					board[rowIndex][col].turnOnEnergy(Square.WATER);
			}
		}

	}

	/**
	 * Spreads electricity to all the squares connected by wires. Spreads from a
	 * square that already has electricity
	 */
	private void conductWires()
	{
		// Go through each wire and conduct the wires adjacent
		for (Wire wire : wires)
		{
			int row = yCoordToRow(wire.y);
			int col = xCoordToCol(wire.x);
			// Transmit electricity if power is on
			if (board[row][col].returnEnergyState(Square.ELECTRICITY))
			{
				// Down
				if (row + 1 < NO_OF_ROWS && board[row + 1][col].hasWire())
					board[row + 1][col].turnOnEnergy(Square.ELECTRICITY);
				// Up
				if (row - 1 >= 0 && board[row - 1][col].hasWire())
					board[row - 1][col].turnOnEnergy(Square.ELECTRICITY);
				// Right
				if (col + 1 < NO_OF_COLS - INVENTORY_COLS
						&& board[row][col + 1].hasWire())
					board[row][col + 1].turnOnEnergy(Square.ELECTRICITY);
				// Left
				if (col - 1 >= 0 && board[row][col - 1].hasWire())
					board[row][col - 1].turnOnEnergy(Square.ELECTRICITY);
			}
		}
	}

	/**
	 * Teleports the item above the Teleport Sender to the Teleport Receiver if
	 * possible
	 * 
	 * @param row the row location of the Teleport Sender
	 * @param col the column location of the Teleport Sender
	 */
	private void teleport(int row, int col)
	{
		// Variable determining if the teleportation is possible
		boolean isSwitchPossible = false;
		// Variable storing the index of the item to be teleported
		int itemStoreIndex = -1;
		// The coordinates of the Teleport Receiver
		int rowStore = -1, colStore = -1;

		// Only teleport if there is an object to be teleported. Store the index
		// of the item to be teleported
		if (row - 1 >= 0 && board[row - 1][col].getItemIndex() > Square.EMPTY)
		{
			itemStoreIndex = board[row - 1][col].getItemIndex();
			// Checks if the area above the Teleport Receiver is empty, then
			// stores its location
			for (int rowReceive = 1; isSwitchPossible == false
					&& rowReceive < NO_OF_ROWS; rowReceive++)
				for (int colReceive = 0; colReceive < NO_OF_COLS
						- INVENTORY_COLS; colReceive++)
					if (board[rowReceive][colReceive].getItemIndex() > Square.EMPTY)
						if (items[board[rowReceive][colReceive].getItemIndex()]
								.getItemType() == Item.TRECEIVER
								&& items[board[rowReceive][colReceive]
										.getItemIndex()].getState() == Item.STATE_ON)
							if (board[rowReceive - 1][colReceive]
									.getItemIndex() == Square.EMPTY)
							{
								isSwitchPossible = true;
								rowStore = rowReceive - 1;
								colStore = colReceive;
							}
		}

		if (isSwitchPossible == true)
		{
			// Remove the item from the previous position, then redraw it in the
			// new position
			board[row - 1][col].removeItem();

			Point initialPoint = new Point(col * SQUARE_SIZE, (row - 1)
					* SQUARE_SIZE);
			Point newPoint = new Point(colStore * SQUARE_SIZE, rowStore
					* SQUARE_SIZE);

			items[itemStoreIndex].move(initialPoint, newPoint);
			board[rowStore][colStore].addItem(itemStoreIndex);
		}
	}

	/**
	 * Turns on the power starting at the source
	 */
	private void turnOnPower()
	{
		powerOn = true;

		// Give electricity to the source
		for (int item = 0; item < items.length; item++)
			if (items[item].getItemType() == Item.SOURCE)
			{
				if (items[item].getState() == Item.STATE_OFF)
					items[item].switchState();
				board[yCoordToRow(items[item].y)][xCoordToCol(items[item].x)].energyState[Square.ELECTRICITY] = true;
			}

		// Start timers
		timerLong.start();
		timerShort.start();
		repaint();
	}

	/**
	 * Shuts down all power on the board
	 */
	private void turnOffPower()
	{
		powerOn = false;

		// No energy
		for (int row = 0; row < board.length; row++)
			for (int col = 0; col < board[row].length; col++)
				for (int state = 0; state < Square.NO_OF_STATES; state++)
					board[row][col].energyState[state] = false;

		// All items in off states
		for (Item item : items)
			if (item.getState() == Item.STATE_ON)
				item.switchState();

		// Stop timers
		timerLong.stop();
		timerShort.stop();
		repaint();
	}

	/**
	 * Checks to see if target has power
	 */
	private void checkForWin()
	{
		// Check for winner by seeing if target is powered
		for (int item = 0; item < items.length; item++)
			if (items[item].getItemType() == Item.TARGET)
				if (board[yCoordToRow(items[item].y)][xCoordToCol(items[item].x)].energyState[0] == true)
				{
					// Congratulation Message
					JOptionPane.showMessageDialog(this,
							"Congratulations, level " + levelNo + " complete!");

					turnOffPower();

					// Remove this level panel from the frame
					Container main = getParent();
					main.remove(Level.this);

					// Decide next action
					// If finished last level:
					if (levelNo + 1 > Main.noOfLevels)
					{
						// Show winning message
						JOptionPane
								.showMessageDialog(
										this,
										"You have beat all of the levels! Now go to the Editor and make some levels of your own.");

						// Bring user to the editor
						Main.levelEditor = new LevelEditor();
						main.add(Main.levelEditor);
						Main.state = 1;

						main.revalidate();
						repaint();
						return;
					}
					// More levels to go:
					else
					{
						// Try to load the next level
						try
						{
							Main.level = new Level(levelNo + 1);
							main.add(Main.level);

							main.revalidate();
							repaint();
							return;
						}
						catch (FileNotFoundException e)
						{
							e.printStackTrace();
						}
					}
				}
	}

	/**
	 * Paints the level's drawing panel
	 * 
	 * @param g The Graphics context
	 */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		// Draw the background
		g.drawImage(imageBackground, 0, 0, this);

		// Default info box prompts
		if (!powerOn)
			g.drawImage(imagePrompt[0], 657, 297, this);
		else
			g.drawImage(imagePrompt[1], 657, 297, this);

		// Draw the items and wires on the board
		for (Wire wire : wires)
			wire.draw(g);
		for (Item item : items)
			item.draw(g);

		// Draw the selected item last so it appears on top
		if (selectedItem != null)
			items[selectedItemIndex].draw(g);

		// Draw the energies of the Squares
		if (powerOn)
			for (int row = 0; row < NO_OF_ROWS; row++)
				for (int col = 0; col < NO_OF_COLS - INVENTORY_COLS; col++)
					board[row][col].draw(g);
	}

	/* Listeners */
	// Keyboard Events
	/**
	 * Handles key pressed events
	 * 
	 * @param event information about the key pressed event
	 */
	public void keyPressed(KeyEvent event)
	{
		// Space is another way to power on the source
		if (event.getKeyCode() == KeyEvent.VK_SPACE)
		{
			if (!powerOn)
				turnOnPower();
			else
				turnOffPower();

			repaint();
			return;
		}
	}

	// Mouse events
	/**
	 * Handles mouse presses
	 * 
	 * @param event information about the mouse pressed event
	 */
	public void mousePressed(MouseEvent event)
	{
		Point selectedPoint = event.getPoint();

		// Source item
		if (sourceItem.contains(selectedPoint))
		{
			if (event.getButton() == MouseEvent.BUTTON1)
			{

				// Turn on the power
				if (!powerOn)
					turnOnPower();
				else
					turnOffPower();

				repaint();
				return;
			}
		}
		// Other items
		else
		{
			// Check if point clicked contains an item
			for (int i = 0; i < noOfItems; i++)
				if (items[i].contains(selectedPoint) && !items[i].isLocked()
						&& !powerOn)
				{
					// Left mouse button
					if (event.getButton() == MouseEvent.BUTTON1)
					{
						// Store the selected item
						selectedItem = items[i];
						selectedItemIndex = i;

						// Store the point where this item came from
						firstPoint = selectedPoint;
						lastPoint = selectedPoint;

						// Remove the item from the square
						board[yCoordToRow(selectedPoint.y)][xCoordToCol(selectedPoint.x)]
								.removeItem();

					}
					repaint();
					return;
				}
		}
	}

	/**
	 * Handles mouse movements
	 * 
	 * @param event information about the mouse moved event
	 */
	public void mouseMoved(MouseEvent event)
	{
		Point currentPoint = event.getPoint();

		// Source item
		if (sourceItem.contains(currentPoint))
		{
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

			// Get what item is being hovered and update the item info
			// bar to match the item being hovered
			if (hoveredItem != null)
				hoveredItem.notHovered();
			hoveredItem = sourceItem;
			hoveredItem.hovered();

			repaint();
			return;
		}
		// Other items
		else if (!powerOn)
		{
			for (int i = 0; i < noOfItems; i++)
				if (items[i].contains(currentPoint))
				{
					// Set the cursor to the hand if we are on an item
					if (!items[i].isLocked())
						setCursor(Cursor
								.getPredefinedCursor(Cursor.HAND_CURSOR));

					// Get what item is being hovered and update the item info
					// bar to match the item being hovered
					if (hoveredItem != null)
						hoveredItem.notHovered();
					hoveredItem = items[i];
					hoveredItem.hovered();

					repaint();
					return;
				}
		}

		// Otherwise we just use the default cursor
		setCursor(Cursor.getDefaultCursor());

		// No item is hovered
		if (hoveredItem != null)
			hoveredItem.notHovered();
		hoveredItem = null;
		repaint();
	}

	/**
	 * Handles mouse drags
	 * 
	 * @param event information about the mouse dragged event
	 */
	public void mouseDragged(MouseEvent event)
	{
		Point currentPoint = event.getPoint();

		// Update the selected item with the mouse position
		if (selectedItem != null)
		{
			selectedItem.move(lastPoint, currentPoint);
			lastPoint = currentPoint;
			repaint();
		}
	}

	/**
	 * Handles mouse releases
	 * 
	 * @param event information about the mouse pressed event
	 */
	public void mouseReleased(MouseEvent event)
	{
		// Released an item
		if (selectedItem != null)
		{
			// Use item coordinates instead of mouse since we want the item to
			// snap to its closest square and not snap to the closest square to
			// the mouse
			int itemX = selectedItem.x + selectedItem.width / 2;
			int itemY = selectedItem.y + selectedItem.height / 2;

			// Find the best column and row numbers for the current point
			int squareCol = xCoordToCol(itemX);
			int squareRow = yCoordToRow(itemY);

			// Out of board or on another piece
			if (itemX > NO_OF_COLS * SQUARE_SIZE
					|| itemY > NO_OF_ROWS * SQUARE_SIZE || itemX < 0
					|| itemY < 0
					|| board[squareRow][squareCol].getItemIndex() != -1)
			{
				// Return the item to where it came from
				selectedItem.move(lastPoint, firstPoint);
				board[yCoordToRow(firstPoint.y)][xCoordToCol(firstPoint.x)]
						.addItem(selectedItemIndex);
			}
			// Valid move
			else
			{
				// Adjust the new position and add the item to the square
				selectedItem.snapToSquare(itemX, itemY, SQUARE_SIZE);
				board[yCoordToRow(selectedItem.y)][xCoordToCol(selectedItem.x)]
						.addItem(selectedItemIndex);
			}

			selectedItem = null;
			selectedItemIndex = -1;
			repaint();
		}
	}

	// Unused Listeners
	public void keyReleased(KeyEvent event)
	{
	}

	public void keyTyped(KeyEvent event)
	{
	}

	public void mouseClicked(MouseEvent event)
	{
	}

	public void mouseEntered(MouseEvent event)
	{
	}

	public void mouseExited(MouseEvent event)
	{
	}

	/**
	 * TimerEventHandler - an inner class that handles the timer events
	 * 
	 * @author Derrick Thai and Riddle Li
	 * @version v6.2Final, Last Updated: June 16, 2014
	 */
	private class TimerEventHandler implements ActionListener
	{

		/**
		 * Progressively sends power around the grid by activating items and
		 * conducting electricity
		 * 
		 * @param event the Timer event
		 */
		public void actionPerformed(ActionEvent event)
		{
			// Less frequent checks
			if (event.getSource() == timerLong)
			{
				turnOnEnergy();
				energyToElectricity();
				checkForWin();
			}
			// More frequently conduct wires
			else
			{
				conductWires();
			}
		}
	}
}
