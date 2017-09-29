package de.pdbm.janki.ble.roads;

import java.util.ArrayList;
import java.util.List;

public class StraightRoadPiece extends RoadPiece {
	
	private StraightRoadPiece(int id) {
		super(id);
	}

	@SuppressWarnings("serial")
	private static List<StraightRoadPiece> all = new ArrayList<StraightRoadPiece>() {{
		add(new StraightRoadPiece(36));
		add(new StraightRoadPiece(39));
		add(new StraightRoadPiece(40));
		add(new StraightRoadPiece(48));
		add(new StraightRoadPiece(51));
	}};

	static List<StraightRoadPiece> getAll() {
		return all;
	}
}
