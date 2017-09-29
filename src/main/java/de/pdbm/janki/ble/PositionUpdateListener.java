package de.pdbm.janki.ble;

public interface PositionUpdateListener extends NotificationListener {
	
	void onPositionUpdate(PositionUpdate positionUpdate);
	
}