package de.pdbm.janki.ble;

import org.junit.Assert;
import org.junit.Test;

import de.pdbm.janki.ble.Vehicle.Model;

public class ModelTest {

	@Test
	public void checkSomeValues() {
		// kann im Augenblick nur Groundshock, Skull testen
		Assert.assertEquals("Modell muss 'Groundshock' sein", Model.GROUNDSHOCK, Model.getModel(8));
		Assert.assertEquals("Modell muss 'Skull' sein", Model.SKULL, Model.getModel(9));
	}
}
