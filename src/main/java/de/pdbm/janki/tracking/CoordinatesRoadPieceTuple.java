package de.pdbm.janki.tracking;

import de.pdbm.janki.core.RoadPiece;

public final class CoordinatesRoadPieceTuple {

	public final int x;
	public final int y;
	public final RoadPiece rp;
	public final char c; // ASCII art: '-', '|', '/', '\'

	public CoordinatesRoadPieceTuple(int x, int y, RoadPiece rp, char c) {
		this.x = x;
		this.y = y;
		this.rp = rp;
		this.c = c;
	}

	public CoordinatesRoadPieceTuple shift(int x, int y) {
		return new CoordinatesRoadPieceTuple(this.x + x, this.y + y, rp, c);
	}

	@Override
	public String toString() {
		return "((" + x + "," + y + ")," + rp + ", " + c + ") ";
	}

}
