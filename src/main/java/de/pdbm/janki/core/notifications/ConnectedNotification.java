package de.pdbm.janki.core.notifications;

import de.pdbm.janki.core.Vehicle;

public final class ConnectedNotification extends Notification {

	private final boolean connected;

	public ConnectedNotification(Vehicle vehicle, boolean connected) {
		super(vehicle);
		this.connected = connected;
	}

	@Override
	public String toString() {
		return "ConnectedNotification(" + connected + ")";
	}

	public boolean isConnected() {
		return connected;
	}
	
}
