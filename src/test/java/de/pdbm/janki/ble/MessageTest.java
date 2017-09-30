package de.pdbm.janki.ble;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class MessageTest {

	@Test
	public void checkSpeedWithAccelerationMessage() {
		//System.out.println("Bytes: " + Arrays.toString(Message.speedMessage((short) 100, (short) 1000)));
		Assert.assertTrue(Arrays.equals(new byte[] {6, 36, 100, 0, -24, 3, 0}, Message.speedMessage((short) 100, (short) 1000)));
	}
}
