package de.pdbm.janki.ble;

import de.pdbm.janki.ble.roads.RoadPiece;

/**
 * Parsen der BLE-Nachrichten.
 * <p>
 * 
 * 
 * <a href="https://github.com/anki/drive-sdk/blob/master/include/ankidrive/protocol.h">protocol.h</a>
 * 
 * @author bernd
 *
 */
public class NotificationParser {

	
	private static final byte POSITION_UPDATE   = 0x27; // 39
	private static final byte TRANSITION_UPDATE = 0x29; // 41
	// am Ende immer {7, 54, ...} 0x36 und {3, 77, ...} 0x4D
	
	
	private NotificationParser() {
	}

	/**
	 * Parses the Anki BLE message for some vehicle and returns it as a sub type of {@link Notification}.
	 * 
	 * @param vehicle the source of this notification
	 * @param bytes the Anki BLE message
	 * @return the Notification sub type
	 * 
	 */
	public static Notification parse(Vehicle vehicle, byte[] bytes) {
		switch (bytes[1]) {
		
		case TRANSITION_UPDATE:
			// road_piece_idx road_piece_idx_prev
			//System.out.println("Transition update: " + bytes[2] + " " + bytes[3]);
			return null;
			
		case POSITION_UPDATE:
			/* from protocol.h:
			typedef struct anki_vehicle_msg_localization_position_update {
			    uint8_t     size;
			    uint8_t     msg_id;
			    uint8_t     location_id;
			    uint8_t     road_piece_id;
			    float       offset_from_road_center_mm;
			    uint16_t    speed_mm_per_sec;
			    uint8_t     parsing_flags;
			    uint8_t     last_recv_lane_change_cmd_id;
			    uint8_t     last_exec_lane_change_cmd_id;
			    uint16_t    last_desired_horizontal_speed_mm_per_sec;
			    uint16_t    last_desired_speed_mm_per_sec;
			}
			*/
			
			// location_id road_piece_id
			
			RoadPiece roadPiece = RoadPiece.getRoadPieceForId(bytes[3]);
			System.out.println("Offset: " + bytesToFloat(bytes[4], bytes[5], bytes[6], bytes[7]));
			return new PositionUpdate(vehicle, bytes[2], roadPiece);
			
		default:
			return null;
		}
	}
	
	public static float bytesToFloat(byte byte0, byte byte1, byte byte2, byte byte3) {
		int asInt = (byte0 & 0xFF) | ((byte1 & 0xFF) << 8) | ((byte2 & 0xFF) << 16) | ((byte3 & 0xFF) << 24);
		return Float.intBitsToFloat(asInt);	
	}
	
}
