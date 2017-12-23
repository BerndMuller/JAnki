package de.pdbm.janki.tracking;

import org.junit.Assert;
import org.junit.Test;

public class DirectionTest {

	@Test
	public void increment() {
		Assert.assertSame(Direction.NEGATIVE_Y, Direction.POSITIVE_X.increment());
		Assert.assertSame(Direction.NEGATIVE_X, Direction.NEGATIVE_Y.increment());
		Assert.assertSame(Direction.POSITIVE_Y, Direction.NEGATIVE_X.increment());
		Assert.assertSame(Direction.POSITIVE_X, Direction.POSITIVE_Y.increment());
	}

	@Test
	public void decrement() {
		Assert.assertSame(Direction.POSITIVE_Y, Direction.POSITIVE_X.decrement());
		Assert.assertSame(Direction.POSITIVE_X, Direction.NEGATIVE_Y.decrement());
		Assert.assertSame(Direction.NEGATIVE_Y, Direction.NEGATIVE_X.decrement());
		Assert.assertSame(Direction.NEGATIVE_X, Direction.POSITIVE_Y.decrement());
	}

}
