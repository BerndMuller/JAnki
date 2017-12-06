package de.pdbm.janki.core.notifications;

import de.pdbm.janki.core.RoadPiece;
import de.pdbm.janki.core.Vehicle;

/**
 * Represents information about a transition update of a vehicle, i.e. a road piece transition.
 * 
 * <p>
 * At the moment location ID and road piece ID is always 0. Maybe a firmware update will change
 * this in the future.
 * 
 * @author bernd
 *
 */
// TODO eventually refactor as super class of PositionUpdate
public final class TransitionUpdate extends Notification {
	
	private final int location;
	private final RoadPiece roadPiece;
	
	public TransitionUpdate(Vehicle vehicle, int location, RoadPiece roadPiece) {
		super(vehicle);
		this.location = location;
		this.roadPiece = roadPiece;
	}
	
	@Override
	public String toString() {
		return "TransitionUpdate(" + String.format("%1$2s", "" + location) + ", " + roadPiece + ")";
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

}
