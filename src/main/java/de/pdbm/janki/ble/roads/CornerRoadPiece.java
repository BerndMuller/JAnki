package de.pdbm.janki.ble.roads;

import java.util.ArrayList;
import java.util.List;

public class CornerRoadPiece extends RoadPiece {

	private CornerRoadPiece(int id) {
		super(id);
	}

	@SuppressWarnings("serial")
	private static List<CornerRoadPiece> all = new ArrayList<CornerRoadPiece>() {{
		add(new CornerRoadPiece(17));
		add(new CornerRoadPiece(18));
		add(new CornerRoadPiece(20));
		add(new CornerRoadPiece(23));
		add(new CornerRoadPiece(24));
		add(new CornerRoadPiece(27));
	}};

	static List<CornerRoadPiece> getAll() {
		return all;
	}
	
}
