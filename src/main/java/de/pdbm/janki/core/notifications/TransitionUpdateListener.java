package de.pdbm.janki.core.notifications;

public interface TransitionUpdateListener extends NotificationListener {
	
	void onTransitionUpdate(TransitionUpdate transitionUpdate);
	
}