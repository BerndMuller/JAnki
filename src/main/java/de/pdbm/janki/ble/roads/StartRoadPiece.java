package de.pdbm.janki.ble.roads;

import java.util.ArrayList;
import java.util.List;

public class StartRoadPiece  extends RoadPiece {
	
	private StartRoadPiece(int id) {
		super(id);
	}

	@SuppressWarnings("serial")
	private static List<StartRoadPiece> all = new ArrayList<StartRoadPiece>() {{
		add(new StartRoadPiece(33));
	}};

	static List<StartRoadPiece> getAll() {
		return all;
	}
}
