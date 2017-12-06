package de.pdbm.janki.core.notifications;

import org.junit.Assert;
import org.junit.Test;

import de.pdbm.janki.core.notifications.Notification;
import de.pdbm.janki.core.notifications.NotificationParser;
import de.pdbm.janki.core.notifications.PositionUpdate;
import de.pdbm.janki.core.notifications.TransitionUpdate;

public class NotificationParserTest {

	
	@Test
	public void transitionUpdate() {
		Notification notification = NotificationParser.parse(null, new byte[] {0, NotificationParser.TRANSITION_UPDATE, 0, 0});
		Assert.assertTrue("Must be TransitionUpdate", notification.getClass().equals(TransitionUpdate.class));
	}

	
	@Test(expected = RuntimeException.class)
	public void transitionUpdateFailed() {
		NotificationParser.parse(null, new byte[] {0, NotificationParser.TRANSITION_UPDATE, 1, 1});
	}

	
	@Test
	public void positionUpdate() {
		Notification notification = NotificationParser.parse(null, 
				new byte[] {0, NotificationParser.POSITION_UPDATE, 1, 36, 0, 0, 0, 0, 0, 0, 71});
		Assert.assertTrue("Must be PositionUpdate", notification.getClass().equals(PositionUpdate.class));
	}
}
