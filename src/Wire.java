import java.awt.*;

import javax.swing.*;

/**
 * Wire - the wire objects that conduct electricity to other wires that are
 * connected and adjacent
 * 
 * @author Derrick Thai and Riddle Li
 * @version v6.2Final, Last Updated: June 16, 2014
 */
public class Wire extends Rectangle
{
	// Serial Version ID to remove error
	private static final long serialVersionUID = 1L;

	// Variables
	int width, height;
	Image image;

	/**
	 * Constructs a new wire object
	 * 
	 * @param x the x coordinate of the wire
	 * @param y the y coordinate of the wire
	 * @param parentFrame the parent frame
	 */
	public Wire(int x, int y, Component parentFrame)
	{
		super(x, y, 0, 0);

		// Initialize the wire type and image
		image = new ImageIcon("./images/wire.png").getImage();

		// Set the wire size based off the image size
		width = image.getWidth(parentFrame);
		height = image.getHeight(parentFrame);
		setSize(width, height);
	}

	/**
	 * Moves the wire based off the change between the initial and final
	 * position of the mouse
	 * 
	 * @param initialPos the initial position of the mouse
	 * @param finalPos the final position of the mouse
	 */
	public void move(Point initialPos, Point finalPos)
	{
		// Moves the wire based off the change between the initial and final
		// position of the mouse using the Rectangle class' translate method
		translate(finalPos.x - initialPos.x, finalPos.y - initialPos.y);
	}

	/**
	 * Snaps the wire's position to the square of best fit
	 * 
	 * @param centreX the x coordinate of the centre of the wire
	 * @param centreY the y coordinate of the centre of the wire
	 * @param squareSize the square size
	 */
	public void snapToSquare(int centreX, int centreY, int squareSize)
	{
		// Adjust the position of the wire to fit in the closest square relative
		// to the centre of the wire
		x = centreX / squareSize * squareSize;
		y = centreY / squareSize * squareSize;
	}

	/**
	 * Draws this wire in a Graphics context
	 * 
	 * @param g the Graphics context to draw the wire in
	 */
	public void draw(Graphics g)
	{
		g.drawImage(image, x, y, null);
	}

	/**
	 * Returns a String representation of the wire
	 * 
	 * @return the x and y coordinates and the type of this wire
	 */
	public String toString()
	{
		return String.format("Wire -> x: %d y: %d%n", x, y);
	}
}
