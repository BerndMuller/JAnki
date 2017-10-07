package de.pdbm.janki.ble;

import org.junit.Assert;
import org.junit.Test;

public class NotificationParserTest {

	@Test
	public void transitionUpdate() {
		Notification notification = NotificationParser.parse(null, new byte[] {0, NotificationParser.TRANSITION_UPDATE, 36, 0});
		Assert.assertTrue("Must be TransitionUpdate", notification.getClass().equals(TransitionUpdate.class));
	}

	@Test
	public void positionUpdate() {
		Notification notification = NotificationParser.parse(null, 
				new byte[] {0, NotificationParser.POSITION_UPDATE, 1, 36, 0, 0, 0, 0, 0, 0, 71});
		Assert.assertTrue("Must be PositionUpdate", notification.getClass().equals(PositionUpdate.class));
	}
}
