package de.pdbm.janki.ble.roads;

import java.util.ArrayList;
import java.util.List;

public class FinishRoadPiece extends RoadPiece {

	private FinishRoadPiece(int id) {
		super(id);
	}

	@SuppressWarnings("serial")
	private static List<FinishRoadPiece> all = new ArrayList<FinishRoadPiece>() {{
		add(new FinishRoadPiece(34));
	}};

	static List<FinishRoadPiece> getAll() {
		return all;
	}
	
}
