package de.pdbm.janki.cli;

import de.pdbm.janki.core.notifications.TransitionUpdate;
import de.pdbm.janki.core.notifications.TransitionUpdateListener;

public class TransitionUpdateListenerExample implements TransitionUpdateListener {

	@Override
	public void onTransitionUpdate(TransitionUpdate transitionUpdate) {
		System.out.println(transitionUpdate);
	}

}
