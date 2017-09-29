package de.pdbm.janki.ble;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * BLE-Nachrichtenformat für Anki als Byte-Array.
 * 
 * Erstes Byte: Anzahl der Bytes
 * Zweites Byte: Identifier
 * Rest: Payload
 * 
 * <p>
 * Originaldokumentation:
 * <a href="https://github.com/anki/drive-sdk/blob/master/include/ankidrive/protocol.h">protocol.h</a>
 * </p>
 * 
 * @author bernd
 *
 */
class Message {

	private static final byte SET_SPEED = 0x24;
	
	
	
	/**
	 * Gibt die Message zum Setzen des SDK-Modes auf 1 zurück.
	 * 
	 * Muss nach dem Connecten eines Devices immer gemacht werden.
	 * 
	 * @return Message representing 'set sdk mode to 1'
	 * 
	 */
	public static byte[] getSdkMode() {
		return new byte[] {3, -112, 1, 1};
	}
	
	public static byte[] getSpeed(short speed) {
		return getSpeed(speed, (short) 10000);
	}
	
	public static byte[] getSpeed(short speed, short acceleration) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream( );
			os.write(new byte[] {6, SET_SPEED});
			os.write(shortToBytes(speed));
			os.write(shortToBytes(acceleration));
			os.write(new byte[] {0});
			return os.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	private static byte[] shortToBytes(short value) {
		byte[] bytes = new byte[2];
		bytes[0] = (byte)(value & 0xff);
		bytes[1] = (byte)((value >> 8) & 0xff);
		return bytes;
	}
}
