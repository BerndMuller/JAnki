package de.pdbm.janki.ble.roads;

import java.util.ArrayList;
import java.util.List;

public class IntersectionRoadPiece extends RoadPiece {
	
	private IntersectionRoadPiece(int id) {
		super(id);
	}

	@SuppressWarnings("serial")
	private static List<IntersectionRoadPiece> all = new ArrayList<IntersectionRoadPiece>() {{
		add(new IntersectionRoadPiece(10));
	}};

	static List<IntersectionRoadPiece> getAll() {
		return all;
	}
}
