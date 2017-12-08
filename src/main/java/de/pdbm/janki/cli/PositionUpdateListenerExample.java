package de.pdbm.janki.cli;

import de.pdbm.janki.core.notifications.PositionUpdate;
import de.pdbm.janki.core.notifications.PositionUpdateListener;

public class PositionUpdateListenerExample implements PositionUpdateListener {

	@Override
	public void onPositionUpdate(PositionUpdate positionUpdate) {
		System.out.println(positionUpdate);
	}

}
