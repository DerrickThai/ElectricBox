import java.awt.*;

import javax.swing.*;

/**
 * Item - the item objects that holds all of the items, their states, and images
 * 
 * @author Derrick Thai and Riddle Li
 * @version v6.2Final, Last Updated: June 16, 2014
 */
public class Item extends Rectangle
{
	// Serial Version ID to remove error
	private static final long serialVersionUID = 1L;

	/* Constants */
	// Item Types
	public static final int SOURCE = 0;
	public static final int TARGET = 1;
	public static final int LIGHT_BULB = 2;
	public static final int SOLAR_PANEL = 3;
	public static final int RADIO = 4;
	public static final int DISH = 5;
	public static final int BLOCK = 6;
	public static final int TSENDER = 7;
	public static final int TRECEIVER = 8;
	public static final int KETTLE = 9;
	public static final int TURBINE = 10;

	// Item States
	public static final int STATE_ON = 1;
	public static final int STATE_OFF = -1;

	// Images
	private static final String IMAGE_FOLDER = "./images/";

	public static final String[] OFF_FILE_NAMES = { "source.png", "target.png",
			"lightBulb.png", "solarPanel.png", "radio.png", "dish.png",
			"block.png", "tSender.png", "tReceiver.png", "kettle.png",
			"turbine.png" };
	public static final String[] ON_FILE_NAMES = { "sourceOn.png",
			"targetOn.png", "lightBulbOn.png", "solarPanelOn.png",
			"radioOn.png", "dishOn.png", "block.png", "tSenderOn.png",
			"tReceiverOn.png", "kettleOn.png", "turbineOn.png" };
	public static final String[] INFO_FILE_NAMES = { "sourceInfo.png",
			"targetInfo.png", "lightBulbInfo.png", "solarPanelInfo.png",
			"radioInfo.png", "dishInfo.png", "blockInfo.png",
			"tSenderInfo.png", "tReceiverInfo.png", "kettleInfo.png",
			"turbineInfo.png" };

	/* Variables */
	private int type, state;
	private boolean locked, hovered;
	Image image, infoImage;

	/**
	 * Constructs a new Item object
	 * 
	 * @param x the x coordinate of the item
	 * @param y the y coordinate of the item
	 * @param itemType the item type
	 * @param parentFrame the parent frame
	 */
	public Item(int x, int y, int itemType, Component parentFrame)
	{
		super(x, y, 0, 0);

		// Initialize the item type, state and images
		type = itemType;
		state = STATE_OFF;
		image = new ImageIcon(IMAGE_FOLDER + OFF_FILE_NAMES[itemType])
				.getImage();
		infoImage = new ImageIcon(IMAGE_FOLDER + INFO_FILE_NAMES[itemType])
				.getImage();

		// Lock the item if it is meant to be immovable
		if (itemType == SOURCE || itemType == TARGET)
			locked = true;

		// Set the item size based off the image size
		width = image.getWidth(parentFrame);
		height = image.getHeight(parentFrame);
		setSize(width, height);
	}

	/**
	 * Moves the item based off the change between the initial and final
	 * position of the mouse
	 * 
	 * @param initialPos the initial position of the mouse
	 * @param finalPos the final position of the mouse
	 */
	public void move(Point initialPos, Point finalPos)
	{
		// Moves the item based off the change between the initial and final
		// position of the mouse using the Rectangle class' translate method
		translate(finalPos.x - initialPos.x, finalPos.y - initialPos.y);
	}

	/**
	 * Snaps the item's position to the square of best fit
	 * 
	 * @param centreX the x coordinate of the centre of the item
	 * @param centreY the y coordinate of the centre of the item
	 * @param squareSize the square size
	 */
	public void snapToSquare(int centreX, int centreY, int squareSize)
	{
		// Adjust the position of the item to fit in the closest square relative
		// to the centre of the item
		x = centreX / squareSize * squareSize;
		y = centreY / squareSize * squareSize;
	}

	/**
	 * Changes the on/off state of the item
	 */
	public void switchState()
	{
		state *= -1;
		// Loads the corresponding image for the item according to the current
		// state
		if (state == STATE_ON)
			image = new ImageIcon(IMAGE_FOLDER + ON_FILE_NAMES[type])
					.getImage();
		else
			image = new ImageIcon(IMAGE_FOLDER + OFF_FILE_NAMES[type])
					.getImage();
	}

	/**
	 * Gives access to the current on/off state of the object
	 * 
	 * @return the on/off state of the item
	 */
	public int getState()
	{
		return state;
	}

	/**
	 * Gives access to this item's type
	 * 
	 * @return this item's type
	 */
	public int getItemType()
	{
		return type;
	}

	/**
	 * Locks the item to prevent moving
	 */
	public void lock()
	{
		locked = true;
	}

	/**
	 * Gives access to whether or not the item is movable
	 * 
	 * @return the move-ability of the item
	 */
	public boolean isLocked()
	{
		return locked;
	}

	/**
	 * Hovers the item above it's square
	 */
	public void hovered()
	{
		hovered = true;
	}

	/**
	 * Disables hovering of the item
	 */
	public void notHovered()
	{
		hovered = false;
	}

	/**
	 * Draws this item in a Graphics context
	 * 
	 * @param g the Graphics context to draw the item in
	 */
	public void draw(Graphics g)
	{
		g.drawImage(image, x, y, null);

		// If the item is being hovered draw the info image in the bottom corner
		// of the screen in the info box
		if (hovered)
			g.drawImage(infoImage, 657, 297, null);
	}

	/**
	 * Returns a String representation of the item
	 * 
	 * @return the x and y coordinates and characteristics of this item
	 */
	public String toString()
	{
		return String.format(
				"Item -> x: %d y: %d Type: %d State: %d Locked: %b%n", x, y,
				type, state, locked);
	}
}
