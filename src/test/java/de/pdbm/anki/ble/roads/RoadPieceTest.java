package de.pdbm.anki.ble.roads;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.pdbm.janki.ble.roads.CornerRoadPiece;
import de.pdbm.janki.ble.roads.RoadPiece;

public class RoadPieceTest {

	@Test
	public void countRoadPieces() {
		List<RoadPiece> all = RoadPiece.all();
		Assert.assertEquals("Falsche Anzahl Schienen (muss 14 sein).", 14, all.size());
	}
	
	@Test
	public void doesCorner17existAndIsCorner() {
		Assert.assertTrue("Schiene 17 ist keine Kurve.", RoadPiece.getRoadPieceForId(17) instanceof CornerRoadPiece);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void roadPieceDoesNotExist() {
		RoadPiece.getRoadPieceForId(1);
		
	}
}
