package de.pdbm.janki.tracking;

/**
 * Describe the (imaginary) information about a vehicle.
 * <p>
 * The vehicle runs on a field of square tiles. Each tile can contain a
 * road piece, i.e. straight, corner, finish/start, intersection.
 * 
 * 
 * @author bernd
 *
 */
public final class VehicleInfo {

	final Direction direction;
	final int xPosition;
	final int yPosition;
	
	public VehicleInfo(int xPosition, int yPosition, Direction direction) {
		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.direction = direction;
	}

}
