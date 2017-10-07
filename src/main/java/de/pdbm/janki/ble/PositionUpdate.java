package de.pdbm.janki.ble;

import de.pdbm.janki.ble.roads.RoadPiece;

/**
 * Represents information about a position update of a vehicle.
 * 
 * @author bernd
 *
 */
public final class PositionUpdate extends Notification {
	
	private final int location;
	private final RoadPiece roadPiece;
	private final boolean ascendingLocations;
	
	public PositionUpdate(Vehicle vehicle, int location, RoadPiece roadPiece, boolean ascendingLocations) {
		super(vehicle);
		this.location = location;
		this.roadPiece = roadPiece;
		this.ascendingLocations = ascendingLocations;
	}
	
	@Override
	public String toString() {
		return "PositionUpdate(" + String.format("%1$2s", "" + location) + ", " + roadPiece.getId() + ", " + ascendingLocations + ")";
	}
	

	/**
	 * Returns the location ID.
	 * 
	 * @return location ID
	 * 
	 */
	public int getLocation() {
		return location;
	}

	/**
	 * Returns the road piece ID.
	 * 
	 * @return road piede ID
	 */
	public RoadPiece getRoadPiece() {
		return roadPiece;
	}

	/**
	 * Returns true, if and only if, location IDs are passed in ascending order. 
	 * 
	 * @return true, if location IDs are passed in ascending order, false otherwise
	 * 
	 */
	public boolean isAscendingLocations() {
		return ascendingLocations;
	}
	
}
