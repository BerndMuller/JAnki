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
	private static final byte CHANGE_LANE= 0x25;
	private static final byte SET_OFFSET_FROM_ROAD_CENTER = 0x2c;
	
	
	
	/**
	 * Returns message representing 'set sdk mode to 1'.
	 * 
	 * This message must be send after connecting a device.
	 * 
	 * @return message representing 'set sdk mode to 1'
	 * 
	 */
	public static byte[] getSdkMode() {
		return new byte[] {3, -112, 1, 1};
	}
	
	/**
	 * 
	 * @param speed the speed
	 * @return message representing a speed message using the given speed
	 */
	public static byte[] speedMessage(short speed) {
		return speedMessage(speed, (short) 10000);
	}
	
	/**
	 * Returns a speed message using the given speed and acceleration.
	 * 
	 * @param speed the speed 
	 * @param acceleration the acceleration
	 * @return message representing a speed message using the given speed and acceleration
	 * 
	 */
	public static byte[] speedMessage(short speed, short acceleration) {
		/* from protocol.h:
		typedef struct anki_vehicle_msg_set_speed {
		    uint8_t     size;
		    uint8_t     msg_id;
		    int16_t     speed_mm_per_sec;  // mm/sec
		    int16_t     accel_mm_per_sec2; // mm/sec^2
		    uint8_t     respect_road_piece_speed_limit;
		}
		*/
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
	
	
	public static byte[] changeLaneMessage(short speed, short acceleration, float offset)  {
		/* from protocol.h: 
		typedef struct anki_vehicle_msg_change_lane {
	    uint8_t     size;
	    uint8_t     msg_id;
	    uint16_t    horizontal_speed_mm_per_sec;
	    uint16_t    horizontal_accel_mm_per_sec2; 
	    float       offset_from_road_center_mm;
	    uint8_t     hop_intent;
	    uint8_t     tag;
		}
		*/
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream( );
			os.write(new byte[] {11, CHANGE_LANE});
			os.write(shortToBytes(speed));
			os.write(shortToBytes(acceleration));
			os.write(floatToBytes(offset));
			os.write(new byte[] {0});
			os.write(new byte[] {0});
			return os.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * Must be used before a change lane message to calibrate.
	 * 
	 * 
	 * @return the offset from road center message
	 */
	public static byte[] setOffsetFromRoadCenter() {
		/* from protocol.h:
		typedef struct anki_vehicle_msg_set_offset_from_road_center {
		    uint8_t     size;
		    uint8_t     msg_id;
		    float       offset_mm;
		}
		*/
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream( );
			os.write(new byte[] {5, SET_OFFSET_FROM_ROAD_CENTER});
			os.write(floatToBytes(0.0f));
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
	
	
	private static byte[] floatToBytes(float value) {
		int bits = Float.floatToIntBits(value);
		byte[] bytes = new byte[4];
		bytes[0] = (byte)(bits & 0xff);
		bytes[1] = (byte)((bits >> 8) & 0xff);
		bytes[2] = (byte)((bits >> 16) & 0xff);
		bytes[3] = (byte)((bits >> 24) & 0xff);
		return bytes;
	}
}
