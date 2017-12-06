package de.pdbm.janki.core;
import org.junit.Assert;
import org.junit.Test;

import de.pdbm.janki.core.RoadPiece;

public class RoadPieceTest {

	
	@Test
	public void start() {
		Assert.assertSame("Muss START sein", RoadPiece.valueOf(33), RoadPiece.START);
	}

	@Test
	public void finish() {
		Assert.assertSame("Muss FINISH sein", RoadPiece.valueOf(34), RoadPiece.FINISH);
	}

	@Test
	public void straight() {
		Assert.assertSame("Muss STRAIGHT sein", RoadPiece.valueOf(36), RoadPiece.STRAIGHT);
		Assert.assertSame("Muss STRAIGHT sein", RoadPiece.valueOf(39), RoadPiece.STRAIGHT);
		Assert.assertSame("Muss STRAIGHT sein", RoadPiece.valueOf(40), RoadPiece.STRAIGHT);
		Assert.assertSame("Muss STRAIGHT sein", RoadPiece.valueOf(48), RoadPiece.STRAIGHT);
		Assert.assertSame("Muss STRAIGHT sein", RoadPiece.valueOf(51), RoadPiece.STRAIGHT);
	}

	@Test
	public void corner() {
		Assert.assertSame("Muss CORNER sein", RoadPiece.valueOf(17), RoadPiece.CORNER);
		Assert.assertSame("Muss CORNER sein", RoadPiece.valueOf(18), RoadPiece.CORNER);
		Assert.assertSame("Muss CORNER sein", RoadPiece.valueOf(20), RoadPiece.CORNER);
		Assert.assertSame("Muss CORNER sein", RoadPiece.valueOf(23), RoadPiece.CORNER);
		Assert.assertSame("Muss CORNER sein", RoadPiece.valueOf(24), RoadPiece.CORNER);
		Assert.assertSame("Muss CORNER sein", RoadPiece.valueOf(27), RoadPiece.CORNER);
	}

	@Test
	public void intersection() {
		Assert.assertSame("Muss INTERSECTION sein", RoadPiece.valueOf(10), RoadPiece.INTERSECTION);
	}

	@Test(expected = IllegalArgumentException.class)
	public void unknown() {
		RoadPiece.valueOf(0);
	}

}
