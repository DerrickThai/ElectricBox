import java.awt.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Level Editor - the level editor panel where levels are created and saved to a
 * ".op" file for future loading by the Level class
 * 
 * @author Derrick Thai and Riddle Li
 * @version v6.2Final, Last Updated: June 16, 2014
 */
public class LevelEditor extends JPanel implements MouseListener,
		MouseMotionListener, KeyListener, ActionListener
{
	// Serial Version ID to remove error
	private static final long serialVersionUID = 1L;

	/* Constants */
	// Board Size
	private final int SQUARE_SIZE = 60;
	private final int NO_OF_ROWS = 10;
	private final int NO_OF_COLS = 16;
	private final int INVENTORY_COLS = 6;
	private final int INVENTORY_ROWS = 6;
	private final Dimension BOARD_SIZE = new Dimension(
			NO_OF_COLS * SQUARE_SIZE, NO_OF_ROWS * SQUARE_SIZE);

	// Total number of item types
	private int NO_OF_CHOICE_ITEMS = Item.OFF_FILE_NAMES.length;

	/* Variables */
	// Board
	private Square[][] board;

	// Items and Wires
	private int noOfItems;
	private Item[] choiceItems;
	private ArrayList<Item> items;

	private int noOfWires;
	private Wire choiceWire;
	private ArrayList<Wire> wires;

	// Selected Item and Wire
	Point lastPoint, firstPoint;

	private Item selectedItem;
	private int selectedItemIndex;
	private boolean selectedChoiceItem;

	private Wire selectedWire;
	private int selectedWireIndex;
	private boolean selectedChoiceWire;

	// Buttons
	private JButton saveButton;
	private JButton openButton;

	// Background image
	Image imageBackground;

	/**
	 * Constructs a new Level object
	 * 
	 * @throws FileNotFoundException if the level file is not found
	 */
	public LevelEditor()
	{
		// Add the listeners
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);

		// Panel preferences
		setPreferredSize(BOARD_SIZE);
		setLayout(null);
		setFocusable(true);
		requestFocusInWindow();

		// Load up the squares, choices' ArrayLists and positions (for both the
		// choice items and wire), and buttons
		loadArrayLists();
		loadSquares();
		loadChoices();
		loadButtons();

		// Initialize the background image for the editor
		imageBackground = new ImageIcon("./images/editorBoard.png").getImage();
	}

	/**
	 * Initializes the square objects
	 */
	private void loadSquares()
	{
		// Initialize the array of squares
		board = new Square[NO_OF_ROWS][NO_OF_COLS];

		// Initialize each square
		for (int row = 0; row < board.length; row++)
			for (int col = 0; col < board[row].length; col++)
				board[row][col] = new Square(col * SQUARE_SIZE, row
						* SQUARE_SIZE, SQUARE_SIZE);

		// Disable choice item squares and the dividing column between the
		// inventory and board, but leave the inventory squares enabled
		for (int row = 0; row < board.length; row++)
			for (int col = NO_OF_COLS - INVENTORY_COLS; col < board[row].length; col++)
				board[row][col].disable();
		for (int row = 0; row < NO_OF_ROWS - INVENTORY_ROWS; row++)
			for (int col = NO_OF_COLS - INVENTORY_COLS + 1; col < board[row].length; col++)
				board[row][col].removeItem();

	}

	/**
	 * Initializes the items and wires ArrayLists
	 */
	private void loadArrayLists()
	{
		items = new ArrayList<Item>();
		wires = new ArrayList<Wire>();
	}

	/**
	 * Initializes the choices (items and wire)
	 */
	private void loadChoices()
	{
		// Initializes the choice items array
		choiceItems = new Item[NO_OF_CHOICE_ITEMS];

		// Add each choice item to the choice squares. Fills the squares
		// from left to right and then down once the row is full
		for (int item = 0; item < NO_OF_CHOICE_ITEMS; item++)
		{
			// Calculate the coordinates of square to put the item in
			int itemX = ((item % (INVENTORY_COLS - 1)) + NO_OF_COLS
					- INVENTORY_COLS + 1)
					* SQUARE_SIZE;
			int itemY = ((item / (INVENTORY_COLS - 1)) + 5) * SQUARE_SIZE;

			// Place the item in the choice item area
			choiceItems[item] = new Item(itemX, itemY, item, this);
		}

		// Initialize the choice wire and add it to the bottom-left most corner
		// choice square since we do not want the items to overlap the wire
		choiceWire = new Wire((NO_OF_COLS - 1) * SQUARE_SIZE, (NO_OF_ROWS - 1)
				* SQUARE_SIZE, this);

		// Start with no items or wires on the board
		noOfItems = 0;
		noOfWires = 0;
	}

	/**
	 * Initializes the buttons
	 */
	private void loadButtons()
	{
		// Save button
		saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		saveButton.setBounds(660, 540, 120, 60);
		add(saveButton);

		// Open button
		openButton = new JButton("Open");
		openButton.addActionListener(this);
		openButton.setBounds(780, 540, 120, 60);
		add(openButton);
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
	 * Saves the level to a file
	 */
	private void saveFile()
	{
		// Validate the level
		if (validateLevel())
		{
			// Disable the option of renaming files
			UIManager.put("FileChooser.readOnly", Boolean.TRUE);

			// Create a new file chooser to save the level
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Save your level");

			// Set the suggested file name to the next level number
			Main.checkNoOfLevels();
			fileChooser.setSelectedFile(new File("level"
					+ (Main.noOfLevels + 1)));

			// Add the filter for level file extension "op"
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"OP files", "op");
			fileChooser.setFileFilter(filter);

			// Make the starting directory the levels folder
			File startingDirectory = new File("./levels");
			fileChooser.setCurrentDirectory(startingDirectory);

			// Check if user confirms saving
			int userSelection = fileChooser.showSaveDialog(this);
			if (userSelection == JFileChooser.APPROVE_OPTION)
			{
				// If extension is missing, add it
				File fileToSave = fileChooser.getSelectedFile();
				String filePathName = fileToSave.getAbsolutePath();
				if (!filePathName.endsWith(".op"))
					fileToSave = new File(fileChooser.getSelectedFile() + ".op");

				// If the file exists, confirm to overwrite
				if (fileToSave.exists())
				{
					int existResponse = JOptionPane
							.showConfirmDialog(
									this,
									"The level \""
											+ fileToSave.getName()
											+ " \"already exists. Do you want to replace the existing level?",
									"Ovewrite file", JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE);
					if (existResponse != JOptionPane.YES_OPTION)
						return;
				}

				// Once the file is ready, write the level to the file
				try
				{
					// Make a brand new file to handle overwriting
					PrintWriter outFile = new PrintWriter(new FileWriter(
							fileToSave, false));

					// Print the number of items and wires on the first two
					// lines of the file
					outFile.println(noOfItems + " = Number of Items");
					outFile.println(noOfWires + " = Number of Wires");

					// Scan each square of the board and print the correct
					// characters to the file to identify each square
					for (int row = 0; row < board.length; row++)
					{
						for (int col = 0; col < board[row].length; col++)
						{
							// Get the item index that square has
							int printCh = board[row][col].getItemIndex();
							boolean wire = board[row][col].hasWire();

							// Square has no item
							if (printCh == Square.EMPTY)
								// Square has no wire
								if (!wire)
									outFile.print('O');
								// Square has wire
								else
									outFile.print('W');
							// Square is disabled
							else if (printCh == Square.DISABLED)
								outFile.print('X');
							// Square has item
							else
							{
								// Make number negative if wire + item
								if (wire)
									outFile.print("-");
								outFile.print(items.get(printCh).getItemType());
							}
							outFile.print(" ");
						}
						outFile.println();
					}
					// Successful save message
					JOptionPane.showMessageDialog(this,
							"Level saved sucessfully.");
					outFile.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

			}
		}
		// Invalid level message
		else
			JOptionPane
					.showMessageDialog(this,
							"Invalid level. Make sure there is exactly one source and one target.");

	}

	/**
	 * Opens an existing level file
	 */
	private void openFile()
	{
		// Disable the renaming of files
		UIManager.put("FileChooser.readOnly", Boolean.TRUE);

		// Create a new file chooser to open a level
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select a level to open.");

		// Add a filter for level file extensions ".op"
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"OP files", "op");
		fileChooser.setFileFilter(filter);

		// Make starting directory the levels folder
		File startingDirectory = new File("./levels");
		fileChooser.setCurrentDirectory(startingDirectory);

		// Check if user confirms opening
		int userSelection = fileChooser.showOpenDialog(this);
		if (userSelection == JFileChooser.APPROVE_OPTION)
		{
			// Try to load the file
			try
			{
				// Recreate the squares and ArrayLists
				loadArrayLists();
				loadSquares();
				int itemIndex = 0;

				// Load the file and read the number of items and wires
				Scanner inFile = new Scanner(fileChooser.getSelectedFile());
				noOfItems = inFile.nextInt();
				inFile.nextLine();
				noOfWires = inFile.nextInt();
				inFile.nextLine();

				// Add the item objects
				for (int row = 0; row < board.length; row++)
					for (int col = 0; col < board[row].length; col++)
					{
						// Store the next String
						String readStr = inFile.next();

						// If the string is not a letter there must be an item
						if (!Level.isLetter(readStr))
						{
							// If the number is negative, there is also a wire
							// (check the character because -0 is a possibility)
							if (readStr.charAt(0) == '-')
							{
								// Add the wire
								wires.add(new Wire(col * SQUARE_SIZE, row
										* SQUARE_SIZE, this));
								board[row][col].addWire();
							}

							// Use the positive value of the integer to decide
							// the item type and add that item
							int readInt = Math.abs(Integer.parseInt(readStr));

							items.add(new Item(col * SQUARE_SIZE, row
									* SQUARE_SIZE, readInt, this));
							board[row][col].addItem(itemIndex++);

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
								wires.add(new Wire(col * SQUARE_SIZE, row
										* SQUARE_SIZE, this));
								board[row][col].addWire();
							}
							// 'O' is for a completely empty square but no code
						}
					}
				inFile.close();
				paintImmediately(0, 0, BOARD_SIZE.width, BOARD_SIZE.height);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Checks to make sure the level is valid(has only one source and target)
	 * 
	 * @return whether the level is valid or not
	 */
	private boolean validateLevel()
	{
		// Make sure there is only one source and target
		int noOfSources = 0;
		int noOfTargets = 0;

		// Find the source and target and make sure there are wires beneath it
		for (int item = 0; item < items.size(); item++)
			if (items.get(item).getItemType() == Item.SOURCE
					|| items.get(item).getItemType() == Item.TARGET)
			{
				// Convert the item's coordinate to row and column numbers
				int row = yCoordToRow(items.get(item).y);
				int col = xCoordToCol(items.get(item).x);

				// Ensure there is a wire under the source and target
				if (!board[row][col].hasWire())
				{
					wires.add(new Wire(col * SQUARE_SIZE, row * SQUARE_SIZE,
							this));
					board[row][col].addWire();
					noOfWires++;

					// Notify user that a wire was automatically added under a
					// source or target that was missing a wire
					JOptionPane
							.showMessageDialog(this,
									"A wire was not placed under a source/target and has been added.");
				}

				// Check number of sources and targets
				if (items.get(item).getItemType() == Item.SOURCE)
					noOfSources++;
				else
					noOfTargets++;
			}

		// Determine if level is valid or not
		if (noOfSources == 1 && noOfTargets == 1)
			return true;
		return false;
	}

	/**
	 * Paints the editors's drawing panel
	 * 
	 * @param g The Graphics context
	 */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		// Draw the background image
		g.drawImage(imageBackground, 0, 0, this);

		// Draw the choice items and wires
		for (Item choiceItem : choiceItems)
			choiceItem.draw(g);
		choiceWire.draw(g);

		// Draw the items and wires
		for (Wire wire : wires)
			wire.draw(g);
		for (Item item : items)
			item.draw(g);

		// Draw the selected item/wire last so it appears on top
		if (selectedItem != null)
			items.get(selectedItemIndex).draw(g);
		else if (selectedWire != null)
			wires.get(selectedWireIndex).draw(g);
	}

	/* Listener Events */
	/**
	 * Handles mouse presses
	 * 
	 * @param event information about the mouse pressed event
	 */
	public void mousePressed(MouseEvent event)
	{
		Point selectedPoint = event.getPoint();

		// Check if point clicked contains a choice item
		for (int i = 0; i < choiceItems.length; i++)
			if (choiceItems[i].contains(selectedPoint))
			{
				// Left mouse button
				if (event.getButton() == MouseEvent.BUTTON1)
				{
					// Create a new item that matches the selected choice item
					noOfItems++;
					items.add(new Item(choiceItems[i].x, choiceItems[i].y,
							choiceItems[i].getItemType(), this));

					// Store the selected item
					selectedItem = items.get(noOfItems - 1);
					selectedItemIndex = noOfItems - 1;

					// Store the point where this item came from
					firstPoint = selectedPoint;
					lastPoint = selectedPoint;
					selectedChoiceItem = true;
				}
				repaint();
				return;
			}

		// Check if point clicked contains a choice wire
		if (choiceWire.contains(selectedPoint))
		{
			// Left mouse button
			if (event.getButton() == MouseEvent.BUTTON1)
			{
				// Create a new wire
				noOfWires++;
				wires.add(new Wire(choiceWire.x, choiceWire.y, this));

				// Store the selected wire
				selectedWire = wires.get(noOfWires - 1);
				selectedWireIndex = noOfWires - 1;

				// Store the point where this wire came from
				firstPoint = selectedPoint;
				lastPoint = selectedPoint;
				selectedChoiceWire = true;
			}
			repaint();
			return;
		}

		// Check if point clicked contains an item
		for (int i = 0; i < items.size(); i++)
			if (items.get(i).contains(selectedPoint))
			{
				// Right mouse button
				if (event.getButton() == MouseEvent.BUTTON3)
				{
					// Remove the item
					items.remove(i);
					correctItemIndexes();
					noOfItems--;
				}
				// Left mouse button
				else
				{
					// Store the selected item
					selectedItem = items.get(i);
					selectedItemIndex = i;

					// Store the point where this item came from
					firstPoint = selectedPoint;
					lastPoint = selectedPoint;
					selectedChoiceItem = false;
				}

				// Remove the item from the square
				board[yCoordToRow(selectedPoint.y)][xCoordToCol(selectedPoint.x)]
						.removeItem();

				repaint();
				return;
			}

		// Check if the point clicked contains a wire
		for (int i = 0; i < wires.size(); i++)
			if (wires.get(i).contains(selectedPoint))
			{
				// Right mouse button
				if (event.getButton() == MouseEvent.BUTTON3)
				{
					// Remove the wire
					board[yCoordToRow(selectedPoint.y)][xCoordToCol(selectedPoint.x)]
							.removeWire();
					wires.remove(i);
					noOfWires--;
				}
				// Left mouse button
				else
				{
					// Store the selected wire
					selectedWire = wires.get(i);
					selectedWireIndex = i;

					// Store the point where this wire came from
					firstPoint = selectedPoint;
					lastPoint = selectedPoint;
					selectedChoiceWire = false;
				}

				// Remove the wire from the square
				board[yCoordToRow(selectedPoint.y)][xCoordToCol(selectedPoint.x)]
						.removeWire();

				repaint();
				return;
			}
	}

	/**
	 * Adjust the Square object's item indexes when a deletion is made
	 */
	private void correctItemIndexes()
	{
		// Make sure each Item matches its Square's item index
		for (int item = 0; item < items.size(); item++)
			if (board[yCoordToRow(items.get(item).y)][xCoordToCol(items
					.get(item).x)].getItemIndex() != item)
			{
				board[yCoordToRow(items.get(item).y)][xCoordToCol(items
						.get(item).x)].removeItem();
				board[yCoordToRow(items.get(item).y)][xCoordToCol(items
						.get(item).x)].addItem(item);
			}
	}

	/**
	 * Handles mouse movements
	 * 
	 * @param event information about the mouse moved event
	 */
	public void mouseMoved(MouseEvent event)
	{
		// Set the cursor to the hand if we are on an item, choice item, wire,
		// or on a choice wire
		Point currentPoint = event.getPoint();

		// Item
		for (int i = 0; i < items.size(); i++)
			if (items.get(i).contains(currentPoint))
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				return;
			}
		// Choice Item
		for (int i = 0; i < choiceItems.length; i++)
			if (choiceItems[i].contains(currentPoint))
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				return;
			}
		// Wire
		for (int i = 0; i < wires.size(); i++)
			if (wires.get(i).contains(currentPoint))
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				return;
			}
		// Choice wire
		if (choiceWire.contains(currentPoint))
		{
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			return;
		}

		// Otherwise return to the default cursor
		setCursor(Cursor.getDefaultCursor());
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
		// Update the selected wire with the mouse position
		else if (selectedWire != null)
		{
			selectedWire.move(lastPoint, currentPoint);
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
		// Item release
		if (selectedItem != null)
		{
			// Use item coordinates instead of mouse to determine what square
			// the item should snap to since we want the item to snap to its
			// closest square and not snap to the closest square to the mouse
			int itemX = selectedItem.x + selectedItem.width / 2;
			int itemY = selectedItem.y + selectedItem.height / 2;

			// Find the best column and row numbers for the current point
			int squareCol = xCoordToCol(itemX);
			int squareRow = yCoordToRow(itemY);

			// Invalid Move
			// Check if the item is out of board or on another item or out of
			// bound in another way
			if (itemX > NO_OF_COLS * SQUARE_SIZE
					|| itemY > NO_OF_ROWS * SQUARE_SIZE
					|| itemX < 0
					|| itemY < 0
					|| board[squareRow][squareCol].getItemIndex() != -1
					|| ((selectedItem.getItemType() == Item.SOURCE || selectedItem
							.getItemType() == Item.TARGET) && itemX > (NO_OF_COLS - INVENTORY_COLS)
							* SQUARE_SIZE))
				// If holding an already placed item:
				if (!selectedChoiceItem)
				{
					// Return the item to where it came from
					selectedItem.move(lastPoint, firstPoint);
					board[yCoordToRow(firstPoint.y)][xCoordToCol(firstPoint.x)]
							.addItem(selectedItemIndex);
				}
				// If holding an item just spawned:
				else
				{
					// Remove that item completely
					items.remove(selectedItemIndex);
					noOfItems--;
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
		// Wire release
		else if (selectedWire != null)
		{
			// Use wire coordinates instead of mouse to determine what square
			// the wire should snap to since we want the wire to snap to its
			// closest square and not snap to the closest square to the mouse
			int wireX = selectedWire.x + selectedWire.width / 2;
			int wireY = selectedWire.y + selectedWire.height / 2;

			// Find the best column and row numbers for the current point
			int squareCol = xCoordToCol(wireX);
			int squareRow = yCoordToRow(wireY);

			// Invalid move
			// Check if the wire is out of electricity grid
			if (wireX > (NO_OF_COLS - INVENTORY_COLS) * SQUARE_SIZE
					|| wireY > NO_OF_ROWS * SQUARE_SIZE || wireX < 0
					|| wireY < 0 || board[squareRow][squareCol].hasWire()
					|| board[squareRow][squareCol].getItemIndex() != -1)
				// If holding an already placed wire:
				if (!selectedChoiceWire)
				{
					// Return the wire to where it came from
					selectedWire.move(lastPoint, firstPoint);
					board[yCoordToRow(firstPoint.y)][xCoordToCol(firstPoint.x)]
							.addWire();
				}
				// If holding an wire just spawned:
				else
				{
					// Remove that wire
					wires.remove(selectedWireIndex);
					noOfWires--;
				}
			// Valid move
			else
			{
				// Adjust the new position and add the wire to the square
				selectedWire.snapToSquare(wireX, wireY, SQUARE_SIZE);
				board[yCoordToRow(selectedWire.y)][xCoordToCol(selectedWire.x)]
						.addWire();
			}

			selectedWire = null;
			selectedWireIndex = -1;
			repaint();
		}
	}

	/**
	 * Handles button presses
	 * 
	 * @param event information about the action event
	 */
	public void actionPerformed(ActionEvent event)
	{
		// Save button
		if (event.getSource() == saveButton)
			saveFile();
		// Open button
		else if (event.getSource() == openButton)
			openFile();

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

	public void keyPressed(KeyEvent event)
	{
	}

	public void keyReleased(KeyEvent event)
	{
	}

	public void keyTyped(KeyEvent event)
	{
	}
}
