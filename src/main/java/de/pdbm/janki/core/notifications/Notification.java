package de.pdbm.janki.core.notifications;

import de.pdbm.janki.core.Vehicle;

/**
 * Oberklasse aller BLE-Benachrichtigungen.
 * 
 *  
 * @author bernd
 *
 */
public abstract class Notification {

	/**
	 * Das Vehicle, für das die Notification ist.
	 */
	private final Vehicle vehicle;

	protected Notification(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}
	
}
