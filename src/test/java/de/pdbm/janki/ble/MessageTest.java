package de.pdbm.janki.ble;

import java.util.Arrays;

import org.junit.Test;

public class MessageTest {

	@Test
	public void checkSpeedWithAccelerationMessage() {
		System.out.println("Bytes: " + Arrays.toString(Message.speedMessage((short) 100, (short) 1000)));
	}
}
