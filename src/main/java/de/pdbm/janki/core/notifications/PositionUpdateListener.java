package de.pdbm.janki.core.notifications;

public interface PositionUpdateListener extends NotificationListener {
	
	void onPositionUpdate(PositionUpdate positionUpdate);
	
}