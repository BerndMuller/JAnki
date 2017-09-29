package de.pdbm.janki.ble.roads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Superclass of all road pieces.
 * <p>
 * There is no (single) documentation about all road piece Ids used by Anki (at least to my knowledge).
 * The Ids are gathered from different resources and are used in the subclasses.
 * <p>
 * Subclasses:
 * <ul>
 *   <li>{@link CornerRoadPiece}</li>
 *   <li>{@link FinishRoadPiece}</li>
 *   <li>{@link IntersectionRoadPiece}</li>
 *   <li>{@link StartRoadPiece}</li>
 *   <li>{@link StraightRoadPiece}</li>
 * </ul>
 *  
 * @author bernd
 *
 */
public abstract class RoadPiece {
	
	private static final List<RoadPiece> all;
	
	static {
		List<RoadPiece> tmp = new ArrayList<>();
		tmp.addAll((List<? extends RoadPiece>) CornerRoadPiece.getAll());
		tmp.addAll((List<? extends RoadPiece>) FinishRoadPiece.getAll());
		tmp.addAll((List<? extends RoadPiece>) IntersectionRoadPiece.getAll());
		tmp.addAll((List<? extends RoadPiece>) StartRoadPiece.getAll());
		tmp.addAll((List<? extends RoadPiece>) StraightRoadPiece.getAll());
		all = Collections.unmodifiableList(tmp);
	}

	private final int id;

	protected RoadPiece(int id) {
		this.id = id;
	}

	public static RoadPiece getRoadPieceForId(int id) {
		return all.stream().filter(r -> r.id == id).findFirst().orElseThrow(() -> new IllegalArgumentException("Road Piece Id not valid"));
	}
	
	public static List<RoadPiece> all() {
		return all;
	}
	
	public int getId() {
		return id;
	}
	
}
