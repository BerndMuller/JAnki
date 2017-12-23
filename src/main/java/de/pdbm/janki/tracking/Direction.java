package de.pdbm.janki.tracking;

/**
 * Direction of a vehicle in a two dimensional coordinate system.
 * 
 * <p>
 * Order must be fixed and clockwise rotation to guarantee increment/decrement semantics.
 * 
 * @author bernd
 *
 */
public enum Direction {
	
	POSITIVE_X, NEGATIVE_Y, NEGATIVE_X, POSITIVE_Y; // order must be fixed, do not change!
	
	/**
	 * @return next direction clockwise
	 */
	public Direction increment() {
		return Direction.values()[(this.ordinal() + 1) % 4];
	}

	/**
	 * @return next direction counterclockwise
	 */
	public Direction decrement() {
		return Direction.values()[(this.ordinal() + 3) % 4];
	}

	
}
