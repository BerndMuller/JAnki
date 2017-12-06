package de.pdbm.janki.core;


/**
 * Enumeration to abstract from arbitray road piece IDs assigned by Anki. 
 * 
 * @author bernd
 *
 */
public enum RoadPiece {

	START(33), FINISH(34), STRAIGHT(36, 39, 40, 48, 51), CORNER(17, 18, 20, 23, 24, 27), INTERSECTION(10);
	
	@SuppressWarnings("unused")
	private int[] ids;

	private RoadPiece(int... ids) {
		this.ids = ids;
	}
	
	public static RoadPiece valueOf(int id) {
		switch (id) {
		case 33:
			return START;
		case 34:
			return FINISH;
		case 36:
		case 39:
		case 40:
		case 48:
		case 51:
			return STRAIGHT;
		case 17:
		case 18:
		case 20:
		case 23:
		case 24:
		case 27:
			return CORNER;
		case 10:
			return INTERSECTION;
		}
		throw new IllegalArgumentException("unknown road piece id");
	}
	
}
