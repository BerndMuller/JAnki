package de.pdbm.janki.ble;

import de.pdbm.janki.ble.roads.RoadPiece;

public final class PositionUpdate extends Notification {
	
	private final int location;
	private final RoadPiece roadPiece;
	
	public PositionUpdate(Vehicle vehicle, int location, RoadPiece roadPiece) {
		super(vehicle);
		this.location = location;
		this.roadPiece = roadPiece;
	}
	
	@Override
	public String toString() {
		return "PositionUpdate [location=" + location + ", roadPiece=" + roadPiece + "]";
	}

	public int getLocation() {
		return location;
	}

	public RoadPiece getRoadPiece() {
		return roadPiece;
	}
	
}
