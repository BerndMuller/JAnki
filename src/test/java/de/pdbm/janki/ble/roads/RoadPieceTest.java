package de.pdbm.janki.ble.roads;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

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
	
	@Test
	public void startRoadPieceIsSingleton() {
		RoadPiece start1 = StartRoadPiece.getAll().get(0);
		RoadPiece start2 = RoadPiece.getRoadPieceForId(33);
		Assert.assertEquals("Muss Startbahn sein.", start1, start2);
	}
	
	@Test
	public void finishRoadPieceIsSingleton() {
		RoadPiece finish1 = FinishRoadPiece.getAll().get(0);
		RoadPiece finish2 = RoadPiece.getRoadPieceForId(34);
		Assert.assertEquals("Muss Ziel sein.", finish1, finish2);
	}
	
	
}
