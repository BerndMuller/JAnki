package de.pdbm.janki.core.notifications;

import de.pdbm.janki.core.Vehicle;

public final class ChargerInfoNotification extends Notification {

	private boolean unknown;
	private boolean onCharger;
	private boolean loading;
	private boolean full;
	
	public ChargerInfoNotification(Vehicle vehicle, boolean unknown, boolean onCharger, boolean loading, boolean full) {
		super(vehicle);
		this.unknown = unknown;
		this.onCharger = onCharger;
		this.loading = loading;
		this.full = full;
	}

	@Override
	public String toString() {
		return "ChargerInfo(" + unknown + ", " + onCharger + ", " + loading + ", " + full + ")";
	}

	public boolean isUnknown() {
		return unknown;
	}

	public boolean isOnCharger() {
		return onCharger;
	}

	public boolean isLoading() {
		return loading;
	}

	public boolean isFull() {
		return full;
	}

}
