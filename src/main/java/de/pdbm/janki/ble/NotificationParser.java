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
			// location_id road_piece_id
			// todo copy from protocol.h
			RoadPiece roadPiece = RoadPiece.getRoadPieceForId(bytes[3]);
			return new PositionUpdate(vehicle, bytes[2], roadPiece);
			
		default:
			return null;
		}
	}
	
}
