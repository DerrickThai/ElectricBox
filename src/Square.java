import java.awt.*;

/**
 * Square - the square objects that fill up the 2D board array of each level.
 * Each square keeps track of its item and energies
 * 
 * @author Derrick Thai and Riddle Li
 * @version v6.2Final, Last Updated: June 16, 2014
 */
public class Square extends Rectangle
{
	// Serial Version ID to remove error
	private static final long serialVersionUID = 1L;

	/* Constants */
	// Energy States
	public static final int NO_OF_STATES = 4;
	public static final int ELECTRICITY = 0;
	public static final int LIGHT = 1;
	public static final int RADIO = 2;
	public static final int WATER = 3;

	// Predefined Item Indexes
	public static final int EMPTY = -1;
	public static final int DISABLED = -2;

	// Variables
	boolean[] energyState = new boolean[NO_OF_STATES];
	private int itemIndex;
	private boolean hasWire;

	/**
	 * Constructs a new Square object
	 * 
	 * @param x the x coordinate of the square
	 * @param y the y coordinate of the square
	 * @param squareSize the width/height of the square
	 */
	public Square(int x, int y, int squareSize)
	{
		super(x, y, squareSize, squareSize);

		// Squares are empty by default
		itemIndex = EMPTY;
	}

	/**
	 * Adds the item to the square
	 * 
	 * @param itemIndex
	 */
	public void addItem(int itemIndex)
	{
		// If the square is not occupied, store the item index
		if (this.itemIndex == EMPTY)
			this.itemIndex = itemIndex;
	}

	/**
	 * Removes the item from the square
	 */
	public void removeItem()
	{
		this.itemIndex = EMPTY;
	}

	/**
	 * Finds the item index of the item this square holds
	 * 
	 * @return the item index of the item this square holds
	 */
	public int getItemIndex()
	{
		return itemIndex;
	}

	/**
	 * Make the square recognize it has a wire
	 */
	public void addWire()
	{
		hasWire = true;
	}

	/**
	 * Make a square recognize it doesn't have a wire
	 */
	public void removeWire()
	{
		this.hasWire = false;
	}

	/**
	 * Lets the user know whether or not this square has a wire on it
	 * 
	 * @return the wire state of the square
	 */
	public boolean hasWire()
	{
		return hasWire;
	}

	/**
	 * Allow a given energy to occupy the square
	 * 
	 * @param energyIndex the integer representation of the energy type to be
	 *            turned on
	 */
	public void turnOnEnergy(int energyIndex)
	{
		energyState[energyIndex] = true;
	}

	/**
	 * Allow a given energy to no longer occupy the square
	 * 
	 * @param energyIndex the integer representation of the energy type to be
	 *            turned off
	 */
	public void turnOffEnergy(int energyIndex)
	{
		energyState[energyIndex] = false;
	}

	/**
	 * Gives access to the whether or not a certain energy occupies the square
	 * 
	 * @param energyIndex the integer representation of the energy type to be
	 *            returned
	 * @return
	 */
	public boolean returnEnergyState(int energyIndex)
	{
		return this.energyState[energyIndex];
	}

	/**
	 * Disables the square so no items can be placed on it
	 */
	public void disable()
	{
		itemIndex = DISABLED;
	}

	/**
	 * Draws this square in a Graphics context
	 * 
	 * @param g the Graphics context to draw the square in
	 */
	public void draw(Graphics g)
	{
		if (energyState[LIGHT] == true)
		{
			Color lightColour = new Color(245, 245, 35, 100);
			g.setColor(lightColour);
			g.fillRect(x, y, width, height);
		}
		if (energyState[WATER] == true)
		{
			Color waterColour = new Color(30, 145, 255, 100);
			g.setColor(waterColour);
			g.fillRect(x + 10, y, width - 20, height);
		}
	}

	/**
	 * Returns a String representation of the square
	 * 
	 * @return the x and y coordinates and the item of this square
	 */
	public String toString()
	{
		return String
				.format("Square -> x: %d y: %d Item %d%n", x, y, itemIndex);
	}
}
